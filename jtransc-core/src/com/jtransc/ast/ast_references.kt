package com.jtransc.ast

object References {
	fun get(clazz:AstClass) = clazz.getClassReferences()

	fun AstClass.getClassReferences(): List<AstClassRef> {
		val me = AstClassRef(this.fqname)
		val parent = if (this.extending != null) AstClassRef(this.extending) else null
		val interfaces = this.implementing.map { AstClassRef(it) }
		val fields = this.fields.flatMap { it.getClassReferences() }
		val methods = this.methods.flatMap { it.getClassReferences() }
		val annotations = this.annotations.getClassReferences()

		return (listOf(me) + listOf(parent) + interfaces + fields + methods + annotations).filterNotNull()
	}

	fun AstField.getClassReferences(): List<AstClassRef> {
		return this.genericType.getRefClasses() + this.annotations.getClassReferences()
	}

	fun AstMethod.getClassReferences(): List<AstClassRef> {
		val signatureRefs = this.genericMethodType.getRefClasses()
		val refs = this.body?.getClassReferences() ?: listOf()
		val annotations = this.annotations.getClassReferences()
		return signatureRefs + refs + annotations
	}

	fun List<AstAnnotation>.getClassReferences(): List<AstClassRef> {
		return this.flatMap { it.getClassReferences() }
	}

	fun AstAnnotation.getClassReferences(): List<AstClassRef> {
		return listOf(this.type.classRef)
	}

	fun AstBody.getClassReferences(): List<AstClassRef> {
		val locals = this.locals.flatMap { it.type.getRefClasses() }
		val traps = this.traps.map { it.exception.classRef }
		val stms = this.stm.getClassReferences()
		return locals + traps + stms
	}

	fun AstStm.getClassReferences(): List<AstClassRef> {
		val refs = ReferencesVisitor()
		refs.visit(this)
		return (refs.classReferences + refs.fieldReferences.map { it.classRef } + refs.methodReferences.flatMap { it.allClassRefs }).toList()
	}

	class ReferencesVisitor : AstVisitor() {
		val classReferences = hashSetOf<AstClassRef>()
		val fieldReferences = hashSetOf<AstFieldRef>()
		val methodReferences = hashSetOf<AstMethodRef>()

		override fun visit(ref: AstClassRef) {
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
