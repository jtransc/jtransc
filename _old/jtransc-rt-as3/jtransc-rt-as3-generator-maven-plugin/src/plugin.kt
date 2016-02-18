import build.generateAirCoreSource
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.project.MavenProject

@Mojo(name = "build-rt-as3", defaultPhase = LifecyclePhase.INITIALIZE)
public class BuildAs3Mojo : AbstractMojo() {
	@Component
	lateinit private var project: MavenProject

	override fun execute() {
		val generatedPath = project.build.sourceDirectory
		log.info("build-rt-as3: generating to '$generatedPath'")
		generateAirCoreSource(generatedPath)
		log.info("build-rt-as3: completed!")
	}
}