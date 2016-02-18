package com.jtransc.js

import com.jtransc.ast.*
import com.jtransc.ast.feature.SwitchesFeature
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.toStringWithStackTrace
import com.jtransc.gen.GenTarget
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.GenTargetInfo
import com.jtransc.gen.GenTargetProcessor
import com.jtransc.io.ProcessResult2
import com.jtransc.lang.getResourceAsString
import com.jtransc.text.Indenter
import com.jtransc.text.escape
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import javax.script.ScriptEngineManager

object JsGenDescriptor : GenTargetDescriptor() {
	override val name = "js"
	override val longName = "Javascript ES5-compatible"
	override val sourceExtension = "js"
	override val outputExtension = "js"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override fun getGenerator() = GenJs
}

object GenJs : GenTarget {
	val FEATURES = setOf(SwitchesFeature)
	override val runningAvailable: Boolean = true

	private val FqName.js: String get() = fqname

	private val AstType.jsDefault: String get() = when (this) {
		is AstType.BOOL -> "false"
		is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> "0"
		is AstType.LONG -> "Long.ZERO"
		is AstType.FLOAT, is AstType.DOUBLE -> "0.0"
		is AstType.REF -> "null"
		is AstType.ARRAY -> "null"
		is AstType.NULL -> "null"
		else -> throw RuntimeException("Not supported JavaScript type $this")
	}

	private val AstType.js: String get() = when (this) {
		is AstType.REF -> "JVM.classes['${name.js}']"
		else -> throw RuntimeException("Not supported JavaScript type $this")
	}

	private val AstMethodRef.mangledName: String get() = "$name${type.desc}"

	private val AstMember.mangledName: String get() = when (this) {
		is AstField -> name
		is AstMethod -> ref.mangledName
		else -> throw RuntimeException("Not supported JavaScript type $this")
	}

	private val AstProgram.fileName: String get() = entrypoint.js + ".js"
	private val AstClass.jsName: String get() = name.js
	private val AstMethodRef.jsName: String get() = mangledName

	override fun getProcessor(tinfo: GenTargetInfo): GenTargetProcessor {
		val outputFilePath = tinfo.outputFile
		val outFile = File(outputFilePath).absoluteFile
		val srcFolder = LocalVfs(outFile.parentFile.absolutePath).ensuredir()

		return object : GenTargetProcessor {
			override fun buildSource() {
				println("\n\nGenerating JS file: $outputFilePath")
				write(tinfo.program, AstFeatures(), srcFolder)
			}

			override fun compile(): Boolean {
				return true
			}

			override fun run(redirect: Boolean): ProcessResult2 {
				try {
					val engine = ScriptEngineManager().getEngineByMimeType("text/javascript")
					val output = "" + engine.eval(listOf(
						"(function() {",
						"var result = '';",
						"var console = { log: function(s) { result += s + '\\n'; } };",
						srcFolder["program.js"].readString(),
						"return result;",
						"})()"
					).joinToString("\n"))

					return ProcessResult2(output, 0)
				} /*catch (t: ScriptException) {
					return ProcessResult2(t.cause!!.toStringWithStackTrace(), -1)
				} */ catch (t: Throwable) {
					return ProcessResult2(t.toStringWithStackTrace(), -1)

				}
			}

		}
	}

	private fun write(program: AstProgram, features: AstFeatures, vfs: SyncVfsFile): SyncVfsFile {
		var programBlocks = arrayListOf<String>()
		programBlocks.add(javaClass.getResourceAsString("/long.js"))
		programBlocks.add(javaClass.getResourceAsString("/rt.js"))
		programBlocks.add(Indenter.gen {
			line("var JVM = Jvm.create(function(JVM) {")
			indent {
				for (clazz in program.classes) {
					line(gen(program, clazz, features))
				}
				for (clazz in program.classes) {
					val method = clazz.methodsByName["<clinit>"]?.firstOrNull()
					if (method != null) {
						line("JVM.classes['${clazz.jsName}']['<clinit>()V']();")
					}
				}
			}
			line("});")
		}.toString())
		programBlocks.add(javaClass.getResourceAsString("/rt-after.js"))
		programBlocks.add("JVM.classes['${program.entrypoint}']['main([Ljava/lang/String;)V']([]);")
		vfs["program.js"] = programBlocks.joinToString("\n")
		return vfs
	}

	fun gen(program: AstProgram, clazz: AstClass, features: AstFeatures): Indenter = Indenter.gen {
		val extending = clazz.extending?.fqname
		val extending2 = if (!extending.isNullOrEmpty()) "'$extending'" else "null"
		val implementing = clazz.implementing.map { "'${it.fqname}'" }.joinToString(", ")
		line("JVM.registerClass('${clazz.name.fqname}', $extending2, [$implementing], function (ctx) {")
		indent {
			clazz.fields.forEach { field ->
				line("ctx.registerField(${field.isStatic}, '${field.name}', '${field.type.mangle()}');")
			}
			clazz.methods.forEach { method ->
				val margs = method.methodType.argNames
				line("ctx.registerMethod(${method.isStatic}, '${method.name}', '${method.type.mangle()}', function(${margs.joinToString(", ")}) {")
				indent {
					if (method.body != null) {
						line(gen(program, features.apply(method.body!!, FEATURES), method.isAsync(program)))
					} else {
						line("throw new Error('Native or abstract :${clazz.name}: ${method.name}${method.desc} ');")
					}
				}
				line("});")
			}
		}
		line("});")
	}

