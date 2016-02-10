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

import com.jtransc.AllBuild
import com.jtransc.ast.AstBuildSettings
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File
import java.util.*

@Mojo(name = "jtransc", defaultPhase = LifecyclePhase.PACKAGE) class JTranscMojo : AbstractMojo() {
	@Component @JvmField var project: MavenProject? = null

	@Parameter(property = "target", defaultValue = "as3") @JvmField var target: String = "js"
	@Parameter(property = "mainClass") @JvmField var mainClass: String = ""
	@Parameter(property = "output", defaultValue = "program.js") @JvmField var output: String = "program.js"
	@Parameter(property = "release", defaultValue = "false") @JvmField var release: Boolean = false
	@Parameter(property = "library", defaultValue = "") @JvmField var libraries: Array<String> = arrayOf()
	@Parameter(property = "version", defaultValue = "0.0.0") @JvmField var version: String = "0.0.0" // @TODO: Or better use project.version?
	@Parameter(property = "title", defaultValue = "MyApp") @JvmField var title: String = "MyApp"
	@Parameter(property = "name", defaultValue = "MyApp") @JvmField var name: String = "MyApp"
	@Parameter(property = "company", defaultValue = "Company") @JvmField var company: String = "Company"
	@Parameter(property = "package", defaultValue = "com.example") @JvmField var package_: String = "com.example"
	@Parameter(property = "initialWidth", defaultValue = "1280") @JvmField var initialWidth: Int = 1280
	@Parameter(property = "initialHeight", defaultValue = "720") @JvmField var initialHeight: Int = 720
	@Parameter(property = "orientation", defaultValue = "auto") @JvmField var orientation: String = "auto"
	@Parameter(property = "borderless", defaultValue = "false") @JvmField var borderless: Boolean = false
	@Parameter(property = "fullscreen", defaultValue = "false") @JvmField var fullscreen: Boolean = false
	@Parameter(property = "resizable", defaultValue = "true") @JvmField var resizable: Boolean = true
	@Parameter(property = "vsync", defaultValue = "true") @JvmField var vsync: Boolean = true
	@Parameter(property = "icon", defaultValue = "") @JvmField var icon: String? = null

	// @TODO: Use <resources> instead?
	@Parameter(property = "assets") @JvmField var assets: Array<File> = arrayOf()

	@Throws(MojoExecutionException::class, MojoFailureException::class)
	override fun execute() {
		val log = log
		val project = project!!
		val build = project.build

		val targetParts = target.split(':')
		val targetActual = targetParts.getOrNull(0) ?: "js"
		val subtargetActual = targetParts.getOrNull(1) ?: ""
		val outputActual = targetParts.getOrNull(2) ?: output

		val settings = AstBuildSettings(
			title = title,
			version = version,
			assets = assets.map { it.absolutePath },
			company = company,
			package_ = package_,
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
			vsync = vsync
		)
		//project.version

		log.info("KT: Transcompiling entry point '$mainClass' to '$targetActual':'$subtargetActual' ('$target') at '$outputActual' with dependencies:")
		val dependencyJarPaths = ArrayList<String>()
		for (artifact in project.dependencyArtifacts) {
			val artifactPath = artifact.file.absolutePath
			dependencyJarPaths.add(artifactPath)
			log.info("    " + artifactPath)
		}
		val targetDirectory = project.build.directory
		val projectOutputDir = project.build.outputDirectory
		dependencyJarPaths.add(projectOutputDir)
		log.info("    " + projectOutputDir)

		val finalOutputDirectory = File(project.build.outputDirectory).parentFile

		log.info("Building... ")
		val beforeBuild = System.currentTimeMillis()
		val allBuild = AllBuild(targetActual, dependencyJarPaths, mainClass, File(finalOutputDirectory!!, outputActual).absolutePath, subtargetActual, targetDirectory)
		allBuild.buildWithoutRunning(settings)

		val afterBuild = System.currentTimeMillis()
		log.info("DONE building in " + (afterBuild - beforeBuild) + " ms")
	}
}
