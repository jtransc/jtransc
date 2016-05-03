package com.jtransc.json

import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object Typer {
	fun <T : Any> toTyped(value: Any?, target: Class<T>): T {
		return Reflect.dynamicCast(value, target)!!
	}

	fun <T : Any> fromTyped(value: T?): Any? {
		return Reflect.fromTyped(value)
	}
}

private object Reflect {
	@Suppress("UNCHECKED_CAST")
	fun <T> createEmptyClass(clazz: Class<T>): T {
		if (clazz == java.util.List::class.java) return listOf<Any?>() as T
		if (clazz == java.util.Map::class.java) return mapOf<Any?, Any?>() as T
		if (clazz == java.lang.Iterable::class.java) return listOf<Any?>() as T

		val constructor = clazz.constructors.first()
		val args = constructor.parameterTypes.map {
			dynamicCast(null, it)
		}
		return constructor.newInstance(*args.toTypedArray()) as T
	}

	fun <T : Any> setField(instance: T, name: String, value: Any?) {
		val field = instance.javaClass.declaredFields.find { it.name == name }
		//val field = instance.javaClass.getField(name)
		field?.isAccessible = true
		field?.set(instance, value)
	}

	fun hasField(javaClass: Class<Any>, name: String): Boolean {
		return javaClass.declaredFields.any { it.name == name }
	}

	fun getFieldType(javaClass: Class<Any>, name: String): Class<*> {
		return javaClass.getField(name).type
	}

	inline fun <reified T : Any> dynamicCast(value: Any?): T? = dynamicCast(value, T::class.java)

	@Suppress("UNCHECKED_CAST")
	fun <T : Any>dynamicCast(value: Any?, target: Class<T>, genericType: Type? = null): T? {
		if (value != null && target.isAssignableFrom(value.javaClass)) {
			return if (genericType != null && genericType is ParameterizedType) {
				val typeArgs = genericType.actualTypeArguments
				when(value) {
					is List<*> -> value.map { dynamicCast(it, typeArgs[0] as Class<Any>) }
					else -> value
				} as T
			} else {
				value as T
			}
		}
		val str = if (value != null) "$value" else "0"
		if (target.isPrimitive) {
			when (target) {
				java.lang.Boolean.TYPE -> return (str == "true" || str == "1") as T
				java.lang.Byte.TYPE -> return str.parseInt().toByte() as T
				java.lang.Character.TYPE -> return str.parseInt().toChar() as T
				java.lang.Short.TYPE -> return str.parseInt().toShort() as T
				java.lang.Long.TYPE -> return str.parseLong() as T
				java.lang.Float.TYPE -> return str.toFloat() as T
				java.lang.Double.TYPE -> return str.parseDouble() as T
				java.lang.Integer.TYPE -> return str.parseInt() as T
				else -> throw InvalidOperationException("Unhandled primitive '${target.name}'")
			}
		}
		if (target.isAssignableFrom(java.lang.Boolean::class.java)) return (str == "true" || str == "1") as T
		if (target.isAssignableFrom(java.lang.Byte::class.java)) return str.parseInt().toByte() as T
		if (target.isAssignableFrom(java.lang.Character::class.java)) return str.parseInt().toChar() as T
		if (target.isAssignableFrom(java.lang.Short::class.java)) return str.parseShort() as T
		if (target.isAssignableFrom(java.lang.Integer::class.java)) return str.parseInt() as T
		if (target.isAssignableFrom(java.lang.Long::class.java)) return str.parseLong() as T
		if (target.isAssignableFrom(java.lang.Float::class.java)) return str.toFloat() as T
		if (target.isAssignableFrom(java.lang.Double::class.java)) return str.toDouble() as T
		if (target.isAssignableFrom(java.lang.String::class.java)) {
			return (if (value == null) "" else str) as T
		}
		if (target.isEnum) {
			if (value == null) return (target.getMethod("values").invoke(null) as Array<Any?>)[0] as T
			return java.lang.Enum.valueOf<AnyEnum>(target as Class<AnyEnum>, str) as T
		}
		if (value is List<*>) {
			return value.toList() as T
		}
		if (value is Map<*, *>) {
			val map = value as Map<Any?, *>
			val resultClass = target as Class<Any>
			val result = Reflect.createEmptyClass(resultClass)
			for (field in result.javaClass.declaredFields) {
				if (field.name in map) {
					val value = map[field.name]
					field.isAccessible = true
					field.set(result, dynamicCast(value, field.type, field.genericType))
				}
			}
			return result as T
		}
		if (value == null) return createEmptyClass(target)
		throw InvalidOperationException("Can't convert '$value' to '$target'")
	}

	private enum class AnyEnum {}

	fun String?.parseBool(): Boolean? = when (this) {
		"true", "yes", "1" -> true
		"false", "no", "0" -> false
		else -> null
	}

	fun String?.parseInt(): Int = this?.parseDouble()?.toInt() ?: 0
	fun String?.parseShort(): Short = this?.parseDouble()?.toShort() ?: 0
	fun String?.parseLong(): Long = try {
		this?.toLong()
	} catch (e: Throwable) {
		this?.parseDouble()?.toLong()
	} ?: 0L


	fun String?.parseDouble(): Double {
		if (this == null) return 0.0
		try {
			return this.toDouble()
		} catch (e: Throwable) {
			return 0.0
		}
	}

	fun <T : Any> fromTyped(value: T?): Any? {
		return when (value) {
			null -> null
			true -> true
			false -> false
			is Number -> value.toDouble()
			is String -> value
			is Map<*, *> -> value
			is Iterable<*> -> value
			else -> {
				val clazz = value.javaClass
				val out = hashMapOf<Any?, Any?>()
				for (field in clazz.declaredFields) {
					if (field.name.startsWith('$')) continue
					field.isAccessible = true
					out[field.name] = fromTyped(field.get(value))
				}
				out
			}
		}
	}
}
