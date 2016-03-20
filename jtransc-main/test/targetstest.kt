import com.jtransc.AllBuildTargets
import com.jtransc.locateTargetByName
import com.jtransc.locateTargetByOutExt
import org.junit.Assert
import org.junit.Test

class TargetsTest {
	@Test
	fun testLocateSubtargetByExtensionName() {
		Assert.assertEquals("haxe:js(.js)", AllBuildTargets.locateTargetByOutExt("js").toString())
	}

	@Test
	fun testLocateTargetByName() {
		Assert.assertEquals("haxe:js(.js)", AllBuildTargets.locateTargetByName("haxe").toString())
		Assert.assertEquals("haxe:js(.js)", AllBuildTargets.locateTargetByName("haxe:js").toString())
		Assert.assertEquals("lime:html5(.js)", AllBuildTargets.locateTargetByName("lime").toString())
		Assert.assertEquals("lime:windows(.WINDOWS)", AllBuildTargets.locateTargetByName("lime:windows").toString())
		Assert.assertEquals("lime:html5(.HTML5)", AllBuildTargets.locateTargetByName("lime:html5").toString())
	}
}