package com.jtransc.sourcemaps

import com.jtransc.error.noImpl
import com.jtransc.json.Json
import java.util.*

object Sourcemaps {
	fun decodeRaw(str: String): List<List<List<Int>>> {
		return str.split(";").map {
			it.split(",").map { Base64Vlq.decode(it) }
		}
	}

	fun encodeRaw(items: List<List<List<Int>>>): String {
		return items.map {
			it.map { Base64Vlq.encode(it) }.joinToString(",")
		}.joinToString(";")
	}

	data class MappingItem(val sourceIndex: Int, val sourceLine: Int, val sourceColumn: Int, val targetColumn: Int)
	data class MappingRow(val mappings: List<MappingItem>)
	data class MappingFile(val rows: List<MappingRow>)

	data class TargetMapping(val name: String, val line: Int, val column: Int)

	data class SourceMap(
		val version: Int = 3,
		val file: String = "unknown.js",
		val sourceRoot: String = "file:///",
		val sources: List<String> = arrayListOf<String>(),
		val names: List<String> = arrayListOf<String>(),
		val mappings: String = ""
	) {
		val mappingFile = decode(mappings)

		fun decodePos(line: Int, column: Int = 0): TargetMapping? {
			for (item in mappingFile.rows.getOrNull(line)?.mappings ?: listOf()) {
				if (item.targetColumn >= column) {
					return TargetMapping(sourceRoot + sources[item.sourceIndex], item.sourceLine, item.sourceColumn)
				}
			}
			return null
		}
	}

	fun decodeFile(str: String) = Json.decodeTo<SourceMap>(str)

	fun decode(str: String): MappingFile {
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

	fun encode(mapping: MappingFile): String {
		noImpl("no impl encoding!")
	}

	fun encodeFile(targetPath: String, targetContent: String, source: String, mappings: HashMap<Int, Int>): String {
		//(0 until targetContent.count { it == '\n' }).map {}
		return Json.encode(mapOf(
			"version" to 3,
			"file" to targetPath,
			"sources" to arrayListOf(source),
			"names" to arrayListOf<String>()
		), prettify = true)
	}
}