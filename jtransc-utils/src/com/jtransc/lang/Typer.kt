package com.jtransc.lang

object Typer {
	fun <T : Any> toTyped(value: Any?, target: Class<T>): T = Reflect.dynamicCast(value, target)!!
	fun <T : Any> fromTyped(value: T?): Any? = Reflect.fromTyped(value)
}
