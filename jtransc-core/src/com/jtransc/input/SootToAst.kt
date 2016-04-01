package com.jtransc.input

import com.jtransc.ast.*
import com.jtransc.ds.cast
import com.jtransc.ds.zipped
import com.jtransc.env.OS
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.log.log
import com.jtransc.vfs.SyncVfsFile
import soot.*
import soot.jimple.*
import soot.options.Options
import soot.tagkit.*
import java.io.File

class SootToAst : AstClassGenerator {
	fun getSootClass(fqname: FqName) = Scene.v().loadClassAndSupport(fqname.fqname)

	override fun generateClass(program: AstProgram, fqname: FqName): AstClass {
		return generateClass(program, getSootClass(fqname))
	}

	fun generateClass(program: AstProgram, sootClass: SootClass): AstClass {
		val astClass = AstClass(
			program = program,
			name = sootClass.name.fqname,
			modifiers = sootClass.modifiers,
			annotations = sootClass.tags.toAstAnnotations(),
			classType = if (sootClass.isInterface) AstClassType.INTERFACE else if (sootClass.isAbstract) AstClassType.ABSTRACT else AstClassType.CLASS,
			visibility = AstVisibility.PUBLIC,
			extending = if (sootClass.hasSuperclass() && !sootClass.isInterface) FqName(sootClass.superclass.name) else null,
			implementing = sootClass.interfaces.map { FqName(it.name) }
		)
		program.add(astClass)

		for (method in sootClass.methods.map { generateMethod(astClass, it) }) {
			astClass.add(method)
		}

		for (field in sootClass.fields.map { generateField(astClass, it) }) {
			astClass.add(field)
		}

		//astClass.finish()

		return astClass
	}

	fun generateMethod(containingClass: AstClass, method: SootMethod) = AstMethod(
		containingClass = containingClass,
		annotations = method.tags.toAstAnnotations(),
		name = method.name,
		type = method.astRef.type,
		signature = method.astType.mangle(),
		genericSignature = method.tags.filterIsInstance<SignatureTag>().firstOrNull()?.signature,
		defaultTag = method.tags.filterIsInstance<AnnotationDefaultTag>().firstOrNull()?.toAstAnnotation(),
		modifiers = method.modifiers,
		isStatic = method.isStatic,
		visibility = method.astVisibility,
		isNative = method.isNative,
		generateBody = { if (method.isConcrete) AstMethodProcessor.processBody(method, containingClass) else null }
	)

	fun generateField(containingClass: AstClass, field: SootField) = AstField(
		containingClass = containingClass,
		name = field.name,
		annotations = field.tags.toAstAnnotations(),
		type = field.type.astType,
		descriptor = field.type.astType.mangle(),
		genericSignature = field.tags.filterIsInstance<SignatureTag>().firstOrNull()?.signature,
		modifiers = field.modifiers,
		isStatic = field.isStatic,
		isFinal = field.isFinal,
		constantValue = field.tags.getConstant(),
		visibility = field.astVisibility
	)
}


open class BaseProjectContext(
	val classNames: List<String>,
	val mainClass: String,
	val classPaths: List<String>,
	val output: SyncVfsFile,
	val generator: AstClassGenerator
)

