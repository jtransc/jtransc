package com.jtransc.lang

import com.jtransc.error.InvalidOperationException
import com.jtransc.error.noImpl
import com.jtransc.util.toDoubleOrNull2
import java.lang.reflect.Array
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

object Dynamic {
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

	fun <T : Any> getField(instance: T?, name: String): Any? {
		if (instance == null) return null
		val getter = instance.javaClass.getMethod("get${name.capitalize()}")
		if (getter != null) {
			getter.isAccessible = true
			return getter.invoke(instance)
		} else {
			val field = instance.javaClass.declaredFields.find { it.name == name }

			//val field = instance.javaClass.getField(name)
			field?.isAccessible = true
			return field?.get(instance)
		}
	}

	fun toNumber(it: Any?): Double {
		return when (it) {
			null -> 0.0
			is Number -> it.toDouble()
			else -> {
				it.toString().toDoubleOrNull2() ?: 0.0
			}
		}
	}

	fun toInt(it: Any?): Int {
		return toNumber(it).toInt()
	}

	fun toBool(it: Any?): Boolean {
		return when (it) {
			null -> false
			is Boolean -> it
			is String -> it.isNotEmpty() && it != "0" && it != "false"
			else -> toInt(it) != 0
		}
	}

	fun toIterable(it: Any?): Iterable<*> {
		return when (it) {
			null -> listOf<Any?>()
			is Iterable<*> -> it
			is CharSequence -> it.toList()
			else -> listOf<Any?>()
		}
	}

	fun toMap(it: Any?): Map<*, *> {
		return when (it) {
			null -> mapOf<Any?, Any?>()
			is Iterable<*> -> it.withIndex().map { it.index to it.value }.toMap()
			is Map<*, *> -> it
			else -> {
				val out = hashMapOf<String, Any?>()
				for (field in it.javaClass.fields.filter { !Modifier.isStatic(it.modifiers) }) {
					field.isAccessible = true
					out[field.name] = field.get(it)
				}
				//for (method in it.javaClass.methods.filter { it.name.startsWith("get") && it.parameterCount == 0 }) {
				//	out[method.name] = method.invoke(it)
				//}
				out
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun toComparable(it: Any?): Comparable<Any?> {
		return when (it) {
			null -> 0 as Comparable<Any?>
			is Comparable<*> -> it as Comparable<Any?>
			else -> it.toString() as Comparable<Any?>
		}
	}

	fun compare(l: Any?, r: Any?): Int {
		val lc = toComparable(l)
		val rc = toComparable(r)
		if (lc.javaClass.isAssignableFrom(rc.javaClass)) {
			return lc.compareTo(rc)
		} else {
			return -1
		}
	}

	fun accessAny(instance: Any?, key:Any?): Any? {
		return when (instance) {
			null -> null
			is Map<*, *> -> instance[key]
			is Iterable<*> -> instance.toList()[toInt(key)]
			else -> getField(instance, key.toString())
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun setAny(instance: Any?, key:Any?, value: Any?): Any? {
		return when (instance) {
			null -> null
			is MutableMap<*, *> -> (instance as MutableMap<Any?, Any?>).set(key, value)
			is MutableList<*> -> (instance as MutableList<Any?>)[toInt(key)] = value
			else -> setField(instance, key.toString(), value)
		}
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
		if (target.isAssignableFrom(java.lang.String::class.java)) return (if (value == null) "" else str) as T
		if (target.isEnum) return if (value != null) java.lang.Enum.valueOf<AnyEnum>(target as Class<AnyEnum>, str) as T else target.enumConstants.first()
		if (value is List<*>) return value.toList() as T
		if (value is Map<*, *>) {
			val map = value as Map<Any?, *>
			val resultClass = target as Class<Any>
			val result = Dynamic.createEmptyClass(resultClass)
			for (field in result.javaClass.declaredFields) {
				if (field.name in map) {
					val v = map[field.name]
					field.isAccessible = true
					field.set(result, dynamicCast(v, field.type, field.genericType))
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

	fun unop(r: Any?, op: String): Any? {
		return when (op) {
			"+" -> r
			"-" -> -toNumber(r)
			"~" -> toInt(r).inv()
			"!" -> !toBool(r)
			else -> noImpl("Not implemented unary operator $op")
		}
	}

	//fun toFixNumber(value: Double): Any = if (value == value.toInt().toDouble()) value.toInt() else value

	fun toString(value: Any?): String {
		when (value) {
			is Double -> {
				if (value == value.toInt().toDouble()) {
					return value.toInt().toString()
				}
			}
		}
		return value.toString()
	}

	fun binop(l: Any?, r: Any?, op: String): Any? {
		return when (op) {
			"+" -> {
				when (l) {
					is String -> l.toString() + r.toBetterString()
					is Iterable<*> -> toIterable(l) + toIterable(r)
					else -> toNumber(l) + toNumber(r)
				}
			}
			"-"  -> toNumber(l) - toNumber(r)
			"*"  -> toNumber(l) * toNumber(r)
			"/"  -> toNumber(l) / toNumber(r)
			"%"  -> toNumber(l) % toNumber(r)
			"**" -> Math.pow(toNumber(l), toNumber(r))
			"&"  -> toInt(l) and toInt(r)
			"or" -> toInt(l) or toInt(r)
			"^"  -> toInt(l) xor toInt(r)
			"&&" -> toBool(l) && toBool(r)
			"||" -> toBool(l) || toBool(r)
			"==" -> Objects.equals(l, r)
			"!=" -> !Objects.equals(l, r)
			"<" -> compare(l, r) < 0
			"<=" -> compare(l, r) <= 0
			">" -> compare(l, r) > 0
			">=" -> compare(l, r) >= 0
			else -> noImpl("Not implemented binary operator $op")
		}
	}

	fun callAny(obj: Any?, key: Any?, args: List<Any?>): Any? {
		if (obj == null) return null
		if (key == null) return null
		val method = obj.javaClass.methods.first { it.name == key }
		method.isAccessible = true
		val result = method.invoke(obj, *args.toTypedArray())
		return result
	}

	fun callAny(callable: Any?, args: List<Any?>): Any? {
		return callAny(callable, "invoke", args)
	}

	fun length(subject: Any?): Int {
		if (subject == null) return 0
		if (subject.javaClass.isArray) return Array.getLength(subject)
		if (subject is List<*>) return subject.size
		return subject.toString().length
	}
}
