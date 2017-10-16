package com.jtransc.gen.php

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
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
import com.jtransc.text.quote
import com.jtransc.vfs.*
import java.io.File

class PhpTarget : GenTargetDescriptor() {
	override val name = "php"
	override val longName = "php"
	override val sourceExtension = "php"
	override val outputExtension = "php"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable = true
	override val programFeatures: Set<Class<AstProgramFeature>> = setOf()

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("php", "php", "program.php", minimizeNames = false)
	)

	@Suppress("ConvertLambdaToReference")
	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-php"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<PhpGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"php" -> "php"
		else -> null
	}
}

@Singleton
class PhpGenerator(injector: Injector) : CommonGenerator(injector) {
	override val TARGET_NAME: String = "PHP"
	override val SINGLE_FILE: Boolean = true

	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val staticAccessOperator: String = "::"
	override val instanceAccessOperator: String = "->"

	override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	//override val methodFeatures = setOf(SwitchFeature::class.java)
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java)
	override val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	override val interfacesSupportStaticMembers: Boolean = false
	override val localVarPrefix = "\$"
	override val floatHasFSuffix: Boolean = false

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

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = listOf(outputFile)

		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")

		val cmdAndArgs = listOf("php", outFile.realpathOS)

		return ProcessResult2(RootLocalVfs().exec(cmdAndArgs, ExecOptions(passthru = redirect, sysexec = true)))
	}

	override fun writeClasses(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeClasses(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter {
		val static = if (field.isStatic) "static " else ""
		line("public $static\$${field.targetName} = ${field.type.getNull().escapedConstantField};")
	}

	override fun staticAccess(name: String, field: Boolean): String = if (field) "$staticAccessOperator\$$name" else "$staticAccessOperator$name"

	override fun quoteString(str: String) = str.dquote()

	override fun genSingleFileClasses(output: SyncVfsFile): Indenter = Indenter {
		val classesStr = super.genSingleFileClasses(output)
		line(classesStr)
		line("class Bootstrap") {
			for (lit in getGlobalStrings()) {
				line("static \$${lit.name};")
			}
			line("static public function __initStrings()") {
				for (lit in getGlobalStrings()) {
					// STRINGLIT_
					line("Bootstrap::\$${lit.name} = N::str(${lit.str.dquote()});")
				}
			}
			val entryPointFqName = program.entrypoint
			val entryPointClass = program[entryPointFqName]
			line("static public function main(array \$args)") {
				line("try {")
				indent {
					line("N::init();")
					line("Bootstrap::__initStrings();")
					line(genStaticConstructorsSorted())
					//line(buildStaticInit(entryPointFqName))
					val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.STRING)))]
					line(buildMethod(mainMethod, static = true) + "(N::strArray(\$args));")
				}
				line("} catch (WrappedThrowable \$e) {")
				indent {
					line("echo \$e->t, \"\\n\";")
					line("echo \$e, \"\\n\";")
				}
				line("} catch (Throwable \$e) {")
				indent {
					line("echo \$e, \"\\n\";")
				}
				line("}")
			}
		}
		line("Bootstrap::main([]);")
	}

	//override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String = "$array[$index] = $value;"

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "($array->get($index))"
	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String) = "$array->set($index, $value);"

	override fun N_i2b(str: String) = "(N::i2b($str))"
	override fun N_i2c(str: String) = "(N::i2c($str))"
	override fun N_i2s(str: String) = "(N::i2s($str))"
	override fun N_f2i(str: String) = "(N::f2i($str))"

	//fun String?.dquote(): String = if (this != null) "\"${this.escape()}\"" else "null"

	fun String?.dquote(): String {
		if (this != null) {
			val bb = this.toByteArray(Charsets.UTF_8)
			val out = StringBuilder()

			for (b in bb) {
				val c = b.toChar()
				when (c) {
					'\u0000' -> out.append("\\0")
					'\\' -> out.append("\\\\")
				//'\'' -> out.append("\\\'")
					'"' -> out.append("\\\"")
					'$' -> out.append("\\\$")
					in ' '..'~' -> out.append(c)
					else -> out.append("\\x%02x".format(c.toInt() and 0xFF))
				}

			}

			return "\"" + out.toString() + "\""
		} else {
			return "null"
		}
	}

	override val AstLocal.decl: String get() = "\$${this.targetName} = ${this.type.nativeDefaultString};"

	override fun genExprParam(e: AstExpr.PARAM) = "\$" + e.argument.targetName

	override fun actualSetLocal(stm: AstStm.SET_LOCAL, localName: String, exprStr: String) = "\$$localName = $exprStr;"

	override val AstArgument.decl: String get() = "${this.type.targetNameNullable} \$${this.targetName}"

	override fun genMetodDecl(method: AstMethod): String {
		val args = method.methodType.args.map { it.decl }
		val static = if (method.isStatic) "static " else ""
		return "public ${static}function ${method.targetName}(${args.joinToString(", ")})"
	}

	override fun genExprThis(e: AstExpr.THIS): String = "\$this"

	override fun genMethodDeclModifiers(method: AstMethod): String {
		if (method.containingClass.isInterface) {
			return if (method.isStatic) "static" else ""
		} else {
			var mods = super.genMethodDeclModifiers(method)
			//if (method.isStatic && (method.isOverriding || method.isClassInit)) mods += "new "
			//if (!method.isStatic && !method.targetIsOverriding) mods += "virtual "
			mods += "public "
			return mods
		}
	}

	override fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		return "JA_I${staticAccessOperator}T([" + e.values.joinToString(",") + "])"
	}

	override fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		return if (kind.isStatic) {
			"class ${clazz.name.targetNameForStatic}"
		} else {
			super.genClassDecl(clazz, kind)
		}
	}

	override fun genClassDeclExtends(clazz: AstClass, kind: MemberTypes): String {
		return if (clazz.isInterface) {
			if (clazz.implementing.isNotEmpty()) " extends ${clazz.implementing.map { it.targetClassFqName }.joinToString(", ")}" else ""
		} else {
			super.genClassDeclExtends(clazz, kind)
		}
	}

	override fun genClassDeclImplements(clazz: AstClass, kind: MemberTypes): String {
		return if (clazz.isInterface) {
			""
		} else {
			super.genClassDeclImplements(clazz, kind)
		}
	}


	override fun N_f2d(str: String) = "((double)($str))"
	override fun N_d2f(str: String) = "((double)($str))"
	override fun N_d2j(str: String) = "N::d2j($str)"

	override fun N_is(a: String, b: String): String = "(($a) instanceof $b)"

	//override val FqName.targetName: String get() = "?" + super.targetName

	override val NullType by lazy { AstType.OBJECT.targetName }
	override val VoidType = "void"
	override val BoolType = "bool"
	override val IntType = "int"
	override val ShortType = "int"
	override val CharType = "int"
	override val ByteType = "int"
	override val FloatType = "float"
	override val DoubleType = "float"
	override val LongType = "Int64"

	override val FqName.targetSimpleName: String get() = this.targetName

	//override fun N_c(str: String, from: AstType, to: AstType) = "((${to.targetName})($str))"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(${e.array.genNotNull()})->length"
	override fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw new WrappedThrowable(${stm.exception.genExpr()});")

	override fun genLabel(label: AstLabel) = "${label.name}:"

	override fun genSIMethod(clazz: AstClass): Indenter = Indenter {
		if (clazz.isJavaLangObject) {
			line("public function __toString()") {
				val toStringMethodName = buildMethod(clazz.getMethodWithoutOverrides("toString")!!, static = false)
				//line("try { return N::istr(\$this->$toStringMethodName()); } catch (Throwable \$t) { return '__toString.ERROR:' . \$t; }")
				line("try { return N::istr(\$this->$toStringMethodName()); } catch (WrappedThrowable \$t) { echo \$t->t; return '__toString.ERROR:' . \$t; } catch (Throwable \$t) { echo \$t; return '__toString.ERROR:' . \$t; }")
				//line("return N::istr(\$this->$toStringMethodName());")
			}
		}

		if (!clazz.isInterface) {
			if (clazz.isJavaLangObject) line("public \$__JT__CLASS_ID;")
			line("public function __construct(\$CLASS_ID = ${clazz.classId})") {
				if (clazz.isJavaLangObject) {
					line("\$this->__JT__CLASS_ID = \$CLASS_ID;")
				} else {
					line("parent::__construct(\$CLASS_ID);")
				}
				for (field in clazz.fieldsInstance) {
					line("\$this->${field.targetName} = ${field.escapedConstantValueLocal};")
				}
			}
		}
		line("static public function SI()") {
			val clazzName = if (clazz.isInterface) clazz.name.targetNameForStatic else clazz.name.targetName
			for (field in clazz.fieldsStatic) {
				line("$clazzName::\$${field.targetName} = ${field.escapedConstantValueLocal};")
			}
			if (clazz.staticConstructor != null) {
				line(genSIMethodBody(clazz))
			}
		}
	}

	//override fun N_i(str: String) = "(($str)|0)"
	override fun N_i(str: String) = "((int)($str))"

	override fun N_d2i(str: String) = "N::d2i($str)"

	override fun N_c_eq(l: String, r: String) = "($l == $r)"
	override fun N_c_ne(l: String, r: String) = "($l != $r)"

	override fun N_i2f(str: String) = "((double)($str))"
	override fun N_i2d(str: String) = "((double)($str))"

	override fun N_j2f(str: String) = "(N::j2d($str))"
	override fun N_j2d(str: String) = "(N::j2d($str))"

	//override fun N_c_div(l: String, r: String) = "unchecked($l / $r)"

	override fun N_imul(l: String, r: String): String = "N::imul($l, $r)"
	override fun N_idiv(l: String, r: String): String = "N::idiv($l, $r)"
	override fun N_irem(l: String, r: String): String = "N::irem($l, $r)"

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

	override fun genBodyTrapsPrefix() = Indenter("\$J__exception__ = null;")
	override fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "\$J__exception__"

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (WrappedThrowable \$J__i__exception__)") {
			line("\$J__exception__ = \$J__i__exception__->t;")
			line(stm.catch.genStm())
		}
	}

	override fun genStmRethrow(stm: AstStm.RETHROW, last: Boolean) = Indenter("throw \$J__i__exception__;")

	override fun N_ishl(l: String, r: String) = "N::ishl($l, $r)"
	override fun N_ishr(l: String, r: String) = "N::ishr($l, $r)"
	override fun N_iushr(l: String, r: String) = "N::iushr($l, $r)"

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(\"$desc\", [${e.counts.map { it.genExpr() }.joinToString(", ")}])"
	}

	override val DoubleNegativeInfinityString = "N::\$DOUBLE_NEGATIVE_INFINITY"
	override val DoublePositiveInfinityString = "N::\$DOUBLE_POSITIVE_INFINITY"
	override val DoubleNanString = "N::\$DOUBLE_NAN"

	override val FloatNegativeInfinityString = "N::\$FLOAT_NEGATIVE_INFINITY"
	override val FloatPositiveInfinityString = "N::\$FLOAT_POSITIVE_INFINITY"
	override val FloatNanString = "N::\$FLOAT_NAN"

	override val String.escapeString: String get() = "Bootstrap::\$STRINGLIT_${allocString(currentClass, this)}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
	}

	override fun escapedConstant(v: Any?, place: ConstantPlace): String = when (v) {
	//is Float -> if (v.isInfinite()) if (v < 0) NegativeInfinityString else PositiveInfinityString else if (v.isNaN()) NanString else "${v}f"
	//is Long -> "null"
		is Long -> {
			if (place == ConstantPlace.FIELD) "null" else "Int64::make(${(v shr 32).toInt()}, ${(v shr 0).toInt()})"
		}
		else -> super.escapedConstant(v, place)
	}

	override fun getClassInterfaces(clazz: AstClass): List<FqName> = clazz.implementingNormalized

	val AstType.targetNameNullable get() = if (this.isReference()) "?$targetName" else targetName

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
		val methodName = getTargetMethodAccess(program[method]!!, static = true)
		return "parent$methodName(${args.joinToString(", ")})"
	}

	override fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = indent {
		line("N::monitorEnter(" + stm.expr.genExpr() + ");")
	}

	override fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = indent {
		line("N::monitorExit(" + stm.expr.genExpr() + ");")
	}

	override fun buildStaticInit(clazzName: FqName): String? = null

	override fun genStmContinue(stm: AstStm.CONTINUE) = indent {
		var count = 1;
		for (n in flowBlocks.size - 1 downTo 0) {
			if (flowBlocks[n] == FlowKind.SWITCH) {
				count++
			} else {
				break
			}
		}
		line("continue $count;")
	}

	override fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String {
		return "N::checkcast($e, ${to.targetName.quote()})"
	}
}