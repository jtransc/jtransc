package com.jtransc.gen.js

import com.jtransc.annotation.JTranscAddMembersList
import com.jtransc.annotation.JTranscCustomMainList
import com.jtransc.annotation.JTranscMethodBodyList
import com.jtransc.ast.*
import com.jtransc.ds.Allocator
import com.jtransc.error.invalidOp
import com.jtransc.error.unexpected
import com.jtransc.gen.common.CommonGenGen
import com.jtransc.lang.toBetterString
import com.jtransc.log.log
import com.jtransc.sourcemaps.Sourcemaps
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.vfs.SyncVfsFile
import java.util.*

class GenJsGen(input: Input) : CommonGenGen(input) {
	val names = cnames as JsNames

	// @TODO: Kotlin IDE: Refactoring imports doesn't take into account: JTranscAddFileList::value so this is a workaround for this
	val _JTranscCustomMainList = JTranscCustomMainList::class.java
	val _JTranscMethodBodyList = JTranscMethodBodyList::class.java
	val _JTranscAddMembersList = JTranscAddMembersList::class.java

	internal fun _write(output: SyncVfsFile): JsProgramInfo {
		val resourcesVfs = program.resourcesVfs
		val copyFiles = getFilesToCopy("js")

		data class ConcatFile(val prepend: String?, val append: String?)
		class CopyFile(val content: ByteArray, val dst: String, val isAsset: Boolean)

		val copyFilesTrans = copyFiles.filter { it.src.isNotEmpty() && it.dst.isNotEmpty() }.map {
			val file = resourcesVfs[it.src]
			if (it.process) {
				CopyFile(file.readString().template("copyfile").toByteArray(), it.dst, it.isAsset)
			} else {
				CopyFile(file.read(), it.dst, it.isAsset)
			}
		}

		// Copy assets
		folders.copyAssetsTo(output)
		for (file in copyFilesTrans) {
			output[file.dst].ensureParentDir().write(file.content)
		}

		templateString.params["assetFiles"] = (templateString.params["assetFiles"] as List<SyncVfsFile>) + copyFilesTrans.map { output[it.dst] }

		val concatFilesTrans = copyFiles.filter { it.append.isNotEmpty() || it.prepend.isNotEmpty() || it.prependAppend.isNotEmpty() }.map {
			val prependAppend = if (it.prependAppend.isNotEmpty()) (resourcesVfs[it.prependAppend].readString() + "\n") else null
			val prependAppendParts = prependAppend?.split("/* ## BODY ## */")

			val prepend = if (prependAppendParts != null && prependAppendParts.size >= 2) prependAppendParts[0] else if (it.prepend.isNotEmpty()) (resourcesVfs[it.prepend].readString() + "\n") else null
			val append = if (prependAppendParts != null && prependAppendParts.size >= 2) prependAppendParts[1] else if (it.append.isNotEmpty()) (resourcesVfs[it.append].readString() + "\n") else null

			fun process(str: String?): String? = if (it.process) str?.template("includeFile") else str

			ConcatFile(process(prepend), process(append))
		}

		val classesIndenter = arrayListOf<Indenter>()

		for (clazz in program.classes.filter { !it.isNative }) {
			if (clazz.implCode != null) {
				classesIndenter.add(Indenter.gen { line(clazz.implCode!!) })
			} else {
				classesIndenter.add(writeClass(clazz))
			}
		}

		val mainClassFq = program.entrypoint
		val mainClass = mainClassFq.jsClassFqName
		//val mainMethod = program[mainClassFq].getMethod("main", AstType.build { METHOD(VOID, ARRAY(STRING)) }.desc)!!.jsName
		val mainMethod = "main"
		val entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		val entryPointFilePath = entryPointClass.jsFilePath
		val entryPointFqName = entryPointClass.jsGeneratedFqName
		val entryPointSimpleName = entryPointClass.jsGeneratedSimpleClassName
		val entryPointPackage = entryPointFqName.packagePath

		val customMain = program.allAnnotationsList.getTypedList(JTranscCustomMainList::value).firstOrNull { it.target == "js" }?.value

		val plainMain = Indenter.genString {
			line("program.registerMainClass('{{ mainClass }}');")
		}

		log("Using ... " + if (customMain != null) "customMain" else "plainMain")

		templateString.setExtraData(mapOf(
			"entryPointPackage" to entryPointPackage,
			"entryPointSimpleName" to entryPointSimpleName,
			"mainClass" to mainClass,
			"mainClass2" to mainClassFq.fqname,
			"mainMethod" to mainMethod
		))

		val strs = Indenter.gen {
			line("program.registerStrings({")
			indent {
				for (e in names.allocatedStrings.entries.sortedBy { it.value }) {
					line("${e.value} : ${e.key.quote()},")
				}
			}
			line("});");
		}

		val out = Indenter.gen {
			if (settings.debug) {
				line("//# sourceMappingURL=program.js.map")
			}

			for (f in concatFilesTrans) if (f.prepend != null) line(f.prepend)

			line(strs.toString())

			for (indent in classesIndenter) line(indent)

			line(templateString.gen(customMain ?: plainMain, context, "customMain"))

			for (f in concatFilesTrans.reversed()) if (f.append != null) line(f.append)
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

		val sourceMap = if (settings.debug) {
			Sourcemaps.encodeFile(sources.array, mappings)
		} else {
			null
		}

		// Generate source

		output[tinfo.outputFileBaseName] = source
		if (sourceMap != null) {
			output[tinfo.outputFileBaseName + ".map"] = sourceMap
		}

		return JsProgramInfo(entryPointClass, entryPointFilePath, output[tinfo.outputFile])
	}

	fun annotation(a: AstAnnotation): String {
		fun escapeValue(it: Any?): String {
			return when (it) {
				null -> "null"
				is AstAnnotation -> annotation(it)
				is Pair<*, *> -> escapeValue(it.second)
				is AstFieldRef -> names.buildStaticField(it)
				is AstFieldWithoutTypeRef -> names.buildStaticField(program[it].ref)
				is String -> "N.boxString(${it.quote()})"
				is Boolean, is Byte, is Short, is Char, is Int, is Long, is Float, is Double -> names.escapeConstant(it)
				is List<*> -> "[" + it.map { escapeValue(it) }.joinToString(", ") + "]"
				is com.jtransc.org.objectweb.asm.Type -> "N.resolveClass(" + it.descriptor.quote() + ")"
				else -> invalidOp("GenJsGen.annotation.escapeValue: Don't know how to handle value ${it.javaClass.name} : ${it.toBetterString()} while generating $context")
			}
		}
		//val itStr = a.elements.map { it.key.quote() + ": " + escapeValue(it.value) }.joinToString(", ")
		val annotation = program.get3(a.type)
		val itStr = annotation.methods.map {
			escapeValue(if (it.name in a.elements) a.elements[it.name]!! else it.defaultTag)
		}.joinToString(", ")
		//return "new ${names.getFullAnnotationProxyName(a.type)}([$itStr])"
		return "R.createAnnotation(${names.getJsClassFqNameForCalling(a.type.name)}, [$itStr])"
	}

	fun annotationInit(a: AstAnnotation): List<AstType.REF> {
		fun escapeValue(it: Any?): List<AstType.REF> {
			return when (it) {
				null -> listOf()
				is AstAnnotation -> annotationInit(it)
				is Pair<*, *> -> escapeValue(it.second)
				is AstFieldRef -> listOf(it.containingTypeRef)
				is AstFieldWithoutTypeRef -> listOf(it.containingClass.ref())
				is List<*> -> it.flatMap { escapeValue(it) }
				else -> listOf()
			}
		}
		//val itStr = a.elements.map { it.key.quote() + ": " + escapeValue(it.value) }.joinToString(", ")
		val annotation = program.get3(a.type)
		return annotation.methods.flatMap {
			escapeValue(if (it.name in a.elements) a.elements[it.name]!! else it.defaultTag)
		}
	}

	fun _visibleAnnotations(annotations: List<AstAnnotation>): String = "[" + annotations.filter { it.runtimeVisible }.map { annotation(it) }.joinToString(", ") + "]"
	fun _visibleAnnotationsList(annotations: List<List<AstAnnotation>>): String = "[" + annotations.map { _visibleAnnotations(it) }.joinToString(", ") + "]"
	fun visibleAnnotations(annotations: List<AstAnnotation>): String = "function() { ${annotationsInit(annotations)} return " + _visibleAnnotations(annotations) + "; }"
	fun visibleAnnotationsList(annotations: List<List<AstAnnotation>>): String = "function() { ${annotationsInit(annotations.flatMap { it })} return " + _visibleAnnotationsList(annotations) + "; }"
	fun visibleAnnotationsOrNull(annotations: List<AstAnnotation>): String = if (annotations.isNotEmpty()) visibleAnnotations(annotations) else "null"
	fun visibleAnnotationsListOrNull(annotations: List<List<AstAnnotation>>): String = if (annotations.isNotEmpty()) visibleAnnotationsList(annotations) else "null"

	fun annotationsInit(annotations: List<AstAnnotation>): Indenter = Indenter.gen {
		for (i in annotations.filter { it.runtimeVisible }.flatMap { annotationInit(it) }.toHashSet()) {
			line(names.getJsClassStaticInit(i, "annotationsInit"))
		}
	}

	override fun genStm2(stm: AstStm): Indenter {
		this.stm = stm
		return Indenter.gen {
			when (stm) {
				is AstStm.SET_NEW_WITH_CONSTRUCTOR -> {
					val newClazz = program[stm.target.name]
					//val mapping = mappings.getClassMapping(newClazz)
					refs.add(stm.target)
					val commaArgs = stm.args.map { it.genExpr() }.joinToString(", ")
					val className = stm.target.nativeTypeNew
					val jsLocalName = stm.local.nativeName

					if (newClazz.nativeName != null) {
						line("$jsLocalName = new $className($commaArgs);")
					} else {
						line("$jsLocalName = new $className();")
						line("$jsLocalName.${stm.method.targetName}($commaArgs);")
					}
				}

				is AstStm.SWITCH -> {
					line("switch (${stm.subject.genExpr()})") {
						for (case in stm.cases) {
							val value = case.first
							val caseStm = case.second
							line("case $value:")
							indent {
								line(caseStm.genStm())
							}
						}
						line("default:")
						indent {
							line(stm.default.genStm())
						}
					}
				}
				else -> line(super.genStm2(stm))
			}
		}
	}

	override fun genStmLine(stm: AstStm.LINE) = indent {
		mark(stm)
	}

	override fun genStmTryCatch(stm: AstStm.TRY_CATCH) = indent {
		line("try") {
			line(stm.trystm.genStm())
		}
		line("catch (J__i__exception__)") {
			line("J__exception__ = J__i__exception__;")
			line(stm.catch.genStm())
		}
	}

	override fun genStmRethrow(stm: AstStm.RETHROW) = indent { line("""HaxeNatives.rethrow(J__i__exception__);""") }

	override fun genBodyLocal(local: AstLocal) = indent { line("var ${local.nativeName} = ${local.type.nativeDefaultString};") }
	override fun genBodyTrapsPrefix() = indent { line("var J__exception__ = null;") }
	override fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(names.getJsClassStaticInit(clazzRef, reasons.joinToString(", ")))
	}

