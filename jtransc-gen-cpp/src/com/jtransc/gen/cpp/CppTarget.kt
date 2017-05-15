package com.jtransc.gen.cpp

import com.jtransc.ConfigLibraries
import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.annotation.JTranscAddFileList
import com.jtransc.annotation.JTranscAddHeaderList
import com.jtransc.annotation.JTranscAddMembersList
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.GotosFeature
import com.jtransc.ast.feature.method.OptimizeFeature
import com.jtransc.ast.feature.method.SimdFeature
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.text.uquote
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.LocalVfsEnsureDirs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*

const val CHECK_ARRAYS = true
const val TRACING = false
const val TRACING_JUST_ENTER = false

data class ConfigCppOutput(val cppOutput: SyncVfsFile)

// @TODO: http://en.cppreference.com/w/cpp/language/eval_order
// @TODO: Use std::array to ensure it is deleted
class CppTarget : GenTargetDescriptor() {
	override val name = "cpp"
	override val outputExtension = "bin"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable: Boolean = true

	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-cpp"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		return injector.get<CppGenerator>()
	}

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("cpp", "cpp", "program.cpp", minimizeNames = false),
		TargetBuildTarget("plainCpp", "cpp", "program.cpp", minimizeNames = false)
	)

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"exe" -> "cpp"
		"bin" -> "cpp"
		else -> null
	}
}

@Singleton
class CppGenerator(injector: Injector) : CommonGenerator(injector) {
	override val SINGLE_FILE: Boolean = true

