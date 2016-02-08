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

class Indenter : ToString {
	interface Action
	data class Line(val str: String) : Action
	data class LineDeferred(val callback: () -> Indenter) : Action

	object Indent : Action

	object Unindent : Action

	private val actions = arrayListOf<Action>()

	val noIndentEmptyLines = true

	companion object {
		fun genString(init: Indenter.() -> Unit) = gen(init).toString()

		fun gen(init: Indenter.() -> Unit): Indenter {
			val indenter = Indenter()
			indenter.init()
			return indenter
		}
	}

	var indents: String = ""
	var out: String = ""

	fun line(indenter: Indenter): Indenter {
		this.actions.addAll(indenter.actions)
		return this
	}

	fun line(str: String): Indenter {
		this.actions.add(Line(str))
		return this
	}

	fun linedeferred(init: Indenter.() -> Unit): Indenter {
		this.actions.add(LineDeferred({
			val indenter = Indenter()
			indenter.init()
			indenter
		}))
		return this
	}

	fun line(str: String, callback: () -> Unit): Indenter {
		line("$str {")
		indent(callback)
		line("}")
		return this
	}

	fun line(str: String, after: String, callback: () -> Unit): Indenter {
		line("$str { $after")
		indent(callback)
		line("}")
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
		actions.add(Indent)
	}

	fun _unindent() {
		actions.add(Unindent)
	}

	override fun toString(): String {
		val chunks = arrayListOf<String>()

		var indents = ""

		fun eval(actions: List<Action>) {
			for (action in actions) {
				when (action) {
					is Line -> {
						if (action.str == "" && noIndentEmptyLines) {
							chunks.add("\n")
						} else {
							chunks.add(indents + action.str + "\n")
						}
					}
					is LineDeferred -> {
						eval(action.callback().actions)
					}
					Indent -> {
						indents += "\t"
					}
					Unindent -> {
						indents = indents.substring(0, indents.length - 1)
					}
				}
			}
		}

		eval(actions)

		return chunks.joinToString("")
	}
}

/*
class IndentStringBuilder {
	val noIndentEmptyLines = true

	companion object {
		fun gen(init: IndentStringBuilder.() -> Unit): String {
			val builder = IndentStringBuilder()
			builder.init()
			return builder.toString()
		}
	}

	var indents: String = ""
	var out: String = ""

	fun line(str: String) {
		if (str == "" && noIndentEmptyLines) {
			out += "\n"
		} else {
			out += indents + str + "\n"
		}
	}

	fun line(str: String, callback: () -> Unit) {
		line("$str {")
		indent(callback)
		line("}")
	}

	fun line(str: String, after:String, callback: () -> Unit) {
		line("$str { $after")
		indent(callback)
		line("}")
	}

	inline fun indent(callback: () -> Unit) {
		_indent()
		try {
			callback()
		} finally {
			_unindent()
		}
	}

	fun _indent() {
		indents += "\t"
	}

	fun _unindent() {
		indents = indents.substring(0, indents.length - 1)
	}

	override fun toString() = out
}
*/
