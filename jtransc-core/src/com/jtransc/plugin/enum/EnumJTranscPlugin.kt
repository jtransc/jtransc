package com.jtransc.plugin.enum

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.plugin.JTranscPlugin

/**
 * Plugin that include values() method from enums
 */
class EnumJTranscPlugin : JTranscPlugin() {
	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		if (oldclass.isEnum && oldclass.extending == java.lang.Enum::class.java.fqname) {
			treeShaking.addMethod(AstMethodRef(fqname, "values", AstType.METHOD(ARRAY(oldclass.astType), listOf())), "EnumJTranscPlugin")
		}
	}
}
