package com.jtransc.ast

object References {
	fun get(clazz:AstClass) = clazz.getClassReferences()

	fun AstClass.getClassReferences(): List<AstType.REF> {
		val me = AstType.REF(this.fqname)
		val parent = if (this.extending != null) AstType.REF(this.extending) else null
		val interfaces = this.implementing.map { AstType.REF(it) }
		val fields = this.fields.flatMap { it.getClassReferences() }
		val methods = this.methods.flatMap { it.getClassReferences() }
		val annotations = this.annotations.getClassReferences()

		return (listOf(me) + listOf(parent) + interfaces + fields + methods + annotations).filterNotNull()
	}

	fun AstField.getClassReferences(): List<AstType.REF> {
		return this.genericType.getRefClasses() + this.annotations.getClassReferences()
	}

	fun AstMethod.getClassReferences(): List<AstType.REF> {
		val signatureRefs = this.genericMethodType.getRefClasses()
		val refs = this.body?.getClassReferences() ?: listOf()
		val annotations = this.annotations.getClassReferences()
		return signatureRefs + refs + annotations
	}

	fun List<AstAnnotation>.getClassReferences(): List<AstType.REF> {
		return this.flatMap { it.getClassReferences() }
	}

	fun AstAnnotation.getClassReferences(): List<AstType.REF> {
		return listOf(this.type)
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
