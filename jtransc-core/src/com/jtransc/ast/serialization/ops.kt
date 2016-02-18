package com.jtransc.ast.serialization

object AstExprOp {
	const val THIS = 0
	const val LIT_REF_NULL = 1
	const val LIT_BOOL_TRUE = 2
	const val LIT_BOOL_FALSE = 3
	const val LIT_BYTE = 4
	const val LIT_SHORT = 5
	const val LIT_INT_M1 = 6
	const val LIT_INT_0 = 7
	const val LIT_INT_1 = 8
	const val LIT_INT_2 = 9
	const val LIT_INT_3 = 10
	const val LIT_INT_4 = 11
	const val LIT_INT_5 = 12
	const val LIT_INT_BYTE = 13
	const val LIT_INT_SHORT = 14
	const val LIT_INT_INT = 15

	const val BIN_ADD = 100
	const val BIN_SUB = 101
	const val BIN_MUL = 102
	const val BIN_DIV = 103
	const val BIN_REM = 104

	const val BIN_AND = 105
	const val BIN_OR = 106
	const val BIN_XOR = 107
}

object AstStmOp {
	const val EXPR = 0
}
