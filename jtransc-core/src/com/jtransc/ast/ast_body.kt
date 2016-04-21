package com.jtransc.ast

import com.jtransc.ds.cast
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.lang.*

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

data class AstLocal(val index: Int, val name: String, val type: AstType) {
	override fun toString() = "AstLocal:$name:$type"
	val writes = arrayListOf<AstStm.SET>()
	val reads = arrayListOf<AstExpr.REF>()

	fun write(set: AstStm.SET) {
		writes += set
	}

	fun read(ref: AstExpr.REF) {
		reads += ref
	}
}

data class AstTrap(val start: AstLabel, val end: AstLabel, val handler: AstLabel, val exception: AstType.REF)

data class AstLabel(val name: String)

interface AstElement

interface AstStm : AstElement {
	data class STMS(val stms: List<AstStm>) : AstStm {
		constructor(vararg stms: AstStm) : this(stms.toList())
	}

	object NOP : AstStm

	data class LINE(val line: Int) : AstStm
	data class STM_EXPR(val expr: AstExpr) : AstStm
	data class SET(val local: AstExpr.LocalExpr, val expr: AstExpr) : AstStm
	data class SET_ARRAY(val array: AstExpr, val index: AstExpr, val expr: AstExpr) : AstStm
	data class SET_FIELD_STATIC(val field: AstFieldRef, val expr: AstExpr) : AstStm {
		val clazz = AstType.REF(field.classRef.fqname)
	}

	data class SET_FIELD_INSTANCE(val field: AstFieldRef, val left: AstExpr, val expr: AstExpr) : AstStm
	data class SET_NEW_WITH_CONSTRUCTOR(val local: AstExpr.LocalExpr, val target: AstType.REF, val method: AstMethodRef, val args: List<AstExpr>) : AstStm

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
	data class SWITCH_GOTO(val subject: AstExpr, val default: AstLabel, val cases: List<Pair<Int, AstLabel>>) : AstStm

	data class IF_GOTO(val label: AstLabel, val cond: AstExpr?) : AstStm

	data class MONITOR_ENTER(val expr: AstExpr) : AstStm
	data class MONITOR_EXIT(val expr: AstExpr) : AstStm

	object DEBUG : AstStm

	object NOT_IMPLEMENTED : AstStm
}

interface AstExpr : AstElement {
	open val type: AstType

	interface ImmutableRef : AstExpr
	interface LValueExpr : AstExpr {
	}

	interface LocalExpr : LValueExpr {
		val name: String
	}

	// Reference
	class REF(expr: AstExpr) : AstExpr {
		var expr = expr
			get() = field
			set(value) {
				field = value
			}
		override val type: AstType = expr.type
	}

	data class THIS(val ref: FqName) : LocalExpr {
		override val name: String get() = "this"
		override val type: AstType = AstType.REF(ref)
	}

	data class LOCAL(val local: AstLocal) : LocalExpr {
		override val name: String get() = local.name
		override val type = local.type
	}

	data class PARAM(val argument: AstArgument) : LocalExpr {
		override val name: String get() = argument.name
		override val type = argument.type
	}

	interface LiteralExpr : AstExpr {
		val value: Any?
	}

	//data class CLASS_CONSTANT(val classType: AstType) : AstExpr, LiteralExpr {
	//	override val value = classType
	//	override val type: AstType = AstType.GENERIC(AstType.REF("java.lang.Class"), listOf(classType))
	//}

	data class METHODTYPE_CONSTANT(val methodType: AstType.METHOD) : AstExpr, LiteralExpr {
		override val value = methodType
		override val type: AstType = methodType
	}

	data class METHODREF_CONSTANT(val methodRef: AstMethodRef) : AstExpr, LiteralExpr {
		override val value = methodRef
		override val type: AstType = AstType.UNKNOWN
	}

	data class METHODHANDLE_CONSTANT(val methodHandle: AstMethodHandle) : AstExpr, LiteralExpr {
		override val value = methodHandle
		override val type: AstType = AstType.UNKNOWN
	}

	data class LITERAL(override val value: Any?) : AstExpr, LiteralExpr {
		override val type = AstType.fromConstant(value)
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

	data class CALL_SPECIAL(val obj: AstExpr, override val method: AstMethodRef, override val args: List<AstExpr>, override val isSpecial: Boolean = false) : CALL_BASE {
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

	data class INSTANCE_FIELD_ACCESS(val field: AstFieldRef, val expr: AstExpr) : LValueExpr {
		override val type: AstType = field.type
	}

	data class STATIC_FIELD_ACCESS(val field: AstFieldRef) : LValueExpr {
		val clazzName = field.containingTypeRef
		override val type: AstType = field.type
	}

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

