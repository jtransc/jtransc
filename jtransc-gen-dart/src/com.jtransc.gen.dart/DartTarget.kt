package com.jtransc.gen.dart

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.SwitchFeature
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
class DartTarget : GenTargetDescriptor() {
	override val name = "dart"
	override val longName = "dart"
	override val sourceExtension = "dart"
	override val outputExtension = "dart"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable = true
	override val programFeatures: Set<Class<AstProgramFeature>> = setOf()

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("dart", "dart", "program.dart", minimizeNames = false)
	)

	@Suppress("ConvertLambdaToReference")
	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-dart"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<DartGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"dart" -> "dart"
		else -> null
	}
}

@Singleton
class DartGenerator(injector: Injector) : CommonGenerator(injector) {
	override val TARGET_NAME: String = "DART"
	override val SINGLE_FILE: Boolean = true

	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	//override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	override val methodFeatures = setOf(SwitchFeature::class.java)
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java)
	override val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	override val interfacesSupportStaticMembers: Boolean = false
	override val floatHasFSuffix = false
	override val GENERATE_LINE_NUMBERS = false

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

	// Dart doesn't require compilation
	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		//return CSharpCompiler.genCommand(programFile, debug, libs)
		return listOf()
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		return ProcessResult2(RootLocalVfs().exec(listOf(DartCommand.dart, configTargetFolder.targetFolder["program.dart"].realpathOS), ExecOptions(passthru = redirect, sysexec = true)))
	}

	override fun writeClasses(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeClasses(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter {
		var targetType = field.type.targetName
		//if (field.modifiers.isVolatile) targetType = "shared($targetType)"
		if (field.isStatic) targetType = "static $targetType"

		line("$targetType ${field.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun quoteString(str: String) = str.dquote()

	override fun genSingleFileClasses(output: SyncVfsFile): Indenter = Indenter {
		val StringFqName = buildTemplateClass("java.lang.String".fqname)
		val classesStr = super.genSingleFileClasses(output)
		line(classesStr)
		line("class Bootstrap") {
			for (lit in getGlobalStrings()) {
				line("static $StringFqName ${lit.name} = N.strLitEscape(${lit.str.dquote()});")
			}
			val entryPointFqName = program.entrypoint
			val entryPointClass = program[entryPointFqName]
			line("static void Main(List<String> args)") {
				//line("try {")
				//indent {
				line("N.init();")
				line(genStaticConstructorsSorted())
				//line(buildStaticInit(entryPointFqName))
				val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.Companion.STRING)))]
				line(buildMethod(mainMethod, static = true) + "(N.strArray(args));")
				//}
				//line("} catch (e) {")
				//indent {
				//	line("print(e);")
				//}
				//line("}")
			}
		}
	}

	//override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "($array as ${arrayType.targetNameRef}).data[$index]"
	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "($array).data[$index]"

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		//val actualElementType = if (elementType is AstType.REF) elementType.
		//return "($array as ${arrayType.targetNameRef}).data[$index] = $value;"
		return "$array.data[$index] = $value;"
	}

	//override fun N_i2b(str: String) = "((sbyte)($str))"
	//override fun N_i2c(str: String) = "((ushort)($str))"
	//override fun N_i2s(str: String) = "((short)($str))"
	//override fun N_f2i(str: String) = "((int)($str))"

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
					'$' -> out.append("\\$")
				//in '\u0000'..'\u001f' -> out.append("\\x" + "%02x".format(c.toInt()))
				//in '\u0020'..'\u00ff' -> out.append(c)
					in 'a'..'z', in 'A'..'Z', in '0'..'9', '_', '.', ',', ';', ':', '<', '>', '{', '}', '[', ']', '/', ' ', '=', '!', '%', '&' -> out.append(c)
					else -> out.append("\\u" + "%04x".format(c.toInt()))
				}
			}
			return "\"" + out.toString() + "\""
		} else {
			return "null"
		}
	}


	override fun genMethodDeclModifiers(method: AstMethod): String {
		return if (method.isStatic) "static" else ""
	}

	override fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		return "JA_I${staticAccessOperator}T([" + e.values.joinToString(",") + "])"
	}

	override fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		if (kind.isStatic) {
			return "class ${clazz.name.targetNameForStatic}"
		} else {
			val CLASS = "class"
			val iabstract = if (clazz.isInterface || clazz.isAbstract) "abstract " else ""
			val base = "$iabstract$CLASS ${clazz.name.targetSimpleName}"
			val extends = if (clazz.extending != null) {
				"extends " + clazz.extending!!.targetClassFqName
			} else {
				""
			}
			val implements = if (clazz.implementingUnique.isNotEmpty()) {
				"implements " + clazz.implementingUnique.map { it.targetClassFqName }.joinToString(", ")
			} else {
				""
			}
			return "$base $extends $implements"
		}
	}

	override fun N_f2d(str: String) = "(($str))"
	override fun N_d2f(str: String) = "(($str))"

	override fun N_is(a: String, b: String): String = "(($a) is $b)"

	override val NullType by lazy { AstType.OBJECT.targetName }
	override val VoidType = "void"
	override val BoolType = "bool"
	override val IntType = "int"
	override val ShortType = "int"
	override val CharType = "int"
	override val ByteType = "int"
	override val FloatType = "double"
	override val DoubleType = "double"
	override val LongType = "Int64"

	override val FqName.targetSimpleName: String get() = this.targetName

	//override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(($BaseArrayType)${e.array.genNotNull()}).length"
	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(${e.array.genNotNull()} as JA_0).length"

	//override fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw new WrappedThrowable(${stm.value.genExpr()});")
	override fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw (${stm.exception.genExpr()}).${prepareThrow.targetName}().dartError;")

	override fun genSIMethod(clazz: AstClass): Indenter = Indenter {
		if (clazz.isJavaLangObject) {
			line("String toString()") {
				val toStringMethodName = buildMethod(clazz.getMethodWithoutOverrides("toString")!!, static = false)
				line("return N.istr($toStringMethodName());")
				//line("return 'hello world';")
			}
		}

		if (!clazz.isInterface) {
			if (clazz.isJavaLangObject) {
				line("int __JT__CLASS_ID;")
				line("${clazz.name.targetName}([int CLASS_ID = ${clazz.classId}]) { this.__JT__CLASS_ID = CLASS_ID; }")
			} else {
				line("${clazz.name.targetName}([int CLASS_ID = ${clazz.classId}]) : super(CLASS_ID) { }")
			}
		}
		if (clazz.staticConstructor != null) {
			line("static void SI()") {
				val clazzName = if (clazz.isInterface) clazz.name.targetNameForStatic else clazz.name.targetName
				for (field in clazz.fields.filter { it.isStatic }) {
					line("$clazzName.${field.targetName} = ${field.escapedConstantValue};")
				}
				line(genSIMethodBody(clazz))
			}
		} else {
			line("static void SI() { }")
		}
	}

	//override fun N_i(str: String) = "N.i($str)"
	override fun N_i(str: String) = "($str)"

	override fun N_f2i(str: String) = "N.f2i($str)"
	override fun N_d2i(str: String) = "N.d2i($str)"

	override fun N_c(str: String, from: AstType, to: AstType): String {
		if (to is AstType.REF && to.fqname == "java.lang.Object" && from is AstType.Reference) return str

		if (from is AstType.REF && to is AstType.REF) {
			val fromClass = program[from]!!
			val toClass = program[to]!!
			if (toClass in fromClass.ancestors) return str
			if (toClass in fromClass.allInterfacesInAncestors) return str
		}
		return "($str as ${to.targetNameRef})"
	}

	override fun N_c_eq(l: String, r: String) = "($l == $r)"
	override fun N_c_ne(l: String, r: String) = "($l != $r)"

	override fun N_i2f(str: String) = "N.i2f($str)"
	override fun N_i2d(str: String) = "N.i2d($str)"

	override fun N_j2i(str: String) = "N.j2i($str)"
	override fun N_j2f(str: String) = "N.j2f($str)"
	override fun N_j2d(str: String) = "N.j2d($str)"

	override fun N_ineg(str: String): String = "N.ineg($str)"
	override fun N_iadd(l: String, r: String): String = "N.I($l + $r)"
	override fun N_isub(l: String, r: String): String = "N.I($l - $r)"
	override fun N_imul(l: String, r: String): String = "N.I($l * $r)"
	override fun N_idiv(l: String, r: String): String = "N.I($l ~/ $r)"
	override fun N_irem(l: String, r: String): String = "N.I($l.remainder($r))"
	override fun N_iand(l: String, r: String): String = "N.I($l & $r)"
	override fun N_ior(l: String, r: String): String = "N.I($l | $r)"
	override fun N_ixor(l: String, r: String): String = "N.I($l ^ $r)"

	override fun N_ishl(l: String, r: String): String = "N.ishl($l, $r)"
	override fun N_ishr(l: String, r: String): String = "N.ishr($l, $r)"
	override fun N_iushr(l: String, r: String) = "N.iushr($l, $r)"

	override fun N_ishl_cst(l: String, r: Int): String = "N.I($l << $r)"
	override fun N_ishr_cst(l: String, r: Int): String = "N.I($l >> $r)"
	override fun N_iushr_cst(l: String, r: Int): String = "N.iushr_opt($l, $r)"

	//override fun N_ishl(l: String, r: String): String = "N.I($l << $r)"
	//override fun N_ishr(l: String, r: String): String = "N.I($l >> $r)"
	//override fun N_iushr(l: String, r: String) = "N.iushr($l, $r)"

	//override fun N_lnew(value: Long): String = "N.lnew($value)"
	//override fun N_lneg(str: String): String = "N.lneg($str)"
	//override fun N_ladd(l: String, r: String): String = "N.ladd($l, $r)"
	//override fun N_lsub(l: String, r: String): String = "N.lsub($l, $r)"
	//override fun N_lmul(l: String, r: String): String = "N.lmul($l, $r)"
	//override fun N_ldiv(l: String, r: String): String = "N.ldiv($l, $r)"
	//override fun N_lrem(l: String, r: String): String = "N.lrem($l, $r)"
	//override fun N_land(l: String, r: String): String = "N.land($l, $r)"
	//override fun N_lor(l: String, r: String): String = "N.lor($l, $r)"
	//override fun N_lxor(l: String, r: String): String = "N.lxor($l, $r)"
	//override fun N_lshl(l: String, r: String): String = "N.lshl($l, $r)"
	//override fun N_lshr(l: String, r: String): String = "N.lshr($l, $r)"
	//override fun N_lushr(l: String, r: String) = "N.lushr($l, $r)"


	override fun N_lnew(value: Long): String = "N.lnew($value)"
	override fun N_lneg(str: String): String = "(-($str))"
	override fun N_linv(str: String): String = "(~($str))"
	override fun N_ladd(l: String, r: String): String = "($l+$r)"
	override fun N_lsub(l: String, r: String): String = "($l-$r)"
	override fun N_lmul(l: String, r: String): String = "($l*$r)"
	override fun N_ldiv(l: String, r: String): String = "($l~/$r)"
	override fun N_lrem(l: String, r: String): String = "N.lrem($l, $r)"
	override fun N_land(l: String, r: String): String = "($l&$r)"
	override fun N_lor(l: String, r: String): String = "($l|$r)"
	override fun N_lxor(l: String, r: String): String = "($l^$r)"

	override fun N_lshl(l: String, r: String): String = "N.lshl($l, $r)"
	override fun N_lshr(l: String, r: String): String = "N.lshr($l, $r)"
	override fun N_lushr(l: String, r: String) = "N.lushr($l, $r)"

	override fun N_lshl_cst(l: String, r: Int): String = "($l << $r)"
	override fun N_lshr_cst(l: String, r: Int): String = "($l >> $r)"
	override fun N_lushr_cst(l: String, r: Int) = "N.lushr_opt($l, $r)"

	override fun genMissingBody(method: AstMethod): Indenter = Indenter {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw new Exception(${message.dquote()});")
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
		//line("on TypeError catch (e)") {
		//	line("print('TypeError! CATCHED');")
		//}
		line("catch (J__i__exception__)") {
			line("J__exception__ = N.getJavaException(J__i__exception__);")
			line(stm.catch.genStm())
		}
	}

	override fun genStmRethrow(stm: AstStm.RETHROW, last: Boolean) = Indenter("""rethrow;""")

	//override fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "(${e.type.targetName})J__exception__"
	override fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "J__exception__"

	//override fun N_c_ushr(l: String, r: String) = "(int)(((uint)($l)) >> $r)"

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(\"$desc\", [${e.counts.map { it.genExpr() }.joinToString(", ")}])"
	}

	override val DoubleNegativeInfinityString = "double.NEGATIVE_INFINITY"
	override val DoublePositiveInfinityString = "double.INFINITY"
	override val DoubleNanString = "N.DOUBLE_NAN"

	override val String.escapeString: String get() = "Bootstrap.STRINGLIT_${allocString(currentClass, this)}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
	}

	override fun N_i2b(str: String) = "N.i2b($str)"
	override fun N_i2s(str: String) = "N.i2s($str)"
	override fun N_i2c(str: String) = "N.i2c($str)"
	override fun N_i2j(str: String) = "N.i2j($str)"

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

	override fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>, isNativeCall: Boolean): String {
		return "super$methodAccess(${args.joinToString(", ")})"
	}

	override fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = Indenter("N.monitorEnter(" + stm.expr.genExpr() + ");")
	override fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = Indenter("N.monitorExit(" + stm.expr.genExpr() + ");")

	override fun buildStaticInit(clazzName: FqName): String? = null

	override fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter {
		line("${stm.array.genExpr()}.setArraySlice(${stm.startIndex}, [ ${stm.values.map { it.genExpr() }.joinToString(", ")} ]);")
	}

	override fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String {
		if (from == to) return e;
		if (from is AstType.NULL) return e
		//return "N.CHECK_CAST($e, ${to.targetNameRef})"
		return "(($e) as ${to.targetNameRef})"
	}
}