package com.jtransc.ast.treeshaking

import com.jtransc.annotation.JTranscAddFileList
import com.jtransc.annotation.JTranscKeep
import com.jtransc.annotation.JTranscMethodBodyList
import com.jtransc.ast.*
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.template.Minitemplate
import java.util.*

class ClassTree(val program: AstProgram) {
	val childrenList = hashMapOf<AstClass, ArrayList<AstClass>>()

	fun getChildren(clazz: AstClass): ArrayList<AstClass> = childrenList.getOrPut(clazz) { arrayListOf() }
	fun getDescendants(clazz: AstClass): List<AstClass> = getChildren(clazz) + getChildren(clazz).flatMap { getChildren(it) }

	fun add(clazz: AstClass) {
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

fun TreeShaking(program: AstProgram, target: String, trace: Boolean): AstProgram {
	val SHAKING_TRACE = trace

	val main = program[program.entrypoint].getMethodSure("main", AstTypeBuild { METHOD(VOID, ARRAY(STRING)) }.desc)

	class TreeShakingClass(val oldprogram: AstProgram, val target: String) {
		val newprogram = AstProgram(program.configResourcesVfs, program.configEntrypoint, program.types)
		val processed = hashSetOf<Any>()
		val newclasses = hashMapOf<FqName, AstClass>()
		val classtree = ClassTree(newprogram)

		fun addTemplateReferences(template: String, templateReason: String) {
			for (ref in GetTemplateReferences(oldprogram, template)) {
				if (SHAKING_TRACE) println("TEMPLATEREF: $ref")
				when (ref) {
					is CommonTagHandler.CLASS -> addBasicClass(ref.clazz.name, reason = "template $templateReason")
					is CommonTagHandler.SINIT -> addBasicClass(ref.method.containingClass.name, reason = "template $templateReason")
					is CommonTagHandler.CONSTRUCTOR -> addMethod(ref.method.ref, reason = "template $templateReason")
					is CommonTagHandler.FIELD -> addField(ref.field.ref, reason = "template $templateReason")
					is CommonTagHandler.METHOD -> addMethod(ref.method.ref, reason = "template $templateReason")
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
					annotations = oldclazz.annotations
				)

				newclasses[fqname] = newclazz

				newprogram.add(newclazz)

				for (impl in oldclazz.implementing) _addMiniBasicClass(impl, reason = "implementing $fqname")
				if (oldclazz.extending != null) _addMiniBasicClass(oldclazz.extending, reason = "extending $fqname") else null

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

				for (impl in oldclazz.implementing) addBasicClass(impl, reason = "implementing $fqname")
				if (oldclazz.extending != null) addBasicClass(oldclazz.extending, reason = "extending $fqname") else null

				if (oldclazz.annotationsList.contains<JTranscKeep>()) {
					addFullClass(fqname, reason = "$fqname+@JTranscKeep")
				}

				for (field in oldclazz.fields) {
					if (field.annotationsList.contains<JTranscKeep>()) addField(field.ref, reason = "<field>@JTranscKeep")
				}

				for (method in oldclazz.methods) {
					if (method.annotationsList.contains<JTranscKeep>()) addMethod(method.ref, reason = "<method>@JTranscKeep")
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

				//for (file in newclazz.annotationsList.getAllTyped<JTranscAddFile>()) {
				for (file in newclazz.annotationsList.getTypedList(JTranscAddFileList::value)) {
					if (file.process && file.target == target) {
						val possibleFiles = listOf(file.prepend, file.append, file.prependAppend)
						for (pf in possibleFiles.filter { !it.isNullOrEmpty() }) {
							val filecontent = program.resourcesVfs[pf].readString()
							addTemplateReferences(filecontent, templateReason = "JTranscAddFileList: $pf")
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
				id = oldfield.id,
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

			addAnnotations(newfield.annotationsList, reason = "field $fieldRef")

			for (type in newfield.type.getRefTypesFqName()) {
				addBasicClass(type, reason = "field $fieldRef")
			}
		}

		fun addAnnotations(annotations: AstAnnotationList, reason: String) {
			for (annotation in annotations.list) {
				for (ref in annotation.getRefTypesFqName()) {
					addBasicClass(ref, "annotation $reason")
				}
			}
		}

		fun addMethod(methodRef: AstMethodRef, reason: String) {
			if (methodRef in processed) return
			if (SHAKING_TRACE) println("methodRef: $methodRef. Reason: $reason")
			processed += methodRef
			val oldmethod = program[methodRef]!!
			val oldclazz = program[methodRef]!!.containingClass
			val newclazz = addBasicClass(methodRef.containingClass, reason)
			val newmethod = AstMethod(
				containingClass = newclazz,
				id = oldmethod.id,
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

			for (ref in methodRef.type.getRefTypesFqName()) addBasicClass(ref, reason = "$methodRef")

			var dependenciesInBody = true

			JTranscMethodBodyList::class.java // @HACK intelliJ remove JTranscMethodBodyList with just JTranscMethodBodyList::value

			for (methodBody in newmethod.annotationsList.getTypedList(JTranscMethodBodyList::value)) {
				if (methodBody.target == target) {
					addTemplateReferences(methodBody.value.joinToString("\n"), "methodBody=$newmethod")
					if (methodBody.cond.isNullOrEmpty()) {
						dependenciesInBody = false
					}
				}
			}

			if (dependenciesInBody) {
				for (dep in oldmethod.bodyDependencies.classes) addBasicClass(dep.name, reason = "dependenciesInBody $methodRef")
				for (dep in oldmethod.bodyDependencies.fields) addField(dep, reason = "dependenciesInBody $methodRef")
				for (dep in oldmethod.bodyDependencies.methods) addMethod(dep, reason = "dependenciesInBody $methodRef")
			}

			addAnnotations(newmethod.annotationsList, reason = "method $methodRef")
			for (paramAnnotation in newmethod.parameterAnnotations) {
				addAnnotations(AstAnnotationList(paramAnnotation), reason = "method $methodRef")
			}

			checkTreeNewMethod(newmethod)
		}

		// This should propagate methods to ancestors and descendants
		// @TODO: We should really include ancestors? Even when they are not referenced? For now, let's play it safe.
		private fun checkTreeNewClass(newclazz: AstClass) {
			// ancestors are known (descendants may not have been built completely)
			val relatedClasses = listOf(newclazz) + newclazz.ancestors + newclazz.allInterfacesInAncestors + classtree.getDescendants(newclazz)

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
			val relatedClasses = listOf(newclazz) + newclazz.ancestors + newclazz.allInterfacesInAncestors + classtree.getDescendants(newclazz)
			//val relatedClasses = newclazz.ancestors + classtree.getDescendants(newclazz)
			// ancestors are known (descendants may not have been built completely)

			for (relatedClass in relatedClasses) {
				val rmethod = oldprogram[relatedClass.name].getMethod(newmethod.name, newmethod.desc)
				if (rmethod != null) addMethod(rmethod.ref, "checkTreeNewMethod $newmethod")
			}
		}
	}

	// The unshaked program should be cached, in a per class basis, since it doesn't have information about other classes.
	val shaking = TreeShakingClass(oldprogram = program, target = target)
	shaking.addMethod(main.ref, "<ENTRY>")
	//shaking.addMethod(program[FqName("java.lang.reflect.InvocationHandler")].getMethods("invoke").first().ref, "<ENTRY>")

	when (target) {
	// HACK
		"cpp" -> {
			val filecontent = program.resourcesVfs["cpp/Base.cpp"].readString()
			shaking.addTemplateReferences(filecontent, templateReason = "<base target>: cpp/Base.cpp")
		}
	}

	return shaking.newprogram;
}