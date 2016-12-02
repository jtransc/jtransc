package com.jtransc.plugin.lang

import com.jtransc.ast.AstClass
import com.jtransc.ast.FqName
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.plugin.JTranscPlugin

class IncludeSingleConstructorsJTranscPlugin : JTranscPlugin() {
	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		if (oldclass.constructors.size == 1) {
			treeShaking.addMethod(oldclass.constructors.first().ref, "Single constructor")
		}
	}
}