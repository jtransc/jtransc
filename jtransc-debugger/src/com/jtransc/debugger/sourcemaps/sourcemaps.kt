package com.jtransc.debugger.sourcemaps

object Sourcemaps {
	fun decodeRaw(str:String):List<List<List<Int>>> {
		return str.split(";").map {
			it.split(",").map { Base64Vlq.decode(it) }
		}
	}
	fun encodeRaw(items:List<List<List<Int>>>):String {
		return items.map {
			it.map { Base64Vlq.encode(it) }.joinToString(",")
		}.joinToString(";")
	}

	data class MappingItem(val sourceIndex:Int, val sourceLine:Int, val sourceColumn:Int, val targetColumn:Int)
	data class MappingRow(val mappings: List<MappingItem>)
	data class MappingFile(val rows: List<MappingRow>)

	fun decode(str:String):MappingFile {
		var sourceLine = 0
		var sourceIndex = 0
		var sourceColumn = 0

		return MappingFile(decodeRaw(str).map { row ->
			var targetColumn = 0

			MappingRow(row.flatMap { info ->
				if (info.size >= 4) {
					targetColumn += info[0]
					sourceIndex += info[1]
					sourceLine += info[2]
					sourceColumn += info[3]
					listOf(MappingItem(sourceIndex, sourceLine, sourceColumn, targetColumn))
				} else {
					listOf()
				}
			})
		})
	}
}