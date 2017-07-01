package com.jtransc.gen.js

import com.jtransc.ConfigOutputFile
import com.jtransc.ConfigTargetDirectory
import com.jtransc.annotation.JTranscCustomMainList
import com.jtransc.ast.*
import com.jtransc.ast.feature.method.SwitchFeature
import com.jtransc.ds.Allocator
import com.jtransc.ds.getOrPut2
import com.jtransc.error.invalidOp
import com.jtransc.gen.GenTargetDescriptor
import com.jtransc.gen.TargetBuildTarget
import com.jtransc.gen.common.*
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.io.ProcessResult2
import com.jtransc.log.log
import com.jtransc.sourcemaps.Sourcemaps
import com.jtransc.text.Indenter
import com.jtransc.text.Indenter.Companion
import com.jtransc.text.isLetterDigitOrUnderscore
import com.jtransc.text.quote
import com.jtransc.vfs.ExecOptions
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.LocalVfsEnsureDirs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*

class JsTarget() : GenTargetDescriptor() {
	override val priority = 500
	override val name = "js"
	override val longName = "Javascript"
	override val outputExtension = "js"
	override val extraLibraries = listOf<String>()
	override val extraClasses = listOf<String>()
	override val runningAvailable: Boolean = true

	override val buildTargets: List<TargetBuildTarget> = listOf(
		TargetBuildTarget("js", "js", "program.js", minimizeNames = true),
		TargetBuildTarget("plainJs", "js", "program.js", minimizeNames = true)
	)

	override fun getGenerator(injector: Injector): CommonGenerator {
		val settings = injector.get<AstBuildSettings>()
		val configTargetDirectory = injector.get<ConfigTargetDirectory>()
		val configOutputFile = injector.get<ConfigOutputFile>()
		val targetFolder = LocalVfsEnsureDirs(File("${configTargetDirectory.targetDirectory}/jtransc-js"))
		injector.mapInstance(CommonGenFolders(settings.assets.map { LocalVfs(it) }))
		injector.mapInstance(ConfigTargetFolder(targetFolder))
		injector.mapInstance(ConfigSrcFolder(targetFolder))
		injector.mapInstance(ConfigOutputFile2(targetFolder[configOutputFile.outputFileBaseName].realfile))
		injector.mapImpl<IProgramTemplate, IProgramTemplate>()
		return injector.get<JsGenerator>()
	}

	override fun getTargetByExtension(ext: String): String? = when (ext) {
		"js" -> "js"
		else -> null
	}
}

data class ConfigJavascriptOutput(val javascriptOutput: SyncVfsFile)

fun hasSpecialChars(name: String): Boolean = !name.all(Char::isLetterDigitOrUnderscore)

@Suppress("ConvertLambdaToReference")
@Singleton
class JsGenerator(injector: Injector) : CommonGenerator(injector) {
	override val SINGLE_FILE: Boolean = true
	override val ADD_UTF8_BOM = true
	override val GENERATE_LINE_NUMBERS = false

	override val methodFeatures = super.methodFeatures + setOf(SwitchFeature::class.java)
	override val keywords = super.keywords + setOf("name", "constructor", "prototype", "__proto__", "G", "N", "S", "SS", "IO")
	override val stringPoolType = StringPool.Type.GLOBAL
	override val floatHasFSuffix: Boolean = false

	override val optionalDoubleDummyDecimals = true

	override fun compileAndRun(redirect: Boolean): ProcessResult2 = _compileRun(run = true, redirect = redirect)
	override fun compile(): ProcessResult2 = _compileRun(run = false, redirect = false)

	private fun commonAccess(name: String, field: Boolean): String = if (hasSpecialChars(name)) "[${name.quote()}]" else ".$name"
	override fun staticAccess(name: String, field: Boolean): String = commonAccess(name, field)
	override fun instanceAccess(name: String, field: Boolean): String = commonAccess(name, field)

	val jsOutputFile by lazy { injector.get<ConfigJavascriptOutput>().javascriptOutput }

