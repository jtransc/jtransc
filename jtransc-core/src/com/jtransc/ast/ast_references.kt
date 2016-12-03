package com.jtransc.ast

import com.jtransc.annotation.JTranscAddFileList
import com.jtransc.annotation.JTranscMethodBodyList
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.ast.treeshaking.GetClassTemplateReferences
import com.jtransc.gen.TargetName
import com.jtransc.template.Minitemplate

object References {
	private val bl = JTranscMethodBodyList::class.java

	fun get(clazz: AstClass, targetName: TargetName) = clazz.getClassReferences(targetName)

	fun AstClass.getClassReferences(targetName: TargetName): List<AstType.REF> {
		val me = AstType.REF(this.fqname)
		val parent = if (this.extending != null) AstType.REF(this.extending) else null
		val interfaces = this.implementing.map { AstType.REF(it) }
		val fields = this.fields.flatMap { it.getClassReferences() }
		val methods = this.methods.flatMap { it.getClassReferences() }
		val annotations = this.annotations.getClassReferences()
		val annotations2 = this.annotationsList.getClassReferencesExtra(this.program, targetName)
		//val annotations2 = listOf<AstType.REF>()

		return (listOf(me) + listOf(parent) + interfaces + fields + methods + annotations + annotations2).filterNotNull()
	}

	fun AstAnnotationList.getClassReferencesExtra(program: AstProgram, targetName: TargetName): List<AstType.REF> {
		val out = arrayListOf<AstType.REF>()
		for (file in this.getTypedList(JTranscAddFileList::value)) {
			if (file.process && targetName.matches(file.target)) {
				val possibleFiles = listOf(file.prepend, file.append, file.prependAppend)
				for (pf in possibleFiles.filter { !it.isNullOrEmpty() }) {
					val filecontent = program.resourcesVfs[pf].readString()
					out += GetClassTemplateReferences(program, filecontent).map { AstType.REF(it) }
				}
			}
		}
		return out
	}

	fun AstField.getClassReferences(): List<AstType.REF> {
		return this.genericType.getRefClasses() + this.annotations.getClassReferences()
	}

	fun AstMethod.getClassReferences(): List<AstType.REF> {
		val signatureRefs = this.genericMethodType.getRefClasses()
		val refs = this.body?.getClassReferences() ?: listOf()
		val annotations = this.annotations.getClassReferences()
		val parameterAnnotations = this.parameterAnnotations.flatMap { it }.getClassReferences()
		val templateRefs = arrayListOf<AstType.REF>()
		for (methodBody in this.annotationsList.getTypedList(JTranscMethodBodyList::value)) {
			val template = Minitemplate(methodBody.value.joinToString("\n"), Minitemplate.Config(
				extraTags = listOf(
					Minitemplate.Tag(
						":programref:", setOf(), null,
						aliases = listOf(
							//"sinit", "constructor", "smethod", "method", "sfield", "field", "class",
							"SINIT", "CONSTRUCTOR", "SMETHOD", "METHOD", "SFIELD", "FIELD", "CLASS"
						)
					) {
						//val tag = it.first().token.name
						val desc = it.first().token.content
						val ref = CommonTagHandler.getRefFqName(desc, hashMapOf())
						templateRefs += ref.ref
						Minitemplate.BlockNode.TEXT("")
					}
				),
				extraFilters = listOf(
				)
			))
			template(hashMapOf<String, Any?>())
		}
		return signatureRefs + refs + annotations + parameterAnnotations + templateRefs
	}

	fun List<AstAnnotation>.getClassReferences(): List<AstType.REF> {
		return this.flatMap { it.getClassReferences() }
	}

	fun Any?.getClassReferences(): List<AstType.REF> {
		return when (this) {
			null -> listOf()
			is List<*> -> this.flatMap { it.getClassReferences() }
			is AstAnnotation -> this.getClassReferences()
			is AstBody -> this.getClassReferences()
			is AstStm -> this.getClassReferences()
			is AstMethod -> this.getClassReferences()
			is AstField -> this.getClassReferences()
			is AstClass -> this.getClassReferences()
			else -> listOf()
		}
	}

	fun AstAnnotation.getClassReferences(): List<AstType.REF> {
		return listOf(this.type) + this.elements.values.getClassReferences()
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
