package com.jtransc.ast.treeshaking

import com.jtransc.annotation.JTranscAddFileList
import com.jtransc.annotation.JTranscKeepConstructors
import com.jtransc.annotation.JTranscMethodBodyList
import com.jtransc.annotation.haxe.HaxeAddFilesTemplate
import com.jtransc.annotation.haxe.HaxeMethodBodyList
import com.jtransc.ast.*
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.error.invalidOp
import com.jtransc.gen.TargetName
import com.jtransc.plugin.JTranscPlugin
import java.util.*

class ClassTree(val SHAKING_TRACE: Boolean, val program: AstProgram) {
	val childrenList = hashMapOf<AstClass, ArrayList<AstClass>>()

	fun getChildren(clazz: AstClass): ArrayList<AstClass> {
		if (clazz.program != program) invalidOp("TreeShaking Internal Error: Invalid program [1]")
		return childrenList.getOrPut(clazz) { arrayListOf() }
	}

	fun getDescendants(clazz: AstClass): List<AstClass> {
		if (clazz.program != program) invalidOp("TreeShaking Internal Error: Invalid program [2]")
		return getChildren(clazz) + getChildren(clazz).flatMap { getChildren(it) }
	}

	fun getDescendantsAndAncestors(clazz: AstClass): List<AstClass> {
		return getDescendants(clazz).flatMap { it.thisAncestorsAndInterfaces }
	}

	fun add(clazz: AstClass) {
		if (clazz.program != program) invalidOp("TreeShaking Internal Error: Invalid program [3]")
		if (SHAKING_TRACE) println("  :: $clazz :: ${clazz.extending} :: ${clazz.implementing}")
		if (clazz.extending != null) getChildren(program[clazz.extending]) += clazz
		for (impl in clazz.implementing) getChildren(program[impl]) += clazz
	}

	fun dump() {
		for ((clazz, children) in childrenList) {
			println("* $clazz")
			println("  - $children")
		}
	}
}

