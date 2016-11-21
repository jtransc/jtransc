package com.jtransc.gen.common

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.annotation.JTranscInvisible
import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.OptimizeFeature
import com.jtransc.ast.feature.method.SimdFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.ast.treeshaking.getTargetAddFiles
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.noImplWarn
import com.jtransc.gen.MinimizedNames
import com.jtransc.gen.TargetName
import com.jtransc.injector.Injector
import com.jtransc.io.ProcessResult2
import com.jtransc.lang.JA_I
import com.jtransc.lang.high
import com.jtransc.lang.low
import com.jtransc.template.Minitemplate
import com.jtransc.text.Indenter
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.quote
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import java.util.*
import kotlin.reflect.KMutableProperty1

class ConfigSrcFolder(val srcFolder: SyncVfsFile)

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "RemoveSingleExpressionStringTemplate")
open class CommonGenerator(val injector: Injector) : IProgramTemplate {
	val program: AstProgram = injector.get()
	val targetName = injector.get<TargetName>()
	open val methodFeatures: Set<Class<out AstMethodFeature>> = setOf()
	open val keywords: Set<String> = program.getExtraKeywords(targetName.name).toSet()
	val configSrcFolder: ConfigSrcFolder = injector.get()
	open val srcFolder: SyncVfsFile = configSrcFolder.srcFolder

	val settings: AstBuildSettings = injector.get()
	val folders: CommonGenFolders = injector.get()

	val configOutputFile: ConfigOutputFile = injector.get()
	val configOutputFile2: ConfigOutputFile2 = injector.get()
	val outputFileBaseName = configOutputFile.outputFileBaseName
	val outputFile = configOutputFile.output

	val features = injector.get<AstMethodFeatures>()

	val types: AstTypes = program.types
	val context = AstGenContext()
	val refs = References()

	open fun buildSource(): Unit {
		TODO()
	}

	open fun compile(): ProcessResult2 {
		TODO()
	}
	open fun run(redirect: Boolean = true): ProcessResult2 {
		TODO()
	}

	open fun compileAndRun(redirect: Boolean = true): ProcessResult2 {
		val compileResult = compile()
		return if (!compileResult.success) {
			ProcessResult2(compileResult.exitValue)
		} else {
			this.run(redirect)
		}
	}

	val JAVA_LANG_OBJECT by lazy { nativeName<java.lang.Object>() }
	val JAVA_LANG_CLASS by lazy { nativeName<java.lang.Class<*>>() }
	val JAVA_LANG_CLASS_name by lazy { getNativeFieldName(java.lang.Class::class.java, "name") }
	val JAVA_LANG_STRING by lazy { nativeName<java.lang.String>() }

	val invocationHandlerTargetName by lazy { nativeName<java.lang.reflect.InvocationHandler>() }
	val methodTargetName by lazy { nativeName<java.lang.reflect.Method>() }
	val invokeTargetName by lazy { AstMethodRef(java.lang.reflect.InvocationHandler::class.java.name.fqname, "invoke", types.build { METHOD(OBJECT, OBJECT, METHOD, ARRAY(OBJECT)) }).targetName }
	val toStringTargetName by lazy { AstMethodRef(java.lang.Object::class.java.name.fqname, "toString", types.build { METHOD(STRING) }).targetName }
	val hashCodeTargetName by lazy { AstMethodRef(java.lang.Object::class.java.name.fqname, "hashCode", types.build { METHOD(INT) }).targetName }
	val getClassTargetName by lazy { AstMethodRef(java.lang.Object::class.java.name.fqname, "getClass", types.build { METHOD(CLASS) }).targetName }

	protected fun setCurrentClass(clazz: AstClass) {
		context.clazz = clazz
		currentClass = clazz.name
	}

	protected fun setCurrentMethod(method: AstMethod) {
		context.method = method
		currentMethod = method.ref
		//context.clazz = method.containingClass
		//names.currentClass = method.clazz.name
	}

	open fun genStm2(stm: AstStm): Indenter {
		this.stm = stm
		return when (stm) {
			is AstStm.STM_LABEL -> genStmLabel(stm)
			is AstStm.GOTO -> genStmGoto(stm)
			is AstStm.IF_GOTO -> genStmIfGoto(stm)
			is AstStm.SWITCH_GOTO -> genStmSwitchGoto(stm)
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
			is AstStm.SET_ARRAY_LITERALS -> genStmSetArrayLiterals(stm)
			is AstStm.BREAK -> genStmBreak(stm)
			is AstStm.CONTINUE -> genStmContinue(stm)
			is AstStm.SET_FIELD_INSTANCE -> genStmSetFieldInstance(stm)
			is AstStm.SET_FIELD_STATIC -> genStmSetFieldStatic(stm)
			is AstStm.SWITCH -> genStmSwitch(stm)
			is AstStm.SET_NEW_WITH_CONSTRUCTOR -> genStmSetNewWithConstructor(stm)
			else -> noImpl("Statement $stm")
		}
	}

	open fun genExpr2(e: AstExpr): String = when (e) {
		is AstExpr.THIS -> genExprThis(e)
		is AstExpr.TERNARY -> genExprTernary(e)
		is AstExpr.LITERAL -> genExprLiteral(e)
		is AstExpr.LITERAL_REFNAME -> genExprLiteralRefName(e)
		is AstExpr.CAST -> genExprCast(e)
		is AstExpr.PARAM -> genExprParam(e)
		is AstExpr.LOCAL -> genExprLocal(e)
		is AstExpr.UNOP -> genExprUnop(e)
		is AstExpr.BINOP -> genExprBinop(e)
		is AstExpr.FIELD_STATIC_ACCESS -> genExprFieldStaticAccess(e)
		is AstExpr.FIELD_INSTANCE_ACCESS -> genExprFieldInstanceAccess(e)
		is AstExpr.ARRAY_ACCESS -> genExprArrayAccess(e)
		is AstExpr.CAUGHT_EXCEPTION -> genExprCaughtException(e)
		is AstExpr.ARRAY_LENGTH -> genExprArrayLength(e)
		is AstExpr.INSTANCE_OF -> genExprInstanceOf(e)
		is AstExpr.NEW -> genExprNew(e)
		is AstExpr.NEW_WITH_CONSTRUCTOR -> genExprNewWithConstructor(e)
		is AstExpr.NEW_ARRAY -> genExprNewArray(e)
		is AstExpr.INTARRAY_LITERAL -> genExprIntArrayLit(e)
		is AstExpr.STRINGARRAY_LITERAL -> genExprStringArrayLit(e)
		is AstExpr.CALL_BASE -> genExprCallBase(e)
		is AstExpr.INVOKE_DYNAMIC_METHOD -> genExprMethodClass(e)
		else -> noImpl("Expression $e")
	}

	open fun genExprMethodClass(e: AstExpr.INVOKE_DYNAMIC_METHOD): String {
		System.err.println("GenCommonGen.genExprMethodClass. Lambdas should be replaced by not including LambdaProgramFeature")
		return "N::dummyMethodClass()"
	}

