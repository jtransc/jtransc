package com.jtransc.gen.d

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.JTranscSystem
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.text.Indenter
import com.jtransc.text.escape
import com.jtransc.text.quote
import com.jtransc.vfs.*
import java.io.File

// Supports GOTO keyword
// Supports static fields and methods on interfaces
class CSharpTarget : GenTargetDescriptor() {
	override val name = "cs"
	override val longName = "csharp"
	override val sourceExtension = "cs"
	override val outputExtension = "exe"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable = true
	override val programFeatures: Set<Class<AstProgramFeature>> = setOf()

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("cs", "cs", "program.cs", minimizeNames = false)
	)

	@Suppress("ConvertLambdaToReference")
	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-cs"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<CSharpGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"exe" -> "cpp"
		"bin" -> "cpp"
		else -> null
	}
}

@Singleton
class CSharpGenerator(injector: Injector) : SingleFileCommonGenerator(injector) {
	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java)
	override val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	override val interfacesSupportStaticMembers: Boolean = false

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
		"__FILE__", "__FILE_FULL_PATH__", "__MODULE__", "__LINE__", "__FUNCTION__", "__PRETTY_FUNCTION__", "static", "__traits", "__vector", "__parameters",

		// Known Object symbols
		"clone", "toString",
		"std", "core"
	)

	override val languageRequiresDefaultInSwitch = true
	override val defaultGenStmSwitchHasBreaks = true

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return CSharpCompiler.genCommand(programFile, debug, libs)
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

	override fun writeProgram(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeProgram(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter.gen {
		var targetType = field.type.targetName
		//if (field.modifiers.isVolatile) targetType = "shared($targetType)"
		if (field.isStatic) targetType = "static $targetType"

		line("public $targetType ${field.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun genClasses(output: SyncVfsFile): Indenter = Indenter.gen {
		val StringFqName = buildTemplateClass("java.lang.String".fqname)
		val classesStr = super.genClasses(output)
		line(classesStr)
		line("class Bootstrap") {
			for (lit in getGlobalStrings()) {
				line("static public $StringFqName ${lit.name};")
			}
			line("static public void __initStrings()") {
				for (lit in getGlobalStrings()) {
					// STRINGLIT_
					line("${lit.name} = N.strLitEscape(${lit.str.dquote()});")
				}
			}
			val entryPointFqName = program.entrypoint
			val entryPointClass = program[entryPointFqName]
			line("static void Main(string[] args)") {
				line("try {")
				indent {
					line("N.init();")
					line("__initStrings();")
					line(genStaticConstructorsSorted())
					//line(buildStaticInit(entryPointFqName))
					val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.STRING)))]
					line(buildMethod(mainMethod, static = true) + "(N.strArray(args));")
				}
				line("} catch (Exception e) {")
				indent {
					line("Console.WriteLine(e.ToString());")
				}
				line("}")
			}
		}
	}

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String) = "$array[$index] = (${elementType.targetName})$value;"

	override fun N_i2b(str: String) = "((sbyte)($str))"
	override fun N_i2c(str: String) = "((ushort)($str))"
	override fun N_i2s(str: String) = "((short)($str))"
	override fun N_f2i(str: String) = "((int)($str))"

	fun String?.dquote(): String = if (this != null) "\"${this.escape()}\"" else "null"

	override fun genMetodDeclModifiers(method: AstMethod): String {
		if (method.containingClass.isInterface) {
			return if (method.isStatic) "static" else ""
		} else {
			var mods = super.genMetodDeclModifiers(method)
			if (method.isStatic && (method.isOverriding || method.isClassInit)) mods += "new "
			if (!method.isStatic && !method.targetIsOverriding) mods += "virtual "
			mods += "public "
			return mods
		}
	}

	override fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		return "JA_I${staticAccessOperator}T(new int[] { " + e.values.joinToString(",") + " })"
	}

	override fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		if (kind.isStatic) {
			return "class ${clazz.name.targetNameForStatic}"
		} else {
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
	}

	override fun N_is(a: String, b: String): String = "((($b)$a) != null)"

	override val NullType by lazy { AstType.OBJECT.targetName }
	override val VoidType = "void"
	override val BoolType = "bool"
	override val IntType = "int"
	override val ShortType = "short"
	override val CharType = "ushort"
	override val ByteType = "sbyte"
	override val FloatType = "float"
	override val DoubleType = "double"
	override val LongType = "long"

	override val FqName.targetSimpleName: String get() = this.targetName

	override fun N_c(str: String, from: AstType, to: AstType) = "((${to.targetName})($str))"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(($BaseArrayType)${e.array.genNotNull()}).length"
	override fun genStmThrow(stm: AstStm.THROW) = Indenter("throw new WrappedThrowable(${stm.value.genExpr()});")

	override fun genStmLabelCore(stm: AstStm.STM_LABEL) = "${stm.label.name}:"

	override fun genSIMethod(clazz: AstClass): Indenter = Indenter.gen {
		if (clazz.isJavaLangObject) {
			line("override public string ToString()") {
				val toStringMethodName = buildMethod(clazz.getMethodWithoutOverrides("toString")!!, static = false)
				line("return N.istr($toStringMethodName());")
			}
		}

		if (!clazz.isInterface) {
			if (clazz.isJavaLangObject) {
				line("public int __CS__CLASS_ID;")
				line("public ${clazz.name.targetName}(int CLASS_ID = ${clazz.classId}) { this.__CS__CLASS_ID = CLASS_ID; }")
			} else {
				line("public ${clazz.name.targetName}(int CLASS_ID = ${clazz.classId}) : base(CLASS_ID) { }")
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

	//override fun N_i(str: String) = "((int)($str))"
	override fun N_i(str: String) = "($str)"

	//override fun N_f2i(str: String) = "((int)($str))"
	override fun N_d2i(str: String) = "((int)($str))"
	override fun N_c_eq(l: String, r: String) = "($l == $r)"
	override fun N_c_ne(l: String, r: String) = "($l != $r)"

	override fun N_i2f(str: String) = "((float)($str))"
	override fun N_i2d(str: String) = "((double)($str))"

	override fun N_l2f(str: String) = "((float)($str))"
	override fun N_l2d(str: String) = "((double)($str))"

	override fun N_lnew(value: Long): String = "((long)(${value}L))"

	override fun genMissingBody(method: AstMethod): Indenter = Indenter.gen {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw new Exception(${message.quote()});")
	}

	//override val MethodRef.targetNameBase: String get() = "${this.ref.name}${this.ref.desc}"
	//override val MethodRef.targetNameBase: String get() = "${this.ref.name}"

	override fun genStmRawTry(trap: AstTrap): Indenter = Indenter.gen {
		//line("try {")
		//_indent()
	}

	override fun genStmRawCatch(trap: AstTrap): Indenter = Indenter.gen {
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

	override protected fun N_c_ushr(l: String, r: String) = "(int)(((uint)($l)) >> $r)"

	override val NegativeInfinityString = "-double.infinity"
	override val PositiveInfinityString = "double.infinity"
	override val NanString = "double.nan"

	override val String.escapeString: String get() = "Bootstrap.STRINGLIT_${allocString(currentClass, this)}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
	}

	override fun escapedConstant(v: Any?): String = when (v) {
		is Float -> if (v.isInfinite()) if (v < 0) NegativeInfinityString else PositiveInfinityString else if (v.isNaN()) NanString else "${v}f"
		else -> super.escapedConstant(v)
	}


		//override fun escapedConstant(v: Any?): String = when (v) {
	//	is Double -> {
	//		val isVerySmall = (v >= 0.0 && v <= 4.940656e-324)
	//		val representable = !isVerySmall
	//		if (representable) {
	//			super.escapedConstant(v)
	//		} else {
	//			"N.longBitsToDouble((long)0x" + java.lang.Long.toHexString(java.lang.Double.doubleToRawLongBits(v)) + "UL)"
	//		}
	//		//"N.longBitsToDouble(" + java.lang.Double.doubleToRawLongBits(v) + "L)"
	//	}
	//	else -> super.escapedConstant(v)
	//}

	override fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		return "base$methodAccess(${args.joinToString(", ")})"
	}

	override fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = indent {
		line("N.monitorEnter(" + stm.expr.genExpr() + ");")
	}

	override fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = indent {
		line("N.monitorExit(" + stm.expr.genExpr() + ");")
	}

	override fun buildStaticInit(clazzName: FqName): String? = null
}