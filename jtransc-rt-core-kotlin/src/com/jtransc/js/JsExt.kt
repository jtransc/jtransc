@file:Suppress("NOTHING_TO_INLINE")

package com.jtransc.js

import com.jtransc.annotation.JTranscCallSiteBody
import com.jtransc.annotation.JTranscLiteralParam
import com.jtransc.annotation.JTranscMethodBody
import com.jtransc.annotation.JTranscUnboxParam

interface JsDynamic

class JsBoundedMethod(val obj: JsDynamic?, val methodName: String) {
	operator fun invoke(vararg args: Any?): JsDynamic? = obj.call(methodName, *args)
}

val global: JsDynamic
	//@JTranscMethodBody(target = "js", value = "return (typeof(window) != 'undefined') ? window : global;")
	@JTranscCallSiteBody(target = "js", value = ["_global"])
	get() = throw RuntimeException()

val window: JsDynamic
	//@JTranscCallSiteBody(target = "js", value = "window")
	@JTranscCallSiteBody(target = "js", value = ["window"])
	get() = throw RuntimeException()

val console: JsDynamic get() = global["console"]!!

val document: JsDynamic get() = global["document"]!!

@JTranscCallSiteBody(target = "js", value = ["#0#.1"])
external operator fun JsDynamic?.get(@JTranscUnboxParam key: String): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1]"])
external operator fun JsDynamic?.get(key: Int): JsDynamic?

fun JsDynamic?.getMethod(key: String): JsBoundedMethod = JsBoundedMethod(this, key)

fun JsDynamic?.method(key: String): JsBoundedMethod = JsBoundedMethod(this, key)

@JTranscCallSiteBody(target = "js", value = ["#0#.1 = #2;"])
external operator fun JsDynamic?.set(@JTranscUnboxParam key: String, @JTranscUnboxParam value: Any?): Unit

@JTranscCallSiteBody(target = "js", value = ["#0[#1] = #2;"])
external operator fun JsDynamic?.set(key: Int, @JTranscUnboxParam value: Any?): Unit

// invoke is unsafe, because it could be split in several statements and method name not bound to object

//@JTranscCallSiteBody(target = "js", value = "#0()")
//external operator fun JsDynamic?.invoke(): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3, #4)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3, #4, #5)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3, #4, #5, #6)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3, #4, #5, #6, #7)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?, @JTranscUnboxParam p7: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3, #4, #5, #6, #7, #8)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?, @JTranscUnboxParam p7: Any?, @JTranscUnboxParam p8: Any?): JsDynamic?
//
//@JTranscCallSiteBody(target = "js", value = "#0(#1, #2, #3, #4, #5, #6, #7, #8, #9)")
//external operator fun JsDynamic?.invoke(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?, @JTranscUnboxParam p7: Any?, @JTranscUnboxParam p8: Any?, @JTranscUnboxParam p9: Any?): JsDynamic?


@JTranscCallSiteBody(target = "js", value = ["#0[#1]()"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4, #5)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4, #5, #6)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4, #5, #6, #7)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4, #5, #6, #7, #8)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?, @JTranscUnboxParam p7: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4, #5, #6, #7, #8, #9)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?, @JTranscUnboxParam p7: Any?, @JTranscUnboxParam p8: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["#0[#1](#2, #3, #4, #5, #6, #7, #8, #9, #10)"])
external fun JsDynamic?.call(@JTranscUnboxParam method: String, @JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?, @JTranscUnboxParam p5: Any?, @JTranscUnboxParam p6: Any?, @JTranscUnboxParam p7: Any?, @JTranscUnboxParam p8: Any?, @JTranscUnboxParam p9: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["new (#0)()"])
external fun JsDynamic?.new(): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["new (#0)(#1)"])
external fun JsDynamic?.new(@JTranscUnboxParam p1: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["new (#0)(#1, #2)"])
external fun JsDynamic?.new(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["new (#0)(#1, #2, #3)"])
external fun JsDynamic?.new(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["new (#0)(#1, #2, #3, #4)"])
external fun JsDynamic?.new(@JTranscUnboxParam p1: Any?, @JTranscUnboxParam p2: Any?, @JTranscUnboxParam p3: Any?, @JTranscUnboxParam p4: Any?): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	var clazz = p0, rawArgs = p1;
	var args = [null];
	for (var n = 0; n < rawArgs.length; n++) args.push(N.unbox(rawArgs.data[n]));
	return new (Function.prototype.bind.apply(clazz, args));
"""])
external fun JsDynamic?.new(vararg args: Any?): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	var obj = p0, methodName = N.istr(p1), rawArgs = p2;
	var args = [];
	for (var n = 0; n < rawArgs.length; n++) args.push(N.unbox(rawArgs.data[n]));
	return obj[methodName].apply(obj, args)
"""])
external fun JsDynamic?.call(name: String, vararg args: Any?): JsDynamic?

