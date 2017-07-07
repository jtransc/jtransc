package com.jtransc.template

import com.jtransc.ds.ListReader
import com.jtransc.error.invalidOp
import com.jtransc.imaging.ImagePropsDecoder
import com.jtransc.json.Json
import com.jtransc.lang.Dynamic
import com.jtransc.text.*
import java.io.File

class Minitemplate(val template: String, val config: Config = Config()) {
	val templateTokens = Token.tokenize(template)
	val node = BlockNode.parse(templateTokens, config)

	class Config(
		private val extraTags: List<Tag> = listOf(),
		private val extraFilters: List<Filter> = listOf()
	) {
		val integratedFilters = listOf(
			Filter("length") { subject, args -> Dynamic.length(subject) },
			Filter("capitalize") { subject, args -> Dynamic.toString(subject).toLowerCase().capitalize() },
			Filter("upper") { subject, args -> Dynamic.toString(subject).toUpperCase() },
			Filter("lower") { subject, args -> Dynamic.toString(subject).toLowerCase() },
			Filter("trim") { subject, args -> Dynamic.toString(subject).trim() },
			Filter("quote") { subject, args -> Dynamic.toString(subject).quote() },
			Filter("escape") { subject, args -> Dynamic.toString(subject).escape() },
			Filter("json") { subject, args -> Json.encodeAny(subject) },
			Filter("join") { subject, args -> Dynamic.toIterable(subject).map { Dynamic.toString(it) }.joinToString(Dynamic.toString(args[0])) },
			Filter("file_exists") { subject, args -> File(Dynamic.toString(subject)).exists() },
			Filter("image_info") { subject, args ->
				if (subject is ByteArray) {
					ImagePropsDecoder.tryDecodeHeader(subject)
				} else {
					val file = when (subject) {
						is File -> subject
						else -> File(Dynamic.toString(subject))
					}
					ImagePropsDecoder.tryDecodeHeader(file)
				}
			}
		)

		private val allTags = listOf(Tag.EMPTY, Tag.IF, Tag.FOR, Tag.SET, Tag.DEBUG) + extraTags
		private val allFilters = integratedFilters + extraFilters

		val tags = hashMapOf<String, Tag>().apply {
			for (tag in allTags) {
				this[tag.name] = tag
				for (alias in tag.aliases) this[alias] = tag
			}
		}

		val filters = hashMapOf<String, Filter>().apply {
			for (filter in allFilters) this[filter.name] = filter
		}
	}

	data class Filter(val name: String, val eval: (subject: Any?, args: List<Any?>) -> Any?)

	class Scope(val map: Any?, val parent: Scope? = null) {
		operator fun get(key: Any?): Any? {
			return Dynamic.accessAny(map, key) ?: parent?.get(key)
		}

		operator fun set(key: Any?, value: Any?) {
			Dynamic.setAny(map, key, value)
		}
	}

	operator fun invoke(args: Any?): String {
		val str = StringBuilder()
		val context = Context(Scope(args), config) { str.append(it) }
		context.createScope { node.eval(context) }
		return str.toString()
	}

	class Context(var scope: Scope, val config: Config, val write: (str: String) -> Unit) {
		inline fun createScope(callback: () -> Unit) = this.apply {
			val old = this.scope
			this.scope = Scope(hashMapOf<Any?, Any?>(), old)
			callback()
			this.scope = old
		}
	}

	interface ExprNode {
		fun eval(context: Context): Any?

		data class VAR(val name: String) : ExprNode {
			override fun eval(context: Context): Any? = context.scope[name]
		}

		data class LIT(val value: Any?) : ExprNode {
			override fun eval(context: Context): Any? = value
		}

		data class ARRAY_LIT(val items: List<ExprNode>) : ExprNode {
			override fun eval(context: Context): Any? = items.map { it.eval(context) }
		}

		data class FILTER(val name: String, val expr: ExprNode, val params: List<ExprNode>) : ExprNode {
			override fun eval(context: Context): Any? {
				val filter = context.config.filters[name] ?: invalidOp("Unknown filter '$name'")
				return filter.eval(expr.eval(context), params.map { it.eval(context) })
			}
		}

		data class ACCESS(val expr: ExprNode, val name: ExprNode) : ExprNode {
			override fun eval(context: Context): Any? {
				val obj = expr.eval(context)
				val key = name.eval(context)
				try {
					return Dynamic.accessAny(obj, key)
				} catch (t: Throwable) {
					try {
						return Dynamic.callAny(obj, key, listOf())
					} catch (t: Throwable) {
						return null
					}
				}
			}
		}