class TreeShakingApi(
	val oldprogram: AstProgram,
	val target: String,
	val trace: Boolean,
	val plugins: List<JTranscPlugin>
) {
	val program = oldprogram
	val targetName = TargetName(target)
	val SHAKING_TRACE = trace

	val main = program[program.entrypoint].getMethodSure("main", AstTypeBuild { METHOD(VOID, ARRAY(STRING)) }.desc)

	val newprogram = AstProgram(
		program.configResourcesVfs, program.configEntrypoint, program.types, program.injector
	).apply {
		this.lastClassId = oldprogram.lastClassId
		this.lastFieldId = oldprogram.lastFieldId
		this.lastMethodId = oldprogram.lastMethodId
	}
	val processed = hashSetOf<Any>()
	val newclasses = hashMapOf<FqName, AstClass>()
	val classtree = ClassTree(SHAKING_TRACE, newprogram)

	val classesWithKeepConstructors = oldprogram.classes.filter { it.annotationsList.contains<JTranscKeepConstructors>() }.map { it.name }.toSet()
	val actualClassesWithKeepConstructors = oldprogram.classes.filter {
		it.name in classesWithKeepConstructors ||
			it.annotationsList.list.any { it.type.name in classesWithKeepConstructors }
	}.map { it.name }.toSet()
	// oldclazz.annotationsList.list.any { it.type.name in classesWithKeepConstructors })

	fun addTemplateReferences(template: String, currentClass: FqName, templateReason: String) {
		val refs = GetTemplateReferences(oldprogram, template, currentClass)
		val reason = "template $templateReason"
		for (ref in refs) {
			if (SHAKING_TRACE) println("TEMPLATEREF: $ref")
			when (ref) {
				is CommonTagHandler.CLASS -> addBasicClass(ref.clazz.name, reason = reason)
				is CommonTagHandler.SINIT -> addBasicClass(ref.method.containingClass.name, reason = reason)
				is CommonTagHandler.CONSTRUCTOR -> {
					addBasicClass(ref.ref.classRef.name, reason = reason)
					addMethod(ref.method.ref, reason = reason)
				}
				is CommonTagHandler.FIELD -> {
					addBasicClass(ref.ref.classRef.name, reason = reason)
					addField(ref.field.ref, reason = reason)
				}
				is CommonTagHandler.METHOD -> {
					addBasicClass(ref.ref.classRef.name, reason = reason)
					addMethod(ref.method.ref, reason = reason)
				}
			}
		}
	}

	fun addFullClass(fqname: FqName, reason: Any? = null) {
		val oldclazz = program[fqname]
		//val newclazz = addBasicClass(fqname)
		for (method in oldclazz.methods) addMethod(method.ref, reason = "fullclass $fqname")
		for (field in oldclazz.fields) addField(field.ref, reason = "fullclass $fqname")
	}

	private fun _addMiniBasicClass(fqname: FqName, reason: Any? = null): AstClass {
		if (fqname !in newclasses) {
			if (SHAKING_TRACE) println("_addMiniBasicClass: $fqname. Reason: $reason")

			val oldclazz = program[fqname]
			val newclazz = AstClass(
				source = oldclazz.source,
				program = newprogram,
				name = fqname,
				modifiers = oldclazz.modifiers,
				extending = oldclazz.extending,
				implementing = oldclazz.implementing,
				annotations = oldclazz.annotations//,
				//classId = oldclazz.classId
			)

			newclasses[fqname] = newclazz

			newprogram.add(newclazz)

			for (impl in oldclazz.implementing) _addMiniBasicClass(impl, reason = "implementing $fqname")
			if (oldclazz.extending != null) _addMiniBasicClass(oldclazz.extending, reason = "extending $fqname")

			classtree.add(newclazz)
		}
		return newclasses[fqname]!!
	}

	val initializedClasses = hashSetOf<FqName>()

	fun addBasicClass(fqname: FqName, reason: String): AstClass {
		if (fqname !in initializedClasses) {
			initializedClasses += fqname

			if (SHAKING_TRACE) println("addBasicClass: $fqname. Reason: $reason")

			val oldclazz = program[fqname]
			val newclazz = _addMiniBasicClass(fqname)

			for (plugin in plugins) plugin.onTreeShakingAddBasicClass(this, fqname, oldclazz, newclazz)

			for (impl in oldclazz.implementing) addBasicClass(impl, reason = "implementing $fqname")
			if (oldclazz.extending != null) addBasicClass(oldclazz.extending, reason = "extending $fqname")

			val relatedTypes = oldclazz.getAllRelatedTypes().map { it.name }

			val keepConstructors = relatedTypes.any { it in actualClassesWithKeepConstructors }

			if (keepConstructors) {
				for (constructor in oldclazz.constructors) {
					addMethod(constructor.ref, reason = "<method>@JTranscKeepConstructors")
				}
				//println("keepConstructors:")
			}

			if (oldclazz.keep) {
				addFullClass(fqname, reason = "$fqname+@JTranscKeep")
			}

			for (field in oldclazz.fields) {
				if (field.keep) addField(field.ref, reason = "<field>@JTranscKeep")
			}

			for (method in oldclazz.methods) {
				if (method.keep) addMethod(method.ref, reason = "<method>@JTranscKeep")
			}

			// Add static constructors
			for (method in oldclazz.methods.filter { it.isClassInit }) {
				addMethod(method.ref, reason = "static constructor <clinit> fqname")
			}

			addAnnotations(newclazz.annotationsList, reason = "class $fqname")

			//if (newclazz.fqname == "java.lang.Object") {
			//	invalidOp
			//}

			JTranscAddFileList::class.java

			//println(fqname)

			if (targetName.matches("haxe")) {
				val templateFiles = newclazz.annotationsList.getTyped<HaxeAddFilesTemplate>()?.value?.toList() ?: listOf()
				if (templateFiles.isNotEmpty()) {
					for (pf in templateFiles) {
						val filecontent = program.resourcesVfs[pf].readString()
						addTemplateReferences(filecontent, fqname, templateReason = "HaxeAddFilesTemplate: $pf")
					}
				}
			}


			//for (file in newclazz.annotationsList.getAllTyped<JTranscAddFile>()) {
			for (file in newclazz.annotationsList.getTypedList(JTranscAddFileList::value)) {
				if (file.process && TargetName.matches(file.target, target)) {
					val possibleFiles = listOf(file.prepend, file.append, file.prependAppend)
					for (pf in possibleFiles.filter { !it.isNullOrEmpty() }) {
						val filecontent = program.resourcesVfs[pf].readString()
						addTemplateReferences(filecontent, fqname, templateReason = "JTranscAddFileList: $pf")
					}
				}
			}

			checkTreeNewClass(newclazz)
		}
		return newclasses[fqname]!!
	}

	fun addField(fieldRef: AstFieldRef, reason: String) {
		if (fieldRef in processed) return
		if (SHAKING_TRACE) println("addField: $fieldRef. Reason: $reason")
		processed += fieldRef
		val oldfield = oldprogram[fieldRef]
		val oldfieldRef = oldfield.ref
		val oldfield2 = oldfield.containingClass[oldfieldRef.withoutClass]

		addBasicClass(fieldRef.containingClass, reason = "field $fieldRef")
		val newclazz = addBasicClass(oldfield2.containingClass.name, reason = "field $fieldRef")

		if (oldfield2.refWithoutClass in newclazz.fieldsByInfo) {
			// Already added
			return
		}

		val newfield = AstField(
			containingClass = newclazz,
			//id = oldfield.id,
			name = oldfield.name,
			type = oldfield.type,
			annotations = oldfield.annotations,
			genericSignature = oldfield.genericSignature,
			modifiers = oldfield.modifiers,
			types = newprogram.types,
			desc = oldfield.desc,
			constantValue = oldfield.constantValue
		)
		newclazz.add(newfield)

		for (plugin in plugins) plugin.onTreeShakingAddField(this, oldfield, newfield)

		addAnnotations(newfield.annotationsList, reason = "field $fieldRef")

		for (type in newfield.type.getRefTypesFqName()) {
			addBasicClass(type, reason = "field $fieldRef")
		}
	}

	fun addAnnotations(annotations: AstAnnotationList, reason: String) {
		for (annotation in annotations.list) {
			try {
				for (ref in annotation.getRefTypesFqName()) {
					addBasicClass(ref, "annotation $reason")
				}
			} catch (e: Throwable) {
				System.err.println("While adding annotations for ${annotations.containerRef}:")
				e.printStackTrace()
			}
		}
	}

	fun addMethod(methodRef: AstMethodRef, reason: String) {
		if (methodRef in processed) return
		if (SHAKING_TRACE) println("methodRef: $methodRef. Reason: $reason")
		processed += methodRef
		val oldmethod = program[methodRef] ?: invalidOp("Can't find $methodRef")
		val oldclazz = program[methodRef]?.containingClass ?: invalidOp("Can't find $methodRef : containingClass")
		val newclazz = addBasicClass(methodRef.containingClass, reason)
		val newmethod = AstMethod(
			containingClass = newclazz,
			//id = oldmethod.id,
			name = oldmethod.name,
			methodType = oldmethod.methodType,
			annotations = oldmethod.annotations,
			signature = oldmethod.signature,
			genericSignature = oldmethod.genericSignature,
			defaultTag = oldmethod.defaultTag,
			modifiers = oldmethod.modifiers,
			generateBody = oldmethod.generateBody,
			bodyRef = oldmethod.bodyRef,
			parameterAnnotations = oldmethod.parameterAnnotations,
			types = newprogram.types
		)
		//println("    -> ${oldmethod.dependencies.classes}")

		//if (methodRef.name == "testStaticTest1") println(methodRef)

		newclazz.add(newmethod)

		for (plugin in plugins) plugin.onTreeShakingAddMethod(this, oldmethod, newmethod)

		for (ref in methodRef.type.getRefTypesFqName()) addBasicClass(ref, reason = "$methodRef")

		if (targetName.matches("haxe")) {
			for (methodBody in newmethod.annotationsList.getTypedList(HaxeMethodBodyList::value)) {
				if (targetName.matches(methodBody.target)) {
					addTemplateReferences(methodBody.value, methodRef.containingClass, "methodBody=$newmethod")
				}
			}
		}

		for (methodBody in newmethod.annotationsList.getBodiesForTarget(targetName)) {
			addTemplateReferences(methodBody.value, methodRef.containingClass, "methodBody=$newmethod")
		}

		//if (methodRef.name == "_onKeyDownUp") {
		//	val bodyDependencies = oldmethod.bodyDependencies
		//	println(bodyDependencies)
		//}

		if (oldmethod.hasDependenciesInBody(targetName)) {
			for (dep in oldmethod.bodyDependencies.classes) addBasicClass(dep.name, reason = "dependenciesInBody $methodRef")
			for (dep in oldmethod.bodyDependencies.fields) addField(dep, reason = "dependenciesInBody $methodRef")
			for (dep in oldmethod.bodyDependencies.methods) addMethod(dep, reason = "dependenciesInBody $methodRef")
		}

		addAnnotations(newmethod.annotationsList, reason = "method $methodRef")
		for (paramAnnotation in newmethod.parameterAnnotations) {
			addAnnotations(AstAnnotationList(newmethod.ref, paramAnnotation), reason = "method $methodRef")
		}

		checkTreeNewMethod(newmethod)
	}

	// This should propagate methods to ancestors and descendants
	// @TODO: We should really include ancestors? Even when they are not referenced? For now, let's play it safe.
	private fun checkTreeNewClass(newclazz: AstClass) {
		// ancestors are known (descendants may not have been built completely)
		val relatedClasses = (listOf(newclazz) + newclazz.ancestors + newclazz.allInterfacesInAncestors + classtree.getDescendantsAndAncestors(newclazz)).distinct()

		val oldclazz = program[newclazz.name]

		//println(oldclazz.annotationsList.list)

		/*
        println("----------")
        println(newclazz.name)
        println(oldclazz.annotationsList.getTypedList(com.jtransc.annotation.JTranscAddFileList::value))
        println(oldclazz.annotationsList.getAllTyped<JTranscAddFileList>())
        */

		for (addFile in oldclazz.annotationsList.getTargetAddFiles(target)) {
			for (filePath in addFile.filesToProcess()) {
				val filecontent = program.resourcesVfs[filePath].readString()
				if (SHAKING_TRACE) println("PROCESSNG(TargetAddFile::${newclazz.name}): $filePath")
				addTemplateReferences(filecontent, newclazz.name, templateReason = "TargetAddFile : ${newclazz.name}")
			}
			//println(":" + addFile.prependAppend)
			//println(":" + addFile.prepend)
			//println(":" + addFile.append)
		}

		val methodRefs = arrayListOf<AstMethodWithoutClassRef>()

		for (relatedClass in relatedClasses) {
			for (newmethod in relatedClass.methods) {
				if (!newmethod.isClassOrInstanceInit) methodRefs += newmethod.ref.nameDesc
			}
		}

		for (relatedClass in relatedClasses) {
			for (mref in methodRefs) {
				val rmethod = oldprogram[relatedClass.name].getMethod(mref)
				if (rmethod != null) addMethod(rmethod.ref, "checkTreeNewClass $newclazz")
			}
		}
	}

	//
	private fun checkTreeNewMethod(newmethod: AstMethod) {
		val newclazz = newmethod.containingClass
		val relatedClasses = (newclazz.thisAncestorsAndInterfaces + classtree.getDescendantsAndAncestors(newclazz)).distinct()
		//val relatedClasses = newclazz.ancestors + classtree.getDescendants(newclazz)
		// ancestors are known (descendants may not have been built completely)

		val methods = (relatedClasses).map { relatedClass ->
			oldprogram[relatedClass.name].getMethod(newmethod.name, newmethod.desc)
			//val rmethod = oldprogram[relatedClass.name].getMethod(newmethod.name, newmethod.desc)
			//if (rmethod != null) addMethod(rmethod.ref, "checkTreeNewMethod $newmethod")
		}.filterNotNull()

		if (SHAKING_TRACE) {
			for (rclass in relatedClasses) {
				//println("  *-- $rclass")
			}
			for (rmethod in methods) {
				val methodRef = rmethod.ref
				if (methodRef !in processed) {
					println("  <-- $rmethod")
				} else {
					//println("  :-- $rmethod")
				}
			}
		}

		for (rmethod in methods) {
			addMethod(rmethod.ref, "checkTreeNewMethod $newmethod")
		}
	}
}

fun TreeShaking(program: AstProgram, target: String, trace: Boolean, plugins: List<JTranscPlugin>): AstProgram {
	// The unshaked program should be cached, in a per class basis, since it doesn't have information about other classes.
	val shaking = TreeShakingApi(program, target, trace, plugins)
	shaking.addMethod(shaking.main.ref, "<ENTRY>")
//shaking.addMethod(program[FqName("java.lang.reflect.InvocationHandler")].getMethods("invoke").first().ref, "<ENTRY>")

	when (target) {
	// HACK
		"cpp" -> {
			val filecontent = program.resourcesVfs["cpp/Base.cpp"].readString()
			shaking.addTemplateReferences(filecontent, "java.lang.Object".fqname, templateReason = "<base target>: cpp/Base.cpp")
		}
	}

	return shaking.newprogram;
}