open class AstMethodProcessor private constructor(
	private val method: SootMethod,
	private val containingClass: AstClass
) {
	private val program = containingClass.program

	companion object {
		//const val DEBUG = true
		const val DEBUG = false

		private fun processBodyNoCatch(method: SootMethod, containingClass: AstClass): AstBody? {
			return AstMethodProcessor(method, containingClass).handle()
		}

		fun processBody(method: SootMethod, containingClass: AstClass): AstBody? {
			if (DEBUG) {
				return processBodyNoCatch(method, containingClass)
			} else {
				try {
					return processBodyNoCatch(method, containingClass)
				} catch (e: Throwable) {
					e.printStackTrace()
					println("WARNING: Couldn't generate method ${containingClass.name}::${method.name}, because: " + e)
					return null
				}
			}
		}
	}

	private val activeBody = method.retrieveActiveBody()
	private val units = activeBody.units.toList()
	private val traps = activeBody.traps.toList()

	private var labelIndex = 0
	private val labels = hashMapOf<soot.Unit, AstLabel>()

	private val locals = hashMapOf<String, AstLocal>()

	private fun ensureLabel(unit: soot.Unit): AstLabel {
		if (unit !in labels) {
			labelIndex++
			labels[unit] = AstLabel("label_$labelIndex")
		}
		return labels[unit]!!
	}

	private fun ensureLocal(c: soot.Local): AstLocal {
		if (locals[c.name] == null) {
			locals[c.name] = AstLocal(c.index, c.name, c.type.astType)
		}
		return locals[c.name]!!
	}

	private fun handle(): AstBody {
		prepareInternal(units)
		val stm = handleInternal(units)
		return AstBody(
			stm = stm,
			locals = locals.values.sortedBy { it.name },
			traps = traps.map { AstTrap(ensureLabel(it.beginUnit), ensureLabel(it.endUnit), ensureLabel(it.handlerUnit), it.exception.astType) }
		)
	}

	private fun prepareInternal(units: List<soot.Unit>) {
		for (trap in traps) {
			ensureLabel(trap.beginUnit)
			ensureLabel(trap.endUnit)
			ensureLabel(trap.handlerUnit)
		}
		for (unit in units) {
			when (unit) {
				is IfStmt -> ensureLabel(unit.target)
				is GotoStmt -> ensureLabel(unit.target)
				is LookupSwitchStmt -> {
					unit.targets.map { it as soot.Unit }.forEach { ensureLabel(it) }
					ensureLabel(unit.defaultTarget)
				}
				is TableSwitchStmt -> {
					unit.targets.map { it as soot.Unit }.forEach { ensureLabel(it) }
					ensureLabel(unit.defaultTarget)
				}
				else -> {
				}
			}
		}
	}

	private fun handleInternal(units: List<soot.Unit>): AstStm {
		var stms = arrayListOf<AstStm>()
		for (unit in units) {
			if (unit in labels) stms.add(AstStm.STM_LABEL(labels[unit]!!))
			stms.add(this.convert(unit))
		}
		return AstStm.STMS(stms.toList())
	}

	private fun cast(e: AstExpr, to: AstType): AstExpr {
		return if (e.type != to) AstExpr.CAST(e, to) else e
	}

	private fun convert(s: soot.Unit): AstStm = when (s) {
		is DefinitionStmt -> {
			val (l, r) = Pair(convert(s.leftOp), convert(s.rightOp))
			val r_casted = cast(r, l.type)
			when (l) {
				is AstExpr.LocalExpr -> AstStm.SET(l, r_casted)
				is AstExpr.ARRAY_ACCESS -> AstStm.SET_ARRAY(l.array, l.index, r_casted)
				is AstExpr.STATIC_FIELD_ACCESS -> AstStm.SET_FIELD_STATIC(l.field, r_casted)
				is AstExpr.INSTANCE_FIELD_ACCESS -> AstStm.SET_FIELD_INSTANCE(l.field, l.expr, r_casted)
				else -> invalidOp("Can't handle leftOp: $l")
			}
		}
		is ReturnStmt -> AstStm.RETURN(cast(convert(s.op), method.returnType.astType))
		is ReturnVoidStmt -> AstStm.RETURN(null)
		is IfStmt -> AstStm.IF_GOTO(ensureLabel(s.target), cast(convert(s.condition), AstType.BOOL))
		//is IfStmt -> AstStm.IF_GOTO(convert(s.condition), ensureLabel(s.target))
		is GotoStmt -> AstStm.IF_GOTO(ensureLabel(s.target), null)
		is ThrowStmt -> AstStm.THROW(convert(s.op))
		is InvokeStmt -> AstStm.STM_EXPR(convert(s.invokeExpr))
		is EnterMonitorStmt -> AstStm.MONITOR_ENTER(convert(s.op))
		is ExitMonitorStmt -> AstStm.MONITOR_EXIT(convert(s.op))
		is NopStmt -> AstStm.STMS(listOf())
		is LookupSwitchStmt -> AstStm.SWITCH_GOTO(
			convert(s.key), ensureLabel(s.defaultTarget),
			(0 until s.targetCount).map { Pair(s.getLookupValue(it), ensureLabel(s.getTarget(it))) }
		)
		is TableSwitchStmt -> AstStm.SWITCH_GOTO(
			convert(s.key), ensureLabel(s.defaultTarget),
			(s.lowIndex..s.highIndex).map { Pair(it, ensureLabel(s.getTarget(it - s.lowIndex))) }
		)
		else -> throw RuntimeException()
	}

	private fun convert(c: Value): AstExpr = when (c) {
		is Local -> AstExpr.LOCAL(ensureLocal(c))
		is NullConstant -> AstExpr.LITERAL(null)
		is IntConstant -> AstExpr.LITERAL(c.value)
		is LongConstant -> AstExpr.LITERAL(c.value)
		is FloatConstant -> AstExpr.LITERAL(c.value)
		is DoubleConstant -> AstExpr.LITERAL(c.value)
		is StringConstant -> AstExpr.LITERAL(c.value)
		is ClassConstant -> AstExpr.CLASS_CONSTANT(AstType.REF_INT(c.value))
		is SootMethodType -> AstExpr.METHODTYPE_CONSTANT(c.astType)
		is SootMethodRef -> AstExpr.METHODREF_CONSTANT(c.ast)
		is SootMethodHandle -> AstExpr.METHODHANDLE_CONSTANT(c.ast)
		is ThisRef -> AstExpr.THIS(FqName(method.declaringClass.name))
		is ParameterRef -> AstExpr.PARAM(AstArgument(c.index, c.type.astType))
		is CaughtExceptionRef -> AstExpr.CAUGHT_EXCEPTION(c.type.astType)
		is ArrayRef -> AstExpr.ARRAY_ACCESS(convert(c.base), convert(c.index))
		is InstanceFieldRef -> AstExpr.INSTANCE_FIELD_ACCESS(c.field.ast, convert(c.base))
		is StaticFieldRef -> AstExpr.STATIC_FIELD_ACCESS(c.field.ast)
		is CastExpr -> AstExpr.CAST(convert(c.op), c.castType.astType)
		is InstanceOfExpr -> AstExpr.INSTANCE_OF(convert(c.op), c.checkType.astType)
		is NewExpr -> AstExpr.NEW(c.type.astType as AstType.REF)
		is NewArrayExpr -> {
			AstExpr.NEW_ARRAY(AstType.ARRAY(c.baseType.astType, 1), listOf(convert(c.size)))
		}
		is NewMultiArrayExpr -> {
			AstExpr.NEW_ARRAY(c.baseType.astType as AstType.ARRAY, (0 until c.sizeCount).map { convert(c.getSize(it)) })
		}
		is LengthExpr -> AstExpr.ARRAY_LENGTH(convert(c.op))
		is NegExpr -> AstExpr.UNOP(AstUnop.NEG, convert(c.op))
		is BinopExpr -> {
			// @TODO: Make this generic! and simpler without breaking code!
			val l = convert(c.op1)
			val r = convert(c.op2)
			val lType = l.type
			val rType = r.type
			// @TODO: FIX THIS!
			val destType = if (lType == AstType.BOOL && rType == AstType.BOOL) {
				AstType.BOOL
			} else if (c is ConditionExpr) {
				AstType.BOOL
			} else {
				c.type.astType
			}
			val op = c.getAstOp(lType, rType)
			// @TODO: FIX THIS!
			when (op) {
				AstBinop.SHL, AstBinop.SHR, AstBinop.USHR ->
					AstExpr.BINOP(if (l.type == AstType.LONG) AstType.LONG else AstType.INT, l, op, r)
				else -> {
					val commonType = getCommonType(lType, rType)
					if (commonType != null) {
						AstExpr.BINOP(destType, cast(l, commonType), op, cast(r, commonType))
					} else {
						AstExpr.BINOP(destType, l, op, r)
					}
				}
			}
		}
		is InvokeExpr -> {
			val argsList = c.args.toList()
			val castTypes = c.methodRef.parameterTypes().map { it as Type }
			val args = Pair(argsList, castTypes).zipped.map {
				val (value, expectedType) = it
				doCastIfNeeded(expectedType, value)
			}.toList()
			val astMethodRef = c.methodRef.astRef
			val i = c
			when (i) {
				is StaticInvokeExpr -> {
					AstExpr.CALL_STATIC(AstType.REF(c.method.declaringClass.name), astMethodRef, args)
				}
				is InstanceInvokeExpr -> {
					val isSpecial = i is SpecialInvokeExpr
					val obj = convert(i.base)
					val method = astMethodRef

					if (isSpecial) {
						AstExprUtils.INVOKE_SPECIAL(obj, method, args)
					} else {
						AstExpr.CALL_INSTANCE(AstExpr.CAST(obj, method.classRef.type), method, args, isSpecial)
					}
				}
				is DynamicInvokeExpr -> {
					// astMethodRef.classRef == "soot.dummy.InvokeDynamic"
					val c2 = c as DynamicInvokeExpr
					val methodRef = c2.methodRef.astRef
					val bootstrapMethodRef = c2.bootstrapMethodRef.astRef
					val bootstrapArgs = c2.bootstrapArgs.map { convert(it) }

					AstExprUtils.INVOKE_DYNAMIC(methodRef.withoutClass, bootstrapMethodRef, bootstrapArgs)
				}
				else -> {
					invalidOp("Unsupported invoke type")
				}
			}
		}
		else -> {
			noImpl("$c")
		}
	}

	final fun doCastIfNeeded(toType: Type, value: Value): AstExpr = if (value.type == toType) {
		convert(value)
	} else {
		AstExpr.CAST(convert(value), toType.astType)
	}
}

