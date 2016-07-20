package com.jtransc.gen.common

import com.jtransc.annotation.JTranscInvisible
import com.jtransc.annotation.JTranscInvisibleExternal
import com.jtransc.ast.*
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.gen.GenTargetInfo
import com.jtransc.text.Indenter
import com.jtransc.vfs.SyncVfsFile
import java.util.*

open class CommonGenGen(val input: Input) {
	class Input(
		val program: AstProgram,
		val features: AstFeatures,
		val featureSet: Set<AstFeature>,
		val settings: AstBuildSettings,
		val tinfo: GenTargetInfo,
		val names: CommonNames,
		val templateString: CommonProgramTemplate,
		val folders: CommonGenFolders,
		val srcFolder: SyncVfsFile
	)

	val program = input.program
	val features = input.features
	val featureSet = input.featureSet
	val settings = input.settings
	val tinfo = input.tinfo
	val cnames = input.names
	val templateString = input.templateString
	val folders = input.folders
	val srcFolder = input.srcFolder

	val types: AstTypes = program.types
	val context = AstGenContext()
	val refs = References()

	fun String.template(): String = templateString.gen(this)

	lateinit var mutableBody: MutableBody
	lateinit var stm: AstStm

	fun AstExpr.genExpr(): String = genExpr2(this)
	fun AstExpr.Box.genExpr(): String = genExpr2(this.value)

	fun AstStm.genStm(): Indenter = genStm2(this)
	fun AstStm.Box.genStm(): Indenter = genStm2(this.value)

	fun AstExpr.Box.genNotNull(): String = this.value.genNotNull()

	fun AstField.isVisible(): Boolean = !this.annotationsList.contains<JTranscInvisible>()
	fun AstMethod.isVisible(): Boolean = !this.annotationsList.contains<JTranscInvisible>()

	val invisibleExternalList = program.allAnnotations
		.map { it.toObject<JTranscInvisibleExternal>() }.filterNotNull()
		.flatMap { it.classes.toList() }

	fun AstClass.isVisible(): Boolean {
		if (this.fqname in invisibleExternalList) return false
		if (this.annotationsList.contains<JTranscInvisible>()) return false
		return true
	}

	fun AstExpr.genNotNull(): String = genExpr2(this)
	//fun AstBody.genBody(): Indenter = genBody2(this)
	//fun AstBody.genBodyWithFeatures(): Indenter = features.apply(this, featureSet, settings, types).genBody()

	fun AstBody.genBody(): Indenter = genBody2(this)
	fun AstBody.genBodyWithFeatures(): Indenter = genBody2WithFeatures(this)

	open fun genBody2WithFeatures(body: AstBody): Indenter {
		//return if (ENABLE_HXCPP_GOTO_HACK && (tinfo.subtarget in setOf("cpp", "windows", "linux", "mac", "android"))) {
		//	features.apply(body, (featureSet + setOf(GotosFeature)), settings, types).genBody()
		//} else {
		return features.apply(body, featureSet, settings, types).genBody()
		//}
	}

	// @TODO: Remove this from here, so new targets don't have to do this too!
	// @TODO: AstFieldRef should be fine already, so fix it in asm_ast!
	fun fixField(field: AstFieldRef): AstFieldRef = program[field].ref

	fun fixMethod(method: AstMethodRef): AstMethodRef = program[method]?.ref ?: invalidOp("Can't find method $method while generating $context")

	val allAnnotationTypes = program.allAnnotations.flatMap { it.getAllDescendantAnnotations() }.map { it.type }.distinct().map { program[it.name] }.toSet()

	open fun genBody2(body: AstBody): Indenter {
		val method = context.method
		this.mutableBody = MutableBody(method)

		return Indenter.gen {
			for (local in body.locals) {
				refs.add(local.type)
				line(genBodyLocal(local))

			}
			if (body.traps.isNotEmpty()) {
				line(genBodyTrapsPrefix())
			}
			for (field in method.dependencies.fields2.filter { it.isStatic }) {
				val clazz = field.containingClass
				if (clazz.isInterface) {

				} else {
				}
			}

			val bodyContent = body.stm.genStm()

			for ((clazzRef, reasons) in mutableBody.referencedClasses) {
				if (program[clazzRef.name].isNative) continue
				line(genBodyStaticInitPrefix(clazzRef, reasons))
			}
			line(bodyContent)
		}
	}

