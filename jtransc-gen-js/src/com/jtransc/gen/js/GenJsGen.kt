package com.jtransc.gen.js

import com.jtransc.JTranscFunction
import com.jtransc.annotation.*
import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImplWarn
import com.jtransc.ffi.StdCall
import com.jtransc.gen.GenTargetInfo
import com.jtransc.internal.JTranscAnnotationBase
import com.jtransc.lang.nullMap
import com.jtransc.lang.toBetterString
import com.jtransc.log.log
import com.jtransc.text.Indenter
import com.jtransc.text.quote
import com.jtransc.util.sortDependenciesSimple
import com.jtransc.vfs.MemoryVfs
import com.jtransc.vfs.SyncVfsFile
import java.util.*

class GenJsGen(
	val program: AstProgram,
	val features: AstFeatures,
	val featureSet: Set<AstFeature>,
	val settings: AstBuildSettings,
	val tinfo: GenTargetInfo,
	val names: JsNames,
	val jsTemplateString: JsTemplateString
) {
	val refs = References()
	val context = AstGenContext()
	lateinit var mutableBody: MutableBody
	lateinit var stm: AstStm

	fun AstStm.genStm(): Indenter = genStm2(this)
	fun AstStm.Box.genStm(): Indenter = genStm2(this.value)
	fun AstExpr.genExpr(): String = genExpr2(this)
	fun AstExpr.Box.genExpr(): String = genExpr2(this.value)
	fun AstExpr.Box.genNotNull(): String = this.value.genNotNull()

	val invisibleExternalList = program.allAnnotations
		.map { it.toObject<JTranscInvisibleExternal>() }.filterNotNull()
		.flatMap { it.classes.toList() }

	fun AstClass.isVisible(): Boolean {
		if (this.fqname in invisibleExternalList) return false
		if (this.annotationsList.contains<JTranscInvisible>()) return false
		return true
	}

	fun AstField.isVisible(): Boolean = !this.annotationsList.contains<JTranscInvisible>()
	fun AstMethod.isVisible(): Boolean = !this.annotationsList.contains<JTranscInvisible>()

	fun AstExpr.genNotNull(): String {
		return if (this is AstExpr.THIS) {
			genExpr2(this)
		} else {
			genExpr2(this)
			//"HaxeNatives.checkNotNull(${gen2(this)})"
		}
	}

	fun AstBody.genBody(): Indenter = genBody2(this)
	fun AstBody.genBodyWithFeatures(): Indenter = features.apply(this, featureSet, settings).genBody()

	// @TODO: Remove this from here, so new targets don't have to do this too!
	// @TODO: AstFieldRef should be fine already, so fix it in asm_ast!
	fun fixField(field: AstFieldRef): AstFieldRef {
		return program[field].ref
	}

	fun fixMethod(method: AstMethodRef): AstMethodRef {
		return program[method]?.ref ?: invalidOp("Can't find method $method while generating $context")
	}

	val allAnnotationTypes = program.allAnnotations.flatMap {
		it.getAllDescendantAnnotations()
	}.map { it.type }.distinct().map { program[it.name] }.toSet()

	internal fun _write(): JsProgramInfo {
		val out = StringBuilder()

		val copyFiles = program.classes.flatMap { it.annotationsList.getTypedList(JTranscAddFileList::value).filter { it.target == "js" } }.sortedBy { it.priority }

		fun renderFiles(files: List<JTranscAddFile>, textSelector: (JTranscAddFile) -> String) {
			for (file in files.filter { textSelector(it) != "" }) {
				val str = program.resourcesVfs[textSelector(file)].readString() + "\n"
				out.append(if (file.process) str.template("includeFile") else str)
			}
		}

		val vfs = MemoryVfs()

		for (clazz in program.classes.filter { !it.isNative }) {
			if (clazz.implCode != null) {
				vfs[clazz.name.haxeFilePath] = clazz.implCode!!
			} else {
				writeClass(clazz, vfs)
			}
		}

		val mainClassFq = program.entrypoint
		val mainClass = mainClassFq.haxeClassFqName
		//val mainMethod = program[mainClassFq].getMethod("main", AstType.build { METHOD(VOID, ARRAY(STRING)) }.desc)!!.haxeName
		val mainMethod = "main"
		val entryPointClass = FqName(mainClassFq.fqname + "_EntryPoint")
		val entryPointFilePath = entryPointClass.haxeFilePath
		val entryPointFqName = entryPointClass.haxeGeneratedFqName
		val entryPointSimpleName = entryPointClass.haxeGeneratedSimpleClassName
		val entryPointPackage = entryPointFqName.packagePath

		fun inits() = Indenter.gen {
			line("haxe.CallStack.callStack();")
			line(names.getJsClassStaticInit(program[mainClassFq].ref, "program main"))
		}

		val customMain = program.allAnnotationsList.getTypedList(JTranscCustomMainList::value).firstOrNull { it.target == "js" }?.value

		val plainMain = Indenter.genString {
			line("this.registerMainClass('{{ mainClass }}');")
		}

		log("Using ... " + if (customMain != null) "customMain" else "plainMain")

		jsTemplateString.setExtraData(mapOf(
			"entryPointPackage" to entryPointPackage,
			"entryPointSimpleName" to entryPointSimpleName,
			"mainClass" to mainClass,
			"mainClass2" to mainClassFq.fqname,
			"mainMethod" to mainMethod,
			"inits" to inits().toString()
		))

		val strs = Indenter.gen {
			line("this.registerStrings({")
			indent {
				for (e in names.allocatedStrings.entries.sortedBy { it.value }) {
					line("${e.value} : ${e.key.quote()},")
				}
			}
			line("});");
		}

		renderFiles(copyFiles.toList()) { it.prepend }

		out.append(strs.toString())

		for (file in vfs.listdirRecursive()) {
			out.append(file.file.readString())
		}

		out.append(jsTemplateString.gen(customMain ?: plainMain, context, "customMain"))

		renderFiles(copyFiles.reversed().toList()) { it.append }

		return JsProgramInfo(entryPointClass, entryPointFilePath, out.toString())
	}


	fun annotation(a: AstAnnotation): String {
		fun escapeValue(it: Any?): String {
			return when (it) {
				null -> "null"
				is AstAnnotation -> annotation(it)
				is Pair<*, *> -> escapeValue(it.second)
				is AstFieldRef -> it.containingTypeRef.name.haxeClassFqName + "." + it.haxeName
				is AstFieldWithoutTypeRef -> program[it.containingClass].ref.name.haxeClassFqName + "." + program.get(it).haxeName
				is String -> "HaxeNatives.boxString(${it.quote()})"
				is Boolean, is Byte, is Short, is Char, is Int, is Long, is Float, is Double -> names.escapeConstant(it)
				is List<*> -> "[" + it.map { escapeValue(it) }.joinToString(", ") + "]"
				is com.jtransc.org.objectweb.asm.Type -> "HaxeNatives.resolveClass(" + it.descriptor.quote() + ")"
				else -> invalidOp("GenHaxeGen.annotation.escapeValue: Don't know how to handle value ${it.javaClass.name} : ${it.toBetterString()} while generating $context")
			}
		}
		//val itStr = a.elements.map { it.key.quote() + ": " + escapeValue(it.value) }.joinToString(", ")
		val annotation = program.get3(a.type)
		val itStr = annotation.methods.map { escapeValue(if (it.name in a.elements) a.elements[it.name]!! else it.defaultTag) }.joinToString(", ")
		return "new ${names.getFullAnnotationProxyName(a.type)}([$itStr])"
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

	fun visibleAnnotations(annotations: List<AstAnnotation>): String = "[" + annotations.filter { it.runtimeVisible }.map { annotation(it) }.joinToString(", ") + "]"
	fun visibleAnnotationsList(annotations: List<List<AstAnnotation>>): String = "[" + annotations.map { visibleAnnotations(it) }.joinToString(", ") + "]"

	fun annotationsInit(annotations: List<AstAnnotation>): Indenter {
		return Indenter.gen {
			for (i in annotations.filter { it.runtimeVisible }.flatMap { annotationInit(it) }.toHashSet()) {
				line(names.getJsClassStaticInit(i, "annotationsInit"))
			}
		}
	}

	//fun dumpClassInfo(clazz: AstClass) = Indenter.genString {
	//	line("static public var HAXE_CLASS_NAME = ${clazz.name.fqname.quote()};")
	//	line("static public function HAXE_CLASS_INIT(c:$JAVA_LANG_CLASS = null):$JAVA_LANG_CLASS") {
	//		line("if (c == null) c = new $JAVA_LANG_CLASS();")
	//		line("c.$JAVA_LANG_CLASS_name = N.strLit(HAXE_CLASS_NAME);")
	//		//line("info(c, \"${clazz.name.haxeGeneratedFqName}\", " + (clazz.extending?.fqname?.quote() ?: "null") + ", [" + clazz.implementing.map { "\"${it.fqname}\"" }.joinToString(", ") + "], ${clazz.modifiers}, " + annotations(clazz.runtimeAnnotations) + ");")
	//		line(annotationsInit(clazz.runtimeAnnotations))
	//		val proxyClassName = if (clazz.isInterface) clazz.name.haxeGeneratedFqName.fqname + "." + clazz.name.haxeGeneratedSimpleClassName + "_Proxy" else "null"
	//		val ffiClassName = if (clazz.hasFFI) clazz.name.haxeGeneratedFqName.fqname + "." + clazz.name.haxeGeneratedSimpleClassName + "_FFI" else "null"
	//		line("R.i(c, ${clazz.name.haxeGeneratedFqName}, $proxyClassName, $ffiClassName, " + (clazz.extending?.fqname?.quote() ?: "null") + ", [" + clazz.implementing.map { "\"${it.fqname}\"" }.joinToString(", ") + "], ${clazz.modifiers}, " + visibleAnnotations(clazz.runtimeAnnotations) + ");")
	//		if (clazz.isVisible()) {
	//			for ((slot, field) in clazz.fields.withIndex()) {
	//				val internalName = field.haxeName
	//				if (field.isVisible()) {
	//					line("R.f(c, ${internalName.quote()}, $slot, \"${field.name}\", \"${field.descriptor}\", ${field.modifiers}, ${field.genericSignature.quote()}, ${visibleAnnotations(field.annotations)});");
	//				}
	//			}
	//			for ((slot, method) in clazz.methods.withIndex()) {
	//				val internalName = method.haxeName
	//				if (method.isVisible()) {
	//					if (method.name == "<init>") {
	//						line("R.c(c, ${internalName.quote()}, $slot, ${method.modifiers}, ${method.signature.quote()}, ${method.genericSignature.quote()}, ${visibleAnnotations(method.annotations)}, ${visibleAnnotationsList(method.parameterAnnotations)});");
	//					} else if (method.name == "<clinit>") {
	//					} else {
	//						line("R.m(c, ${method.id}, ${internalName.quote()}, $slot, \"${method.name}\", ${method.modifiers}, ${method.desc.quote()}, ${method.genericSignature.quote()}, ${visibleAnnotations(method.annotations)}, ${visibleAnnotationsList(method.parameterAnnotations)});");
	//					}
	//				}
	//			}
	//		}
	//		line("return c;")
	//	}
	//}

	fun genStm2(stm: AstStm): Indenter {
		this.stm = stm
		val program = program
		//val clazz = context.clazz
		val mutableBody = mutableBody
		return Indenter.gen {
			when (stm) {
			// plain
				is AstStm.NOP -> Unit
				is AstStm.IF -> {
					line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) }
				}
				is AstStm.IF_ELSE -> {
					line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) }
					line("else") { line(stm.sfalse.genStm()) }
				}
				is AstStm.RETURN_VOID -> {
					if (context.method.methodVoidReturnThis) line("return this;") else line("return;")
				}
				is AstStm.RETURN -> {
					line("return ${stm.retval.genExpr()};")
				}
				is AstStm.SET_LOCAL -> {
					val localName = stm.local.haxeName
					val expr = stm.expr.genExpr()
					if (localName != expr) {
						// Avoid: Assigning a value to itself
						line("$localName = $expr;")
					}
				}
				is AstStm.SET_NEW_WITH_CONSTRUCTOR -> {
					val newClazz = program[stm.target.name]
					//val mapping = mappings.getClassMapping(newClazz)
					refs.add(stm.target)
					val commaArgs = stm.args.map { it.genExpr() }.joinToString(", ")
					val className = stm.target.haxeTypeNew
					val localHaxeName = stm.local.haxeName

					if (newClazz.nativeName != null) {
						line("$localHaxeName = new $className($commaArgs);")
					} else {
						line("$localHaxeName = new $className();")
						line("$localHaxeName.${stm.method.haxeName}($commaArgs);")
					}
				}
				is AstStm.SET_ARRAY -> {
					val set = when (stm.array.type.elementType) {
						AstType.BOOL -> "setBool"
						else -> "set"
					}
					line("${stm.array.genNotNull()}.$set(${stm.index.genExpr()}, ${stm.expr.genExpr()});")
				}
				is AstStm.SET_FIELD_STATIC -> {
					refs.add(stm.clazz)
					mutableBody.initClassRef(fixField(stm.field).classRef, "SET_FIELD_STATIC")
					val left = fixField(stm.field).haxeStaticText
					val right = stm.expr.genExpr()
					if (left != right) {
						// Avoid: Assigning a value to itself
						line("$left /*${stm.field.name}*/ = $right;")
					}
				}
				is AstStm.SET_FIELD_INSTANCE -> {
					val left = "${stm.left.genExpr()}.${fixField(stm.field).haxeName}"
					val right = stm.expr.genExpr()
					if (left != right) {
						// Avoid: Assigning a value to itself
						line("$left = $right;")
					}
				}
				is AstStm.STM_EXPR -> line("${stm.expr.genExpr()};")
				is AstStm.STMS -> for (s in stm.stms) line(s.genStm())
				is AstStm.STM_LABEL -> line("${stm.label.name}:;")
				is AstStm.BREAK -> line("break;")
				is AstStm.CONTINUE -> line("continue;")
				is AstStm.WHILE -> {
					line("while (${stm.cond.genExpr()})") {
						line(stm.iter.genStm())
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
				is AstStm.TRY_CATCH -> {
					line("try") {
						line(stm.trystm.genStm())
					}
					line("catch (J__i__exception__)") {
						line("J__exception__ = J__i__exception__;")
						line(stm.catch.genStm())
					}
				}
				is AstStm.THROW -> line("throw ${stm.value.genExpr()};")
				is AstStm.RETHROW -> line("""HaxeNatives.rethrow(J__i__exception__);""")
				is AstStm.MONITOR_ENTER -> line("// MONITOR_ENTER")
				is AstStm.MONITOR_EXIT -> line("// MONITOR_EXIT")
				is AstStm.LINE -> {
					mark(stm)
					//line("// ${stm.line}")
				}
				else -> throw RuntimeException("Unhandled statement $stm")
			}
		}
	}

	fun genBody2(body: AstBody): Indenter {
		val method = context.method
		this.mutableBody = MutableBody(method)

		return Indenter.gen {
			for (local in body.locals) {
				refs.add(local.type)
				line("var ${local.haxeName} = ${local.type.haxeDefaultString};")
			}
			if (body.traps.isNotEmpty()) {
				line("var J__exception__ = null;")
			}
			for (field in method.dependencies.fields2.filter { it.isStatic }) {
				val clazz = field.containingClass
				if (clazz.isInterface) {

				} else {
				}
			}

			val bodyContent = body.stm.genStm()

			for ((clazzRef, reasons) in mutableBody.referencedClasses) {
				line(names.getJsClassStaticInit(clazzRef, reasons.joinToString(", ")))
			}
			line(bodyContent)
		}
	}

	fun genExpr2(e: AstExpr): String {
		return when (e) {
			is AstExpr.THIS -> "this"
			is AstExpr.LITERAL -> {
				val value = e.value
				if (value is String) {
					"S[" + names.allocString(value) + "]"
				} else {
					names.escapeConstant(value)
				}
			}
			is AstExpr.TERNARY -> "((" + e.cond.genExpr() + ") ? (" + e.etrue.genExpr() + ") : (" + e.efalse.genExpr() + "))"
			is AstExpr.PARAM -> "${e.argument.name}"
			is AstExpr.LOCAL -> "${e.local.haxeName}"
			is AstExpr.UNOP -> "(${e.op.symbol}(" + e.right.genExpr() + "))"
			is AstExpr.BINOP -> {
				val resultType = e.type
				var l = e.left.genExpr()
				var r = e.right.genExpr()
				val opSymbol = e.op.symbol
				val opName = e.op.str

				val binexpr = if (resultType == AstType.LONG) {
					"N.l$opName($l, $r)"
				} else if (resultType == AstType.INT && opSymbol in setOf("/", "<<", ">>", ">>>")) {
					"N.i$opName($l, $r)"
				} else {
					when (opSymbol) {
						"lcmp", "cmp", "cmpl", "cmpg" -> "N.$opName($l, $r)"
						//"lcmp", "cmp", "cmpl", "cmpg", "==", "!=" -> "N.$opName($l, $r)"
						else -> "($l $opSymbol $r)"
					}
				}
				when (resultType) {
					AstType.INT -> "N.i($binexpr)"
					AstType.CHAR -> "N.i2c($binexpr)"
					AstType.SHORT -> "N.i2s($binexpr)"
					AstType.BYTE -> "N.i2b($binexpr)"
					else -> binexpr
				}
			}
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
					if (isNativeCall) convertToHaxe(it) else it.genExpr()
				}.joinToString(", ")

				val base = when (e2) {
					is AstExpr.CALL_STATIC -> "${clazz.haxeTypeNew}"
					is AstExpr.CALL_SUPER -> "_super"
					is AstExpr.CALL_INSTANCE -> "${e2.obj.genNotNull()}"
					else -> invalidOp("Unexpected")
				}

				val result = "$base${refMethod.haxeNameAccess}($commaArgs)"
				if (isNativeCall) convertToJava(refMethod.methodType.ret, result) else result
			}
			is AstExpr.FIELD_INSTANCE_ACCESS -> {
				"${e.expr.genNotNull()}${fixField(e.field).haxeNameAccess}"
			}
			is AstExpr.FIELD_STATIC_ACCESS -> {
				refs.add(e.clazzName)
				mutableBody.initClassRef(fixField(e.field).classRef, "FIELD_STATIC_ACCESS")

				"${fixField(e.field).haxeStaticText}"
			}
			is AstExpr.ARRAY_LENGTH -> {
				val type = e.array.type
				"(${e.array.genNotNull()}).length"
			}
			is AstExpr.ARRAY_ACCESS -> {
				val get = when (e.array.type.elementType) {
					AstType.BOOL -> "getBool"
					else -> "get"
				}
				"${e.array.genNotNull()}.$get(${e.index.genExpr()})"
			}
			is AstExpr.CAST -> {
				refs.add(e.from)
				refs.add(e.to)
				genCast(e.expr.genExpr(), e.from, e.to)
			}
			is AstExpr.NEW -> {
				refs.add(e.target)
				val className = e.target.haxeTypeNew
				"new $className()"
			}
			is AstExpr.INSTANCE_OF -> {
				refs.add(e.checkType)
				"Std.is(${e.expr.genExpr()}, ${e.checkType.haxeTypeCast})"
			}
			is AstExpr.NEW_ARRAY -> {
				refs.add(e.type.elementType)
				val desc = e.type.mangle().replace('/', '.') // Internal to normal name!?
				when (e.counts.size) {
					1 -> {
						if (e.type.elementType !is AstType.Primitive) {
							"new ${names.JsArrayAny}(${e.counts[0].genExpr()}, \"$desc\")"
						} else {
							"new ${e.type.haxeTypeNew}(${e.counts[0].genExpr()})"
						}
					}
					else -> {
						"${names.JsArrayAny}.createMultiSure([${e.counts.map { it.genExpr() }.joinToString(", ")}], \"$desc\")"
					}
				}
			}
			is AstExpr.CAUGHT_EXCEPTION -> "J__exception__"
			is AstExpr.METHOD_CLASS -> {
				val methodInInterfaceRef = e.methodInInterfaceRef
				val methodToConvertRef = e.methodToConvertRef
				val interfaceName = methodInInterfaceRef.classRef.name

				val interfaceLambdaFqname = interfaceName.haxeLambdaName
				"new $interfaceLambdaFqname(" + Indenter.genString {
					//methodInInterfaceRef.type.args

					val argNameTypes = methodInInterfaceRef.type.args.map { it.haxeNameAndType }.joinToString(", ")

					line("function($argNameTypes)") {
						// @TODO: Static + non-static
						//val methodToCallClassName = methodToConvertRef.classRef.name.haxeClassFqName
						//val methodToCallName = methodToConvertRef.haxeName

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
		//is AstExpr.REF -> genExpr2(e.expr)
			else -> throw NotImplementedError("Unhandled expression $this")
		}
	}

	fun convertToHaxe(expr: AstExpr.Box): String {
		return convertToHaxe(expr.type, expr.genExpr())
	}

	fun convertToJava(expr: AstExpr.Box): String {
		return convertToJava(expr.type, expr.genExpr())
	}

	fun convertToHaxe(type: AstType, text: String): String {
		return return convertToFromHaxe(type, text, toHaxe = true)
	}

	fun convertToJava(type: AstType, text: String): String {
		return return convertToFromHaxe(type, text, toHaxe = false)
	}

	fun convertToFromHaxe(type: AstType, text: String, toHaxe:Boolean): String {
		if (type is AstType.ARRAY) {
			return (if (toHaxe) "HaxeNatives.unbox($text)" else "cast(HaxeNatives.box($text), ${names.getJsType(type, TypeKind.CAST)})")
		}

		//if (type is AstType.REF) {
		//	val conversion = program[type.name].annotationsList.getTyped<HaxeNativeConversion>()
		//	if (conversion != null) {
		//		return (if (toHaxe) conversion.toHaxe else conversion.toJava).replace("@self", text)
		//	}
		//}
		return text
	}

	fun genCast(e: String, from: AstType, to: AstType): String {
		if (from == to) return e

		if (from !is AstType.Primitive && to is AstType.Primitive) {
			return when (from) {
			// @TODO: Check!
				AstType.BOOL.CLASSTYPE -> genCast("HaxeNatives.unboxBool($e)", AstType.BOOL, to)
				AstType.BYTE.CLASSTYPE -> genCast("HaxeNatives.unboxByte($e)", AstType.BYTE, to)
				AstType.SHORT.CLASSTYPE -> genCast("HaxeNatives.unboxShort($e)", AstType.SHORT, to)
				AstType.CHAR.CLASSTYPE -> genCast("HaxeNatives.unboxChar($e)", AstType.CHAR, to)
				AstType.INT.CLASSTYPE -> genCast("HaxeNatives.unboxInt($e)", AstType.INT, to)
				AstType.LONG.CLASSTYPE -> genCast("HaxeNatives.unboxLong($e)", AstType.LONG, to)
				AstType.FLOAT.CLASSTYPE -> genCast("HaxeNatives.unboxFloat($e)", AstType.FLOAT, to)
				AstType.DOUBLE.CLASSTYPE -> genCast("HaxeNatives.unboxDouble($e)", AstType.DOUBLE, to)
			//AstType.OBJECT -> genCast(genCast(e, from, to.CLASSTYPE), to.CLASSTYPE, to)
			//else -> noImpl("Unhandled conversion $e : $from -> $to")
				else -> genCast(genCast(e, from, to.CLASSTYPE), to.CLASSTYPE, to)
			}
		}

		fun unhandled(): String {
			noImplWarn("Unhandled conversion ($from -> $to) at $context")
			return "($e)"
		}

		return when (from) {
			is AstType.BOOL, is AstType.INT, is AstType.CHAR, is AstType.SHORT, is AstType.BYTE -> {
				val e2 = if (from == AstType.BOOL) "N.z2i($e)" else "$e"

				when (to) {
					is AstType.BOOL -> "N.i2z($e2)"
					is AstType.BYTE -> "N.i2b($e2)"
					is AstType.CHAR -> "N.i2c($e2)"
					is AstType.SHORT -> "N.i2s($e2)"
					is AstType.INT -> "($e2)"
					is AstType.LONG -> "HaxeNatives.intToLong($e2)"
					is AstType.FLOAT, is AstType.DOUBLE -> "($e2)"
					else -> unhandled()
				}
			}
			is AstType.DOUBLE, is AstType.FLOAT -> {
				when (to) {
					is AstType.BOOL -> "N.i2z(Std.int($e))"
					is AstType.BYTE -> "N.i2b(Std.int($e))"
					is AstType.CHAR -> "N.i2c(Std.int($e))"
					is AstType.SHORT -> "N.i2s(Std.int($e))"
					is AstType.INT -> "Std.int($e)"
					is AstType.LONG -> "HaxeNatives.floatToLong($e)"
					is AstType.FLOAT, is AstType.DOUBLE -> "($e)"
					else -> unhandled()
				}
			}
			is AstType.LONG -> {
				when (to) {
					is AstType.BOOL -> "N.i2z(($e).low)"
					is AstType.BYTE -> "N.i2b(($e).low)"
					is AstType.CHAR -> "N.i2c(($e).low)"
					is AstType.SHORT -> "N.i2s(($e).low)"
					is AstType.INT -> "($e).low"
					is AstType.LONG -> "($e)"
					is AstType.FLOAT, is AstType.DOUBLE -> "HaxeNatives.longToFloat($e)"
					else -> unhandled()
				}
			}
			is AstType.REF, is AstType.ARRAY, is AstType.GENERIC -> {
				when (to) {
					FUNCTION_REF -> "(HaxeNatives.getFunction($e))"
					//else -> "N.c($e, ${to.haxeTypeCast})"
					else -> "$e"
				}
			}
			is AstType.NULL -> "$e"
			else -> unhandled()
		}
	}

	val FUNCTION_REF = AstType.REF(JTranscFunction::class.java.name)

	private fun AstMethod.getHaxeNativeBody(defaultContent: Indenter): Indenter {
		val bodies = this.annotationsList.getTypedList(JTranscMethodBodyList::value)

		return if (bodies.size > 0) {
			val bodiesmap = bodies.map { it.target to it.value }.toMap()
			val defaultbody: Indenter = if ("" in bodiesmap) Indenter.gen { line(bodiesmap[""]!!) } else defaultContent
			val extrabodies = bodiesmap.filterKeys { it != "" }
			Indenter.gen {
				if (extrabodies.size == 0) {
					line(defaultbody)
				} else {
					for ((target, extrabody) in extrabodies) {
						line(extrabody)
					}
				}
			}
		} else {
			defaultContent
		}
	}

	fun writeClass(clazz: AstClass, vfs: SyncVfsFile) {
		context.clazz = clazz

		val isRootObject = clazz.name.fqname == "java.lang.Object"
		//val isInterface = clazz.isInterface
		val isAbstract = (clazz.classType == AstClassType.ABSTRACT)
		//val isNormalClass = (clazz.classType == AstClassType.CLASS)
		//val classType = if (isInterface) "interface" else "class"
		val simpleClassName = clazz.name.haxeGeneratedSimpleClassName
		//val implementingString = getInterfaceList("implements")
		//val isInterfaceWithStaticMembers = isInterface && clazz.fields.any { it.isStatic }
		//val isInterfaceWithStaticFields = clazz.name.withSimpleName(clazz.name.simpleName + "\$StaticMembers")
		refs._usedDependencies.clear()

		if (!clazz.extending?.fqname.isNullOrEmpty()) refs.add(AstType.REF(clazz.extending!!))
		for (impl in clazz.implementing) refs.add(AstType.REF(impl))
		//val interfaceClassName = clazz.name.append("_Fields");

		var output = arrayListOf<Pair<String, String>>()

		fun writeField(field: AstField): Indenter = Indenter.gen {
			//val static = if (field.isStatic) "static " else ""
			//val visibility = if (isInterface) " " else field.visibility.haxe
			val fieldType = field.type
			refs.add(fieldType)
			val defaultValue: Any? = if (field.hasConstantValue) field.constantValue else fieldType.haxeDefault
			val fieldName = field.haxeName
			//if (field.name == "this\$0") println("field: $field : fieldRef: ${field.ref} : $fieldName")

			//val keep = if (field.annotationsList.contains<JTranscKeep>()) "@:keep " else ""

			//line("$keep$static$visibility var $fieldName:${fieldType.haxeTypeTag} = ${names.escapeConstant(defaultValue, fieldType)}; // /*${field.name}*/")
			line("this.registerField(${fieldName.quote()}, ${fieldType.haxeTypeTag.fqname.quote()}, ${field.modifiers.acc}, ${names.escapeConstant(defaultValue, fieldType)});")
		}

		fun writeMethod(method: AstMethod): Indenter {
			context.method = method
			return Indenter.gen {
				//val static = if (method.isStatic) "static " else ""
				//val visibility = if (isInterface) " " else method.visibility.haxe
				refs.add(method.methodType)
				//val margs = method.methodType.args.map { it.name + ":" + it.type.haxeTypeTag }
				val margs = method.methodType.args.map { it.name }
				//var override = if (method.haxeIsOverriding) "override " else ""
				//val inline = if (method.isInline) "inline " else ""
				//val rettype = if (method.methodVoidReturnThis) method.containingClass.astType else method.methodType.ret
				val decl = try {
					"this.registerMethod(${method.haxeName.quote()}, ${method.modifiers.acc}, function (${margs.joinToString(", ")}) {".trim()
				} catch (e: RuntimeException) {
					println("@TODO abstract interface not referenced: ${method.containingClass.fqname} :: ${method.name} : $e")
					//null
					throw e
				}
				val declTail = "});"

				val rbody = if (method.body != null) {
					method.body
				} else if (method.bodyRef != null) {
					program[method.bodyRef!!]?.body
				} else {
					null
				}
				line(decl)
				indent {
					try {
						// @TODO: Do not hardcode this!
						if (method.name == "throwParameterIsNullException") line("HaxeNatives.debugger();")
						val javaBody = if (rbody != null) {
							rbody.genBodyWithFeatures()
						} else Indenter.gen {
							line("throw R.n(HAXE_CLASS_NAME, ${method.id});")
						}
						//line(method.getHaxeNativeBody(javaBody).toString().template("nativeMethod"))
						line(method.getHaxeNativeBody(javaBody))
						if (method.methodVoidReturnThis) line("return this;")
					} catch (e: Throwable) {
						//e.printStackTrace()
						log.warn("WARNING haxe_gen.writeMethod:" + e.message)

						line("HaxeNatives.debugger(); throw " + "Errored method: ${clazz.name}.${method.name} :: ${method.desc} :: ${e.message}".quote() + ";")
					}
				}
				line(declTail)
			}
		}

		//val annotationTypeHaxeName = AstMethodRef(java.lang.annotation.Annotation::class.java.name.fqname, "annotationType", AstType.build { METHOD(java.lang.annotation.Annotation::class.java.ast()) }).haxeName
		val annotationTypeHaxeName = AstMethodRef(java.lang.annotation.Annotation::class.java.name.fqname, "annotationType", AstType.build { METHOD(CLASS) }).haxeName
		// java.lang.annotation.Annotation
		//abstract fun annotationType():Class<out Annotation>

		val classCodeIndenter = Indenter.gen {
			if (isAbstract) line("// ABSTRACT")

			val interfaces = "[" + clazz.implementing.map { it.haxeClassFqName.quote() }.joinToString(", ") + "]"
			val declarationHead = "this.registerType(${simpleClassName.quote()}, ${clazz.modifiers.acc}, ${clazz.extending?.haxeClassFqName?.quote()}, $interfaces, function() {"
			val declarationTail = "});"

			line(declarationHead)
			indent {
				val nativeMembers = clazz.annotationsList.getTypedList(JTranscAddMembersList::value)

				for (member in nativeMembers.filter { it.target == "js" }.flatMap { it.value.toList() }) {
					line(member)
				}

				for (field in clazz.fields) {
					line(writeField(field))
				}

				for (method in clazz.methods) {
					line(writeMethod(method))
				}

				// @TODO: Check! Check Haxe too!
				//if (!isInterface) {
				//	//println(clazz.fqname + " -> " + program.getAllInterfaces(clazz))
				//	val isFunctionType = program.isImplementing(clazz, JTranscFunction::class.java.name)
				//
				//	if (isFunctionType) {
				//		val executeFirst = clazz.methodsByName["execute"]!!.first()
				//		line("public const _execute:Function = ${executeFirst.ref.haxeName};")
				//	}
				//}

				/*
				if (isNormalClass) {
					val override = if (isRootObject) " " else "override "
					line("$override public function toString():String { return HaxeNatives.toNativeString(this.toString__Ljava_lang_String_()); }")
				}
				*/
				if (isRootObject) {
					//line("public function toString():String { return HaxeNatives.toNativeString(this.$toStringHaxeName()); }")
					//line("public function hashCode():Int { return this.$hashCodeHaxeName(); }")
				}

				//line(dumpClassInfo(clazz))
			}

			line(declarationTail)
		}

		val lineMappings = hashMapOf<Int, Int>()

		val fileStr = classCodeIndenter.toString { sb, line, data ->
			if (data is AstStm.LINE) {
				//println("MARKER: ${sb.length}, $line, $data, ${clazz.source}")
				lineMappings[line] = data.line
				//clazzName.internalFqname + ".java"
			}
		}

		val haxeFilePath = clazz.name.haxeFilePath
		vfs["$haxeFilePath"] = fileStr
		//vfs["$haxeFilePath.map"] = Sourcemaps.encodeFile(vfs["$haxeFilePath"].realpathOS, fileStr, clazz.source, lineMappings)
	}

	//val FqName.as3Fqname: String get() = this.fqname
	//fun AstMethod.getHaxeMethodName(program: AstProgram): String = this.ref.getHaxeMethodName(program)

	enum class TypeKind { TYPETAG, NEW, CAST }

	val AstVisibility.haxe: String get() = "public"

	val AstType.haxeTypeTag: FqName get() = names.getJsType(this, TypeKind.TYPETAG)
	val AstType.haxeTypeNew: FqName get() = names.getJsType(this, TypeKind.NEW)
	val AstType.haxeTypeCast: FqName get() = names.getJsType(this, TypeKind.CAST)
	val AstType.haxeDefault: Any? get() = names.getJsDefault(this)
	val AstType.haxeDefaultString: String get() = names.escapeConstant(names.getJsDefault(this), this)

	fun AstType.box(arg: String): String {
		return when (this) {
			is AstType.Primitive -> "HaxeNatives.box${this.shortName.capitalize()}($arg)"
			else -> "cast($arg)";
		}
	}

	fun AstType.unbox(arg: String): String {
		return when (this) {
			is AstType.Primitive -> "HaxeNatives.unbox${this.shortName.capitalize()}($arg)"
			else -> "cast($arg)";
		}
	}

	val AstLocal.haxeName: String get() = this.name.replace('$', '_')
	val AstExpr.LocalExpr.haxeName: String get() = this.name.replace('$', '_')

	val AstField.haxeName: String get() = names.getJsFieldName(this)
	val AstFieldRef.haxeName: String get() = names.getJsFieldName(this)
	val AstFieldRef.haxeStaticText: String get() = names.getStaticFieldText(this)

	val AstFieldRef.haxeNameAccess: String get() = "[" + names.getJsFieldName(this).quote() + "]"

	val AstMethod.haxeName: String get() = names.getJsMethodName(this)
	val AstMethodRef.haxeName: String get() = names.getJsMethodName(this)

	val AstMethod.haxeNameAccess: String get() = "[" + names.getJsMethodName(this).quote() + "]"
	val AstMethodRef.haxeNameAccess: String get() = "[" + names.getJsMethodName(this).quote() + "]"

	val AstMethod.haxeIsOverriding: Boolean get() = this.isOverriding && !this.isInstanceInit

	val FqName.haxeLambdaName: String get() = names.getJsClassFqNameLambda(this)
	val FqName.haxeClassFqName: String get() = names.getJsClassFqName(this)
	val FqName.haxeClassFqNameInt: String get() = names.getJsClassFqNameInt(this)
	val FqName.haxeFilePath: String get() = names.getJsFilePath(this)
	val FqName.haxeGeneratedFqPackage: String get() = names.getJsGeneratedFqPackage(this)
	val FqName.haxeGeneratedFqName: FqName get() = names.getJsGeneratedFqName(this)
	val FqName.haxeGeneratedSimpleClassName: String get() = names.getJsGeneratedSimpleClassName(this)

	fun String.template(reason: String): String = jsTemplateString.gen(this, context, reason)

	val AstArgument.haxeNameAndType: String get() = this.name + ":" + this.type.haxeTypeTag

	class MutableBody(val method: AstMethod) {
		val referencedClasses = hashMapOf<AstType.REF, ArrayList<String>>()
		fun initClassRef(classRef: AstType.REF, reason:String) {
			referencedClasses.putIfAbsent(classRef, arrayListOf())
			referencedClasses[classRef]!! += reason
		}
	}

	class References {
		var _usedDependencies = hashSetOf<AstType.REF>()
		fun add(type: AstType?) {
			when (type) {
				null -> Unit
				is AstType.METHOD -> {
					for (arg in type.argTypes) add(arg)
					add(type.ret)
				}
				is AstType.REF -> _usedDependencies.add(type)
				is AstType.ARRAY -> add(type.elementType)
				else -> Unit
			}
		}
	}
}