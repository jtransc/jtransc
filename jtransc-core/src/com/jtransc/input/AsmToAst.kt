package com.jtransc.input

import com.jtransc.ast.AstProgram
import com.jtransc.ast.AstRef
import com.jtransc.vfs.SyncVfsFile

object AsmToAst {
	fun createProgramAst(dependencies: List<String>, entryPoint: String, classPaths2: List<String>, localVfs: SyncVfsFile, set: Set<AstRef>?): AstProgram {
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}