package com.jtransc.gen.common

import com.jtransc.annotation.JTranscInvisible
import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.noImplWarn
import com.jtransc.gen.GenTargetInfo
import com.jtransc.text.Indenter
import com.jtransc.vfs.SyncVfsFile
import java.util.*

open class CommonGenGen(val input: Input) {
	class Input(
		val program: AstProgram,
		val features: AstFeatures,
		val featureSet: Set<AstFeature>,
		val settings: AstBuildSettings,
		val tinfo: GenTargetInfo,
		val names: CommonNames,
		val templateString: CommonProgramTemplate,
		val folders: CommonGenFolders,
		val srcFolder: SyncVfsFile
	)

	val program = input.program
	val features = input.features
	val featureSet = input.featureSet
	val settings = input.settings
	val tinfo = input.tinfo
	val cnames = input.names
	val templateString = input.templateString
	val folders = input.folders
	val srcFolder = input.srcFolder

	val types: AstTypes = program.types
	val context = AstGenContext()
	val refs = References()

	fun String.template(type: String = "template"): String = templateString.gen(this, context, type)

	lateinit var mutableBody: MutableBody
	lateinit var stm: AstStm

	fun AstExpr.genExpr(): String = genExpr2(this)
	fun AstExpr.Box.genExpr(): String = genExpr2(this.value)

	fun AstStm.genStm(): Indenter = genStm2(this)
	fun AstStm.Box.genStm(): Indenter = genStm2(this.value)

	fun AstExpr.Box.genNotNull(): String = this.value.genNotNull()

	fun AstField.isVisible(): Boolean = !this.annotationsList.contains<JTranscInvisible>()
	fun AstMethod.isVisible(): Boolean = !this.annotationsList.contains<JTranscInvisible>()

	val invisibleExternalList = program.allAnnotations
		.map { it.toObject<JTranscInvisibleExternal>() }.filterNotNull()
		.flatMap { it.classes.toList() }

	fun AstClass.isVisible(): Boolean {
		if (this.fqname in invisibleExternalList) return false
		if (this.annotationsList.contains<JTranscInvisible>()) return false
		return true
	}

	fun getFilesToCopy(target: String) = program.classes.flatMap { it.annotationsList.getTypedList(com.jtransc.annotation.JTranscAddFileList::value).filter { it.target == target || it.target == "all" } }.sortedBy { it.priority }

	fun AstExpr.genNotNull(): String = genExpr2(this)
	//fun AstBody.genBody(): Indenter = genBody2(this)
	//fun AstBody.genBodyWithFeatures(): Indenter = features.apply(this, featureSet, settings, types).genBody()

	fun AstBody.genBody(): Indenter = genBody2(this)
	fun AstBody.genBodyWithFeatures(): Indenter = genBody2WithFeatures(this)

	open fun genBody2WithFeatures(body: AstBody): Indenter {
		//return if (ENABLE_HXCPP_GOTO_HACK && (tinfo.subtarget in setOf("cpp", "windows", "linux", "mac", "android"))) {
		//	features.apply(body, (featureSet + setOf(GotosFeature)), settings, types).genBody()
		//} else {
		return features.apply(body, featureSet, settings, types).genBody()
		//}
	}

	// @TODO: Remove this from here, so new targets don't have to do this too!
	// @TODO: AstFieldRef should be fine already, so fix it in asm_ast!
	fun fixField(field: AstFieldRef): AstFieldRef = program[field].ref

	fun fixMethod(method: AstMethodRef): AstMethodRef = program[method]?.ref ?: invalidOp("Can't find method $method while generating $context")

	val allAnnotationTypes = program.allAnnotations.flatMap { it.getAllDescendantAnnotations() }.map { it.type }.distinct().map { program[it.name] }.toSet()

