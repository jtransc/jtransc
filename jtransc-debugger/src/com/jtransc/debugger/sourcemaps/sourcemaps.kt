package com.jtransc.debugger.sourcemaps

import io.vertx.core.json.Json

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

	data class TargetMapping(val name: String, val line: Int, val column: Int)

	class SourceMap() {
		@JvmField var version = 3
		@JvmField var file = "unknown.js"
		@JvmField var sourceRoot = "file:///"
		@JvmField var sources = arrayListOf<String>()
		@JvmField var names = arrayListOf<String>()
		@JvmField var mappings = ""
		@JvmField var mappingFile = MappingFile(listOf())
		//"version":3,
		//"file":"test.js",
		//"sourceRoot":"file:///",
		//"sources":["C:/projects/jtransc/Test.hx"],
		//"names":[],
		//"mappings":";;;YACe,WAAgB;AAAA,CAC7B,YAAM;CACN,YAAM;CACN,YAAM;;;;;"

		fun decodePos(line:Int, column:Int = 0): TargetMapping? {
			for (item in mappingFile.rows[line].mappings) {
				if (item.targetColumn >= column) {
					return TargetMapping(sourceRoot + sources[item.sourceIndex], item.sourceLine, item.sourceColumn)
				}
			}
			return null
		}

		fun init() = this.apply {
			mappingFile = decode(mappings)
		}
	}

	fun decodeFile(str:String):SourceMap {
		return Json.decodeValue(str, SourceMap::class.java).init()
	}

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