	fun gen(astProgram: AstProgram, body: AstBody, async: Boolean): Indenter = Indenter.gen {
		for (local in body.locals) {
			line("var ${local.name} = ${local.type.jsDefault};")
		}
        line("var me = this;")
        if (async) {
            when (body.stm) {
                is AstStm.WHILE -> {
                    line("function async(_gotostate) ") {
                        line(gen(astProgram, body.stm, true))
                    }
                    line("return async(0);")
                }
                else -> {
                    line("var _gotostate = 0;")
                    line("function async(_gotostate) ") {
                        line("while(true)") {
                            line("switch (_gotostate) ") {
                                line("case 0:")
                                indent { line(gen(astProgram, body.stm, true)) }
                            }
                        }
                    }
                    line("return async(0);")
                }
            }
        } else {
            line(gen(astProgram, body.stm))
        }
	}

    var labelCounter: Int = 0

    fun nextLabel(): String = "l${labelCounter++}"

	fun gen(astProgram: AstProgram, stm: AstStm, async: Boolean = false): Indenter = Indenter.gen {
		when (stm) {
			is AstStm.IF -> {
				line("if (${stm.cond.gen()})") { line(gen(astProgram, stm.strue, async)) }
				if (stm.sfalse != null) {
					line("else") { line(gen(astProgram, stm.sfalse!!, async)) }
				}
			}
			is AstStm.RETURN -> {
                if (async) {
                    line("return Promise.resolve(${stm.retval?.gen() ?: ""});")
                } else {
                    line("return ${stm.retval?.gen() ?: ""};")
                }
			}
			is AstStm.SET -> {
                if (async && (stm.expr is AstExpr.CALL_BASE) && ((stm.expr as AstExpr.CALL_BASE).method.isAsync(astProgram))) {
                    line("var promise = ${stm.expr.gen()};")
                    val promiseLabel = nextLabel()
                    line("return promise.then(function(res) { ${stm.local.name} = res; return async('$promiseLabel'); });")
                    line("case '$promiseLabel':")
                } else {
                    line("${stm.local.name} = ${stm.expr.gen()}; /* SET */")
                }
            }
			is AstStm.SET_ARRAY -> line("${stm.local.name}[${stm.index.gen()}] = ${stm.expr.gen()};  /* SET_ARRAY */")
			is AstStm.SET_FIELD_STATIC -> line("${stm.clazz.js}.${stm.field.name} = ${stm.expr.gen()}; /* SET_FIELD_STATIC */")
			is AstStm.SET_FIELD_INSTANCE -> line("${stm.left.gen()}.${stm.field.name} = ${stm.expr.gen()}; /* SET_FIELD_INSTANCE */")
			is AstStm.STM_EXPR -> line("${stm.expr.gen()}; /* STM_EXPR */")
			is AstStm.STMS -> for (s in stm.stms) line(gen(astProgram, s, async))
			is AstStm.STM_LABEL -> line("${stm.label.name}:;")
			is AstStm.GOTO -> line("goto ${stm.label.name};")
			is AstStm.BREAK -> line("break;")
			is AstStm.CONTINUE -> line("continue;")
			is AstStm.WHILE -> {
				line("while (${stm.cond.gen()})") {
					line(gen(astProgram, stm.iter, async))
				}
			}
			is AstStm.SWITCH -> {
				line("switch (${stm.subject.gen()})") {
					for (case in stm.cases) {
						val value = case.first
						val caseStm = case.second
						line("case $value:")
						indent {
							line(gen(astProgram, caseStm, async))
						}
						line("break;")
					}
					line("default:")
					indent {
						line(gen(astProgram, stm.default, async))
					}
					line("break;")
				}
			}
			is AstStm.TRY_CATCH -> {
				line("try") {
					line(gen(astProgram, stm.trystm, async))
				}
				line("catch (e)") {
					line(gen(astProgram, stm.catch, async))
				}
				/*
				for ((type, cstm) in stm.catches) {
				}
				*/
			}
			is AstStm.THROW -> line("throw ${stm.value.gen()};")
			is AstStm.SET_NEW_WITH_CONSTRUCTOR -> {
				val commaArgs = stm.args.map { it.gen() }.joinToString(", ")
				val className = stm.target.js
				line("/* SET_NEW_WITH_CONSTRUCTOR */")
				line("${stm.local.name} = new $className();")
				line("${stm.local.name}['${stm.method.jsName}']($commaArgs);")
			}
			is AstStm.NOP -> line("/* NOP */")
			is AstStm.MONITOR_ENTER -> {
				line("/* MONITOR_ENTER */")
			}
			is AstStm.MONITOR_EXIT -> {
				line("/* MONITOR_EXIT */")
			}
			else -> throw RuntimeException("Unhandled statement $stm")
		}
	}

