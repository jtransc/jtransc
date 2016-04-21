import com.jtransc.ast.AstExpr
import com.jtransc.ast.AstType
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
}