inline fun jsNew(clazz: String): JsDynamic? = global[clazz].new()
inline fun jsNew(clazz: String, p1: Any?): JsDynamic? = global[clazz].new(p1)
inline fun jsNew(clazz: String, p1: Any?, p2: Any?): JsDynamic? = global[clazz].new(p1, p2)
inline fun jsNew(clazz: String, p1: Any?, p2: Any?, p3: Any?): JsDynamic? = global[clazz].new(p1, p2, p3)
inline fun jsNew(clazz: String, p1: Any?, p2: Any?, p3: Any?, p4: Any?): JsDynamic? = global[clazz].new(p1, p2, p3, p4)

inline fun jsNew(clazz: String, vararg args: Any?): JsDynamic? = global[clazz].new(*args)

@JTranscCallSiteBody(target = "js", value = ["typeof(#0)"])
external fun JsDynamic?.typeOf(): JsDynamic?

fun jsGlobal(key: String): JsDynamic? = window[key]

@JTranscCallSiteBody(target = "js", value = ["(#0)"])
external fun Any?.asJsDynamic(): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["(#0)"])
external fun <T : Any?> JsDynamic?.asJavaType(): T

@JTranscCallSiteBody(target = "js", value = ["N.unbox(#0)"])
external fun Any?.toJsDynamic(): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["N.box(#0)"])
external fun JsDynamic?.box(): Any?

@JTranscCallSiteBody(target = "js", value = ["N.str(#0)"])
external fun JsDynamic?.toJavaString(): String

@JTranscCallSiteBody(target = "js", value = ["N.str(#0)"])
external fun JsDynamic?.toJavaStringOrNull(): String?

fun <TR> jsFunction(v: Function0<TR>): JsDynamic? = v.toJsDynamic()
fun <T1, TR> jsFunction(v: Function1<T1, TR>): JsDynamic? = v.toJsDynamic()
fun <T1, T2, TR> jsFunction(v: Function2<T1, T2, TR>): JsDynamic? = v.toJsDynamic()

@JTranscMethodBody(target = "js", value = ["""
	var handler = p0;
	return function() {
		return N.unbox(handler{% IMETHOD kotlin.jvm.functions.Function0:invoke %}());
	};
"""])
external fun <TR> Function0<TR>.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	var handler = p0;
	return function(p1) {
		return N.unbox(handler{% IMETHOD kotlin.jvm.functions.Function1:invoke %}(N.box(p1)));
	};
"""])
external fun <T1, TR> Function1<T1, TR>.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	var handler = p0;
	return function(p1, p2) {
		return N.unbox(handler{% IMETHOD kotlin.jvm.functions.Function2:invoke %}(N.box(p1), N.box(p2)));
	};
"""])
external fun <T1, T2, TR> Function2<T1, T2, TR>.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	return function() {return N.unbox(p0{% IMETHOD kotlin.jvm.functions.Function0:invoke %}());};
"""])
external fun <TR> jsFunctionRaw0(v: Function0<TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	return function(p1) {return N.unbox(p0{% IMETHOD kotlin.jvm.functions.Function1:invoke %}(p1));};
"""])
external fun <TR> jsFunctionRaw1(v: Function1<JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	return function(p1, p2) {return N.unbox(p0[% IMETHOD kotlin.jvm.functions.Function2:invoke %}(p1, p2));};
"""])
external fun <TR> jsFunctionRaw2(v: Function2<JsDynamic?, JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	return function(p1, p2, p3) {return N.unbox(p0[% IMETHOD kotlin.jvm.functions.Function3:invoke %}(p1, p2, p3));};
"""])
external fun <TR> jsFunctionRaw3(v: Function3<JsDynamic?, JsDynamic?, JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = ["""
	return function(p1, p2, p3, p4) {return N.unbox(p0{% IMETHOD kotlin.jvm.functions.Function4:invoke %}(p1, p2, p3, p4));};
"""])
external fun <TR> jsFunctionRaw4(v: Function4<JsDynamic?, JsDynamic?, JsDynamic?, JsDynamic?, TR>): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["(!!(#0))"])
external fun JsDynamic?.toBool(): Boolean

