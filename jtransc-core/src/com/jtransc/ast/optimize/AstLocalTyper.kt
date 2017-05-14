package com.jtransc.ast.optimize

import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstLocal
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstVisitor

class AstLocalTyper : AstVisitor() {
	override fun visit(local: AstLocal) {
		super.visit(local)
	}
}