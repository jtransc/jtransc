package com.jtransc.ast

import com.jtransc.ds.toTypedArray2
import kotlin.reflect.KProperty1

data class AstAnnotation(
	val type: AstType.REF,
	val elements: Map<String, Any?>
)

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

operator inline fun <reified C : Annotation, T> List<AstAnnotation>?.get(field: KProperty1<C, T>): T? {
	if (this != null) {
		return this?.get(C::class.java.name.fqname, field.name) as T?
	} else {
		return null
	}
}

inline fun <reified C : Annotation> List<AstAnnotation>?.contains(): Boolean {
	return C::class.java.name.fqname in this
}
