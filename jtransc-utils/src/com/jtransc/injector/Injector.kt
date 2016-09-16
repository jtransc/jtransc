package com.jtransc.injector

import com.jtransc.error.invalidOp

@Suppress("UNCHECKED_CAST")
class Injector() {
	val maps = hashMapOf<Class<*>, () -> Any>(
		Injector::class.java to { this@Injector }
	)

	inline fun <reified T : Any> get(): T = getInstance(T::class.java)
	inline fun <reified T : Any> get(default: () -> T): T = if (T::class.java in maps) getInstance(T::class.java) else default()

	fun <T : Any> getInstance(clazz: Class<T>): T {
		if (clazz !in maps) mapImplementation(clazz, clazz)
		return this.maps[clazz]!!() as T
	}

	internal fun <T : Any> createInstance(clazz: Class<T>): T {
		val c = clazz.constructors.firstOrNull() ?: invalidOp("No constructors")
		return c.newInstance(*(c.parameterTypes.map { this.getInstance(it) }).toTypedArray()) as T
	}

	private fun getAllAnnotations(clazz: Class<*>): List<Annotation> {
		return if (clazz.superclass == null) {
			clazz.annotations.toList()
		} else {
			clazz.annotations.toList() + getAllAnnotations(clazz.superclass)
		} + clazz.interfaces.flatMap { getAllAnnotations(it) }
	}

	fun mapImplementation(classInterface: Class<*>, classImpl: Class<*>) {
		val isSingleton = (getAllAnnotations(classImpl).filterIsInstance<Singleton?>().isNotEmpty())
		var cached: Any? = null

		this.maps[classInterface] = {
			if (isSingleton) {
				if (cached == null) cached = createInstance(classImpl)
				cached!!
			} else {
				createInstance(classImpl)
			}
		}
	}

	fun mapInstances(vararg objs: Any) = run { for (obj in objs) mapInstance(obj) }
	fun <T : Any> mapInstance(obj: T): T = mapInstance(obj, obj.javaClass)
	fun <T : Any> mapInstance(obj: T, type: Class<T>): T = obj.apply { maps[type] = { obj } }
	inline fun <reified TInt : Any, reified TImpl : TInt> mapImpl() = mapImplementation(TInt::class.java, TImpl::class.java)
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Singleton()