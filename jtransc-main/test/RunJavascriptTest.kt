import org.junit.Assert
import org.junit.Test
import javax.script.ScriptEngineManager

class RunJavascriptTest {
	val engine = ScriptEngineManager().getEngineByMimeType("text/javascript")
	
	@Test
	fun testExecJsTest1() {
		Assert.assertEquals(10, engine.eval("(function() { return 10; })()"));
	}

	@Test
	fun testExecJsTest2() {
		Assert.assertEquals(10, engine.eval("(function() { return 10; })()"));
	}
}