val SootMethodRef.ast: AstMethodRef get() = AstMethodRef(
	this.declaringClass().name.fqname,
	this.name(),
	AstType.METHOD_TYPE(this.returnType().astType, this.parameterTypes().cast<Type>().map { it.astType }),
	this.isStatic
)
val SootMethodHandle.ast: AstMethodHandle get() = AstMethodHandle(
	this.methodType.astType,
	this.methodRef.ast,
	AstMethodHandle.Kind.fromId(this.referenceKind)
)

fun BinopExpr.getAstOp(l: AstType, r: AstType): AstBinop {
	if (l == AstType.BOOL && r == AstType.BOOL) {
		when (this) {
			is XorExpr -> return AstBinop.NE
			is AndExpr -> return AstBinop.BAND
			is OrExpr -> return AstBinop.BOR
		}
	}

	return when (this) {
		is AddExpr -> AstBinop.ADD
		is SubExpr -> AstBinop.SUB
		is MulExpr -> AstBinop.MUL
		is DivExpr -> AstBinop.DIV
		is RemExpr -> AstBinop.REM
		is AndExpr -> AstBinop.AND
		is OrExpr -> AstBinop.OR
		is XorExpr -> AstBinop.XOR
		is ShlExpr -> AstBinop.SHL
		is ShrExpr -> AstBinop.SHR
		is UshrExpr -> AstBinop.USHR
		is EqExpr -> AstBinop.EQ
		is NeExpr -> AstBinop.NE
		is GeExpr -> AstBinop.GE
		is LeExpr -> AstBinop.LE
		is LtExpr -> AstBinop.LT
		is GtExpr -> AstBinop.GT
		is CmpExpr -> if (l == AstType.LONG) AstBinop.LCMP else AstBinop.CMP
		is CmplExpr -> AstBinop.CMPL
		is CmpgExpr -> AstBinop.CMPG
		else -> throw RuntimeException()
	}
}

