package com.jtransc.ast

import com.jtransc.error.noImpl
import kotlin.reflect.KProperty1

interface AstRef
interface AstMemberRef : AstRef {
	val classRef: AstType.REF
	val containingClass: FqName
	val name: String
	val memberType: AstType
}

inline fun <reified T1 : Any, reified T2 : Any> KProperty1<T1, T2>.ast(): AstFieldRef {
	return AstFieldRef(T1::class.java.name.fqname, this.name, T2::class.java.ast())
}

inline fun <reified T: Any> Class<T>.ast(): AstType {
	if (this.isPrimitive) {
		noImpl("Not implemented " + T::class.java)
	} else {
		return AstType.REF(this.name.fqname)
	}
}

object AstProgramRef : AstRef {

}

data class AstFieldRef(override val containingClass: FqName, override val name: String, val type: AstType) : AstMemberRef, FieldRef {
	override val ref = this
	override val classRef: AstType.REF by lazy { AstType.REF(containingClass) }
	override val memberType: AstType = type
	val containingTypeRef = AstType.REF(containingClass)
	override fun hashCode() = containingClass.hashCode() + name.hashCode() + type.hashCode()
	override fun toString() = "AstFieldRef(${containingClass.fqname},$name,${type.mangle()})"
}

data class AstMethodRef(override val containingClass: FqName, override val name: String, val type: AstType.METHOD) : AstMemberRef, MethodRef {
	override val ref = this
	override val classRef: AstType.REF by lazy { AstType.REF(containingClass) }
	val withoutClass: AstMethodWithoutClassRef by lazy { AstMethodWithoutClassRef(this.name, this.type) }
	val containingClassType: AstType.REF by lazy { AstType.REF(containingClass) }
	override val memberType: AstType = type
	val fid: String get() = "${containingClass.fqname}:$name:$desc"
	val fidWildcard: String get() = "${containingClass.fqname}:$name:*"
	val desc by lazy { type.desc }
	val descWithoutRetval by lazy { type.desc2 }
	val nameDesc by lazy { AstMethodWithoutClassRef(name, type) }
	val nameDescStr by lazy { "$name$desc" }
	val nameWithClass by lazy { "${containingClass.fqname}.${name}" }

	val allClassRefs: List<AstType.REF> by lazy { type.getRefClasses() + classRef }

	fun resolve(program: AstProgram): AstMethod {
		return program[containingClass][this]
	}

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
	return if (this.type.ret is AstType.UNKNOWN) this else AstMethodRef(containingClass, name, type.withoutRetval)
}

val AstFieldRef.withoutClass: AstFieldWithoutClassRef get() = AstFieldWithoutClassRef(this.name, this.type)
fun AstMethodRef.withClass(other: AstType.REF) = AstMethodRef(other.name, this.name, this.type)
fun AstMethodWithoutClassRef.withClass(containingClass: FqName): AstMethodRef = AstMethodRef(containingClass, this.name, this.type)
