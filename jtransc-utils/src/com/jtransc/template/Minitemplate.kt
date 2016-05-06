package com.jtransc.template

import com.jtransc.ds.ListReader
import com.jtransc.error.noImpl
import com.jtransc.lang.Reflect
import com.jtransc.text.StrReader
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.readUntil
import com.jtransc.text.readWhile

class Minitemplate(val template: String) {
	val templateTokens = Token.tokenize(template)
	val node = BlockNode.parse(templateTokens)

	private fun Context.evaluate(it: BlockNode) {
		when (it) {
			is BlockNode.GROUP -> for (n in it.children) this.evaluate(n)
			is BlockNode.TEXT -> this.write(it.content)
			is BlockNode.EXPR -> this.write(this.evaluate(it.expr).toString())
			is BlockNode.FOR -> {
				this.createScope {
					for (v in Reflect.toIterable(this.evaluate(it.expr))) {
						this.scope[it.varname] = v
						this.evaluate(it.loop)
					}
				}
			}
			else -> noImpl("Not implemented $it")
		}
	}

	class Scope(val map: Any?, val parent: Scope? = null){
		operator fun get(key: Any?): Any? {
			return Reflect.accessAny(map, key) ?: parent?.get(key)
		}
		operator fun set(key: Any?, value: Any?) {
			Reflect.setAny(map, key, value)
		}
	}

	private fun Context.evaluate(node: ExprNode): Any? {
		return when (node) {
			is ExprNode.VAR -> this.scope[node.name]
			is ExprNode.LIT -> node.value
			is ExprNode.ACCESS -> Reflect.accessAny(this.evaluate(node.expr), this.evaluate(node.name))
			else -> noImpl("Not implemented $node")
		}
	}

	operator fun invoke(args: Any?): String {
		val ctx = Context(Scope(args))
		ctx.createScope {
			ctx.evaluate(node)
		}
		return ctx.str.toString()
	}

	class Context(var scope: Scope) {
		var str = StringBuilder()

		fun write(text: String) = this.apply { str.append(text) }
		inline fun createScope(callback: () -> Unit) = this.apply {
			val old = this.scope
			this.scope = Scope(hashMapOf<Any?, Any?>(), old)
			callback()
			this.scope = old
		}
	}

	interface ExprNode {
		data class VAR(val name: String) : ExprNode
		data class LIT(val value: Any?) : ExprNode
		data class ACCESS(val expr: ExprNode, val name: ExprNode) : ExprNode
		data class BINOP(val l: ExprNode, val r: ExprNode, val op: String) : ExprNode
		data class UNOP(val r: ExprNode, val op: String) : ExprNode

		companion object {
			fun parse(str: String): ExprNode {
				val tokens = Token.tokenize(str)
				val result = parse(ListReader(tokens))
				return result
			}

			fun parse(r: ListReader<Token>): ExprNode {
				return parseExpr(r)
			}

			fun parseExpr(r: ListReader<Token>): ExprNode {
				var construct: ExprNode = ExprNode.VAR(r.read().text)
				loop@while (true) {
					when (r.peek().text) {
						"." -> {
							r.read()
							val id = r.read().text
							construct = ExprNode.ACCESS(construct, ExprNode.LIT(id))
							continue@loop
						}
						else -> break@loop
					}
				}
				return construct
			}
		}

		interface Token {
			val text:String
			data class TId(override val text:String) : Token
			data class TNumber(override val text:String) : Token
			data class TString(override val text:String) : Token
			data class TOperator(override val text:String) : Token
			data class TEnd(override val text:String = "") : Token

			companion object {
				fun tokenize(str: String): List<Token> {
					val r = StrReader(str)
					val out = arrayListOf<Token>()
					fun emit(str: Token) {
						out += str
					}
					while (r.hasMore) {
						r.skipSpaces()
						val id = r.readWhile { it.isLetterDigitOrUnderscore() }
						if (id != null) emit(TId(id))
						r.skipSpaces()
						val symbol = r.readWhile { !it.isLetterDigitOrUnderscore() && !it.isWhitespace() }
						if (symbol != null) emit(TOperator(symbol))
					}
					emit(TEnd())
					return out
				}
			}
		}
	}

