package com.jtransc.ast

import com.jtransc.ds.cast
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import kotlin.reflect.KProperty

data class AstBody constructor(
	val types: AstTypes,
	var stm: AstStm,
	var type: AstType.METHOD,
	val traps: List<AstTrap>,
	val flags: AstBodyFlags,
	val methodRef: AstMethodRef
) {
	private var _locals: List<AstLocal>? = null

	val locals: List<AstLocal> get() {
		if (_locals == null) {
			val locals = hashSetOf<AstLocal>()
			(object : AstVisitor() {
				override fun visit(local: AstLocal) {
					locals += local
				}
			}).visit(stm)
			_locals = locals.toList()
		}
		return _locals!!
	}

	fun invalidateLocals() {
		_locals = null
	}

	fun invalidate() {
		invalidateLocals()
	}
}

fun AstBody(types: AstTypes, stm: AstStm, desc: AstType.METHOD, methodRef: AstMethodRef): AstBody {
	return AstBody(types, stm, desc, listOf(), AstBodyFlags(types, false), methodRef = methodRef)
}

data class AstBodyFlags(
	val types: AstTypes,
	val strictfp: Boolean = true,
	val hasDynamicInvoke: Boolean = false
)

enum class AstBinop(val symbol: String, val str: String) {
	ADD("+", "add"), SUB("-", "sub"), MUL("*", "mul"), DIV("/", "div"), REM("%", "rem"),
	AND("&", "and"), OR("|", "or"), XOR("^", "xor"),
	SHL("<<", "shl"), SHR(">>", "shr"), USHR(">>>", "ushr"),
	BAND("&&", "band"), BOR("||", "bor"),
	EQ("==", "eq"), NE("!=", "ne"), GE(">=", "ge"), LE("<=", "le"), LT("<", "lt"), GT(">", "gt"),
	LCMP("lcmp", "lcmp"), CMPL("cmpl", "cmpl"), CMPG("cmpg", "cmpg");

	companion object {
		val SHIFTS = setOf(SHL, SHR, USHR)
		val COMPARISONS = setOf(EQ, NE, GE, LE, LT, GT)
		//val operators = values.flatMap { listOf(Pair(it.symbol, it), Pair(it.str, it)) }.toMap()
	}
}

enum class AstUnop(val symbol: String, val str: String) {
	NEG("-", "neg"),
	NOT("!", "not"),
	INV("~", "inv");

	companion object {
		//val operators = values.flatMap { listOf(Pair(it.symbol, it), Pair(it.str, it)) }.toMap()
	}
}

interface LocalParamRef {
	val name: String
}

interface LocalRef : LocalParamRef
interface ArgumentRef : LocalParamRef

class TempAstLocalFactory {
	var lastId = 0
	fun create(type: AstType): AstLocal {
		val out = AstLocal(lastId, "temp$lastId", type)
		lastId++
		return out
	}
}

data class AstLocal(val index: Int, override val name: String, val type: AstType) : LocalRef {
	constructor(index: Int, type: AstType) : this(index, "v$index", type)

	override fun toString() = "AstLocal:$name:$type(w:$writesCount,r:$readCount)"

	// @TODO: Move this to ast_optimize in an attached object
	val writes = arrayListOf<AstStm.SET_LOCAL>()
	val reads = arrayListOf<AstExpr.LOCAL>()
	val writesCount: Int get() = writes.size // @TODO: In SSA this should be one
	val readCount: Int get() = reads.size
	val isUsed: Boolean get() = (writesCount != 0) || (readCount != 0)
	fun write(set: AstStm.SET_LOCAL) = run { writes += set }
	fun read(ref: AstExpr.LOCAL) = run { reads += ref }
}

fun AstType.local(name: String, index: Int = 0) = AstExpr.LOCAL(AstLocal(index, name, this))

data class AstTrap(val start: AstLabel, val end: AstLabel, val handler: AstLabel, val exception: AstType.REF)
data class AstLabel(val name: String)

interface AstElement

interface Cloneable<T> {
	fun clone(): T
}

open class AstStm : AstElement, Cloneable<AstStm> {
	class Box(_value: AstStm) {
		var value: AstStm = _value
			get() = field
			set(value) {
				if (field !== value) {
					field.box = AstStm.Box(field)
					field = value
					field.box = this
				}
			}

		fun replaceWith(stm: AstStm) = run { this.value = stm }
		fun replaceWith(stm: AstStm.Box) = run { this.value = stm.value }

		init {
			_value.box = this
		}
	}

