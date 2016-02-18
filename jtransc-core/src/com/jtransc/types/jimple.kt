package com.jtransc.types

import com.jtransc.ast.AstBinop
import com.jtransc.ast.AstType
import com.jtransc.ast.AstUnop

interface Jimple {
	data class Body(val items: List<Jimple>)
	data class Local(val type: AstType, val index: Int)

	data class ILabel(val id: String)

	interface Method
	interface Field
	data class LABEL(val label: ILabel) : Jimple
	data class THIS(val target: Local) : Jimple
	data class PARAM(val target: Local, val index: Int) : Jimple
	data class CONST(val target: Local, val value: Any?) : Jimple
	data class BINOP(val target: Local, val left: Local, val right: Local, val operator: AstBinop) : Jimple
	data class UNOP(val target: Local, val right: Local, val operation: AstUnop) : Jimple
	data class ARRAYSET(val array: Local, val index: Local, val value: Local) : Jimple
	data class ARRAYGET(val target: Local, val array: Local, val index: Local) : Jimple
	data class FIELDSET(val obj: Local, val field: Field, val value: Local) : Jimple
	data class FIELDGET(val target: Local, val obj: Local, val field: Field) : Jimple
	data class INVOKE(val target: Local, val obj: Local, val method: Method, val args: List<Local>) : Jimple
	data class GOTO(val label: ILabel) : Jimple
	data class IF_TRUE(val local: Local, val label: ILabel) : Jimple
	data class RETURN(val value: Local) : Jimple
	class RETURNVOID() : Jimple
	data class THROW(val value: Local) : Jimple
}