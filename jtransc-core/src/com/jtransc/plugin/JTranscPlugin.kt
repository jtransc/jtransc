package com.jtransc.plugin

import com.jtransc.ast.*
import com.jtransc.ast.dependency.AstDependencyAnalyzer
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.gen.TargetName
import com.jtransc.injector.Injector

abstract class JTranscPlugin {
	open val priority: Int = 0

	lateinit var injector: Injector; private set
	lateinit var targetName: TargetName; private set

	fun initialize(injector: Injector) {
		this.injector = injector
		this.targetName = injector.get()
	}

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

	open fun onStaticInitHandleMethodCall(program: AstProgram, ast: AstExpr.CALL_BASE, body: AstBody?, da: AstDependencyAnalyzer.AstDependencyAnalyzerGen) {
	}

	open fun onAfterAppliedClassFeatures(program: AstProgram) {
	}

	open fun onAfterAppliedMethodBodyFeature(method: AstMethod, transformedBody: AstBody) {
	}
}

class JTranscPluginGroup(val plugins: Iterable<JTranscPlugin>) : JTranscPlugin() {
	override fun onStartBuilding(program: AstProgram) {
		for (plugin in plugins) plugin.onStartBuilding(program)
	}

	override fun onAfterAllClassDiscovered(program: AstProgram) {
		for (plugin in plugins) plugin.onAfterAllClassDiscovered(program)
	}

	override fun onAfterClassDiscovered(clazz: AstType.REF, program: AstProgram) {
		for (plugin in plugins) plugin.onAfterClassDiscovered(clazz, program)
	}

	override fun processAfterTreeShaking(program: AstProgram) {
		for (plugin in plugins) plugin.processAfterTreeShaking(program)
	}

	override fun processBeforeTreeShaking(programBase: AstProgram) {
		for (plugin in plugins) plugin.processBeforeTreeShaking(programBase)
	}

	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		for (plugin in plugins) plugin.onTreeShakingAddBasicClass(treeShaking, fqname, oldclass, newclass)
	}

	override fun onTreeShakingAddField(treeShakingApi: TreeShakingApi, oldfield: AstField, newfield: AstField) {
		for (plugin in plugins) plugin.onTreeShakingAddField(treeShakingApi, oldfield, newfield)
	}

	override fun onTreeShakingAddMethod(treeShakingApi: TreeShakingApi, oldmethod: AstMethod, newmethod: AstMethod) {
		for (plugin in plugins) plugin.onTreeShakingAddMethod(treeShakingApi, oldmethod, newmethod)
	}

	override fun onStaticInitHandleMethodCall(program: AstProgram, ast: AstExpr.CALL_BASE, body: AstBody?, da: AstDependencyAnalyzer.AstDependencyAnalyzerGen) {
		for (plugin in plugins) plugin.onStaticInitHandleMethodCall(program, ast, body, da)
	}

	override fun onAfterAppliedClassFeatures(program: AstProgram) {
		for (plugin in plugins) plugin.onAfterAppliedClassFeatures(program)
	}

	override fun onAfterAppliedMethodBodyFeature(method: AstMethod, transformedBody: AstBody) {
		for (plugin in plugins) plugin.onAfterAppliedMethodBodyFeature(method, transformedBody)
	}
}

fun Iterable<JTranscPlugin>.toGroup(injector: Injector) = JTranscPluginGroup(this).apply {
	initialize(injector)
	for (plugin in this@toGroup) plugin.initialize(injector)
}