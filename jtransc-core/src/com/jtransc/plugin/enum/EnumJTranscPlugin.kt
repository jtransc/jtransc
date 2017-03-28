package com.jtransc.plugin.enum

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.plugin.JTranscPlugin

/**
 * Plugin that include values() method from enums
 */
class EnumJTranscPlugin : JTranscPlugin() {
	val enumName = "java.lang.Enum"
	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		if (oldclass.modifiers.isEnum && oldclass.extending.toString() == enumName) {
			treeShaking.addMethod(AstMethodRef(fqname, "values", AstType.METHOD(ARRAY(oldclass.astType), listOf())), "EnumJTranscPlugin")
		}
	}
}