	var box: AstStm.Box = AstStm.Box(this)

	fun replaceWith(stm: AstStm) = this.box.replaceWith(stm)
	fun replaceWith(stm: AstStm.Box) = this.box.replaceWith(stm)

	override fun clone(): AstStm = noImpl("AstStm.clone: $this")

	class STMS(stms: List<AstStm>, dummy: Boolean) : AstStm() {
		val stms = stms.map { it.box }
	}

	class NOP(val reason: String) : AstStm() {
		override fun toString(): String = "NOP($reason)"
	}

	class LINE(val file: String, val line: Int) : AstStm() {
		override fun toString() = "AstStm.LINE($line)"
	}

	class STM_EXPR(expr: AstExpr) : AstStm() {
		val expr = expr.box
	}

	class SET_LOCAL constructor(val local: AstExpr.LOCAL, expr: AstExpr, dummy: Boolean) : AstStm() {
		val expr = expr.box
		override fun toString(): String = "SET_LOCAL($local = $expr)"
	}

	class SET_ARRAY constructor(array: AstExpr, index: AstExpr, expr: AstExpr) : AstStm() {
		val array = array.box
		val index = index.box
		val expr = expr.box
	}

	class SET_ARRAY_LITERALS(array: AstExpr, val startIndex: Int, val values: List<AstExpr.Box>) : AstStm() {
		val array = array.box
		val arrayType get() = array.type.asArray()
		val elementType get() = arrayType.element
	}

	class SET_FIELD_STATIC(val field: AstFieldRef, expr: AstExpr) : AstStm() {
		val clazz = AstType.REF(field.classRef.fqname)
		val expr = expr.box
	}

	class SET_FIELD_INSTANCE(val field: AstFieldRef, left: AstExpr, expr: AstExpr) : AstStm() {
		val left = left.box
		val expr = expr.box
	}

	class SET_NEW_WITH_CONSTRUCTOR(val local: AstExpr.LocalExpr, val target: AstType.REF, val method: AstMethodRef, args: List<AstExpr>) : AstStm() {
		val args = args.map { it.box }
	}

	class IF(cond: AstExpr, strue: AstStm) : AstStm() {
		val cond = cond.box
		val strue = strue.box
	}

	class IF_ELSE(cond: AstExpr, strue: AstStm, sfalse: AstStm) : AstStm() {
		val cond = cond.box
		val strue = strue.box
		val sfalse = sfalse.box
	}

	class WHILE(cond: AstExpr, iter: AstStm) : AstStm() {
		val cond = cond.box
		val iter = iter.box
	}

	class RETURN(retval: AstExpr) : AstStm() {
		val retval = retval.box
	}

	class RETURN_VOID() : AstStm() {
	}

	class THROW(value: AstExpr) : AstStm() {
		val value = value.box
	}

	class RETHROW() : AstStm()

	//data class TRY_CATCH(val trystm: AstStm, val catches: List<Pair<AstType, AstStm>>) : AstStm
	class TRY_CATCH(trystm: AstStm, catch: AstStm) : AstStm() {
		val trystm = trystm.box
		val catch = catch.box
	}

	class BREAK() : AstStm()
	class CONTINUE() : AstStm()

	// SwitchFeature
	class SWITCH(subject: AstExpr, default: AstStm, cases: List<Pair<Int, AstStm>>) : AstStm() {
		val subject = subject.box
		val default = default.box
		val cases = cases.map { it.first to it.second.box }
	}

	// GotoFeature

	class STM_LABEL(val label: AstLabel) : AstStm()

	class SWITCH_GOTO(subject: AstExpr, val default: AstLabel, val cases: List<Pair<Int, AstLabel>>) : AstStm() {
		val subject = subject.box
	}

	class IF_GOTO(val label: AstLabel, cond: AstExpr) : AstStm() {
		val cond = cond.box
	}

	class GOTO(val label: AstLabel) : AstStm() {
	}

	class MONITOR_ENTER(expr: AstExpr) : AstStm() {
		val expr = expr.box
	}

	class MONITOR_EXIT(expr: AstExpr) : AstStm() {
		val expr = expr.box
	}

	//class DEBUG() : AstStm() {
	//}
	//
	//class NOT_IMPLEMENTED() : AstStm() {
	//}
}

fun AstStm.isEmpty() = when (this) {
	is AstStm.STMS -> this.stms.isEmpty()
	else -> false
}

fun AstStm.isSingleStm(): Boolean {
	if (this is AstStm.STMS) return (this.stms.count() == 1) && this.stms.last().value.isSingleStm()
	return true
}