	val LocalRef.nativeName: String get() = cnames.getNativeName(this)
	val AstType.nativeDefaultString: String get() = cnames.escapeConstant(this.getNull(), this)

	open fun genBodyLocal(local: AstLocal): Indenter = indent { line("var ${local.nativeName} = ${local.type.nativeDefaultString};") }
	open fun genBodyTrapsPrefix() = indent { line("var J__exception__ = null;") }
	open fun genBodyStaticInitPrefix(clazzRef: AstType.REF, reasons: ArrayList<String>) = indent {
		line(cnames.buildStaticInit(program[clazzRef.name]))
		//line(cnames.getJsClassStaticInit(clazzRef, reasons.joinToString(", ")))
	}

	open fun genStm2(stm: AstStm): Indenter {
		this.stm = stm
		return when (stm) {
			is AstStm.NOP -> genStmNop(stm)
			is AstStm.IF -> genStmIf(stm)
			is AstStm.IF_ELSE -> genStmIfElse(stm)
			is AstStm.RETURN_VOID -> genReturnVoid(stm)
			is AstStm.RETURN -> genReturn(stm)
			else -> noImpl("Statement $stm")
		}
	}

	open fun genExpr2(e: AstExpr): String = when (e) {
		is AstExpr.THIS -> genThis(e)
		is AstExpr.TERNARY -> genTernary(e)
		is AstExpr.LITERAL -> genLiteral(e)
		else -> noImpl("Expression $e")
	}

	protected fun indent(init: Indenter.() -> Unit): Indenter = Indenter.gen(init)

	open fun genReturnVoid(stm: AstStm.RETURN_VOID) = Indenter.line(if (context.method.methodVoidReturnThis) "return this;" else "return;")
	open fun genReturn(stm: AstStm.RETURN) = Indenter.line("return ${stm.retval.genExpr()};")
	open fun genStmIf(stm: AstStm.IF) = indent { line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) } }
	open fun genStmIfElse(stm: AstStm.IF_ELSE) = indent {
		line("if (${stm.cond.genExpr()})") { line(stm.strue.genStm()) }
		line("else") { line(stm.sfalse.genStm()) }
	}

	open fun genStmNop(stm: AstStm.NOP) = Indenter.EMPTY

	open fun genTernary(e: AstExpr.TERNARY): String = "((" + e.cond.genExpr() + ") ? (" + e.etrue.genExpr() + ") : (" + e.efalse.genExpr() + "))"
	open fun genThis(e: AstExpr.THIS): String = "this"
	open fun genLiteral(e: AstExpr.LITERAL): String {
		val value = e.value

		return when (value) {
			null -> genLiteralNull()
			is AstType -> genLiteralType(value)
			is String -> genLiteralString(value)
			is Boolean -> genLiteralBoolean(value)
			is Byte -> genLiteralByte(value)
			is Char -> genLiteralChar(value)
			is Short -> genLiteralShort(value)
			is Int -> genLiteralInt(value)
			is Long -> genLiteralLong(value)
			is Float -> genLiteralFloat(value)
			is Double -> genLiteralDouble(value)
			else -> invalidOp("Unsupported value $value")
		}
	}

	open fun genLiteralType(v: AstType): String {
		for (fqName in v.getRefClasses()) {
			mutableBody.initClassRef(fqName, "class literal")
		}
		return cnames.escapeConstant(v)
	}

	open fun genLiteralNull(): String = cnames.escapeConstant(null)
	open fun genLiteralString(v: String): String = cnames.escapeConstant(v)
	open fun genLiteralBoolean(v: Boolean): String = cnames.escapeConstant(v)
	open fun genLiteralByte(v: Byte): String = cnames.escapeConstant(v)
	open fun genLiteralChar(v: Char): String = cnames.escapeConstant(v)
	open fun genLiteralShort(v: Short): String = cnames.escapeConstant(v)
	open fun genLiteralInt(v: Int): String = cnames.escapeConstant(v)
	open fun genLiteralLong(v: Long): String = cnames.escapeConstant(v)
	open fun genLiteralFloat(v: Float): String = cnames.escapeConstant(v)
	open fun genLiteralDouble(v: Double): String = cnames.escapeConstant(v)

	class MutableBody(val method: AstMethod) {
		val referencedClasses = hashMapOf<AstType.REF, ArrayList<String>>()
		fun initClassRef(classRef: AstType.REF, reason: String) {
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