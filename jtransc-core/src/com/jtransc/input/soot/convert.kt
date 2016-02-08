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
import soot.*
import soot.jimple.*

fun BinopExpr.getAstOp(l:AstType, r:AstType): AstBinop {
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
	FqName(this.declaringClass.name),
	this.name,
	AstType.METHOD_TYPE(
		this.parameterTypes.withIndex().map {
			val (index, type) = it
			AstArgument(index, (type as Type).astType)
		},
		this.returnType.astType
	)
)

val SootMethod.astType: AstType.METHOD_TYPE get() = AstType.METHOD_TYPE(this.returnType.astType, this.parameterTypes.map { (it as Type).astType })

val SootClass.astType: AstType.REF get() = this.type.astType as AstType.REF

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
	is ArrayType -> AstType.ARRAY(baseType.astType, numDimensions)
	is RefType -> AstType.REF(FqName(className))
	is NullType -> AstType.NULL
	else -> throw NotImplementedError("toAstType: $this")
}

val SootField.ast: AstFieldRef get() = AstFieldRef(
	FqName(this.declaringClass.name),
	this.name,
	this.type.astType
)

val FieldRef.ast: AstFieldRef get() = this.field.ast

val SootMethod.astVisibility: AstVisibility get() = if (this.isPublic) {
	AstVisibility.PUBLIC
} else if (this.isProtected) {
	AstVisibility.PROTECTED
} else {
	AstVisibility.PRIVATE
}