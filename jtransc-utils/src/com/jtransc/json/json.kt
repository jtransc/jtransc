package com.jtransc.json

object Json {
	fun decode(str: String): JsonObject = JsonObject(JsonReader(str).readObject())
	fun decodeAny(str: String): Any? = JsonReader(str).readValue()
	fun encode(data: Map<*, *>, prettify: Boolean = false): String = JsonWriter(prettify).writeObject(data).toString()
	fun encodeAny(data: Any?, prettify: Boolean = false): String = JsonWriter(prettify).writeValue(data).toString()

	@Deprecated("", ReplaceWith("Json.decodeTo(str, clazz)"))
	fun <T : Any> decodeValue(str: String, clazz: Class<T>): T = decodeTo(str, clazz)

	fun <T : Any> decodeTo(str: String, clazz: Class<T>): T = Typer.toTyped(decodeAny(str), clazz)
	fun <T : Any> encodeFrom(value: T?, prettify: Boolean = false): String = JsonWriter(prettify).writeValue(Typer.fromTyped(value)).toString()

	inline fun <reified T : Any> decodeTo(str: String): T = decodeTo(str, T::class.java)
	//inline fun <reified T : Any> encodeFrom(obj: T, prettify: Boolean = false): String = encodeFrom(obj, prettify)
}

class JsonObject(val map: Map<*, *>) : Map<Any?, Any?> by map as Map<Any?, Any?> {
	constructor(str: String) : this(Json.decode(str))
	fun getString(key: String): String = map[key] as String
	fun getJsonObject(key: String): JsonObject = JsonObject(map[key] as Map<*, *>)
	fun getArray(key: String): List<*> = (map[key] as Iterable<*>).toList()
	fun getNumber(key: String): Double = (map[key] as Number).toDouble()
	fun getInteger(key: String): Int = (map[key] as Number).toInt()
	fun getBoolean(key: String): Boolean = map[key] as Boolean
	fun getValue(key: String): Any? = map[key]
	fun encode(prettify: Boolean = false): String = Json.encode(map, prettify)
	fun encodePrettily(): String = encode(prettify = true)
	override fun toString(): String = encode()
	inline fun <reified T : Any> to(): T = Typer.toTyped(this, T::class.java)
}