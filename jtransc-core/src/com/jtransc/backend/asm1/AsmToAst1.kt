package com.jtransc.backend.asm1

import com.jtransc.ast.AstBody
import com.jtransc.ast.AstType
import com.jtransc.ast.AstTypes
import com.jtransc.backend.asm1.AsmToAstMethodBody1
import com.jtransc.backend.BaseAsmToAst
import com.jtransc.injector.Singleton
import com.jtransc.org.objectweb.asm.tree.MethodNode

@Singleton
class AsmToAst1(types: AstTypes) : BaseAsmToAst(types) {
	override fun genBody(classRef: AstType.REF, methodNode: MethodNode, types: AstTypes, source: String): AstBody {
		return AsmToAstMethodBody1(classRef, methodNode, types, source)
	}
}
