package com.jtransc.ast

import com.jtransc.ds.stripNulls
import com.jtransc.ds.toTypedArray2
import com.jtransc.lang.Dynamic
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*
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

class AstAnnotationList(val list: List<AstAnnotation>) {

}

inline fun <reified T : Any> AstAnnotation.toObject(): T? {
	return this.toObject(T::class.java)
}

fun <T> AstAnnotation.toObject(clazz: Class<T>): T? {
	return if (clazz.name == this.type.fqname) toAnyObject() as T else null
}

fun AstAnnotation.toAnyObject(): Any? {
	val classLoader = this.javaClass.classLoader

	fun minicast(it: Any?, type: Class<*>): Any? {
		return when (it) {
			is List<*> -> {
				val array = java.lang.reflect.Array.newInstance(type.componentType, it.size)
				for (n in 0 until it.size) java.lang.reflect.Array.set(array, n, minicast(it[n], type.componentType))
				array
			}
			is AstAnnotation -> {
				it.toAnyObject()
			}
			else -> {
				it
			}
		}
	}

	return Proxy.newProxyInstance(classLoader, arrayOf(classLoader.loadClass(this.type.fqname))) { proxy, method, args ->
		val valueUncasted = this.elements[method.name] ?: method.defaultValue
		val returnType = method.returnType
		minicast(valueUncasted, returnType)
	}
}

operator fun List<AstAnnotation>?.get(name: FqName): AstAnnotation? {
	val ref = AstType.REF(name)
	return this?.firstOrNull { it.type == ref }
}

inline fun <reified T : Any> List<AstAnnotation>?.getTyped(): T? {
	val ref = AstType.REF(T::class.java.name)
	return this?.firstOrNull { it.type == ref }?.toObject<T>()
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