fun AstStm.lastStm(): AstStm {
	if (this is AstStm.STMS) return this.stms.lastOrNull()?.value?.lastStm() ?: this
	return this
}

fun AstStm.expand(): List<AstStm> {
	return when (this) {
		is AstStm.STMS -> this.stms.flatMap { it.value.expand() }
		else -> listOf(this)
	}
}

fun AstStm?.isBreakingFlow() = when (this) {
	null -> false
	is AstStm.BREAK -> true
	is AstStm.CONTINUE -> true
	is AstStm.GOTO -> true
	is AstStm.RETURN -> true
	is AstStm.RETURN_VOID -> true
	else -> false
}

abstract class AstExpr : AstElement, Cloneable<AstExpr> {
	class Box(_value: AstExpr) {
		var value: AstExpr = _value
			set(value) {
				if (field !== value) {
					field.box = AstExpr.Box(field)
					field = value
					field.box = this
				}
			}

		fun replaceWith(expr: AstExpr) = run { this.value = expr }
		fun replaceWith(expr: AstExpr.Box) = run { this.value = expr.value }

		init {
			_value.box = this
		}

		val type: AstType get() = value.type

		override fun toString(): String = "BOX($value)"
	}

	var box: AstExpr.Box = AstExpr.Box(this)
	var stm: AstStm? = null

	fun replaceWith(expr: AstExpr) = this.box.replaceWith(expr)
	fun replaceWith(expr: AstExpr.Box) = this.box.replaceWith(expr)

	abstract val type: AstType

	override fun clone(): AstExpr = noImpl("AstExpr.clone: $this")

	abstract class ImmutableRef : AstExpr()
	abstract class LValueExpr : AstExpr() {
	}

	abstract class LocalExpr : LValueExpr(), LocalRef {
		override abstract val name: String
	}

	// Reference

	class THIS(val ref: FqName) : LocalExpr() {
		override val name: String get() = "this"
		override val type: AstType = AstType.REF(ref)

		override fun clone(): AstExpr.THIS = THIS(ref)
	}

	abstract class LOCAL_BASE(val local: AstLocal, override val type: AstType) : LocalExpr() {
		override val name: String get() = local.name

		override fun clone(): AstExpr.LOCAL = LOCAL(local)
		override fun toString(): String = if (local.type == type) "LOCAL($local)" else "LOCAL($local as $type)"
	}

	class TYPED_LOCAL(local: AstLocal, type: AstType) : LOCAL_BASE(local, type)

	class LOCAL(local: AstLocal) : LOCAL_BASE(local, local.type)

	class PARAM(val argument: AstArgument) : LocalExpr() {
		override val name: String get() = argument.name
		override val type = argument.type

		override fun clone(): AstExpr.PARAM = PARAM(argument)
	}

	abstract class LiteralExpr : AstExpr() {
		abstract val value: Any?
	}

	/*
	class METHODTYPE_CONSTANT(val methodType: AstType.METHOD) : LiteralExpr() {
		override val value = methodType
		override val type: AstType = methodType
	}

	class METHODREF_CONSTANT(val methodRef: AstMethodRef) : LiteralExpr() {
		override val value = methodRef
		override val type: AstType = AstType.UNKNOWN
	}

	class METHODHANDLE_CONSTANT(val methodHandle: AstMethodHandle) : LiteralExpr() {
		override val value = methodHandle
		override val type: AstType = AstType.UNKNOWN
	}
	*/

	class LITERAL constructor(override val value: Any?, dummy: Boolean) : LiteralExpr() {
		override val type = AstType.fromConstant(value)
	}

	class LITERAL_REFNAME(override val value: Any?) : LiteralExpr() {
		override val type = AstType.STRING
	}

	class CAUGHT_EXCEPTION(override val type: AstType = AstType.OBJECT) : AstExpr() {

	}

	class BINOP(override val type: AstType, left: AstExpr, val op: AstBinop, right: AstExpr) : AstExpr() {
		val left = left.box
		val right = right.box
	}

	class UNOP(val op: AstUnop, right: AstExpr) : AstExpr() {
		val right = right.box
		override val type = right.type
	}

	abstract class CALL_BASE : AstExpr() {
		//override val type = method.type.ret
		abstract val method: AstMethodRef
		abstract val args: List<AstExpr.Box>
		abstract val isSpecial: Boolean
	}