	private fun genExprCallBase(e: AstExpr.CALL_BASE): String {
		// Determine method to call!
		val e2 = if (e.isSpecial && e is AstExpr.CALL_INSTANCE) AstExprUtils.RESOLVE_SPECIAL(program, e, context) else e
		val method = fixMethod(e2.method)
		val refMethod = program[method] ?: invalidOp("Can't find method: $method while generating $context")
		val refMethodClass = refMethod.containingClass
		val clazz = method.containingClassType
		val args = e2.args
		val isStaticCall = e2 is AstExpr.CALL_STATIC
		if (isStaticCall) {
			refs.add(clazz)
			mutableBody.initClassRef(clazz, "CALL_STATIC")
		}

		val isNativeCall = refMethodClass.isNative

		val processedArgs = args.map { processCallArg(it.value, if (isNativeCall) convertToTarget(it) else it.genExpr()) }

		val methodAccess = getTargetMethodAccess(refMethod, static = isStaticCall)
		val result = when (e2) {
			is AstExpr.CALL_STATIC -> genExprCallBaseStatic(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
			is AstExpr.CALL_SUPER -> genExprCallBaseSuper(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
			is AstExpr.CALL_INSTANCE -> genExprCallBaseInstance(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
			else -> invalidOp("Unexpected")
		}
		return if (isNativeCall) convertToJava(refMethod.methodType.ret, result) else result
	}

	open fun processCallArg(e: AstExpr, str: String): String {
		return str
	}

	open fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		return "super$methodAccess(${args.joinToString(", ")})"
	}

	open fun genExprCallBaseStatic(e2: AstExpr.CALL_STATIC, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		//val className = method.containingClassType.fqname
		//val methodName = method.name
		return "${clazz.targetTypeNew}$methodAccess(${args.joinToString(", ")})"
	}

	open fun genExprCallBaseInstance(e2: AstExpr.CALL_INSTANCE, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		return "${e2.obj.genNotNull()}$methodAccess(${args.joinToString(", ")})"
	}

	fun convertToTarget(expr: AstExpr.Box): String = convertToTarget(expr.type, expr.genExpr())
	fun convertToJava(expr: AstExpr.Box): String = convertToJava(expr.type, expr.genExpr())
	fun convertToTarget(type: AstType, text: String): String = convertToFromTarget(type, text, toTarget = true)
	fun convertToJava(type: AstType, text: String): String = convertToFromTarget(type, text, toTarget = false)

	open fun convertToFromTarget(type: AstType, text: String, toTarget: Boolean): String {
		return if (type is AstType.ARRAY) (if (toTarget) "N.unbox($text)" else "N.box($text)") else text
	}

	fun String.template(type: String = "template"): String = gen(this, context, type)

	lateinit var mutableBody: MutableBody
	lateinit var stm: AstStm

	fun AstExpr.genExpr(): String = genExpr2(this)
	fun AstExpr.Box.genExpr(): String = genExpr2(this.value)

	fun AstStm.genStm(): Indenter = genStm2(this)
	fun AstStm.Box.genStm(): Indenter = genStm2(this.value)

	fun AstExpr.Box.genNotNull(): String = this.value.genNotNull()

	fun AstField.isVisible(): Boolean = !this.invisible
	fun AstMethod.isVisible(): Boolean = !this.invisible

	val invisibleExternalList = program.allAnnotations
		.map { it.toObject<JTranscInvisibleExternal>() }.filterNotNull()
		.flatMap { it.classes.toList() }

	fun AstClass.isVisible(): Boolean {
		if (this.fqname in invisibleExternalList) return false
		if (this.invisible) return false
		return true
	}

	fun getFilesToCopy(target: String) = program.classes.flatMap { it.annotationsList.getTargetAddFiles(target) }.sortedBy { it.priority }

	fun AstExpr.genNotNull(): String = genExpr2(this)
	//fun AstBody.genBody(): Indenter = genBody2(this)
	//fun AstBody.genBodyWithFeatures(): Indenter = features.apply(this, featureSet, settings, types).genBody()

	fun AstBody.genBody(): Indenter = genBody2(this)
	fun AstBody.genBodyWithFeatures(method: AstMethod): Indenter = genBody2WithFeatures(method, this)


	open fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter {
		return features.apply(method, body, methodFeatures, settings, types).genBody()
	}

	// @TODO: Remove this from here, so new targets don't have to do this too!
	// @TODO: AstFieldRef should be fine already, so fix it in asm_ast!
	fun fixField(field: AstFieldRef): AstFieldRef = program[field].ref

	fun fixMethod(method: AstMethodRef): AstMethodRef = program[method]?.ref ?: invalidOp("Can't find method $method while generating $context")

	val allAnnotationTypes = program.allAnnotations.flatMap { it.getAllDescendantAnnotations() }.map { it.type }.distinct().map { program[it.name] }.toSet()

	val trapsByStart = hashMapOf<AstLabel, ArrayList<AstTrap>>()
	val trapsByEnd = hashMapOf<AstLabel, ArrayList<AstTrap>>()

	open fun genBody2(body: AstBody): Indenter {
		val method = context.method
		this.mutableBody = MutableBody(method)

		//val referencedLabels = (body.stm as AstStm.STMS).stms.filterIsInstance<AstStm.STM_LABEL>().map { it.label }.toHashSet()

		trapsByStart.clear()
		trapsByEnd.clear()

		for (trap in body.traps) {
			//if (trap.start !in referencedLabels) invalidOp("Invalid trap start!")
			//if (trap.end !in referencedLabels) invalidOp("Invalid trap end!")
			//if (trap.handler !in referencedLabels) invalidOp("Invalid trap handler!")
			trapsByStart.getOrPut(trap.start) { arrayListOf() } += trap
			trapsByEnd.getOrPut(trap.end) { arrayListOf() } += trap
		}


		return Indenter.gen {
			for (local in body.locals) {
				refs.add(local.type)
			}

			resetLocalsPrefix()
			var info: Indenter? = null

			linedeferred {
				line(info!!)
			}
			line(genBodyLocals(body.locals))
			if (body.traps.isNotEmpty()) {
				line(genBodyTrapsPrefix())
			}

			//for (field in method.dependencies.fields2.filter { it.isStatic }) {
			//	val clazz = field.containingClass
			//}

			val bodyContent = body.stm.genStm()
			info = genLocalsPrefix()

			if (method.isClassOrInstanceInit) {
				mutableBody.initClassRef(context.clazz.ref, "self")
			}

			for ((clazzRef, reasons) in mutableBody.referencedClasses) {
				if (program[clazzRef.name].isNative) continue
				if (!method.isClassOrInstanceInit && (context.clazz.ref == clazzRef)) continue // Calling internal methods (which should be initialized already!)
				line(genBodyStaticInitPrefix(clazzRef, reasons))
			}
			line(bodyContent)
		}
	}

	open fun genStmSetNewWithConstructor(stm: AstStm.SET_NEW_WITH_CONSTRUCTOR): Indenter = indent {
		val newClazz = program[stm.target.name]
		refs.add(stm.target)
		val commaArgs = stm.args.map { it.genExpr() }.joinToString(", ")
		val className = stm.target.targetTypeNew
		val targetLocalName = stm.local.nativeName

		if (newClazz.nativeName != null) {
			line("$targetLocalName = new $className($commaArgs);")
		} else {
			line("$targetLocalName = new $className();")
			line("$targetLocalName.${stm.method.targetName}($commaArgs);")
		}
	}

	open fun resetLocalsPrefix() = Unit
	open fun genLocalsPrefix(): Indenter = indent { }
	open fun genBodyLocals(locals: List<AstLocal>): Indenter = indent { for (local in locals) line(genBodyLocal(local)) }
	open fun genBodyLocal(local: AstLocal): Indenter = indent { line("var ${local.nativeName} = ${local.type.nativeDefaultString};") }
	open fun genBodyTrapsPrefix() = indent { line("var J__exception__ = null;") }
	open fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(buildStaticInit(program[clazzRef.name]))
		//line(cnames.getJsClassStaticInit(clazzRef, reasons.joinToString(", ")))
	}

	open fun genStmSetFieldStatic(stm: AstStm.SET_FIELD_STATIC): Indenter = indent {
		refs.add(stm.clazz)
		mutableBody.initClassRef(fixField(stm.field).classRef, "SET_FIELD_STATIC")
		val left = fixField(stm.field).nativeStaticText
		val right = stm.expr.genExpr()
		if (allowAssignItself || left != right) {
			// Avoid: Assigning a value to itself
			line(genStmSetFieldStaticActual(stm, left, stm.field, right))
		}
	}

	open fun genStmSetFieldStaticActual(stm: AstStm.SET_FIELD_STATIC, left: String, field: AstFieldRef, right: String): Indenter = indent {
		//line("$left /*${stm.field.name}*/ = $right;")
		line("$left = $right;")
	}

	open fun genStmSwitchGoto(stm: AstStm.SWITCH_GOTO): Indenter = indent {
		line("switch (${stm.subject.genExpr()})") {
			for ((value, label) in stm.cases) {
				line("case $value: goto ${label.name};");
			}
			line("default: goto ${stm.default.name};");
		}
	}

	open fun genStmIfGoto(stm: AstStm.IF_GOTO): Indenter = Indenter.single("if (${stm.cond.genExpr()}) goto ${stm.label.name};")
	open fun genStmGoto(stm: AstStm.GOTO): Indenter = Indenter.single("goto ${stm.label.name};")

	open fun genStmSetFieldInstance(stm: AstStm.SET_FIELD_INSTANCE): Indenter = indent {
		val left = buildInstanceField(stm.left.genExpr(), fixField(stm.field))
		val right = stm.expr.genExpr()
		if (allowAssignItself || left != right) {
			// Avoid: Assigning a value to itself
			line(actualSetField(stm, left, right))
		}
	}

	open fun actualSetField(stm: AstStm.SET_FIELD_INSTANCE, left: String, right: String): String {
		return "$left = $right;"
	}

	open fun genStmContinue(stm: AstStm.CONTINUE) = Indenter.single("continue;")
	open fun genStmBreak(stm: AstStm.BREAK) = Indenter.single("break;")
	open fun genStmLabel(stm: AstStm.STM_LABEL): Indenter = Indenter.gen {
		if (stm.label in trapsByEnd) {
			for (trap in trapsByEnd[stm.label]!!) line(genStmRawCatch(trap))
		}
		line("${stm.label.name}:;")
		if (stm.label in trapsByStart) {
			for (trap in trapsByStart[stm.label]!!) line(genStmRawTry(trap))
		}
	}

	open fun genStmRawTry(trap: AstTrap): Indenter = Indenter.gen {
	}

	open fun genStmRawCatch(trap: AstTrap): Indenter = Indenter.gen {
	}

	open val defaultGenStmSwitchHasBreaks = true
	open fun genStmSwitch(stm: AstStm.SWITCH): Indenter = indent {
		if (stm.cases.isNotEmpty() || !stm.default.value.isEmpty()) {
			line("switch (${stm.subject.genExpr()})") {
				for (case in stm.cases) {
					val value = case.first
					val caseStm = case.second
					if (caseStm.value.isSingleStm()) {
						if (!defaultGenStmSwitchHasBreaks || caseStm.value.lastStm().isBreakingFlow()) {
							line("case $value: " + caseStm.genStm().toString().trim())
						} else {
							line("case $value: " + caseStm.genStm().toString().trim() + " break;")
						}
					} else {
						line("case $value:")
						indent {
							line(caseStm.genStm())
							if (defaultGenStmSwitchHasBreaks && !caseStm.value.lastStm().isBreakingFlow()) line("break;")
						}
					}
				}
				if (!stm.default.value.isEmpty()) {
					line("default:")
					indent {
						line(stm.default.genStm())
						if (defaultGenStmSwitchHasBreaks && !stm.default.value.lastStm().isBreakingFlow()) line("break;")
					}
				}
			}
		}
	}

	open fun genExprNewArray(e: AstExpr.NEW_ARRAY): String {
		refs.add(e.type.elementType)
		val desc = e.type.mangle().replace('/', '.') // Internal to normal name!?
		return when (e.counts.size) {
			1 -> createArraySingle(e, desc)
			else -> createArrayMultisure(e, desc)
		}
	}

	open fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		return "JA_I${staticAccessOperator}T([" + e.values.joinToString(",") + "])"
	}

