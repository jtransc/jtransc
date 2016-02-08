/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.input.soot

import com.jtransc.ast.*
import com.jtransc.ds.zipped
import com.jtransc.error.InvalidOperationException
import soot.*
import soot.jimple.*

open class AstMethodProcessor private constructor(
	private val method: SootMethod,
	private val context: SootToAst.SootContext
) {
	companion object {
		fun processBody(method: SootMethod, context: SootToAst.SootContext): AstBody {
			return AstMethodProcessor(method, context).handle()
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

	private fun convert(s: soot.Unit): AstStm = when (s) {
		is DefinitionStmt -> {
			val l = convert(s.leftOp)
			val r = convert(s.rightOp)
			when (l) {
				is AstExpr.LOCAL -> AstStm.SET(l.local, r)
				is AstExpr.ARRAY_ACCESS -> AstStm.SET_ARRAY((l.array as AstExpr.LOCAL).local, l.index, r)
				is AstExpr.STATIC_FIELD_ACCESS -> AstStm.SET_FIELD_STATIC(l.clazzName, l.field, r, l.isInterface)
				is AstExpr.INSTANCE_FIELD_ACCESS -> {
					AstStm.SET_FIELD_INSTANCE(l.expr, l.field, r)
				}
				else -> throw InvalidOperationException("Can't handle leftOp: $l")
			}
		}
		is ReturnStmt -> AstStm.RETURN(convert(s.op))
		is ReturnVoidStmt -> AstStm.RETURN(null)
		is IfStmt -> AstStm.IF_GOTO(convert(s.condition), ensureLabel(s.target))
		is GotoStmt -> AstStm.GOTO(ensureLabel(s.target))
		is ThrowStmt -> AstStm.THROW(convert(s.op))
		is InvokeStmt -> AstStm.STM_EXPR(convert(s.invokeExpr))
		is EnterMonitorStmt -> AstStm.MONITOR_ENTER(convert(s.op))
		is ExitMonitorStmt -> AstStm.MONITOR_EXIT(convert(s.op))
		is NopStmt -> AstStm.STMS(listOf())
		is LookupSwitchStmt -> AstStm.SWITCH_GOTO(
			convert(s.key),
			ensureLabel(s.defaultTarget),
			(0 until s.targetCount).map {
				val (key, label) = Pair(s.getLookupValue(it), s.getTarget(it))
				Pair(key, ensureLabel(label))
			}//.uniqueMap()
		)
		is TableSwitchStmt -> AstStm.SWITCH_GOTO(
			convert(s.key),
			ensureLabel(s.defaultTarget),
			(s.lowIndex..s.highIndex).map {
				Pair(it, ensureLabel(s.getTarget(it - s.lowIndex)))
			}//.uniqueMap()
		)
		else -> throw RuntimeException()
	}

	private fun simplify(expr: AstExpr): AstExpr {
		if ((expr is AstExpr.CAST) && (expr.expr is AstExpr.LITERAL) && (expr.from == AstType.INT) && (expr.to == AstType.BOOL)) {
			return AstExpr.LITERAL(expr.expr.value != 0)
		}

		// No simplified!
		return expr
	}

	private fun convert(c: Value): AstExpr = when (c) {
		is Local -> AstExpr.LOCAL(ensureLocal(c))
		is NullConstant -> AstExpr.LITERAL(null)
		is IntConstant -> AstExpr.LITERAL(c.value)
		is LongConstant -> AstExpr.LITERAL(c.value)
		is FloatConstant -> AstExpr.LITERAL(c.value)
		is DoubleConstant -> AstExpr.LITERAL(c.value)
		is StringConstant -> AstExpr.LITERAL(c.value)
		is ClassConstant -> {
			val className = c.value.replace('/', '.')
			if (className.startsWith("[")) {
				AstExpr.CLASS_CONSTANT(AstType.demangle(className))
			} else {
				AstExpr.CLASS_CONSTANT(AstType.REF(className))
			}
		}
		is ThisRef -> AstExpr.THIS(FqName(method.declaringClass.name))
		is ParameterRef -> AstExpr.PARAM(AstArgument(c.index, c.type.astType))
		is CaughtExceptionRef -> AstExpr.CAUGHT_EXCEPTION(c.type.astType)
		is ArrayRef -> AstExpr.ARRAY_ACCESS(convert(c.base), convert(c.index))
		is InstanceFieldRef -> AstExpr.INSTANCE_FIELD_ACCESS(convert(c.base), c.field.ast, c.field.type.astType)
		is StaticFieldRef -> AstExpr.STATIC_FIELD_ACCESS(AstType.REF(c.field.declaringClass.name), c.field.ast, c.field.type.astType, c.field.declaringClass.isInterface)
		is CastExpr -> AstExpr.CAST(c.castType.astType, convert(c.op))
		is InstanceOfExpr -> AstExpr.INSTANCE_OF(convert(c.op), c.checkType.astType)
		is NewExpr -> AstExpr.NEW(c.type.astType as AstType.REF)
		is NewArrayExpr -> AstExpr.NEW_ARRAY(c.baseType.astType, listOf(convert(c.size)))
		is NewMultiArrayExpr -> AstExpr.NEW_ARRAY(c.baseType.astType, (0 until c.sizeCount).map { convert(c.getSize(it)) })
		is LengthExpr -> AstExpr.ARRAY_LENGTH(convert(c.op))
		is NegExpr -> AstExpr.UNOP(AstUnop.NEG, convert(c.op))
		is BinopExpr -> {
			// @TODO: Make this generic!
			val destType = c.type.astType
			val l = convert(c.op1)
			val r = convert(c.op2)
			val lType = l.type
			val rType = r.type
			val op = c.getAstOp(lType, rType)
			if (c.op1.type is BooleanType && (op.symbol == "==") && (c.op2.type is IntType)) {
				AstExpr.BINOP(destType, l, op, simplify(AstExpr.CAST(AstType.BOOL, r)))
			} else {
				AstExpr.BINOP(destType, l, op, r)
			}
		}
		is InvokeExpr -> {
			val argsList = c.args.toList()
			val castTypes = c.method.parameterTypes.map { it as Type }
			val args = Pair(argsList, castTypes).zipped.map {
				val (value, expectedType) = it
				doCastIfNeeded(expectedType, value)
			}.toList()
			val i = c
			when (i) {
				is StaticInvokeExpr -> {
					AstExpr.CALL_STATIC(AstType.REF(c.method.declaringClass.name), c.method.astRef, args)
				}
				is InstanceInvokeExpr -> {
					val isSpecial = i is SpecialInvokeExpr
					val obj = convert(i.base)
					val method = c.method.astRef
					val objType = obj.type
					var castToObject = false

					if (isSpecial && ((obj.type as AstType.REF).name != method.containingClass)) {
						AstExpr.CALL_SUPER(obj, method.containingClass, method, args, isSpecial)
					} else {
						if (objType is AstType.ARRAY) {
							castToObject = true
						} else if (objType is AstType.REF && context.getClass(objType.name).clazz.isInterface) {
							castToObject = true
						}

						val obj2 = if (castToObject) AstExpr.CAST(method.classRef.type, obj) else obj

						AstExpr.CALL_INSTANCE(obj2, method, args, isSpecial)
					}
				}
				else -> throw RuntimeException()
			}
		}
		else -> throw RuntimeException()
	}

	final fun doCastIfNeeded(toType: Type, value: Value): AstExpr = if (value.type == toType) {
		convert(value)
	} else {
		AstExpr.CAST(value.type.astType, toType.astType, convert(value))
	}
}
