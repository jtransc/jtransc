package com.jtransc.js

import com.jtransc.annotation.JTranscMethodBody
import com.sun.tracing.dtrace.ArgsAttributes

interface JsDynamic

class JsBoundedMethod(val obj: JsDynamic?, val methodName: String) {
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

@JTranscMethodBody(target = "js", value = "p0[p1] = N.unbox(p2);")
external operator fun JsDynamic?.set(key: Int, value: Any?): Unit

@JTranscMethodBody(target = "js", value = """
	var obj = p0, methodName = N.istr(p1), rawArgs = p2;
	var args = [];
	for (var n = 0; n < rawArgs.length; n++) args.push(N.unbox(rawArgs.data[n]));
	return obj[methodName].apply(obj, args)
""")
external fun JsDynamic?.call(name: String, vararg args: Any?): JsDynamic?

//operator fun JsDynamic?.invoke(name: String, vararg args: Any?): JsDynamic? = this.call(name, *args)

fun jsNew(clazz: String, vararg args: Any?): JsDynamic? = global[clazz].new(*args)

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
external fun <TR> Function0<TR>.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	var handler = p0;
	return function(p1) {
		return N.unbox(handler['{% METHOD kotlin.jvm.functions.Function1:invoke %}'](N.box(p1)));
	};
""")
external fun <T1, TR> Function1<T1, TR>.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	var handler = p0;
	return function(p1, p2) {
		return N.unbox(handler['{% METHOD kotlin.jvm.functions.Function2:invoke %}'](N.box(p1), N.box(p2)));
	};
""")
external fun <T1, T2, TR> Function2<T1, T2, TR>.toJsDynamic(): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	return function() {return N.unbox(p0['{% METHOD kotlin.jvm.functions.Function0:invoke %}']());};
""")
external fun <TR> jsFunctionRaw0(v: Function0<TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	return function(p1) {return N.unbox(p0['{% METHOD kotlin.jvm.functions.Function1:invoke %}'](p1));};
""")
external fun <TR> jsFunctionRaw1(v: Function1<JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	return function(p1, p2) {return N.unbox(p0['{% METHOD kotlin.jvm.functions.Function2:invoke %}'](p1, p2));};
""")
external fun <TR> jsFunctionRaw2(v: Function2<JsDynamic?, JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	return function(p1, p2, p3) {return N.unbox(p0['{% METHOD kotlin.jvm.functions.Function3:invoke %}'](p1, p2, p3));};
""")
external fun <TR> jsFunctionRaw3(v: Function3<JsDynamic?, JsDynamic?, JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = """
	return function(p1, p2, p3, p4) {return N.unbox(p0['{% METHOD kotlin.jvm.functions.Function4:invoke %}'](p1, p2, p3, p4));};
""")
external fun <TR> jsFunctionRaw4(v: Function4<JsDynamic?, JsDynamic?, JsDynamic?, JsDynamic?, TR>): JsDynamic?

@JTranscMethodBody(target = "js", value = """return !!p0;""")
external fun JsDynamic?.toBool(): Boolean

@JTranscMethodBody(target = "js", value = "return p0|0;")
external fun JsDynamic?.toInt(): Int

@JTranscMethodBody(target = "js", value = "return +p0;")
external fun JsDynamic?.toDouble(): Double

@JTranscMethodBody(target = "js", value = "return p0 == p1;")
external infix fun JsDynamic?.eq(that: JsDynamic?): Boolean

@JTranscMethodBody(target = "js", value = "return p0 != p1;")
external infix fun JsDynamic?.ne(that: JsDynamic?): Boolean

@JTranscMethodBody(target = "js", value = "return N.istr(p0);")
external fun String.toJavaScriptString(): JsDynamic?

@JTranscMethodBody(target = "js", value = "debugger;")
external fun jsDebugger(): Unit

class JsMethods(val obj: JsDynamic?) {
	operator fun get(name: String) = JsBoundedMethod(obj, name)
}

val JsDynamic?.methods: JsMethods get() = JsMethods(this)

@JTranscMethodBody(target = "js", value = """
	var out = [];
	for (var n = 0; n < p0.length; n++) out.push(N.unbox(p0.data[n]));
	return out;
""")
external fun jsArray(vararg items: Any?): JsDynamic?

fun jsRegExp(regex: String): JsDynamic? = global["RegExp"].new(regex)
fun jsRegExp(regex: Regex): JsDynamic? = global["RegExp"].new(regex.pattern)
fun Regex.toJs() = jsRegExp(this)

data class JsAssetStat(val path: String, val size: Long)

@JTranscMethodBody(target = "js", value = """
	var out = new JA_L({{ assetFiles|length }}, '[Lcom/jtransc/js/JsAssetStat;');
	var n = 0;
	{% for asset in assetFiles %}
	out.data[n++] = {% CONSTRUCTOR com.jtransc.js.JsAssetStat %}(N.str({{ asset.path|quote }}), Int64.ofFloat({{ asset.size }}));
	{% end %}
	return out
""")
external fun jsGetAssetStats(): Array<JsAssetStat>

@JTranscMethodBody(target = "js", value = """
	var out = {};
	for (var n = 0; n < p0.length; n++) {
		var item = p0.data[n];
		out[N.istr(item["{% FIELD kotlin.Pair:first %}"])] = N.unbox(item["{% FIELD kotlin.Pair:second %}"]);
	}
	return out
""")
external fun jsObject(vararg items: Pair<String, Any?>): JsDynamic?

fun jsObject(map: Map<String, Any?>): JsDynamic? = jsObject(*map.map { it.key to it.value }.toTypedArray())

@JTranscMethodBody(target = "js", value = "return require(N.istr(p0));")
external fun jsRequire(name: String): JsDynamic?

@JTranscMethodBody(target = "js", value = "return this instanceof p0;")
external fun JsDynamic?.jsInstanceOf(type: JsDynamic?): JsDynamic?

fun JsDynamic?.jsIsString(): Boolean = this.typeOf().toJavaString() == "string"

@JTranscMethodBody(target = "js", value = "return JA_B.fromTypedArray(new Int8Array(p0));")
external fun JsDynamic?.toByteArray(): ByteArray

@JTranscMethodBody(target = "js", value = "return new Int8Array(p0.data.buffer, 0, p0.length);")
external fun ByteArray.toJsTypedArray(): JsDynamic?