val SootClass.astRef: AstClassRef get() = AstClassRef(this.name)

val SootMethod.astRef: AstMethodRef get() = AstMethodRef(
	this.declaringClass.name.fqname, this.name,
	AstType.METHOD_TYPE(
		this.parameterTypes.withIndex().map { AstArgument(it.index, (it.value as Type).astType) },
		this.returnType.astType
	)
)

val SootMethodRef.astRef: AstMethodRef get() = AstMethodRef(
	this.declaringClass().name.fqname, this.name(),
	AstType.METHOD_TYPE(
		this.parameterTypes().withIndex().map { AstArgument(it.index, (it.value as Type).astType) },
		this.returnType().astType
	)
)

val SootMethodType.astType: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(
	this.returnType.astType,
	this.parameterTypes.map { it.astType }
)

val SootField.astRef: AstFieldRef get() = AstFieldRef(this.declaringClass.name.fqname, this.name, this.type.astType, this.isStatic)

val SootMethod.astType: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(this.returnType.astType, this.parameterTypes.map { (it as Type).astType })

val SootClass.astType: AstType.REF get() = this.type.astType as AstType.REF

val PRIM_SCORES = mapOf(
	AstType.BOOL to 0,
	AstType.BYTE to 1,
	AstType.SHORT to 2,
	AstType.CHAR to 3,
	AstType.INT to 4,
	AstType.LONG to 5,
	AstType.FLOAT to 6,
	AstType.DOUBLE to 7
)

