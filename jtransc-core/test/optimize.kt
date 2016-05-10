import com.jtransc.ast.*
import com.jtransc.ast.optimize.optimize
import com.jtransc.types.dump
import com.jtransc.types.exprDump
import org.junit.Assert
import org.junit.Test

class OptimizeTest {
	@Test fun test1() {
		val expr = AstBuild { null.lit.cast(OBJECT).cast(CLASS).cast(STRING) }
		Assert.assertEquals("((java.lang.String)((java.lang.Class)((java.lang.Object)null)))", expr.exprDump())
		expr.optimize()
		Assert.assertEquals("((java.lang.String)null)", expr.exprDump())
	}

	@Test fun test2() {
		Assert.assertEquals("1", AstBuild { 1.lit.cast(INT) }.optimize().exprDump())
		Assert.assertEquals("(Class1.array).length", AstExpr.ARRAY_LENGTH(
			AstExpr.STATIC_FIELD_ACCESS(AstFieldRef("Class1".fqname, "array", AstType.ARRAY(AstType.INT)))
		).optimize().exprDump())
	}

	@Test fun test3() {
		Assert.assertEquals("test", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.EQ, AstExpr.LITERAL(1)).optimize().exprDump())
		Assert.assertEquals("test", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.NE, AstExpr.LITERAL(0)).optimize().exprDump())

		Assert.assertEquals("(!test)", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.EQ, AstExpr.LITERAL(0)).optimize().exprDump())
		Assert.assertEquals("(!test)", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LOCAL(AstLocal(0, "test", AstType.BOOL)), AstType.INT), AstBinop.NE, AstExpr.LITERAL(1)).optimize().exprDump())
	}

	@Test fun test4() {
		Assert.assertEquals("true", AstExpr.BINOP(AstType.BOOL, AstExpr.CAST(AstExpr.LITERAL(true), AstType.INT), AstBinop.EQ, AstExpr.LITERAL(1)).optimize().exprDump())
		Assert.assertEquals("false", AstBuild { 0.lit.cast(BOOL) }.optimize().exprDump())
		Assert.assertEquals("true", AstBuild { 1.lit.cast(BOOL) }.optimize().exprDump())
	}

	@Test fun test5() {
		val test = AstExpr.LOCAL(AstLocal(0, "test", AstType.INT))
		Assert.assertEquals("(test != 10)", AstBuild { test ne 10.lit }.optimize().exprDump())
		Assert.assertEquals("(test == 10)", AstBuild { (test ne 10.lit).not() }.optimize().exprDump())
		Assert.assertEquals("(test != 10)", AstBuild { (test ne 10.lit).not().not() }.optimize().exprDump())
	}
}