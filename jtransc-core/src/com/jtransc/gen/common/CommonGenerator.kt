package com.jtransc.gen.common

import com.jtransc.*
import com.jtransc.annotation.*
import com.jtransc.ast.*
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.ast.treeshaking.getTargetAddFiles
import com.jtransc.ds.getOrPut2
import com.jtransc.ds.toHashMap
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.noImplWarn
import com.jtransc.error.unexpected
import com.jtransc.gen.MinimizedNames
import com.jtransc.gen.TargetName
import com.jtransc.injector.Injector
import com.jtransc.io.ProcessResult2
import com.jtransc.json.Json
import com.jtransc.lang.high
import com.jtransc.lang.low
import com.jtransc.lang.putIfAbsentJre7
import com.jtransc.log.log
import com.jtransc.plugin.JTranscPluginGroup
import com.jtransc.template.Minitemplate
import com.jtransc.text.Indenter
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.quote
import com.jtransc.text.substr
import com.jtransc.util.toIntOrNull2
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*
import kotlin.reflect.KMutableProperty1

class ConfigSrcFolder(val srcFolder: SyncVfsFile)

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "RemoveSingleExpressionStringTemplate")
abstract class CommonGenerator(val injector: Injector) : IProgramTemplate {
	abstract val SINGLE_FILE: Boolean
	open val ADD_UTF8_BOM = false

	// CONFIG
	open val staticAccessOperator: String = "."
	open val instanceAccessOperator: String = "."
	open val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	open val languageRequiresDefaultInSwitch = false
	open val defaultGenStmSwitchHasBreaks = true
	open val interfacesSupportStaticMembers = true
	open val usePackages = true
	open val classFileExtension = ""
	open val allowRepeatMethodsInInterfaceChain = true
	open val localVarPrefix = ""
	open val floatHasFSuffix = true
	open val casesWithCommas = false
	open val optionalDoubleDummyDecimals = false

	open val GENERATE_LINE_NUMBERS = true

	val configTargetFolder: ConfigTargetFolder = injector.get()
	val plugins: JTranscPluginGroup = injector.get()
	val program: AstProgram = injector.get()
	val sortedClasses by lazy { program.classes.filter { it.mustGenerate }.sortedByExtending() }
	val targetName = injector.get<TargetName>()
	open val methodFeatures: Set<Class<out AstMethodFeature>> = setOf()
	open val methodFeaturesWithTraps: Set<Class<out AstMethodFeature>> get() = methodFeatures
	open val keywords: Set<String> = program.getExtraKeywords(targetName.name).toSet()
	val configSrcFolder: ConfigSrcFolder = injector.get()
	open val srcFolder: SyncVfsFile = configSrcFolder.srcFolder
	val configMinimizeNames: ConfigMinimizeNames? = injector.getOrNull()
	val minimize: Boolean = configMinimizeNames?.minimizeNames ?: false

	val settings: AstBuildSettings = injector.get()
	val extraParams = settings.extra
	val extraVars by lazy { program.getTemplateVariables(targetName, settings.extraVars) }
	val debugVersion: Boolean = settings.debug
	val folders: CommonGenFolders = injector.get()

	val configOutputFile: ConfigOutputFile = injector.get()
	val configOutputFile2: ConfigOutputFile2 = injector.get()
	val outputFileBaseName: String = configOutputFile.outputFileBaseName
	val outputFile = configOutputFile.output

	open val outputFile2 = configOutputFile2.file
	val configTargetDirectory: ConfigTargetDirectory = injector.get()
	val tempdir = configTargetDirectory.targetDirectory

	val features = injector.get<AstMethodFeatures>()

	val types: AstTypes = program.types
	val context = AstGenContext()
	val refs = References()

	val targetLibraries by lazy { program.getLibsFor(targetName) }
	val targetIncludes by lazy { program.getIncludesFor(targetName) }
	val targetImports by lazy { program.getImportsFor(targetName) }
	val targetDefines by lazy { program.getDefinesFor(targetName) }

	open val allTargetLibraries by lazy { targetLibraries + (injector.getOrNull<ConfigLibraries>()?.libs ?: listOf()) }
	open val allTargetDefines by lazy { targetDefines }

	val AstClass.nativeMembers get() = this.getMembersFor(targetName)

	open fun writeProgramAndFiles(): Unit {
		if (SINGLE_FILE) {
			writeClasses(configTargetFolder.targetFolder)
			setTemplateParamsAfterBuildingSource()
		} else {
			val output = configTargetFolder.targetFolder
			writeClasses(output)
			setTemplateParamsAfterBuildingSource()
			for (file in getFilesToCopy(targetName.name)) {
				val str = program.resourcesVfs[file.src].readString()
				val strr = if (file.process) str.template("includeFile") else str
				output[file.dst] = strr
			}
		}
	}

	open val fixencoding = true

	open fun compile(): ProcessResult2 {
		val cmdAndArgs = genCompilerCommand(
			programFile = configTargetFolder.targetFolder[configOutputFile.output].realfile,
			debug = settings.debug,
			libs = injector.getOrNull<ConfigLibraries>()?.libs ?: listOf()
		)

		val cmdAndArgsStr = cmdAndArgs.joinToString(" ")

		println(cmdAndArgsStr)
		return if (cmdAndArgs.isEmpty()) {
			ProcessResult2(0)
		} else {
			val result = LocalVfs(File(configTargetFolder.targetFolder.realpathOS)).exec(cmdAndArgs, ExecOptions(sysexec = true, fixencoding = fixencoding, passthru = true))
			if (!result.success) {
				throw RuntimeException("success=${result.success} exitCode=${result.exitCode} output='${result.outputString}' error='${result.errorString}' folder=${configTargetFolder.targetFolder.realpathOS} command='$cmdAndArgsStr'")
			}
			ProcessResult2(result)
		}
	}

