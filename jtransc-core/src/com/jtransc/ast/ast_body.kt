package com.jtransc.ast

data class AstBody(
	val stm: AstStm,
	val locals: List<AstLocal>,
	val traps: List<AstTrap>
)

enum class AstBinop(val symbol: String, val str: String) {
	ADD("+", "add"), SUB("-", "sub"), MUL("*", "mul"), DIV("/", "div"), REM("%", "rem"),
	AND("&", "and"), OR("|", "or"), XOR("^", "xor"),
	SHL("<<", "shl"), SHR(">>", "shr"), USHR(">>>", "ushr"),
	BAND("&&", "band"), BOR("||", "bor"),
	EQ("==", "eq"), NE("!=", "ne"), GE(">=", "ge"), LE("<=", "le"), LT("<", "lt"), GT(">", "gt"),
	LCMP("lcmp", "lcmp"), CMP("cmp", "cmp"), CMPL("cmpl", "cmpl"), CMPG("cmpg", "cmpg");

	companion object {
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

data class AstLocal(val index: Int, val name: String, val type: AstType) {
	override fun toString() = "AstLocal:$name:$type"
}

data class AstTrap(val start: AstLabel, val end: AstLabel, val handler: AstLabel, val exception: AstType.REF)

data class AstLabel(val name: String)

interface AstElement

interface AstStm : AstElement {
	data class STMS(val stms: List<AstStm>) : AstStm {
		constructor(vararg stms: AstStm) : this(stms.toList())
	}

	data class NOP(val dummy: Any? = null) : AstStm
	data class STM_EXPR(val expr: AstExpr) : AstStm
	data class SET(val local: AstLocal, val expr: AstExpr) : AstStm
	data class SET_ARRAY(val local: AstLocal, val index: AstExpr, val expr: AstExpr) : AstStm
	data class SET_FIELD_STATIC(val clazz: AstType.REF, val field: AstFieldRef, val expr: AstExpr, val isInterface: Boolean) : AstStm
	data class SET_FIELD_INSTANCE(val left: AstExpr, val field: AstFieldRef, val expr: AstExpr) : AstStm
	data class SET_NEW_WITH_CONSTRUCTOR(val local: AstLocal, val target: AstType.REF, val method: AstMethodRef, val args: List<AstExpr>) : AstStm

	data class IF(val cond: AstExpr, val strue: AstStm, val sfalse: AstStm? = null) : AstStm
	data class WHILE(val cond: AstExpr, val iter: AstStm) : AstStm
	data class RETURN(val retval: AstExpr?) : AstStm
	data class THROW(val value: AstExpr) : AstStm

	object RETHROW : AstStm

	//data class TRY_CATCH(val trystm: AstStm, val catches: List<Pair<AstType, AstStm>>) : AstStm
	data class TRY_CATCH(val trystm: AstStm, val catch: AstStm) : AstStm

	class BREAK() : AstStm
	class CONTINUE() : AstStm

	// SwitchFeature
	data class SWITCH(val subject: AstExpr, val default: AstStm, val cases: List<Pair<Int, AstStm>>) : AstStm

	// GotoFeature

	data class STM_LABEL(val label: AstLabel) : AstStm
	data class IF_GOTO(val cond: AstExpr, val label: AstLabel) : AstStm
	data class SWITCH_GOTO(val subject: AstExpr, val default: AstLabel, val cases: List<Pair<Int, AstLabel>>) : AstStm
	data class GOTO(val label: AstLabel) : AstStm

	data class MONITOR_ENTER(val expr: AstExpr) : AstStm
	data class MONITOR_EXIT(val expr: AstExpr) : AstStm
}

interface AstExpr : AstElement {
	open val type: AstType

	interface ImmutableRef : AstExpr
	interface LValueExpr : AstExpr

	data class THIS(val ref: FqName) : LValueExpr {
		override val type: AstType = AstType.REF(ref)
	}

	data class CLASS_CONSTANT(val classType: AstType) : AstExpr {
		override val type: AstType = AstType.GENERIC(AstType.REF("java.lang.Class"), listOf(classType))
	}

	data class LITERAL(val value: Any?) : AstExpr {
		override val type: AstType = when (value) {
			null -> AstType.NULL
			is Boolean -> AstType.BOOL
			is Byte -> AstType.BYTE
			is Char -> AstType.CHAR
			is Short -> AstType.SHORT
			is Int -> AstType.INT
			is Long -> AstType.LONG
			is Float -> AstType.FLOAT
			is Double -> AstType.DOUBLE
			is String -> AstType.STRING
			else -> throw NotImplementedError("Literal type: $value")
		}
	}

	data class LOCAL(val local: AstLocal) : LValueExpr {
		override val type = local.type
	}

	data class PARAM(val argument: AstArgument) : LValueExpr {
		override val type = argument.type
	}

	data class CAUGHT_EXCEPTION(override val type: AstType = AstType.OBJECT) : AstExpr
	data class BINOP(override val type: AstType, val left: AstExpr, val op: AstBinop, val right: AstExpr) : AstExpr

	data class UNOP(val op: AstUnop, val right: AstExpr) : AstExpr {
		override val type = right.type
	}

	interface CALL_BASE : AstExpr {
		//override val type = method.type.ret
		val method: AstMethodRef
		val args: List<AstExpr>
		val isSpecial: Boolean
	}

	data class CALL_INSTANCE(val obj: AstExpr, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
		override val type = method.type.ret
	}

	data class CALL_SUPER(val obj: AstExpr, val target: FqName, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
		override val type = method.type.ret
	}

	data class CALL_STATIC(val clazz: AstType.REF, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
		//val clazz: AstType.REF = method.classRef.type
		override val type = method.type.ret
	}

	data class ARRAY_LENGTH(val array: AstExpr) : AstExpr {
		override val type = AstType.INT
	}

	data class ARRAY_ACCESS(val array: AstExpr, val index: AstExpr) : LValueExpr {
		override val type = array.type.elementType
	}

	data class INSTANCE_FIELD_ACCESS(val expr: AstExpr, val field: AstFieldRef, override val type: AstType) : LValueExpr
	data class STATIC_FIELD_ACCESS(val clazzName: AstType.REF, val field: AstFieldRef, override val type: AstType, val isInterface: Boolean) : LValueExpr

	data class INSTANCE_OF(val expr: AstExpr, val checkType: AstType) : AstExpr {
		override val type = AstType.BOOL
	}

	data class CAST(val expr: AstExpr, val to: AstType) : AstExpr {
		val from: AstType get() = expr.type

		override val type = to
	}

	data class NEW(val target: AstType.REF) : AstExpr {
		override val type = target
	}

	data class NEW_WITH_CONSTRUCTOR(val target: AstType.REF, val method: AstMethodRef, val args: List<AstExpr>) : AstExpr {
		override val type = target
	}

	data class NEW_ARRAY(val element: AstType, val counts: List<AstExpr>) : AstExpr {
		override val type = AstType.ARRAY(element, counts.size)
	}

	data class METHOD_CLASS(
		val methodInInterfaceRef: AstMethodRef,
		val methodToConvertRef: AstMethodRef
	) : AstExpr {
		override val type = AstType.REF(methodInInterfaceRef.containingClass)
	}

	infix fun ge(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.GE, that)
	infix fun le(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.LE, that)
	infix fun band(that: AstExpr) = AstExpr.BINOP(AstType.BOOL, this, AstBinop.BAND, that)
	infix fun and(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.AND, that)
	infix fun instanceof(that: AstType) = AstExpr.INSTANCE_OF(this, that)
}

open class AstTransformer {
	open fun visit(type: AstType) {
	}

	open fun transform(body: AstBody): AstBody {
		for (local in body.locals) visit(local.type)
		return AstBody(
			stm = transform(body.stm),
			locals = body.locals,
			traps = body.traps
		)
	}

	open fun transform(stm: AstStm): AstStm = when (stm) {
		is AstStm.STMS -> transform(stm)
		else -> throw NotImplementedError("Unhandled statement $stm")
	}

	open fun transform(stm: AstStm.STMS): AstStm = AstStm.STMS(stm.stms.map { transform(stm) })

	open fun transform(expr: AstExpr): AstExpr = when (expr) {
	//else -> expr
		else -> throw NotImplementedError("Unhandled expression $expr")
	}
}

val AstLocal.expr: AstExpr.LOCAL get() = AstExpr.LOCAL(this)
val Iterable<AstStm>.stms: AstStm get() = AstStm.STMS(this.toList())
val Any?.lit: AstExpr get() = AstExpr.LITERAL(this)
