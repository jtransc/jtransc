package com.jtransc.gen.as3

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
class As3Generator(injector: Injector) : CommonGenerator(injector) {
	override val TARGET_NAME: String = "AS3"
	override val SINGLE_FILE: Boolean = false

	//class DGenerator(injector: Injector) : FilePerClassCommonGenerator(injector) {
	override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java)
	override val stringPoolType: StringPool.Type = StringPool.Type.GLOBAL
	override val interfacesSupportStaticMembers: Boolean = false
	override val usePackages = true
	override val classFileExtension = ".as"
	override val languageRequiresDefaultInSwitch = true
	override val defaultGenStmSwitchHasBreaks = true
	override val allowRepeatMethodsInInterfaceChain = false
	override val floatHasFSuffix: Boolean = false

	override val keywords = setOf(
		"abstract", "alias", "align", "asm", "assert", "auto",
		"body", "bool", "break", "byte",
		"case", "cast", "catch", "cdouble", "cent", "cfloat", "char", "class", "const", "continue", "creal",
		"dchar", "debug", "default", "delegate", "delete", "deprecated", "do", "double",
		"else", "enum", "export", "extern",
		"false", "final", "finally", "float", "for", "foreach", "foreach_reverse", "function",
		"goto",
		"as", "is",
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

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return As3Compiler.genCommand(programFile.parentFile, File(programFile.parentFile, "Main.as"), debug, libs)
	}

	override fun setTemplateParamsAfterBuildingSource() {
		super.setTemplateParamsAfterBuildingSource()
		params["AIRSDK_VERSION_INT"] = As3Compiler.AIRSDK_VERSION_INT
		params["BASE_CLASSES_FQNAMES"] = getClassesForStaticConstruction().map { it.name.targetNameForStatic }
		params["STATIC_CONSTRUCTORS"] = genStaticConstructorsSortedLines()

		val entryPointFqName = program.entrypoint
		val entryPointClass = program[entryPointFqName]
		val mainMethod = entryPointClass[AstMethodRef(entryPointFqName, "main", AstType.METHOD(AstType.VOID, ARRAY(AstType.STRING)))]
		params["MAIN_METHOD_CALL"] = buildMethod(mainMethod, static = true) + "(N.strArray([]));"

	}

	override val FqName.targetName: String get() = this.fqname.replace('.', '_').replace('$', '_')
	//override val FqName.targetName: String get() = this.actualFqName.fqname.replace('$', '_')

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = listOf("Main.xml")
		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")

		return As3Compiler.useAdl {
			ProcessResult2(RootLocalVfs().exec(listOf(As3Compiler.ADL) + (if (debugVersion) listOf() else listOf("-nodebug")) + listOf(outFile.realpathOS), ExecOptions(passthru = redirect, sysexec = false, fixLineEndings = true)))
		}
	}

	override fun writeClasses(output: SyncVfsFile) {
		//println(program.resourcesVfs)
		super.writeClasses(output)

		val StringFqName = buildTemplateClass("java.lang.String".fqname)

		output["Bootstrap.as"] = Indenter {
			line("package") {
				line("public class Bootstrap") {
					for (lit in getGlobalStrings()) {
						line("static public var ${lit.name}: $StringFqName = N.strLitEscape(${lit.str.quote()});")
					}

					line("static public function init():void") {
					}
				}
			}
		}
	}

	override fun genField(field: AstField): Indenter = Indenter {
		val static = field.isStatic.map("static ", "")

		line("public ${static}var ${field.targetName}: ${field.type.targetName} = ${field.type.getNull().escapedConstant};")
	}

	override fun genClassPart(clazz: AstClass, type: MemberTypes): ClassResult {
		val result = super.genClassPart(clazz, type)
		return result.copy(indenter = Indenter {
			line("package") {
				line("import Int64;")
				line("import avm2.intrinsics.memory.*;")
				for (header in clazz.annotationsList.getHeadersForTarget(targetName)) {
					line(header)
				}
				val actualImports = arrayListOf<FqName>()
				linedeferred {
					for (import in actualImports) {
						line("import $import;")
					}
				}
				line(result.indenter)
				actualImports += imports.toSet()
			}
		})
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

	override fun genClassDecl(clazz: AstClass, kind: MemberTypes): String {
		val CLASS = when (kind) {
			MemberTypes.STATIC -> "class"
			else -> if (clazz.isInterface) "interface" else "class"
		}
		val name = when (kind) {
			MemberTypes.STATIC -> clazz.name.targetNameForStatic
			else -> clazz.name.targetSimpleName
		}
		var decl = "public $CLASS $name"
		decl += when (kind) {
			MemberTypes.STATIC -> ""
			else -> genClassDeclExtendsImplements(clazz, kind)
		}
		return decl
	}

	//override fun getTypeTargetName(type: AstType): String {
	//	val res = super.getTypeTargetName(type)
	//	return if (res == "java_lang_Object") "*" else res
	//}

	override val AstLocal.decl: String get() = "var ${this.targetName}: ${this.type.localDeclType} = ${this.type.nativeDefaultString};"
	override val AstArgument.decl: String get() = "${this.targetName}: ${this.type.localDeclType}"

	override fun genMetodDecl(method: AstMethod): String {
		val args = method.methodType.args.map { it.decl }

		//if (method.isInstanceInit) mods += "final "

		val mods = genMethodDeclModifiers(method)
		val clazz = method.containingClass
		val m = method

		val rmods = if (!clazz.isInterface || m.isStatic) mods else ""
		//val rmods = mods
		return "$rmods function ${method.targetName}(${args.joinToString(", ")}): ${method.actualRetType.targetName}"
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

	override fun N_i2b(str: String) = "avm2.intrinsics.memory.sxi8($str)"
	override fun N_i2c(str: String) = "(($str)&0xFFFF)"
	override fun N_i2s(str: String) = "avm2.intrinsics.memory.sxi16($str)"

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String): String {
		// avm2.intrinsics.memory.sxi8
		// avm2.intrinsics.memory.sxi16
		return when (elementType) {
		//AstType.BYTE -> "((($array.data[$index])<<24)>>24)"
		//AstType.CHAR -> "((($array.data[$index]))&0xFFFF)"
		//AstType.SHORT -> "((($array.data[$index])<<16)>>16)"
			AstType.BYTE -> N_i2b("$array.data[$index]")
			AstType.CHAR -> N_i2c("$array.data[$index]")
			AstType.SHORT -> N_i2s("$array.data[$index]")
			else -> "($array.data[$index])"
		}
		//return "$array.get($index)"
	}

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		return "$array.data[$index] = $value;"
	}

	override fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		return "JA_I${staticAccessOperator}T(new<int>[ " + e.values.joinToString(",") + " ])"
	}

	override fun N_f2d(str: String) = "Number($str)"
	override fun N_d2f(str: String) = "Number($str)"

	override fun N_is(a: String, b: String): String = "(($a) is $b)"

	override fun N_ineg(str: String) = "N.ineg($str)"

	override val NullType by lazy { AstType.OBJECT.targetName }
	override val VoidType = "void"
	override val BoolType = "Boolean"
	override val IntType = "int"
	override val ShortType = "int"
	override val CharType = "int"
	override val ByteType = "int"
	override val FloatType = "Number"
	override val DoubleType = "Number"
	override val LongType = "Int64"


	override val FqName.targetSimpleName: String get() = this.targetName

	override fun N_c(str: String, from: AstType, to: AstType) = "(($str) as ${to.targetName})"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "(${e.array.genNotNull()} as $BaseArrayType).length"
	override fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw new WrappedThrowable(${stm.exception.genExpr()});")

	override fun genLabel(label: AstLabel) = "${label.name}:"

	override fun genSIMethod(clazz: AstClass): Indenter = Indenter {
		if (clazz.isJavaLangObject) {
			//line("override public function toString(): String") {
			line("public function toString(): String") {
				val toStringMethodName = buildMethod(clazz.getMethodWithoutOverrides("toString")!!, static = false)
				line("return N.istr($toStringMethodName());")
			}
		}

		if (!clazz.isInterface) {
			if (clazz.isJavaLangObject) {
				line("public var __JT__CLASS_ID: int;")
				line("public function ${clazz.name.targetName}(CLASS_ID: int = ${clazz.classId}) { this.__JT__CLASS_ID = CLASS_ID; }")
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

	override fun N_j2f(str: String) = "Number($str)"
	override fun N_j2d(str: String) = "Number($str)"

	//override fun N_c_div(l: String, r: String) = "unchecked($l / $r)"

	override fun N_idiv(l: String, r: String): String = "N.idiv($l, $r)"
	override fun N_irem(l: String, r: String): String = "N.irem($l, $r)"

	override fun genMissingBody(method: AstMethod): Indenter = Indenter {
		val message = "Missing body ${method.containingClass.name}.${method.name}${method.desc}"
		line("throw new Error(${message.quote()});")
	}

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (J__i__exception__: WrappedThrowable)") {
			line("J__exception__ = J__i__exception__.t;")
			line(stm.catch.genStm())
		}
	}

	//override fun N_c_ushr(l: String, r: String) = "N.iushr($l, $r)"

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(\"$desc\", [${e.counts.map { it.genExpr() }.joinToString(", ")}])"
	}

	override val DoubleNegativeInfinityString = "Number.NEGATIVE_INFINITY"
	override val DoublePositiveInfinityString = "Number.POSITIVE_INFINITY"
	override val DoubleNanString = "N.NaN"

	override val String.escapeString: String get() = "Bootstrap.STRINGLIT_${allocString(currentClass, this)}"

	override fun AstExpr.genNotNull(): String {
		if (debugVersion) {
			return "(" + genExpr2(this) + ")"
		} else {
			return genExpr2(this)
		}
	}

	override fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = indent {
		line("N.monitorEnter(" + stm.expr.genExpr() + ");")
	}

	override fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = indent {
		line("N.monitorExit(" + stm.expr.genExpr() + ");")
	}

	override fun buildStaticInit(clazzName: FqName): String? = null

	override fun genExprCaughtException(e: AstExpr.CAUGHT_EXCEPTION): String = "(J__exception__ as ${e.type.targetName})"

	override fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String {
		if (from == to) return e;
		if (from is AstType.NULL) return e
		return "N.CHECK_CAST($e, ${to.targetNameRef})"
	}
}