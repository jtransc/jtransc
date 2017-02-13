package com.jtransc.gen.common

import com.jtransc.ConfigLibraries
import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.JTranscVersion
import com.jtransc.annotation.JTranscAddMembersList
import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.annotation.JTranscLiteralParam
import com.jtransc.annotation.JTranscUnboxParam
import com.jtransc.ast.*
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.ast.treeshaking.getTargetAddFiles
import com.jtransc.ds.getOrPut2
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.error.noImplWarn
import com.jtransc.error.unexpected
import com.jtransc.gen.MinimizedNames
import com.jtransc.gen.TargetName
import com.jtransc.injector.Injector
import com.jtransc.io.ProcessResult2
import com.jtransc.lang.high
import com.jtransc.lang.low
import com.jtransc.lang.putIfAbsentJre7
import com.jtransc.template.Minitemplate
import com.jtransc.text.Indenter
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.quote
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*
import kotlin.reflect.KMutableProperty1

class ConfigSrcFolder(val srcFolder: SyncVfsFile)

@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN", "RemoveSingleExpressionStringTemplate")
open class CommonGenerator(val injector: Injector) : IProgramTemplate {
	// CONFIG
	open val staticAccessOperator: String = "."
	open val instanceAccessOperator: String = "."
	open val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	open val languageRequiresDefaultInSwitch = false
	open val defaultGenStmSwitchHasBreaks = true
	open val interfacesSupportStaticMembers = true

	val configTargetFolder: ConfigTargetFolder = injector.get()
	val program: AstProgram = injector.get()
	val sortedClasses by lazy { program.classes.filter { !it.isNative }.sortedByExtending() }
	val targetName = injector.get<TargetName>()
	open val methodFeatures: Set<Class<out AstMethodFeature>> = setOf()
	open val methodFeaturesWithTraps: Set<Class<out AstMethodFeature>> get() = methodFeatures
	open val keywords: Set<String> = program.getExtraKeywords(targetName.name).toSet()
	val configSrcFolder: ConfigSrcFolder = injector.get()
	open val srcFolder: SyncVfsFile = configSrcFolder.srcFolder
	val configMinimizeNames: ConfigMinimizeNames? = injector.getOrNull()
	val minimize: Boolean = configMinimizeNames?.minimizeNames ?: false

	val settings: AstBuildSettings = injector.get()
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

	open fun buildSource(): Unit {
		val targetFolder = configTargetFolder.targetFolder
		writeProgram(targetFolder)
		setInfoAfterBuildingSource()
	}

