package com.jtransc.plugin.junit

import com.jtransc.ast.*
import com.jtransc.plugin.JTranscPlugin
import com.jtransc.plugin.reflection.createClass
import com.jtransc.plugin.reflection.createMethod
import com.sun.org.apache.bcel.internal.generic.NEW

class JUnitJTranscPlugin : JTranscPlugin() {
	fun createEntry(program: AstProgram, classesToTest: Iterable<FqName>) {
		val JunitEntryPointClass = program.createClass("JunitEntryPoint".fqname) {
			createMethod(
				name = "main",
				desc = AstType.METHOD(AstType.VOID, listOf(AstType.ARRAY(AstType.STRING))),
				isStatic = true
			) {
				val coreLocal = AstLocal(0, AstType.REF("org.junit.runner.JUnitCore".fqname))
				SET(coreLocal, AstExpr.NEW_WITH_CONSTRUCTOR(AstMethodRef("org.junit.runner.JUnitCore".fqname, "<init>", AstType.METHOD(AstType.VOID, listOf())), listOf()))
				//STM(AstExpr.CALL_INSTANCE(coreLocal.expr, AstMethodRef))
			}
		}
	}
}

/*
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

public class Demo {
    static public void main(String[] args) {
        JUnitCore core = new JUnitCore();
        core.addListener(new TextListener(System.out));
        core.run(Test1.class);
    }
}

public class Test1 {
    @Test
    @JTranscKeep // @Test should work like @JTranscKeep
    public void test1() {
        Assert.assertEquals(1, 0);
    }
}
*/