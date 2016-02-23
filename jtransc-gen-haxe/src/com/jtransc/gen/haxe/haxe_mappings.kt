/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.gen.haxe

import com.jtransc.ast.AstType
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.error.InvalidOperationException
import com.jtransc.gen.ClassMappings
import javax.print.DocFlavor

val HaxeCopyFiles = listOf(
	"HaxeNatives.hx",
	"HaxeFormat.hx",
	"HaxeNativeWrapper.hx",
	"HaxeBaseArray.hx",
	"HaxeByteArray.hx",
	"HaxeShortArray.hx",
	"HaxeIntArray.hx",
	"HaxeFloatArray.hx",
	"HaxeDoubleArray.hx",
	"HaxeLongArray.hx",
	"HaxeArray.hx"
)

//val HaxeFeatures = setOf(GotosFeature, SwitchesFeature)
val HaxeFeatures = setOf(SwitchesFeature)

val HaxeKeywords = setOf(
	"java",
	"package",
	"import",
	"class", "interface", "extends", "implements",
	"internal", "private", "protected", "final",
	"function", "var", "const",
	"if", "else",
	"switch", "case", "default",
	"do", "while", "for", "each", "in",
	"break", "continue",
	"int", "uint", "void",
	"goto"
)

enum class HaxeSubtarget(val switch: String, val singleFile: Boolean, val interpreter: String? = null) {
	JS(switch = "-js", singleFile = true, interpreter = "node"),
	CPP(switch = "-cpp", singleFile = false, interpreter = null),
	SWF(switch = "-swf", singleFile = true, interpreter = null),
	NEKO(switch = "-neko", singleFile = true, interpreter = "neko"),
	PHP(switch = "-php", singleFile = false, interpreter = "php"),
	CS(switch = "-cs", singleFile = false, interpreter = null),
	JAVA(switch = "-java", singleFile = false, interpreter = "java -jar"),
	PYTHON(switch = "-python", singleFile = true, interpreter = "python")
	;

	companion object {
		fun fromString(subtarget: String) = when (subtarget.toLowerCase()) {
			"" -> HaxeSubtarget.JS
			"js", "javascript" -> HaxeSubtarget.JS
			"cpp", "c", "c++" -> HaxeSubtarget.CPP
			"swf", "flash", "as3" -> HaxeSubtarget.SWF
			"neko" -> HaxeSubtarget.NEKO
			"php" -> HaxeSubtarget.PHP
			"cs", "c#" -> HaxeSubtarget.CS
			"java" -> HaxeSubtarget.JAVA
			"python" -> HaxeSubtarget.PYTHON
			else -> throw InvalidOperationException("Unknown subtarget '$subtarget'")
		}
	}
}

fun HaxeMappings(): ClassMappings {
	return ClassMappings()
}