	override val methodFeatures = setOf(SwitchFeature::class.java, GotosFeature::class.java)
	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java)
	override val keywords = setOf(
		"alignas", "alignof", "and", "and_eq", "asm", "atomic_cancel", "atomic_commit", "atomic_noexcept", "auto",
		"bitand", "bitor", "bool", "break",
		"case", "catch", "char", "char16_t", "char32_t", "class", "compl", "concept", "const", "constexpr", "const_cast", "continue",
		"decltype", "default", "delete", "do", "double", "dynamic_cast",
		"else", "enum", "explicit", "export", "extern", "false", "float", "for", "friend",
		"goto", "if", "import", "inline", "int", "long",
		"module", "mutable", "namespace", "new", "noexcept", "not", "not_eq", "nullptr",
		"operator", "or", "or_eq", "private", "protected", "public",
		"register", "reinterpret_cast", "requires", "return",
		"short", "signed", "sizeof", "static", "static_assert", "static_cast", "struct", "switch", "synchronized",
		"template", "this", "thread_local", "throw", "true", "try", "typedef", "typeid", "typename",
		"union", "unsigned", "using", "virtual", "void", "volatile", "wchar_t", "while",
		"xor", "xor_eq", "override", "final", "transaction_safe", "transaction_safe_dynamic",
		// Macro
		"if", "elif", "else", "endif", "defined", "ifdef", "ifndef",
		"define", "undef", "include", "line", "error", "pragma", "_Pragma"
	)
	override val stringPoolType = StringPool.Type.GLOBAL
	override val staticAccessOperator: String = "::"
	override val instanceAccessOperator: String = "->"

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return CppCompiler.genCommand(
			//programFile = File(configOutputFile.output),
			programFile = configTargetFolder.targetFolder[configOutputFile.output].realfile,
			debug = settings.debug,
			libs = injector.get<ConfigLibraries>().libs
		)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		val names = listOf("a.exe", "a", "a.out")
		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")
		val result = LocalVfs(File(configTargetFolder.targetFolder.realpathOS)).exec(outFile.realpathOS)
		return ProcessResult2(result)
	}

	override val allowAssignItself = true
	val lastClassId = program.classes.map { it.classId }.max() ?: 0

	fun generateTypeTableHeader() = Indenter.gen {
		line("struct TYPE_INFO", after2 = ";") {
			line("const size_t size;")
			line("const int* subtypes;")
		}
		line("struct TYPE_TABLE { static const int count; static const TYPE_INFO TABLE[$lastClassId]; };")
		line("const TYPE_INFO TABLE_INFO_NULL = {1, new int[1]{0}};")
	}

	fun generateTypeTableFooter() = Indenter.gen {
		var objectClassId = program["java.lang.Object".fqname].classId
		for (clazz in ordereredClasses) {
			val ids = clazz.getAllRelatedTypesIdsWith0AtEnd()
			line("const TYPE_INFO ${clazz.cppName}::TABLE_INFO = { ${ids.size}, new int[${ids.size}]{${ids.joinToString(", ")}} };")
		}

		line("const int TYPE_TABLE::count = $lastClassId;")
		line("const TYPE_INFO TYPE_TABLE::TABLE[$lastClassId] =", after2 = ";") {
			val classesById = program.classes.map { it.classId to it }.toMap()

			@Suppress("LoopToCallChain")
			for (n in 0 until lastClassId) {
				val clazz = classesById[n]
				if (clazz != null) {
					line("${clazz.cppName}::TABLE_INFO,")
				} else if (n == 1) { // Special case for the array base class, which is also an object
					line("{ 1, new int[1]{${objectClassId}} },")
				} else {
					line("TABLE_INFO_NULL,")
				}
			}
		}
	}

	val ordereredClasses = Unit.let {
		val childrenMap = hashMapOf<AstClass, ArrayList<AstClass>>()
		for (current in program.classes) {
			val parent = current.parentClass
			if (parent != null) {
				val list = childrenMap.getOrPut(parent) { arrayListOf() }
				list += current
			}
		}

		val out = LinkedHashSet<AstClass>()

		fun explore(classes: List<AstClass>) {
			if (classes.isNotEmpty()) {
				for (clazz in classes) out += clazz
				explore(classes.flatMap { childrenMap[it] ?: arrayListOf() }.filter { it !in out })
			}
		}

		val roots = program.classes.filter { it.parentClass == null }
		explore(roots)

		out.toList()
	}

	var prefixTempId = 0
	val bodyPrefixes = arrayListOf<String>()

	override fun resetLocalsPrefix() {
		prefixTempId = 0
		bodyPrefixes.clear()
	}

	override fun genLocalsPrefix(): Indenter = indent {
		line(super.genLocalsPrefix())
		for (prefix in bodyPrefixes) line(prefix)
	}

	override fun genBodyTrapsPrefix(): Indenter = indent { line("JAVA_OBJECT J__exception__ = null;") }

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH): Indenter = Indenter.gen {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (JAVA_OBJECT J__i__exception__)") {
			line("J__exception__ = J__i__exception__;")
			line(stm.catch.genStm())
		}
	}

	override fun writeClasses(output: SyncVfsFile) {
		val arrayTypes = listOf(
			"JA_B" to "int8_t",
			"JA_Z" to "int8_t",
			"JA_S" to "int16_t",
			"JA_C" to "uint16_t",
			"JA_I" to "int32_t",
			"JA_J" to "int64_t",
			"JA_F" to "float",
			"JA_D" to "double",
			"JA_L" to "JAVA_OBJECT"
		)

		val mainClassFq = program.entrypoint
		entryPointClass = FqName(mainClassFq.fqname)
		entryPointFilePath = entryPointClass.targetFilePath

		val HEADER = Indenter.gen {
			// {{ HEADER }}
			val resourcesVfs = program.resourcesVfs

			for (clazz in program.classes) {
				for (includes in clazz.annotationsList.getTypedList(JTranscAddHeaderList::value).filter { it.target == "cpp" }) {
					for (header in includes.value) line(header)
				}
				for (files in clazz.annotationsList.getTypedList(JTranscAddFileList::value).filter { it.target == "cpp" }.filter { it.prepend.isNotEmpty() }) {
					line(gen(resourcesVfs[files.prepend].readString(), process = files.process))
				}
			}
		}


		val CLASS_REFERENCES = Indenter.gen {
			// {{ CLASS_REFERENCES }}
			for (clazz in ordereredClasses.filter { !it.isNative }) {
				line(writeClassRef(clazz))
			}
		}

		val TYPE_TABLE_HEADERS = Indenter.gen {
			// {{ TYPE_TABLE_HEADERS }}
			line(generateTypeTableHeader())
		}

		val ARRAY_TYPES = Indenter.gen {
			// {{ ARRAY_TYPES }}
			for (name in arrayTypes.map { it.first }) line("struct $name;")
		}

		val ARRAY_HEADERS_PRE = Indenter.gen {
			// {{ ARRAY_HEADERS }}
			for (clazz in ordereredClasses.filter { !it.isNative }.filter { it.fqname == "java.lang.Object" }) {
				line(writeClassHeader(clazz))
			}
		}

		val ARRAY_HEADERS_POST = Indenter.gen {
			// {{ ARRAY_HEADERS }}
			for (clazz in ordereredClasses.filter { !it.isNative }.filter { it.fqname != "java.lang.Object" }) {
				line(writeClassHeader(clazz))
			}
		}

		val impls = Indenter.gen {
			for (clazz in ordereredClasses.filter { !it.isNative }) {
				if (clazz.implCode != null) {
					line(clazz.implCode!!)
				} else {
					line(writeClassImpl(clazz))
				}
			}
		}

		val STRINGS = Indenter.gen {
			val globalStrings = getGlobalStrings()
			line("static JAVA_OBJECT ${globalStrings.map { it.name }.joinToString(", ")};")
			line("void N::initStringPool()", after2 = ";") {
				for (gs in globalStrings) {
					line("""${gs.name} = N::str(L${gs.str.uquote()}, ${gs.str.length});""")
				}
			}
		}

		val CLASSES_IMPL = Indenter.gen { line(impls) }
		val TYPE_TABLE_FOOTER = Indenter.gen { line(generateTypeTableFooter()) }
		val MAIN = Indenter.gen { line(writeMain()) }

		val classesIndenter = Indenter.gen {
			if (settings.debug) {
				line("#define DEBUG 1")
			} else {
				line("#define RELEASE 1")
			}
			if (TRACING_JUST_ENTER) line("#define TRACING_JUST_ENTER")
			if (TRACING) line("#define TRACING")
			line(gen(program.resourcesVfs["cpp/Base.cpp"].readString(), extra = hashMapOf(
				"HEADER" to HEADER.toString(),
				"CLASS_REFERENCES" to CLASS_REFERENCES.toString(),
				"TYPE_TABLE_HEADERS" to TYPE_TABLE_HEADERS.toString(),
				"ARRAY_TYPES" to ARRAY_TYPES.toString(),
				"ARRAY_HEADERS_PRE" to ARRAY_HEADERS_PRE.toString(),
				"ARRAY_HEADERS_POST" to ARRAY_HEADERS_POST.toString(),
				"CLASSES_IMPL" to CLASSES_IMPL.toString(),
				"STRINGS" to STRINGS.toString(),
				"TYPE_TABLE_FOOTER" to TYPE_TABLE_FOOTER.toString(),
				"MAIN" to MAIN.toString()
			)))
		}


		output[outputFile] = classesIndenter.toString()

		injector.mapInstance(ConfigCppOutput(output[outputFile]))

		println(output[outputFile].realpathOS)
	}

	val AstClass.cppName: String get() = this.name.targetName
	val AstType.REF.cppName: String get() = this.name.targetName
	val AstType.cppString: String get() = getTypeStringForCpp(this)
	val AstType.underlyingCppString: String get() = getUnderlyingType(this)

	fun writeMain(): Indenter = Indenter.gen {
		line("int main(int argc, char *argv[])") {
			line("""TRACE_REGISTER("::main");""")
			line("try") {
				line("N::startup();")
				line(genStaticConstructorsSorted())
				val callMain = buildMethod(program[AstMethodRef(program.entrypoint, "main", AstType.METHOD(AstType.VOID, listOf(ARRAY(AstType.STRING))))]!!, static = true)

				line("$callMain(N::strEmptyArray());")
			}
			line("catch (char const *s)") {
				line("""std::cout << "ERROR char const* " << s << "\n";""")
			}
			line("catch (std::wstring s)") {
				line("""std::wcout << L"ERROR std::wstring " << s << L"\n";""")
			}
			//line("catch (java_lang_Throwable *s)") {
			//	line("""std::wcout  << L"${"java.lang.Throwable".fqname.targetName}:" << L"\n";""")
			//	line("""printf("Exception: %p\n", (void*)s);""")
			//}
			line("catch (JAVA_OBJECT s)") {
				val toStringMethod = program["java.lang.Object".fqname].getMethodWithoutOverrides("toString")!!.targetName
				line("""std::wcout << L"ERROR JAVA_OBJECT " << N::istr2(s->$toStringMethod()) << L"\n";""")
			}
			//line("catch (...)") {
			//	line("""std::wcout << L"ERROR unhandled unknown exception\n";""")
			//}
			line("return 0;")
		}
	}

	fun writeClassRef(clazz: AstClass): Indenter = Indenter.gen {
		setCurrentClass(clazz)
		line("struct ${clazz.cppName};")
	}

	fun writeClassHeader(clazz: AstClass): Indenter = Indenter.gen {
		setCurrentClass(clazz)
		val directImplementing = clazz.allInterfacesInAncestors - (clazz.parentClass?.allInterfacesInAncestors ?: listOf())
		val directExtendingAndImplementing = (clazz.parentClassList + directImplementing)

		val parts = if (clazz.isInterface) {
			""
		} else if (clazz.fqname == "java.lang.Object") {
			"public gc"
		} else {
			directExtendingAndImplementing.map { "public ${it.cppName}" }.joinToString(", ")
		}

		line("struct ${clazz.cppName}${if (parts.isNotEmpty()) " : $parts " else " "} { public:")
		indent {
			for (member in clazz.annotationsList.getTypedList(JTranscAddMembersList::value).filter { it.target == "cpp" }.flatMap { it.value.toList() }) {
				line(member)
			}

			if (clazz.fqname == "java.lang.Object") {
				line("int __INSTANCE_CLASS_ID;")
				//line("SOBJ sptr() { return shared_from_this(); };")
			}
			for (field in clazz.fields) {
				val normalStatic = if (field.isStatic) "static " else ""
				val add = ""
				val btype = field.type.cppString
				val type = if (btype == "SOBJ" && field.isWeak) "WOBJ" else btype
				line("$normalStatic$type ${field.targetName}$add;")
			}

			val decl = if (clazz.parentClass != null) {
				"${clazz.cppName}(int __INSTANCE_CLASS_ID = ${clazz.classId}) : ${clazz.parentClass?.cppName}(__INSTANCE_CLASS_ID)"
			} else {
				"${clazz.cppName}(int __INSTANCE_CLASS_ID = ${clazz.classId})"
			}

			line(decl) {
				if (!clazz.isInterface) {
					if (clazz.parentClass == null) {
						line("this->__INSTANCE_CLASS_ID = __INSTANCE_CLASS_ID;")
					}
					for (field in clazz.fields.filter { !it.isStatic }) {
						val cst = if (field.hasConstantValue) field.constantValue.escapedConstant else "0"
						line("this->${field.targetName} = $cst;")
					}
				}
			}
			if (clazz.fqname == "java.util.Set") {
				println(clazz.fqname)
			}
			for (method in clazz.methods) {
				val type = method.methodType
				val argsString = type.args.map { it.type.cppString + " " + it.name }.joinToString(", ")
				val zero = if (clazz.isInterface && !method.isStatic) " = 0" else ""
				val inlineNone = if (method.isInline) "inline " else ""
				val virtualStatic = if (method.isStatic) "static " else "virtual "
				line("$inlineNone$virtualStatic${method.returnTypeWithThis.cppString} ${method.targetName}($argsString)$zero;")
			}
			for (parentMethod in directImplementing.flatMap { it.methods }) {
				val type = parentMethod.methodType
				val returnStr = if (type.retVoid) "" else "return "
				val argsString = type.args.map { it.type.cppString + " " + it.name }.joinToString(", ")
				val argsCallString = type.args.map { it.name }.joinToString(", ")
				val callingMethod = clazz.getMethodInAncestors(parentMethod.ref.withoutClass)
				if (callingMethod != null) {
					line("virtual ${parentMethod.returnTypeWithThis.cppString} ${parentMethod.targetName}($argsString) { $returnStr this->${callingMethod.targetName}($argsCallString); }")
				}
			}
			line("static bool SI_once;")
			line("static void SI();")

			val ids = (clazz.thisAndAncestors + clazz.allInterfacesInAncestors).distinct().map { it.classId }.filterNotNull() + listOf(-1)
			line("static const TYPE_INFO TABLE_INFO;")

			line("static ${clazz.cppName} *GET(java_lang_Object *obj);")
			line("static ${clazz.cppName} *GET_npe(java_lang_Object *obj, const wchar_t *location);")
		}
		line("};")

		line("${clazz.cppName} *${clazz.cppName}::GET(java_lang_Object *obj)") {
			line("return dynamic_cast<${clazz.cppName}*>(obj);")
		}

		line("${clazz.cppName} *${clazz.cppName}::GET_npe(java_lang_Object *obj, const wchar_t *location)") {
			line("return dynamic_cast<${clazz.cppName}*>(obj);")
		}

	}

	@Suppress("LoopToCallChain")
	fun writeClassImpl(clazz: AstClass): Indenter = Indenter.gen {
		setCurrentClass(clazz)

		for (field in clazz.fields) line(writeField(field))

		for (method in clazz.methods) {
			if (!clazz.isInterface || method.isStatic) {
				try {
					line(writeMethod(method))
				} catch (e: Throwable) {
					throw RuntimeException("Couldn't generate method $method for class $clazz due to ${e.message}", e)
				}
			}
		}

		line("void ${clazz.cppName}::SI() {")
		indent {
			line("""TRACE_REGISTER("${clazz.cppName}::SI");""")
			for (field in clazz.fields.filter { it.isStatic }) {
				if (field.isStatic) {
					val cst = if (field.hasConstantValue) field.constantValue.escapedConstant else "0"
					line("${clazz.cppName}::${field.targetName} = $cst;")
				}
			}

			val sim = clazz.staticInitMethod
			if (sim != null) {
				line("${sim.targetName}();")
			}
		}
		line("};")
	}

	fun writeField(field: AstField): Indenter = Indenter.gen {
		val clazz = field.containingClass
		if (field.isStatic) {
			line("${field.type.cppString} ${clazz.cppName}::${field.targetName} = 0;")
		}
	}

	val FEATURE_FOR_FUNCTION_WITH_TRAPS = setOf(OptimizeFeature::class.java, SwitchFeature::class.java, SimdFeature::class.java)
	val FEATURE_FOR_FUNCTION_WITHOUT_TRAPS = (FEATURE_FOR_FUNCTION_WITH_TRAPS + GotosFeature::class.java)

	override fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter {
		if (body.traps.isNotEmpty()) {
			return features.apply(method, body, FEATURE_FOR_FUNCTION_WITH_TRAPS, settings, types).genBody()
		} else {
			return features.apply(method, body, FEATURE_FOR_FUNCTION_WITHOUT_TRAPS, settings, types).genBody()
		}
	}

	fun writeMethod(method: AstMethod): Indenter = Indenter.gen {
		val clazz = method.containingClass
		val type = method.methodType

		val argsString = type.args.map { it.type.cppString + " " + it.name }.joinToString(", ")

		line("${method.returnTypeWithThis.cppString} ${clazz.cppName}::${method.targetName}($argsString)") {
			if (method.name == "finalize") {
				//line("""std::cout << "myfinalizer\n"; """);
			}

			if (!method.isStatic) {
				//line("""SOBJ _this(this);""")
			}

			line("""const wchar_t *FUNCTION_NAME = L"${method.containingClass.name}::${method.name}::${method.desc}";""")
			line("""TRACE_REGISTER(FUNCTION_NAME);""")

			setCurrentMethod(method)
			val body = method.body

			fun genJavaBody() = Indenter.gen {
				if (body != null) {
					line(this@CppGenerator.genBody2WithFeatures(method, body))
				} else {
					line("throw \"Empty BODY : ${method.containingClass.name}::${method.name}::${method.desc}\";")
				}
			}

			val bodies = method.getNativeBodies("cpp")

			val nonDefaultBodies = bodies.filterKeys { it != "" }
			val defaultBody = bodies[""] ?: genJavaBody()

			if (nonDefaultBodies.isNotEmpty()) {
				for ((cond, nbody) in nonDefaultBodies) {
					line("#ifdef $cond")
					indent {
						line(nbody)
					}
				}
				line("#else")
				indent {
					line(defaultBody)
				}
				line("#endif")
			} else if (method.isNative && bodies.isEmpty() && method.name.startsWith("dooFoo")) {
				line(genJniMethod(method))
			} else {
				line(defaultBody)
			}

			if (method.methodVoidReturnThis) line("return this;")
		}
	}


	fun genJniMethod(method: AstMethod) = Indenter.gen {
		var mangledJniFunctionName: String
		//if (method.isOverloaded) {
		//	mangledJniFunctionName = JniUtils.mangleLongJavaMethod(method);
		//} else {
		mangledJniFunctionName = JniUtils.mangleShortJavaMethod(method);
		//}

		val sb = StringBuilder(30)
		for (i in method.methodType.args.indices) {
			val arg = method.methodType.args[i]
			sb.append(", ") // This seperates the arguments that are _always_ passed to jni from the other arguments
			sb.append(toNativeType(arg.type))
		}
		val nativeParameterString = sb.toString()
		var standardJniArgumentString = "JNIEnv*"
		if (method.isStatic) standardJniArgumentString += ", jclass"
		else standardJniArgumentString += ", jobject"


		line("typedef ${toNativeType(method.methodType.ret)} (JNICALL *func_ptr_t)(${standardJniArgumentString + nativeParameterString});")
		line("static void* nativePointer = NULL;")
		//{% CLASS ${method.containingClass.fqname} %}
		line("func_ptr_t fptr = (func_ptr_t)N::jtvmResolveNative(N::resolveClass(L\"${method.containingClass.fqname}\"), \"${JniUtils.mangleShortJavaMethod(method)}\", \"${JniUtils.mangleLongJavaMethod(method)}\", &nativePointer);")

		val sb2 = StringBuilder(30)
		for (i in method.methodType.args.indices) {
			val arg = method.methodType.args[i].type;
			if (arg is AstType.REF) {
				sb2.append(", ((${referenceToNativeType(arg)})((SOBJ)");
				sb2.append("p${i}");
				sb2.append(").get())");
			} else if (arg is AstType.ARRAY) {
				sb2.append(", ((${arrayToNativeType(arg)})((SOBJ)");
				sb2.append("p${i}");
				sb2.append(").get())");
			} else {
				sb2.append(", p${i}");
			}

		}
		line("return fptr(N::getJniEnv(), NULL ${sb2.toString()});")
		//line("JNI: \"Empty BODY : ${method.containingClass.name}::${method.name}::${method.desc}\";")
	}

	private fun toNativeType(type: AstType): String {
		when (type) {
			is AstType.BOOL -> return "jboolean"
			is AstType.BYTE -> return "jbyte"
			is AstType.CHAR -> return "jchar"
			is AstType.SHORT -> return "jshort"
			is AstType.INT -> return "jint"
			is AstType.LONG -> return "jlong"
			is AstType.FLOAT -> return "jfloat"
			is AstType.DOUBLE -> return "jdouble"
			is AstType.REF -> return referenceToNativeType(type)
			is AstType.ARRAY -> return arrayToNativeType(type)
			AstType.VOID -> return "void"
			else -> throw Exception("Encountered unrecognized type for JNI: ${type}")
		}
	}

	private fun arrayToNativeType(type: AstType.ARRAY): String {
		val arrayType = type.element
		when (arrayType) {
			is AstType.BOOL -> return "jbooleanArray"
			is AstType.BYTE -> return "jbyteArray"
			is AstType.CHAR -> return "jcharArray"
			is AstType.SHORT -> return "jshortArray"
			is AstType.INT -> return "jintArray"
			is AstType.LONG -> return "jlongArray"
			is AstType.FLOAT -> return "jfloatArray"
			is AstType.DOUBLE -> return "jdoubleArray"
			else -> return "jobjectArray"
		}
	}

	private fun referenceToNativeType(type: AstType.REF): String {
		val throwableClass = program.get("java.lang.Throwable".fqname.ref)
		fun isThrowable(type: AstType.REF): Boolean {
			val clazz = program.get(type)
			if (clazz == throwableClass) return true
			if (clazz == null) throw RuntimeException("Couldn't generate jni call because the class for reference ${clazz} is null")
			return clazz.parentClassList.contains(throwableClass)
		}
		when {
			type == AstType.STRING -> return "jstring"
			type == AstType.CLASS -> return "jclass"
			isThrowable(type) -> return "jthrowable"
			else -> return "jobject"
		}


	}


	override fun processCallArg(e: AstExpr, str: String) = "((" + e.type.cppString + ")(" + str + "))"

	override val AstLocal.decl: String get() = "${this.type.cppString} ${this.targetName} = ${this.type.nativeDefaultString};"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "((JA_0*)${e.array.genNotNull()})->length"
	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String): String {
		val getMethod = if (context.useUnsafeArrays) "get" else "fastGet"
		return "((${getUnderlyingType(arrayType)})(N::ensureNpe($array, FUNCTION_NAME)))->$getMethod($index)"
	}

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		val setMethod = if (context.useUnsafeArrays) "set" else "fastSet"
		return "((${getUnderlyingType(arrayType)})(N::ensureNpe($array, FUNCTION_NAME)))->$setMethod($index, $value);"
	}

	private fun isThisOrThisWithCast(e: AstExpr): Boolean {
		return when (e) {
			is AstExpr.THIS -> true
			is AstExpr.CAST -> if (e.to == this.mutableBody.method.containingClass.astType) {
				isThisOrThisWithCast(e.subject.value)
			} else {
				false
			}
			else -> false
		}
	}

	private fun getPtr(clazz: AstClass, objStr: String): String {
		if (clazz.isInterface) {
			return "(dynamic_cast<${clazz.cppName}*>(N::ensureNpe($objStr, FUNCTION_NAME)))"
		} else {
			return "(static_cast<${clazz.cppName}*>(N::ensureNpe($objStr, FUNCTION_NAME)))"
		}
	}

	override fun genExprCallBaseInstance(e2: AstExpr.CALL_INSTANCE, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		//return "((${refMethodClass.cppName}*)(${e2.obj.genNotNull()}.get()))$methodAccess(${args.joinToString(", ")})"
		if (isThisOrThisWithCast(e2.obj.value)) {
			return "this$methodAccess(${args.joinToString(", ")})"
		} else {
			val objStr = e2.obj.genNotNull()
			return "${getPtr(refMethodClass, objStr)}$methodAccess(${args.joinToString(", ")})"
		}
	}

	override fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		val superMethod = refMethodClass[method.withoutClass] ?: invalidOp("Can't find super for method : $method")
		return "${refMethodClass.ref.cppName}::${superMethod.targetName}(${args.joinToString(", ")})"
	}

	override fun genExprThis(e: AstExpr.THIS): String {
		return genExprThis()
	}

	fun genExprThis(): String {
		return "this" //->sptr()"
	}

	override fun genStmSetFieldInstance(stm: AstStm.SET_FIELD_INSTANCE): Indenter {
		return super.genStmSetFieldInstance(stm)
	}

	override fun genExprMethodClass(e: AstExpr.INVOKE_DYNAMIC_METHOD): String {
		return "N::dummyMethodClass()"
	}

	override fun N_i2b(str: String) = "((int8_t)($str))"
	override fun N_i2c(str: String) = "((uint16_t)($str))"
	override fun N_i2s(str: String) = "((int16_t)($str))"
	override fun N_f2i(str: String) = "((int32_t)($str))"
	override fun N_d2i(str: String) = "((int32_t)($str))"

	override fun N_i2f(str: String) = "((float)($str))"
	override fun N_i2d(str: String) = "((double)($str))"

	override fun N_l2f(str: String) = "((float)($str))"
	override fun N_l2d(str: String) = "((double)($str))"

	//override fun N_i(str: String) = "((int32_t)($str))"
	override fun N_i(str: String) = str

	override fun N_idiv(l: String, r: String) = N_func("idiv", "$l, $r")
	override fun N_irem(l: String, r: String) = N_func("irem", "$l, $r")
	override fun N_iushr(l: String, r: String) = N_func("iushr", "$l, $r")
	override fun N_frem(l: String, r: String) = "::fmod($l, $r)"
	override fun N_drem(l: String, r: String) = "::fmod($l, $r)"

	override fun N_ladd(l: String, r: String) = "(($l) + ($r))"
	override fun N_lsub(l: String, r: String) = "(($l) - ($r))"
	override fun N_lmul(l: String, r: String) = "(($l) * ($r))"
	override fun N_ldiv(l: String, r: String) = "N::ldiv($l, $r)"
	override fun N_lrem(l: String, r: String) = "N::lrem($l, $r)"
	override fun N_lshl(l: String, r: String) = "(($l) << ($r))"
	override fun N_lshr(l: String, r: String) = "(($l) >> ($r))"
	override fun N_lushr(l: String, r: String) = "(int64_t)((uint64_t)($l) >> ($r))"
	override fun N_lor(l: String, r: String) = "(($l) | ($r))"
	override fun N_lxor(l: String, r: String) = "(($l) ^ ($r))"
	override fun N_land(l: String, r: String) = "(($l) & ($r))"

	override fun N_obj_eq(l: String, r: String) = "(($l) == ($r))"
	override fun N_obj_ne(l: String, r: String) = "(($l) != ($r))"

	override fun genStmSetFieldStaticActual(stm: AstStm.SET_FIELD_STATIC, left: String, field: AstFieldRef, right: String): Indenter = indent {
		line("$left = (${field.type.cppString})($right);")
	}

	override fun genStmReturnVoid(stm: AstStm.RETURN_VOID): Indenter = Indenter.gen {
		line(if (context.method.methodVoidReturnThis) "return " + genExprThis() + ";" else "return;")
	}

	override fun genStmReturnValue(stm: AstStm.RETURN): Indenter = Indenter.gen {
		line("return (${context.method.returnTypeWithThis.cppString})${stm.retval.genExpr()};")
	}

	override fun N_is(a: String, b: AstType.Reference): String = when (b) {
		is AstType.REF -> N_func("is", "($a), ${program[b.name].classId}")
		is AstType.ARRAY -> N_func("isArray", "($a), L${b.mangle().quote()}")
		else -> N_func("isUnknown", """$a, "Unsupported $b"""")
	}

	override fun genExprFieldInstanceAccess(e: AstExpr.FIELD_INSTANCE_ACCESS): String {
		if (isThisOrThisWithCast(e.expr.value)) {
			return buildInstanceField("this", fixField(e.field))
		} else {
			return buildInstanceField("((" + e.field.containingTypeRef.underlyingCppString + ")(N::ensureNpe(" + e.expr.genNotNull() + ", FUNCTION_NAME)))", fixField(e.field))
		}
	}

	override fun actualSetField(stm: AstStm.SET_FIELD_INSTANCE, left: String, right: String): String {
		val left2 = if (stm.left.value is AstExpr.THIS) {
			buildInstanceField("this", fixField(stm.field))
		} else {
			buildInstanceField("((${stm.field.containingTypeRef.underlyingCppString})N::ensureNpe(" + stm.left.genExpr() + ", FUNCTION_NAME))", fixField(stm.field))
		}
		val right2 = "(${stm.field.type.cppString})((${stm.field.type.cppString})(" + stm.expr.genExpr() + "))"

		return "$left2 = $right2;"
	}

	override fun actualSetLocal(stm: AstStm.SET_LOCAL, localName: String, exprStr: String): String {
		return "$localName = (${stm.local.type.cppString})($exprStr);"
	}

	override fun genExprIntArrayLit(e: AstExpr.INTARRAY_LITERAL): String {
		val ints = e.values.joinToString(",")
		if (e.values.size <= 4) {
			return "JA_I::fromArgValues($ints)"
		} else {
			val id = prefixTempId++
			val tempname = "arraylit_$id"
			bodyPrefixes += "int32_t $tempname[] = {$ints};"
			return "JA_I::fromVector($tempname, ${e.values.size})"
		}
	}

	override fun genExprStringArrayLit(e: AstExpr.STRINGARRAY_LITERAL): String {
		noImpl("C++ genExprStringArrayLit")
	}

	override fun createArraySingle(e: AstExpr.NEW_ARRAY, desc: String): String {
		return if (e.type.elementType !is AstType.Primitive) {
			"new $ObjectArrayType(${e.counts[0].genExpr()}, L\"$desc\")"
		} else {
			"new ${e.type.targetName}(${e.counts[0].genExpr()})"
		}
	}

	override fun createArrayMultisure(e: AstExpr.NEW_ARRAY, desc: String): String {
		return "$ObjectArrayType${staticAccessOperator}createMultiSure(L\"$desc\", { ${e.counts.map { it.genExpr() }.joinToString(", ")} } )"
	}

	override fun genExprNew(e: AstExpr.NEW): String = "" + super.genExprNew(e) + ""

	fun getUnderlyingType(type: AstType): String {
		return when (type) {
			is AstType.ARRAY -> when (type.element) {
				AstType.BOOL -> BoolArrayType
				AstType.BYTE -> ByteArrayType
				AstType.CHAR -> CharArrayType
				AstType.SHORT -> ShortArrayType
				AstType.INT -> IntArrayType
				AstType.LONG -> LongArrayType
				AstType.FLOAT -> FloatArrayType
				AstType.DOUBLE -> DoubleArrayType
				else -> ObjectArrayType
			} + "*"
			is AstType.REF -> "${type.name.targetName}*"
			else -> type.cppString
		}
	}

	override fun genStmRawTry(trap: AstTrap): Indenter = Indenter.gen {
		//line("try {")
		//_indent()
	}

	override fun genStmRawCatch(trap: AstTrap): Indenter = Indenter.gen {
		//_unindent()
		//line("} catch (SOBJ e) {")
		//indent {
		//	line("if (N::is(e, ${getClassId(trap.exception.name)})) goto ${trap.handler.name};")
		//	line("throw e;")
		//}
		//line("}")
	}

	override fun genStmThrow(stm: AstStm.THROW): Indenter = Indenter.gen {
		//line("""std::wcout << L"THROWING! ${context.clazz}:${context.method.name}" << L"\n";""")
		line(super.genStmThrow(stm))
	}

	override fun genStmRethrow(stm: AstStm.RETHROW): Indenter {
		return super.genStmRethrow(stm)
	}

	override fun genStmLine(stm: AstStm.LINE) = indent {
		mark(stm)
	}

	override fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter.gen {
		val values = stm.values.map { it.genExpr() }
		line("") {
			line("const ${stm.array.type.elementType.cppString} ARRAY_LITERAL[${values.size}] = { ${values.joinToString(", ")} };")
			line("((${stm.array.type.underlyingCppString})((" + stm.array.genExpr() + ")))->setArray(${stm.startIndex}, ${values.size}, ARRAY_LITERAL);")
		}
	}

	//override val MethodRef.targetName: String get() {
	//	return getClassNameAllocator(ref.containingClass).allocate(ref) {
	//		val astMethod = program[ref]!!
	//		val containingClass = astMethod.containingClass
	//
	//		val prefix = if (containingClass.isInterface) "I_" else if (astMethod.isStatic) "S_" else "M_"
	//		val prefix2 = if (containingClass.isInterface || ref.isClassOrInstanceInit) "${containingClass.name.targetName}_" else ""
	//
	//		"$prefix$prefix2${super.targetName}_" + normalizeName(astMethod.methodType.mangle())
	//	}
	//}

	override fun buildAccessName(name: String, static: Boolean, field: Boolean): String = if (static) "::$name" else "->$name"

	fun getTypeStringForCpp(type: AstType): String = when (type) {
		AstType.VOID -> "void"
		AstType.BOOL -> "int8_t"
		AstType.BYTE -> "int8_t"
		AstType.CHAR -> "uint16_t"
		AstType.SHORT -> "int16_t"
		AstType.INT -> "int32_t"
		AstType.LONG -> "int64_t"
		AstType.FLOAT -> "float"
		AstType.DOUBLE -> "double"
		is AstType.Reference -> "JAVA_OBJECT"
		else -> "AstType_cppString_UNIMPLEMENTED($this)"
	}

	//override val FieldRef.targetName: String get() {
	//	val fieldRef = this
	//	val ref = fieldRef.ref
	//	return getClassNameAllocator(ref.containingClass).allocate(ref) { "F_" + normalizeName(ref.name + "_" + ref.type.mangle()) }
	//}

	override val DoubleNegativeInfinityString = "-INFINITY"
	override val DoublePositiveInfinityString = "INFINITY"
	override val DoubleNanString = "NAN"

	override val String.escapeString: String get() = "STRINGLIT_${allocString(currentClass, this)}"
	override val AstType.escapeType: String get() = N_func("resolveClass", "L${this.mangle().uquote()}")

	override fun N_lnew(value: Long): String = when (value) {
		Long.MIN_VALUE -> "(int64_t)(0x8000000000000000U)"
		else -> "(int64_t)(${value}L)"
	}

	override val FieldRef.targetName: String get() = getNativeName(this)

	override val MethodRef.targetNameBase: String get() {
		val method = this
		return getClassNameAllocator(method.ref.containingClass).allocate(method.ref) {
			val astMethod = program[method.ref]!!
			val containingClass = astMethod.containingClass
			val prefix = if (containingClass.isInterface) "I_" else if (astMethod.isStatic) "S_" else "M_"
			val prefix2 = if (containingClass.isInterface || method.ref.isClassOrInstanceInit) {
				//getClassFqNameForCalling(containingClass.name) + "_"
				containingClass.name.fqname + "_"
			} else {
				""
			}
			val suffix = "_${astMethod.name}${astMethod.desc}"
			//"$prefix$prefix2" + super.getNativeName(method) + "$suffix"

			"$prefix$prefix2$suffix"
		}
	}

	fun getNativeName(field: FieldRef): String {
		return getClassNameAllocator(field.ref.containingClass).allocate(field.ref) { "F_" + normalizeName(field.ref.name + "_" + field.ref.type.mangle(), NameKind.FIELD) }
	}

	override val AstMethodRef.objectToCache: Any get() = this

	override fun buildStaticInit(clazzName: FqName): String? = null
}
