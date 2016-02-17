package com.jtransc.ast.serialization

object AstExprOp {
	const val THIS:Int = 0
	const val LIT_REF_NULL:Int = 1
	const val LIT_BOOL_TRUE:Int = 2
	const val LIT_BOOL_FALSE:Int = 3
	const val LIT_BYTE:Int = 4
	const val LIT_SHORT:Int = 5
	const val LIT_INT_M1:Int = 6
	const val LIT_INT_0:Int = 7
	const val LIT_INT_1:Int = 8
	const val LIT_INT_2:Int = 9
	const val LIT_INT_3:Int = 10
	const val LIT_INT_4:Int = 11
	const val LIT_INT_5:Int = 12
	const val LIT_INT_BYTE:Int = 13
	const val LIT_INT_SHORT:Int = 14
	const val LIT_INT_INT:Int = 15

	const val BIN_ADD:Int = 100
	const val BIN_SUB:Int = 101
	const val BIN_MUL:Int = 102
	const val BIN_DIV:Int = 103
	const val BIN_REM:Int = 104

	const val BIN_AND:Int = 105
	const val BIN_OR:Int = 106
	const val BIN_XOR:Int = 107
}

object AstStmOp {
	const val EXPR:Int = 0
}