		data class CALL(val method: ExprNode, val args: List<ExprNode>) : ExprNode {
			override fun eval(context: Context): Any? {
				if (method !is ACCESS) {
					return Dynamic.callAny(method.eval(context), args.map { it.eval(context) })
				} else {
					return Dynamic.callAny(method.expr.eval(context), method.name.eval(context), args.map { it.eval(context) })
				}
			}
		}

		data class BINOP(val l: ExprNode, val r: ExprNode, val op: String) : ExprNode {
			override fun eval(context: Context): Any? = Dynamic.binop(l.eval(context), r.eval(context), op)
		}

		data class UNOP(val r: ExprNode, val op: String) : ExprNode {
			override fun eval(context: Context): Any? = Dynamic.unop(r.eval(context), op)
		}

		companion object {
			fun ListReader<Token>.expectPeek(vararg types: String): Token {
				val token = this.peek()
				if (token.text !in types) throw RuntimeException("Expected ${types.joinToString(", ")}")
				return token
			}

			fun ListReader<Token>.expect(vararg types: String): Token {
				val token = this.read()
				if (token.text !in types) throw RuntimeException("Expected ${types.joinToString(", ")}")
				return token
			}

			fun parse(str: String): ExprNode {
				return parseFullExpr(Token.tokenize(str))
			}

			fun parseId(r: ListReader<ExprNode.Token>): String {
				return r.read().text
			}

			fun expect(r: ListReader<ExprNode.Token>, vararg tokens: String) {
				val token = r.read()
				if (token.text !in tokens) invalidOp("Expected ${tokens.joinToString(", ")} but found $token")
			}

			fun parseFullExpr(r: ListReader<Token>): ExprNode {
				val result = parseExpr(r)
				if (r.hasMore && r.peek() !is Token.TEnd) {
					invalidOp("Expected expression at " + r.peek() + " :: " + r.list.map { it.text }.joinToString(""))
				}
				return result
			}

			private val BINOPS = setOf(
				"+", "-", "*", "/", "%",
				"==", "!=", "<", ">", "<=", ">=", "<=>",
				"&&", "||"
			)

			fun parseExpr(r: ListReader<Token>): ExprNode {
				var result = parseFinal(r)
				while (r.hasMore) {
					if (r.peek() !is Token.TOperator || r.peek().text !in BINOPS) break
					val operator = r.read().text
					val right = parseFinal(r)
					result = ExprNode.BINOP(result, right, operator)
				}
				// @TODO: Fix order!
				return result
			}

			private fun parseFinal(r: ListReader<Token>): ExprNode {

				var construct: ExprNode = when (r.peek().text) {
					"!", "~", "-", "+" -> {
						val op = r.read().text
						ExprNode.UNOP(parseFinal(r), op)
					}
					"(" -> {
						r.read()
						val result = parseExpr(r)
						if (r.read().text != ")") throw RuntimeException("Expected ')'")
						result
					}
				// Array literal
					"[" -> {
						val items = arrayListOf<ExprNode>()
						r.read()
						loop@ while (r.hasMore && r.peek().text != "]") {
							items += parseExpr(r)
							when (r.peek().text) {
								"," -> r.read()
								"]" -> continue@loop
								else -> invalidOp("Expected , or ]")
							}
						}
						r.expect("]")
						ExprNode.ARRAY_LIT(items)
					}
					else -> {
						if (r.peek() is Token.TNumber) {
							ExprNode.LIT(r.read().text.toDouble())
						} else if (r.peek() is Token.TString) {
							ExprNode.LIT((r.read() as Token.TString).processedValue)
						} else {
							ExprNode.VAR(r.read().text)
						}
					}
				}

				loop@ while (r.hasMore) {
					when (r.peek().text) {
						"." -> {
							r.read()
							val id = r.read().text
							construct = ExprNode.ACCESS(construct, ExprNode.LIT(id))
							continue@loop
						}
						"[" -> {
							r.read()
							val expr = parseExpr(r)
							construct = ExprNode.ACCESS(construct, expr)
							val end = r.read()
							if (end.text != "]") throw RuntimeException("Expected ']' but found $end")
						}
						"|" -> {
							r.read()
							val name = r.read().text
							val args = arrayListOf<ExprNode>()
							if (r.peek().text == "(") {
								r.read()
								callargsloop@ while (r.hasMore && r.peek().text != ")") {
									args += parseExpr(r)
									when (r.expectPeek(",", ")").text) {
										"," -> r.read()
										")" -> break@callargsloop
									}
								}
								r.expect(")")
							}
							construct = ExprNode.FILTER(name, construct, args)
						}
						"(" -> {
							r.read()
							val args = arrayListOf<ExprNode>()
							callargsloop@ while (r.hasMore && r.peek().text != ")") {
								args += parseExpr(r)
								when (r.expectPeek(",", ")").text) {
									"," -> r.read()
									")" -> break@callargsloop
								}
							}
							r.expect(")")
							construct = ExprNode.CALL(construct, args)
						}
						else -> break@loop
					}
				}
				return construct
			}
		}

