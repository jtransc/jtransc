package com.jtransc.gen.cpp

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.annotation.JTranscAddFileList
import com.jtransc.annotation.JTranscAddHeaderList
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.*
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.gen.cpp.libs.Libs
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.text.toCommentString
import com.jtransc.text.uquote
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.LocalVfsEnsureDirs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*

const val CHECK_ARRAYS = true
const val TRACING = false
const val TRACING_JUST_ENTER = false
//const val ENABLE_TYPING = true
const val ENABLE_TYPING = false

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
	override val TARGET_NAME: String = "CPP"
	override val SINGLE_FILE: Boolean = true
	override val GENERATE_LINE_NUMBERS = false

	override val ARRAY_SUPPORT_SHORTCUTS = false
	override val ARRAY_OPEN_SYMBOL = "{"
	override val ARRAY_CLOSE_SYMBOL = "}"

//	override val methodFeaturesWithTraps = setOf(SwitchFeature::class.java, UndeterministicParameterEvaluationFeature::class.java)
//	override val methodFeatures = methodFeaturesWithTraps + setOf(GotosFeature::class.java)

	override val methodFeaturesWithTraps = setOf(OptimizeFeature::class.java, SwitchFeature::class.java, SimdFeature::class.java, UndeterministicParameterEvaluationFeature::class.java)
	override val methodFeatures = (methodFeaturesWithTraps + GotosFeature::class.java)

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

	override val allTargetLibraries by lazy { Libs.libs + super.allTargetLibraries }
	override val allTargetDefines by lazy { Libs.extraDefines + super.allTargetDefines }

	//override fun copyFilesExtra(output: SyncVfsFile) {
	//	output["CMakeLists.txt"] = Indenter {
	//		line("cmake_minimum_required(VERSION 2.8.9)")
	//		line("project (program)")
	//		line("add_executable(program program.cpp)")
	//	}
	//}

	override fun compile(): ProcessResult2 {
		Libs.installIfRequired(program.resourcesVfs)
		return super.compile()
	}

	//override fun escapedConstant(v: Any?): String = when (v) {
	//	null -> "((p_java_lang_Object)(null))"
	//	else -> super.escapedConstant(v)
	//}

	//override val AstType.nativeDefaultString: String get() = this.nativeDefault?.escapedConstant ?: "NULL"

	override fun genCompilerCommand(programFile: File, debug: Boolean, libs: List<String>): List<String> {
		return CppCompiler.genCommand(
			//programFile = File(configOutputFile.output),
			programFile = configTargetFolder.targetFolder[configOutputFile.output].realfile,
			debug = settings.debug,
			libs = allTargetLibraries,
			includeFolders = Libs.includeFolders.map { it.absolutePath },
			libsFolders = Libs.libFolders.map { it.absolutePath },
			defines = allTargetDefines,
			extraVars = extraVars
		)
	}

	override fun run(redirect: Boolean): ProcessResult2 {
		val cmakeFolder = if (debugVersion) "Debug" else "Release"
		//val names = listOf("bin/$cmakeFolder/program.exe", "bin/$cmakeFolder/program", "bin/$cmakeFolder/a", "bin/$cmakeFolder/a.out", "program", "a.exe", "a", "a.out")
		val names = listOf("$cmakeFolder/program.exe", "$cmakeFolder/program", "$cmakeFolder/a", "$cmakeFolder/a.out", "program", "a.exe", "a", "a.out")

		val outFile = names.map { configTargetFolder.targetFolder[it] }.firstOrNull { it.exists } ?: invalidOp("Not generated output file $names")
		val result = LocalVfs(File(configTargetFolder.targetFolder.realpathOS)).exec(listOf(outFile.realpathOS), options = ExecOptions(passthru = redirect, sysexec = false, fixLineEndings = true, fixencoding = false))
		return ProcessResult2(result)
	}

	override val allowAssignItself = true
	val lastClassId = program.classes.map { it.classId }.max() ?: 0

	fun generateTypeTableHeader() = Indenter {
		line("struct TYPE_INFO", after2 = ";") {
			line("const size_t size;")
			line("const int32_t* subtypes;")
		}
		line("struct TYPE_TABLE { static const int32_t count; static const TYPE_INFO TABLE[$lastClassId]; };")
		line("const TYPE_INFO TABLE_INFO_NULL = {1, new int32_t[1]{0}};")
	}

	fun generateTypeTableFooter() = Indenter {
		val objectClassId = program["java.lang.Object".fqname].classId
		for (clazz in ordereredClassesMustGenerate) {
			val ids = clazz.getAllRelatedTypesIdsWith0AtEnd()
			line("const TYPE_INFO ${clazz.cppName}::TABLE_INFO = { ${ids.size}, new int32_t[${ids.size}]{${ids.joinToString(", ")}} };")
		}

		line("const int32_t TYPE_TABLE::count = $lastClassId;")
		line("const TYPE_INFO TYPE_TABLE::TABLE[$lastClassId] =", after2 = ";") {
			val classesById = program.classes.map { it.classId to it }.toMap()

			@Suppress("LoopToCallChain")
			for (n in 0 until lastClassId) {
				val clazz = classesById[n]
				if (clazz != null && clazz.mustGenerate) {
					line("${clazz.cppName}::TABLE_INFO,")
				} else if (n == 1) { // Special case for the array base class, which is also an object
					line("{ 1, new int32_t[1]{$objectClassId} },")
				} else {
					line("TABLE_INFO_NULL,")
				}
			}
		}
	}

	fun generateCppCtorMap() = Indenter {
		line("typedef JAVA_OBJECT* (*ctor_func)(void);")
		//line("const int32_t TYPE_TABLE::count = $lastClassId;")
		line("static ctor_func CTOR_TABLE[$lastClassId] =", after2 = ";") {
			val classesById = program.classes.map { it.classId to it }.toMap()

			@Suppress("LoopToCallChain")
			for (n in 0 until lastClassId) {
				val clazz = classesById[n]
				if (clazz != null && !clazz.mustGenerate) {
					println("$n:" + clazz)
				}
				if (clazz != null && clazz.mustGenerate && !clazz.isAbstract && !clazz.isInterface) {
					line("[](){return (JAVA_OBJECT*)(new ${clazz.cppName}());},")
				} else if (clazz != null && clazz.mustGenerate && (clazz.isAbstract || clazz.isInterface)) {
					line("[](){ std::cerr << \"Class id \" << $n << \" refers to abstract class or interface!\"; abort(); return (JAVA_OBJECT*)(NULL);},")
				} else if (n == 1) {
					line("[](){ std::cerr << \"Class id \" << $n << \" refers to array base class!\"; abort(); return (JAVA_OBJECT*)(NULL);},")
				} else {
					line("[](){ std::cerr << \"Class id \" << $n << \" referred to a null clazz at compile time!\"; abort(); return (JAVA_OBJECT*)(NULL);},")
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

	val ordereredClassesMustGenerate by lazy { ordereredClasses.filter { it.mustGenerate } }
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

	val JAVA_LANG_STRING_FQ = AstType.REF("java.lang.String")

	override fun genBodyTrapsPrefix(): Indenter = indent { line("p_java_lang_Object J__exception__ = (p_java_lang_Object)nullptr;") }

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH): Indenter = Indenter {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (p_java_lang_Object J__i__exception__)") {
			line("J__exception__ = J__i__exception__;")
			line(stm.catch.genStm())
		}
	}

	fun Indenter.condWrapper(cond: String, callback: Indenter.() -> Unit) {
		if (cond.isNotEmpty()) {
			if (cond.startsWith("!")) line("#ifndef ${cond.substring(1)}") else line("#ifdef $cond")
			indent {
				callback()
			}
			line("#endif")
		} else {
			callback()
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
			"JA_L" to "p_java_lang_Object"
		)

		this.params["ENABLE_TYPING"] = ENABLE_TYPING
		this.params["CPP_LIB_FOLDERS"] = Libs.libFolders
		this.params["CPP_INCLUDE_FOLDERS"] = Libs.includeFolders
		this.params["CPP_LIBS"] = allTargetLibraries
		this.params["CPP_INCLUDES"] = targetIncludes
		this.params["CPP_DEFINES"] = allTargetDefines
		this.params["CPP_GLOBAL_POINTERS"] = ordereredClasses.flatMap { it.fields.filter { it.isStatic } }.map { "&${it.containingClass.ref.targetName}::${it.ref.targetName}" }

		val mainClassFq = program.entrypoint
		entryPointClass = FqName(mainClassFq.fqname)
		entryPointFilePath = entryPointClass.targetFilePath

		val HEADER = Indenter {
			// {{ HEADER }}
			val resourcesVfs = program.resourcesVfs

			for (clazz in program.classes) {
				for (includes in clazz.annotationsList.getTypedList(JTranscAddHeaderList::value).filter { it.target == "cpp" }) {
					condWrapper(includes.cond) {
						for (header in includes.value) line(header)
					}
				}
				for (files in clazz.annotationsList.getTypedList(JTranscAddFileList::value).filter { it.target == "cpp" }.filter { it.prepend.isNotEmpty() }) {
					line(gen(resourcesVfs[files.prepend].readString(), process = files.process))
				}
			}
		}


		val CLASS_REFERENCES = Indenter {
			// {{ CLASS_REFERENCES }}
			for (clazz in ordereredClassesMustGenerate) {
				line(writeClassRef(clazz))
			}
			for (clazz in ordereredClassesMustGenerate) {
				line(writeClassRefPtr(clazz))
			}
		}

		val TYPE_TABLE_HEADERS = Indenter {
			// {{ TYPE_TABLE_HEADERS }}
			line(generateTypeTableHeader())
		}

		val ARRAY_TYPES = Indenter {
			// {{ ARRAY_TYPES }}
			for (name in arrayTypes.map { it.first }) line("struct $name;")
			for (name in arrayTypes.map { it.first }) line("typedef $name* p_$name;")
		}

		val ARRAY_HEADERS_PRE = Indenter {
			// {{ ARRAY_HEADERS }}
			for (clazz in ordereredClasses.filter { !it.isNative }.filter { it.fqname == "java.lang.Object" }) {
				line(writeClassHeader(clazz))
			}
		}

		val ARRAY_HEADERS_POST = Indenter {
			// {{ ARRAY_HEADERS }}
			for (clazz in ordereredClasses.filter { !it.isNative }.filter { it.fqname != "java.lang.Object" }) {
				line(writeClassHeader(clazz))
			}
		}

		val impls = Indenter {
			for (clazz in ordereredClasses.filter { !it.isNative }) {
				if (clazz.implCode != null) {
					line(clazz.implCode!!)
				} else {
					line(writeClassImpl(clazz))
				}
			}
		}

		val STRINGS = Indenter {
			val globalStrings = getGlobalStrings()
			line("static void* STRINGS_START = nullptr;")
			line("static ${JAVA_LANG_STRING_FQ.targetNameRef} ${globalStrings.map { "${it.name} = nullptr" }.joinToString(", ")};")
			line("static void* STRINGS_END = nullptr;")
			line("void N::initStringPool()", after2 = ";") {
				for (gs in globalStrings) {
					line("""${gs.name} = N::str(L${gs.str.uquote()}, ${gs.str.length});""")
				}
			}
		}

		val CLASSES_IMPL = Indenter { line(impls) }
		val CPP_CTOR_MAP = Indenter { line(generateCppCtorMap()) }
		val TYPE_TABLE_FOOTER = Indenter { line(generateTypeTableFooter()) }
		val MAIN = Indenter { line(writeMain()) }

		val classesIndenter = Indenter {
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
				"CPP_CTOR_MAP" to CPP_CTOR_MAP.toString(),
				"STRINGS" to STRINGS.toString(),
				"TYPE_TABLE_FOOTER" to TYPE_TABLE_FOOTER.toString(),
				"MAIN" to MAIN.toString()
			)))
		}


		output[outputFile] = classesIndenter.toString()

		injector.mapInstance(ConfigCppOutput(output[outputFile]))

		println(output[outputFile].realpathOS)

		copyFiles(output)
	}

	val AstClass.cppName: String get() = this.name.targetName
	val AstClass.cppNameRefCast: String get() = this.name.targetNameRef
	override val FqName.targetNameRef: String get() = "p_" + this.targetName
	val AstType.REF.cppName: String get() = this.name.targetName

	fun writeMain(): Indenter = Indenter {
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
			//line("catch (p_java_lang_Object s)") {
			//	val toStringMethod = program["java.lang.Object".fqname].getMethodWithoutOverrides("toString")!!.targetName
			//	line("""std::wcout << L"ERROR p_java_lang_Object " << N::istr2(s->$toStringMethod()) << L"\n";""")
			//}
			//line("catch (...)") {
			//	line("""std::wcout << L"ERROR unhandled unknown exception\n";""")
			//}
			line("return 0;")
		}
	}

	fun writeClassRef(clazz: AstClass): Indenter = Indenter {
		setCurrentClass(clazz)
		line("struct ${clazz.cppName};")
	}

	fun writeClassRefPtr(clazz: AstClass): Indenter = Indenter {
		setCurrentClass(clazz)
		line("typedef ${clazz.cppName}* ${clazz.cppNameRefCast};")
	}

	fun writeClassHeader(clazz: AstClass): Indenter = Indenter {
		setCurrentClass(clazz)
		val directImplementing = clazz.allInterfacesInAncestors - (clazz.parentClass?.allInterfacesInAncestors ?: listOf())
		val directExtendingAndImplementing = (clazz.parentClassList + directImplementing)

		val parts = if (clazz.isInterface) {
			""
			//"public java_lang_Object"
		} else if (clazz.fqname == "java.lang.Object") {
			"public gc"
		} else {
			directExtendingAndImplementing.map { "public ${it.cppName}" }.joinToString(", ")
		}

		line("struct ${clazz.cppName}${if (parts.isNotEmpty()) " : $parts " else " "} { public:")
		indent {
			for (memberCond in clazz.nativeMembers) {
				condWrapper(memberCond.cond) {
					for (member in memberCond.members) {
						line(member.replace("###", "").template("native members"))
					}
				}
			}

			if (clazz.fqname == "java.lang.Object") {
				line("int32_t __JT__CLASS_ID;")
				//line("SOBJ sptr() { return shared_from_this(); };")
			}
			for (field in clazz.fields) {
				val normalStatic = if (field.isStatic) "static " else ""
				val add = ""
				val btype = field.type.targetNameRef
				val type = if (btype == "SOBJ" && field.isWeak) "WOBJ" else btype
				line("$normalStatic$type ${field.targetName}$add;")
			}

			val decl = if (clazz.parentClass != null) {
				"${clazz.cppName}(int __JT__CLASS_ID = ${clazz.classId}) : ${clazz.parentClass?.cppName}(__JT__CLASS_ID)"
			} else {
				"${clazz.cppName}(int __JT__CLASS_ID = ${clazz.classId})"
			}

			line(decl) {
				if (!clazz.isInterface) {
					if (clazz.parentClass == null) {
						line("this->__JT__CLASS_ID = __JT__CLASS_ID;")
					}
					for (field in clazz.fields.filter { !it.isStatic }) {
						val cst = if (field.hasConstantValue) field.constantValue.escapedConstant else "0"
						line("this->${field.targetName} = $cst;")
					}
				}
			}

			if (clazz.isInterface) {
				line("virtual p_java_lang_Object __getObj() = 0;")
				line("virtual void* __getInterface(int classId) = 0;")
			} else {
				line("virtual ${JAVA_LANG_OBJECT_REF.targetNameRef} __getObj() { return (${JAVA_LANG_OBJECT_REF.targetNameRef})this; }")
				line("virtual void* __getInterface(int classId)") {
					if (clazz.allInterfacesInAncestors.isNotEmpty()) {
						line("switch (classId)") {
							for (ifc in clazz.allInterfacesInAncestors) {
								line("case ${ifc.classId}: return (void*)(${ifc.ref.targetNameRefCast})this;")
							}
						}
					}
					line("return nullptr;")
				}
			}

			for (method in clazz.methods) {
				val type = method.methodType
				val argsString = type.args.map { it.type.targetNameRef + " " + it.name }.joinToString(", ")
				val zero = if (clazz.isInterface && !method.isStatic) " = 0" else ""
				val inlineNone = if (method.isInline) "inline " else ""
				val virtualStatic = if (method.isStatic) "static " else "virtual "
				line("$inlineNone$virtualStatic${method.returnTypeWithThis.targetNameRef} ${method.targetName}($argsString)$zero;")
			}
			for (parentMethod in directImplementing.flatMap { it.methods }) {
				val type = parentMethod.methodType
				val returnStr = if (type.retVoid) "" else "return "
				val argsString = type.args.map { it.type.targetNameRef + " " + it.name }.joinToString(", ")
				val argsCallString = type.args.map { it.name }.joinToString(", ")
				val callingMethod = clazz.getMethodInAncestors(parentMethod.ref.withoutClass)
				if (callingMethod != null) {
					line("virtual ${parentMethod.returnTypeWithThis.targetNameRef} ${parentMethod.targetName}($argsString) { $returnStr this->${callingMethod.targetName}($argsCallString); }")
				}
			}
			line("static bool SI_once;")
			line("static void SI();")

			val ids = (clazz.thisAndAncestors + clazz.allInterfacesInAncestors).distinct().map { it.classId }.filterNotNull() + listOf(-1)
			line("static const TYPE_INFO TABLE_INFO;")

			//line("static ${clazz.cppName} *GET(java_lang_Object *obj);")
			//line("static ${clazz.cppName} *GET_npe(java_lang_Object *obj, const wchar_t *location);")
		}
		line("};")

		/*line("${clazz.cppName} *${clazz.cppName}::GET(java_lang_Object *obj)") {
            line("return dynamic_cast<${clazz.cppName}*>(obj);")
        }

        line("${clazz.cppName} *${clazz.cppName}::GET_npe(java_lang_Object *obj, const wchar_t *location)") {
            line("return dynamic_cast<${clazz.cppName}*>(obj);")
        }*/

	}

	@Suppress("LoopToCallChain")
	fun writeClassImpl(clazz: AstClass): Indenter = Indenter {
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

		for (memberCond in clazz.nativeMembers) {
			condWrapper(memberCond.cond) {
				for (member in memberCond.members) {
					if (member.startsWith("static ")) {
						line(member.replace("###", "${clazz.cppName}::").replace("static ", "").template("native members 2"))
					}
				}
			}
		}

		line("void ${clazz.cppName}::SI() {")
		indent {
			line("""TRACE_REGISTER("${clazz.cppName}::SI");""")
			for (field in clazz.fields.filter { it.isStatic }) {
				if (field.isStatic) {
					val cst = if (field.hasConstantValue) field.constantValue.escapedConstant else field.type.nativeDefaultString
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

	fun writeField(field: AstField): Indenter = Indenter {
		val clazz = field.containingClass
		if (field.isStatic) {
			line("${field.type.targetNameRef} ${clazz.cppName}::${field.targetName} = ${field.type.nativeDefaultString};")
		}
	}

	fun writeMethod(method: AstMethod): Indenter = Indenter {
		val clazz = method.containingClass
		val type = method.methodType

		val argsString = type.args.map { it.type.targetNameRef + " " + it.name }.joinToString(", ")

		line("${method.returnTypeWithThis.targetNameRef} ${clazz.cppName}::${method.targetName}($argsString)") {
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

			fun genJavaBody() = Indenter {
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
					if (cond.startsWith("!")) line("#ifndef ${cond.substring(1)}") else line("#ifdef $cond")
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


	fun genJniMethod(method: AstMethod) = Indenter {
		//if (method.isOverloaded) {
		//	mangledJniFunctionName = JniUtils.mangleLongJavaMethod(method);
		//} else {
		//val mangledJniFunctionName = JniUtils.mangleShortJavaMethod(method);
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
		line("static void* nativePointer = nullptr;")

		//{% CLASS ${method.containingClass.fqname} %}
		line("func_ptr_t fptr = (func_ptr_t)DYN::jtvmResolveNative(N::resolveClass(L\"${method.containingClass.fqname}\"), \"${JniUtils.mangleShortJavaMethod(method)}\", \"${JniUtils.mangleLongJavaMethod(method)}\", &nativePointer);")

		fun genJavaToJniCast(arg: AstType): String {
			if (arg is AstType.REF) {
				return "(${referenceToNativeType(arg)})"
			} else if (arg is AstType.ARRAY) {
				return "(${arrayToNativeType(arg)})"
			} else {
				return "";
			}
		}

		fun genJniToJavaCast(arg: AstType): String {
			return "(${arg.targetNameRef})"
		}

		val sb2 = StringBuilder(30)
		for (i in method.methodType.args.indices) {
			val arg = method.methodType.args[i].type
			sb2.append(", ${genJavaToJniCast(arg)}p${i}")
		}
		line("return ${genJniToJavaCast(method.actualRetType)}fptr(N::getJniEnv(), NULL $sb2);")
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

	override fun processCallArg(e: AstExpr, str: String, targetType: AstType) = doArgCast(targetType, str)

	override val AstType.nativeDefaultString: String
		get() {
			if (this is AstType.REF) {
				val clazz = program[this]!!
				if (clazz.isNative) {
					val nativeInfo = clazz.nativeNameInfo
					if (nativeInfo != null && nativeInfo.defaultValue.isNotEmpty()) {
						return nativeInfo.defaultValue
					}
				}
			}
			return this.nativeDefault.escapedConstant
		}

	override val AstLocal.decl: String get() = "${this.type.targetNameRef} ${this.targetName} = (${this.type.targetNameRef})${this.type.nativeDefaultString};"

	override fun genExprArrayLength(e: AstExpr.ARRAY_LENGTH): String = "((JA_0*)${e.array.genNotNull()})->length"
	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String): String {
		val getMethod = if (context.useUnsafeArrays) "get" else "fastGet"
		return doCast(arrayType, array) + "->$getMethod($index)"
	}

	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String): String {
		val setMethod = if (context.useUnsafeArrays) "set" else "fastSet"
		return doCast(arrayType, array) + "->$setMethod($index, $value)" + ";"
	}

	private fun doCast(target: AstType, expr: String, from: AstType? = null, npe: Boolean = true): String {
		if (target is AstType.REF) {
			return getPtr(program[target]!!, expr, npe = npe)
		} else {
			return "((${target.targetNameRefCast})($expr))"
		}
	}

	private fun doArgCast(target: AstType, expr: String, from: AstType? = null): String {
		if (ENABLE_TYPING) {
			return doCast(target, expr, from, npe = false)
		} else {
			return "((${target.targetNameRef})($expr))"
		}
	}

	private fun getPtr(clazz: AstClass, objStr: String, npe: Boolean = true): String {
		// http://www.cplusplus.com/doc/tutorial/typecasting/
		if (objStr == "null" || objStr == "nullptr") {
			return "(${clazz.cppNameRefCast})(nullptr)"
		}
		if (clazz.isInterface) {
			if (npe) {
				return "(dynamic_cast<${clazz.cppNameRefCast}>(N::ensureNpe($objStr, FUNCTION_NAME)))"
			} else {
				return "(dynamic_cast<${clazz.cppNameRefCast}>($objStr))"
			}
		} else {
			if (clazz.name.targetName == JAVA_LANG_OBJECT) {
				return "((p_java_lang_Object)($objStr))"
				//return "($objStr)"
				//return "(dynamic_cast<${clazz.cppNameRef}>(N::ensureNpe($objStr, FUNCTION_NAME)))"
			} else {
				if (npe) {
					return "(static_cast<${clazz.cppNameRefCast}>(N::ensureNpe($objStr, FUNCTION_NAME)))"
				} else {
					return "(static_cast<${clazz.cppNameRefCast}>($objStr))"
				}
			}
			//return "(static_cast<${clazz.cppNameRef}>(N::ensureNpe($objStr, FUNCTION_NAME)))"
			//return "((${clazz.cppNameRef})(N::ensureNpe($objStr, FUNCTION_NAME)))"
		}
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

	override fun genExprCallBaseInstance(e2: AstExpr.CALL_INSTANCE, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>, isNativeCall: Boolean): String {
		//return "((${refMethodClass.cppName}*)(${e2.obj.genNotNull()}.get()))$methodAccess(${args.joinToString(", ")})"
		if (isThisOrThisWithCast(e2.obj.value)) {
			return "this$methodAccess(${args.joinToString(", ")})"
		} else {
			val objStr = e2.obj.genNotNull()
			return "${getPtr(refMethodClass, objStr)}$methodAccess(${args.joinToString(", ")})"
		}
	}

	override fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>, isNativeCall: Boolean): String {
		val superMethod = refMethodClass[method.withoutClass] ?: invalidOp("Can't find super for method : $method")
		return "${refMethodClass.ref.cppName}::${superMethod.targetName}(${args.joinToString(", ")})"
	}

	override fun genExprThis(e: AstExpr.THIS): String = genExprThis()
	fun genExprThis(): String = "this" //->sptr()"
	override fun genExprMethodClass(e: AstExpr.INVOKE_DYNAMIC_METHOD): String = "N::dummyMethodClass()"

	override val AstType.targetNameRef: String
		get() {
			if (ENABLE_TYPING) {
				return getTypeTargetName(this, ref = true)
			} else {
				if (this is AstType.Reference) {
					if (this is AstType.REF) {
						val clazz = program[this]!!
						val nativeName = clazz.nativeName
						if (nativeName != null) {
							return nativeName
						}
					}
					return "p_java_lang_Object"
				} else {
					return getTypeTargetName(this, ref = true)
				}
			}
		}

	val AstType.targetNameRefCast: String get() = getTypeTargetName(this, ref = true)


	//override val AstType.targetNameRefBounds: String get() {
	//	return if (this is AstType.Reference) {
	//		"p_java_lang_Object"
	//	} else {
	//		getTypeTargetName(this, ref = true)
	//	}
	//}

	override fun genBody2WithFeatures(method: AstMethod, body: AstBody): Indenter = Indenter {
		if (method.isSynchronized) {
			line("SynchronizedMethodLocker __locker(" + getMonitorLockedObjectExpr(method).genExpr() + ");")
		}
		line(genBody2WithFeatures2(method, body))
	}

	override fun N_i2b(str: String) = "((int8_t)($str))"
	override fun N_i2c(str: String) = "((uint16_t)($str))"
	override fun N_i2s(str: String) = "((int16_t)($str))"
	override fun N_f2i(str: String) = "N::f2i($str)"
	override fun N_d2i(str: String) = "N::d2i($str)"

	override fun N_i2f(str: String) = "((float)($str))"
	override fun N_i2d(str: String) = "((double)($str))"

	override fun N_j2f(str: String) = "((float)($str))"
	override fun N_j2d(str: String) = "((double)($str))"

	//override fun N_i(str: String) = "((int32_t)($str))"
	override fun N_i(str: String) = str

	override fun N_idiv(l: String, r: String) = N_func("idiv", "$l, $r")
	override fun N_irem(l: String, r: String) = N_func("irem", "$l, $r")

	override fun N_ishl(l: String, r: String) = N_func("ishl", "$l, $r")
	override fun N_ishr(l: String, r: String) = N_func("ishr", "$l, $r")
	override fun N_iushr(l: String, r: String) = N_func("iushr", "$l, $r")

	override fun N_ishl_cst(l: String, r: Int) = N_func("ishl_cst", "$l, $r")
	override fun N_ishr_cst(l: String, r: Int) = N_func("ishr_cst", "$l, $r")
	override fun N_iushr_cst(l: String, r: Int) = N_func("iushr_cst", "$l, $r")

	override fun N_lshl_cst(l: String, r: Int) = N_func("lshl_cst", "$l, $r")
	override fun N_lshr_cst(l: String, r: Int) = N_func("lshr_cst", "$l, $r")
	override fun N_lushr_cst(l: String, r: Int) = N_func("lushr_cst", "$l, $r")

	override fun N_frem(l: String, r: String) = "::fmod($l, $r)"
	override fun N_drem(l: String, r: String) = "::fmod($l, $r)"

	override fun N_lneg(str: String) = "(-($str))"
	override fun N_linv(str: String) = "(~($str))"

	override fun N_ladd(l: String, r: String) = "(($l) + ($r))"
	override fun N_lsub(l: String, r: String) = "(($l) - ($r))"
	override fun N_lmul(l: String, r: String) = "(($l) * ($r))"
	override fun N_ldiv(l: String, r: String) = "N::ldiv($l, $r)"
	override fun N_lrem(l: String, r: String) = "N::lrem($l, $r)"
	override fun N_lshl(l: String, r: String) = N_func("lshl", "$l, $r")
	override fun N_lshr(l: String, r: String) = N_func("lshr", "$l, $r")
	override fun N_lushr(l: String, r: String) = N_func("lushr", "$l, $r")
	override fun N_lor(l: String, r: String) = "(($l) | ($r))"
	override fun N_lxor(l: String, r: String) = "(($l) ^ ($r))"
	override fun N_land(l: String, r: String) = "(($l) & ($r))"

	override fun N_obj_eq(l: String, r: String) = "(($l) == ($r))"
	override fun N_obj_ne(l: String, r: String) = "(($l) != ($r))"

	override fun genStmSetFieldStaticActual(stm: AstStm.SET_FIELD_STATIC, left: String, field: AstFieldRef, right: String): Indenter = indent {
		line("$left = (${field.type.targetNameRef})($right);")
	}

	override fun genStmReturnVoid(stm: AstStm.RETURN_VOID, last: Boolean): Indenter = Indenter {
		line(if (context.method.methodVoidReturnThis) "return " + genExprThis() + ";" else "return;")
	}

	override fun genStmReturnValue(stm: AstStm.RETURN, last: Boolean): Indenter = Indenter {
		line("return (${context.method.returnTypeWithThis.targetNameRef})${stm.retval.genExpr()};")
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
			return buildInstanceField(doCast(e.field.containingTypeRef, e.expr.genNotNull()), fixField(e.field))
		}
	}

	override fun actualSetField(stm: AstStm.SET_FIELD_INSTANCE, left: String, right: String): String {
		val left2 = if (stm.left.value is AstExpr.THIS) {
			buildInstanceField("this", fixField(stm.field))
		} else {

			buildInstanceField(doCast(stm.field.containingTypeRef, stm.left.genNotNull()), fixField(stm.field))
		}
		val right2 = "(${stm.field.type.targetNameRef})((${stm.field.type.targetNameRef})(" + stm.expr.genExpr() + "))"

		return "$left2 = $right2;"
	}

	override fun actualSetLocal(stm: AstStm.SET_LOCAL, localName: String, exprStr: String): String {
		return "$localName = (${stm.local.type.targetNameRef})($exprStr);"
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

	//override fun genExprObjectArrayLit(e: AstExpr.OBJECTARRAY_LITERAL): String {
	//	noImpl("C++ genExprStringArrayLit")
	//}

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

	override fun genStmRawTry(trap: AstTrap): Indenter = Indenter {
		//line("try {")
		//_indent()
	}

	override fun genStmRawCatch(trap: AstTrap): Indenter = Indenter {
		//_unindent()
		//line("} catch (SOBJ e) {")
		//indent {
		//	line("if (N::is(e, ${getClassId(trap.exception.name)})) goto ${trap.handler.name};")
		//	line("throw e;")
		//}
		//line("}")
	}

	override fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter {
		val values = stm.values.map { it.genExpr() }
		line("") {
			line("const ${stm.array.type.elementType.targetNameRef} ARRAY_LITERAL[${values.size}] = { ${values.joinToString(", ")} };")

			line(doCast(stm.array.type, stm.array.genExpr()) + "->setArray(${stm.startIndex}, ${values.size}, ARRAY_LITERAL);")
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

	override fun access(name: String, static: Boolean, field: Boolean): String = if (static) "::$name" else "->$name"

	override val NullType = "p_java_lang_Object"
	override val VoidType = "void"
	override val BoolType = "int8_t"
	override val IntType = "int32_t"
	override val ShortType = "int16_t"
	override val CharType = "uint16_t"
	override val ByteType = "int8_t"
	override val FloatType = "float"
	override val DoubleType = "double"
	override val LongType = "int64_t"

	override val BaseArrayTypeRef = "p_JA_0"
	override val BoolArrayTypeRef = "p_JA_Z"
	override val ByteArrayTypeRef = "p_JA_B"
	override val CharArrayTypeRef = "p_JA_C"
	override val ShortArrayTypeRef = "p_JA_S"
	override val IntArrayTypeRef = "p_JA_I"
	override val LongArrayTypeRef = "p_JA_J"
	override val FloatArrayTypeRef = "p_JA_F"
	override val DoubleArrayTypeRef = "p_JA_D"
	override val ObjectArrayTypeRef = "p_JA_L"

//override val FieldRef.targetName: String get() {
//	val fieldRef = this
//	val ref = fieldRef.ref
//	return getClassNameAllocator(ref.containingClass).allocate(ref) { "F_" + normalizeName(ref.name + "_" + ref.type.mangle()) }
//}

	override val DoubleNegativeInfinityString = "-N::INFINITY_DOUBLE"
	override val DoublePositiveInfinityString = "N::INFINITY_DOUBLE"
	override val DoubleNanString = "N::NAN_DOUBLE"

	override val FloatNegativeInfinityString = "-N::INFINITY_FLOAT"
	override val FloatPositiveInfinityString = "N::INFINITY_FLOAT"
	override val FloatNanString = "N::NAN_FLOAT"

	override val String.escapeString: String get() = "STRINGLIT_${allocString(currentClass, this)}${this.toCommentString()}"
	override val AstType.escapeType: String get() = N_func("resolveClass", "L${this.mangle().uquote()}")

	override fun pquote(str: String): String = "L" + str.uquote()

	override fun N_lnew(value: Long): String {
		if (value == Long.MIN_VALUE) {
			//return "(int64_t)(0x8000000000000000L"
			return "(int64_t)(-${Long.MAX_VALUE}LL - 1)"
		} else {
			return "(int64_t)(${value}LL)"
		}
	}

	override val FieldRef.targetName: String get() = getNativeName(this)

	override val MethodRef.targetNameBase: String
		get() {
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
		val clazz = field.ref.getClass(program)
		val actualField = clazz[field.ref]
		return getClassNameAllocator(actualField.ref.containingClass).allocate(actualField.ref) { "F_" + normalizeName(actualField.ref.name + "_" + clazz.fqname + "_" + actualField.ref.type.mangle(), NameKind.FIELD) }
	}

	override val AstMethodRef.objectToCache: Any get() = this

	override fun buildStaticInit(clazzName: FqName): String? = null

	override fun escapedConstant(v: Any?, place: ConstantPlace): String = when (v) {
		null -> "nullptr"
		else -> super.escapedConstant(v, place)
	}

	override fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String {
		if (from == to) return e;
		if (from is AstType.NULL) return e
		if (from is AstType.Reference && to is AstType.REF) {
			val toCls = program[to]!!
			if (toCls.isInterface) {
				return "(N::CC_CHECK_UNTYPED($e, ${toCls.classId}))"
				//return e
			} else {
				return "(N::CC_CHECK_CLASS<${getTypeTargetName(to, ref = true)}>($e, ${toCls.classId}))"
				//return e
			}
			//}
		}
		return "(N::CC_CHECK_GENERIC<${getTypeTargetName(to, ref = true)}>($e))"
		//return e
	}

	override fun genStmMonitorEnter(stm: AstStm.MONITOR_ENTER) = Indenter("N::monitorEnter(" + stm.expr.genExpr() + ");")
	override fun genStmMonitorExit(stm: AstStm.MONITOR_EXIT) = Indenter("N::monitorExit(" + stm.expr.genExpr() + ");")
}
