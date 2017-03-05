package com.jtransc.gen.as3

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.ds.getOrPut2
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.lang.map
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.vfs.*
import java.io.File

// Supports GOTO keyword
// Supports static fields and methods on interfaces
class As3Target : GenTargetDescriptor() {
	override val name = "as3"
	override val longName = "ActionScript3"
	override val sourceExtension = "as"
	override val outputExtension = "swf"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable = true
	override val programFeatures: Set<Class<AstProgramFeature>> = setOf()

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("as3", "as3", "program.as", minimizeNames = false)
	)

	@Suppress("ConvertLambdaToReference")
	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-as3"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<As3Generator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"swf" -> "as3"
		else -> null
	}
}

@Singleton
class As3Generator(injector: Injector) : FilePerClassCommonGenerator(injector) {
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
		"internal",
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
		return As3Compiler.genCommand(programFile.parentFile, File(programFile.parentFile, "Main.as"), debug, libs)
	}

	override fun setTemplateParamsAfterBuildingSource() {
		super.setTemplateParamsAfterBuildingSource()
		params["AIRSDK_VERSION_INT"] = As3Compiler.AIRSDK_VERSION_INT
		params["BASE_CLASSES_FQNAMES"] = getClassesForStaticConstruction().map { it.name.targetName }
		params["STATIC_CONSTRUCTORS"] = genStaticConstructorsSortedLines()
	}

	@Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
	private fun _getActualFqName(name: FqName): FqName {
		val realclass = if (name in program) program[name] else null
		return FqName(classNames.getOrPut2(name) {
			if (realclass?.nativeName != null) {
				realclass!!.nativeName!!
			} else {
				FqName(name.packageParts.map { if (it in keywords) "${it}_" else it }.map(String::decapitalize), "${name.simpleName.replace('$', '_')}_".capitalize()).fqname
			}
		})
	}

	//override fun getClassBaseFilename(clazz: AstClass): String = clazz.actualFqName.fqname.replace('.', '/').replace('$', '_')
	//override fun getClassBaseFilename(clazz: AstClass): String = clazz.actualFqName.fqname.replace('.', '/').replace('$', '_')
	override fun getClassBaseFilename(clazz: AstClass): String = clazz.fqname.replace('.', '_').replace('$', '_')
	override fun getClassFilename(clazz: AstClass) = getClassBaseFilename(clazz) + ".as"

	override val FqName.targetName: String get() = this.fqname.replace('.', '_').replace('$', '_')
	//override val FqName.targetName: String get() = this.actualFqName.fqname.replace('$', '_')

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = listOf("Main.xml")
		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")

		val cmdAndArgs = listOf(As3Compiler.ADL, outFile.realpathOS)

		return ProcessResult2(RootLocalVfs().exec(cmdAndArgs, ExecOptions(passthru = redirect, sysexec = true)))
	}

	override fun writeClasses(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeClasses(output)
		println(output)
	}

	override fun genField(field: AstField): Indenter = Indenter.gen {
		val static = field.isStatic.map("static ", "")

		line("public ${static}var ${field.targetName}: ${field.type.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun genClass(clazz: AstClass): Indenter = Indenter {
		line("package") {
			line(super.genClass(clazz))
		}
	}

	//override fun genClasses(output: SyncVfsFile): Indenter = Indenter.gen {
	//	val StringFqName = buildTemplateClass("java.lang.String".fqname)
	//	val classesStr = super.genClasses(output)
	//	line(classesStr)
	//	line("class Bootstrap") {
	//		for (lit in getGlobalStrings()) {
	//			line("static public $StringFqName ${lit.name};")
	//		}
	//		line("static public void __initStrings()") {
	//			for (lit in getGlobalStrings()) {
	//				// STRINGLIT_
	//				line("${lit.name} = N.strLitEscape(${lit.str.quote()});")
	//			}
	//		}
	//		val entryPointFqName = program.entrypoint
	//		val entryPointClass = program[entryPointFqName]
	//		line("static void Main(string[] args)") {
	//			line("try {")
	//			indent {
	//				line("N.init();")
	//				line("__initStrings();")
	//				line(genStaticConstructorsSorted())
	//				//line(buildStaticInit(entryPointFqName))
	//				val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.STRING)))]
	//				line(buildMethod(mainMethod, static = true) + "(N.strArray(args));")
	//			}
	//			line("} catch (WrappedThrowable e) {")
	//			indent {
	//				line("Console.WriteLine(e.t.ToString());")
	//				line("Console.WriteLine(e.ToString());")
	//			}
	//			line("} catch (Exception e) {")
	//			indent {
	//				line("Console.WriteLine(e.ToString());")
	//			}
	//			line("}")
	//		}
	//	}
	//}

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		if (elementType is AstType.Primitive) {
			return "$array[$index] = ($value) as ${elementType.targetName};"
		} else {
			return "$array[$index] = ($value) as ${AstType.OBJECT.targetName};"
		}
	}

	override fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		val CLASS = if (clazz.isInterface) "interface" else "class"
		var decl = "public $CLASS ${clazz.name.targetSimpleName}"
		decl += genClassDeclExtendsImplements(clazz, kind)
		return decl
	}

	override val AstLocal.decl: String get() = "var ${this.targetName}: ${this.type.localDeclType} = ${this.type.nativeDefaultString};"
	override val AstArgument.decl: String get() = "${this.targetName}: ${this.type.localDeclType}"

	override fun genMetodDecl(method: AstMethod): String {
		val args = method.methodType.args.map { it.decl }

		//if (method.isInstanceInit) mods += "final "

		val mods = genMethodDeclModifiers(method)
		return "$mods function ${method.targetName}(${args.joinToString(", ")}): ${method.actualRetType.targetName}"
	}

	override fun genMethodDeclModifiers(method: AstMethod): String {
		if (method.containingClass.isInterface) {
			return "public " + method.isStatic.map("static ", "")
		} else {
			var mods = ""
			if (!method.isStatic && method.targetIsOverriding) mods += "override "
			if (method.isStatic) mods += "static "
			mods += "public "
			return mods
		}
	}

	override fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		return "JA_I${staticAccessOperator}T(new<int>[ " + e.values.joinToString(",") + " ])"
	}

	override fun N_f2d(str: String) = "Number($str)"
	override fun N_d2f(str: String) = "Number($str)"

	override fun N_is(a: String, b: String): String = "(($a) is $b)"

	override val NullType by lazy { AstType.OBJECT.targetName }
	override val VoidType = "void"
	override val BoolType = "boolean"
	override val IntType = "int"
	override val ShortType = "int"
	override val CharType = "int"
	override val ByteType = "int"
	override val FloatType = "Number"
	override val DoubleType = "Number"
	override val LongType = "Long"

	override val FqName.targetSimpleName: String get() = this.targetName

	override fun N_c(str: String, from: AstType, to: AstType) = "(($str) as ${to.targetName})"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(${e.array.genNotNull()} as $BaseArrayType).length"
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
				line("public __AS3__CLASS_ID: int;")
				line("public function ${clazz.name.targetName}(CLASS_ID: int = ${clazz.classId}) { this.__AS3__CLASS_ID = CLASS_ID; }")
			} else {
				line("public function ${clazz.name.targetName}(CLASS_ID: int = ${clazz.classId}) { super(CLASS_ID); }")
			}
		}
		if (clazz.staticConstructor != null) {
			line("static public function SI(): void") {
				val clazzName = if (clazz.isInterface) clazz.name.targetNameForStatic else clazz.name.targetName
				for (field in clazz.fields.filter { it.isStatic }) {
					line("$clazzName.${field.targetName} = ${field.escapedConstantValue};")
				}
				line(genSIMethodBody(clazz))
			}
		} else {
			line("static public function SI(): void { }")
		}
	}

	//override fun N_i(str: String) = "((int)($str))"
	override fun N_i(str: String) = "($str)"

	//override fun N_f2i(str: String) = "((int)($str))"
	override fun N_d2i(str: String) = "int($str)"

	override fun N_c_eq(l: String, r: String) = "($l == $r)"
	override fun N_c_ne(l: String, r: String) = "($l != $r)"

	override fun N_i2f(str: String) = "Number($str)"
	override fun N_i2d(str: String) = "Number($str)"

	override fun N_l2f(str: String) = "Number($str)"
	override fun N_l2d(str: String) = "Number($str)"

	//override fun N_c_div(l: String, r: String) = "unchecked($l / $r)"

	override fun N_idiv(l: String, r: String): String = "N.idiv($l, $r)"
	override fun N_irem(l: String, r: String): String = "N.irem($l, $r)"

	override fun N_lnew(value: Long): String = "((long)(${value}L))"

	override fun genMissingBody(method: AstMethod): Indenter = Indenter.gen {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw new Error(${message.quote()});")
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

	override fun N_c_ushr(l: String, r: String) = "int((uint($l)) >> $r)"

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(\"$desc\", ${e.counts.map { it.genExpr() }.joinToString(", ")})"
	}

	override val NegativeInfinityString = "Double.NegativeInfinity"
	override val PositiveInfinityString = "Double.PositiveInfinity"
	//override val NanString = "Double.NaN"
	override val NanString = "N.DoubleNaN"

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