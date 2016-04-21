import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstFieldRef
import com.jtransc.ast.AstType
import com.jtransc.ast.fqname
import com.jtransc.ast.optimize.optimize
import com.jtransc.types.dump
import com.jtransc.types.exprDump
import org.junit.Assert
import org.junit.Test

class OptimizeTest {
	@Test fun test1() {
		val expr = AstExpr.CAST(AstExpr.CAST(AstExpr.CAST(AstExpr.LITERAL(null), AstType.OBJECT), AstType.CLASS), AstType.STRING)
		Assert.assertEquals("((java.lang.String)((java.lang.Class)((java.lang.Object)null)))", expr.exprDump())
		expr.optimize()
		Assert.assertEquals("((java.lang.String)null)", expr.exprDump())
	}
	@Test fun test2() {
		Assert.assertEquals("1", AstExpr.CAST(AstExpr.LITERAL(1), AstType.INT).optimize().exprDump())
		Assert.assertEquals("(Class1.array).length", AstExpr.ARRAY_LENGTH(
			AstExpr.STATIC_FIELD_ACCESS(AstFieldRef("Class1".fqname, "array", AstType.ARRAY(AstType.INT)))
		).optimize().exprDump())
	}
}