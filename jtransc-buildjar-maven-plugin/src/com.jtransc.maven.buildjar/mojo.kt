package com.jtransc.maven.buildjar

import com.jtransc.tools.HaxeTools
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

//@Mojo(name = "jtransc-buildjar", defaultPhase = LifecyclePhase.COMPILE)
@Mojo(name = "jtransc-buildjar", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class JTranscBuildjarMojo : AbstractMojo() {
	@Component lateinit var project: MavenProject

	//@Parameter(property = "libName", defaultValue = "") @JvmField var libName: String = ""
	//@Parameter(property = "libVersion", defaultValue = "") @JvmField var libVersion: String = ""
	@Parameter(property = "libraries", defaultValue = "") @JvmField var libraries: Array<String> = arrayOf()
	@Parameter(property = "target", defaultValue = "") @JvmField var target: String = ""
	@Parameter(property = "includePackages", defaultValue = "") @JvmField var includePackages: Array<String> = arrayOf()
	@Parameter(property = "includePackagesRec", defaultValue = "") @JvmField var includePackagesRec: Array<String> = arrayOf()

	//@Parameter(defaultValue = "\${project.build.directory}", readonly = true)
	//@JvmField var projectBuildDirectory: String? = null

	override fun execute() {
		//generateClasses()
		generateSources()
	}

	private fun getLibraryInfo() = HaxeTools.LibraryInfo(
		libraries.toList(),
		includePackages.toList(),
		includePackagesRec.toList(),
		target
	)

	private fun generateSources() {
		val srcGenerated = project.build.directory + "/src-generated"
		println("JTranscBuildjarMojo: $srcGenerated")
		project.addCompileSourceRoot(srcGenerated)
		val files = HaxeTools.generateJavaSourcesFromHaxeLib(getLibraryInfo())
		for (file in files) {
			val classFile = File(srcGenerated + "/" + file.key)
			classFile.parentFile.mkdirs()
			classFile.writeBytes(file.value.toByteArray(Charsets.UTF_8))
		}
	}

	//private fun generateClasses() {
	//	val classesDirectory = project.build.directory + "/classes"
	//	//File(projectBuildDirectory).mkdirs()
	//	println("JTranscBuildjarMojo: ${project.build.directory}")
	//	val files = HaxeTools.generateClassesFromHaxeLib(getLibraryInfo())
	//	for (file in files) {
	//		val classFile = File(classesDirectory + "/" + file.key)
	//		classFile.parentFile.mkdirs()
	//		classFile.writeBytes(file.value)
	//	}
	//}
}