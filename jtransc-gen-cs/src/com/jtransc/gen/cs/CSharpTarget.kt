package com.jtransc.gen.cs

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
class CSharpGenerator(injector: Injector) : CommonGenerator(injector) {
	override val SINGLE_FILE: Boolean = true

	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java)
	override val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	override val interfacesSupportStaticMembers: Boolean = false
	override val floatHasFPrefix = true

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

	override val fixencoding = false

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return CSharpCompiler.genCommand(programFile, debug, libs)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = if (JTranscSystem.isWindows()) {
			listOf("program.exe", "a.exe")
		} else {
			listOf("program.exe", "program", "program.out", "a", "a.out")
		}
		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")

		val cmdAndArgs = if (JTranscSystem.isWindows()) {
			listOf(outFile.realpathOS)
		} else {
			listOf("mono", outFile.realpathOS)
		}

		return ProcessResult2(RootLocalVfs().exec(cmdAndArgs, ExecOptions(passthru = redirect, sysexec = true)))
	}

	override fun writeClasses(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeClasses(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter.gen {
		var targetType = field.type.targetName
		//if (field.modifiers.isVolatile) targetType = "shared($targetType)"
		if (field.isStatic) targetType = "static $targetType"

		line("public $targetType ${field.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun quoteString(str: String) = str.dquote()

	override fun genSingleFileClasses(output: SyncVfsFile): Indenter = Indenter.gen {
		val StringFqName = buildTemplateClass("java.lang.String".fqname)
		val classesStr = super.genSingleFileClasses(output)
		line(classesStr)
		line("class Bootstrap") {
			for (lit in getGlobalStrings()) {
				line("static public $StringFqName ${lit.name} = N.strLitEscape(${lit.str.dquote()});")
			}
			val entryPointFqName = program.entrypoint
			val entryPointClass = program[entryPointFqName]
			line("static void Main(string[] args)") {
				line("try {")
				indent {
					line("N.init();")
					line(genStaticConstructorsSorted())
					//line(buildStaticInit(entryPointFqName))
					val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.Companion.STRING)))]
					line(buildMethod(mainMethod, static = true) + "(N.strArray(args));")
				}
				line("} catch (WrappedThrowable e) {")
				indent {
					line("Console.WriteLine(e.t.ToString());")
					line("Console.WriteLine(e.ToString());")
				}
				line("} catch (Exception e) {")
				indent {
					line("Console.WriteLine(e.ToString());")
				}
				line("}")
			}
		}
	}

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "$array.data[$index]"

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		if (elementType is AstType.Primitive) {
			return "$array.data[$index] = (${elementType.targetName})$value;"
		} else {
			return "$array.data[$index] = (${AstType.OBJECT.targetName})$value;"
		}
	}

	override fun N_i2b(str: String) = "((sbyte)($str))"
	override fun N_i2c(str: String) = "((ushort)($str))"
	override fun N_i2s(str: String) = "((short)($str))"
	override fun N_f2i(str: String) = "((int)($str))"

	//fun String?.dquote(): String = if (this != null) "\"${this.escape()}\"" else "null"

	fun String?.dquote(): String {
		if (this != null) {
			val out = StringBuilder()
			for (n in 0 until this.length) {
				val c = this[n]
				when (c) {
					'\\' -> out.append("\\\\")
					'\'' -> out.append("\\\'")
					'"' -> out.append("\\\"")
					'\n' -> out.append("\\n")
					'\r' -> out.append("\\r")
					'\t' -> out.append("\\t")
				//in '\u0000'..'\u001f' -> out.append("\\x" + "%02x".format(c.toInt()))
				//in '\u0020'..'\u00ff' -> out.append(c)
					in 'a' .. 'z', in 'A' .. 'Z', in '0' .. '9', '_', '.', ',', ';', ':', '<', '>', '{', '}', '[', ']', '/', ' ', '=', '!', '%', '$', '&' -> out.append(c)
					else -> out.append("\\u" + "%04x".format(c.toInt()))
				}
			}
			return "\"" + out.toString() + "\""
		} else {
			return "null"
		}
	}


	override fun genMethodDeclModifiers(method: AstMethod): String {
		if (method.containingClass.isInterface) {
			return if (method.isStatic) "static" else ""
		} else {
			var mods = super.genMethodDeclModifiers(method)
			if (method.isStatic && (method.isOverriding || method.isClassInit)) mods += "new "
			if (!method.isStatic && !method.targetIsOverriding && !method.isInstanceInit) mods += "virtual "
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

	override fun N_f2d(str: String) = "((double)($str))"
	override fun N_d2f(str: String) = "((float)($str))"

	override fun N_is(a: String, b: String): String = "(($a) is $b)"

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
				val clazzName = if (clazz.isInterface) clazz.name.targetNameForStatic else clazz.name.targetName
				for (field in clazz.fields.filter { it.isStatic }) {
					line("$clazzName.${field.targetName} = ${field.escapedConstantValue};")
				}
				line(genSIMethodBody(clazz))
			}
		} else {
			line("static public void SI() { }")
		}
	}

	override fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter = Indenter {
		line("unchecked") {
			line(super.genBody2WithFeatures(method, body))
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

	//override fun N_c_div(l: String, r: String) = "unchecked($l / $r)"

	override fun N_idiv(l: String, r: String): String = "N.idiv($l, $r)"
	override fun N_irem(l: String, r: String): String = "N.irem($l, $r)"

	override fun N_lnew(value: Long): String = "((long)(${value}L))"

	override fun genMissingBody(method: AstMethod): Indenter = Indenter.gen {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw new Exception(${message.dquote()});")
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

	//override fun N_c_ushr(l: String, r: String) = "(int)(((uint)($l)) >> $r)"
	override fun N_c_ushr(l: String, r: String) = "N.iushr($l, $r)"

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(\"$desc\", ${e.counts.map { it.genExpr() }.joinToString(", ")})"
	}

	override val DoubleNegativeInfinityString = "Double.NegativeInfinity"
	override val DoublePositiveInfinityString = "Double.PositiveInfinity"
	//override val NanString = "Double.NaN"
	override val DoubleNanString = "N.DoubleNaN"

	override val FloatNegativeInfinityString = "Single.NegativeInfinity"
	override val FloatPositiveInfinityString = "Single.PositiveInfinity"
	override val FloatNanString = "N.FloatNaN"

	override val String.escapeString: String get() = "Bootstrap.STRINGLIT_${allocString(currentClass, this)}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
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

	override fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter.gen {
		line("${stm.array.genExpr()}.setArraySlice(${stm.startIndex}, new ${stm.elementType.targetName}[] { ${stm.values.map { it.genExpr() }.joinToString(", ")} });")
	}


}