	fun AstExpr.gen(): String = when (this) {
		is AstExpr.THIS -> "me"
		is AstExpr.LITERAL -> {
			val value = this.value
			when (value) {
				null -> "null"
				is Boolean -> if (value) "true" else "false"
				is Int, is Double, is Float -> "$value"
                is Long -> "Long.fromString('$value')"
				is String -> "\"" + value.escape() + "\""
				else -> throw NotImplementedError("Not yet implemented $this")
			}
		}
		is AstExpr.PARAM -> "${this.argument.name}"
		is AstExpr.LOCAL -> "${this.local.name}"
		is AstExpr.UNOP -> {
			val r = right.gen()
			when (type) {
				is AstType.LONG -> "Long.${op.str}($r)"
				else -> "${op.symbol}$r"
			}
		}
		is AstExpr.BINOP -> {
			val l = left.gen()
			val r = right.gen()

			when (type) {
				is AstType.LONG -> "$l.${op.str}($r)"
                is AstType.INT -> "($l ${op.symbol} $r) | 0"
			// @TODO: Short, should truncate (<< 16 >> 16) or & 0x0000FFFF
				else -> when (op.symbol) {
					"cmp", "cmpl", "cmpg" -> "all.native.JsNatives.${op.symbol}($l, $r)"
					else -> "$l ${op.symbol} $r"
				}
			}

		}
		is AstExpr.CALL_BASE -> {
			val base = when (this) {
				is AstExpr.CALL_STATIC -> "${clazz.js}"
				is AstExpr.CALL_INSTANCE -> "${obj.gen()}"
				is AstExpr.CALL_SUPER -> "JVM.classes['${this.target.fqname}'].prototype"
				else -> throw InvalidOperationException("Unexpected $this")
			}
			val sargs = args.map { it.gen() }
			if (this is AstExpr.CALL_SUPER) {
				"$base['${method.jsName}'].call(${ (listOf("this") + sargs).joinToString(", ") })"
			} else {
				"$base['${method.jsName}'](${ sargs.joinToString(", ") })"
			}
		}
		is AstExpr.STATIC_FIELD_ACCESS -> {
			"${clazzName.js}['${field.name}'] /* STATIC_FIELD_ACCESS */"
		}
		is AstExpr.INSTANCE_FIELD_ACCESS -> {
			"${expr.gen()}['${field.name}'] /* INSTANCE_FIELD_ACCESS */"
		}
		is AstExpr.ARRAY_LENGTH -> {
			"${array.gen()}.length"
		}
		is AstExpr.ARRAY_ACCESS -> {
			"${array.gen()}[${index.gen()}]"
		}
		is AstExpr.CAST -> {
			val e = expr.gen()
			if (from == to) {
				"$e"
			} else {
				when (from) {
					is AstType.INT, is AstType.CHAR, is AstType.SHORT, is AstType.BYTE, is AstType.FLOAT, is AstType.DOUBLE -> {
						when (to) {
							is AstType.INT, is AstType.LONG -> "($e)"
							is AstType.FLOAT, is AstType.DOUBLE -> "($e)"
							is AstType.BOOL -> "(($e) != 0)"
							is AstType.CHAR -> "(($e) & 0xFFFF)"
							is AstType.SHORT -> "((($e) << 16) >> 16)"
							is AstType.BYTE -> "((($e) << 24) >> 24)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.REF, is AstType.ARRAY -> {
						when (to) {
							is AstType.REF -> "($e)"
							is AstType.ARRAY -> "($e) /* REF->ARRAY $this */"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.LONG -> {
						when (to) {
							is AstType.INT, is AstType.FLOAT, is AstType.DOUBLE -> "($e)"
							else -> throw NotImplementedError("Unhandled conversion $from -> $to")
						}
					}
					is AstType.NULL -> {
						"$e"
					}
					else -> throw NotImplementedError("Unhandled conversion $from -> $to")
				}
			}
		}
		is AstExpr.NEW -> {
			when (target) {
				is AstType.REF -> "new ${target.js}()"
				else -> throw NotImplementedError("Unhandled NEW for type $target")
			}
		}
		is AstExpr.NEW_ARRAY -> {
			"[]"
		}
		is AstExpr.CLASS_CONSTANT -> {
			"${this} /* CLASS_CONSTANT  */"
		}
		is AstExpr.CAUGHT_EXCEPTION -> {
			"null /*CAUGHT_EXCEPTION*/"
		}
		is AstExpr.INSTANCE_OF -> {
			"${expr.gen()} instanceof ${checkType.js}"
		}
		else -> throw NotImplementedError("Unhandled expression $this")
	}

}