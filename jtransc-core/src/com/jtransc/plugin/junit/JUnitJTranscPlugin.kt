package com.jtransc.plugin.junit

import com.jtransc.ast.*
import com.jtransc.ast.treeshaking.TreeShakingApi
import com.jtransc.backend.asm2.TIR
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.plugin.reflection.createClass
import com.jtransc.plugin.reflection.createMethod
import kotlin.reflect.jvm.internal.impl.resolve.constants.ClassLiteralValue

class JUnitJTranscPlugin : JTranscPlugin() {
	val System_class = System::class.fqname
	val TestRunner_class = "junit.textui.TestRunner".fqname
	val TestResult_class = "junit.framework.TestResult".fqname
	val JUnit4TestAdapter_class = "junit.framework.JUnit4TestAdapter".fqname
	val JUnit4TestAdapter_constructor = AstTypeBuilder { JUnit4TestAdapter_class.ref.constructor(CLASS) }
	val TestResult_wasSuccessful = TestResult_class.methodRef("wasSuccessful") { METHOD(BOOL) }
	val Test_class = "junit.framework.Test".fqname
	val TestSuite_class = "junit.framework.TestSuite".fqname
	val TestSuite_addTest = TestSuite_class.methodRef("addTest") { METHOD(VOID, Test_class.ref) }
	val System_exit_method = System_class.methodRef("exit") { METHOD(VOID, INT) }
	val TestRunner_run_method = TestRunner_class.methodRef("run") { METHOD(TestResult_class.ref, Test_class.ref) }
	val TestSuite_String_constructor = TestSuite_class.ref.constructor(AstType { STRING })

	val allTestClasses = arrayListOf<FqName>()
	override fun onTreeShakingAddBasicClass(treeShaking: TreeShakingApi, fqname: FqName, oldclass: AstClass, newclass: AstClass) {
		var hasTest = false
		for (method in oldclass.methods) {
			if (method.annotationsList.contains("org.junit.Test".fqname)) {
				hasTest = true
				treeShaking.addMethod(method.ref)
			}
		}
		if (hasTest) {
			if (allTestClasses.isEmpty()) {
				treeShaking.addClassFull(TestRunner_class)
				treeShaking.addClassFull(TestSuite_class)
				treeShaking.addClassFull(TestResult_class)
				treeShaking.addClassFull(JUnit4TestAdapter_class)
				treeShaking.addMethod(System_exit_method)
				treeShaking.addMethod(TestResult_wasSuccessful)
				treeShaking.addMethod(TestRunner_run_method)
				treeShaking.addMethod(TestSuite_String_constructor)
				treeShaking.addMethod(TestSuite_addTest)
				treeShaking.addMethod(JUnit4TestAdapter_constructor)
			}
			allTestClasses += oldclass.name
		}
	}

	override fun processAfterTreeShaking(program: AstProgram) {
		val runningTests = injector.getOrNull<ConfigRunningTests>()?.tests ?: false
		if (runningTests) {
			val AllTests_fqname = "AllTests".fqname
			program.entrypoint = AllTests_fqname
			program.createClass(AllTests_fqname) {
				createMethod("main", AstTypeBuild { METHOD(VOID, ARRAY(STRING)) }, isStatic = true) {
					val suite = TestSuite_class.ref.local("suite")
					SET(suite, NEW(TestSuite_String_constructor, "suite".lit))
					for (testClass in allTestClasses) {
						STM(suite.invoke(TestSuite_addTest, NEW(JUnit4TestAdapter_constructor, testClass.ref.lit)))
					}
					IF(TestRunner_run_method(suite).invoke(TestResult_wasSuccessful)) {
						STM(System_exit_method(0.lit))
					} ELSE {
						STM(System_exit_method(1.lit))
					}
				}
			}
		}
	}
}