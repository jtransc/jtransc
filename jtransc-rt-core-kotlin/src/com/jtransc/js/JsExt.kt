package com.jtransc.js

import com.jtransc.annotation.JTranscMethodBody

interface JsDynamic

class JsBoundedMethod(@JvmField val obj: JsDynamic?, @JvmField val methodName: String) {
	operator fun invoke(vararg args: Any?): JsDynamic? = obj.call(methodName, *args)
}

val global: JsDynamic
	@JTranscMethodBody(target = "js", value = "return (typeof(window) != 'undefined') ? window : global;")
	get() = throw RuntimeException()

val window: JsDynamic
	@JTranscMethodBody(target = "js", value = "return window;")
	get() = throw RuntimeException()

val console: JsDynamic get() = global["console"]!!

val document: JsDynamic get() = global["document"]!!

@JTranscMethodBody(target = "js", value = "return p0[N.istr(p1)];")
external operator fun JsDynamic?.get(key: String): JsDynamic?

@JTranscMethodBody(target = "js", value = "return p0[p1];")
external operator fun JsDynamic?.get(key: Int): JsDynamic?

fun JsDynamic?.getMethod(key: String): JsBoundedMethod = JsBoundedMethod(this, key)

fun JsDynamic?.method(key: String): JsBoundedMethod = JsBoundedMethod(this, key)

@JTranscMethodBody(target = "js", value = "p0[N.istr(p1)] = N.unbox(p2);")
external operator fun JsDynamic?.set(key: String, value: Any?): Unit

@JTranscMethodBody(target = "js", value = """
	var obj = p0, methodName = N.istr(p1), rawArgs = p2;
	var args = [];
	for (var n = 0; n < rawArgs.length; n++) args.push(N.unbox(rawArgs.data[n]));
	return obj[methodName].apply(obj, args)
""")
external fun JsDynamic?.call(name: String, vararg args: Any?): JsDynamic?

fun jsNew(clazz: String): JsDynamic? = global[clazz].new()
fun jsNew(clazz: String, arg1: Any?): JsDynamic? = global[clazz].new(arg1)
fun jsNew(clazz: String, arg1: Any?, arg2: Any?): JsDynamic? = global[clazz].new(arg1, arg2)

@JTranscMethodBody(target = "js", value = """
	var clazz = p0, rawArgs = p1;
	var args = [null];
	for (var n = 0; n < rawArgs.length; n++) args.push(N.unbox(rawArgs.data[n]));
	return new (Function.prototype.bind.apply(clazz, args));
""")
external fun JsDynamic?.new(vararg args: Any?): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	return N.str(typeof(p0));
""")
external fun JsDynamic?.typeOf(): JsDynamic?

fun jsGlobal(key: String): JsDynamic? = window[key]

@JTranscMethodBody(target = "js", value = "return p0;")
external fun Any?.asJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = "return p0;")
external fun <T : Any?> JsDynamic?.asJavaType(): T

@JTranscMethodBody(target = "js", value = "return N.unbox(p0);")
external fun Any?.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = "return N.box(p0);")
external fun JsDynamic?.box(): Any?

@JTranscMethodBody(target = "js", value = "return N.str(p0);")
external fun JsDynamic?.toJavaString(): String

@JTranscMethodBody(target = "js", value = "return N.str(p0);")
external fun JsDynamic?.toJavaStringOrNull(): String?

fun <TR> jsFunction(v: Function0<TR>): JsDynamic? = v.toJsDynamic()
fun <T1, TR> jsFunction(v: Function1<T1, TR>): JsDynamic? = v.toJsDynamic()
fun <T1, T2, TR> jsFunction(v: Function2<T1, T2, TR>): JsDynamic? = v.toJsDynamic()

@JTranscMethodBody(target = "js", value = """
	var handler = p0;
	return function() {
		return N.unbox(handler['{% METHOD kotlin.jvm.functions.Function0:invoke %}']());
	};
""")
fun <TR> Function0<TR>.toJsDynamic(): JsDynamic? = throw NotImplementedError()

@JTranscMethodBody(target = "js", value = """
	var handler = p0;
	return function(p1) {
		return N.unbox(handler['{% METHOD kotlin.jvm.functions.Function1:invoke %}'](N.box(p1)));
	};
""")
fun <T1, TR> Function1<T1, TR>.toJsDynamic(): JsDynamic? = throw NotImplementedError()

@JTranscMethodBody(target = "js", value = """
	var handler = p0;
	return function(p1, p2) {
		return N.unbox(handler['{% METHOD kotlin.jvm.functions.Function2:invoke %}'](N.box(p1), N.box(p2)));
	};
""")
fun <T1, T2, TR> Function2<T1, T2, TR>.toJsDynamic(): JsDynamic? = throw NotImplementedError()

@JTranscMethodBody(target = "js", value = "return p0|0;")
external fun JsDynamic?.toInt(): Int

@JTranscMethodBody(target = "js", value = "return +p0;")
external fun JsDynamic?.toDouble(): Double

@JTranscMethodBody(target = "js", value = "return p0 == p1;")
external fun JsDynamic?.eq(that: JsDynamic?): Boolean

@JTranscMethodBody(target = "js", value = "return N.istr(p0);")
external fun String.toJavaScriptString(): JsDynamic?

@JTranscMethodBody(target = "js", value = "debugger;")
external fun jsDebugger(): Unit
