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

package com.jtransc.ast.serialization

import com.jtransc.ast.AstBinop
import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstStm
import com.jtransc.error.noImpl
import com.jtransc.io.i16
import com.jtransc.io.i32
import com.jtransc.io.i8
import java.io.OutputStream

class AstWriter {
	fun writeStm(stm: AstStm, s: OutputStream) {
		when (stm) {
			is AstStm.STM_EXPR -> {
				s.i8(AstStmOp.EXPR)
				writeExpr(stm.expr, s)
			}
			else -> noImpl
		}
	}

	fun writeExpr(expr: AstExpr, s: OutputStream) {
		when (expr) {
			is AstExpr.THIS -> s.i8(AstExprOp.THIS)
			is AstExpr.LITERAL -> {
				val value = expr.value
				when (value) {
					null -> s.i8(AstExprOp.LIT_REF_NULL)
					is Boolean -> s.i8(if (value) AstExprOp.LIT_BOOL_TRUE else AstExprOp.LIT_BOOL_FALSE)
					is Byte -> {
						s.i8(AstExprOp.LIT_BYTE)
						s.i8(value.toInt())
					}
					is Short -> {
						s.i8(AstExprOp.LIT_SHORT)
						s.i16(value.toInt())
					}
					is Int -> {
						when (value) {
							-1 -> s.i8(AstExprOp.LIT_INT_M1)
							0 -> s.i8(AstExprOp.LIT_INT_0)
							1 -> s.i8(AstExprOp.LIT_INT_1)
							2 -> s.i8(AstExprOp.LIT_INT_2)
							3 -> s.i8(AstExprOp.LIT_INT_3)
							4 -> s.i8(AstExprOp.LIT_INT_4)
							5 -> s.i8(AstExprOp.LIT_INT_5)
							else -> {
								if (value.toByte().toInt() == value) {
									s.i8(AstExprOp.LIT_INT_BYTE)
									s.i8(value)
								} else if (value.toShort().toInt() == value) {
									s.i8(AstExprOp.LIT_INT_SHORT)
									s.i16(value)
								} else {
									s.i8(AstExprOp.LIT_INT_INT)
									s.i32(value)
								}
							}
						}
					}
					else -> noImpl
				}
			}
			is AstExpr.BINOP -> {
				when (expr.op) {
					AstBinop.ADD -> s.i8(AstExprOp.BIN_ADD)
					AstBinop.SUB -> s.i8(AstExprOp.BIN_SUB)
					else -> noImpl
				}
				writeExpr(expr.left, s)
				writeExpr(expr.right, s)
			}
			else -> noImpl
		}
	}
}