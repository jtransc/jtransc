package com.jtransc.ast

import java.lang.reflect.Proxy

data class AstAnnotation(
	val type: AstType.REF,
	val elements: Map<String, Any?>,
	val runtimeVisible: Boolean
) {
	private var typedObject: Any? = null

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

	inline fun <reified T : Any> toObject(): T? {
		return this.toObject(T::class.java)
	}

	fun <T> toObject(clazz: Class<T>): T? {
		return if (clazz.name == this.type.fqname) toAnyObject() as T else null
	}

	fun toAnyObject(): Any? {
		if (typedObject == null) {
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

			typedObject = Proxy.newProxyInstance(classLoader, arrayOf(classLoader.loadClass(this.type.fqname))) { proxy, method, args ->
				val valueUncasted = this.elements[method.name] ?: method.defaultValue
				val returnType = method.returnType
				minicast(valueUncasted, returnType)
			}
		}
		return typedObject!!
	}
}

class AstAnnotationList(val list: List<AstAnnotation>) {
	val byClassName by lazy { list.groupBy { it.type.fqname } }

	inline fun <reified T : Any> getTyped(): T? = byClassName[T::class.java.name]?.firstOrNull()?.toObject<T>()
	operator fun get(name: FqName): AstAnnotation? = byClassName[name.fqname]?.firstOrNull()
	inline fun <reified T : Any> contains(): Boolean = T::class.java.name in byClassName
}