	open fun genBody2(body: AstBody): Indenter {
		val method = context.method
		this.mutableBody = MutableBody(method)

		return Indenter.gen {
			for (local in body.locals) {
				refs.add(local.type)
				line(genBodyLocal(local))

			}
			if (body.traps.isNotEmpty()) {
				line(genBodyTrapsPrefix())
			}
			for (field in method.dependencies.fields2.filter { it.isStatic }) {
				val clazz = field.containingClass
				if (clazz.isInterface) {

				} else {
				}
			}

			val bodyContent = body.stm.genStm()

			for ((clazzRef, reasons) in mutableBody.referencedClasses) {
				if (program[clazzRef.name].isNative) continue
				line(genBodyStaticInitPrefix(clazzRef, reasons))
			}
			line(bodyContent)
		}
	}

	open fun genBodyLocal(local: AstLocal): Indenter = indent { line("var ${local.nativeName} = ${local.type.nativeDefaultString};") }
	open fun genBodyTrapsPrefix() = indent { line("var J__exception__ = null;") }
	open fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(cnames.buildStaticInit(program[clazzRef.name]))
		//line(cnames.getJsClassStaticInit(clazzRef, reasons.joinToString(", ")))
	}

	open fun genStm2(stm: AstStm): Indenter {
		this.stm = stm
		return when (stm) {
			is AstStm.NOP -> genStmNop(stm)
			is AstStm.WHILE -> genStmWhile(stm)
			is AstStm.IF -> genStmIf(stm)
			is AstStm.IF_ELSE -> genStmIfElse(stm)
			is AstStm.RETURN_VOID -> genStmReturnVoid(stm)
			is AstStm.RETURN -> genStmReturnValue(stm)
			is AstStm.STM_EXPR -> genStmExpr(stm)
			is AstStm.STMS -> genStmStms(stm)
			is AstStm.THROW -> genStmThrow(stm)
			is AstStm.RETHROW -> genStmRethrow(stm)
			is AstStm.MONITOR_ENTER -> genStmMonitorEnter(stm)
			is AstStm.MONITOR_EXIT -> genStmMonitorExit(stm)
			is AstStm.TRY_CATCH -> genStmTryCatch(stm)
			is AstStm.SET_LOCAL -> genStmSetLocal(stm)
			is AstStm.LINE -> genStmLine(stm)
			is AstStm.SET_ARRAY -> genStmSetArray(stm)
			is AstStm.STM_LABEL -> genStmLabel(stm)
			is AstStm.BREAK -> genStmBreak(stm)
			is AstStm.CONTINUE -> genStmContinue(stm)
			is AstStm.SET_FIELD_INSTANCE -> genStmSetFieldInstance(stm)
			is AstStm.SET_FIELD_STATIC -> indent {
				refs.add(stm.clazz)
				mutableBody.initClassRef(fixField(stm.field).classRef, "SET_FIELD_STATIC")
				val left = fixField(stm.field).nativeStaticText
				val right = stm.expr.genExpr()
				if (left != right) {
					// Avoid: Assigning a value to itself
					line("$left /*${stm.field.name}*/ = $right;")
				}
			}
			else -> noImpl("Statement $stm")
		}
	}

	private fun genStmSetFieldInstance(stm: AstStm.SET_FIELD_INSTANCE): Indenter = indent {
		val left = cnames.buildInstanceField(stm.left.genExpr(), fixField(stm.field))
		val right = stm.expr.genExpr()
		if (left != right) {
			// Avoid: Assigning a value to itself
			line("$left = $right;")
		}
	}

	open fun genStmContinue(stm: AstStm.CONTINUE) = Indenter.single("continue;")
	open fun genStmBreak(stm: AstStm.BREAK) = Indenter.single("break;")
	open fun genStmLabel(stm: AstStm.STM_LABEL) = Indenter.single("${stm.label.name}:;")

	open fun genExpr2(e: AstExpr): String = when (e) {
		is AstExpr.THIS -> genExprThis(e)
		is AstExpr.TERNARY -> genExprTernary(e)
		is AstExpr.LITERAL -> genExprLiteral(e)
		is AstExpr.CAST -> genExprCast(e)
		is AstExpr.PARAM -> genExprParam(e)
		is AstExpr.LOCAL -> genExprLocal(e)
		is AstExpr.UNOP -> genExprUnop(e)
		is AstExpr.FIELD_INSTANCE_ACCESS -> genExprFieldInstanceAccess(e)
		is AstExpr.ARRAY_ACCESS -> genExprArrayAccess(e)
		else -> noImpl("Expression $e")
	}

	open fun genExprArrayAccess(e: AstExpr.ARRAY_ACCESS): String = N_AGET_T(e.array.type.elementType, e.array.genNotNull(), e.index.genExpr())

	open fun genExprFieldInstanceAccess(e: AstExpr.FIELD_INSTANCE_ACCESS): String {
		return cnames.buildInstanceField(e.expr.genNotNull(), fixField(e.field))
	}

	private fun genExprUnop(e: AstExpr.UNOP): String {
		val resultType = e.type
		val es = e.right.genExpr()
		return when (resultType) {
			AstType.BOOL -> {
				when (e.op) {
					AstUnop.NOT -> N_znot(es)
					else -> invalidOp("${e.op} for bool")
				}
			}
			AstType.LONG -> {
				when (e.op) {
					AstUnop.NEG -> N_lneg(es)
					AstUnop.INV -> N_linv(es)
					else -> invalidOp("${e.op} for longs")
				}
			}
			AstType.BYTE, AstType.SHORT, AstType.CHAR, AstType.INT -> {
				val expr = when (e.op) {
					AstUnop.NEG -> N_ineg(es)
					AstUnop.INV -> N_iinv(es)
					else -> invalidOp("${e.op} for $resultType")
				}
				when (resultType) {
					AstType.INT -> N_i(expr)
					AstType.CHAR -> N_i2c(expr)
					AstType.SHORT -> N_i2s(expr)
					AstType.BYTE -> N_i2b(expr)
					else -> expr
				}
			}
			AstType.FLOAT -> {
				when (e.op) {
					AstUnop.NEG -> N_fneg(es)
					else -> invalidOp("${e.op} for float")
				}
			}
			AstType.DOUBLE -> {
				when (e.op) {
					AstUnop.NEG -> N_dneg(es)
					else -> invalidOp("${e.op} for double")
				}
			}
			else -> invalidOp("Invalid type")
		}
	}

	inline protected fun indent(init: Indenter.() -> Unit): Indenter = Indenter.gen(init)

	open fun genStmSetArray(stm: AstStm.SET_ARRAY) = Indenter.single(N_ASET_T(stm.array.type.elementType, stm.array.genNotNull(), stm.index.genExpr(), stm.expr.genExpr()))

	open fun genExprParam(e: AstExpr.PARAM) = e.argument.nativeName
	open fun genExprLocal(e: AstExpr.LOCAL) = e.local.nativeName

	open fun genStmLine(stm: AstStm.LINE) = indent {
		mark(stm)
		line("// ${stm.line}")
	}

	open fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (J__i__exception__)") {
			line("J__exception__ = J__i__exception__;")
			line(stm.catch.genStm())
		}
	}

	open fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = indent { line("// MONITOR_ENTER") }
	open fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = indent { line("// MONITOR_EXIT") }
	open fun genStmThrow(stm: AstStm.THROW) = indent { line("throw ${stm.value.genExpr()};") }
	open fun genStmRethrow(stm: AstStm.RETHROW) = indent { line("""throw J__i__exception__;""") }
	open fun genStmStms(stm: AstStm.STMS) = indent { for (s in stm.stms) line(s.genStm()) }
	open fun genStmExpr(stm: AstStm.STM_EXPR) = Indenter.single("${stm.expr.genExpr()};")
	open fun genStmReturnVoid(stm: AstStm.RETURN_VOID) = Indenter.single(if (context.method.methodVoidReturnThis) "return this;" else "return;")
	open fun genStmReturnValue(stm: AstStm.RETURN) = Indenter.single("return ${stm.retval.genExpr()};")
	open fun genStmWhile(stm: AstStm.WHILE) = indent {
		line("while (${stm.cond.genExpr()})") {
			line(stm.iter.genStm())
		}
	}

	open fun genStmIf(stm: AstStm.IF) = indent { line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) } }
	open fun genStmIfElse(stm: AstStm.IF_ELSE) = indent {
		line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) }
		line("else") { line(stm.sfalse.genStm()) }
	}

	open fun genStmSetLocal(stm: AstStm.SET_LOCAL) = indent {
		val localName = stm.local.nativeName
		val expr = stm.expr.genExpr()
		if (localName != expr) {
			// Avoid: Assigning a value to itself
			line("$localName = $expr;")
		}
	}

	open fun genStmNop(stm: AstStm.NOP) = Indenter.EMPTY

	open fun genExprTernary(e: AstExpr.TERNARY): String = "((${e.cond.genExpr()}) ? (${e.etrue.genExpr()}) : (${e.efalse.genExpr()}))"
	open fun genExprThis(e: AstExpr.THIS): String = "this"
	open fun genExprLiteral(e: AstExpr.LITERAL): String {
		val value = e.value

		return when (value) {
			null -> genLiteralNull()
			is AstType -> genLiteralType(value)
			is String -> genLiteralString(value)
			is Boolean -> genLiteralBoolean(value)
			is Byte -> genLiteralByte(value)
			is Char -> genLiteralChar(value)
			is Short -> genLiteralShort(value)
			is Int -> genLiteralInt(value)
			is Long -> genLiteralLong(value)
			is Float -> genLiteralFloat(value)
			is Double -> genLiteralDouble(value)
			else -> invalidOp("Unsupported value $value")
		}
	}

	open fun genLiteralType(v: AstType): String {
		for (fqName in v.getRefClasses()) {
			mutableBody.initClassRef(fqName, "class literal")
		}
		return cnames.escapeConstant(v)
	}

	open fun genLiteralNull(): String = cnames.escapeConstant(null)
	open fun genLiteralString(v: String): String = cnames.escapeConstant(v)
	open fun genLiteralBoolean(v: Boolean): String = cnames.escapeConstant(v)
	open fun genLiteralByte(v: Byte): String = cnames.escapeConstant(v)
	open fun genLiteralChar(v: Char): String = cnames.escapeConstant(v)
	open fun genLiteralShort(v: Short): String = cnames.escapeConstant(v)
	open fun genLiteralInt(v: Int): String = cnames.escapeConstant(v)
	open fun genLiteralLong(v: Long): String = cnames.escapeConstant(v)
	open fun genLiteralFloat(v: Float): String = cnames.escapeConstant(v)
	open fun genLiteralDouble(v: Double): String = cnames.escapeConstant(v)

	class MutableBody(val method: AstMethod) {
		val referencedClasses = hashMapOf<AstType.REF, ArrayList<String>>()
		fun initClassRef(classRef: AstType.REF, reason: String) {
			referencedClasses.putIfAbsent(classRef, arrayListOf())
			referencedClasses[classRef]!! += reason
		}
	}

	open fun genExprCast(e: AstExpr.CAST): String = genExprCast(e.expr.genExpr(), e.from, e.to)

	open fun genExprCast(e: String, from: AstType, to: AstType): String {
		refs.add(from)
		refs.add(to)

		if (from == to) return e

		if (from !is AstType.Primitive && to is AstType.Primitive) {
			return when (from) {
			// @TODO: Check!
				AstType.BOOL.CLASSTYPE -> genExprCast(N_unboxBool(e), AstType.BOOL, to)
				AstType.BYTE.CLASSTYPE -> genExprCast(N_unboxByte(e), AstType.BYTE, to)
				AstType.SHORT.CLASSTYPE -> genExprCast(N_unboxShort(e), AstType.SHORT, to)
				AstType.CHAR.CLASSTYPE -> genExprCast(N_unboxChar(e), AstType.CHAR, to)
				AstType.INT.CLASSTYPE -> genExprCast(N_unboxInt(e), AstType.INT, to)
				AstType.LONG.CLASSTYPE -> genExprCast(N_unboxLong(e), AstType.LONG, to)
				AstType.FLOAT.CLASSTYPE -> genExprCast(N_unboxFloat(e), AstType.FLOAT, to)
				AstType.DOUBLE.CLASSTYPE -> genExprCast(N_unboxDouble(e), AstType.DOUBLE, to)
			//AstType.OBJECT -> genCast(genCast(e, from, to.CLASSTYPE), to.CLASSTYPE, to)
			//else -> noImpl("Unhandled conversion $e : $from -> $to")
				else -> genExprCast(genExprCast(e, from, to.CLASSTYPE), to.CLASSTYPE, to)
			}
		}

		fun unhandled(): String {
			noImplWarn("Unhandled conversion ($from -> $to) at $context")
			return "($e)"
		}

		return when (from) {
			is AstType.BOOL, is AstType.INT, is AstType.CHAR, is AstType.SHORT, is AstType.BYTE -> {
				val e2 = if (from == AstType.BOOL) N_z2i(e) else "$e"

				when (to) {
					is AstType.BOOL -> N_i2z(e2)
					is AstType.BYTE -> N_i2b(e2)
					is AstType.CHAR -> N_i2c(e2)
					is AstType.SHORT -> N_i2s(e2)
					is AstType.INT -> N_i2i(e2)
					is AstType.LONG -> N_i2j(e2)
					is AstType.FLOAT -> N_i2f(e2)
					is AstType.DOUBLE -> N_i2d(e2)
					else -> unhandled()
				}
			}
			is AstType.FLOAT -> {
				when (to) {
					is AstType.BOOL -> N_i2z(N_f2i(e))
					is AstType.BYTE -> N_i2b(N_f2i(e))
					is AstType.CHAR -> N_i2c(N_f2i(e))
					is AstType.SHORT -> N_i2s(N_f2i(e))
					is AstType.INT -> N_i2i(N_f2i(e))
					is AstType.LONG -> N_i2j(N_f2i(e))
					is AstType.FLOAT -> N_f2f(e)
					is AstType.DOUBLE -> N_f2d(e)
					else -> unhandled()
				}
			}
			is AstType.DOUBLE -> {
				when (to) {
					is AstType.BOOL -> N_i2z(N_d2i(e))
					is AstType.BYTE -> N_i2b(N_d2i(e))
					is AstType.CHAR -> N_i2c(N_d2i(e))
					is AstType.SHORT -> N_i2s(N_d2i(e))
					is AstType.INT -> N_i2i(N_d2i(e))
					is AstType.LONG -> N_i2j(N_d2i(e))
					is AstType.FLOAT -> N_d2f(e)
					is AstType.DOUBLE -> N_d2d(e)
					else -> unhandled()
				}
			}
			is AstType.LONG -> {
				when (to) {
					is AstType.BOOL -> N_i2z(N_l2i(e))
					is AstType.BYTE -> N_i2b(N_l2i(e))
					is AstType.CHAR -> N_i2c(N_l2i(e))
					is AstType.SHORT -> N_i2s(N_l2i(e))
					is AstType.INT -> N_l2i(e)
					is AstType.LONG -> N_l2l(e)
					is AstType.FLOAT -> N_l2f(e)
					is AstType.DOUBLE -> N_l2d(e)
					else -> unhandled()
				}
			}
			is AstType.REF, is AstType.ARRAY, is AstType.GENERIC -> {
				when (to) {
					FUNCTION_REF -> N_getFunction(e)
					else -> N_c(e, from, to)
				}
			}
			is AstType.NULL -> "$e"
			else -> unhandled()
		}
	}

	val FUNCTION_REF = AstType.REF(com.jtransc.JTranscFunction::class.java.name)

	open protected fun N_AGET_T(elementType: AstType, array: String, index: String) = "($array[$index])"
	open protected fun N_ASET_T(elementType: AstType, array: String, index: String, value: String) = "$array[$index] = $value;"
	open protected fun N_unboxBool(e: String) = "N.unboxBool($e)"
	open protected fun N_unboxByte(e: String) = "N.unboxByte($e)"
	open protected fun N_unboxShort(e: String) = "N.unboxShort($e)"
	open protected fun N_unboxChar(e: String) = "N.unboxChar($e)"
	open protected fun N_unboxInt(e: String) = "N.unboxInt($e)"
	open protected fun N_unboxLong(e: String) = "N.unboxLong($e)"
	open protected fun N_unboxFloat(e: String) = "N.unboxFloat($e)"
	open protected fun N_unboxDouble(e: String) = "N.unboxDouble($e)"
	open protected fun N_is(a: String, b: String) = "N.is($a, $b)"
	open protected fun N_z2i(str: String) = "N.z2i($str)"
	open protected fun N_i(str: String) = "(($str)|0)"
	open protected fun N_i2z(str: String) = "(($str)!=0)"
	open protected fun N_i2b(str: String) = "(($str)<<24>>24)"
	open protected fun N_i2c(str: String) = "(($str)&0xFFFF)"
	open protected fun N_i2s(str: String) = "(($str)<<16>>16)"
	open protected fun N_f2i(str: String) = "(($str)|0)"
	open protected fun N_i2i(str: String) = N_i(str)
	open protected fun N_i2j(str: String) = "N.i2j($str)"
	open protected fun N_i2f(str: String) = "(($str))"
	open protected fun N_i2d(str: String) = "($str)"
	open protected fun N_f2f(str: String) = "($str)"
	open protected fun N_f2d(str: String) = "($str)"
	open protected fun N_d2f(str: String) = "(($str))"
	open protected fun N_d2d(str: String) = "($str)"
	open protected fun N_d2i(str: String) = "(($str)|0)"
	open protected fun N_l2i(str: String) = "N.l2i($str)"
	open protected fun N_l2l(str: String) = "N.l2l($str)"
	open protected fun N_l2f(str: String) = "(N.l2d($str))"
	open protected fun N_l2d(str: String) = "N.l2d($str)"
	open protected fun N_getFunction(str: String) = "N.getFunction($str)"
	open protected fun N_c(str: String, from: AstType, to: AstType) = "($str)"
	open protected fun N_lneg(str: String) = "-($str)"
	open protected fun N_ineg(str: String) = "-($str)"
	open protected fun N_fneg(str: String) = "-($str)"
	open protected fun N_dneg(str: String) = "-($str)"
	open protected fun N_iinv(str: String) = "~($str)"
	open protected fun N_linv(str: String) = "~($str)"
	open protected fun N_znot(str: String) = "!($str)"

	class References {
		var _usedDependencies = hashSetOf<AstType.REF>()
		fun add(type: AstType?) {
			when (type) {
				null -> Unit
				is AstType.METHOD -> {
					for (arg in type.argTypes) add(arg)
					add(type.ret)
				}
				is AstType.REF -> _usedDependencies.add(type)
				is AstType.ARRAY -> add(type.elementType)
				else -> Unit
			}
		}
	}

	fun AstMethod.getNativeBodies(target: String): Map<String, Indenter> {
		val bodies = this.annotationsList.getTypedList(com.jtransc.annotation.JTranscMethodBodyList::value).filter { it.target == target }

		return bodies.associate { body ->
			body.cond to Indenter.gen {
				for (line in body.value.toList()) line(line.template("nativeBody"))
			}
		}
	}

	enum class TypeKind { TYPETAG, NEW, CAST }

	val AstType.nativeTypeNew: FqName get() = cnames.getNativeType(this, TypeKind.NEW)
	val AstType.nativeTypeCast: FqName get() = cnames.getNativeType(this, TypeKind.CAST)

	val LocalParamRef.nativeName: String get() = cnames.getNativeName(this)
	val AstType.nativeDefault: Any? get() = cnames.getDefault(this)
	val AstType.nativeDefaultString: String get() = cnames.escapeConstant(cnames.getDefault(this), this)
	val FieldRef.nativeStaticText: String get() = cnames.buildStaticField(this)
}