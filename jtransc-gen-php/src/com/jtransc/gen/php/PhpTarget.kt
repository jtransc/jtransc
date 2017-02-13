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
class PhpGenerator(injector: Injector) : SingleFileCommonGenerator(injector) {
	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val staticAccessOperator: String = "::"
	override val instanceAccessOperator: String = "->"

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

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = listOf(outputFile)

		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")

		val cmdAndArgs = listOf("php", outFile.realpathOS)

		return ProcessResult2(RootLocalVfs().exec(cmdAndArgs, ExecOptions(passthru = redirect, sysexec = true)))
	}

	override fun writeProgram(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeProgram(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter.gen {
		val static = if (field.isStatic) "static " else ""
		line("public $static\$${field.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun buildStaticAccessName(name: String, field: Boolean): String = if (field) "$staticAccessOperator\$$name" else "$staticAccessOperator$name"

	override fun quoteString(str: String) = str.dquote()

	override fun genClasses(output: SyncVfsFile): Indenter = Indenter.gen {
		val classesStr = super.genClasses(output)
		line(classesStr)
		line("class Bootstrap") {
			for (lit in getGlobalStrings()) {
				line("static \$${lit.name};")
			}
			line("static public function __initStrings()") {
				for (lit in getGlobalStrings()) {
					// STRINGLIT_
					line("Bootstrap::\$${lit.name} = N::strLitEscape(${lit.str.dquote()});")
				}
			}
			val entryPointFqName = program.entrypoint
			val entryPointClass = program[entryPointFqName]
			line("static public function main(\$args)") {
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
					line("echo \$e->t;")
					line("echo \$e;")
				}
				line("} catch (Exception \$e) {")
				indent {
					line("echo \$e;")
				}
				line("}")
			}
		}
		line("Bootstrap::main(array());")
	}

	//override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String = "$array[$index] = $value;"

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "($array->get($index))"
	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String) = "$array->set($index, $value);"

	override fun N_i2b(str: String) = "(N::i2b($str))"
	override fun N_i2c(str: String) = "(N::i2c($str))"
	override fun N_i2s(str: String) = "(N::i2s($str))"
	override fun N_f2i(str: String) = "(($str)|0)"

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
					//else -> out.append("\\u" + "%04x".format(c.toInt()))
					else -> out.append(c)
				}
			}
			return "'" + out.toString() + "'"
		} else {
			return "null"
		}
	}

	override fun genBodyLocal(local: AstLocal): Indenter = Indenter("\$${local.targetName} = ${local.type.nativeDefaultString};")

	override fun genExprParam(e: AstExpr.PARAM) = "\$" + e.argument.targetName
	override fun genExprLocal(e: AstExpr.LOCAL) = "\$" + e.local.targetName

	override fun actualSetLocal(stm: AstStm.SET_LOCAL, localName: String, exprStr: String) = "\$$localName = $exprStr;"

	override val AstArgument.argDecl: String get() = "\$${this.targetName}"

	override fun genMetodDecl(method: AstMethod): String {
		val args = method.methodType.args.map { it.argDecl }
		val static = if (method.isStatic) "static " else ""
		return "public ${static}function ${method.targetName}(${args.joinToString(", ")})"
	}

	override fun genExprThis(e: AstExpr.THIS): String = "\$this"

	override fun genMetodDeclModifiers(method: AstMethod): String {
		if (method.containingClass.isInterface) {
			return if (method.isStatic) "static" else ""
		} else {
			var mods = super.genMetodDeclModifiers(method)
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


	override fun N_f2d(str: String) = "(+($str))"
	override fun N_d2f(str: String) = "(+($str))"

	override fun N_is(a: String, b: String): String = "(($a) instanceof $b)"

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

	//override fun N_c(str: String, from: AstType, to: AstType) = "((${to.targetName})($str))"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(${e.array.genNotNull()})->length"
	override fun genStmThrow(stm: AstStm.THROW) = Indenter("throw new WrappedThrowable(${stm.value.genExpr()});")

	override fun genStmLabelCore(stm: AstStm.STM_LABEL) = "${stm.label.name}:"

	override fun genSIMethod(clazz: AstClass): Indenter = Indenter.gen {
		if (clazz.isJavaLangObject) {
			line("public function __toString()") {
				val toStringMethodName = buildMethod(clazz.getMethodWithoutOverrides("toString")!!, static = false)
				line("return N::istr(\$this->$toStringMethodName());")
			}
		}

		if (!clazz.isInterface) {
			if (clazz.isJavaLangObject) {
				line("public \$__PHP__CLASS_ID;")
				line("public function __construct(\$CLASS_ID = ${clazz.classId}) { \$this->__PHP__CLASS_ID = \$CLASS_ID; }")
			} else {
				line("public function __construct(\$CLASS_ID = ${clazz.classId}) { parent::__construct(\$CLASS_ID); }")
			}
		}
		if (clazz.staticConstructor != null) {
			line("static public function SI()") {
				val clazzName = if (clazz.isInterface) clazz.name.targetNameForStatic else clazz.name.targetName
				for (field in clazz.fields.filter { it.isStatic }) {
					line("$clazzName::\$${field.targetName} = ${field.escapedConstantValue};")
				}
				line(genSIMethodBody(clazz))
			}
		} else {
			line("static public function SI() { }")
		}
	}

	override fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter = Indenter {
		line(super.genBody2WithFeatures(method, body))
	}

	override fun N_i(str: String) = "($str)"
	override fun N_d2i(str: String) = "(($str)|0)"

	override fun N_c_eq(l: String, r: String) = "($l == $r)"
	override fun N_c_ne(l: String, r: String) = "($l != $r)"

	override fun N_i2f(str: String) = "(+($str))"
	override fun N_i2d(str: String) = "(+($str))"

	override fun N_l2f(str: String) = "(+($str))"
	override fun N_l2d(str: String) = "(+($str))"

	//override fun N_c_div(l: String, r: String) = "unchecked($l / $r)"

	override fun N_idiv(l: String, r: String): String = "N::idiv($l, $r)"
	override fun N_irem(l: String, r: String): String = "N::irem($l, $r)"

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

	override fun genBodyTrapsPrefix() = Indenter("\$J__exception__ = null;")
	override fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "\$J__exception__"

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (JavaWrappedException \$J__i__exception__)") {
			line("\$J__exception__ = \$J__i__exception__->t;")
			line(stm.catch.genStm())
		}
	}

	override fun N_c_ushr(l: String, r: String) = "(int)(((uint)($l)) >> $r)"

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(\"$desc\", ${e.counts.map { it.genExpr() }.joinToString(", ")})"
	}

	override val NegativeInfinityString = "Double.NegativeInfinity"
	override val PositiveInfinityString = "Double.PositiveInfinity"
	//override val NanString = "Double.NaN"
	override val NanString = "N.DoubleNaN"

	override val String.escapeString: String get() = "Bootstrap::\$STRINGLIT_${allocString(currentClass, this)}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
	}

	override fun escapedConstant(v: Any?): String = when (v) {
	//is Float -> if (v.isInfinite()) if (v < 0) NegativeInfinityString else PositiveInfinityString else if (v.isNaN()) NanString else "${v}f"
		is Long -> "null"
		else -> super.escapedConstant(v)
	}

	override fun getClassInterfaces(clazz: AstClass): List<FqName> = clazz.implementingNormalized

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
}