		interface Token {
			val text: String

			data class TId(override val text: String) : Token
			data class TNumber(override val text: String) : Token
			data class TString(override val text: String, val processedValue: String) : Token
			data class TOperator(override val text: String) : Token
			data class TEnd(override val text: String = "") : Token

			companion object {
				private val OPERATORS = setOf(
					"(", ")",
					"[", "]",
					"{", "}",
					"&&", "||",
					"&", "|", "^",
					"==", "!=", "<", ">", "<=", ">=", "<=>",
					"+", "-", "*", "/", "%", "**",
					"!", "~",
					".", ",", ";", ":",
					"="
				)

				fun tokenize(str: String): ListReader<Token> {
					val r = StrReader(str)
					val out = arrayListOf<Token>()
					fun emit(str: Token) {
						out += str
					}
					while (r.hasMore) {
						val start = r.offset
						r.skipSpaces()
						val id = r.readWhile { it.isLetterDigitOrUnderscore() }
						if (id != null) {
							if (id[0].isDigit()) emit(TNumber(id)) else emit(TId(id))
						}
						r.skipSpaces()
						if (r.peek(3) in OPERATORS) emit(TOperator(r.read(3)))
						if (r.peek(2) in OPERATORS) emit(TOperator(r.read(2)))
						if (r.peek(1) in OPERATORS) emit(TOperator(r.read(1)))
						if (r.peekch() == '\'' || r.peekch() == '"') {
							val strStart = r.readch()
							val strBody = r.readUntil { it == strStart } ?: ""
							val strEnd = r.readch()
							emit(TString(strStart + strBody + strEnd, strBody))
						}
						val end = r.offset
						if (end == start) invalidOp("Don't know how to handle '${r.peekch()}'")
					}
					emit(TEnd())
					return ListReader(out)
				}
			}
		}
	}

	interface BlockNode {
		fun eval(context: Context): Unit

		data class GROUP(val children: List<BlockNode>) : BlockNode {
			override fun eval(context: Context) = Unit.apply { for (n in children) n.eval(context) }
		}

		data class TEXT(val content: String) : BlockNode {
			override fun eval(context: Context) = Unit.apply { context.write(content) }
		}

		data class EXPR(val expr: ExprNode) : BlockNode {
			override fun eval(context: Context) = Unit.apply { context.write(Dynamic.toString(expr.eval(context))) }
		}

		data class IF(val cond: ExprNode, val trueContent: BlockNode, val falseContent: BlockNode?) : BlockNode {
			override fun eval(context: Context) = Unit.apply {
				if (Dynamic.toBool(cond.eval(context))) {
					trueContent.eval(context)
				} else {
					falseContent?.eval(context)
				}
			}
		}

		data class FOR(val varname: String, val expr: ExprNode, val loop: BlockNode) : BlockNode {
			override fun eval(context: Context) = Unit.apply {
				context.createScope {
					for (v in Dynamic.toIterable(expr.eval(context))) {
						context.scope[varname] = v
						loop.eval(context)
					}
				}
			}
		}

		data class SET(val varname: String, val expr: ExprNode) : BlockNode {
			override fun eval(context: Context) = Unit.apply {
				context.scope[varname] = expr.eval(context)
			}
		}

		data class DEBUG(val expr: ExprNode) : BlockNode {
			override fun eval(context: Context) = Unit.apply {
				println(expr.eval(context))
			}
		}

