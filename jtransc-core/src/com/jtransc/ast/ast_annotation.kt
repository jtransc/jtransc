package com.jtransc.ast

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

operator inline fun <reified C : Annotation, T> List<AstAnnotation>?.get(field: KProperty1<C, T>): T? {
	return this?.get(C::class.java.name.fqname, field.name) as T?
}

inline fun <reified C : Annotation> List<AstAnnotation>?.contains(): Boolean {
	return C::class.java.name.fqname in this
}