	open protected fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return listOf()
	}

	open fun run(redirect: Boolean = true): ProcessResult2 {
		return ProcessResult2(0)
	}

	open fun compileAndRun(redirect: Boolean = true): ProcessResult2 {
		val compileResult = compile()
		return if (!compileResult.success) {
			ProcessResult2(compileResult.exitValue)
		} else {
			this.run(redirect)
		}
	}

	open fun writeClasses(output: SyncVfsFile) {
		if (SINGLE_FILE) {
			if (ADD_UTF8_BOM) {
				output[outputFileBaseName] = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + genSingleFileClasses(output).toString().toByteArray()
			} else {
				output[outputFileBaseName] = genSingleFileClasses(output).toString()
			}
		} else {
			for (clazz in sortedClasses) {
				val results = genClass(clazz)
				for (result in results) {
					output[getClassFilename(result.subclass.clazz, result.subclass.type)] = result.indenter.toString()
				}
			}
		}
	}

	open fun getClassBaseFilename(clazz: AstClass, type: MemberTypes): String {
		val basename = if (type == MemberTypes.STATIC) {
			clazz.name.targetNameForStatic
		} else {
			clazz.name.targetName
		}

		return basename.replace('.', if (usePackages) '/' else '_')
	}

	open fun getClassFilename(clazz: AstClass, type: MemberTypes) = getClassBaseFilename(clazz, type) + classFileExtension

	val indenterPerClass = hashMapOf<AstClass, Indenter>()

	open fun genSingleFileClasses(output: SyncVfsFile): Indenter = Indenter {
		imports.clear()
		val concatFilesTrans = copyFiles(output)

		line(concatFilesTrans.prepend)
		line(genSingleFileClassesWithoutAppends(output))
		line(concatFilesTrans.append)
	}

	open fun genSingleFileClassesWithoutAppends(output: SyncVfsFile): Indenter = Indenter {
		for (clazz in sortedClasses) {
			val indenters = if (clazz.implCode != null) listOf(Indenter(clazz.implCode!!)) else genClass(clazz).map { it.indenter }
			for (indenter in indenters) {
				indenterPerClass[clazz] = indenter
				line(indenter)
			}
		}
	}

	data class SubClass(val clazz: AstClass, val type: MemberTypes)

	data class ClassResult(val subclass: SubClass, val indenter: Indenter)

	open fun genClass(clazz: AstClass): List<ClassResult> {
		setCurrentClass(clazz)

		val out = arrayListOf<ClassResult>()

		if (interfacesSupportStaticMembers || !clazz.isInterface) {
			out += genClassPart(clazz, MemberTypes.ALL)
		} else {
			out += genClassPart(clazz, MemberTypes.INSTANCE)
			out += genClassPart(clazz, MemberTypes.STATIC)
		}

		return out
	}

	open fun genClassPart(clazz: AstClass, type: MemberTypes): ClassResult {
		imports.clear()
		return ClassResult(
			SubClass(clazz, type),
			//when (type) {
			//	MemberTypes.ALL, MemberTypes.INSTANCE -> FqName(clazz.name.targetName)
			//	MemberTypes.STATIC -> FqName(clazz.name.targetNameForStatic)
			//},
			Indenter {
				val nativeName = clazz.nativeName
				if (nativeName != null) line(singleLineComment("Native $nativeName"))
				line(genClassDecl(clazz, type)) {
					line(genClassBody(clazz, type))
				}
			}
		)
	}

	open fun singleLineComment(text: String) = Indenter("// $text")

	enum class MemberTypes(val isStatic: Boolean) {
		ALL(false), INSTANCE(false), STATIC(true);

		fun check(member: AstMember) = when (this) {
			ALL -> true
			INSTANCE -> !member.isStatic
			STATIC -> member.isStatic
		}
	}

	open fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		val CLASS = if (clazz.isInterface) "interface" else "class"
		val iabstract = if (clazz.isAbstract) "abstract " else ""
		var decl = "$iabstract$CLASS ${clazz.name.targetSimpleName}"
		decl += genClassDeclExtendsImplements(clazz, kind)
		return decl
	}

	protected open fun genClassDeclExtendsImplements(clazz: AstClass, kind: MemberTypes): String {
		var decl = ""
		decl += genClassDeclExtends(clazz, kind)
		decl += genClassDeclImplements(clazz, kind)
		return decl
	}

	protected open fun genClassDeclExtends(clazz: AstClass, kind: MemberTypes): String {
		return if (clazz.extending != null) " extends ${clazz.extending.targetClassFqName}" else ""
	}

	protected open fun genClassDeclImplements(clazz: AstClass, kind: MemberTypes): String {
		val implementing = getClassInterfaces(clazz).distinct()
		val IMPLEMENTS = if (clazz.isInterface) "extends" else "implements"
		return if (implementing.isNotEmpty()) " $IMPLEMENTS ${implementing.map { it.targetClassFqName }.joinToString(", ")}" else ""
	}

	protected open fun getClassInterfaces(clazz: AstClass): List<FqName> = clazz.implementing

	open fun genClassBody(clazz: AstClass, kind: MemberTypes): Indenter = Indenter {
		val members = clazz.annotationsList.getTypedList(JTranscAddMembersList::value).filter { it.target == targetName.name }.flatMap { it.value.toList() }.joinToString("\n")
		line(gen(members, process = true))
		line(genClassBodyFields(clazz, kind))
		line(genClassBodyMethods(clazz, kind))
		if (kind != MemberTypes.INSTANCE) {
			line(genSIMethod(clazz))
		}
	}

	open fun genClassBodyFields(clazz: AstClass, kind: MemberTypes): Indenter = Indenter {
		for (f in clazz.fields) if (kind.check(f)) line(genField(f))
	}

	open fun genClassBodyMethods(clazz: AstClass, kind: MemberTypes): Indenter = Indenter {
		val methodsWithoutClassToIgnore = if (clazz.isInterface && !allowRepeatMethodsInInterfaceChain) {
			clazz.allInterfacesInAncestors.flatMap { it.methodsWithoutConstructors }.distinct().map { it.ref.withoutClass }.toSet()
		} else {
			setOf()
		}

		val nativeClass = clazz.isNative
		for (m in clazz.methods) {
			if (!kind.check(m)) continue
			if (m.ref.withoutClass in methodsWithoutClassToIgnore) continue
			//if (nativeClass && !m.annotationsList.nonNativeCall) continue

			val mustPutBody = !clazz.isInterface || m.isStatic
			line(genMethod(clazz, m, mustPutBody))
		}
	}

	open fun genSIMethod(clazz: AstClass): Indenter = Indenter {
		line(genSIMethodBody(clazz))
	}

	open fun genSIMethodBody(clazz: AstClass): Indenter = Indenter {
		//for (field in clazz.fields.filter { it.isStatic }) {
		//	line("${field.buildField(static = true)};")
		//}
		if (clazz.staticConstructor != null) {
			line("${buildMethod(clazz.staticConstructor!!, static = true)}();")
		}
	}

	open fun genField(field: AstField): Indenter = Indenter {
		val istatic = if (field.isStatic) "static " else ""
		line("$istatic${field.type.targetName} ${field.targetName} = ${field.escapedConstantValueField};")
	}

	open fun genMetodDecl(method: AstMethod): String {
		val args = method.methodType.args.map { it.decl }

		//if (method.isInstanceInit) mods += "final "

		val mods = genMethodDeclModifiers(method)
		return "$mods ${method.actualRetType.targetName} ${method.targetName}(${args.joinToString(", ")})"
	}

	open fun genMethodDeclModifiers(method: AstMethod): String {
		var mods = ""
		if (!method.isStatic && method.targetIsOverriding) mods += "override "
		if (method.isStatic) mods += "static "
		return mods
	}

	open val AstMethod.actualRetType: AstType get() = if (this.isInstanceInit) this.containingClass.astType else this.methodType.ret

	open fun genMethod(clazz: AstClass, method: AstMethod, mustPutBody: Boolean): Indenter = Indenter {
		currentMethod = method.ref
		context.method = method

		val decl = genMetodDecl(method)
		if (mustPutBody) {
			line(decl) {
				val actualMethod: AstMethod = if (method.bodyRef != null) {
					// Default methods
					method.bodyRef.resolve(program)
				} else {
					method
				}

				val native = actualMethod.nativeBodies
				val defaultNativeBody = native[""]
				if (defaultNativeBody != null) {
					line(defaultNativeBody)
				} else if (actualMethod.body != null) {
					line(genBody2WithFeatures(actualMethod, actualMethod.body!!))
					if (actualMethod.isInstanceInit) line("return " + genExprThis(AstExpr.THIS(clazz.name)) + ";")
				} else {
					line(genMissingBody(actualMethod))
				}
			}
		} else {
			line("$decl;")
		}
	}

	open fun genMissingBody(method: AstMethod): Indenter = Indenter {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw ${quoteString(message)};")
	}

	open val AstMethod.targetIsOverriding: Boolean get() = this.isOverriding && !this.isInstanceInit && !this.isStatic

	var entryPointClass = FqName("EntryPointClass")
	var entryPointFilePath = "EntryPointFile"

	val JAVA_LANG_OBJECT_CLASS by lazy { program["java.lang.Object".fqname]!! }
	val JAVA_LANG_OBJECT_REF by lazy { AstType.REF("java.lang.Object") }
	val JAVA_LANG_OBJECT by lazy { nativeName<java.lang.Object>() }
	val JAVA_LANG_CLASS by lazy { nativeName<java.lang.Class<*>>() }
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
		params["CLASS"] = clazz.fqname
	}

	protected fun setCurrentMethod(method: AstMethod) {
		context.method = method
		currentMethod = method.ref
	}

	open fun genStm2(stm: AstStm, last: Boolean = false): Indenter {
		this.stm = stm
		return when (stm) {
			is AstStm.STM_LABEL -> genStmLabel(stm)
			is AstStm.GOTO -> genStmGoto(stm, last)
			is AstStm.IF_GOTO -> genStmIfGoto(stm)
			is AstStm.SWITCH_GOTO -> genStmSwitchGoto(stm)
			is AstStm.NOP -> genStmNop(stm)
			is AstStm.WHILE -> genStmWhile(stm)
			is AstStm.IF -> genStmIf(stm)
			is AstStm.IF_ELSE -> genStmIfElse(stm)
			is AstStm.RETURN_VOID -> genStmReturnVoid(stm, last)
			is AstStm.RETURN -> genStmReturnValue(stm, last)
			is AstStm.STM_EXPR -> genStmExpr(stm)
			is AstStm.STMS -> genStmStms(stm)
			is AstStm.THROW -> genStmThrow(stm, last)
			is AstStm.RETHROW -> genStmRethrow(stm, last)
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
		is AstExpr.CHECK_CAST -> genExprCheckCast(e)
		is AstExpr.PARAM -> genExprParam(e)
		is AstExpr.LOCAL -> genExprLocal(e)
		is AstExpr.TYPED_LOCAL -> genExprTypedLocal(e)
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
		is AstExpr.OBJECTARRAY_LITERAL -> genExprObjectArrayLit(e)
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
		//val nonNativeCall = if (isNativeCall) refMethod.annotationsList.nonNativeCall else false
		val nonNativeCall = false

		fun processArg(arg: AstExpr.Box, targetType: AstType) = processCallArg(arg.value, if (isNativeCall) convertToTarget(arg) else arg.genExpr(), targetType)
		fun processArg(arg: AstExpr.Box) = processArg(arg, arg.type)

		val callsiteBody = refMethod.annotationsList.getCallSiteBodyForTarget(targetName)?.template("JTranscCallSiteBody")

		fun unbox(arg: AstExpr.Box): String {
			val processed = processArg(arg)
			val invoke = PeepholeMatcher.matchOptionalCastAndStaticInvoke(arg.value)
			if (invoke != null) {
				val ret = when (invoke.method.fid) {
					"java.lang.Integer:valueOf:(I)Ljava/lang/Integer;",
					"java.lang.Long:valueOf:(J)Ljava/lang/Long;",
					"java.lang.Double:valueOf:(D)Ljava/lang/Double;",
					"java.lang.Float:valueOf:(F)Ljava/lang/Float;"
					-> {
						processArg(invoke.args[0])
					}
					else -> null
				}
				//println(":::" + invoke.method.fid + " -> $ret")
				if (ret != null) return ret
			}
			//println("Unbox: ${arg.value}")
			//println("Unbox: ${invoke}")
			//println("Unbox: $processed")
			return N_unboxRaw(processed)
		}

		fun processArg2(param: AstArgumentCallWithAnnotations): String {
			val arg = param.exprBox
			if (param.annotationList.contains<JTranscUnboxParam>()) {
				val lit = (arg.value as? AstExpr.LITERAL)
				return if (lit != null) {
					when (lit.value) {
						is String -> quoteString(lit.value)
						else -> unbox(arg)
					}
				} else {
					unbox(arg)
				}
			}
			return processArg(param.exprBox, param.arg.type)
		}

		val pparams = refMethod.getParamsWithAnnotationsBox(args)

		if (callsiteBody != null) {
			val args2Unquoted = arrayOfNulls<String>(pparams.size)

			val args2 = pparams.map { arginfo ->
				val index = arginfo.arg.index
				val arg = arginfo.exprBox
				val paramAnnotations = refMethod.parameterAnnotationsList[index]
				if (paramAnnotations.contains<JTranscLiteralParam>()) {
					val lit = (arg.value as? AstExpr.LITERAL) ?: invalidOp("Used @JTranscLiteralParam without a literal: ${processArg(arg)} in $context")
					lit.value.toString().template("JTranscLiteralParam")
				} else if (paramAnnotations.contains<JTranscUnboxParam>()) {
					val lit = (arg.value as? AstExpr.LITERAL)
					if (lit != null) {
						when (lit.value) {
							is String -> {
								args2Unquoted[index] = lit.value
								quoteString(lit.value)
							}
						//is String -> lit.value
							else -> unbox(arg)
						}
					} else {
						unbox(arg)
					}
				} else {
					processArg(arg)
				}
			}

			val objStr = when (e2) {
				is AstExpr.CALL_INSTANCE -> e2.obj.genNotNull()
				else -> ""
			}

			val out = Regex("#([',.:])?((@|\\d)+)").replace(callsiteBody) { mr ->
				val mustQuote = mr.groupValues[1]
				val rid = mr.groupValues[2]
				val id = rid.toIntOrNull2() ?: 0
				val res = if (rid == "@") objStr else args2[id]
				val res2 = if (rid != "@") args2Unquoted[id] ?: args2[id] else objStr
				when (mustQuote) {
					"'", "," -> quoteString(res2)
					".", ":" -> {
						access(res2, static = false, field = (mustQuote == "."))
					}
					else -> res
				}
			}
			return out
		} else {
			val processedArgs = pparams.map { processArg2(it) }
			val methodAccess = getTargetMethodAccess(refMethod, static = isStaticCall)
			val result = when (e2) {
				is AstExpr.CALL_STATIC -> genExprCallBaseStatic(e2, clazz, refMethodClass, method, methodAccess, processedArgs, nonNativeCall)
				is AstExpr.CALL_SUPER -> {
					genExprCallBaseSuper(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
				}
				is AstExpr.CALL_INSTANCE -> genExprCallBaseInstance(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
				else -> invalidOp("Unexpected")
			}
			return if (isNativeCall) convertToJava(refMethod.methodType.ret, result) else result
		}
	}

	open fun quoteString(str: String) = str.quote()

	open fun processCallArg(e: AstExpr, str: String, targetType: AstType): String = str

	open fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		return "super$methodAccess(${args.joinToString(", ")})"
	}

	fun genExprCallBaseStatic(e2: AstExpr.CALL_STATIC, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>, nonNativeCall: Boolean): String {
		if (nonNativeCall) {
		}
		return "${clazz.targetName}$methodAccess(${args.joinToString(", ")})"
	}

	open fun genExprCallBaseInstance(e2: AstExpr.CALL_INSTANCE, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		//if (method.isInstanceInit) {
		//	return "${e2.obj.value.withoutCasts().genNotNull()}$methodAccess(${args.joinToString(", ")})"
		//} else {
		return "${e2.obj.genNotNull()}$methodAccess(${args.joinToString(", ")})"
		//}
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
	fun AstStm.genStm(last: Boolean = false): Indenter = genStm2(this, last)
	fun AstStm.Box.genStm(last: Boolean = false): Indenter = genStm2(this.value, last)
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

	data class ConcatFile(val prepend: String?, val append: String?)
	class CopyFile(val content: ByteArray, val dst: String, val isAsset: Boolean)

	class PrependAppend(val prepend: String, val append: String)

	open fun copyFilesExtra(output: SyncVfsFile) {

	}

	fun copyFiles(output: SyncVfsFile): PrependAppend {
		val resourcesVfs = program.resourcesVfs
		val copyFiles = getFilesToCopy(targetName.name)

		copyFilesExtra(output)

		//println(copyFiles)
		val copyFilesTrans = copyFiles.filter { it.src.isNotEmpty() && it.dst.isNotEmpty() }.map {
			val file = resourcesVfs[it.src]
			if (it.process) {
				CopyFile(file.readString().template("copyfile").toByteArray(), it.dst, it.isAsset)
			} else {
				CopyFile(file.read(), it.dst, it.isAsset)
			}
		}

		// Copy assets
		folders.copyAssetsTo(output)
		for (file in copyFilesTrans) {
			output[file.dst].ensureParentDir().write(file.content)
		}

		params["assetFiles"] = (params["assetFiles"] as List<SyncVfsFile>) + copyFilesTrans.map { output[it.dst] }

		val concatFilesTrans = copyFiles.filter { it.append.isNotEmpty() || it.prepend.isNotEmpty() || it.prependAppend.isNotEmpty() }.map {
			val prependAppend = if (it.prependAppend.isNotEmpty()) (resourcesVfs[it.prependAppend].readString() + "\n") else null
			val prependAppendParts = prependAppend?.split("/* ## BODY ## */")

			val prepend = if (prependAppendParts != null && prependAppendParts.size >= 2) prependAppendParts[0] else if (it.prepend.isNotEmpty()) (resourcesVfs[it.prepend].readString() + "\n") else null
			val append = if (prependAppendParts != null && prependAppendParts.size >= 2) prependAppendParts[1] else if (it.append.isNotEmpty()) (resourcesVfs[it.append].readString() + "\n") else null

			fun process(str: String?): String? = if (it.process) str?.template("includeFile") else str

			ConcatFile(process(prepend), process(append))
		}

		return PrependAppend(
			concatFilesTrans.map { it.prepend }.filterNotNull().joinToString("\n"),
			concatFilesTrans.map { it.append }.filterNotNull().reversed().joinToString("\n")
		)
	}

//fun locateDefaultMethod(method: List<AstClass>) { sad ad ad as
//
//}

	open fun AstExpr.genNotNull(): String = genExpr2(this)

	fun AstBody.genBody(): Indenter = genBody2(this)
	fun AstBody.genBodyWithFeatures(method: AstMethod): Indenter = genBody2WithFeatures(method, this)

	open fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter {
		val actualFeatures = if (body.traps.isNotEmpty()) methodFeaturesWithTraps else methodFeatures
		val transformedBody = features.apply(method, body, actualFeatures, settings, types)
		plugins.onAfterAppliedMethodBodyFeature(method, transformedBody)
		return transformedBody.genBody()
	}

	// @TODO: Remove this from here, so new targets don't have to do this too!
// @TODO: AstFieldRef should be fine already, so fix it in asm_ast!
	fun fixField(field: AstFieldRef): AstFieldRef = program[field].ref

	fun fixMethod(method: AstMethodRef): AstMethodRef = program[method]?.ref
		?: invalidOp("Can't find method $method while generating $context")

	val allAnnotationTypes = program.allAnnotations.flatMap { it.getAllDescendantAnnotations() }.filter { it.runtimeVisible }.map { it.type }.distinct().map { program[it.name] }.toSet()

	val trapsByStart = hashMapOf<AstLabel, ArrayList<AstTrap>>()
	val trapsByEnd = hashMapOf<AstLabel, ArrayList<AstTrap>>()

	open fun genBody2(body: AstBody): Indenter {
		val method = context.method
		this.mutableBody = MutableBody(method)

		trapsByStart.clear()
		trapsByEnd.clear()

		for (trap in body.traps) {
			trapsByStart.getOrPut(trap.start) { arrayListOf() } += trap
			trapsByEnd.getOrPut(trap.end) { arrayListOf() } += trap
		}

		return Indenter {
			@Suppress("LoopToCallChain", "Destructure")
			for (local in body.locals) refs.add(local.type)

			resetLocalsPrefix()
			var info: Indenter? = null

			linedeferred { line(info!!) }
			line(genBodyLocals(body.locals))
			if (body.traps.isNotEmpty()) line(genBodyTrapsPrefix())

			val bodyContent = body.stm.genStm(last = true)
			info = genLocalsPrefix()

			if (method.isClassOrInstanceInit) mutableBody.initClassRef(context.clazz.ref, "self")

			for ((clazzRef, reasons) in mutableBody.referencedClasses) {
				if (!program[clazzRef.name].mustGenerate) continue
				if (!method.isClassOrInstanceInit && (context.clazz.ref == clazzRef)) continue // Calling internal methods (which should be initialized already!)
				line(genBodyStaticInitPrefix(clazzRef, reasons))
			}
			line(bodyContent)
		}
	}

	val AstClass.nativeNameInfo: JTranscNativeName? get() = this.nativeNameForTarget(this@CommonGenerator.targetName)
	val AstClass.nativeName: String? get() = this.nativeNameInfo?.value

	open fun genStmSetNewWithConstructor(stm: AstStm.SET_NEW_WITH_CONSTRUCTOR): Indenter = indent {
		val newClazz = program[stm.target.name]
		refs.add(stm.target)
		val commaArgs = stm.args.map { it.genExpr() }.joinToString(", ")
		val className = stm.target.targetName
		val targetLocalName = stm.local.targetName

		if (newClazz.nativeName != null) {
			imports += FqName(newClazz.nativeName!!)
			line("$targetLocalName = new $className($commaArgs);")
		} else {
			line("$targetLocalName = new $className();")
			line("$targetLocalName.${stm.method.targetName}($commaArgs);")
		}
	}

	open fun resetLocalsPrefix() = Unit
	open fun genLocalsPrefix(): Indenter = indent { }
	open fun genBodyLocals(locals: List<AstLocal>): Indenter = indent { for (local in locals) line(local.decl) }

	open fun genBodyTrapsPrefix() = Indenter(AstLocal(0, "J__exception__", AstType.THROWABLE).decl)
	open fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "J__exception__"

	open val AstType.localDeclType: String get() = this.targetName

	open val AstLocal.decl: String get() = "${this.type.localDeclType} ${this.targetName} = ${this.type.nativeDefaultString};"
	open val AstArgument.decl: String get() = "${this.type.localDeclType} ${this.targetName}"

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
		line("$left = $right;")
	}

	fun genStmSwitchGoto(stm: AstStm.SWITCH_GOTO): Indenter = indent {
		flowBlock(FlowKind.SWITCH) {
			line("switch (${stm.subject.genExpr()})") {
				for ((values, label) in stm.cases) {
					line("${buildMultipleCase(values)} ${genGoto(label, false)}")
				}
				line("default: ${genGoto(stm.default, false)}")
			}
		}
	}

	open fun buildMultipleCase(cases: List<Int>): String = if (casesWithCommas) {
		"case " + cases.joinToString(",") + ":"
	} else {
		cases.map { "case $it:" }.joinToString(" ")
	}

	open fun genStmIfGoto(stm: AstStm.IF_GOTO): Indenter = Indenter("if (${stm.cond.genExpr()}) " + genGoto(stm.label, false))
	open fun genStmGoto(stm: AstStm.GOTO, last: Boolean): Indenter = Indenter(genGoto(stm.label, last))

	open fun genGoto(label: AstLabel, last: Boolean) = "goto ${label.name};"

	open fun genStmSetFieldInstance(stm: AstStm.SET_FIELD_INSTANCE): Indenter = indent {
		val left = buildInstanceField(stm.left.genExpr(), fixField(stm.field))
		val right = stm.expr.genExpr()
		if (allowAssignItself || left != right) {
			// Avoid: Assigning a value to itself
			line(actualSetField(stm, left, right))
		}
	}

	open fun actualSetField(stm: AstStm.SET_FIELD_INSTANCE, left: String, right: String): String = "$left = $right;"

	open fun genStmContinue(stm: AstStm.CONTINUE) = Indenter("continue;")
	open fun genStmBreak(stm: AstStm.BREAK) = Indenter("break;")
	open fun genStmLabel(stm: AstStm.STM_LABEL): Indenter = Indenter {
		if (stm.label in trapsByEnd) {
			for (trap in trapsByEnd[stm.label]!!) line(genStmRawCatch(trap))
		}
		line(genStmLabelCore(stm))
		if (stm.label in trapsByStart) {
			for (trap in trapsByStart[stm.label]!!) line(genStmRawTry(trap))
		}
	}

	open fun genLabel(label: AstLabel) = "${label.name}:;"

	fun genStmLabelCore(stm: AstStm.STM_LABEL) = genLabel(stm.label)

	open fun genStmRawTry(trap: AstTrap): Indenter = Indenter {
	}

	open fun genStmRawCatch(trap: AstTrap): Indenter = Indenter {
	}

	enum class FlowKind { SWITCH, WHILE }

	val flowBlocks = ArrayList<FlowKind>()

	inline fun <T> flowBlock(kind: FlowKind, callback: () -> T): T {
		try {
			flowBlocks += kind
			return callback()
		} finally {
			flowBlocks.removeAt(flowBlocks.size - 1)
		}
	}

	open fun genStmSwitch(stm: AstStm.SWITCH): Indenter = indent {
		flowBlock(FlowKind.SWITCH) {
			if (stm.cases.isNotEmpty() || !stm.default.value.isEmpty()) {
				line("switch (${stm.subject.genExpr()})") {
					for (case in stm.cases) {
						val values = case.first
						val caseStm = case.second
						if (caseStm.value.isSingleStm()) {
							val append = if (!defaultGenStmSwitchHasBreaks || caseStm.value.lastStm().isBreakingFlow()) "" else "break;"
							line(buildMultipleCase(values) + caseStm.genStm().toString().trim() + " " + append)
						} else {
							line(buildMultipleCase(values))
							indent {
								line(caseStm.genStm())
								if (defaultGenStmSwitchHasBreaks && !caseStm.value.lastStm().isBreakingFlow()) line("break;")
							}
						}
					}
					if (languageRequiresDefaultInSwitch || !stm.default.value.isEmpty()) {
						line("default:")
						indent {
							line(stm.default.genStm())
							if (defaultGenStmSwitchHasBreaks && !stm.default.value.lastStm().isBreakingFlow()) line("break;")
						}
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

	open val ARRAY_SUPPORT_SHORTCUTS = true
	open val ARRAY_OPEN_SYMBOL = "["
	open val ARRAY_CLOSE_SYMBOL = "]"

	open fun genExprObjectArrayLit(e: AstExpr.OBJECTARRAY_LITERAL): String {
		val count = e.values.size
		return when {
			ARRAY_SUPPORT_SHORTCUTS && (count in 0..4) -> "JA_L${staticAccessOperator}T$count(" + e.kind.mangle().quote() + ", " + e.values.map { genExpr2(it) }.joinToString(", ") + ")"
			else -> "JA_L${staticAccessOperator}fromArray(" + pquote(e.kind.mangle()) + ", $ARRAY_OPEN_SYMBOL" + e.values.map { genExpr2(it) }.joinToString(",") + "$ARRAY_CLOSE_SYMBOL)"
		}
	}

	open fun pquote(str: String) = str.quote()

	open fun createArraySingle(e: AstExpr.NEW_ARRAY, desc: String): String {
		return if (e.type.elementType !is AstType.Primitive) {
			"new $ObjectArrayType(${e.counts[0].genExpr()}, " + quoteString(desc) + ")"
		} else {
			"new ${e.type.targetName}(${e.counts[0].genExpr()})"
		}
	}

	open fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure([${e.counts.map { it.genExpr() }.joinToString(", ")}], \"$desc\")"
	}

	open fun genExprNew(e: AstExpr.NEW): String {
		refs.add(e.target)
		val className = e.target.targetName
		return "(new $className())"
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
		return e.array.genNotNull().instanceAccessField("length")
	}

	fun AstType.resolve(): AstType = when (this) {
		is AstType.COMMON -> this.resolve(program)
		is AstType.MUTABLE -> this.ref.resolve()
		else -> this
	}

	private fun SHIFT_FIX_32(r: Int): Int {
		if (r < 0) {
			return (32 - ((-r) and 0x1F)) and 0x1F;
		} else {
			return r and 0x1F;
		}
	}

	private fun SHIFT_FIX_64(r: Int): Int {
		if (r < 0) {
			return (64 - ((-r) and 0x3F)) and 0x3F;
		} else {
			return r and 0x3F;
		}
	}

	open fun genExprBinop(e: AstExpr.BINOP): String {
		val resultType = e.type.resolve()
		val leftType = e.left.type.resolve()
		val rightType = e.right.type.resolve()
		val lv = e.left.value
		val rv = e.right.value
		val l = lv.genExpr()
		val r = rv.genExpr()
		val op = e.op

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
				AstBinop.SHL -> {
					val rv2 = rv.withoutCasts()
					if (rv2 is AstExpr.LITERAL) {
						N_lshl_cst(l, SHIFT_FIX_64(rv2.valueAsInt))
					} else {
						N_lshl(l, r)
					}
				}
				AstBinop.SHR -> {
					val rv2 = rv.withoutCasts()
					if (rv2 is AstExpr.LITERAL) {
						N_lshr_cst(l, SHIFT_FIX_64(rv2.valueAsInt))
					} else {
						N_lshr(l, r)
					}
				}
				AstBinop.USHR -> {
					val rv2 = rv.withoutCasts()
					if (rv2 is AstExpr.LITERAL) {
						N_lushr_cst(l, SHIFT_FIX_64(rv2.valueAsInt))
					} else {
						N_lushr(l, r)
					}
				}
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
				AstBinop.SHL -> {
					val rv2 = rv.withoutCasts()
					if (rv2 is AstExpr.LITERAL) {
						N_ishl_cst(l, SHIFT_FIX_32(rv2.valueAsInt))
					} else {
						N_ishl(l, r)
					}
				}
				AstBinop.SHR -> {
					val rv2 = rv.withoutCasts()
					if (rv2 is AstExpr.LITERAL) {
						N_ishr_cst(l, SHIFT_FIX_32(rv2.valueAsInt))
					} else {
						N_ishr(l, r)
					}
				}
				AstBinop.USHR -> {
					val rv2 = rv.withoutCasts()
					if (rv2 is AstExpr.LITERAL) {
						N_iushr_cst(l, SHIFT_FIX_32(rv2.valueAsInt))
					} else {
						N_iushr(l, r)
					}
				}
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
			AstType.LONG -> N_j2j(result)
			AstType.FLOAT -> N_f2f(result)
			AstType.DOUBLE -> N_d2d(result)
			else -> invalid()
		}
	}

	open fun genExprArrayAccess(e: AstExpr.ARRAY_ACCESS): String = N_AGET_T(e.array.type.resolve(program).asArray(), e.array.type.elementType, e.array.genNotNull(), e.index.genExpr())

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

	inline protected fun indent(init: Indenter.() -> Unit): Indenter = Indenter(init)

	open fun genStmSetArray(stm: AstStm.SET_ARRAY): Indenter {
		val array = stm.array.genNotNull()
		//if (array == "((JA_B)(((java_lang_Object)(p1))))") println(array)
		val res = N_ASET_T(
			stm.array.type.resolve(program).asArray(),
			stm.array.type.elementType,
			array,
			stm.index.genExpr(),
			stm.expr.genExpr()
		)

		return Indenter(res)
	}

	open fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter {
		var n = 0
		for (v in stm.values) {
			line(genStmSetArray(AstStm.SET_ARRAY(stm.array.value, (stm.startIndex + n).lit, v.value)))
			n++
		}
	}

	open fun genExprParam(e: AstExpr.PARAM) = e.argument.targetName
	fun genExprLocal(e: AstLocal) = if (localVarPrefix.isEmpty()) e.targetName else localVarPrefix + e.targetName
	fun genExprLocal(e: AstExpr.LOCAL) = if (localVarPrefix.isEmpty()) e.local.targetName else localVarPrefix + e.local.targetName
	fun genExprTypedLocal(e: AstExpr.TYPED_LOCAL) = genExprCast(genExprLocal(e.local), e.local.type, e.type)

	fun genStmLine(stm: AstStm.LINE) = indent {
		mark(stm)
		if (GENERATE_LINE_NUMBERS) line("// ${stm.line}")
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

	open fun getMonitorLockedObjectExpr(method: AstMethod):AstExpr{
		if(method.isStatic)
			return (AstExpr.LITERAL(method.containingClass.astType, dummy = true))
		else
			return (AstExpr.THIS(method.containingClass.name))
	}

	open fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = indent { line("// MONITOR_ENTER") }
	open fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = indent { line("// MONITOR_EXIT") }
	open fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw ${stm.exception.genExpr()};")
	open fun genStmRethrow(stm: AstStm.RETHROW, last: Boolean) = Indenter("""throw J__i__exception__;""")
	open fun genStmStms(stm: AstStm.STMS) = indent {
		val stms = stm.stms
		for (i in stms.indices) {
			val last = i == stms.lastIndex
			line(stms[i].genStm(last))
		}
	}

	open fun genStmExpr(stm: AstStm.STM_EXPR) = Indenter("${stm.expr.genExpr()};")
	open fun genStmReturnVoid(stm: AstStm.RETURN_VOID, last: Boolean) = Indenter(if (context.method.methodVoidReturnThis) "return " + genExprThis(AstExpr.THIS("Dummy".fqname)) + ";" else "return;")
	open fun genStmReturnValue(stm: AstStm.RETURN, last: Boolean) = Indenter("return ${stm.retval.genExpr()};")
	fun genStmWhile(stm: AstStm.WHILE) = indent {
		flowBlock(FlowKind.WHILE) {
			line("while (${stm.cond.genExpr()})") {
				line(stm.iter.genStm())
			}
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
		val localName = stm.local.targetName
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
			null -> null.escapedConstant
			is AstType -> value.escapedConstant
			is String -> value.escapedConstant
			is Boolean -> value.escapedConstant
			is Byte -> value.escapedConstant
			is Char -> value.escapedConstant
			is Short -> value.escapedConstant
			is Int -> value.escapedConstant
			is Long -> value.escapedConstant
			is Float -> value.escapedConstant
			is Double -> value.escapedConstant
			else -> invalidOp("Unsupported value $value")
		}
	}

	open fun genExprLiteralRefName(e: AstExpr.LITERAL_REFNAME): String {
		val value = e.value
		return when (value) {
			is AstType.REF -> value.targetName
			is AstMethodRef -> value.targetName
			is AstFieldRef -> value.targetName
			else -> invalidOp("Unknown AstExpr.LITERAL_REFNAME value type : ${value?.javaClass} : $value")
		}.escapedConstant
	}

	class MutableBody(val method: AstMethod) {
		val referencedClasses = hashMapOf<AstType.REF, ArrayList<String>>()
		fun initClassRef(classRef: AstType.REF, reason: String) {
			referencedClasses.putIfAbsentJre7(classRef, arrayListOf())
			referencedClasses[classRef]!! += reason
		}
	}

	open fun genExprCast(e: AstExpr.CAST): String = genExprCast(e.subject.genExpr(), e.from, e.to)
	open fun genExprCheckCast(e: AstExpr.CHECK_CAST): String = genExprCastChecked(e.subject.genExpr(), e.from.asReference(), e.to.asReference())

	open fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String = N_c(e, from, to)

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
					is AstType.BOOL -> N_i2z(N_j2i(e))
					is AstType.BYTE -> N_i2b(N_j2i(e))
					is AstType.CHAR -> N_i2c(N_j2i(e))
					is AstType.SHORT -> N_i2s(N_j2i(e))
					is AstType.INT -> N_j2i(e)
					is AstType.LONG -> N_j2j(e)
					is AstType.FLOAT -> N_j2f(e)
					is AstType.DOUBLE -> N_j2d(e)
					else -> unhandled()
				}
			}
			is AstType.REF, is AstType.ARRAY, is AstType.GENERIC -> {
				when (to) {
					FUNCTION_REF -> N_getFunction(e)
					else -> {

						N_c(e, from, to)
					}
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
//else -> N_unboxRaw(e)
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

	open fun N_func(name: String, args: String) = "N$staticAccessOperator$name($args)"

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
	open protected fun N_unboxRaw(e: String) = N_func("unbox", "$e")

	open protected fun N_is(a: String, b: AstType.Reference) = N_is(a, b.targetName)
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
	open protected fun N_j2i(str: String) = N_func("j2i", "$str")
	open protected fun N_j2j(str: String) = "($str)"
	open protected fun N_j2f(str: String) = N_func("j2f", "$str")
	open protected fun N_j2d(str: String) = N_func("j2d", "$str")
	open protected fun N_getFunction(str: String) = N_func("getFunction", "$str")
	open protected fun N_c(str: String, from: AstType, to: AstType) = "($str)"
	open protected fun N_ineg(str: String) = "-($str)"
	open protected fun N_fneg(str: String) = "-($str)"
	open protected fun N_dneg(str: String) = "-($str)"
	open protected fun N_iinv(str: String) = "~($str)"
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

	open protected fun N_inew(value: Int) = "$value"
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

	open protected fun N_ishl_cst(l: String, r: Int) = N_ishl(l, "$r")
	open protected fun N_ishr_cst(l: String, r: Int) = N_ishr(l, "$r")
	open protected fun N_iushr_cst(l: String, r: Int) = N_iushr(l, "$r")

	open protected fun N_lnew(value: Long) = N_func("lnew", "${value.high}, ${value.low}")

	open protected fun N_lneg(str: String) = N_func("lneg", "$str")
	open protected fun N_linv(str: String) = N_func("linv", "$str")

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

	open protected fun N_lshl_cst(l: String, r: Int) = N_lshl(l, "$r")
	open protected fun N_lshr_cst(l: String, r: Int) = N_lshr(l, "$r")
	open protected fun N_lushr_cst(l: String, r: Int) = N_lushr(l, "$r")

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

		fun clear() {
			_usedDependencies.clear()
		}

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

	val AstMethod.nativeBodies: Map<String, Indenter> get() = getNativeBodies(target = this@CommonGenerator.targetName.name)

	fun AstMethod.getNativeBodies(target: String): Map<String, Indenter> {
		val bodies = this.annotationsList.getBodiesForTarget(TargetName(target))

		return bodies.associate { body ->
			body.cond to Indenter {
				for (line in body.lines) line(line.template("nativeBody"))
			}
		}
	}

///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////

	@Suppress("ConvertLambdaToReference")
	val params by lazy {
		val result = (
			mapOf(
				"CLASS" to "",
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
				"fullscreen" to settings.fullscreen,
				"resizable" to settings.resizable,
				"borderless" to settings.borderless,
				"vsync" to settings.vsync,
				"orientation" to settings.orientation.lowName,
				"assetFiles" to MergeVfs(settings.assets.map { LocalVfs(it) }).listdirRecursive().filter { it.isFile }.map { it.file },
				"embedResources" to settings.embedResources,
				"assets" to settings.assets,
				"hasIcon" to !settings.icon.isNullOrEmpty(),
				"icon" to settings.icon,
				"libraries" to settings.libraries,
				"extra" to settings.extra,
				"folders" to folders,
				"TARGET_IMPORTS" to targetImports,
				"TARGET_INCLUDES" to targetIncludes,
				"TARGET_LIBRARIES" to targetLibraries,
				"TARGET_DEFINES" to targetDefines,
				"JTRANSC_VERSION" to JTranscVersion.getVersion(),
				"JTRANSC_OS" to JTranscSystem.getOS()
			) + extraVars
			)
			.toHashMap()
		log.info("TEMPLATE VARS:")
		log.info(Json.encode(result))
		log.info("extraVars:")
		log.info(Json.encode(extraVars))
		result
	}


	open fun setTemplateParamsAfterBuildingSource() {
		params["entryPointFile"] = entryPointFilePath
		params["entryPointClass"] = entryPointClass.targetName
	}

	fun setExtraData(map: Map<String, Any?>) {
		for ((key, value) in map) this.params[key] = value
	}

	private fun getOrReplaceVar(name: String): String = if (name.startsWith("#")) params[name.substring(1)].toString() else name

	private fun evalReference(type: String, desc: String): String {
		val ref = CommonTagHandler.getRef(program, type, desc, params)
		return when (ref) {
			is CommonTagHandler.SINIT -> buildStaticInit(ref.method.containingClass.name) ?: ""
			is CommonTagHandler.CONSTRUCTOR -> buildConstructor(ref.method)
			is CommonTagHandler.METHOD -> buildMethod(ref.method, static = ref.isStatic, includeDot = ref.includeDot)
			is CommonTagHandler.FIELD -> ref.field.buildField(static = ref.isStatic, includeDot = ref.includeDot)
			is CommonTagHandler.CLASS -> buildTemplateClass(ref.clazz)
			else -> invalidOp("Unsupported result")
		}
	}

	class ProgramRefNode(val ts: CommonGenerator, val type: String, val desc: String) : Minitemplate.BlockNode {
		override fun eval(context: Minitemplate.Context) {
			context.write(ts.evalReference(type, desc))
		}
	}

	val miniConfig = Minitemplate.Config(
		extraTags = listOf(
			Minitemplate.Tag(
				":programref:", setOf(), null,
				aliases = listOf("SINIT", "CONSTRUCTOR", "SMETHOD", "METHOD", "IMETHOD", "SFIELD", "FIELD", "IFIELD", "CLASS")
			) { ProgramRefNode(this, it.first().token.name, it.first().token.content) }
		),
		extraFilters = listOf(
		)
	)

	override fun gen(template: String): String = gen(template, extra = hashMapOf())
	fun gen(template: String, extra: HashMap<String, Any?> = hashMapOf()): String = Minitemplate(template, miniConfig).invoke(HashMap(params + extra))
	fun gen(template: String, process: Boolean): String = if (process) Minitemplate(template, miniConfig).invoke(params) else template
	@Suppress("UNUSED_PARAMETER")
	fun gen(template: String, context: AstGenContext, type: String): String = context.rethrowWithContext { Minitemplate(template, miniConfig).invoke(params) }

///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////

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

	val perClassNameAllocator = hashMapOf<FqName, PerClassNameAllocator>()

	private val stringPoolGlobal = StringPool()
	private val stringPoolPerClass = hashMapOf<FqName, StringPool>()

	data class StringInPool(val id: Int, val str: String) {
		val name = "STRINGLIT_$id"
	}

	fun getClassNameAllocator(clazz: FqName) = perClassNameAllocator.getOrPut(clazz) { PerClassNameAllocator() }

	private fun getPerClassStrings(clazz: FqName) = stringPoolPerClass.getOrPut(clazz) { StringPool() }

	fun getGlobalStrings(): List<StringInPool> = when (stringPoolType) {
		StringPool.Type.GLOBAL -> stringPoolGlobal.getAllSorted()
		else -> invalidOp("This target doesn't support global string pool")
	}

	fun getClassStrings(clazz: FqName): List<StringInPool> = when (stringPoolType) {
		StringPool.Type.PER_CLASS -> getPerClassStrings(clazz).getAllSorted()
		else -> invalidOp("This target doesn't support per class string pool")
	}

	fun allocString(clazz: FqName, str: String): Int = when (stringPoolType) {
		StringPool.Type.GLOBAL -> stringPoolGlobal.alloc(str)
		StringPool.Type.PER_CLASS -> getPerClassStrings(clazz).alloc(str)
	}

	open fun buildTemplateClass(clazz: FqName): String = clazz.targetName
	fun buildTemplateClass(clazz: AstClass): String = buildTemplateClass(clazz.name)

//open fun getClassStaticInit(classRef: AstType.REF, reason: String): String = buildStaticInit(classRef.name)

	val normalizeNameCache = hashMapOf<String, String>()

	protected open var baseElementPrefix = ""

	enum class NameKind { PARAM, LOCAL, METHOD, FIELD }

	open fun normalizeName(name: String, kind: NameKind): String {
		if (name.isNullOrEmpty()) return ""
		val rname = when (kind) {
			NameKind.METHOD, NameKind.FIELD -> "$baseElementPrefix$name"
			NameKind.PARAM, NameKind.LOCAL -> name
		}
		if (rname !in normalizeNameCache) {
			if (rname in keywords) return normalizeName("_$rname", kind)
			val chars = rname.toCharArray()
			for (i in chars.indices) {
				var c = chars[i]
				if (!c.isLetterDigitOrUnderscore() || c == '$') c = '_'
				chars[i] = c
			}
			if (chars[0].isDigit()) chars[0] = '_'
			normalizeNameCache[rname] = String(chars)
		}
		return normalizeNameCache[rname]!!
	}

//////////////////////////////////////////////////
// Primitive types
//////////////////////////////////////////////////

	open val NullType by lazy { AstType.OBJECT.targetName }
	open val VoidType = "void"
	open val BoolType = "boolean"
	open val IntType = "int"
	open val ShortType: String get() = IntType
	open val CharType: String get() = IntType
	open val ByteType: String get() = IntType
	open val FloatType = "float"
	open val DoubleType = "double"
	open val LongType = "long"

	open val BaseArrayType = "JA_0"
	open val BoolArrayType = "JA_Z"
	open val ByteArrayType = "JA_B"
	open val CharArrayType = "JA_C"
	open val ShortArrayType = "JA_S"
	open val IntArrayType = "JA_I"
	open val LongArrayType = "JA_J"
	open val FloatArrayType = "JA_F"
	open val DoubleArrayType = "JA_D"
	open val ObjectArrayType = "JA_L"

	open val BaseArrayTypeRef get() = BaseArrayType
	open val BoolArrayTypeRef get() = BoolArrayType
	open val ByteArrayTypeRef get() = ByteArrayType
	open val CharArrayTypeRef get() = CharArrayType
	open val ShortArrayTypeRef get() = ShortArrayType
	open val IntArrayTypeRef get() = IntArrayType
	open val LongArrayTypeRef get() = LongArrayType
	open val FloatArrayTypeRef get() = FloatArrayType
	open val DoubleArrayTypeRef get() = DoubleArrayType
	open val ObjectArrayTypeRef get() = ObjectArrayType

	open val DoubleNegativeInfinityString = "-Infinity"
	open val DoublePositiveInfinityString = "Infinity"
	open val DoubleNanString = "NaN"

	open val FloatNegativeInfinityString get() = DoubleNegativeInfinityString
	open val FloatPositiveInfinityString get() = DoublePositiveInfinityString
	open val FloatNanString get() = DoubleNanString

//////////////////////////////////////////////////
// Constants
//////////////////////////////////////////////////

	open val AstType.nativeDefault: Any? get() = this.getNull()
	open val AstType.nativeDefaultString: String get() {
		return this.nativeDefault.escapedConstant
	}

	fun Any?.escapedConstantOfType(type: AstType): String {
		if (type == AstType.BOOL) {
			return when ("$this") {
				"", "0", "false" -> "false"
				else -> "true"
			}
		} else {
			return this.escapedConstant
		}
	}

	enum class ConstantPlace { ANY, LOCAL, PARAM, FIELD }

	val Any?.escapedConstant: String get() = escapedConstant(this, ConstantPlace.ANY)
	val Any?.escapedConstantLocal: String get() = escapedConstant(this, ConstantPlace.LOCAL)
	val Any?.escapedConstantParam: String get() = escapedConstant(this, ConstantPlace.PARAM)
	val Any?.escapedConstantField: String get() = escapedConstant(this, ConstantPlace.FIELD)

	open fun escapedConstant(v: Any?, place: ConstantPlace): String = when (v) {
		null -> "null"
		is Boolean -> if (v) "true" else "false"
		is String -> v.escapeString
		is Long -> N_lnew(v)
		is Float -> if (v.isInfinite()) if (v < 0) FloatNegativeInfinityString else FloatPositiveInfinityString else if (v.isNaN()) FloatNanString else if (floatHasFSuffix) "${v}f" else "$v"
		is Double -> {
			val out = if (v.isInfinite()) if (v < 0) DoubleNegativeInfinityString else DoublePositiveInfinityString else if (v.isNaN()) if (v < 0) "-$DoubleNanString" else DoubleNanString else "$v"
			if (optionalDoubleDummyDecimals && out.endsWith(".0")) {
				out.substr(0, -2)
			} else {
				out
			}
		}
		is Int -> when (v) {
			Int.MIN_VALUE -> "N${staticAccessOperator}MIN_INT32"
			else -> N_inew(v.toInt())
		}
		is Number -> N_inew(v.toInt())
		is Char -> N_inew(v.toInt())
		is AstType -> {
			for (fqName in v.getRefClasses()) mutableBody.initClassRef(fqName, "class literal")
			v.escapeType
		}
		else -> throw NotImplementedError("Literal of type $v")
	}

	open protected val String.escapeString: String get() = N_func("strLitEscape", quoteString(this))
	open protected val AstType.escapeType: String get() = N_func("resolveClass", quoteString(this.mangle()))

//////////////////////////////////////////////////
// Type names
//////////////////////////////////////////////////

	enum class TypeType {
		NORMAL, REF, STATIC_CALL
	}

	val AstType.targetName: String get() = getTypeTargetName(this, ref = false)
	open val AstType.targetNameRef: String get() = getTypeTargetName(this, ref = true)
	//open val AstType.targetNameRefBounds: String get() = getTypeTargetName(this, ref = true)

	fun getTypeTargetName(type: AstType, ref: Boolean): String {
		val type = type.resolve()
		return when (type) {
			is AstType.NULL -> NullType
			is AstType.UNKNOWN -> {
				println("Referenced UNKNOWN: $type")
				NullType
			}
			is AstType.VOID -> VoidType
			is AstType.BOOL -> BoolType
			is AstType.GENERIC -> type.type.targetName
			is AstType.INT -> IntType
			is AstType.SHORT -> ShortType
			is AstType.CHAR -> CharType
			is AstType.BYTE -> ByteType
			is AstType.FLOAT -> FloatType
			is AstType.DOUBLE -> DoubleType
			is AstType.LONG -> LongType
			is AstType.REF -> {
				val clazz = program[type.name]
				val nativeName = clazz.nativeName
				if (nativeName != null) imports += FqName(clazz.nativeName!!)
				nativeName ?: if (ref) type.name.targetNameRef else type.name.targetName
			}
			is AstType.ARRAY -> when (type.element) {
				is AstType.BOOL -> if (ref) BoolArrayTypeRef else BoolArrayType
				is AstType.BYTE -> if (ref) ByteArrayTypeRef else ByteArrayType
				is AstType.CHAR -> if (ref) CharArrayTypeRef else CharArrayType
				is AstType.SHORT -> if (ref) ShortArrayTypeRef else ShortArrayType
				is AstType.INT -> if (ref) IntArrayTypeRef else IntArrayType
				is AstType.LONG -> if (ref) LongArrayTypeRef else LongArrayType
				is AstType.FLOAT -> if (ref) FloatArrayTypeRef else FloatArrayType
				is AstType.DOUBLE -> if (ref) DoubleArrayTypeRef else DoubleArrayType
				else -> if (ref) ObjectArrayTypeRef else ObjectArrayType
			}
			else -> throw RuntimeException("Not supported native type $this")
		}
	}

//////////////////////////////////////////////////
// Local names
//////////////////////////////////////////////////

	open val LocalParamRef.targetName: String get() = normalizeName(this.name, NameKind.LOCAL)

//////////////////////////////////////////////////
// Class names
//////////////////////////////////////////////////

	inline fun <reified T : Any> nativeName(): String = T::class.java.name.fqname.targetName

	val imports = hashSetOf<FqName>()

	open val FqName.targetNameRef: String get() = this.targetName
	open val FqName.targetName: String get() = this.fqname.replace('.', '_').replace('$', '_')
	open val FqName.targetClassFqName: String get() = this.targetName
	open val FqName.targetSimpleName: String get() = this.simpleName
	open val FqName.targetNameForStatic: String get() {
		val clazz = program[this]
		return when {
			(clazz.nativeName != null) -> {
				imports += FqName(clazz.nativeName!!)
				clazz.nativeName!!
			}
			else -> this.targetNameForStaticNonNative
		}
	}
	open val FqName.targetNameForStaticNonNative: String get() {
		val clazz = program[this]
		return when {
			(!clazz.isInterface || interfacesSupportStaticMembers) -> this.targetName
			else -> this.targetName + "_IFields"
		}
	}
	open val FqName.targetFilePath: String get() = this.simpleName
	open val FqName.targetGeneratedFqName: FqName get() = this
	open val FqName.targetGeneratedFqPackage: String get() = this.packagePath

//////////////////////////////////////////////////
// Method names
//////////////////////////////////////////////////

//open val MethodRef.targetName: String get() = normalizeName(this.ref.name)

	open val AstMethodRef.objectToCache: Any get() = if (this.isClassOrInstanceInit) this else this.withoutClass

	open val MethodRef.targetName: String get() {
		val method = this.ref
		val realmethod = program[method] ?: invalidOp("Can't find method $method")
		val realclass = realmethod.containingClass

		return if (realclass.isNative) {
			// No cache
			realmethod.nativeNameForTarget(this@CommonGenerator.targetName) ?: method.name
		} else {
			methodNames.getOrPut2(method.objectToCache) {
				if (minimize && !realmethod.keepName) {
					allocMemberName()
				} else {
					if (realmethod.nativeMethod != null) {
						realmethod.nativeMethod!!
					} else {
						val name2 = method.targetNameBase
						val name = when (method.name) {
							"<init>", "<clinit>" -> "${method.containingClass}$name2"
							else -> name2
						}
						cleanMethodName(name)
					}
				}
			}
		}
	}

	open val MethodRef.targetNameBase: String get() = "${this.ref.name}${this.ref.desc}"
//open val MethodRef.targetNameBase: String get() = "${this.ref.name}"

	open protected fun cleanMethodName(name: String): String {
		if (name in keywords) return cleanMethodName("_$name")
		val out = CharArray(name.length)
		for (n in 0 until name.length) out[n] = if (name[n].isLetterOrDigit()) name[n] else '_'
		return String(out)
	}

	open protected fun cleanFieldName(name: String): String {
		if (name in keywords) return cleanFieldName("_$name")
		val out = CharArray(name.length)
		for (n in 0 until name.length) out[n] = if (name[n].isLetterOrDigit()) name[n] else '_'
		return String(out)
	}

//override val MethodRef.targetName: String get() {
//	val methodRef: AstMethodRef = this.ref
//	val keyToUse: Any = if (methodRef.isInstanceInit) methodRef else methodRef.withoutClass
//	return methodNames.getOrPut2(keyToUse) {
//		if (minimize) {
//			allocMemberName()
//		} else {
//			if (program is AstProgram) {
//				val method = methodRef.resolve(program)
//				if (method.nativeName != null) {
//					return method.nativeName!!
//				}
//			}
//			return if (methodRef.isInstanceInit) {
//				"${methodRef.classRef.fqname}${methodRef.name}${methodRef.desc}"
//			} else {
//				"${methodRef.name}${methodRef.desc}"
//			}
//		}
//	}
//}

	fun getTargetMethodAccess(refMethod: AstMethod, static: Boolean): String = access(refMethod.targetName, static, field = false)
	fun buildMethod(method: AstMethod, static: Boolean, includeDot: Boolean = false): String {
		val clazzFqname = method.containingClass.name
		//val nonNativeCall = method.annotationsList.nonNativeCall
		val nonNativeCall = false
		val clazz = when {
			static && nonNativeCall -> clazzFqname.targetNameForStaticNonNative
			static -> clazzFqname.targetNameForStatic
			else -> clazzFqname.targetName
		}
		val name = method.targetName
		return if (static) (clazz + access(name, static = true, field = false)) else if (includeDot) instanceAccess(name, field = false) else name
	}

	fun buildConstructor(method: AstMethod): String {
		val clazz = method.containingClass.name.targetName
		val methodName = method.targetName
		return "(new $clazz())" + access(methodName, static = false, field = false)
	}

//////////////////////////////////////////////////
// Field names
//////////////////////////////////////////////////

	protected val fieldNames = hashMapOf<Any?, String>()
	protected val methodNames = hashMapOf<Any?, String>()
	protected val classNames = hashMapOf<Any?, String>()
	protected val cachedFieldNames = hashMapOf<AstFieldRef, String>()

	open val FieldRef.targetName: String get() {
		val fieldRef = this
		val field = fieldRef.ref
		val realfield = program[field]
		val realclass = program[field.containingClass]
		val keyToUse = field

		val normalizedFieldName = cleanFieldName(field.name)

		//if (normalizedFieldName == "_parameters" || normalizedFieldName == "__parameters") {
		//	println("_parameters")
		//}

		return if (realclass.isNative) {
			realfield.nativeNameForTarget(this@CommonGenerator.targetName) ?: normalizedFieldName
		} else {
			fieldNames.getOrPut2(keyToUse) {
				if (minimize && !realfield.keepName) {
					allocMemberName()
				} else {
					val rnormalizedFieldName = normalizeName(cleanFieldName(field.name), NameKind.FIELD)
					// @TODO: Move to CommonNames
					if (field !in cachedFieldNames) {
						//val fieldName = normalizedFieldName
						val fieldName = rnormalizedFieldName
						//var name = if (fieldName in keywords) "${fieldName}_" else fieldName

						val clazz = program[field].containingClass

						var name = "_$fieldName"
						//var name = "_${fieldName}_${clazz.name.fqname}_${fieldRef.ref.type.mangle()}"

						val clazzAncestors = clazz.ancestors.reversed()
						val names = clazzAncestors.flatMap { it.fields }
							.filter { normalizeName(it.name, NameKind.FIELD) == rnormalizedFieldName }
							//.filter { it.name == field.name }
							.map { it.targetName }.toHashSet()
						val fieldsColliding = clazz.fields.filter {
							(it.ref == field) || (normalizeName(it.name, NameKind.FIELD) == rnormalizedFieldName)
						}.map { it.ref }

						// JTranscBugInnerMethodsWithSameName.kt
						for (f2 in fieldsColliding) {
							while (name in names) name += "_"
							cachedFieldNames[f2] = name
							names += name
						}
					}
					cachedFieldNames[field] ?: unexpected("Unexpected. Not cached: $field")
				}
			}
		}
	}

//override val FieldRef.targetName: String get() {
//	val fieldRef = this
//	//"_" + field.uniqueName
//	val keyToUse = fieldRef.ref
//
//	return fieldNames.getOrPut2(keyToUse) {
//		val field = program[fieldRef.ref]
//		if (minimize) {
//			allocMemberName()
//		} else {
//			if (fieldRef !in cachedFieldNames) {
//				val fieldName = field.name.replace('$', '_')
//				//var name = if (fieldName in JsKeywordsWithToStringAndHashCode) "${fieldName}_" else fieldName
//				var name = "_$fieldName"
//
//				val clazz = program[fieldRef.ref].containingClass
//				val clazzAncestors = clazz.ancestors.reversed()
//				val names = clazzAncestors.flatMap { it.fields }.filter { it.name == field.name }.map { it.targetName }.toHashSet()
//				val fieldsColliding = clazz.fields.filter { it.name == field.name }.map { it.ref }
//
//				// JTranscBugInnerMethodsWithSameName.kt
//				for (f2 in fieldsColliding) {
//					while (name in names) name += "_"
//					cachedFieldNames[f2] = name
//					names += name
//				}
//				cachedFieldNames[field.ref] ?: unexpected("Unexpected. Not cached: $field")
//			}
//			cachedFieldNames[field.ref] ?: unexpected("Unexpected. Not cached: $field")
//		}
//	}
//}

	val AstField.constantValueOrNativeDefault: Any? get() = if (this.hasConstantValue) this.constantValue else this.type.nativeDefault
	val AstField.escapedConstantValue: String get() = this.constantValueOrNativeDefault.escapedConstant
	val AstField.escapedConstantValueField: String get() = this.constantValueOrNativeDefault.escapedConstantField
	val AstField.escapedConstantValueLocal: String get() = this.constantValueOrNativeDefault.escapedConstantLocal
	val FieldRef.nativeStaticText: String get() = this.ref.containingTypeRef.name.targetNameForStatic + buildAccessName(program[this.ref], static = true)
	inline fun <reified T : Any, R> KMutableProperty1<T, R>.getTargetName(): String = this.locate(program).targetName

	fun FieldRef.buildField(static: Boolean, includeDot: Boolean = false): String = if (static) this.nativeStaticText else if (includeDot) instanceAccess(this.targetName, field = true) else this.targetName
	fun buildInstanceField(expr: String, field: FieldRef): String = expr + buildAccessName(program[field], static = false)
	fun buildAccessName(field: AstField, static: Boolean): String = access(field.targetName, static, field = true)

//////////////////////////////////////////////////
// Access to members
//////////////////////////////////////////////////

	open fun access(name: String, static: Boolean, field: Boolean): String = if (static) staticAccess(name, field) else instanceAccess(name, field)
	open fun staticAccess(name: String, field: Boolean): String = "$staticAccessOperator$name"
	open fun instanceAccess(name: String, field: Boolean): String = "$instanceAccessOperator$name"

	open fun String.instanceAccessField(name: String): String = this + instanceAccess(name, field = true)
	open fun String.instanceAccessMethod(name: String): String = this + instanceAccess(name, field = false)


/////////////////
// STATIC INIT //
/////////////////

	// @TODO: This should simplify StaticInit
	open fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(buildStaticInit(clazzRef.name))
	}

	open fun buildStaticInit(clazzName: FqName): String? = clazzName.targetName + access("SI", static = true, field = false) + "();"

	val AstClass?.isNative get() = this.isNativeForTarget(targetName)
	//val AstClass.mustGenerate get() = !this.isNative || this.annotationsList.nonNativeCall
	val AstClass.mustGenerate get() = !this.isNative

	open fun getClassesForStaticConstruction(): List<AstClass> = program.staticInitsSorted.map { program[it]!! }.filter {
		!it.isNative
		//it.mustGenerate && !it.isNative
	}

	open fun genStaticConstructorsSortedLines(): List<String> {
		return getClassesForStaticConstruction().map { "${it.name.targetNameForStatic}" + access("SI", static = true, field = false) + "();" }
	}

	open fun genStaticConstructorsSorted() = indent {
		for (line in genStaticConstructorsSortedLines()) line(line)
	}

	open val FqName.actualFqName: FqName get() = this
	val AstClass.actualFqName: FqName get() = this.name.actualFqName

	val prepareThrow by lazy {
		program["java.lang.Throwable".fqname].getMethods("prepareThrow").first()
	}


//open fun getActualFqName(name: FqName): FqName {
//	/*
//	val realclass = if (name in program) program[name] else null
//	return FqName(classNames.getOrPut2(name) {
//		if (realclass?.nativeName != null) {
//			realclass!!.nativeName!!
//		} else {
//			FqName(name.packageParts.map { if (it in keywords) "${it}_" else it }.map(String::decapitalize), "${name.simpleName.replace('$', '_')}_".capitalize()).fqname
//		}
//	})
//	*/
//}
}