	override fun N_AGET_T(elementType: AstType, array: String, index: String) = "($array.data[$index])"
	override fun N_ASET_T(elementType: AstType, array: String, index: String, value: String) = "$array.data[$index] = $value;"

	override fun N_is(a: String, b: String) = "N.is($a, $b)"
	override fun N_z2i(str: String) = "N.z2i($str)"
	override fun N_i(str: String) = "(($str)|0)"
	override fun N_i2z(str: String) = "(($str)!=0)"
	override fun N_i2b(str: String) = "(($str)<<24>>24)"
	override fun N_i2c(str: String) = "(($str)&0xFFFF)"
	override fun N_i2s(str: String) = "(($str)<<16>>16)"
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
	override fun N_l2i(str: String) = "N.l2i($str)"
	override fun N_l2l(str: String) = "($str)"
	override fun N_l2f(str: String) = "Math.fround(N.l2d($str))"
	override fun N_l2d(str: String) = "N.l2d($str)"
	override fun N_getFunction(str: String) = "N.getFunction($str)"
	override fun N_c(str: String, from: AstType, to: AstType) = "($str)"
	override fun N_lneg(str: String) = "N.lneg($str)"
	override fun N_linv(str: String) = "N.linv($str)"
	override fun N_ineg(str: String) = "-($str)"
	override fun N_iinv(str: String) = "~($str)"
	override fun N_fneg(str: String) = "-($str)"
	override fun N_dneg(str: String) = "-($str)"
	override fun N_znot(str: String) = "!($str)"
	override fun N_imul(l: String, r: String): String = "Math.imul($l, $r)"
	override fun genLiteralString(v: String): String = "S[" + names.allocString(v) + "]"

