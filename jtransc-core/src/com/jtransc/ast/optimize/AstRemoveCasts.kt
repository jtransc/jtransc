package com.jtransc.ast.optimize

import com.jtransc.ast.AstLocal
import com.jtransc.ast.AstStm
import com.jtransc.ast.AstVisitor

class AstRemoveCasts : AstVisitor() {
	override fun visit(local: AstLocal) {
		super.visit(local)
	}

	override fun visit(stm: AstStm.SET_LOCAL) {
		super.visit(stm)
	}
}