	fun _compileRun(run: Boolean, redirect: Boolean): ProcessResult2 {

		log.info("Generated javascript at..." + jsOutputFile.realpathOS)

		if (run) {
			val result = CommonGenCliCommands.runProgramCmd(
				program,
				target = "js",
				default = listOf("node", "{{ outputFile }}"),
				template = this,
				options = ExecOptions(passthru = redirect)
			)
			return ProcessResult2(result)
		} else {
			return ProcessResult2(0)
		}
	}

	override fun run(redirect: Boolean): ProcessResult2 = ProcessResult2(0)

	@Suppress("UNCHECKED_CAST")
	override fun writeClasses(output: SyncVfsFile) {
		val concatFilesTrans = copyFiles(output)

		val classesIndenter = arrayListOf<Indenter>()

		classesIndenter += genSingleFileClassesWithoutAppends(output)

		val SHOW_SIZE_REPORT = true
		if (SHOW_SIZE_REPORT) {
			for ((clazz, text) in indenterPerClass.toList().map { it.first to it.second.toString() }.sortedBy { it.second.length }) {
				log.info("CLASS SIZE: ${clazz.fqname} : ${text.length}")
			}
		}

		val mainClassFq = program.entrypoint
		val mainClass = mainClassFq.targetClassFqName
		//val mainMethod = program[mainClassFq].getMethod("main", AstType.build { METHOD(VOID, ARRAY(STRING)) }.desc)!!.jsName
		val mainMethod = "main"
		entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		entryPointFilePath = entryPointClass.targetFilePath
		val entryPointFqName = entryPointClass.targetGeneratedFqName
		val entryPointSimpleName = entryPointClass.targetSimpleName
		val entryPointPackage = entryPointFqName.packagePath

		val customMain = program.allAnnotationsList.getTypedList(JTranscCustomMainList::value).firstOrNull { it.target == "js" }?.value

		log("Using ... " + if (customMain != null) "customMain" else "plainMain")

		setExtraData(mapOf(
			"entryPointPackage" to entryPointPackage,
			"entryPointSimpleName" to entryPointSimpleName,
			"mainClass" to mainClass,
			"mainClass2" to mainClassFq.fqname,
			"mainMethod" to mainMethod
		))

		val strs = Indenter {
			val strs = getGlobalStrings()
			val maxId = strs.maxBy { it.id }?.id ?: 0
			line("SS = new Array($maxId);")
			for (e in strs) line("SS[${e.id}] = ${e.str.quote()};")
		}

		val out = Indenter {
			if (settings.debug) line("//# sourceMappingURL=$outputFileBaseName.map")
			line(concatFilesTrans.prepend)
			line(strs.toString())
			for (indent in classesIndenter) line(indent)
			val mainClassClass = program[mainClassFq]

			line("__createJavaArrays();")
			line("__buildStrings();")
			line("N.linit();")
			line(genStaticConstructorsSorted())
			//line(buildStaticInit(mainClassFq))
			val mainMethod2 = mainClassClass[AstMethodRef(mainClassFq, "main", AstType.METHOD(AstType.VOID, listOf(ARRAY(AstType.STRING))))]
			val mainCall = buildMethod(mainMethod2, static = true)
			line("try {")
			indent {
				line("$mainCall(N.strArray(N.args()));")
			}
			line("} catch (e) {")
			indent {
				line("console.error(e);")
				line("console.error(e.stack);")
			}
			line("}")
			line(concatFilesTrans.append)
		}

		val sources = Allocator<String>()
		val mappings = hashMapOf<Int, Sourcemaps.MappingItem>()

		val source = out.toString { sb, line, data ->
			if (settings.debug && data is AstStm.LINE) {
				//println("MARKER: ${sb.length}, $line, $data, ${clazz.source}")
				mappings[line] = Sourcemaps.MappingItem(
					sourceIndex = sources.allocateOnce(data.file),
					sourceLine = data.line,
					sourceColumn = 0,
					targetColumn = 0
				)
				//clazzName.internalFqname + ".java"
			}
		}

		val sourceMap = if (settings.debug) Sourcemaps.encodeFile(sources.array, mappings) else null
		// Generate source
		//println("outputFileBaseName:$outputFileBaseName")
		output[outputFileBaseName] = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + source.toByteArray(Charsets.UTF_8)
		if (sourceMap != null) output[outputFileBaseName + ".map"] = sourceMap

		injector.mapInstance(ConfigJavascriptOutput(output[outputFile]))
	}

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (J__i__exception__)") {
			//line("J__exception__ = J__i__exception__.native || J__i__exception__;")
			line("J__exception__ = J__i__exception__.javaThrowable || J__i__exception__;")
			line(stm.catch.genStm())
		}
	}

	override fun genStmRethrow(stm: AstStm.RETHROW, last: Boolean) = indent { line("throw J__i__exception__;") }

	override fun genBodyLocals(locals: List<AstLocal>) = indent {
		if (locals.isNotEmpty()) {
			val vars = locals.map { local -> "${local.targetName} = ${local.type.nativeDefaultString}" }.joinToString(", ")
			line("var $vars;")
		}
	}

	override val AstLocal.decl: String get() = "var ${this.targetName} = ${this.type.nativeDefaultString};"

	override fun genBodyTrapsPrefix() = indent { line("var J__exception__ = null;") }
	override fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(buildStaticInit(clazzRef.name))
	}

	override fun N_AGET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String) = "($array.data[$index])"
	override fun N_ASET_T(arrayType: AstType.ARRAY, elementType: AstType, array: String, index: String, value: String) = "$array.data[$index] = $value;"

	override fun N_is(a: String, b: AstType.Reference): String {
		return when (b) {
			is AstType.REF -> {
				val clazz = program[b]!!
				if (clazz.isInterface) {
					"N.isClassId($a, ${clazz.classId})"
				} else {
					"($a instanceof ${b.targetName})"
				}
			}
			else -> {
				super.N_is(a, b)
			}
		}
	}

	override fun N_is(a: String, b: String) = "N.is($a, $b)"
	override fun N_z2i(str: String) = "N.z2i($str)"
	override fun N_i(str: String) = "(($str)|0)"
	override fun N_i2z(str: String) = "(($str)!=0)"
	override fun N_i2b(str: String) = "(($str)<<24>>24)" // shifts use 32-bit integers
	override fun N_i2c(str: String) = "(($str)&0xFFFF)"
	override fun N_i2s(str: String) = "(($str)<<16>>16)" // shifts use 32-bit integers
	override fun N_f2i(str: String) = "(($str)|0)"
	override fun N_i2i(str: String) = N_i(str)
	override fun N_i2j(str: String) = "N.i2j($str)"
	override fun N_i2f(str: String) = "Math.fround(+($str))"
	override fun N_i2d(str: String) = "+($str)"
	override fun N_f2f(str: String) = "Math.fround($str)"
	override fun N_f2d(str: String) = "($str)"
	override fun N_d2f(str: String) = "Math.fround(+($str))"
	override fun N_d2i(str: String) = "(($str)|0)"
	override fun N_d2d(str: String) = "+($str)"
	override fun N_j2i(str: String) = "N.j2i($str)"
	override fun N_j2j(str: String) = str
	override fun N_j2f(str: String) = "Math.fround(N.j2d($str))"
	override fun N_j2d(str: String) = "N.j2d($str)"
	override fun N_getFunction(str: String) = "N.getFunction($str)"
	override fun N_c(str: String, from: AstType, to: AstType) = "($str)"
	override fun N_ineg(str: String) = "-($str)"
	override fun N_iinv(str: String) = "~($str)"
	override fun N_fneg(str: String) = "-($str)"
	override fun N_dneg(str: String) = "-($str)"
	override fun N_znot(str: String) = "!($str)"
	override fun N_imul(l: String, r: String): String = "Math.imul($l, $r)"

	override val String.escapeString: String get() = "S[" + allocString(context.clazz.name, this) + "]"

	override fun genExprCallBaseSuper(e2: AstExpr.CALL_SUPER, clazz: AstType.REF, refMethodClass: AstClass, method: AstMethodRef, methodAccess: String, args: List<String>): String {
		val superMethod = refMethodClass[method.withoutClass] ?: invalidOp("Can't find super for method : $method")
		val base = superMethod.containingClass.name.targetName + ".prototype"
		val argsString = (listOf(e2.obj.genExpr()) + args).joinToString(", ")
		return "$base$methodAccess.call($argsString)"
	}

	private fun AstMethod.getJsNativeBodies(): Map<String, Indenter> = this.getNativeBodies(target = "js")

	override fun genClass(clazz: AstClass): List<ClassResult> {
		setCurrentClass(clazz)

		val isAbstract = (clazz.classType == AstClassType.ABSTRACT)
		refs._usedDependencies.clear()

		if (!clazz.extending?.fqname.isNullOrEmpty()) refs.add(AstType.REF(clazz.extending!!))
		for (impl in clazz.implementing) refs.add(AstType.REF(impl))

		val classCodeIndenter = Indenter {
			if (isAbstract) line("// ABSTRACT")

			val classBase = clazz.name.targetName
			val memberBaseStatic = classBase
			val memberBaseInstance = "$classBase.prototype"
			fun getMemberBase(isStatic: Boolean) = if (isStatic) memberBaseStatic else memberBaseInstance
			val parentClassBase = if (clazz.extending != null) clazz.extending!!.targetName else "java_lang_Object_base";

			val staticFields = clazz.fields.filter { it.isStatic }
			//val instanceFields = clazz.fields.filter { !it.isStatic }
			val allInstanceFields = (listOf(clazz) + clazz.parentClassList).flatMap { it.fields }.filter { !it.isStatic }

			fun lateInitField(a: Any?) = (a is String)

			val allInstanceFieldsThis = allInstanceFields.filter { lateInitField(it) }
			val allInstanceFieldsProto = allInstanceFields.filter { !lateInitField(it) }

			line("function $classBase()") {
				for (field in allInstanceFieldsThis) {
					val nativeMemberName = if (field.targetName == field.name) field.name else field.targetName
					line("this${instanceAccess(nativeMemberName, field = true)} = ${field.escapedConstantValue};")
				}
			}

			line("$classBase.prototype = Object.create($parentClassBase.prototype);")
			line("$classBase.prototype.constructor = $classBase;")

			for (field in allInstanceFieldsProto) {
				val nativeMemberName = if (field.targetName == field.name) field.name else field.targetName
				line("$classBase.prototype${instanceAccess(nativeMemberName, field = true)} = ${field.escapedConstantValue};")
			}

			// @TODO: Move to genSIMethodBody
			//if (staticFields.isNotEmpty() || clazz.staticConstructor != null) {
			//	line("$classBase.SI = function()", after2 = ";") {
			//		line("$classBase.SI = N.EMPTY_FUNCTION;")
			//		for (field in staticFields) {
			//			val nativeMemberName = if (field.targetName == field.name) field.name else field.targetName
			//			line("${getMemberBase(field.isStatic)}${accessStr(nativeMemberName)} = ${field.escapedConstantValue};")
			//		}
			//		if (clazz.staticConstructor != null) {
			//			line("$classBase${getTargetMethodAccess(clazz.staticConstructor!!, true)}();")
			//		}
			//	}
			//} else {
			//	line("$classBase.SI = N.EMPTY_FUNCTION;")
			//}
			//line("$classBase.SI = N.EMPTY_FUNCTION;")

			if (staticFields.isNotEmpty() || clazz.staticConstructor != null) {
				line("$classBase.SI = function()", after2 = ";") {
					//line("$classBase.SI = N.EMPTY_FUNCTION;")
					for (field in staticFields) {
						val nativeMemberName = if (field.targetName == field.name) field.name else field.targetName
						line("${getMemberBase(field.isStatic)}${instanceAccess(nativeMemberName, field = true)} = ${field.escapedConstantValue};")
					}
					if (clazz.staticConstructor != null) {
						line("$classBase${getTargetMethodAccess(clazz.staticConstructor!!, true)}();")
					}
				}
			} else {
				line("$classBase.SI = function(){};")
			}

			val relatedTypesIds = (clazz.getAllRelatedTypes() + listOf(JAVA_LANG_OBJECT_CLASS)).toSet().map { it.classId }
			line("$classBase.prototype.__JT__CLASS_ID = $classBase.__JT__CLASS_ID = ${clazz.classId};")
			line("$classBase.prototype.__JT__CLASS_IDS = $classBase.__JT__CLASS_IDS = [${relatedTypesIds.joinToString(",")}];")

			//renderFields(clazz.fields);

			fun writeMethod(method: AstMethod): Indenter {
				setCurrentMethod(method)
				return Indenter {
					refs.add(method.methodType)
					val margs = method.methodType.args.map { it.name }

					//val defaultMethodName = if (method.isInstanceInit) "${method.ref.classRef.fqname}${method.name}${method.desc}" else "${method.name}${method.desc}"
					//val methodName = if (method.targetName == defaultMethodName) null else method.targetName
					val nativeMemberName = buildMethod(method, false, includeDot = false)
					val prefix = "${getMemberBase(method.isStatic)}${instanceAccess(nativeMemberName, field = false)}"

					val rbody = if (method.body != null) method.body else if (method.bodyRef != null) program[method.bodyRef!!]?.body else null

					fun renderBranch(actualBody: Indenter?) = Indenter {
						if (actualBody != null) {
							line("$prefix = function(${margs.joinToString(", ")})", after2 = ";") {
								line(actualBody)
								if (method.methodVoidReturnThis) line("return this;")
							}
						} else {
							line("$prefix = function() { N.methodWithoutBody('${clazz.name}.${method.name}') };")
						}
					}

					fun renderBranches() = Indenter {
						try {
							val nativeBodies = method.getJsNativeBodies()
							var javaBodyCacheDone: Boolean = false
							var javaBodyCache: Indenter? = null
							fun javaBody(): Indenter? {
								if (!javaBodyCacheDone) {
									javaBodyCacheDone = true
									javaBodyCache = rbody?.genBodyWithFeatures(method)
								}
								return javaBodyCache
							}
							//val javaBody by lazy {  }

							// @TODO: Do not hardcode this!
							if (nativeBodies.isEmpty() && javaBody() == null) {
								line(renderBranch(null))
							} else {
								if (nativeBodies.isNotEmpty()) {
									val default = if ("" in nativeBodies) nativeBodies[""]!! else javaBody() ?: Indenter.EMPTY
									val options = nativeBodies.filter { it.key != "" }.map { it.key to it.value } + listOf("" to default)

									if (options.size == 1) {
										line(renderBranch(default))
									} else {
										for (opt in options.withIndex()) {
											if (opt.index != options.size - 1) {
												val iftype = if (opt.index == 0) "if" else "else if"
												line("$iftype (${opt.value.first})") { line(renderBranch(opt.value.second)) }
											} else {
												line("else") { line(renderBranch(opt.value.second)) }
											}
										}
									}
									//line(nativeBodies ?: javaBody ?: Indenter.EMPTY)
								} else {
									line(renderBranch(javaBody()))
								}
							}
						} catch (e: Throwable) {
							log.printStackTrace(e)
							log.warn("WARNING GenJsGen.writeMethod:" + e.message)

							line("// Errored method: ${clazz.name}.${method.name} :: ${method.desc} :: ${e.message};")
							line(renderBranch(null))
						}
					}

					line(renderBranches())
				}
			}

			for (method in clazz.methods.filter { it.isClassOrInstanceInit }) line(writeMethod(method))
			for (method in clazz.methods.filter { !it.isClassOrInstanceInit }) line(writeMethod(method))
		}

		return listOf(ClassResult(SubClass(clazz, MemberTypes.ALL), classCodeIndenter))
	}

	override fun genStmSetArrayLiterals(stm: AstStm.SET_ARRAY_LITERALS) = Indenter {
		line("${stm.array.genExpr()}.setArraySlice(${stm.startIndex}, [${stm.values.map { it.genExpr() }.joinToString(", ")}]);")
	}

	override fun buildStaticInit(clazzName: FqName): String? = null

	override val FqName.targetName: String get() = classNames.getOrPut2(this) { if (minimize) allocClassName() else this.fqname.replace('.', '_') }

	override fun cleanMethodName(name: String): String = name

	override val AstType.localDeclType: String get() = "var"

	override fun genStmThrow(stm: AstStm.THROW, last: Boolean) = Indenter("throw new WrappedError(${stm.exception.genExpr()});")

	override fun genExprCastChecked(e: String, from: AstType.Reference, to: AstType.Reference): String {
		return "N.checkCast($e, ${to.targetNameRef})"
	}

}