	override fun genExpr2(e: AstExpr): String {
		return when (e) {
			is AstExpr.CALL_BASE -> {
				// Determine method to call!
				val e2 = if (e.isSpecial && e is AstExpr.CALL_INSTANCE) AstExprUtils.RESOLVE_SPECIAL(program, e, context) else e
				val method = fixMethod(e2.method)
				val refMethod = program[method] ?: invalidOp("Can't find method: $method while generating $context")
				val refMethodClass = refMethod.containingClass
				val clazz = method.containingClassType
				val args = e2.args

				if (e2 is AstExpr.CALL_STATIC) {
					refs.add(clazz)
					mutableBody.initClassRef(clazz, "CALL_STATIC")
				}

				val isNativeCall = refMethodClass.isNative

				val commaArgs = args.map {
					if (isNativeCall) convertToJs(it) else it.genExpr()
				}.joinToString(", ")

				val base = when (e2) {
					is AstExpr.CALL_STATIC -> "${clazz.nativeTypeNew}"
					is AstExpr.CALL_SUPER -> {
						val superMethod = refMethodClass.get(method.withoutClass) ?: invalidOp("Can't find super for method : $method")
						names.getJsClassFqNameForCalling(superMethod.containingClass.name) + ".prototype"
					}
					is AstExpr.CALL_INSTANCE -> "${e2.obj.genNotNull()}"
					else -> invalidOp("Unexpected")
				}

				val base2 = when (e2) {
					is AstExpr.CALL_SUPER -> if (commaArgs.isEmpty()) ".call(this" else ".call(this, "
					else -> "("
				}

				val result = "$base${refMethod.jsNameAccess}$base2$commaArgs)"
				if (isNativeCall) convertToJava(refMethod.methodType.ret, result) else result
			}
			is AstExpr.METHOD_CLASS -> {
				val methodInInterfaceRef = e.methodInInterfaceRef
				val methodToConvertRef = e.methodToConvertRef
				//val interfaceName = methodInInterfaceRef.classRef.name
				val interfaceName = names.getJsClassFqNameForCalling(methodInInterfaceRef.classRef.name)
				"R.createLambda($interfaceName, " + Indenter.genString {
					//methodInInterfaceRef.type.args

					val argNameTypes = methodInInterfaceRef.type.args.map { it.name }.joinToString(", ")

					line("function($argNameTypes)") {
						val args = methodInInterfaceRef.type.args.map { AstLocal(-1, it.name, it.type) }

						line("return " + genExpr2(AstExpr.CAST(AstExpr.CALL_STATIC(
							methodToConvertRef.containingClassType,
							methodToConvertRef,
							args.zip(methodToConvertRef.type.args).map { AstExpr.CAST(AstExpr.LOCAL(it.first), it.second.type) }
						), methodInInterfaceRef.type.ret)) + ";"
						)
					}
				} + ")"

			}
			else -> super.genExpr2(e)
		}
	}

