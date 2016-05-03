import com.jtransc.json.Json
import org.junit.Assert
import org.junit.Test

class JsonTest {
	@Test
	fun testName() {
		val result = Json.decode("""{"a":1}""")
		Assert.assertEquals("""{a=1.0}""", result.map.toString())

		Assert.assertEquals(Demo(10), Json.decodeTo<Demo>("""{"a":10}"""))
	}
	data class Demo(val a:Int)
}