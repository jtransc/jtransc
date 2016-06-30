import com.jtransc.KotlinVersion
import com.jtransc.maven.GradleLocalRepository
import com.jtransc.maven.MavenGradleLocalRepository
import org.junit.Assert
import org.junit.Test

class LocateKotlinTest {
	@Test
	fun name() {
		Assert.assertEquals(1, GradleLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-runtime:$KotlinVersion").size)
		Assert.assertEquals(true, MavenGradleLocalRepository.locateJars("org.jetbrains.kotlin:kotlin-runtime:$KotlinVersion").size >= 1)
	}
}