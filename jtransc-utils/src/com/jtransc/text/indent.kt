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

package com.jtransc.text

import java.util.*

object INDENTS {
	private val INDENTS = arrayListOf<String>("")

	operator fun get(index: Int): String {
		if (index >= INDENTS.size) {
			val calculate = INDENTS.size * 10
			var indent = INDENTS[INDENTS.size - 1]
			while (calculate >= INDENTS.size) {
				indent += "\t"
				INDENTS.add(indent)
			}
		}
		return if (index <= 0) "" else INDENTS[index]
	}
}

class Indenter(private val actions: ArrayList<Action> = arrayListOf<Indenter.Action>()) : ToString {
	interface Action {
		data class Marker(val data: Any) : Action
		data class Line(val str: String) : Action
		data class LineDeferred(val callback: () -> Indenter) : Action
		object Indent : Action
		object Unindent : Action
	}

	val noIndentEmptyLines = true

	companion object {
		fun genString(init: Indenter.() -> Unit) = gen(init).toString()

		val EMPTY = Indenter.gen { }

		inline fun gen(init: Indenter.() -> Unit): Indenter {
			val indenter = Indenter()
			indenter.init()
			return indenter
		}

		inline operator fun invoke(init: Indenter.() -> Unit): Indenter {
			val indenter = Indenter()
			indenter.init()
			return indenter
		}

		fun single(str: String): Indenter = Indenter(arrayListOf(Action.Line(str)))

		operator fun invoke(str: String): Indenter = single(str)

		fun replaceString(templateString: String, replacements: Map<String, String>): String {
			val pattern = Regex("\\$(\\w+)")
			return pattern.replace(templateString) { result ->
				replacements[result.groupValues[1]] ?: ""
			}
		}
	}

	var out: String = ""

	fun line(indenter: Indenter) = this.apply { this.actions.addAll(indenter.actions) }
	fun line(str: String) = this.apply { this.actions.add(Action.Line(str)) }
	fun line(str: String?) {
		if (str != null) line(str)
	}
	fun mark(data: Any) = this.apply { this.actions.add(Action.Marker(data)) }

	fun linedeferred(init: Indenter.() -> Unit): Indenter {
		this.actions.add(Action.LineDeferred({
			val indenter = Indenter()
			indenter.init()
			indenter
		}))
		return this
	}

	inline fun line(str: String, callback: () -> Unit): Indenter {
		line(if (str.isEmpty()) "{" else "$str {")
		indent(callback)
		line("}")
		return this
	}

	inline fun line(str: String, after: String = "", after2:String = "", callback: () -> Unit): Indenter {
		line(if (str.isEmpty()) "{ $after" else "$str { $after")
		indent(callback)
		line("}$after2")
		return this
	}

	inline fun indent(callback: () -> Unit): Indenter {
		_indent()
		try {
			callback()
		} finally {
			_unindent()
		}
		return this
	}

	fun _indent() {
		actions.add(Action.Indent)
	}

	fun _unindent() {
		actions.add(Action.Unindent)
	}

	fun toString(markHandler: ((sb: StringBuilder, line: Int, data: Any) -> Unit)?, doIndent: Boolean): String {
		val out = StringBuilder()
		var line = 0

		var indentIndex = 0

		fun eval(actions: List<Action>) {
			for (action in actions) {
				when (action) {
					is Action.Line -> {
						if (noIndentEmptyLines && action.str.isEmpty()) {
							if (doIndent) out.append('\n')
							line++
						} else {
							if (doIndent) out.append(INDENTS[indentIndex]) else out.append(" ")
							out.append(action.str)
							line += action.str.count { it == '\n' }
							if (doIndent) out.append('\n')
							line++
						}
					}
					is Action.LineDeferred -> eval(action.callback().actions)
					Action.Indent -> indentIndex++
					Action.Unindent -> indentIndex--
					is Action.Marker -> {
						markHandler?.invoke(out, line, action.data)
					}
				}
			}
		}

		eval(actions)

		return out.toString()
	}

	fun toString(markHandler: ((sb: StringBuilder, line: Int, data: Any) -> Unit)?): String = toString(markHandler = markHandler, doIndent = true)
	fun toString(doIndent: Boolean = true): String = toString(markHandler = null, doIndent = doIndent)
	override fun toString(): String = toString(null, doIndent = true)
}
