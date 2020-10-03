package com.jtransc.plugin.junit

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.plugin.JTranscPlugin

class JUnitJTranscPlugin : JTranscPlugin() {
	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		for (method in oldclass.methods) {
			if (method.annotationsList.contains("org.junit.Test".fqname)) {
				treeShaking.addMethod(method.ref, JUnitJTranscPlugin::class.simpleName!!)
			}
		}
	}
}