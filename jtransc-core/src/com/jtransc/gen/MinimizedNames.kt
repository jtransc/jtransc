package com.jtransc.gen

object MinimizedNames {
	private val firstTypeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	private val firstFieldChars = "abcdefghijklmnopqrstuvwxyz"
	private val laterChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_"

	fun genName(id:Int, first:String, later:String):String {
		var out = ""

		var value = id

		out += first[value % first.length]
		value /= first.length

		while (value != 0) {
			out += later[value % later.length]
			value /= later.length
		}

		return out
	}

	fun getTypeNameById(id:Int) = genName(id, firstTypeChars, laterChars)
	fun getIdNameById(id:Int) = genName(id, firstFieldChars, laterChars)
}