	open fun genExprStringArrayLit(e: AstExpr.STRINGARRAY_LITERAL): String {
		return "JA_J${staticAccessOperator}fromArray([" + e.values.joinToString(",") + "], \"Ljava/lang/String;\")"
	}

	open fun createArraySingle(e: AstExpr.NEW_ARRAY, desc: String): String {
		return if (e.type.elementType !is AstType.Primitive) {
			"new ${ObjectArrayType}(${e.counts[0].genExpr()}, \"$desc\")"
		} else {
			"new ${e.type.targetTypeNew}(${e.counts[0].genExpr()})"
		}
	}

	open fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "${ObjectArrayType}${staticAccessOperator}createMultiSure([${e.counts.map { it.genExpr() }.joinToString(", ")}], \"$desc\")"
	}

	open fun genExprNew(e: AstExpr.NEW): String {
		refs.add(e.target)
		val className = e.target.targetTypeNew
		return "new $className()"
	}

	open fun genExprNewWithConstructor(e: AstExpr.NEW_WITH_CONSTRUCTOR): String {
		return genExprCallBase(AstExpr.CALL_INSTANCE(
			AstExpr.NEW(e.target),
			e.constructor,
			e.args.map { it.value },
			isSpecial = true
		))
	}

	open fun genExprInstanceOf(e: AstExpr.INSTANCE_OF): String {
		refs.add(e.checkType)
		return N_is(e.expr.genExpr(), e.checkType)
	}

	open fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String {
		return "(${e.array.genNotNull()})${instanceAccessOperator}length"
	}

	open fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "J__exception__"

	open fun genExprBinop(e: AstExpr.BINOP): String {
		val resultType = e.type
		val leftType = e.left.type
		val rightType = e.right.type
		val l = e.left.genExpr()
		val r = e.right.genExpr()
		val op = e.op

		//if (leftType != rightType) {
		//	invalidOp("$leftType != $rightType")
		//}

		val commonType = leftType

		fun invalid(): Nothing = invalidOp("leftType=$leftType, rightType=$rightType, op=$op, resultType=$resultType")

		val result = when (commonType) {
			AstType.BOOL -> when (op) {
				AstBinop.BAND -> N_zand(l, r)
				AstBinop.BOR -> N_zor(l, r)
				AstBinop.EQ -> N_zeq(l, r)
				AstBinop.NE -> N_zne(l, r)
				else -> invalid()
			}
			AstType.LONG -> when (op) {
				AstBinop.ADD -> N_ladd(l, r)
				AstBinop.SUB -> N_lsub(l, r)
				AstBinop.MUL -> N_lmul(l, r)
				AstBinop.DIV -> N_ldiv(l, r)
				AstBinop.REM -> N_lrem(l, r)
				AstBinop.EQ -> N_leq(l, r)
				AstBinop.NE -> N_lne(l, r)
				AstBinop.GE -> N_lge(l, r)
				AstBinop.LE -> N_lle(l, r)
				AstBinop.LT -> N_llt(l, r)
				AstBinop.GT -> N_lgt(l, r)
				AstBinop.AND -> N_land(l, r)
				AstBinop.OR -> N_lor(l, r)
				AstBinop.XOR -> N_lxor(l, r)
				AstBinop.SHL -> N_lshl(l, r)
				AstBinop.SHR -> N_lshr(l, r)
				AstBinop.USHR -> N_lushr(l, r)
				AstBinop.LCMP -> N_lcmp(l, r) // long,long -> int
				else -> invalid()
			}
			AstType.BYTE, AstType.CHAR, AstType.SHORT, AstType.INT -> when (op) {
				AstBinop.ADD -> N_iadd(l, r)
				AstBinop.SUB -> N_isub(l, r)
				AstBinop.MUL -> N_imul(l, r)
				AstBinop.DIV -> N_idiv(l, r)
				AstBinop.REM -> N_irem(l, r)
				AstBinop.EQ -> N_ieq(l, r)
				AstBinop.NE -> N_ine(l, r)
				AstBinop.GE -> N_ige(l, r)
				AstBinop.LE -> N_ile(l, r)
				AstBinop.LT -> N_ilt(l, r)
				AstBinop.GT -> N_igt(l, r)
				AstBinop.AND -> N_iand(l, r)
				AstBinop.OR -> N_ior(l, r)
				AstBinop.XOR -> N_ixor(l, r)
				AstBinop.SHL -> N_ishl(l, r)
				AstBinop.SHR -> N_ishr(l, r)
				AstBinop.USHR -> N_iushr(l, r)
				else -> invalid()
			}
			AstType.FLOAT -> when (op) {
				AstBinop.ADD -> N_fadd(l, r)
				AstBinop.SUB -> N_fsub(l, r)
				AstBinop.MUL -> N_fmul(l, r)
				AstBinop.DIV -> N_fdiv(l, r)
				AstBinop.REM -> N_frem(l, r)
				AstBinop.EQ -> N_feq(l, r)
				AstBinop.NE -> N_fne(l, r)
				AstBinop.GE -> N_fge(l, r)
				AstBinop.LE -> N_fle(l, r)
				AstBinop.LT -> N_flt(l, r)
				AstBinop.GT -> N_fgt(l, r)
				AstBinop.CMPL -> N_fcmpl(l, r)
				AstBinop.CMPG -> N_fcmpg(l, r)
				else -> invalid()
			}
			AstType.DOUBLE -> when (op) {
				AstBinop.ADD -> N_dadd(l, r)
				AstBinop.SUB -> N_dsub(l, r)
				AstBinop.MUL -> N_dmul(l, r)
				AstBinop.DIV -> N_ddiv(l, r)
				AstBinop.REM -> N_drem(l, r)
				AstBinop.EQ -> N_deq(l, r)
				AstBinop.NE -> N_dne(l, r)
				AstBinop.GE -> N_dge(l, r)
				AstBinop.LE -> N_dle(l, r)
				AstBinop.LT -> N_dlt(l, r)
				AstBinop.GT -> N_dgt(l, r)
				AstBinop.CMPL -> N_dcmpl(l, r)
				AstBinop.CMPG -> N_dcmpg(l, r)
				else -> invalid()
			}
			is AstType.Reference -> when (op) {
				AstBinop.EQ -> N_obj_eq(l, r)
				AstBinop.NE -> N_obj_ne(l, r)
				else -> invalid()
			}
			else -> invalid()
		}
		return when (resultType) {
			AstType.BOOL -> N_z2z(result)
			AstType.BYTE -> N_i2b(result)
			AstType.CHAR -> N_i2c(result)
			AstType.SHORT -> N_i2s(result)
			AstType.INT -> N_i(result)
			AstType.LONG -> N_l2l(result)
			AstType.FLOAT -> N_f2f(result)
			AstType.DOUBLE -> N_d2d(result)
			else -> invalid()
		}
	}

	open fun genExprArrayAccess(e: AstExpr.ARRAY_ACCESS): String = N_AGET_T(e.array.type as AstType.ARRAY, e.array.type.elementType, e.array.genNotNull(), e.index.genExpr())

	open fun genExprFieldStaticAccess(e: AstExpr.FIELD_STATIC_ACCESS): String {
		refs.add(e.clazzName)
		mutableBody.initClassRef(fixField(e.field).classRef, "FIELD_STATIC_ACCESS")
		return "${fixField(e.field).nativeStaticText}"
	}

	open fun genExprFieldInstanceAccess(e: AstExpr.FIELD_INSTANCE_ACCESS): String {
		return buildInstanceField(e.expr.genNotNull(), fixField(e.field))
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

	open fun genStmSetArray(stm: AstStm.SET_ARRAY) = Indenter.single(N_ASET_T(stm.array.type as AstType.ARRAY, stm.array.type.elementType, stm.array.genNotNull(), stm.index.genExpr(), stm.expr.genExpr()))

	open fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter.gen {
		var n = 0
		for (v in stm.values) {
			line(genStmSetArray(AstStm.SET_ARRAY(stm.array.value, AstExpr.LITERAL(stm.startIndex + n, types), v.value)))
			n++
		}
	}

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

	open fun genStmIf(stm: AstStm.IF) = indent {
		line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) }
	}

	open fun genStmIfElse(stm: AstStm.IF_ELSE) = indent {
		line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) }
		line("else") { line(stm.sfalse.genStm()) }
	}

	open val allowAssignItself = false

	open fun genStmSetLocal(stm: AstStm.SET_LOCAL) = indent {
		val localName = stm.local.nativeName
		val expr = stm.expr.genExpr()
		if (allowAssignItself || localName != expr) {
			// Avoid: Assigning a value to itself
			line(actualSetLocal(stm, localName, expr))
		}
	}

	open fun actualSetLocal(stm: AstStm.SET_LOCAL, localName: String, exprStr: String): String {
		return "$localName = $exprStr;"
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
	open fun genExprLiteralRefName(e: AstExpr.LITERAL_REFNAME): String {
		val value = e.value
		return genLiteralString(when (value) {
			is AstType.REF -> value.targetTypeNew.fqname
			is AstMethodRef -> value.targetName
			is AstFieldRef -> value.targetName2
			else -> invalidOp("Unknown AstExpr.LITERAL_REFNAME value type : ${value?.javaClass} : $value")
		})
	}

	open fun genLiteralType(v: AstType): String {
		for (fqName in v.getRefClasses()) {
			mutableBody.initClassRef(fqName, "class literal")
		}
		return escapeConstant(v)
	}

	open fun genLiteralNull(): String = escapeConstant(null)
	open fun genLiteralString(v: String): String = escapeConstant(v)
	open fun genLiteralBoolean(v: Boolean): String = escapeConstant(v)
	open fun genLiteralByte(v: Byte): String = escapeConstant(v)
	open fun genLiteralChar(v: Char): String = escapeConstant(v)
	open fun genLiteralShort(v: Short): String = escapeConstant(v)
	open fun genLiteralInt(v: Int): String = escapeConstant(v)
	open fun genLiteralLong(v: Long): String = escapeConstant(v)
	open fun genLiteralFloat(v: Float): String = escapeConstant(v)
	open fun genLiteralDouble(v: Double): String = escapeConstant(v)

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

		if ((from !is AstType.Primitive) && (to is AstType.Primitive)) {
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

		if ((from is AstType.Primitive) && (to !is AstType.Primitive)) {
			return when (from) {
				AstType.BOOL -> N_boxBool(e)
				AstType.BYTE -> N_boxByte(e)
				AstType.SHORT -> N_boxShort(e)
				AstType.CHAR -> N_boxChar(e)
				AstType.INT -> N_boxInt(e)
				AstType.LONG -> N_boxLong(e)
				AstType.FLOAT -> N_boxFloat(e)
				AstType.DOUBLE -> N_boxDouble(e)
				else -> invalidOp
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
					is AstType.LONG -> N_f2j(e)
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
					is AstType.LONG -> N_d2j(e)
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

	open protected fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "($array[$index])"
	open protected fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String) = "$array[$index] = $value;"

	fun N_box(type: AstType, e: String) = when (type) {
		is AstType.Primitive -> N_box(type, e)
		else -> e
	}

	fun N_box(type: AstType.Primitive, e: String) = when (type) {
		AstType.VOID -> N_boxVoid(e)
		AstType.BOOL -> N_boxBool(e)
		AstType.BYTE -> N_boxByte(e)
		AstType.CHAR -> N_boxChar(e)
		AstType.SHORT -> N_boxShort(e)
		AstType.INT -> N_boxInt(e)
		AstType.LONG -> N_boxLong(e)
		AstType.FLOAT -> N_boxFloat(e)
		AstType.DOUBLE -> N_boxDouble(e)
		else -> invalidOp("Don't know how to box $type")
	}

	fun N_unbox(type: AstType, e: String) = when (type) {
		is AstType.Primitive -> N_unbox(type, e)
		else -> e
	}

	fun N_unbox(type: AstType.Primitive, e: String) = when (type) {
		AstType.VOID -> N_unboxVoid(e)
		AstType.BOOL -> N_unboxBool(e)
		AstType.BYTE -> N_unboxByte(e)
		AstType.CHAR -> N_unboxChar(e)
		AstType.SHORT -> N_unboxShort(e)
		AstType.INT -> N_unboxInt(e)
		AstType.LONG -> N_unboxLong(e)
		AstType.FLOAT -> N_unboxFloat(e)
		AstType.DOUBLE -> N_unboxDouble(e)
		else -> invalidOp("Don't know how to unbox $type")
	}

	open protected fun N_boxVoid(e: String) = N_func("boxVoid", "$e")
	open protected fun N_boxBool(e: String) = N_func("boxBool", "$e")
	open protected fun N_boxByte(e: String) = N_func("boxByte", "$e")
	open protected fun N_boxShort(e: String) = N_func("boxShort", "$e")
	open protected fun N_boxChar(e: String) = N_func("boxChar", "$e")
	open protected fun N_boxInt(e: String) = N_func("boxInt", "$e")
	open protected fun N_boxLong(e: String) = N_func("boxLong", "$e")
	open protected fun N_boxFloat(e: String) = N_func("boxFloat", "$e")
	open protected fun N_boxDouble(e: String) = N_func("boxDouble", "$e")

	open protected fun N_unboxVoid(e: String) = N_func("unboxVoid", "$e")
	open protected fun N_unboxBool(e: String) = N_func("unboxBool", "$e")
	open protected fun N_unboxByte(e: String) = N_func("unboxByte", "$e")
	open protected fun N_unboxShort(e: String) = N_func("unboxShort", "$e")
	open protected fun N_unboxChar(e: String) = N_func("unboxChar", "$e")
	open protected fun N_unboxInt(e: String) = N_func("unboxInt", "$e")
	open protected fun N_unboxLong(e: String) = N_func("unboxLong", "$e")
	open protected fun N_unboxFloat(e: String) = N_func("unboxFloat", "$e")
	open protected fun N_unboxDouble(e: String) = N_func("unboxDouble", "$e")

	open protected fun N_is(a: String, b: AstType.Reference) = N_is(a, b.targetTypeCast.toString())
	open protected fun N_is(a: String, b: String) = N_func("is", "$a, $b")
	open protected fun N_z2z(str: String) = "($str)"
	open protected fun N_z2i(str: String) = N_func("z2i", "$str")
	open protected fun N_i(str: String) = "(($str)|0)"
	open protected fun N_i2z(str: String) = "(($str)!=0)"
	open protected fun N_i2b(str: String) = "(($str)<<24>>24)"
	open protected fun N_i2c(str: String) = "(($str)&0xFFFF)"
	open protected fun N_i2s(str: String) = "(($str)<<16>>16)"
	open protected fun N_f2i(str: String) = "(($str)|0)"
	open protected fun N_i2i(str: String) = N_i(str)
	open protected fun N_i2j(str: String) = N_func("i2j", "$str")
	open protected fun N_i2f(str: String) = "(($str))"
	open protected fun N_i2d(str: String) = "($str)"
	open protected fun N_f2f(str: String) = "($str)"
	open protected fun N_f2d(str: String) = "($str)"
	open protected fun N_d2f(str: String) = "(($str))"
	open protected fun N_d2d(str: String) = "($str)"
	open protected fun N_d2i(str: String) = "(($str)|0)"
	open protected fun N_f2j(str: String) = N_func("f2j", str)
	open protected fun N_d2j(str: String) = N_func("d2j", str)
	open protected fun N_l2i(str: String) = N_func("l2i", "$str")
	open protected fun N_l2l(str: String) = "($str)"
	open protected fun N_l2f(str: String) = N_func("l2d", "$str")
	open protected fun N_l2d(str: String) = N_func("l2d", "$str")
	open protected fun N_getFunction(str: String) = N_func("getFunction", "$str")
	open protected fun N_c(str: String, from: AstType, to: AstType) = "($str)"
	open protected fun N_lneg(str: String) = "-($str)"
	open protected fun N_ineg(str: String) = "-($str)"
	open protected fun N_fneg(str: String) = "-($str)"
	open protected fun N_dneg(str: String) = "-($str)"
	open protected fun N_iinv(str: String) = "~($str)"
	open protected fun N_linv(str: String) = "~($str)"
	open protected fun N_znot(str: String) = "!($str)"

	open protected fun N_zand(l: String, r: String) = "($l && $r)"
	open protected fun N_zor(l: String, r: String) = "($l || $r)"
	open protected fun N_zeq(l: String, r: String) = N_c_eq(l, r)
	open protected fun N_zne(l: String, r: String) = N_c_ne(l, r)

	open protected fun N_c_add(l: String, r: String) = "($l + $r)"
	open protected fun N_c_sub(l: String, r: String) = "($l - $r)"
	open protected fun N_c_mul(l: String, r: String) = "($l * $r)"
	open protected fun N_c_div(l: String, r: String) = "($l / $r)"
	open protected fun N_c_rem(l: String, r: String) = "($l % $r)"
	open protected fun N_c_eq(l: String, r: String) = "($l == $r)"
	open protected fun N_c_ne(l: String, r: String) = "($l != $r)"
	open protected fun N_c_ge(l: String, r: String) = "($l >= $r)"
	open protected fun N_c_le(l: String, r: String) = "($l <= $r)"
	open protected fun N_c_lt(l: String, r: String) = "($l < $r)"
	open protected fun N_c_gt(l: String, r: String) = "($l > $r)"
	open protected fun N_c_and(l: String, r: String) = "($l & $r)"
	open protected fun N_c_or(l: String, r: String) = "($l | $r)"
	open protected fun N_c_xor(l: String, r: String) = "($l ^ $r)"
	open protected fun N_c_shl(l: String, r: String) = "($l << $r)"
	open protected fun N_c_shr(l: String, r: String) = "($l >> $r)"
	open protected fun N_c_ushr(l: String, r: String) = "($l >>> $r)"

	open protected fun N_iadd(l: String, r: String) = N_c_add(l, r)
	open protected fun N_isub(l: String, r: String) = N_c_sub(l, r)
	open protected fun N_imul(l: String, r: String) = N_c_mul(l, r)
	open protected fun N_idiv(l: String, r: String) = N_c_div(l, r)
	open protected fun N_irem(l: String, r: String) = N_c_rem(l, r)
	open protected fun N_ieq(l: String, r: String) = N_c_eq(l, r)
	open protected fun N_ine(l: String, r: String) = N_c_ne(l, r)
	open protected fun N_ige(l: String, r: String) = N_c_ge(l, r)
	open protected fun N_ile(l: String, r: String) = N_c_le(l, r)
	open protected fun N_ilt(l: String, r: String) = N_c_lt(l, r)
	open protected fun N_igt(l: String, r: String) = N_c_gt(l, r)
	open protected fun N_iand(l: String, r: String) = N_c_and(l, r)
	open protected fun N_ior(l: String, r: String) = N_c_or(l, r)
	open protected fun N_ixor(l: String, r: String) = N_c_xor(l, r)
	open protected fun N_ishl(l: String, r: String) = N_c_shl(l, r)
	open protected fun N_ishr(l: String, r: String) = N_c_shr(l, r)
	open protected fun N_iushr(l: String, r: String) = N_c_ushr(l, r)

	open protected fun N_ladd(l: String, r: String) = N_func("ladd", "$l, $r")
	open protected fun N_lsub(l: String, r: String) = N_func("lsub", "$l, $r")
	open protected fun N_lmul(l: String, r: String) = N_func("lmul", "$l, $r")
	open protected fun N_ldiv(l: String, r: String) = N_func("ldiv", "$l, $r")
	open protected fun N_lrem(l: String, r: String) = N_func("lrem", "$l, $r")
	open protected fun N_leq(l: String, r: String) = N_func("leq", "$l, $r")
	open protected fun N_lne(l: String, r: String) = N_func("lne", "$l, $r")
	open protected fun N_lge(l: String, r: String) = N_func("lge", "$l, $r")
	open protected fun N_lle(l: String, r: String) = N_func("lle", "$l, $r")
	open protected fun N_llt(l: String, r: String) = N_func("llt", "$l, $r")
	open protected fun N_lgt(l: String, r: String) = N_func("lgt", "$l, $r")
	open protected fun N_land(l: String, r: String) = N_func("land", "$l, $r")
	open protected fun N_lor(l: String, r: String) = N_func("lor", "$l, $r")
	open protected fun N_lxor(l: String, r: String) = N_func("lxor", "$l, $r")
	open protected fun N_lshl(l: String, r: String) = N_func("lshl", "$l, $r")
	open protected fun N_lshr(l: String, r: String) = N_func("lshr", "$l, $r")
	open protected fun N_lushr(l: String, r: String) = N_func("lushr", "$l, $r")
	open protected fun N_lcmp(l: String, r: String) = N_func("lcmp", "$l, $r")

	open protected fun N_fadd(l: String, r: String) = N_fd_add(l, r)
	open protected fun N_fsub(l: String, r: String) = N_fd_sub(l, r)
	open protected fun N_fmul(l: String, r: String) = N_fd_mul(l, r)
	open protected fun N_fdiv(l: String, r: String) = N_fd_div(l, r)
	open protected fun N_frem(l: String, r: String) = N_fd_rem(l, r)
	open protected fun N_feq(l: String, r: String) = N_fd_eq(l, r)
	open protected fun N_fne(l: String, r: String) = N_fd_ne(l, r)
	open protected fun N_fge(l: String, r: String) = N_fd_ge(l, r)
	open protected fun N_fle(l: String, r: String) = N_fd_le(l, r)
	open protected fun N_flt(l: String, r: String) = N_fd_lt(l, r)
	open protected fun N_fgt(l: String, r: String) = N_fd_gt(l, r)

	open protected fun N_dadd(l: String, r: String) = N_fd_add(l, r)
	open protected fun N_dsub(l: String, r: String) = N_fd_sub(l, r)
	open protected fun N_dmul(l: String, r: String) = N_fd_mul(l, r)
	open protected fun N_ddiv(l: String, r: String) = N_fd_div(l, r)
	open protected fun N_drem(l: String, r: String) = N_fd_rem(l, r)
	open protected fun N_deq(l: String, r: String) = N_fd_eq(l, r)
	open protected fun N_dne(l: String, r: String) = N_fd_ne(l, r)
	open protected fun N_dge(l: String, r: String) = N_fd_ge(l, r)
	open protected fun N_dle(l: String, r: String) = N_fd_le(l, r)
	open protected fun N_dlt(l: String, r: String) = N_fd_lt(l, r)
	open protected fun N_dgt(l: String, r: String) = N_fd_gt(l, r)


	open protected fun N_fcmpl(l: String, r: String) = N_fd_cmpl(l, r)
	open protected fun N_fcmpg(l: String, r: String) = N_fd_cmpg(l, r)

	open protected fun N_dcmpl(l: String, r: String) = N_fd_cmpl(l, r)
	open protected fun N_dcmpg(l: String, r: String) = N_fd_cmpg(l, r)

	open protected fun N_fd_add(l: String, r: String) = N_c_add(l, r)
	open protected fun N_fd_sub(l: String, r: String) = N_c_sub(l, r)
	open protected fun N_fd_mul(l: String, r: String) = N_c_mul(l, r)
	open protected fun N_fd_div(l: String, r: String) = N_c_div(l, r)
	open protected fun N_fd_rem(l: String, r: String) = N_c_rem(l, r)
	open protected fun N_fd_eq(l: String, r: String) = N_c_eq(l, r)
	open protected fun N_fd_ne(l: String, r: String) = N_c_ne(l, r)
	open protected fun N_fd_ge(l: String, r: String) = N_c_ge(l, r)
	open protected fun N_fd_le(l: String, r: String) = N_c_le(l, r)
	open protected fun N_fd_lt(l: String, r: String) = N_c_lt(l, r)
	open protected fun N_fd_gt(l: String, r: String) = N_c_gt(l, r)
	open protected fun N_fd_cmpl(l: String, r: String) = N_func("cmpl", "$l, $r")
	open protected fun N_fd_cmpg(l: String, r: String) = N_func("cmpg", "$l, $r")

	open protected fun N_obj_eq(l: String, r: String) = N_c_eq(l, r)
	open protected fun N_obj_ne(l: String, r: String) = N_c_ne(l, r)

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
		val bodies = this.annotationsList
			.getTypedList(com.jtransc.annotation.JTranscMethodBodyList::value)
			.filter { TargetName.matches(it.target, target) }

		return bodies.associate { body ->
			body.cond to Indenter.gen {
				for (line in body.value.toList()) line(line.template("nativeBody"))
			}
		}
	}

	enum class TypeKind { TYPETAG, NEW, CAST }

	val AstField.constantValueOrNativeDefault: Any? get() = if (this.hasConstantValue) this.constantValue else this.type.nativeDefault
	val AstField.escapedConstantValue: String get() = escapeConstantRef(this.constantValueOrNativeDefault, this.type)

	val FieldRef.targetName2: String get() = getNativeName(this)
	val LocalParamRef.nativeName: String get() = getNativeName2(this)
	val AstType.nativeDefault: Any? get() = getDefault(this)
	val AstType.nativeDefaultString: String get() = escapeConstant(getDefault(this), this)
	val FieldRef.nativeStaticText: String get() = buildStaticField(this)
	val MethodRef.targetName: String get() = getNativeName(this)

	val AstType.targetTypeTag: FqName get() = getNativeType(this, TypeKind.TYPETAG)
	val AstType.targetTypeNew: FqName get() = getNativeType(this, TypeKind.NEW)
	val AstType.targetTypeCast: FqName get() = getNativeType(this, TypeKind.CAST)

	val FqName.targetClassFqName: String get() = getClassFqName(this)
	val FqName.targetFilePath: String get() = getFilePath(this)
	val FqName.targetGeneratedFqName: FqName get() = getGeneratedFqName(this)
	val FqName.targetGeneratedSimpleClassName: String get() = getGeneratedSimpleClassName(this)

	val configMinimizeNames: ConfigMinimizeNames? = injector.getOrNull()
	val minimize: Boolean = configMinimizeNames?.minimizeNames ?: false

	private var minClassLastId: Int = 0
	private var minMemberLastId: Int = 0

	fun allocClassName(): String = keywords.runUntilNotInSet { MinimizedNames.getTypeNameById(minClassLastId++) }
	fun allocMemberName(): String = keywords.runUntilNotInSet { MinimizedNames.getIdNameById(minMemberLastId++) }

	private fun <T> Set<T>.runUntilNotInSet(callback: () -> T): T {
		while (true) {
			val result = callback()
			if (result !in this) return result
		}
	}

	lateinit var currentClass: FqName
	lateinit var currentMethod: AstMethodRef

	enum class StringPoolType { GLOBAL, PER_CLASS }

	open val stringPoolType: StringPoolType = StringPoolType.GLOBAL

	class StringPool {
		private var lastId = 0
		private val stringIds = hashMapOf<String, Int>()
		private var valid = false
		private var cachedEntries = listOf<StringInPool>()
		fun alloc(str: String): Int {
			return stringIds.getOrPut(str) {
				valid = false
				lastId++
			}
		}

		fun getAllSorted(): List<StringInPool> {
			if (!valid) {
				cachedEntries = stringIds.entries.map { StringInPool(it.value, it.key) }.sortedBy { it.id }.toList()
				valid = true
			}
			return cachedEntries
		}
	}

	class PerClassNameAllocator {
		val usedNames = hashSetOf<String>()
		val allocatedNames = hashMapOf<Any, String>()

		fun allocate(key: Any, requestedName: () -> String): String {
			if (key !in allocatedNames) {
				var finalName = requestedName()
				while (finalName in usedNames) {
					finalName += "_"
				}
				usedNames += finalName
				allocatedNames[key] = finalName
			}
			return allocatedNames[key]!!
		}
	}

	val perClassNameAllocator = hashMapOf<FqName, PerClassNameAllocator>()

	private val stringPoolGlobal = StringPool()
	private val stringPoolPerClass = hashMapOf<FqName, StringPool>()

	data class StringInPool(val id: Int, val str: String) {
		val name = "STRINGLIT_$id"
	}

	fun getClassNameAllocator(clazz: FqName) = perClassNameAllocator.getOrPut(clazz) { PerClassNameAllocator() }

	private fun getPerClassStrings(clazz: FqName) = stringPoolPerClass.getOrPut(clazz) { StringPool() }

	fun getGlobalStrings(): List<StringInPool> = when (stringPoolType) {
		StringPoolType.GLOBAL -> stringPoolGlobal.getAllSorted()
		else -> invalidOp("This target doesn't support global string pool")
	}

	fun getClassStrings(clazz: FqName): List<StringInPool> = when (stringPoolType) {
		StringPoolType.PER_CLASS -> getPerClassStrings(clazz).getAllSorted()
		else -> invalidOp("This target doesn't support per class string pool")
	}

	fun allocString(clazz: FqName, str: String): Int = when (stringPoolType) {
		StringPoolType.GLOBAL -> stringPoolGlobal.alloc(str)
		StringPoolType.PER_CLASS -> getPerClassStrings(clazz).alloc(str)
	}

	open fun buildTemplateClass(clazz: FqName): String = getClassFqNameForCalling(clazz)
	open fun buildTemplateClass(clazz: AstClass): String = getClassFqNameForCalling(clazz.name)

	fun buildField(field: AstField, static: Boolean): String {
		return if (static) buildStaticField(field) else getNativeName(field)
	}

	open fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazz = getClassFqNameForCalling(method.containingClass.name)
		val name = getNativeName(method)
		return if (static) (clazz + buildAccessName(name, static = true)) else name
	}

	open fun buildStaticInit(clazz: AstClass): String {
		//getClassStaticInit(clazz.ref, "template sinit")
		return getClassFqNameForCalling(clazz.name) + buildAccessName("SI", static = true) + "();"
	}

	open fun buildConstructor(method: AstMethod): String {
		val clazz = getClassFqNameForCalling(method.containingClass.name)
		val methodName = getNativeName(method)
		return "new $clazz()[${methodName.quote()}]"
	}

	open fun getClassStaticInit(classRef: AstType.REF, reason: String): String = buildStaticInit(program[classRef.name]!!)

	open fun getClassFqName(name: FqName): String = name.fqname
	open fun getFilePath(name: FqName): String = name.simpleName

	open fun buildInstanceField(expr: String, field: AstField): String = expr + buildAccessName(field, static = false)
	open fun buildStaticField(field: AstField): String = getNativeNameForFields(field.ref.containingTypeRef.name) + buildAccessName(field, static = true)

	fun buildStaticField(field: FieldRef): String = buildStaticField(program[field.ref]!!)
	fun buildInstanceField(expr: String, field: FieldRef): String = buildInstanceField(expr, program[field.ref]!!)

	open fun buildAccessName(field: AstField, static: Boolean): String = buildAccessName(getNativeName(field), static)
	open fun buildAccessName(name: String, static: Boolean): String = ".$name"

	val normalizeNameCache = hashMapOf<String, String>()

	fun normalizeName(name: String): String {
		if (name.isNullOrEmpty()) return ""
		if (name !in normalizeNameCache) {
			val chars = name.toCharArray()
			for (i in chars.indices) {
				var c = chars[i]
				if (!c.isLetterDigitOrUnderscore() || c == '$') c = '_'
				chars[i] = c
			}
			if (chars[0].isDigit()) chars[0] = '_'
			normalizeNameCache[name] = String(chars)
		}
		return normalizeNameCache[name]!!
	}

	open fun getNativeName2(local: LocalParamRef): String = normalizeName(local.name)
	open fun getNativeName(field: FieldRef): String = normalizeName(field.ref.name)
	open fun getNativeName(methodRef: MethodRef): String {
		//if (program is AstProgram) {
		//	val method = methodRef.ref.resolve(program)
		//	return normalizeName(method.nativeName ?: method.ref.name)
		//}
		return normalizeName(methodRef.ref.name)
	}

	open fun getNativeName(clazz: FqName): String = getClassFqNameForCalling(clazz)

	inline fun <reified T : Any> nativeName(): String = getNativeName(T::class.java.name.fqname)

	fun getNativeFieldName(clazz: Class<*>, name: String): String {
		val actualClazz = program[clazz.name.fqname] ?: invalidOp("Can't find field $clazz.$name")
		val actualField = actualClazz.fieldsByName[name] ?: invalidOp("Can't find field $clazz.$name")
		return getNativeName(actualField)
	}

	inline fun <reified T : Any, R> getNativeFieldName(prop: KMutableProperty1<T, R>): String {
		return getNativeFieldName(T::class.java, prop.name)
	}

	open fun getNativeNameForMethods(clazz: FqName): String = getNativeName(clazz)
	open fun getNativeNameForFields(clazz: FqName): String = getNativeName(clazz)

	open val NullType = FqName("Dynamic")
	open val VoidType = FqName("Void")
	open val BoolType = FqName("Bool")
	open val IntType = FqName("Int")
	open val FloatType = FqName("Float32")
	open val DoubleType = FqName("Float64")
	open val LongType = FqName("haxe.Int64")
	open val BaseArrayType = FqName("JA_0")
	open val BoolArrayType = FqName("JA_Z")
	open val ByteArrayType = FqName("JA_B")
	open val CharArrayType = FqName("JA_C")
	open val ShortArrayType = FqName("JA_S")
	open val IntArrayType = FqName("JA_I")
	open val LongArrayType = FqName("JA_J")
	open val FloatArrayType = FqName("JA_F")
	open val DoubleArrayType = FqName("JA_D")
	open val ObjectArrayType = FqName("JA_L")

	open fun getDefault(type: AstType): Any? = type.getNull()

	open fun getNativeType(type: AstType, typeKind: CommonGenerator.TypeKind): FqName {
		return when (type) {
			is AstType.NULL -> NullType
			is AstType.VOID -> VoidType
			is AstType.BOOL -> BoolType
			is AstType.GENERIC -> getNativeType(type.type, typeKind)
			is AstType.INT, is AstType.SHORT, is AstType.CHAR, is AstType.BYTE -> IntType
			is AstType.FLOAT -> FloatType
			is AstType.DOUBLE -> DoubleType
			is AstType.LONG -> LongType
			is AstType.REF -> FqName(program[type.name]?.nativeName ?: getNativeName(type.name))
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> BoolArrayType
				is AstType.BYTE -> ByteArrayType
				is AstType.CHAR -> CharArrayType
				is AstType.SHORT -> ShortArrayType
				is AstType.INT -> IntArrayType
				is AstType.LONG -> LongArrayType
				is AstType.FLOAT -> FloatArrayType
				is AstType.DOUBLE -> DoubleArrayType
				else -> ObjectArrayType
			}
			else -> throw RuntimeException("Not supported native type $type, $typeKind")
		}
	}

	open fun escapeConstantRef(value: Any?, type: AstType): String {
		return when (value) {
			is Long -> N_func("lnewRef", "${value.high}, ${value.low}")
			else -> escapeConstant(value, type)
		}
	}

	open fun escapeConstant(value: Any?, type: AstType): String {
		val result = escapeConstant(value)
		return if (type != AstType.BOOL) result else if (result != "false" && result != "0") "true" else "false"
	}

	open val staticAccessOperator: String = "."
	open val instanceAccessOperator: String = "."

	open fun N_lnew(value: Long) = N_func("lnew", "${value.high}, ${value.low}")

	open fun escapeConstant(value: Any?): String = when (value) {
		null -> "null"
		is Boolean -> if (value) "true" else "false"
		is String -> N_func("strLitEscape", value.quote())
		is Long -> N_lnew(value)
		is Float -> escapeConstant(value.toDouble())
		is Double -> if (value.isInfinite()) if (value < 0) NegativeInfinityString else PositiveInfinityString else if (value.isNaN()) NanString else "$value"
		is Int -> when (value) {
			Int.MIN_VALUE -> "N${staticAccessOperator}MIN_INT32"
			else -> "$value"
		}
		is Number -> "${value.toInt()}"
		is Char -> "${value.toInt()}"
		is AstType -> N_func("resolveClass", "${value.mangle().quote()}")
		else -> throw NotImplementedError("Literal of type $value")
	}

	open val NegativeInfinityString = "-Infinity"
	open val PositiveInfinityString = "Infinity"
	open val NanString = "NaN"

	open fun getClassFqNameForCalling(fqName: FqName): String = fqName.fqname.replace('.', '_')

	open fun getGeneratedFqName(name: FqName): FqName = name
	open fun getGeneratedSimpleClassName(name: FqName): String = name.fqname

	fun getFieldName(clazz: Class<*>, name: String): String = getFieldName(program[clazz.name.fqname].fieldsByName[name]!!)
	fun getFieldName(field: FieldRef): String = getFieldName(field.ref)
	fun getFieldName(field: AstField) = getFieldName(field.ref)
	open fun getFieldName(field: AstFieldRef): String = field.name

	open fun getClassFqNameLambda(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		return getClassFqName(clazz.name) + ".${simpleName}_Lambda"
	}

	open fun getClassFqNameInt(name: FqName): String {
		val clazz = program[name]
		val simpleName = getGeneratedSimpleClassName(name)
		val suffix = if (clazz.isInterface) ".${simpleName}_IFields" else ""
		return getClassFqName(clazz.name) + "$suffix"
	}

	open fun getGeneratedFqPackage(name: FqName): String = name.packagePath

	open fun getFunctionalType2(type: AstType.METHOD): String {
		return type.argsPlusReturnVoidIsEmpty.map { getNativeType(it, CommonGenerator.TypeKind.TYPETAG) }.joinToString(" -> ")
	}

	open fun getAnnotationProxyName(classRef: AstType.REF): String = "AnnotationProxy_${getGeneratedFqName(classRef.name).fqname.replace('.', '_')}"

	open fun getFullAnnotationProxyName(classRef: AstType.REF): String {
		return getClassFqName(classRef.name) + ".AnnotationProxy_${getGeneratedFqName(classRef.name).fqname.replace('.', '_')}"
	}

	open fun getClassStaticClassInit(classRef: AstType.REF): String = "${getClassFqNameInt(classRef.name)}.HAXE_CLASS_INIT"

	open fun getTargetMethodAccess(refMethod: AstMethod, static: Boolean): String = buildAccessName(getNativeName(refMethod), static)

	open fun getTypeStringForCpp(type: AstType): String = noImpl

	fun N_func(name: String, args: String) = "N$staticAccessOperator$name($args)"


	/////////////////////////////////////////////
	/////////////////////////////////////////////

	val outputFile2 = configOutputFile2.file
	val configTargetDirectory: ConfigTargetDirectory = injector.get()

	//val outputFile2 = File(File(tinfo.outputFile).absolutePath)
	val tempdir = configTargetDirectory.targetDirectory

	val params = hashMapOf(
		"outputFolder" to outputFile2.parent,
		"outputFile" to outputFile2.absolutePath,
		"outputFileBase" to outputFile2.name,
		"release" to settings.release,
		"debug" to !settings.release,
		"releasetype" to if (settings.release) "release" else "debug",
		"settings" to settings,
		"title" to settings.title,
		"name" to settings.name,
		"package" to settings.package_,
		"version" to settings.version,
		"company" to settings.company,
		"initialWidth" to settings.initialWidth,
		"initialHeight" to settings.initialHeight,
		"orientation" to settings.orientation.lowName,
		"assetFiles" to MergeVfs(settings.assets.map { LocalVfs(it) }).listdirRecursive().filter { it.isFile }.map { it.file },
		"embedResources" to settings.embedResources,
		"assets" to settings.assets,
		"hasIcon" to !settings.icon.isNullOrEmpty(),
		"icon" to settings.icon,
		"libraries" to settings.libraries,
		"extra" to settings.extra,
		"folders" to folders
	)

	fun setInfoAfterBuildingSource() {
		params["entryPointFile"] = injector.get<ConfigEntryPointFile>().entryPointFile
		params["entryPointClass"] = buildTemplateClass(injector.get<ConfigEntryPointClass>().entryPointClass)
	}

	fun setExtraData(map: Map<String, Any?>) {
		for ((key, value) in map) {
			this.params[key] = value
		}
	}

	private fun getOrReplaceVar(name: String): String {
		val out = if (name.startsWith("#")) {
			params[name.substring(1)].toString()
		} else {
			name
		}
		return out
	}

	private fun evalReference(type: String, desc: String): String {
		val ref = CommonTagHandler.getRef(program, type, desc, params)
		return when (ref) {
			is CommonTagHandler.SINIT -> buildStaticInit(ref.method.containingClass);
			is CommonTagHandler.CONSTRUCTOR -> buildConstructor(ref.method)
			is CommonTagHandler.METHOD -> buildMethod(ref.method, static = ref.isStatic)
			is CommonTagHandler.FIELD -> buildField(ref.field, static = ref.isStatic)
			is CommonTagHandler.CLASS -> buildTemplateClass(ref.clazz)
			else -> invalidOp("Unsupported result")
		}
	}

	class ProgramRefNode(val ts: CommonGenerator, val type: String, val desc: String) : Minitemplate.BlockNode {
		override fun eval(context: Minitemplate.Context) {
			context.write(ts.evalReference(type, desc))
		}
	}

	//class CopyFileNode(val ts: HaxeTemplateString, val type:String, val expr:Minitemplate.ExprNode) : Minitemplate.BlockNode {
	//	override fun eval(context: Minitemplate.Context) {
	//		val filetocopy = expr.eval(context)
	//	}
	//}

	val miniConfig = Minitemplate.Config(
		extraTags = listOf(
			Minitemplate.Tag(
				":programref:", setOf(), null,
				aliases = listOf(
					//"sinit", "constructor", "smethod", "method", "sfield", "field", "class",
					"SINIT", "CONSTRUCTOR", "SMETHOD", "METHOD", "SFIELD", "FIELD", "CLASS"
				)
			) { ProgramRefNode(this, it.first().token.name, it.first().token.content) }
			//, Minitemplate.Tag("copyfile", setOf(), null) {
			//	CopyFileNode(this, it.first().token.name, Minitemplate.ExprNode.parse(it.first().token.content))
			//}
		),
		extraFilters = listOf(
		)
	)

	override fun gen(template: String): String = gen(template, extra = hashMapOf())

	fun gen(template: String, extra: HashMap<String, Any?> = hashMapOf()): String = Minitemplate(template, miniConfig).invoke(HashMap(params + extra))

	fun gen(template: String, process: Boolean): String = if (process) Minitemplate(template, miniConfig).invoke(params) else template

	fun gen(template: String, context: AstGenContext, type: String): String {
		//System.out.println("WARNING: templates not implemented! : $type : $context : $template");
		context.rethrowWithContext {
			return Minitemplate(template, miniConfig).invoke(params)
		}
	}
}