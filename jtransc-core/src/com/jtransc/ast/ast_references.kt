package com.jtransc.ast

import com.jtransc.annotation.JTranscAddFileList
import com.jtransc.annotation.JTranscAddMembersList
import com.jtransc.annotation.JTranscMethodBodyList
import com.jtransc.annotation.haxe.*
import com.jtransc.ast.treeshaking.GetClassTemplateReferences
import com.jtransc.ast.treeshaking.TRefConfig
import com.jtransc.ast.treeshaking.TRefReason
import com.jtransc.gen.TargetName
import com.jtransc.org.objectweb.asm.Type

object References {
	private val bl = JTranscMethodBodyList::class.java

	val config = TRefConfig(TRefReason.UNKNOWN)

	fun get(clazz: AstClass, targetName: TargetName) = clazz.getClassReferences(targetName)

	fun AstClass.getClassReferences(targetName: TargetName): List<AstType.REF> {
		val me = AstType.REF(this.fqname)
		val parent = if (this.extending != null) AstType.REF(this.extending) else null
		val interfaces = this.implementing.map { AstType.REF(it) }
		val fields = this.fields.flatMap { it.getClassReferences(targetName) }
		val methods = this.methods.flatMap { it.getClassReferences(targetName) }
		val annotations = this.annotations.getClassReferences(targetName)
		val annotations2 = this.annotationsList.getClassReferencesExtra(this, this.program, targetName)
		return (listOf(me) + listOf(parent) + interfaces + fields + methods + annotations + annotations2).filterNotNull()
	}

	fun AstAnnotationList.getClassReferencesExtra(clazz: AstClass, program: AstProgram, targetName: TargetName): List<AstType.REF> {
		val out = arrayListOf<AstType.REF>()
		for (file in this.getTypedList(JTranscAddFileList::value).filter { it.process && targetName.matches(it.target) }) {
			val possibleFiles = listOf(file.prepend, file.append, file.prependAppend)
			for (pf in possibleFiles.filter { !it.isNullOrEmpty() }) {
				val filecontent = program.resourcesVfs[pf].readString()
				out += GetClassTemplateReferences(program, filecontent, clazz.name, config).map { AstType.REF(it) }
			}
		}

		for (member in this.getTypedList(JTranscAddMembersList::value).filter { targetName.matches(it.target) }) {
			out += GetClassTemplateReferences(program, member.value.joinToString("\n"), clazz.name, config).map { AstType.REF(it) }
		}

		// @TODO: This should be unified!
		if (targetName.matches("haxe")) {
			val haxeAddFilesTemplate = this.getTyped<HaxeAddFilesTemplate>()
			if (haxeAddFilesTemplate != null) {
				for (file in haxeAddFilesTemplate.value) {
					val filecontent = program.resourcesVfs[file].readString()
					out += GetClassTemplateReferences(program, filecontent, clazz.name, config).map { AstType.REF(it) }
				}
			}
			val haxeAddMembers = this.getTyped<HaxeAddMembers>()
			if (haxeAddMembers != null) {
				out += GetClassTemplateReferences(program, haxeAddMembers.value.joinToString("\n"), clazz.name, config).map { AstType.REF(it) }
			}
		}
		return out
	}

	fun AstField.getClassReferences(targetName: TargetName): List<AstType.REF> {
		return this.genericType.getRefClasses() + this.annotations.getClassReferences(targetName)
	}

