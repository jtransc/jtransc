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
import com.jtransc.ast.FqName
import com.jtransc.error.invalidOp
import com.jtransc.error.noImpl
import com.jtransc.io.i8
import java.io.InputStream

class AstRead {
	val current = FqName("java.lang.Object")

	fun readBinop(s: InputStream, op:AstBinop): AstExpr {
		val l = readExpr(s)
		val r = readExpr(s)
		return AstExpr.BINOP(l.type, l, AstBinop.ADD, r)
	}

	fun readStm(s: InputStream): AstStm {
		return when (s.i8()) {
			AstStmOp.EXPR -> AstStm.STM_EXPR(readExpr(s))
			else -> noImpl
		}
	}

	fun readExpr(s: InputStream): AstExpr {
		return when (s.i8()) {
			AstExprOp.THIS -> AstExpr.THIS(current)
			AstExprOp.LIT_BOOL_FALSE -> AstExpr.LITERAL(false)
			AstExprOp.LIT_BOOL_TRUE -> AstExpr.LITERAL(true)
			AstExprOp.BIN_ADD -> readBinop(s, AstBinop.ADD)
			AstExprOp.BIN_SUB -> readBinop(s, AstBinop.SUB)
			else -> noImpl
		}
	}
}