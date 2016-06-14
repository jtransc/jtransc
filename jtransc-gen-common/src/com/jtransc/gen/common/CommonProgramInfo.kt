package com.jtransc.gen.common

import com.jtransc.ast.FqName

interface CommonProgramInfo {
	val entryPointClass: FqName
	val entryPointFile: String
	//val vfs: SyncVfsFile
}