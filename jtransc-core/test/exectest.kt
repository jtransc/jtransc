import org.junit.Assert
import org.junit.Test
import javax.script.ScriptEngineManager

class ExecTest {
	val engine = ScriptEngineManager().getEngineByMimeType("text/javascript")
	@Test
	fun testName() {
		Assert.assertEquals(10, engine.eval("(function() { return 10; })()"));
	}

	@Test
	fun testName2() {
		Assert.assertEquals(10, engine.eval("(function() { return 10; })()"));
	}
}