	fun convertToJs(expr: AstExpr.Box): String = convertToJs(expr.type, expr.genExpr())
	fun convertToJava(expr: AstExpr.Box): String = convertToJava(expr.type, expr.genExpr())
	fun convertToJs(type: AstType, text: String): String = convertToFromJs(type, text, toJs = true)
	fun convertToJava(type: AstType, text: String): String = convertToFromJs(type, text, toJs = false)

	fun convertToFromJs(type: AstType, text: String, toJs: Boolean): String {
		if (type is AstType.ARRAY) {
			return (if (toJs) "N.unbox($text)" else "N.box($text)")
		}

		return text
	}

	private fun AstMethod.getJsNativeBodies(): Map<String, Indenter> = this.getNativeBodies(target = "js")

	fun writeClass(clazz: AstClass): Indenter {
		context.clazz = clazz

		val isRootObject = clazz.name.fqname == "java.lang.Object"
		//val isInterface = clazz.isInterface
		val isAbstract = (clazz.classType == AstClassType.ABSTRACT)
		//val isNormalClass = (clazz.classType == AstClassType.CLASS)
		//val classType = if (isInterface) "interface" else "class"
		val simpleClassName = clazz.name.jsGeneratedSimpleClassName
		//val implementingString = getInterfaceList("implements")
		//val isInterfaceWithStaticMembers = isInterface && clazz.fields.any { it.isStatic }
		//val isInterfaceWithStaticFields = clazz.name.withSimpleName(clazz.name.simpleName + "\$StaticMembers")
		refs._usedDependencies.clear()

		if (!clazz.extending?.fqname.isNullOrEmpty()) refs.add(AstType.REF(clazz.extending!!))
		for (impl in clazz.implementing) refs.add(AstType.REF(impl))
		//val interfaceClassName = clazz.name.append("_Fields");

		fun writeField(field: AstField): Indenter = Indenter.gen {
			val fieldType = field.type
			refs.add(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) field.constantValue else fieldType.nativeDefault

			val defaultFieldName = field.name
			val fieldName = if (field.jsName == defaultFieldName) null else field.jsName

			line("this.registerField(${fieldName.quote()}, ${field.name.quote()}, ${field.descriptor.quote()}, ${field.genericSignature.quote()}, ${field.modifiers.acc}, ${names.escapeConstant(defaultValue, fieldType)}, ${visibleAnnotationsOrNull(field.annotations)});")
		}

		fun writeMethod(method: AstMethod): Indenter {
			context.method = method
			return Indenter.gen {
				refs.add(method.methodType)
				val margs = method.methodType.args.map { it.name }

				val defaultMethodName = if (method.isInstanceInit) "${method.ref.classRef.fqname}${method.name}${method.desc}" else "${method.name}${method.desc}"
				val methodName = if (method.targetName == defaultMethodName) null else method.targetName

				val rbody = if (method.body != null) {
					method.body
				} else if (method.bodyRef != null) {
					program[method.bodyRef!!]?.body
				} else {
					null
				}


				fun renderBranch(actualBody: Indenter?) = Indenter.gen {
					val isConstructor = method.isInstanceInit

					val registerMethodName = if (isConstructor) {
						"registerConstructor"
					} else {
						"registerMethod"
					}

					val annotationsArgs = "${visibleAnnotationsOrNull(method.annotations)}, ${visibleAnnotationsListOrNull(method.parameterAnnotations)}"

					val commonArgs = if (isConstructor) {
						"${methodName.quote()}, ${method.signature.quote()}, ${method.genericSignature.quote()}, ${method.modifiers.acc}, $annotationsArgs"
					} else {
						"${methodName.quote()}, ${method.name.quote()}, ${method.signature.quote()}, ${method.genericSignature.quote()}, ${method.modifiers.acc}, $annotationsArgs"
					}

					if (actualBody == null) {
						line("this.$registerMethodName($commonArgs, null)")
					} else {
						line("this.$registerMethodName($commonArgs, function (${margs.joinToString(", ")}) {".trim())
						indent {
							line(actualBody)
							if (method.methodVoidReturnThis) line("return this;")
						}
						line("});")
					}
				}

				fun renderBranches() = Indenter.gen {
					try {
						val nativeBodies = method.getJsNativeBodies()
						val javaBody = rbody?.genBodyWithFeatures()

						// @TODO: Do not hardcode this!
						if (javaBody == null && nativeBodies.isEmpty()) {
							line(renderBranch(null))
						} else {
							if (nativeBodies.isNotEmpty()) {
								val default = if ("" in nativeBodies) nativeBodies[""]!! else javaBody ?: Indenter.EMPTY
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
								line(renderBranch(javaBody))
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

		val classCodeIndenter = Indenter.gen {
			if (isAbstract) line("// ABSTRACT")

			val interfaces = "[" + clazz.implementing.map { it.jsClassFqName.quote() }.joinToString(", ") + "]"
			val declarationHead = "var " + names.getJsClassFqNameForCalling(clazz.name) + " = program.registerType(null, ${simpleClassName.quote()}, ${clazz.modifiers.acc}, ${clazz.extending?.jsClassFqName?.quote()}, $interfaces, ${visibleAnnotationsOrNull(clazz.runtimeAnnotations)}, function() {"
			val declarationTail = "});"

			line(declarationHead)
			indent {
				val nativeMembers = clazz.annotationsList.getTypedList(JTranscAddMembersList::value)

				for (member in nativeMembers.filter { it.target == "js" }.flatMap { it.value.toList() }) line(member)
				for (field in clazz.fields) line(writeField(field))
				for (method in clazz.methods) line(writeMethod(method))
			}

			line(declarationTail)
		}

		return classCodeIndenter
	}

	val MethodRef.jsNameAccess: String get() = "[" + this.targetName.quote() + "]"

	val FqName.jsClassFqName: String get() = names.getJsClassFqName(this)
	val FqName.jsFilePath: String get() = names.getJsFilePath(this)
	val FqName.jsGeneratedFqName: FqName get() = names.getJsGeneratedFqName(this)
	val FqName.jsGeneratedSimpleClassName: String get() = names.getJsGeneratedSimpleClassName(this)
}