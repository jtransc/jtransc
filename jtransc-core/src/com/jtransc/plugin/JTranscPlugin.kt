package com.jtransc.plugin

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.TreeShakingApi

abstract class JTranscPlugin {
	open val priority: Int = 0

	open fun onStartBuilding(program: AstProgram): Unit {
	}

	open fun onAfterAllClassDiscovered(program: AstProgram): Unit {
	}

	open fun onAfterClassDiscovered(clazz: AstType.REF, program: AstProgram): Unit {
	}

	open fun processAfterTreeShaking(program: AstProgram): Unit {
	}

	open fun processBeforeTreeShaking(programBase: AstProgram): Unit {
	}

	open fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass): Unit {
	}

	open fun onTreeShakingAddField(treeShakingApi: TreeShakingApi, oldfield: AstField, newfield: AstField) {
	}

	open fun onTreeShakingAddMethod(treeShakingApi: TreeShakingApi, oldmethod: AstMethod, newmethod: AstMethod) {
	}
}