	class CALL_INSTANCE(obj: AstExpr, override val method: AstMethodRef, args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE() {
		val obj = obj.box
		override val args = args.map { it.box }

		override val type = method.type.ret
	}

	//class CALL_SPECIAL(obj: AstExpr, override val method: AstMethodRef, args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE() {
	//	val obj = obj.box
	//	override val args = args.map { it.box }
	//
	//	override val type = method.type.ret
	//}

	class CALL_SUPER(obj: AstExpr, val target: FqName, override val method: AstMethodRef, args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE() {
		val obj = obj.box
		override val args = args.map { it.box }

		override val type = method.type.ret
	}

	class CALL_STATIC(val clazz: AstType.REF, override val method: AstMethodRef, args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE() {
		constructor(method: AstMethodRef, args: List<AstExpr>, isSpecial: Boolean = false) : this(method.classRef, method, args, isSpecial)

		override val args = args.map { it.box }
		//val clazz: AstType.REF = method.classRef.type
		override val type = method.type.ret
	}

	class ARRAY_LENGTH(array: AstExpr) : AstExpr() {
		val array = array.box
		override val type = AstType.INT
	}

	class ARRAY_ACCESS(array: AstExpr, index: AstExpr) : LValueExpr() {
		val array = array.box
		val index = index.box
		override val type by lazy { array.type.elementType }
	}

	class FIELD_INSTANCE_ACCESS(val field: AstFieldRef, expr: AstExpr) : LValueExpr() {
		val expr = expr.box
		override val type: AstType = field.type
	}

	class FIELD_STATIC_ACCESS(val field: AstFieldRef) : LValueExpr() {
		val clazzName = field.containingTypeRef
		override val type: AstType = field.type
	}

	class INSTANCE_OF(expr: AstExpr, val checkType: AstType.Reference) : AstExpr() {
		val expr = expr.box

		override val type = AstType.BOOL
	}

	class CAST constructor(expr: AstExpr, val to: AstType, val dummy: Boolean) : AstExpr() {
		val subject = expr.box
		val from: AstType get() = subject.type

		override val type = to

		override fun clone(): AstExpr = CAST(subject.value.clone(), to, true)
	}

	class NEW(val target: AstType.REF) : AstExpr() {
		override val type = target
	}

	class NEW_WITH_CONSTRUCTOR(val constructor: AstMethodRef, args: List<AstExpr>) : AstExpr() {
		val target: AstType.REF = constructor.containingClassType
		val args = args.map { it.box }
		override val type = target
	}

	class NEW_ARRAY(val arrayType: AstType.ARRAY, counts: List<AstExpr>) : AstExpr() {
		val counts = counts.map { it.box }
		override val type = arrayType
	}

	class INTARRAY_LITERAL(val values: List<Int>) : AstExpr() {
		val arrayType: AstType.ARRAY = AstType.ARRAY(AstType.INT)
		override val type = arrayType
	}

	class STRINGARRAY_LITERAL(val values: List<String>) : AstExpr() {
		val arrayType: AstType.ARRAY = AstType.ARRAY(AstType.STRING)
		override val type = arrayType
	}

	//class ARRAY_VALUES(elementType: AstType, val values: List<AstExpr>) : AstExpr() {
	//	val arrayType: AstType.ARRAY = AstType.ARRAY(elementType)
	//	override val type = arrayType
	//}

	class INVOKE_DYNAMIC_METHOD(
		val methodInInterfaceRef: AstMethodRef,
		val methodToConvertRef: AstMethodRef,
		var extraArgCount: Int
	) : AstExpr() {
		var startArgs = listOf<AstExpr>()
		override val type = AstType.REF(methodInInterfaceRef.containingClass)
	}

	infix fun ge(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.GE, that)
	infix fun gt(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.GT, that)
	infix fun lt(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.LT, that)
	infix fun le(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.LE, that)
	infix fun band(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.BAND, that)
	infix fun and(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.AND, that)
	infix fun instanceof(that: AstType.REF) = AstExpr.INSTANCE_OF(this, that)

	class TERNARY(val cond: AstExpr, val etrue: AstExpr, val efalse: AstExpr, val types: AstTypes) : AstExpr() {
		override val type: AstType = types.unify(etrue.type, efalse.type)
	}
}

fun AstExpr.LOCAL.setTo(value: AstExpr): AstStm.SET_LOCAL {
	val stm = AstStm.SET_LOCAL(this, value.castTo(this.type), dummy = true)
	this.local.write(stm)
	return stm
}

fun AstLocal.setTo(value: AstExpr): AstStm.SET_LOCAL = AstExpr.LOCAL(this).setTo(value)

fun stms(vararg stms: AstStm) = stms.toList().stm()

fun List<AstStm>.stm() = when (this.size) {
	0 -> AstStm.NOP("empty stm")
	1 -> this[0]
	else -> AstStm.STMS(this, dummy = true)
//else -> AstStm.STMS(stms.flatMap { if (it is AstStm.STMS) it.stms.map { it.value } else listOf(it) })
}

fun AstExpr.Box.isPure(): Boolean = this.value.isPure()

fun AstExpr.isPure(): Boolean = when (this) {
	is AstExpr.ARRAY_ACCESS -> this.array.isPure() && this.index.isPure() // Can cause null pointer/out of bounds
	is AstExpr.ARRAY_LENGTH -> true // Can cause null pointer
	is AstExpr.BINOP -> this.left.isPure() && this.right.isPure()
	is AstExpr.UNOP -> this.right.isPure()
	is AstExpr.CALL_BASE -> false // we would have to check call pureness
	is AstExpr.CAST -> this.subject.isPure()
	is AstExpr.FIELD_INSTANCE_ACCESS -> this.expr.isPure()
	is AstExpr.INSTANCE_OF -> this.expr.isPure()
	is AstExpr.TERNARY -> this.cond.isPure() && this.etrue.isPure() && this.efalse.isPure()
	is AstExpr.CAUGHT_EXCEPTION -> true
	is AstExpr.FIELD_STATIC_ACCESS -> true
	is AstExpr.LITERAL -> true
	is AstExpr.LOCAL -> true
	is AstExpr.TYPED_LOCAL -> true
	is AstExpr.NEW -> false
	is AstExpr.NEW_WITH_CONSTRUCTOR -> false
	is AstExpr.NEW_ARRAY -> true
	is AstExpr.INTARRAY_LITERAL -> true
	is AstExpr.STRINGARRAY_LITERAL -> true
	is AstExpr.PARAM -> true
	is AstExpr.THIS -> true
	else -> {
		println("Warning: Unhandled expr $this to check pureness")
		false
	}
}

fun AstExpr.castToUnoptimized(type: AstType): AstExpr = AstExpr.CAST(this, type, true)
fun AstExpr.castTo(type: AstType): AstExpr = this.castToInternal(type)

fun AstExpr.withoutCast(): AstExpr = when (this) {
	is AstExpr.CAST -> this.subject.value
	else -> this
}

fun AstExpr.withoutCasts(): AstExpr = when (this) {
	is AstExpr.CAST -> this.subject.value.withoutCasts()
	else -> this
}

fun AstExpr.castToInternal(type: AstType): AstExpr {
	val expr = this
	if (expr.type == type) return expr
	val exprType = expr.type.simplify()
	val to = type.simplify()
	if (exprType == to) return expr

	return when (expr) {
	//is AstExpr.LOCAL_BASE -> AstExpr.TYPED_LOCAL(expr.local, to)
		is AstExpr.LITERAL -> {
			val value = expr.value
			when (value) {
				null -> expr.castToUnoptimized(to)
				is String -> expr.castToUnoptimized(to)
				is AstType -> expr.castToUnoptimized(to)
				is Boolean -> value.castTo(to).lit
				is Byte -> value.castTo(to).lit
				is Char -> value.castTo(to).lit
				is Short -> value.castTo(to).lit
				is Int -> value.castTo(to).lit
				is Long -> value.castTo(to).lit
				is Float -> value.castTo(to).lit
				is Double -> value.castTo(to).lit
				else -> invalidOp("Unhandled '$value' : ${value.javaClass}")
			}
		}
		is AstExpr.CAST -> {
			if (expr.to is AstType.Primitive) {
				if (to is AstType.Primitive && expr.to.canHold(to)) {
					expr.subject.value.castTo(to)
				} else {
					expr.castToUnoptimized(to)
				}
			} else {
				expr.subject.value.castTo(to)
			}
		}
		else -> expr.castToUnoptimized(to)
	}
}

object AstExprUtils {
	fun localRef(local: AstLocal): AstExpr.LOCAL {
		val localExpr = AstExpr.LOCAL(local)
		//val refExpr = AstExpr.REF(localExpr)
		local.read(localExpr)
		return localExpr
	}

	fun INVOKE_DYNAMIC(generatedMethodRef: AstMethodWithoutClassRef, bootstrapMethodRef: AstMethodRef, bootstrapArgs: List<AstExpr>): AstExpr {
		if (bootstrapMethodRef.containingClass.fqname == "java.lang.invoke.LambdaMetafactory" &&
			bootstrapMethodRef.name == "metafactory"
			) {
			val literals = bootstrapArgs.cast<AstExpr.LiteralExpr>()
			val interfaceMethodType = literals[0].value as AstType.METHOD
			val methodHandle = literals[1].value as AstMethodHandle
			val methodType = literals[2].type

			val interfaceToGenerate = generatedMethodRef.type.ret as AstType.REF
			val methodToConvertRef = methodHandle.methodRef

			val methodFromRef = AstMethodRef(interfaceToGenerate.name, generatedMethodRef.name, interfaceMethodType)

			return AstExpr.INVOKE_DYNAMIC_METHOD(
				methodFromRef,
				methodToConvertRef,
				methodToConvertRef.type.argCount - methodFromRef.type.argCount
			)
		} else {
			noImpl("Not supported DynamicInvoke yet!")
		}
	}

	fun INVOKE_SPECIAL(obj: AstExpr, method: AstMethodRef, args: List<AstExpr>): AstExpr.CALL_BASE {
		if (obj.type !is AstType.REF) {
			invalidOp("Obj must be an object $obj, but was ${obj.type}")
		}

		//if (obj is AstExpr.THIS && ((obj.type as AstType.REF).name != method.containingClass)) {
		if ((obj.type as AstType.REF).name != method.containingClass) {
			//if (caller == "<init>" && ((obj.type as AstType.REF).name != method.containingClass)) {
			return AstExpr.CALL_SUPER(obj.castTo(method.containingClassType), method.containingClass, method, args, isSpecial = true)
		} else {
			return AstExpr.CALL_INSTANCE(obj.castTo(method.containingClassType), method, args, isSpecial = true)
		}
	}

	fun BINOP(type: AstType, l: AstExpr, op: AstBinop, r: AstExpr): AstExpr.BINOP {
		if (l.type == AstType.BOOL && r.type == AstType.BOOL) {
			if (op == AstBinop.AND) return AstExpr.BINOP(AstType.BOOL, l.castTo(AstType.BOOL), AstBinop.BAND, r.castTo(AstType.BOOL))
			if (op == AstBinop.OR) return AstExpr.BINOP(AstType.BOOL, l.castTo(AstType.BOOL), AstBinop.BOR, r.castTo(AstType.BOOL))
			if (op == AstBinop.XOR) return AstExpr.BINOP(AstType.BOOL, l.castTo(AstType.BOOL), AstBinop.NE, r.castTo(AstType.BOOL))
		} else if (l.type == AstType.BOOL) {
			return AstExpr.BINOP(type, l.castTo(r.type), op, r)
		} else if (r.type == AstType.BOOL) {
			return AstExpr.BINOP(type, l, op, r.castTo(l.type))
		}
		return AstExpr.BINOP(type, l, op, r)
	}

	fun RESOLVE_SPECIAL(program: AstProgram, e: AstExpr.CALL_INSTANCE, context: AstGenContext): AstExpr.CALL_BASE {
		val clazz = program.get3(e.method.classRef)
		val refMethod = program.get(e.method) ?: invalidOp("Can't find method: ${e.method} while generating $context")
		// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokespecial
		return if (refMethod.modifiers.isPrivate || refMethod.isInstanceInit) {
			// Call this!
			AstExpr.CALL_INSTANCE(e.obj.value, e.method, e.args.map { it.value }, e.isSpecial)
		} else {
			// Call super!
			if (context.method.ref != e.method) {
				AstExpr.CALL_SUPER(e.obj.value, e.method.containingClass, e.method, e.args.map { it.value }, e.isSpecial)
			} else {
				AstExpr.CALL_INSTANCE(e.obj.value, e.method, e.args.map { it.value }, e.isSpecial)
			}
		}
	}
}

open class BuilderBase(val types: AstTypes) {
	val BOOL get() = AstType.BOOL
	val BYTE get() = AstType.BYTE
	val SHORT get() = AstType.SHORT
	val CHAR get() = AstType.CHAR
	val INT get() = AstType.INT
	val LONG get() = AstType.LONG
	val FLOAT get() = AstType.FLOAT
	val DOUBLE get() = AstType.DOUBLE
	val OBJECT get() = AstType.OBJECT
	val CLASS get() = AstType.CLASS
	val STRING get() = AstType.STRING
	val NULL get() = null.lit
}

fun AstType.array() = AstType.ARRAY(this)
fun AstType.ARRAY.newArray(size: AstExpr) = AstExpr.NEW_ARRAY(this, listOf(size))
fun AstType.ARRAY.newArray(size: Int) = this.newArray(size.lit)

fun AstMethodRef.newInstance(vararg args: AstExpr) = AstExpr.NEW_WITH_CONSTRUCTOR(this, args.toList())

fun AstType.REF.constructor(methodType: AstType.METHOD): AstMethodRef = AstMethodRef(this.name, "<init>", methodType)
fun AstType.REF.constructor(vararg args: AstType): AstMethodRef = AstMethodRef(this.name, "<init>", AstType.METHOD(AstType.VOID, args.toList()))
fun AstType.REF.method(methodName: String, methodType: AstType.METHOD): AstMethodRef = AstMethodRef(this.name, methodName, methodType)

operator fun AstExpr.invoke(method: AstMethodRef, vararg args: AstExpr): AstExpr = AstExpr.CALL_INSTANCE(this, method, args.toList(), isSpecial = false)
operator fun AstLocal.invoke(method: AstMethodRef, vararg args: AstExpr): AstExpr = this.expr.invoke(method, *args)

val Any?.lit: AstExpr.LITERAL get() = AstExpr.LITERAL(this, dummy = true)

val AstLocal.local: AstExpr.LOCAL get() = AstExprUtils.localRef(this)
val AstLocal.expr: AstExpr.LOCAL get() = AstExprUtils.localRef(this)
val AstArgument.expr: AstExpr.PARAM get() = AstExpr.PARAM(this)

operator fun AstExpr.plus(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.ADD, that)
operator fun AstExpr.minus(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.SUB, that)
operator fun AstExpr.times(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.MUL, that)
infix fun AstExpr.eq(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.EQ, that)
infix fun AstExpr.ne(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.NE, that)

operator fun AstMethod.invoke(vararg exprs: AstExpr) = AstExpr.CALL_STATIC(this.ref, exprs.toList())
operator fun AstMethodRef.invoke(vararg exprs: AstExpr) = AstExpr.CALL_STATIC(this.ref, exprs.toList())

operator fun AstMethod.invoke(exprs: List<AstExpr>) = AstExpr.CALL_STATIC(this.ref, exprs)
operator fun AstMethodRef.invoke(exprs: List<AstExpr>) = AstExpr.CALL_STATIC(this.ref, exprs)

operator fun AstExpr.get(field: AstField) = AstExpr.FIELD_INSTANCE_ACCESS(field.ref, this)
operator fun AstExpr.get(field: AstFieldRef) = AstExpr.FIELD_INSTANCE_ACCESS(field, this)

operator fun AstExpr.get(method: MethodRef) = MethodWithRef(this, method.ref)
operator fun AstLocal.get(method: MethodRef) = MethodWithRef(this.expr, method.ref)

val Iterable<AstStm>.stms: AstStm get() = this.toList().stm()
fun AstExpr.not() = AstExpr.UNOP(AstUnop.NOT, this)

class MethodWithRef(val obj: AstExpr, val method: AstMethodRef) {
	operator fun invoke(vararg args: AstExpr) = AstExpr.CALL_INSTANCE(obj, method, args.toList())
}

inline fun <T> AstTypes.build2(callback: AstBuilder2.() -> T) = AstBuilder2(this, AstBuilderBodyCtx()).run { callback() }
inline fun AstTypes.buildStms(callback: AstBuilder2.() -> Unit) = AstBuilder2(this, AstBuilderBodyCtx()).apply { callback() }.stms
inline fun AstTypes.buildStm(callback: AstBuilder2.() -> Unit) = this.buildStms(callback).stm()

class AstBuilderBodyCtx {
	var localId = 0
}

class AstBuilder2(types: AstTypes, val ctx: AstBuilderBodyCtx) : BuilderBase(types) {
	val temps = TempAstLocalFactory()
	val stms = arrayListOf<AstStm>()

	inner class LOCAL(val type: AstType) {
		var local: AstLocal? = null

		operator fun getValue(thisRef: Any?, property: KProperty<*>): AstLocal {
			if (local == null) local = AstLocal(ctx.localId++, property.name, type)
			return local!!
		}
	}

	fun SET(local: AstLocal, expr: AstExpr) = run { stms += local.setTo(expr) }
	fun SET(local: AstExpr.LOCAL, expr: AstExpr) = run { stms += local.local.setTo(expr) }
	fun SET_ARRAY(local: AstLocal, index: AstExpr, value: AstExpr) = run { stms += AstStm.SET_ARRAY(local.local, index, value) }
	fun STM() = run { stms += listOf<AstStm>().stm() }
	fun STM(stm: AstStm) = run { stms += stm }
	fun STM(expr: AstExpr) = run { stms += AstStm.STM_EXPR(expr) }

	class IfElseBuilder(val IF: AstStm.IF, val IF_INDEX: Int, val stms: ArrayList<AstStm>, val types: AstTypes, val ctx: AstBuilderBodyCtx) {
		infix fun ELSE(callback: AstBuilder2.() -> Unit) {
			val body = AstBuilder2(types, ctx)
			body.callback()
			if (stms[IF_INDEX] != IF) invalidOp("Internal problem IF/IF_INDEX")
			stms[IF_INDEX] = AstStm.IF_ELSE(IF.cond.value, IF.strue.value, body.genstm())
		}
	}

	class AstSwitchBuilder(types: AstTypes, val ctx: AstBuilderBodyCtx) : BuilderBase(types) {
		var default: AstStm = listOf<AstStm>().stm()
		val cases = arrayListOf<Pair<Int, AstStm>>()

		inline fun CASE(subject: Int, callback: AstBuilder2.() -> Unit) {
			cases += subject to AstBuilder2(types, ctx).apply { this.callback() }.genstm()
		}

		inline fun DEFAULT(callback: AstBuilder2.() -> Unit) {
			default = AstBuilder2(types, ctx).apply { this.callback() }.genstm()
		}
	}

	inline fun IF(cond: AstExpr, callback: AstBuilder2.() -> Unit): IfElseBuilder {
		val body = AstBuilder2(types, ctx)
		body.callback()
		val IF = AstStm.IF(cond, body.genstm())
		val IF_INDEX = stms.size
		stms += IF
		return IfElseBuilder(IF, IF_INDEX, stms, types, ctx)
	}

	inline fun WHILE(cond: AstExpr, callback: AstBuilder2.() -> Unit) {
		stms += AstStm.WHILE(cond, AstBuilder2(types, ctx).apply(callback).genstm())
	}

	inline fun FOR(local: AstLocal, start: Int, until: Int, callback: AstBuilder2.() -> Unit) {
		val forStartLabel = AstLabel("forStartLabel")
		val forEndLabel = AstLabel("forEndLabel")
		SET(local, start.lit)
		stms += AstStm.STM_LABEL(forStartLabel)
		stms += AstStm.IF_GOTO(forEndLabel, local.expr ge until.lit)
		stms += AstBuilder2(types, ctx).apply { callback() }.genstm()
		SET(local, local.expr + 1.lit)
		stms += AstStm.GOTO(forStartLabel)
		stms += AstStm.STM_LABEL(forEndLabel)
	}

	inline fun SWITCH(subject: AstExpr, callback: AstSwitchBuilder.() -> Unit) {
		val cases = AstSwitchBuilder(types, ctx).apply { this.callback() }
		stms += AstStm.SWITCH(subject, cases.default, cases.cases)
	}

	fun CREATE_ARRAY(arrayType: AstType.ARRAY, items: List<AstExpr>): AstExpr {
		if (items.isEmpty()) {
			return arrayType.newArray(0.lit)
		} else {
			val temp = temps.create(arrayType)
			SET(temp, arrayType.newArray(items.size.lit))
			for ((index, item) in items.withIndex()) {
				SET_ARRAY(temp, index.lit, item)
			}
			return temp.expr
		}
	}

	fun RETURN() = Unit.apply { stms += AstStm.RETURN_VOID() }
	fun RETURN(value: AstExpr) = Unit.apply { stms += AstStm.RETURN(value) }
	fun RETURN(value: AstLocal) = Unit.apply { stms += AstStm.RETURN(value.expr) }

	fun genstm(): AstStm = stms.stm()
}

class AstMethodHandle(val type: AstType.METHOD, val methodRef: AstMethodRef, val kind: Kind) {
	enum class Kind(val id: Int) {
		REF_getField(1),
		REF_getStatic(2),
		REF_putField(3),
		REF_putStatic(4),
		REF_invokeVirtual(5),
		REF_invokeStatic(6),
		REF_invokeSpecial(7),
		REF_newInvokeSpecial(8),
		REF_invokeInterface(9);

		companion object {
			private val table = values().map { it.id to it }.toMap()
			fun fromId(id: Int) = table[id]!!
		}
	}

}