		companion object {
			fun group(children: List<BlockNode>): BlockNode = if (children.size == 1) children[0] else GROUP(children)

			fun parse(tokens: List<Token>, config: Config): BlockNode {
				val tr = ListReader(tokens)
				fun handle(tag: Tag, token: Token.TTag): BlockNode {
					val parts = arrayListOf<TagPart>()
					var currentToken = token
					val mutableChildren = arrayListOf<BlockNode>()

					fun emitPart() {
						val clonedChildren = mutableChildren.toList()
						parts += TagPart(currentToken, BlockNode.group(clonedChildren))
					}

					loop@ while (!tr.eof) {
						val it = tr.read()
						when (it) {
							is Token.TLiteral -> mutableChildren += BlockNode.TEXT(it.content)
							is Token.TExpr -> mutableChildren += BlockNode.EXPR(ExprNode.parse(it.content))
							is Token.TTag -> {
								when (it.name) {
									tag.end -> break@loop
									in tag.nextList -> {
										emitPart()
										currentToken = it
										mutableChildren.clear()
									}
									else -> {
										val newtag = config.tags[it.name] ?: invalidOp("Can't find tag ${it.name}")
										if (newtag.end != null) {
											mutableChildren += handle(newtag, it)
										} else {
											mutableChildren += newtag.buildNode(listOf(TagPart(it, BlockNode.TEXT(""))))
										}
									}
								}
							}
							else -> break@loop
						}
					}

					emitPart()

					return tag.buildNode(parts)
				}
				return handle(Tag.EMPTY, Token.TTag("", ""))
			}
		}
	}

	data class TagPart(val token: Token.TTag, val body: BlockNode)

	data class Tag(val name: String, val nextList: Set<String>, val end: String?, val aliases: List<String> = listOf(), val buildNode: (parts: List<TagPart>) -> BlockNode) {
		companion object {
			val EMPTY = Tag("", setOf(""), "") { parts ->
				BlockNode.group(parts.map { it.body })
			}
			val IF = Tag("if", setOf("else"), "end") { parts ->
				val main = parts[0]
				val elseBlock = parts.getOrNull(1)
				BlockNode.IF(ExprNode.parse(main.token.content), main.body, elseBlock?.body)
			}
			val FOR = Tag("for", setOf(), "end") { parts ->
				val main = parts[0]
				val tr = ExprNode.Token.tokenize(main.token.content)
				val varname = ExprNode.parseId(tr)
				ExprNode.expect(tr, "in")
				val expr = ExprNode.parseExpr(tr)
				BlockNode.FOR(varname, expr, main.body)
			}
			val DEBUG = Tag("debug", setOf(), null) { parts ->
				BlockNode.DEBUG(ExprNode.parse(parts[0].token.content))
			}
			val SET = Tag("set", setOf(), null) { parts ->
				val main = parts[0]
				val tr = ExprNode.Token.tokenize(main.token.content)
				val varname = ExprNode.parseId(tr)
				ExprNode.expect(tr, "=")
				val expr = ExprNode.parseExpr(tr)
				BlockNode.SET(varname, expr)
			}
		}
	}

	interface Token {
		data class TLiteral(val content: String) : Token
		data class TExpr(val content: String) : Token
		data class TTag(val name: String, val content: String) : Token

		companion object {
			fun tokenize(str: String): List<Token> {
				val out = arrayListOf<Token>()
				var lastPos = 0

				fun emit(token: Token) {
					if (token is TLiteral && token.content.isEmpty()) return
					out += token
				}

				var pos = 0
				while (pos < str.length) {
					val c = str[pos++]
					if (c == '{') {
						if (pos >= str.length) break
						val c2 = str[pos++]
						if (c2 == '{' || c2 == '%') {
							val startPos = pos - 2
							val pos2 = if (c2 == '{') str.indexOf("}}", pos) else str.indexOf("%}", pos)
							if (pos2 < 0) break
							val content = str.substring(pos, pos2).trim()

							if (lastPos != startPos) {
								emit(TLiteral(str.substring(lastPos until startPos)))
							}

							if (c2 == '{') {
								//println("expr: '$content'")
								emit(TExpr(content))
							} else {
								val parts = content.split(' ', limit = 2)
								//println("tag: '$content'")
								emit(TTag(parts[0], parts.getOrElse(1) { "" }))
							}
							pos = pos2 + 2
							lastPos = pos
						}
					}
				}
				emit(TLiteral(str.substring(lastPos, str.length)))
				return out
			}
		}
	}
}