	open fun compile(): ProcessResult2 {
		val cmdAndArgs = genCompilerCommand(
			programFile = configTargetFolder.targetFolder[configOutputFile.output].realfile,
			debug = settings.debug,
			libs = injector.getOrNull<ConfigLibraries>()?.libs ?: listOf()
		)
		println(cmdAndArgs)
		return if (cmdAndArgs.isEmpty()) {
			ProcessResult2(0)
		} else {
			val result = LocalVfs(File(configTargetFolder.targetFolder.realpathOS)).exec(cmdAndArgs)
			if (!result.success) {
				throw RuntimeException(result.outputString + result.errorString)
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

	open fun writeProgram(output: SyncVfsFile) {
	}

	val indenterPerClass = hashMapOf<AstClass, Indenter>()

	open fun genClasses(output: SyncVfsFile): Indenter = Indenter.gen {
		val concatFilesTrans = copyFiles(output)

		line(concatFilesTrans.prepend)
		line(genClassesWithoutAppends(output))
		line(concatFilesTrans.append)
	}

	open fun genClassesWithoutAppends(output: SyncVfsFile): Indenter = Indenter.gen {
		for (clazz in sortedClasses) {
			val indenter = if (clazz.implCode != null) Indenter(clazz.implCode!!) else genClass(clazz)
			indenterPerClass[clazz] = indenter
			line(indenter)
		}
	}

	open fun genClass(clazz: AstClass): Indenter = Indenter.gen {
		setCurrentClass(clazz)

		if (interfacesSupportStaticMembers || !clazz.isInterface) {
			line(genClassDecl(clazz, MemberTypes.ALL)) {
				line(genClassBody(clazz, MemberTypes.ALL))
			}
		} else {
			line(genClassDecl(clazz, MemberTypes.INSTANCE)) {
				line(genClassBody(clazz, MemberTypes.INSTANCE))
			}
			line(genClassDecl(clazz, MemberTypes.STATIC)) {
				line(genClassBody(clazz, MemberTypes.STATIC))
			}
		}
	}

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
		val implementing = getClassInterfaces(clazz)
		return if (implementing.isNotEmpty()) " implements ${implementing.map { it.targetClassFqName }.joinToString(", ")}" else ""
	}

	protected open fun getClassInterfaces(clazz: AstClass): List<FqName> = clazz.implementing

	open fun genClassBody(clazz: AstClass, kind: MemberTypes): Indenter = Indenter.gen {
		val members = clazz.annotationsList.getTypedList(JTranscAddMembersList::value).filter { it.target == targetName.name }.flatMap { it.value.toList() }.joinToString("\n")
		line(gen(members, process = true))
		line(genClassBodyFields(clazz, kind))
		line(genClassBodyMethods(clazz, kind))
		if (kind != MemberTypes.INSTANCE) {
			line(genSIMethod(clazz))
		}
	}

	open fun genClassBodyFields(clazz: AstClass, kind: MemberTypes): Indenter = Indenter.gen {
		for (f in clazz.fields) if (kind.check(f)) line(genField(f))
	}

	open fun genClassBodyMethods(clazz: AstClass, kind: MemberTypes): Indenter = Indenter.gen {
		for (m in clazz.methods) if (kind.check(m)) line(genMethod(clazz, m, !clazz.isInterface || m.isStatic))
	}

	open fun genSIMethod(clazz: AstClass): Indenter = Indenter.gen {
		line(genSIMethodBody(clazz))
	}

	open fun genSIMethodBody(clazz: AstClass): Indenter = Indenter.gen {
		//for (field in clazz.fields.filter { it.isStatic }) {
		//	line("${field.buildField(static = true)};")
		//}
		if (clazz.staticConstructor != null) {
			line("${buildMethod(clazz.staticConstructor!!, static = true)}();")
		}
	}

	open fun genField(field: AstField): Indenter = Indenter.gen {
		val istatic = if (field.isStatic) "static " else ""
		line("$istatic${field.type.targetName} ${field.targetName} = ${field.escapedConstantValue};")
	}

	open fun genMetodDecl(method: AstMethod): String {
		val args = method.methodType.args.map { it.argDecl }

		//if (method.isInstanceInit) mods += "final "

		val mods = genMetodDeclModifiers(method)
		return "$mods ${method.actualRetType.targetName} ${method.targetName}(${args.joinToString(", ")})"
	}

	open fun genMetodDeclModifiers(method: AstMethod): String {
		var mods = ""
		if (!method.isStatic && method.targetIsOverriding) mods += "override "
		if (method.isStatic) mods += "static "
		return mods
	}

	open val AstMethod.actualRetType: AstType get() = if (this.isInstanceInit) this.containingClass.astType else this.methodType.ret

	open fun genMethod(clazz: AstClass, method: AstMethod, mustPutBody: Boolean): Indenter = Indenter.gen {
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
					if (actualMethod.isInstanceInit) line("return " + genExprThis(AstExpr.THIS(clazz.name)) +  ";")
				} else {
					line(genMissingBody(actualMethod))
				}
			}
		} else {
			line("$decl;")
		}
	}

	open fun genMissingBody(method: AstMethod): Indenter = Indenter.gen {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw ${quoteString(message)};")
	}

	open val AstMethod.targetIsOverriding: Boolean get() = this.isOverriding && !this.isInstanceInit && !this.isStatic

	var entryPointClass = FqName("EntryPointClass")
	var entryPointFilePath = "EntryPointFile"

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

		fun processArg(it: AstExpr.Box) = processCallArg(it.value, if (isNativeCall) convertToTarget(it) else it.genExpr())

		val callsiteBody = refMethod.annotationsList.getCallSiteBodiesForTarget(targetName)

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

		if (callsiteBody != null) {
			val args2 = args.withIndex().map { arginfo ->
				val index = arginfo.index
				val arg = arginfo.value
				val paramAnnotations = refMethod.parameterAnnotationsList[index]
				if (paramAnnotations.contains<JTranscLiteralParam>()) {
					val lit = (arg.value as? AstExpr.LITERAL) ?: invalidOp("Used @JTranscLiteralParam without a literal: ${processArg(arg)} in $context")
					lit.value.toString().template("JTranscLiteralParam")
				} else if (paramAnnotations.contains<JTranscUnboxParam>()) {
					val lit = (arg.value as? AstExpr.LITERAL)
					if (lit != null) {
						when (lit.value) {
							is String -> quoteString(lit.value)
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

			val out = Regex("#([',])?((@|\\d)+)").replace(callsiteBody) { mr ->
				val mustQuote = mr.groupValues[1]
				val rid = mr.groupValues[2]
				val res = if (rid == "@") {
					objStr
				} else {
					val id = rid.toInt()
					args2[id]
				}
				if (mustQuote.isNotEmpty()) quoteString(res) else res
			}
			return out
		} else {
			val processedArgs = args.map { processArg(it) }
			val methodAccess = getTargetMethodAccess(refMethod, static = isStaticCall)
			val result = when (e2) {
				is AstExpr.CALL_STATIC -> genExprCallBaseStatic(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
				is AstExpr.CALL_SUPER -> genExprCallBaseSuper(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
				is AstExpr.CALL_INSTANCE -> genExprCallBaseInstance(e2, clazz, refMethodClass, method, methodAccess, processedArgs)
				else -> invalidOp("Unexpected")
			}
			return if (isNativeCall) convertToJava(refMethod.methodType.ret, result) else result
		}
	}

	open fun quoteString(str: String) = str.quote()

	open fun processCallArg(e: AstExpr, str: String): String = str

	open fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		return "super$methodAccess(${args.joinToString(", ")})"
	}

	open fun genExprCallBaseStatic(e2: AstExpr.CALL_STATIC, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		return "${clazz.targetName}$methodAccess(${args.joinToString(", ")})"
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

	data class ConcatFile(val prepend: String?, val append: String?)
	class CopyFile(val content: ByteArray, val dst: String, val isAsset: Boolean)

	class PrependAppend(val prepend: String, val append: String)

	fun copyFiles(output: SyncVfsFile): PrependAppend {
		val resourcesVfs = program.resourcesVfs
		val copyFiles = getFilesToCopy(targetName.name)

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
		if (body.traps.isNotEmpty()) {
			return features.apply(method, body, methodFeaturesWithTraps, settings, types).genBody()
		} else {
			return features.apply(method, body, methodFeatures, settings, types).genBody()
		}
	}

	// @TODO: Remove this from here, so new targets don't have to do this too!
	// @TODO: AstFieldRef should be fine already, so fix it in asm_ast!
	fun fixField(field: AstFieldRef): AstFieldRef = program[field].ref

	fun fixMethod(method: AstMethodRef): AstMethodRef = program[method]?.ref
		?: invalidOp("Can't find method $method while generating $context")

	val allAnnotationTypes = program.allAnnotations.flatMap { it.getAllDescendantAnnotations() }.map { it.type }.distinct().map { program[it.name] }.toSet()

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

		return Indenter.gen {
			@Suppress("LoopToCallChain", "Destructure")
			for (local in body.locals) refs.add(local.type)

			resetLocalsPrefix()
			var info: Indenter? = null

			linedeferred { line(info!!) }
			line(genBodyLocals(body.locals))
			if (body.traps.isNotEmpty()) line(genBodyTrapsPrefix())

			val bodyContent = body.stm.genStm()
			info = genLocalsPrefix()

			if (method.isClassOrInstanceInit) mutableBody.initClassRef(context.clazz.ref, "self")

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
		val className = stm.target.targetName
		val targetLocalName = stm.local.targetName

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
	open fun genBodyLocal(local: AstLocal): Indenter = Indenter("${localDecl(local)} = ${local.type.nativeDefaultString};")

	open fun genBodyTrapsPrefix() = Indenter("${AstType.OBJECT.localDeclType} J__exception__ = null;")
	open fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "J__exception__"


	open val AstType.localDeclType: String get() = this.targetName

	open fun localDecl(local: AstLocal) = "${local.type.localDeclType} ${local.targetName}"
	open val AstArgument.argDecl: String get() = "${this.type.localDeclType} ${this.targetName}"

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

	open fun genStmSwitchGoto(stm: AstStm.SWITCH_GOTO): Indenter = indent {
		line("switch (${stm.subject.genExpr()})") {
			for ((value, label) in stm.cases) line("case $value: goto ${label.name};")
			line("default: goto ${stm.default.name};")
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

	open fun actualSetField(stm: AstStm.SET_FIELD_INSTANCE, left: String, right: String): String = "$left = $right;"

	open fun genStmContinue(stm: AstStm.CONTINUE) = Indenter.single("continue;")
	open fun genStmBreak(stm: AstStm.BREAK) = Indenter.single("break;")
	open fun genStmLabel(stm: AstStm.STM_LABEL): Indenter = Indenter.gen {
		if (stm.label in trapsByEnd) {
			for (trap in trapsByEnd[stm.label]!!) line(genStmRawCatch(trap))
		}
		line(genStmLabelCore(stm))
		if (stm.label in trapsByStart) {
			for (trap in trapsByStart[stm.label]!!) line(genStmRawTry(trap))
		}
	}

	open fun genStmLabelCore(stm: AstStm.STM_LABEL) = "${stm.label.name}:;"

	open fun genStmRawTry(trap: AstTrap): Indenter = Indenter.gen {
	}

	open fun genStmRawCatch(trap: AstTrap): Indenter = Indenter.gen {
	}

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
		return "(${e.array.genNotNull()})${instanceAccessOperator}length"
	}

	fun AstType.resolve(): AstType = when (this) {
		is AstType.COMMON -> this.resolve(program)
		is AstType.MUTABLE -> this.ref.resolve()
		else -> this
	}

	open fun genExprBinop(e: AstExpr.BINOP): String {
		val resultType = e.type.resolve()
		val leftType = e.left.type.resolve()
		val rightType = e.right.type.resolve()
		val l = e.left.genExpr()
		val r = e.right.genExpr()
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

	open fun genExprArrayAccess(e: AstExpr.ARRAY_ACCESS): String = N_AGET_T(e.array.type.resolve(program) as AstType.ARRAY, e.array.type.elementType, e.array.genNotNull(), e.index.genExpr())

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

	open fun genStmSetArray(stm: AstStm.SET_ARRAY) = Indenter.single(N_ASET_T(stm.array.type.resolve(program) as AstType.ARRAY, stm.array.type.elementType, stm.array.genNotNull(), stm.index.genExpr(), stm.expr.genExpr()))

	open fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter.gen {
		var n = 0
		for (v in stm.values) {
			line(genStmSetArray(AstStm.SET_ARRAY(stm.array.value, AstExpr.LITERAL(stm.startIndex + n), v.value)))
			n++
		}
	}

	open fun genExprParam(e: AstExpr.PARAM) = e.argument.targetName
	open fun genExprLocal(e: AstExpr.LOCAL) = e.local.targetName

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
	open fun genStmThrow(stm: AstStm.THROW) = Indenter("throw ${stm.value.genExpr()};")
	open fun genStmRethrow(stm: AstStm.RETHROW) = Indenter("""throw J__i__exception__;""")
	open fun genStmStms(stm: AstStm.STMS) = indent { for (s in stm.stms) line(s.genStm()) }
	open fun genStmExpr(stm: AstStm.STM_EXPR) = Indenter.single("${stm.expr.genExpr()};")
	open fun genStmReturnVoid(stm: AstStm.RETURN_VOID) = Indenter.single(if (context.method.methodVoidReturnThis) "return " + genExprThis(AstExpr.THIS("Dummy".fqname)) + ";" else "return;")
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
	open protected fun N_l2i(str: String) = N_func("l2i", "$str")
	open protected fun N_l2l(str: String) = "($str)"
	open protected fun N_l2f(str: String) = N_func("l2f", "$str")
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

	open protected fun N_lnew(value: Long) = N_func("lnew", "${value.high}, ${value.low}")
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

	val AstMethod.nativeBodies: Map<String, Indenter> get() = getNativeBodies(target = this@CommonGenerator.targetName.name)

	fun AstMethod.getNativeBodies(target: String): Map<String, Indenter> {
		val bodies = this.annotationsList.getBodiesForTarget(TargetName(target))

		return bodies.associate { body ->
			body.cond to Indenter.gen {
				for (line in body.lines) line(line.template("nativeBody"))
			}
		}
	}

	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////

	@Suppress("ConvertLambdaToReference")
	val params by lazy {
		hashMapOf(
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
			"orientation" to settings.orientation.lowName,
			"assetFiles" to MergeVfs(settings.assets.map { LocalVfs(it) }).listdirRecursive().filter { it.isFile }.map { it.file },
			"embedResources" to settings.embedResources,
			"assets" to settings.assets,
			"hasIcon" to !settings.icon.isNullOrEmpty(),
			"icon" to settings.icon,
			"libraries" to settings.libraries,
			"extra" to settings.extra,
			"folders" to folders,
			"JTRANSC_VERSION" to JTranscVersion.getVersion()
		)
	}

	open fun setInfoAfterBuildingSource() {
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
			is CommonTagHandler.METHOD -> buildMethod(ref.method, static = ref.isStatic)
			is CommonTagHandler.FIELD -> ref.field.buildField(ref.isStatic)
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
				aliases = listOf("SINIT", "CONSTRUCTOR", "SMETHOD", "METHOD", "SFIELD", "FIELD", "CLASS")
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

	open fun normalizeName(name: String): String {
		if (name.isNullOrEmpty()) return ""
		if (name !in normalizeNameCache) {
			if (name in keywords) return normalizeName("_$name")
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
	open val NegativeInfinityString = "-Infinity"
	open val PositiveInfinityString = "Infinity"
	open val NanString = "NaN"

	//////////////////////////////////////////////////
	// Constants
	//////////////////////////////////////////////////

	val AstType.nativeDefault: Any? get() = this.getNull()
	val AstType.nativeDefaultString: String get() = this.getNull().escapedConstant

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

	val Any?.escapedConstant: String get() = escapedConstant(this)
	open fun escapedConstant(v: Any?): String = when (v) {
		null -> "null"
		is Boolean -> if (v) "true" else "false"
		is String -> v.escapeString
		is Long -> N_lnew(v)
		is Float -> v.toDouble().escapedConstant
		is Double -> if (v.isInfinite()) if (v < 0) NegativeInfinityString else PositiveInfinityString else if (v.isNaN()) NanString else "$v"
		is Int -> when (v) {
			Int.MIN_VALUE -> "N${staticAccessOperator}MIN_INT32"
			else -> "$v"
		}
		is Number -> "${v.toInt()}"
		is Char -> "${v.toInt()}"
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

	open val AstType.targetName: String get() {
		val type = this.resolve()
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
			is AstType.REF -> program[type.name].nativeName ?: type.name.targetName
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
			else -> throw RuntimeException("Not supported native type $this")
		}
	}

	//////////////////////////////////////////////////
	// Local names
	//////////////////////////////////////////////////

	open val LocalParamRef.targetName: String get() = normalizeName(this.name)

	//////////////////////////////////////////////////
	// Class names
	//////////////////////////////////////////////////

	inline fun <reified T : Any> nativeName(): String = T::class.java.name.fqname.targetName

	open val FqName.targetName: String get() = this.fqname.replace('.', '_').replace('$', '_')
	open val FqName.targetClassFqName: String get() = this.targetName
	open val FqName.targetSimpleName: String get() = this.simpleName
	open val FqName.targetNameForStatic: String get() = if (!program[this].isInterface || interfacesSupportStaticMembers) this.targetName else this.targetName + "_IFields"
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
			realmethod.nativeName ?: method.name
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

	fun getTargetMethodAccess(refMethod: AstMethod, static: Boolean): String = buildAccessName(refMethod.targetName, static, field = false)
	fun buildMethod(method: AstMethod, static: Boolean): String {
		val clazzFqname = method.containingClass.name
		val clazz = if (static) clazzFqname.targetNameForStatic else clazzFqname.targetName
		val name = method.targetName
		return if (static) (clazz + buildAccessName(name, static = true, field = false)) else name
	}

	fun buildConstructor(method: AstMethod): String {
		val clazz = method.containingClass.name.targetName
		val methodName = method.targetName
		return "(new $clazz())" + buildAccessName(methodName, static = false, field = false)
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

		return if (realclass.isNative) {
			realfield.nativeName ?: normalizedFieldName
		} else {
			fieldNames.getOrPut2(keyToUse) {
				if (minimize && !realfield.keepName) {
					allocMemberName()
				} else {
					val rnormalizedFieldName = normalizeName(field.name)
					// @TODO: Move to CommonNames
					if (field !in cachedFieldNames) {
						val fieldName = normalizedFieldName
						//var name = if (fieldName in keywords) "${fieldName}_" else fieldName

						val clazz = program[field].containingClass

						var name = "_$fieldName"
						//var name = "_${fieldName}_${clazz.name.fqname}_${fieldRef.ref.type.mangle()}"

						val clazzAncestors = clazz.ancestors.reversed()
						val names = clazzAncestors.flatMap { it.fields }
							.filter { normalizeName(it.name) == rnormalizedFieldName }
							//.filter { it.name == field.name }
							.map { it.targetName }.toHashSet()
						val fieldsColliding = clazz.fields.filter {
							(it.ref == field) || (normalizeName(it.name) == rnormalizedFieldName)
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
	val FieldRef.nativeStaticText: String get() = this.ref.containingTypeRef.name.targetNameForStatic + buildAccessName(program[this.ref], static = true)
	inline fun <reified T : Any, R> KMutableProperty1<T, R>.getTargetName(): String = this.locate(program).targetName

	fun FieldRef.buildField(static: Boolean): String = if (static) this.nativeStaticText else this.targetName
	fun buildInstanceField(expr: String, field: FieldRef): String = expr + buildAccessName(program[field], static = false)
	fun buildAccessName(field: AstField, static: Boolean): String = buildAccessName(field.targetName, static, field = true)

	//////////////////////////////////////////////////
	// Access to members
	//////////////////////////////////////////////////

	open fun buildAccessName(name: String, static: Boolean, field: Boolean): String = if (static) buildStaticAccessName(name, field) else buildInstanceAccessName(name, field)
	open fun buildStaticAccessName(name: String, field: Boolean): String = "$staticAccessOperator$name"
	open fun buildInstanceAccessName(name: String, field: Boolean): String = "$instanceAccessOperator$name"


	/////////////////
	// STATIC INIT //
	/////////////////

	// @TODO: This should simplify StaticInit
	open fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(buildStaticInit(clazzRef.name))
	}

	open fun buildStaticInit(clazzName: FqName): String? = clazzName.targetName + buildAccessName("SI", static = true, field = false) + "();"

	open fun genStaticConstructorsSorted() = indent {
		for (sis in program.staticInitsSorted) {
			val clazz = program[sis]
			if (!clazz.isNative) {
				line("${sis.name.targetNameForStatic}" + buildAccessName("SI", static = true, field = false) + "();")
			}
			//val sc = program[sis]?.staticConstructor
			//if (sc != null) line(buildMethod(sc, true) + "();")
		}
	}
}