	fun AstMethod.getClassReferences(targetName: TargetName): List<AstType.REF> {
		val clazzFqname = this.containingClass.name
		val signatureRefs = this.genericMethodType.getRefClasses()
		val annotations = this.annotations.getClassReferences(targetName)
		val parameterAnnotations = this.parameterAnnotations.flatMap { it }.getClassReferences(targetName)
		val templateRefs = arrayListOf<AstType.REF>()
		//this.annotationsList.list.filter { it.type.name.simpleName == "JsMethodBody" }
		val methodBodyList = this.annotationsList.getBodiesForTarget(targetName)
		val haxeMethodBodyList = this.annotationsList.getTypedList(HaxeMethodBodyList::value).filter { targetName.haxeMatches(it.target) }
		val refs = if (methodBodyList.isEmpty() && haxeMethodBodyList.isEmpty()) this.body?.getClassReferences() ?: listOf() else listOf()

		for (methodBody in methodBodyList) {
			templateRefs += GetClassTemplateReferences(program, methodBody.value, clazzFqname, config).map { AstType.REF(it) }
		}

		for (methodBody in haxeMethodBodyList) {
			templateRefs += GetClassTemplateReferences(program, methodBody.value, clazzFqname, config).map { AstType.REF(it) }
		}

		if (targetName.haxeMatches("")) {
			val haxeMethodBodyPre = this.annotationsList.getTyped<HaxeMethodBodyPre>()
			val haxeMethodBodyPost = this.annotationsList.getTyped<HaxeMethodBodyPost>()

			if (haxeMethodBodyPre != null) templateRefs += GetClassTemplateReferences(program, haxeMethodBodyPre.value, clazzFqname, config).map { AstType.REF(it) }
			if (haxeMethodBodyPost != null) templateRefs += GetClassTemplateReferences(program, haxeMethodBodyPost.value, clazzFqname, config).map { AstType.REF(it) }
		}

		return signatureRefs + refs + annotations + parameterAnnotations + templateRefs
	}

	fun List<AstAnnotation>.getClassReferences(targetName: TargetName): List<AstType.REF> {
		return this.flatMap { it.getClassReferences(targetName) }
	}

	fun Any?.getClassReferences(targetName: TargetName): List<AstType.REF> {
		return when (this) {
			null -> listOf()
		//is List<*> -> this.flatMap { it.getClassReferences(targetName) }
		//is Collection<*> -> this.flatMap { it.getClassReferences(targetName) }
			is Iterable<*> -> this.flatMap { it.getClassReferences(targetName) }
			is AstAnnotation -> this.getClassReferences(targetName)
			is AstBody -> this.getClassReferences()
			is AstStm -> this.getClassReferences()
			is AstMethod -> this.getClassReferences(targetName)
			is AstField -> this.getClassReferences(targetName)
			is AstFieldWithoutTypeRef -> listOf(AstType.REF(this.containingClass))
			is AstClass -> {
				this.getClassReferences(targetName)
			}
			is String, is Boolean, is Int, is Float, is Double, is Long, is Byte, is Short, is Char, is Void -> listOf()
			is Type -> {
				val cn = this.className
				//val cn = this.internalName
				//println("CN: $cn")
				listOf(AstType.REF(cn))
			}
			else -> {
				listOf()
			}
		}
	}

	fun AstAnnotation.getClassReferences(targetName: TargetName): List<AstType.REF> {
		return listOf(this.type) + this.elements.values.getClassReferences(targetName)
	}

	fun AstBody.getClassReferences(): List<AstType.REF> {
		val locals = this.locals.flatMap { it.type.getRefClasses() }
		val traps = this.traps.map { it.exception }
		val stms = this.stm.getClassReferences()
		return locals + traps + stms
	}

	fun AstStm.getClassReferences(): List<AstType.REF> {
		val refs = ReferencesVisitor()
		refs.visit(this)
		return (refs.classReferences + refs.fieldReferences.map { it.classRef } + refs.methodReferences.flatMap { it.allClassRefs }).toList()
	}

	class ReferencesVisitor : AstVisitor() {
		val classReferences = hashSetOf<AstType.REF>()
		val fieldReferences = hashSetOf<AstFieldRef>()
		val methodReferences = hashSetOf<AstMethodRef>()

		override fun visit(ref: AstType.REF) {
			super.visit(ref)
			classReferences += ref
		}

		override fun visit(ref: AstFieldRef) {
			super.visit(ref)
			fieldReferences += ref
		}

		override fun visit(ref: AstMethodRef) {
			super.visit(ref)
			methodReferences += ref
		}
	}
}
