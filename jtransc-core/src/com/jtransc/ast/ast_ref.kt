package com.jtransc.ast

interface AstRef
interface AstMemberRef : AstRef {
	val classRef: AstType.REF
	val containingClass: FqName
	val name: String
	val memberType: AstType
}

data class AstFieldRef(override val containingClass: FqName, override val name: String, val type: AstType) : AstMemberRef {
	override val classRef: AstType.REF by lazy { AstType.REF(containingClass) }
	override val memberType: AstType = type
	val containingTypeRef = AstType.REF(containingClass)
	override fun hashCode() = containingClass.hashCode() + name.hashCode() + type.hashCode()
	override fun toString() = "AstFieldRef(${containingClass.fqname},$name,${type.mangle()})"
}

data class AstMethodRef(override val containingClass: FqName, override val name: String, val type: AstType.METHOD) : AstMemberRef {
	override val classRef: AstType.REF by lazy { AstType.REF(containingClass) }
	val containingClassType: AstType.REF by lazy { AstType.REF(containingClass) }
	override val memberType: AstType = type
	val fid: String get() = "${containingClass.fqname}:$name:$desc"
	val fidWildcard: String get() = "${containingClass.fqname}:$name:*"
	val desc by lazy { type.desc }
	val descWithoutRetval by lazy { type.desc2 }
	val nameDesc by lazy { AstMethodWithoutClassRef(name, type) }

	val allClassRefs: List<AstType.REF> by lazy { type.getRefClasses() + classRef }

	override fun hashCode() = containingClass.hashCode() + name.hashCode() + type.hashCode()

	override fun toString() = "AstMethodRef(${containingClass.fqname},$name,${type.desc})"
}

data class AstFieldWithoutClassRef(val name: String, val type: AstType)
data class AstFieldWithoutTypeRef(val containingClass: FqName, val name: String)

data class AstMethodWithoutClassRef(val name: String, val type: AstType.METHOD) {
	val fid2: String get() = "$name:${type.mangle()}"
	val fid2Wildcard: String get() = "$name:*"
	val desc = type.desc
	val descWithoutRetval = type.desc2

	override fun toString() = "AstMethodWithoutClassRef($name,${type.desc})"
}

val AstMethodRef.methodDesc: AstMethodWithoutClassRef get() = AstMethodWithoutClassRef(this.name, this.type)

val AstMethodRef.withoutRetval: AstMethodRef get() {
	return if (this.type.ret == AstType.UNKNOWN) this else AstMethodRef(containingClass, name, type.withoutRetval)
}

val AstFieldRef.withoutClass: AstFieldWithoutClassRef get() = AstFieldWithoutClassRef(this.name, this.type)
val AstMethodRef.withoutClass: AstMethodWithoutClassRef get() = AstMethodWithoutClassRef(this.name, this.type)
fun AstMethodRef.withClass(other: AstType.REF) = AstMethodRef(other.name, this.name, this.type)
fun AstMethodWithoutClassRef.withClass(containingClass: FqName): AstMethodRef = AstMethodRef(containingClass, this.name, this.type)
