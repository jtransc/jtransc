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

package javatest

import com.jtransc.io.JTranscConsole
import java.util.*

object KotlinCollections {
	@JvmStatic fun main(args: Array<String>) {
		JTranscConsole.log("KotlinCollections.main:")
		println(mapOf("a" to 1, "b" to 2).map { it.key }.get(0))
		println(arrayOf(1, 2, 3, 4, 5).map { it * 2 })

		paramOrderSimple(Reader())

		for (textFieldInfo in paramOrderComplex(Reader(), 11)) {
			println(textFieldInfo)
		}

		sortTest()

		println("KotlinCollections:")
		println(D2Target::class.java.newInstance().name)
		println(D3Target().name)
	}

	@JvmStatic fun sortTest() {
		val list = arrayListOf(1, 7, 3, 5, 2, 4, 0)
		println("KotlinCollections.sortTest:")
		println(list)
		println(list.sortedBy { it })
		println(list.sortedBy { -it })
		println(list.sortedBy { if (it % 2 != 0) 0 else it })

		println(".intarray:")
		val array = list.toIntArray()
		println(Arrays.toString(array))
		Arrays.sort(array)
		println(Arrays.toString(array))

		println(".array<Integer>:")
		val array2 = list.toTypedArray()
		println(Arrays.toString(array2))
		Arrays.sort(array2)
		println(Arrays.toString(array2))
	}

	@JvmStatic fun paramOrderSimple(r: Reader) {
		//println("KotlinCollections.paramOrderSimple:")
		println(TripleInt(r.i8(), r.i16(), PairInt(r.i32(), r.i32())))
	}

	@JvmStatic fun paramOrderComplex(r: Reader, version: Int): List<TextFieldInfo> {
		var multiline = false
		fun setMultiline(value: Boolean): Boolean {
			multiline = value
			return value
		}

		return xrange(r.i16()).map {
			TextFieldInfo(
				symbolId = r.i16(),
				symbolName = r.str(),
				initialText = r.str(),
				rectangle = Rectangle(r.i32(), r.i32(), r.i32(), r.i32()),
				fontName = r.str(),
				fontSize = r.i32(),
				textColor = r.i32(),
				hasHtml = r.bool(),
				isEditable = r.bool(),
				isMultiline = setMultiline(r.bool()),
				extended = if (version >= 11) true else false,
				wordWrap = if (version >= 11) r.bool() else multiline,
				leftMargin = if (version >= 11) r.f32() else 0.0,
				rightMargin = if (version >= 11) r.f32() else 0.0,
				indent = if (version >= 11) r.f32() else 0.0,
				leading = if (version >= 11) r.f32() else 0.0,
				align = r.str(),
				autoSize = false
			)
		}
	}
}

class D2Target : GenTargetDescriptor() {
	override val name: String = "d2"
}

class D3Target : GenTargetDescriptor() {
	override val name: String = "d"
}

abstract class GenTargetDescriptor {
	abstract val name: String
}

data class PairInt(val a: Int, val b: Int)
data class TripleInt(val a: Int, val b: Int, val c: PairInt)

@Suppress("NOTHING_TO_INLINE")
inline fun xrange(stop: Int): IntRange = (0..(stop - 1))

data class Rectangle(var x: Int, var y: Int, var width: Int, var height: Int)

data class TextFieldInfo(
	val symbolId: Int,
	val symbolName: String,
	val initialText: String,
	val rectangle: Rectangle,
	val fontName: String,
	val fontSize: Int,
	val textColor: Int,
	val hasHtml: Boolean,
	val isEditable: Boolean,
	val isMultiline: Boolean,
	val extended: Boolean,
	val wordWrap: Boolean,
	val leftMargin: Double,
	val rightMargin: Double,
	val indent: Double,
	val leading: Double,
	val align: String,
	val autoSize: Boolean
)

class Reader {
	var index = 2

	fun f32(): Double = index++.toDouble()
	fun i32(): Int = index++
	fun i16(): Int = index++
	fun i8(): Int = index++
	fun bool(): Boolean = ((index++) % 2) != 0
	fun str(): String = "" + index++
}
