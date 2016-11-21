package com.jtransc.gen.common

import com.jtransc.ConfigTargetDirectory
import com.jtransc.ast.*
import com.jtransc.ast.template.CommonTagHandler
import com.jtransc.error.InvalidOperationException
import com.jtransc.error.invalidOp
import com.jtransc.injector.Injector
import com.jtransc.injector.Singleton
import com.jtransc.template.Minitemplate
import com.jtransc.vfs.LocalVfs
import com.jtransc.vfs.MergeVfs
import com.jtransc.vfs.SyncVfsFile
import java.io.File
import java.util.*

class ConfigOutputFile2(val file: File)
class ConfigTargetFolder(val targetFolder: SyncVfsFile)

interface IProgramTemplate {
	fun gen(template: String): String
}
