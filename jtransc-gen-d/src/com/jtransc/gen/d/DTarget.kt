package com.jtransc.gen.d

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.JTranscSystem
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.ast.feature.method.UndeterministicParameterEvaluationFeature
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.text.*
import com.jtransc.vfs.*
import java.io.File

// Supports GOTO keyword
// Supports static fields and methods on interfaces
class DTarget : GenTargetDescriptor() {
	override val name = "d"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable = true
	override val programFeatures: Set<Class<AstProgramFeature>> = setOf()

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("d", "d", "program.d", minimizeNames = false)
	)

	@Suppress("ConvertLambdaToReference")
	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-d"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<DGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"exe" -> "cpp"
		"bin" -> "cpp"
		else -> null
	}
}

@Singleton
class DGenerator(injector: Injector) : CommonGenerator(injector) {
	override val TARGET_NAME: String = "DLANG"
	override val SINGLE_FILE: Boolean = true

	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java, UndeterministicParameterEvaluationFeature::class.java) // Undeterministic is required on windows!?
	override val methodFeatures = (methodFeaturesWithTraps + listOf(GotosFeature::class.java)).toSet()
	override val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	override val floatHasFSuffix: Boolean = true

	override val keywords = setOf(
		"abstract", "alias", "align", "asm", "assert", "auto",
		"body", "bool", "break", "byte",
		"case", "cast", "catch", "cdouble", "cent", "cfloat", "char", "class", "const", "continue", "creal",
		"dchar", "debug", "default", "delegate", "delete", "deprecated", "do", "double",
		"else", "enum", "export", "extern",
		"false", "final", "finally", "float", "for", "foreach", "foreach_reverse", "function",
		"goto",
		"idouble", "if", "ifloat", "immutable", "import", "in", "inout", "int", "interface", "invariant", "ireal", "is",
		"lazy", "long",
		"macro", "mixin", "module",
		"new", "nothrow", "null",
		"out", "override",
		"package", "pragma", "private", "protected", "public", "pure",
		"real", "ref", "return",
		"scope", "shared", "short", "static", "struct", "super", "switch", "synchronized",
		"template", "this", "throw", "true", "try", "typedef", "typeid", "typeof",
		"ubyte", "ucent", "uint", "ulong", "union", "unittest", "ushort",
		"version", "void", "volatile",
		"wchar", "while", "with",
		"__FILE__", "__FILE_FULL_PATH__", "__MODULE__", "__LINE__", "__FUNCTION__", "__PRETTY_FUNCTION__", "__gshared", "__traits", "__vector", "__parameters",

		// Known Object symbols
		"clone", "toString",
		"std", "core"
	)

	override val languageRequiresDefaultInSwitch = true
	override val defaultGenStmSwitchHasBreaks = true

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return DCompiler.genCommand(programFile, debug, libs, extraVars)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = if (JTranscSystem.isWindows()) {
			listOf("program.exe", "a.exe")
		} else {
			listOf("program", "program.out", "a", "a.out")
		}
		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")
		return ProcessResult2(RootLocalVfs().exec(outFile.realpathOS, listOf(), ExecOptions(passthru = redirect, sysexec = true)))
	}

	override fun writeClasses(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeClasses(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter {
		var targetType = field.type.targetName
		//if (field.modifiers.isVolatile) targetType = "shared($targetType)"
		if (field.isStatic) targetType = "__gshared $targetType"

		if (field.targetName == "__parameters") {
			println("ERROR")
		}

		line("$targetType ${field.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun genSingleFileClasses(output: SyncVfsFile): Indenter = Indenter {
		val StringFqName = buildTemplateClass("java.lang.String".fqname)
		val classesStr = super.genSingleFileClasses(output)
		line(classesStr)

		for (lit in getGlobalStrings()) {
			line("__gshared $StringFqName ${lit.name} = N.strLitEscape(${lit.str.dquote()});")
		}
		//line("static void __initStrings()") {
		//	for (lit in getGlobalStrings()) {
		//		// STRINGLIT_
		//		line("${lit.name} = N.strLitEscape(${lit.str.dquote()});")
		//	}
		//}

		val entryPointFqName = program.entrypoint
		val entryPointClass = program[entryPointFqName]
		line("int main(string[] args)") {
			line("N.init();")
			//line("__initStrings();")
			line(genStaticConstructorsSorted())
			//line(buildStaticInit(entryPointFqName))
			val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.STRING)))]
			line(buildMethod(mainMethod, static = true) + "(N.strArray(args[1..$]));")
			line("return 0;")
		}
	}

	fun String?.dquote(): String {
		if (this == null) return "null"
		return "[" + this.map { it.toInt() }.joinToString(",") + "]"
		/*
		val out = StringBuilder()
		for (n in 0 until this.length) {
			val c = this[n]
			when (c) {
				'\\' -> out.append("\\\\")
				'"' -> out.append("\\\"")
				'\n' -> out.append("\\n")
				'\r' -> out.append("\\r")
				'\t' -> out.append("\\t")
				in '\u0000'..'\u001f', in '\u007f'..'\uffff' -> out.append("\\u" + "%04x".format(c.toInt()))
				else -> out.append(c)
			}
		}
		return "\"" + out.toString() + "\""
		*/
	}

	override fun genClassBodyMethods(clazz: AstClass, kind: MemberTypes): Indenter = Indenter {
		val directMethods = clazz.methods
		val interfaceMethods = clazz.allDirectInterfaces.flatMap { it.methods }
		val actualMethods = (if (clazz.isInterface) directMethods else directMethods + interfaceMethods).filter { !it.isStatic }
		for (rm in directMethods.filter { it.isStatic }) {
			line(genMethod(clazz, rm, true))
		}
		for (rm in actualMethods.map { clazz.getMethodInAncestors(it.ref.nameDesc) ?: invalidOp("Can't find method $it in $clazz ancestors") }.distinct()) {
			// @TODO: HACK!
			if (rm.containingClass != clazz) {
				if (!rm.isOverriding) line("override")
			}
			line(genMethod(clazz, rm, !clazz.isInterface))
		}
	}

	override fun genMethodDeclModifiers(method: AstMethod): String {
		//if (method.isInstanceInit) {
		//	//return "pragma(inline, true)" + super.genMethodDeclModifiers(method)
		//	return "pragma(inline) final " + super.genMethodDeclModifiers(method)
		//} else {
		return super.genMethodDeclModifiers(method)
		//}
	}

	override fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		val CLASS = if (clazz.isInterface) "interface" else "class"
		val iabstract = if (clazz.isAbstract) "abstract " else ""
		val base = "$iabstract$CLASS ${clazz.name.targetSimpleName}"
		val parts = arrayListOf<String>()
		if (clazz.extending != null) parts += clazz.extending!!.targetClassFqName
		if (clazz.implementing.isNotEmpty()) parts += clazz.implementing.map { it.targetClassFqName }
		if (parts.isEmpty()) {
			return base
		} else {
			return "$base : ${parts.distinct().joinToString(", ")}"
		}
	}

	override fun N_is(a: String, b: String): String = "((cast($b)$a) !is null)"

	override val NullType by lazy { AstType.OBJECT.targetName }
	override val VoidType = "void"
	override val BoolType = "bool"
	override val IntType = "int"
	override val ShortType = "short"
	override val CharType = "wchar"
	override val ByteType = "byte"
	override val FloatType = "float"
	override val DoubleType = "double"
	override val LongType = "long"

	override val FqName.targetSimpleName: String get() = this.targetName

	override fun N_c(str: String, from: AstType, to: AstType): String {
		//if (str == "this") return "this"
		//if (to is AstType.REF && to.fqname == "java.lang.Object" && from is AstType.Reference) return str

		if (from is AstType.REF && to is AstType.REF) {
			val fromClass = program[from]!!
			val toClass = program[to]!!
			if (toClass in fromClass.ancestors) return str
			if (toClass in fromClass.allInterfacesInAncestors) return str
		}

		return "(cast(${to.targetName})($str))"
	}

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(cast($BaseArrayType)${e.array.genNotNull()}).length"
	override fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw new WrappedThrowable(${stm.exception.genExpr()});")

	override fun genSIMethod(clazz: AstClass): Indenter = Indenter {
		if (clazz.isJavaLangObject) {
			line("override public string toString()") {
				line("return to!string(N.istr(" + buildMethod(clazz.getMethodWithoutOverrides("toString")!!, static = false) + "()));")
			}
		}

		if (!clazz.isInterface) {
			if (clazz.isJavaLangObject) {
				line("public int __JT__CLASS_ID;")
				line("this(int CLASS_ID = ${clazz.classId}) { this.__JT__CLASS_ID = CLASS_ID; }")
			} else {
				line("this(int CLASS_ID = ${clazz.classId}) { super(CLASS_ID); }")
			}
		}
		if (clazz.staticConstructor != null) {
			line("static public void SI()") {
				for (field in clazz.fields.filter { it.isStatic }) {
					line("${clazz.name.targetName}.${field.targetName} = ${field.escapedConstantValue};")
				}
				line(genSIMethodBody(clazz))
			}
		} else {
			line("static public void SI() { }")
		}
	}

	//override fun N_i(str: String) = "(cast(int)($str))"
	override fun N_i(str: String) = "($str)"

	override fun N_f2i(str: String) = "N.f2i($str)"
	override fun N_d2i(str: String) = "N.d2i($str)"
	override fun N_c_eq(l: String, r: String) = "($l is $r)"
	override fun N_c_ne(l: String, r: String) = "($l !is $r)"

	override fun N_i2f(str: String) = "(cast(float)($str))"
	override fun N_i2d(str: String) = "(cast(double)($str))"

	override fun N_j2f(str: String) = "(cast(float)($str))"
	override fun N_j2d(str: String) = "(cast(double)($str))"

	override fun N_idiv(l: String, r: String): String = "N.idiv($l, $r)"
	override fun N_irem(l: String, r: String): String = "N.irem($l, $r)"

	override fun N_lnew(value: Long): String = when (value) {
		Long.MIN_VALUE -> "(cast(long)(0x8000000000000000LU))"
		else -> "(cast(long)(${value}L))"
	}

	override fun genMissingBody(method: AstMethod): Indenter = Indenter {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw new Throwable(${message.quote()});")
	}

	//override val MethodRef.targetNameBase: String get() = "${this.ref.name}${this.ref.desc}"
	//override val MethodRef.targetNameBase: String get() = "${this.ref.name}"

	override fun genStmRawTry(trap: AstTrap): Indenter = Indenter {
		//line("try {")
		//_indent()
	}

	override fun genStmRawCatch(trap: AstTrap): Indenter = Indenter {
		//_unindent()
		//line("} catch (Throwable e) {")
		//indent {
		//	line("goto ${trap.handler.name};")
		//}
		//line("}")
	}

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (WrappedThrowable J__i__exception__)") {
			line("J__exception__ = J__i__exception__.t;")
			line(stm.catch.genStm())
		}
	}

	override val DoubleNegativeInfinityString = "-double.infinity"
	override val DoublePositiveInfinityString = "double.infinity"
	override val DoubleNanString = "double.nan"

	override val String.escapeString: String get() = "STRINGLIT_${allocString(currentClass, this)}${this.toCommentString()}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "ensureNotNull(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
	}

	override fun escapedConstant(v: Any?, place: ConstantPlace): String = when (v) {
		is Double -> {
			//val isVerySmall = (v in 0.0..4.940656e-324)
			@Suppress("ConvertTwoComparisonsToRangeCheck")
			val isVerySmall = (v >= 0.0 && v <= 4.940656e-324)
			val representable = !isVerySmall
			if (representable) {
				super.escapedConstant(v, place)
			} else {
				"N.longBitsToDouble(cast(long)0x" + java.lang.Long.toHexString(java.lang.Double.doubleToRawLongBits(v)) + "UL)"
			}
			//"N.longBitsToDouble(" + java.lang.Double.doubleToRawLongBits(v) + "L)"
		}
		else -> super.escapedConstant(v, place)
	}

	override fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = indent {
		line("N.monitorEnter(" + stm.expr.genExpr() + ");")
	}

	override fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = indent {
		line("N.monitorExit(" + stm.expr.genExpr() + ");")
	}

	override fun buildStaticInit(clazzName: FqName): String? = null

	//override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "$array.data[$index]"
	//override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String = "$array.data[$index] = $value;"

	//override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "$array.get($index)"
	//override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String = "$array.set($index, $value);"

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "ARRAY_GET($array, $index)"
	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String = "ARRAY_SET($array, $index, $value);"

	override fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "cast(${e.type.targetName})J__exception__"

	override fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String {
		return "checkCast!(${to.targetNameRef})($e)"
	}
}