fun getCommonType(t1:AstType, t2:AstType):AstType? {
	if (t1 !is AstType.Primitive || t2 !is AstType.Primitive) {
		return null
	}
	val score1 = PRIM_SCORES[t1] ?: 8
	val score2 = PRIM_SCORES[t2] ?: 8
	return if (score1 > score2) t1 else t2
}

val Type.astType: AstType get() = when (this) {
	is VoidType -> AstType.VOID
	is BooleanType -> AstType.BOOL
	is ByteType -> AstType.BYTE
	is CharType -> AstType.CHAR
	is ShortType -> AstType.SHORT
	is IntType -> AstType.INT
	is FloatType -> AstType.FLOAT
	is DoubleType -> AstType.DOUBLE
	is LongType -> AstType.LONG
	is ArrayType -> AstType.ARRAY(this.baseType.astType, this.numDimensions)
	is RefType -> AstType.REF(FqName(this.className))
	is NullType -> AstType.NULL
	else -> throw NotImplementedError("toAstType: $this")
}

val SootField.ast: AstFieldRef get() = AstFieldRef(this.declaringClass.name.fqname, this.name, this.type.astType)

val FieldRef.ast: AstFieldRef get() = this.field.ast

val ClassMember.astVisibility: AstVisibility get() = if (this.isPublic) {
	AstVisibility.PUBLIC
} else if (this.isProtected) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}

object SootUtils {
	fun init(classPaths: List<String>): Unit {
		G.reset()

		val Options_v = Options.v()

		//Options.v().set_output_format(Options.output_format_jimple)
		Options.v().set_output_format(Options.output_format_shimple)
		//Options.v().set_output_format(Options.output_format_grimple)
		Options_v.set_include_all(true)
		Options_v.set_print_tags_in_output(true)

		Options_v.set_allow_phantom_refs(false)
		//Options_v.set_allow_phantom_refs(true)

		Options_v.set_keep_line_number(true)

		val file_separator = OS.fileSeparator

		log("file_separator: $file_separator ... PathSeparator: ${File.pathSeparator}")

		Options_v.set_soot_classpath(classPaths.joinToString(File.pathSeparator))

		for (name in listOf("jap.npc", "jap.abc", "jop")) {
			Options_v.setPhaseOption(name, "enabled:true")
		}

		for (name in listOf(
			"jb.dae", "jb.uce", "jop.cse", "jop.bcm", "jop.lcm", "jop.cp",
			"jop.cpf", "jop.cbf", "jop.dae", "jop.nce", "jop.uce1", "jop.ubf1",
			"jop.uce2", "jop.ubf2", "jop.ule"
		)) {
			Options_v.setPhaseOption(name, "enabled:false")
		}

		Scene.v().loadNecessaryClasses()
	}