	data class NEW_ARRAY(val arrayType: AstType.ARRAY, val counts: List<AstExpr>) : AstExpr {
		override val type = arrayType
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

object AstStmUtils {
	fun set(local: AstLocal, value: AstExpr): AstStm.SET {
		val stm = AstStm.SET(AstExpr.LOCAL(local), AstExprUtils.fastcast(value, local.type))
		local.write(stm)
		return stm
	}
}

object AstExprUtils {
	fun localRef(local: AstLocal): AstExpr.REF {
		val localExpr = AstExpr.LOCAL(local)
		val refExpr = AstExpr.REF(localExpr)
		local.read(refExpr)
		return refExpr
	}

	fun cast(expr: AstExpr, to: AstType): AstExpr {
		if (expr is AstExpr.LITERAL) {
			val value = expr.value
			when (value) {
				is Boolean -> castLiteral(value, to)
				is Byte -> castLiteral(value, to)
				is Char -> castLiteral(value, to)
				is Short -> castLiteral(value, to)
				is Int -> castLiteral(value, to)
				is Long -> castLiteral(value, to)
				is Float -> castLiteral(value, to)
				is Double -> castLiteral(value, to)
			}
			//return AstExpr.LITERAL(expr.value)
		}

		if (expr.type != to) {
			return AstExpr.CAST(expr, to)
		} else {
			return expr
		}
	}

	// Can cast nulls
	fun fastcast(expr: AstExpr, to: AstType): AstExpr {
		// LITERAL + IMMEDIATE = IMMEDIATE casted
		if (expr.type != to) {
			return AstExpr.CAST(expr, to)
		} else {
			return expr
		}
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

			return AstExpr.METHOD_CLASS(
				AstMethodRef(interfaceToGenerate.name, generatedMethodRef.name, interfaceMethodType),
				methodToConvertRef
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
			return AstExpr.CALL_SUPER(cast(obj, method.containingClassType), method.containingClass, method, args, isSpecial = true)
		} else {
			return AstExpr.CALL_INSTANCE(cast(obj, method.containingClassType), method, args, isSpecial = true)
		}
	}

	fun BINOP(type: AstType, l: AstExpr, op: AstBinop, r: AstExpr): AstExpr.BINOP {
		if (l.type == AstType.BOOL && r.type == AstType.BOOL) {
			if (op == AstBinop.AND) return AstExpr.BINOP(AstType.BOOL, cast(l, AstType.BOOL), AstBinop.BAND, cast(r, AstType.BOOL))
			if (op == AstBinop.OR) return AstExpr.BINOP(AstType.BOOL, cast(l, AstType.BOOL), AstBinop.BOR, cast(r, AstType.BOOL))
			if (op == AstBinop.XOR) return AstExpr.BINOP(AstType.BOOL, cast(l, AstType.BOOL), AstBinop.NE, cast(r, AstType.BOOL))
		} else if (l.type == AstType.BOOL) {
			return AstExpr.BINOP(type, cast(l, r.type), op, r)
		} else if (r.type == AstType.BOOL) {
			return AstExpr.BINOP(type, l, op, cast(r, l.type))
		}
		return AstExpr.BINOP(type, l, op, r)
	}

	fun RESOLVE_SPECIAL(program: AstProgram, e: AstExpr.CALL_SPECIAL, context: AstGenContext): AstExpr.CALL_BASE {
		val clazz = program[e.method.classRef]
		val refMethod = program.get(e.method) ?: invalidOp("Can't find method: ${e.method} while generating $context")
		// https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokespecial
		return if (refMethod.modifiers.isPrivate || refMethod.isInstanceInit) {
			// Call this!
			AstExpr.CALL_INSTANCE(e.obj, e.method, e.args, e.isSpecial)
		} else {
			// Call super!
			if (context.method.ref != e.method) {
				AstExpr.CALL_SUPER(e.obj, e.method.containingClass, e.method, e.args, e.isSpecial)
			} else {
				AstExpr.CALL_INSTANCE(e.obj, e.method, e.args, e.isSpecial)
			}
			/*
			val parentClass = clazz.parentClass
			val superMethod = parentClass?.get(e.method.withoutClass)
			if (superMethod == null) {
				if (e.method.name == "append") {
					println("NULL = ${e.method}")
				}
				//invalidOp("superMethod == null")
				AstExpr.CALL_INSTANCE(e.obj, e.method, e.args, e.isSpecial)
			} else {
				AstExpr.CALL_SUPER(e.obj, superMethod.containingClass.ref.name, superMethod.ref, e.args, e.isSpecial)
			}
			*/
		}
	}
}

operator fun AstExpr.plus(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.ADD, that)
operator fun AstExpr.minus(that: AstExpr) = AstExpr.BINOP(this.type, this, AstBinop.SUB, that)

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

val Iterable<AstStm>.stms: AstStm get() = AstStm.STMS(this.toList())
val Any?.lit: AstExpr get() = AstExpr.LITERAL(this)
