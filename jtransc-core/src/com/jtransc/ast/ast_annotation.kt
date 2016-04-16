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

operator inline fun <reified C : Annotation, reified T : Any> List<AstAnnotation>?.get(field: KProperty1<C, T>): T? {
	val cClass = C::class.java
	val tClass = T::class.java
	val value = this?.get(cClass.name.fqname, field.name)
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

inline fun <reified C : Annotation, reified T : Any> List<AstClass>.getAnnotation(field: KProperty1<C, T>): List<T> {
	return this.map { it.annotations.get(field) }.stripNulls()
}

inline fun <reified C : Annotation> List<AstAnnotation>?.contains(): Boolean {
	return C::class.java.name.fqname in this
}
