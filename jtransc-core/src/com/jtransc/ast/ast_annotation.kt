package com.jtransc.ast

import com.jtransc.annotation.JTranscMethodBodyList
import com.jtransc.annotation.JTranscNativeName
import com.jtransc.gen.TargetName
import com.jtransc.log.log
import com.jtransc.org.objectweb.asm.Type
import java.lang.reflect.Proxy
import kotlin.reflect.KProperty1

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

	fun getAnnotationAnnotations(program: AstProgram): AstAnnotationList = program[type]?.annotationsList ?: AstAnnotationList(type, listOf())

	fun getAllDescendantAnnotations(): List<AstAnnotation> {
		val out = arrayListOf<AstAnnotation>()
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

fun AstAnnotation.getRefTypesFqName(): List<FqName> {
	val out = hashSetOf<FqName>()
	out += this.type.getRefTypesFqName()
	for (e in this.elements.values) {
		when (e) {
			is AstAnnotation -> {
				out += e.getRefTypesFqName()
			}
			is Iterable<*> -> {
				for (i in e) {
					when (i) {
						is AstAnnotation -> {
							out += i.getRefTypesFqName()
						}
					}
				}
			}
			is String, is Boolean, is Int, is Float, is Double, is Long, is Byte, is Short, is Char, is Void -> Unit
			is Type -> {
				out += FqName(e.className)
			}
			is AstFieldWithoutTypeRef -> {
				out += e.containingClass
			}
			else -> {
				log.info("AstAnnotation.getRefTypesFqName.Unhandled: $e")
				//println("Unhandled: $e")
			}
		}
		//println("" + e + " : " + e?.javaClass)
	}
	return out.toList()
}


class AstAnnotationList(val containerRef: AstRef, val list: List<AstAnnotation>) {
	val byClassName by lazy { list.groupBy { it.type.fqname } }
	val listRuntime by lazy { list.filter { it.runtimeVisible } }
}

inline fun <reified TItem : Any, reified TList : Any> AstAnnotationList?.getTypedList(field: KProperty1<TList, Array<TItem>>): List<TItem> {
	if (this == null) return listOf()
	val single = this.getTyped<TItem>()
	val list = this.getTyped<TList>()
	return listOf(single).filterNotNull() + (if (list != null) field.get(list).toList() else listOf())
}

inline fun <reified T : Any> AstAnnotationList?.getTyped(): T? = if (this != null) byClassName[T::class.java.name]?.firstOrNull()?.toObject<T>() else null
inline fun <reified T : Any> AstAnnotationList?.getAllTyped(): List<T> = if (this != null) byClassName[T::class.java.name]?.map { it.toObject<T>() }?.filterNotNull() ?: listOf() else listOf()
operator fun AstAnnotationList?.get(name: FqName): AstAnnotation? = if (this != null) byClassName[name.fqname]?.firstOrNull() else null

inline fun <reified T : Any> AstAnnotationList?.contains(): Boolean = if (this != null) T::class.java.name in byClassName else false

class NativeBody(val lines: List<String>, val cond: String = "") {
	val value = lines.joinToString("\n")
}

fun AstAnnotationList.getBodiesForTarget(targetName: TargetName): List<NativeBody> {
	val extra = when (targetName.name) {
		"js" -> this.list.filter { it.type.name.simpleName == "JsMethodBody" }.map { NativeBody(listOf(it.elements["value"]?.toString() ?: "")) }
		else -> listOf()
	}
	return this.getTypedList(JTranscMethodBodyList::value).filter { targetName.matches(it.target) }.map { NativeBody(it.value.toList(), it.cond) } + extra
}

fun AstAnnotationList.getCallSiteBodyForTarget(targetName: TargetName): String? {
	return this.getTypedList(com.jtransc.annotation.JTranscCallSiteBodyList::value).filter { targetName.matches(it.target) }.map { it.value.joinToString("\n") }.firstOrNull()
}

fun AstAnnotationList.getHeadersForTarget(targetName: TargetName): List<String> {
	return this.getTypedList(com.jtransc.annotation.JTranscAddHeaderList::value).filter { targetName.matches(it.target) }.flatMap { it.value.toList() }
}

fun AstAnnotationList.getNativeNameForTarget(targetName: TargetName): JTranscNativeName? {
	return this.getTypedList(com.jtransc.annotation.JTranscNativeNameList::value).filter { targetName.matches(it.target) }.map { it }.firstOrNull()
}

//val AstAnnotationList.nonNativeCall get() = this.contains<JTranscNonNative>()