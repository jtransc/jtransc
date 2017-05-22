package com.jtransc.ast.optimize

import com.jtransc.ast.*
import org.junit.Assert
import org.junit.Test

class OptimizeTests {
	val types = AstTypes()
	val flags = AstBodyFlags(strictfp = false, types = types)

	fun <T> build(callback: AstBuilder2.() -> T): T = types.build2 { callback() }

	@Test fun test1() {
		val expr = build { null.lit.castToUnoptimized(OBJECT).castToUnoptimized(CLASS).castToUnoptimized(STRING) }
		Assert.assertEquals("((java.lang.String)((java.lang.Class)((java.lang.Object)null)))", expr.exprDump(types))
		val result = expr.optimize(flags)
		Assert.assertEquals("((java.lang.String)null)", result.exprDump(types))
	}

	@Test fun test2() {
		Assert.assertEquals("1", build { 1.lit.castTo(INT) }.optimize(flags).exprDump(types))
		Assert.assertEquals("(Class1.array).length", AstExpr.ARRAY_LENGTH(
			AstExpr.FIELD_STATIC_ACCESS(AstFieldRef("Class1".fqname, "array", AstType.ARRAY(AstType.INT)))
		).optimize(flags).exprDump(types))
	}

	@Test fun test3() {
		Assert.assertEquals("test", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT, dummy = true), AstBinop.EQ, 1.lit).optimize(flags).exprDump(types))
		Assert.assertEquals("test", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT, dummy = true), AstBinop.NE, 0.lit).optimize(flags).exprDump(types))

		Assert.assertEquals("(!test)", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT, dummy = true), AstBinop.EQ, 0.lit).optimize(flags).exprDump(types))
		Assert.assertEquals("(!test)", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT, dummy = true), AstBinop.NE, 1.lit).optimize(flags).exprDump(types))
	}

	@Test fun test4() {
		Assert.assertEquals("true", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(true.lit, AstType.INT, dummy = true), AstBinop.EQ, 1.lit).optimize(flags).exprDump(types))
		Assert.assertEquals("false", build { 0.lit.castTo(BOOL) }.optimize(flags).exprDump(types))
		Assert.assertEquals("true", build { 1.lit.castTo(BOOL) }.optimize(flags).exprDump(types))
	}

	@Test fun test5() {
		val test = AstExpr.LOCAL(AstLocal(0, "test", AstType.INT))
		Assert.assertEquals("(test != 10)", build { test ne 10.lit }.optimize(flags).exprDump(types))
		Assert.assertEquals("(test == 10)", build { (test ne 10.lit).not() }.optimize(flags).exprDump(types))
		Assert.assertEquals("(test != 10)", build { (test ne 10.lit).not().not() }.optimize(flags).exprDump(types))
	}

	@Test fun test7() {
		val stms = types.buildStms {
			val obj by LOCAL(AstType.OBJECT)
			val StringBuilder = AstType.REF("java.lang.StringBuilder")
			val StringBuilderAppend = StringBuilder.method("append", AstType.METHOD(StringBuilder, listOf(AstType.STRING)))
			SET(obj, StringBuilder.constructor().newInstance())
			STM(obj[StringBuilderAppend]("hello".lit))
			STM(obj[StringBuilderAppend]("hello".lit))
			STM(obj[StringBuilderAppend]("hello".lit))
		}
		val stm2 = stms.stm().optimize(AstBodyFlags(types))
		println(stms.dump(types))
		println(stm2.dump(types))
	}
}