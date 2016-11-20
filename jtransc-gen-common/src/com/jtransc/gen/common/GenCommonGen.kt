package com.jtransc.gen.common

import com.jtransc.ConfigOutputFile
import com.jtransc.annotation.JTranscInvisible
import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.OptimizeFeature
import com.jtransc.ast.feature.method.SimdFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.ast.treeshaking.getTargetAddFiles
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.noImplWarn
import com.jtransc.injector.Injector
import com.jtransc.lang.JA_I
import com.jtransc.text.Indenter
import com.jtransc.vfs.SyncVfsFile
import java.util.*

class ConfigSrcFolder(val srcFolder: SyncVfsFile)

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "RemoveSingleExpressionStringTemplate")
open class GenCommonGen(val injector: Injector) {
	val configSrcFolder: ConfigSrcFolder = injector.get()
	val srcFolder: SyncVfsFile = configSrcFolder.srcFolder

	val features: AstMethodFeatures = injector.get()
	val configFeatureSet: ConfigFeatureSet = injector.get()
	val featureSet: Set<Class<out AstMethodFeature>> = configFeatureSet.featureSet

	val program: AstProgram = injector.get()
	val settings: AstBuildSettings = injector.get()
	val names: CommonNames = injector.get()
	val templateString: CommonProgramTemplate = injector.get()
	val folders: CommonGenFolders = injector.get()

	val configOutputFile: ConfigOutputFile = injector.get()
	val configOutputFile2: ConfigOutputFile2 = injector.get()
	val outputFileBaseName = configOutputFile.outputFileBaseName
	val outputFile = configOutputFile.output

	val types: AstTypes = program.types
	val context = AstGenContext()
	val refs = References()

	val JAVA_LANG_OBJECT = names.nativeName<java.lang.Object>()
	val JAVA_LANG_CLASS = names.nativeName<java.lang.Class<*>>()
	val JAVA_LANG_CLASS_name = names.getNativeFieldName(java.lang.Class::class.java, "name")
	val JAVA_LANG_STRING = names.nativeName<java.lang.String>()

	val invocationHandlerTargetName by lazy { names.nativeName<java.lang.reflect.InvocationHandler>() }
	val methodTargetName = names.nativeName<java.lang.reflect.Method>()
	val invokeTargetName by lazy { AstMethodRef(java.lang.reflect.InvocationHandler::class.java.name.fqname, "invoke", types.build { METHOD(OBJECT, OBJECT, METHOD, ARRAY(OBJECT)) }).targetName }
	val toStringTargetName by lazy { AstMethodRef(java.lang.Object::class.java.name.fqname, "toString", types.build { METHOD(STRING) }).targetName }
	val hashCodeTargetName by lazy { AstMethodRef(java.lang.Object::class.java.name.fqname, "hashCode", types.build { METHOD(INT) }).targetName }
	val getClassTargetName by lazy { AstMethodRef(java.lang.Object::class.java.name.fqname, "getClass", types.build { METHOD(CLASS) }).targetName }

	protected fun setCurrentClass(clazz: AstClass) {
		context.clazz = clazz
		names.currentClass = clazz.name
	}

	protected fun setCurrentMethod(method: AstMethod) {
		context.method = method
		names.currentMethod = method.ref
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

		val methodAccess = names.getTargetMethodAccess(refMethod, static = isStaticCall)
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

	fun String.template(type: String = "template"): String = templateString.gen(this, context, type)

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
		//return if (ENABLE_HXCPP_GOTO_HACK && (tinfo.subtarget in setOf("cpp", "windows", "linux", "mac", "android"))) {
		//	features.apply(body, (featureSet + setOf(GotosFeature)), settings, types).genBody()
		//} else {
		return features.apply(method, body, featureSet, settings, types).genBody()
		//}
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
		line(names.buildStaticInit(program[clazzRef.name]))
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
		val left = names.buildInstanceField(stm.left.genExpr(), fixField(stm.field))
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
		line("switch (${stm.subject.genExpr()})") {
			for (case in stm.cases) {
				val value = case.first
				val caseStm = case.second
				if (caseStm.value.isSingleStm()) {
					if (caseStm.value.lastStm().isBreakingFlow()) {
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
			"new ${names.ObjectArrayType}(${e.counts[0].genExpr()}, \"$desc\")"
		} else {
			"new ${e.type.targetTypeNew}(${e.counts[0].genExpr()})"
		}
	}

	open fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "${names.ObjectArrayType}${staticAccessOperator}createMultiSure([${e.counts.map { it.genExpr() }.joinToString(", ")}], \"$desc\")"
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
		return names.buildInstanceField(e.expr.genNotNull(), fixField(e.field))
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
		return names.escapeConstant(v)
	}

	open fun genLiteralNull(): String = names.escapeConstant(null)
	open fun genLiteralString(v: String): String = names.escapeConstant(v)
	open fun genLiteralBoolean(v: Boolean): String = names.escapeConstant(v)
	open fun genLiteralByte(v: Byte): String = names.escapeConstant(v)
	open fun genLiteralChar(v: Char): String = names.escapeConstant(v)
	open fun genLiteralShort(v: Short): String = names.escapeConstant(v)
	open fun genLiteralInt(v: Int): String = names.escapeConstant(v)
	open fun genLiteralLong(v: Long): String = names.escapeConstant(v)
	open fun genLiteralFloat(v: Float): String = names.escapeConstant(v)
	open fun genLiteralDouble(v: Double): String = names.escapeConstant(v)

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

	val staticAccessOperator: String = names.staticAccessOperator
	val instanceAccessOperator: String = names.instanceAccessOperator

	fun N_func(name: String, args: String) = names.N_func(name, args)

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
		val bodies = this.annotationsList.getTypedList(com.jtransc.annotation.JTranscMethodBodyList::value).filter { it.target == target }

		return bodies.associate { body ->
			body.cond to Indenter.gen {
				for (line in body.value.toList()) line(line.template("nativeBody"))
			}
		}
	}

	enum class TypeKind { TYPETAG, NEW, CAST }

	val AstField.constantValueOrNativeDefault: Any? get() = if (this.hasConstantValue) this.constantValue else this.type.nativeDefault
	val AstField.escapedConstantValue: String get() = names.escapeConstantRef(this.constantValueOrNativeDefault, this.type)

	val FieldRef.targetName2: String get() = names.getNativeName(this)
	val LocalParamRef.nativeName: String get() = names.getNativeName(this)
	val AstType.nativeDefault: Any? get() = names.getDefault(this)
	val AstType.nativeDefaultString: String get() = names.escapeConstant(names.getDefault(this), this)
	val FieldRef.nativeStaticText: String get() = names.buildStaticField(this)
	val MethodRef.targetName: String get() = names.getNativeName(this)

	val AstType.targetTypeTag: FqName get() = names.getNativeType(this, TypeKind.TYPETAG)
	val AstType.targetTypeNew: FqName get() = names.getNativeType(this, TypeKind.NEW)
	val AstType.targetTypeCast: FqName get() = names.getNativeType(this, TypeKind.CAST)

	val FqName.targetClassFqName: String get() = names.getClassFqName(this)
	val FqName.targetFilePath: String get() = names.getFilePath(this)
	val FqName.targetGeneratedFqName: FqName get() = names.getGeneratedFqName(this)
	val FqName.targetGeneratedSimpleClassName: String get() = names.getGeneratedSimpleClassName(this)
}