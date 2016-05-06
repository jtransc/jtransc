package com.jtransc.ast

import com.jtransc.ds.stripNulls
import com.jtransc.ds.toTypedArray2
import kotlin.reflect.KProperty1

data class AstAnnotation(
	val type: AstType.REF,
	val elements: Map<String, Any?>,
    val runtimeVisible: Boolean
) {
	private fun getAllDescendantAnnotations(value: Any?): List<AstAnnotation> {
		return when (value) {
			is AstAnnotation -> getAllDescendantAnnotations(elements.values.toList())
			is List<*> -> value.filterIsInstance<AstAnnotation>() + value.filterIsInstance<List<*>>().flatMap { getAllDescendantAnnotations(it) }
			else -> listOf()
		}
	}

	fun getAllDescendantAnnotations(): List<AstAnnotation> {
		var out = arrayListOf<AstAnnotation>()
		out.add(this)
		out.addAll(getAllDescendantAnnotations(this))
		return out
	}
}

operator fun List<AstAnnotation>?.get(name: FqName): AstAnnotation? {
	val ref = AstType.REF(name)
	return this?.firstOrNull { it.type == ref }
}

operator fun List<AstAnnotation>?.get(name: FqName, field: String): Any? {
	val value = this?.get(name)?.elements?.get(field)
	return when (value) {
		null -> null
		is List<*> -> value.toTypedArray2()
		else -> value
	}
}

operator fun List<AstAnnotation>?.contains(name: FqName): Boolean {
	return this.get(name) != null
}

operator inline fun List<AstAnnotation>?.contains(clazz: Class<Any?>): Boolean {
	return FqName(clazz.name) in this
}

inline fun <reified T : Any> List<AstAnnotation>?.contains2(): Boolean {
	return FqName(T::class.java.name) in this
}

operator fun <C, T> List<AstAnnotation>?.get(cClass: Class<C>, tClass: Class<T>, fieldName: String): T? {
	val value = this?.get(cClass.name.fqname, fieldName)
	val valueClass = value?.javaClass
	if (valueClass != null && valueClass != tClass) {
		if (tClass.isArray && valueClass.isArray) {
			if (java.lang.reflect.Array.getLength(value) == 0) {
				return java.lang.reflect.Array.newInstance(tClass.componentType, 0) as T?
			}
		}
		println("different! $valueClass, $tClass")
	}
	return value as T?
}

data class FieldReference<C, T>(val cClass: Class<C>, val tClass: Class<T>, val fieldName: String) {
}

inline fun <reified C : Any, reified T : Any> FieldReference(field: KProperty1<C, T>): FieldReference<C, T> {
	return FieldReference(C::class.java, T::class.java, field.name)
}

inline fun <reified C : Any, reified T : Any> KProperty1<C, T>.ref(): FieldReference<C, T> = FieldReference(C::class.java, T::class.java, this.name)

operator fun <C : Any, T : Any> List<AstAnnotation>?.get(ref: FieldReference<C, T>): T? {
	return this[ref.cClass, ref.tClass, ref.fieldName]
}

operator inline fun <reified C : Annotation, reified T : Any> List<AstAnnotation>?.get(field: KProperty1<C, T>): T? {
	return this[C::class.java, T::class.java, field.name]
}

inline fun <reified C : Annotation, reified T : Any> List<AstClass>.getAnnotation(field: KProperty1<C, T>): List<T> {
	return this.map { it.annotations[field] }.stripNulls()
}

inline fun <reified C : Annotation> List<AstAnnotation>?.contains(): Boolean {
	return C::class.java.name.fqname in this
}
