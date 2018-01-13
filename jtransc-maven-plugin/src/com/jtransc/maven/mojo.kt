/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jtransc.maven

import com.jtransc.*
import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstTypes
import com.jtransc.ast.ConfigMinimizeNames
import com.jtransc.ast.ConfigTreeShaking
import com.jtransc.injector.Injector
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.*
import org.apache.maven.project.MavenProject
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.util.artifact.JavaScopes
import java.io.File
import java.util.*

@Mojo(name = "jtransc", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Suppress("UNUSED")
class JTranscMojo : AbstractMojo() {
	@Component @JvmField var project: MavenProject? = null
	@Component @JvmField var session: MavenSession? = null
	@Component @JvmField var repoSystem: RepositorySystem? = null
	@Parameter(defaultValue = "\${repositorySystemSession}", readonly = true)
	@JvmField var repoSession: RepositorySystemSession? = null;
	@Parameter(defaultValue = "\${project.remotePluginRepositories}", readonly = true)
	@JvmField var remoteRepos: List<RemoteRepository>? = null

	@Parameter(property = "target", defaultValue = "") @JvmField var target: String = ""
	@Parameter(property = "targets", defaultValue = "") @JvmField var targets: Array<String> = arrayOf()
	@Parameter(property = "mainClass") @JvmField var mainClass: String = ""
	@Parameter(property = "output", defaultValue = "program.js") @JvmField var output: String = "program.js"
	@Parameter(property = "release", defaultValue = "false") @JvmField var release: Boolean = false
	@Parameter(property = "library", defaultValue = "") @JvmField var libraries: Array<String> = arrayOf()
	@Parameter(property = "version", defaultValue = "") @JvmField var version: String = "" // @TODO: Or better use project.version?
	@Parameter(property = "title", defaultValue = "MyApp") @JvmField var title: String = "MyApp"
	@Parameter(property = "name", defaultValue = "MyApp") @JvmField var name: String = "MyApp"
	@Parameter(property = "company", defaultValue = "Company") @JvmField var company: String = "Company"
	@Parameter(property = "packagePath", defaultValue = "") @JvmField var packagePath: String = ""
	@Parameter(property = "initialWidth", defaultValue = "1280") @JvmField var initialWidth: Int = 1280
	@Parameter(property = "initialHeight", defaultValue = "720") @JvmField var initialHeight: Int = 720
	@Parameter(property = "orientation", defaultValue = "auto") @JvmField var orientation: String = "auto"
	@Parameter(property = "borderless", defaultValue = "false") @JvmField var borderless: Boolean = false
	@Parameter(property = "fullscreen", defaultValue = "false") @JvmField var fullscreen: Boolean = false
	@Parameter(property = "resizable", defaultValue = "true") @JvmField var resizable: Boolean = true
	@Parameter(property = "vsync", defaultValue = "true") @JvmField var vsync: Boolean = true
	@Parameter(property = "icon", defaultValue = "") @JvmField var icon: String? = null
	@Parameter(property = "embedResources", defaultValue = "") @JvmField var embedResources: Boolean = false
	@Parameter(property = "backend", defaultValue = "ASM") @JvmField var backend: BuildBackend = BuildBackend.ASM
	@Parameter(property = "relooper", defaultValue = "false") @JvmField var relooper: Boolean = false
	@Parameter(property = "minimizeNames", defaultValue = "false") @JvmField var minimizeNames: Boolean = false
	@Parameter(property = "analyzer", defaultValue = "false") @JvmField var analyzer: Boolean = false
	@Parameter(property = "treeShaking", defaultValue = "false") @JvmField var treeShaking: Boolean = false
	@Parameter(property = "trace", defaultValue = "false") @JvmField var trace: Boolean = false
	@Parameter(property = "extra") @JvmField var extra = hashMapOf<String?, String?>()

	// @TODO: Use <resources> instead?
	@Parameter(property = "assets") @JvmField var assets: Array<File> = arrayOf()

	@Throws(MojoExecutionException::class, MojoFailureException::class)
	override fun execute() {
		val log = log
		val session = session!!
		val project = project!!
		val remoteRepos = remoteRepos!!
		val repoSystem = repoSystem!!
		val injector = Injector()

		val package_ = if (this.packagePath.isNullOrEmpty()) project.artifact.groupId else this.packagePath
		val version = if (this.version.isNullOrEmpty()) project.artifact.version else this.version

		log.info("KT: target: $target");
		log.info("KT: targets: ${targets.toList()}");

		val allTargets = targets.toList() + if (target != "") {
			listOf(target)
		} else {
			listOf()
		}

		log.info("KT: allTargets: $allTargets");

		//locator.addService(WagonProvider::class.java, MyWagonProvider::class.java)

		log.info("KT: Session.localRepository: ${session.localRepository?.basedir}");

		val jtranscVersion = project.pluginArtifacts.first { it.artifactId == "jtransc-maven-plugin" }.version

		log.info("KT: JTransc version : $jtranscVersion");

		for (artifact in BaseRuntimeArtifactsForVersion(jtranscVersion).toListString()) {
			val jtranscRuntimeArtifact = DefaultArtifact(artifact)

			log.info("KT: Resolving $jtranscRuntimeArtifact");

			fun remote(id: String, url: String) = RemoteRepository.Builder(id, "default", url).build()

			val allRemoteRepos = remoteRepos + listOf(
				remote("sonatype.oss.snapshots", "https://oss.sonatype.org/content/repositories/snapshots/"),
				remote("central.mirror", "https://uk.maven.org/maven2")
			)

			val result = repoSystem.resolveArtifact(
				repoSession,
				ArtifactRequest(jtranscRuntimeArtifact, allRemoteRepos, JavaScopes.COMPILE)
			)

			log.info("KT: Resolved: $result : ${result.artifact.file.absolutePath}");
		}

		val settings = AstBuildSettings(
			jtranscVersion = jtranscVersion,
			title = title,
			version = version,
			assets = assets.map { it.absoluteFile },
			company = company,
			package_ = package_,
			embedResources = embedResources,
			libraries = libraries.map { AstBuildSettings.Library.fromInfo(it) },
			debug = !release,
			initialWidth = initialWidth,
			initialHeight = initialHeight,
			borderless = borderless,
			fullscreen = fullscreen,
			icon = if (icon?.isNullOrEmpty() ?: true) null else icon,
			name = name,
			orientation = AstBuildSettings.Orientation.fromString(orientation),
			resizable = resizable,
			vsync = vsync,
			//backend = backend,
			relooper = relooper,
			//minimizeNames = minimizeNames,
			analyzer = analyzer,
			extra = extra
		)

		//project.version

		log.info("KT: Transcompiling entry point '$mainClass':")
		val dependencyJarPaths = ArrayList<String>()
		for (artifact in project.artifacts) {
			val artifactPath = artifact.file.absolutePath
			dependencyJarPaths.add(artifactPath)
			log.info("    " + artifactPath)
		}
		val targetDirectory = project.build.directory
		val projectOutputDir = project.build.outputDirectory
		dependencyJarPaths.add(projectOutputDir)
		log.info("    " + projectOutputDir)


		injector.mapInstance(ConfigMinimizeNames(minimizeNames))
		injector.mapInstances(ConfigClassPaths(dependencyJarPaths))
		injector.mapInstances(BuildBackend.ASM)
		injector.mapInstances(ConfigTreeShaking(treeShaking = treeShaking, trace = trace))


		val finalOutputDirectory = File(project.build.outputDirectory).parentFile

		log.info("Building targets... " + allTargets.joinToString(", "));
		//var errorCount = 0
		//val types = AstTypes()
		for (target in allTargets) {
			val targetParts = target.split(':')
			val targetActual = targetParts.getOrNull(0) ?: "js"
			val subtargetActual = targetParts.getOrNull(1) ?: ""
			val outputActual = targetParts.getOrNull(2) ?: output

			val beforeBuild = System.currentTimeMillis()
			log.info("Building... to '$targetActual':'$subtargetActual' ('$target') at '$outputActual' with dependencies:")
			//val allBuild: AllBuild = AllBuild(
			//	AllBuildTargets = AllBuildTargets,
			//	target = targetActual,
			//	classPaths = dependencyJarPaths,
			//	entryPoint = mainClass,
			//	output = File(finalOutputDirectory!!, outputActual).absolutePath,
			//	subtarget = subtargetActual,
			//	settings = settings,
			//	types = types,
			//	targetDirectory = targetDirectory
			//)
			val allBuildSimple = AllBuildSimple(
				injector = injector,
				entryPoint = mainClass,
				settings = settings,
				target = target,
				output = File(finalOutputDirectory!!, outputActual).absolutePath,
				targetDirectory = targetDirectory
			)
			val buildResult = allBuildSimple.buildWithoutRunning()
			val afterBuild = System.currentTimeMillis()
			if (buildResult.process.success) {
				log.info("DONE building in " + (afterBuild - beforeBuild) + " ms")
			} else {
				log.error("ERROR building in " + (afterBuild - beforeBuild) + " ms")
				//errorCount++
				throw RuntimeException("There were errors building using jtransc")
			}
		}
		//if (errorCount > 0) {
		//	throw RuntimeException("There were errors building using jtransc")
		//}
	}
}
