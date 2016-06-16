package com.jtransc.input

import com.jtransc.ast.AstClassGenerator
import com.jtransc.ast.AstTypes
import com.jtransc.vfs.SyncVfsFile

open class BaseProjectContext(
	val classNames: List<String>,
	val mainClass: String,
	val classPaths: List<String>,
	val output: SyncVfsFile,
	val generator: AstClassGenerator,
    val types: AstTypes
)
