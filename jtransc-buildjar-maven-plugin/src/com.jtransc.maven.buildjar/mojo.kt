import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

@Mojo(name = "jtransc-buildjar", defaultPhase = LifecyclePhase.COMPILE)
class JTranscBuildjarMojo : AbstractMojo() {
	//@Parameter(property = "libName", defaultValue = "") @JvmField var libName: String = ""
	//@Parameter(property = "libVersion", defaultValue = "") @JvmField var libVersion: String = ""
	@Parameter(property = "libraries", defaultValue = "") @JvmField var libraries: Array<String> = arrayOf()
	@Parameter(property = "target", defaultValue = "") @JvmField var target: String = ""
	@Parameter(property = "includePackages", defaultValue = "") @JvmField var includePackages: Array<String> = arrayOf()
	@Parameter(property = "includePackagesRec", defaultValue = "") @JvmField var includePackagesRec: Array<String> = arrayOf()

	@Parameter(defaultValue = "\${project.build.directory}", readonly = true)
	@JvmField var projectBuildDirectory: String? = null

	override fun execute() {
		val classesDirectory = projectBuildDirectory + "/classes"
		//File(projectBuildDirectory).mkdirs()
		println("JTranscBuildjarMojo: $projectBuildDirectory")
		val files = HaxeTools.generateClassesFromHaxeLib(
			libraries.toList(),
			includePackages.toList(),
			includePackagesRec.toList(),
			target
		)
		for (file in files) {
			val classFile = File(classesDirectory + "/" + file.key)
			classFile.parentFile.mkdirs()
			classFile.writeBytes(file.value)
		}
	}
}