	interface BlockNode {
		data class GROUP(val children: List<BlockNode>) : BlockNode
		data class TEXT(val content: String) : BlockNode
		data class EXPR(val expr: ExprNode) : BlockNode
		data class IF(val cond: BlockNode, val trueContent: BlockNode, val falseContent: BlockNode?) : BlockNode
		data class FOR(val varname: String, val expr: ExprNode, val loop: BlockNode) : BlockNode

		companion object {
			fun group(children: List<BlockNode>): BlockNode = if (children.size == 1) children[0] else GROUP(children.toList())

			fun parse(tokens: List<Token>): BlockNode {
				val tr = ListReader(tokens)
				fun handle(tag: Tag, token: Token.TTag): BlockNode {
					val parts = arrayListOf<TagPart>()
					var currentToken = token
					val children = arrayListOf<BlockNode>()

					fun emitPart() {
						parts += TagPart(currentToken, BlockNode.group(children))
					}

					loop@while (!tr.eof) {
						val it = tr.read()
						when (it) {
							is Token.TLiteral -> children += BlockNode.TEXT(it.content)
							is Token.TExpr -> children += BlockNode.EXPR(ExprNode.parse(it.content))
							is Token.TTag -> {
								when (it.name) {
									tag.end -> break@loop
									in tag.nextList -> {
										emitPart()
										currentToken = it
										children.clear()
									}
									else -> {
										val newtag = Tag.ALL_MAP[it.name]!!
										if (tag.end != null) {
											children += handle(newtag, it)
										} else {
											children += newtag.buildNode(listOf(TagPart(it, BlockNode.TEXT(""))))
										}
									}
								}
							}
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

	data class Tag(val name: String, val nextList: Set<String>, val end: String?, val buildNode: (parts: List<TagPart>) -> BlockNode) {
		companion object {
			val EMPTY = Tag("", setOf(""), "") { parts ->
				BlockNode.group(parts.map { it.body })
			}
			val IF = Tag("if", setOf("elseif", "else"), "end") { parts ->
				noImpl
			}
			val FOR = Tag("for", setOf("else"), "end") { parts ->
				val main = parts[0]
				val parts2 = main.token.content.split("in", limit = 2).map { it.trim() }
				BlockNode.FOR(parts2[0], ExprNode.parse(parts2[1]), main.body)
			}
			val SET = Tag("set", setOf(), null) {
				noImpl
			}

			val ALL = listOf(EMPTY, IF, FOR, SET)
			val ALL_MAP = ALL.associateBy { it.name }
		}
	}

	interface Token {
		data class TLiteral(val content: String) : Token
		data class TExpr(val content: String) : Token
		data class TTag(val name: String, val content: String) : Token

		companion object {
			private val TOKENS = Regex("(\\{[%\\{])(.*?)[%\\}]\\}")
			fun tokenize(str: String): List<Token> {
				val out = arrayListOf<Token>()
				var lastPos = 0

				fun emit(token: Token) {
					if (token is TLiteral && token.content.isEmpty()) return
					out += token
				}

				for (tok in TOKENS.findAll(str)) {
					emit(TLiteral(str.substring(lastPos until tok.range.start)))
					val content = str.substring(tok.groups[2]!!.range).trim()
					if (tok.groups[1]?.value == "{{") {
						emit(TExpr(content))
					} else {
						val parts = content.split(' ', limit = 2)
						emit(TTag(parts[0], parts.getOrElse(1) { "" }))
					}
					lastPos = tok.range.endInclusive + 1
				}
				emit(TLiteral(str.substring(lastPos, str.length)))
				return out
			}
		}
	}
}