@JTranscCallSiteBody(target = "js", value = ["((#0)|0)"])
external fun JsDynamic?.toInt(): Int

@JTranscCallSiteBody(target = "js", value = ["(+(#0))"])
external fun JsDynamic?.toDouble(): Double

@JTranscCallSiteBody(target = "js", value = ["((#0) == (#1))"])
external infix fun JsDynamic?.eq(that: JsDynamic?): Boolean

@JTranscCallSiteBody(target = "js", value = ["((#0) != (#1))"])
external infix fun JsDynamic?.ne(that: JsDynamic?): Boolean

@JTranscCallSiteBody(target = "js", value = ["N.istr(#0)"])
external fun String.toJavaScriptString(): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["debugger;"])
external fun jsDebugger(): Unit

class JsMethods(val obj: JsDynamic?) {
	operator fun get(name: String) = JsBoundedMethod(obj, name)
}

val JsDynamic?.methods: JsMethods get() = JsMethods(this)

@JTranscMethodBody(target = "js", value = ["""
	var out = [];
	for (var n = 0; n < p0.length; n++) out.push(N.unbox(p0.data[n]));
	return out;
"""])
external fun jsArray(vararg items: Any?): JsDynamic?

fun jsRegExp(regex: String): JsDynamic? = global["RegExp"].new(regex)
fun jsRegExp(regex: Regex): JsDynamic? = global["RegExp"].new(regex.pattern)
fun Regex.toJs() = jsRegExp(this)

data class JsAssetStat(val path: String, val size: Long)

@JTranscMethodBody(target = "js", value = ["""
	var out = new JA_L({{ assetFiles|length }}, '[Lcom/jtransc/js/JsAssetStat;');
	var n = 0;
	{% for asset in assetFiles %}
	out.data[n++] = {% CONSTRUCTOR com.jtransc.js.JsAssetStat %}(N.str({{ asset.path|quote }}), Int64.ofFloat({{ asset.size }}));
	{% end %}
	return out
"""])
external fun jsGetAssetStats(): Array<JsAssetStat>

@JTranscMethodBody(target = "js", value = ["""
	var out = {};
	for (var n = 0; n < p0.length; n++) {
	var item = p0.data[n];
	out[N.istr(item["{% FIELD kotlin.Pair:first %}"])] = N.unbox(item["{% FIELD kotlin.Pair:second %}"]);
	}
	return out
"""])
external fun jsObject(vararg items: Pair<String, Any?>): JsDynamic?

fun jsObject(map: Map<String, Any?>): JsDynamic? = jsObject(*map.map { it.key to it.value }.toTypedArray())

@JTranscCallSiteBody(target = "js", value = ["#0"])
external fun jsRaw(@JTranscLiteralParam str: String): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["require(#0)"])
external fun jsRequire(@JTranscUnboxParam name: String): JsDynamic?

@JTranscCallSiteBody(target = "js", value = ["((#0) instanceof (#1))"])
external fun JsDynamic?.jsInstanceOf(type: JsDynamic?): JsDynamic?

fun JsDynamic?.jsIsString(): Boolean = this.typeOf().toJavaString() == "string"

@JTranscMethodBody(target = "js", value = ["return JA_B.fromTypedArray(new Int8Array(p0));"])
external fun JsDynamic?.toByteArray(): ByteArray

@JTranscMethodBody(target = "js", value = ["return new Int8Array(p0.data.buffer, 0, p0.length);"])
external fun ByteArray.toJsTypedArray(): JsDynamic?

fun JsDynamic.arrayToList(): List<JsDynamic?> {
	val array = this
	return (0 until array["length"].toInt()).map { array[it] }
}

fun JsDynamic.getObjectKeys(): List<String> {
	val keys = global["Object"].call("keys", this)
	return (0 until keys["length"].toInt()).map { keys[it].toJavaStringOrNull() ?: "" }
}

fun JsDynamic.toObjectMap(): Map<String, JsDynamic?> = (getObjectKeys()).map { key -> key to this[key] }.toMap()
