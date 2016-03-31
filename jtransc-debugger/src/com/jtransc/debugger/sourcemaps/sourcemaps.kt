package com.jtransc.debugger.sourcemaps

object Sourcemaps {
	fun decodeRaw(str:String):List<List<List<Int>>> {
		return str.split(";").map {
			it.split(",").map { Base64Vlq.decode(it) }
		}
	}
}