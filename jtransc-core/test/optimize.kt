import com.jtransc.ast.*
import com.jtransc.ast.optimize.optimize
import com.jtransc.ast.exprDump
import org.junit.Assert
import org.junit.Test

class OptimizeTest {
	val types = AstTypes()
	val flags = AstBodyFlags(strictfp = false, types = types)

	fun <T> build(callback: AstBuilder2.() -> T): T = types.build2 { callback() }

	@Test fun test1() {
		val expr = build { null.lit.castTo(OBJECT).castTo(CLASS).castTo(STRING) }
		Assert.assertEquals("((java.lang.String)((java.lang.Class)((java.lang.Object)null)))", expr.exprDump(types))
		expr.optimize(flags)
		Assert.assertEquals("((java.lang.String)null)", expr.exprDump(types))
	}

	@Test fun test2() {
		Assert.assertEquals("1", build { 1.lit.castTo(INT) }.optimize(flags).exprDump(types))
		Assert.assertEquals("(Class1.array).length", AstExpr.ARRAY_LENGTH(
			AstExpr.FIELD_STATIC_ACCESS(AstFieldRef("Class1".fqname, "array", AstType.ARRAY(AstType.INT)))
		).optimize(flags).exprDump(types))
	}

	@Test fun test3() {
		Assert.assertEquals("test", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.EQ, AstExpr.LITERAL(1)).optimize(flags).exprDump(types))
		Assert.assertEquals("test", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.NE, AstExpr.LITERAL(0)).optimize(flags).exprDump(types))

		Assert.assertEquals("(!test)", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.EQ, AstExpr.LITERAL(0)).optimize(flags).exprDump(types))
		Assert.assertEquals("(!test)", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.NE, AstExpr.LITERAL(1)).optimize(flags).exprDump(types))
	}

	@Test fun test4() {
		Assert.assertEquals("true", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LITERAL(true), AstType.INT), AstBinop.EQ, AstExpr.LITERAL(1)).optimize(flags).exprDump(types))
		Assert.assertEquals("false", build { 0.lit.castTo(BOOL) }.optimize(flags).exprDump(types))
		Assert.assertEquals("true", build { 1.lit.castTo(BOOL) }.optimize(flags).exprDump(types))
	}

	@Test fun test5() {
		val test = AstExpr.LOCAL(AstLocal(0, "test", AstType.INT))
		Assert.assertEquals("(test != 10)", build { test ne 10.lit }.optimize(flags).exprDump(types))
		Assert.assertEquals("(test == 10)", build { (test ne 10.lit).not() }.optimize(flags).exprDump(types))
		Assert.assertEquals("(test != 10)", build { (test ne 10.lit).not().not() }.optimize(flags).exprDump(types))
	}
}