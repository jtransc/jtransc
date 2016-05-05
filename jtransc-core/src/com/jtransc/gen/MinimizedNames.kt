package com.jtransc.gen

object MinimizedNames {
	private val firstTypeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	private val firstFieldChars = "abcdefghijklmnopqrstuvwxyz"
	//private val laterFieldChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_"
	private val laterFieldChars = "abcdefghijklmnopqrstuvwxyz0123456789_"
	private val laterTypeChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"

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

	fun getTypeNameById(id:Int) = genName(id, firstTypeChars, laterTypeChars)
	fun getIdNameById(id:Int) = genName(id, firstFieldChars, laterFieldChars)
}