	// SootUtils.getTag(method.tags, "Llibcore/MethodBody;", "value") as String?
}

val ANNOTATIONS_BLACKLIST = listOf(
	"java.lang.annotation.Documented", "java.lang.Deprecated",
	"java.lang.annotation.Target", "java.lang.annotation.Retention",
	"kotlin.jvm.internal.KotlinLocalClass", "kotlin.jvm.internal.KotlinSyntheticClass",
	"kotlin.jvm.internal.KotlinClass", "kotlin.jvm.internal.KotlinFunction",
	"kotlin.jvm.internal.KotlinFileFacade", "kotlin.jvm.internal.KotlinMultifileClassPart",
	"kotlin.jvm.internal.KotlinMultifileClass", "kotlin.annotation.MustBeDocumented",
	"kotlin.annotation.Target", "kotlin.annotation.Retention",
	"kotlin.jvm.JvmStatic", "kotlin.Deprecated", "kotlin.Metadata", "org.jetbrains.annotations.NotNull",
	"kotlin.internal.InlineExposed"
).map { AstType.REF(it) }.toSet()

fun Iterable<Tag>.toAstAnnotations(): List<AstAnnotation> {
	return this.filterIsInstance<VisibilityAnnotationTag>()
		.flatMap {
			val runtimeVisible = it.visibility == AnnotationConstants.RUNTIME_VISIBLE
			it.annotations.map {
				AstAnnotation(
					AstType.demangle(it.type) as AstType.REF,
					it.getElements().map {
						Pair(it.name, it.getValue())
					}.toMap(),
					runtimeVisible
				)
			}
		}.filter { it.type !in ANNOTATIONS_BLACKLIST }
}

fun AnnotationTag.getElements(): List<AnnotationElem> {
	return (0 until this.numElems).map { this.getElemAt(it) }
}

//@Deprecated
fun AnnotationElem?.getValue(): Any? = when (this) {
	null -> null
	is AnnotationStringElem -> this.value
	is AnnotationBooleanElem -> this.value
	is AnnotationIntElem -> this.value
	is AnnotationFloatElem -> this.value
	is AnnotationDoubleElem -> this.value
	is AnnotationLongElem -> this.value
	is AnnotationArrayElem -> this.values.map { it.getValue() }
	is AnnotationClassElem -> null
	is AnnotationEnumElem -> {
		val type = AstType.demangle(this.typeName) as AstType.REF
		AstFieldRef(type.name, this.constantName, type)
	}
	is AnnotationAnnotationElem -> null
	else -> {
		noImpl("AnnotationElem.getValue(): Not implemented type: ${this.javaClass} : $this")
	}
}

fun AnnotationElem.unboxAnnotationElement() = Pair(this.name, this.getValue())

fun AnnotationDefaultTag.toAstAnnotation() = this.defaultVal.unboxAnnotationElement()

fun Iterable<Tag>?.getConstant(): Any? {
	val constantValue = this?.filterIsInstance<ConstantValueTag>()?.firstOrNull()
	return when (constantValue) {
		null -> null
		is IntegerConstantValueTag -> constantValue.intValue
		is LongConstantValueTag -> constantValue.longValue
		is DoubleConstantValueTag -> constantValue.doubleValue
		is FloatConstantValueTag -> constantValue.floatValue
		is StringConstantValueTag -> constantValue.stringValue
		else -> invalidOp("Not a valid constant")
	}
}
