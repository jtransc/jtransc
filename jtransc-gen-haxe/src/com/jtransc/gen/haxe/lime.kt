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

package com.jtransc.gen.haxe

import com.jtransc.ast.AstBuildSettings
import com.jtransc.ast.AstFeatures
import com.jtransc.ast.AstProgram
import com.jtransc.error.InvalidOperationException
import com.jtransc.gen.*
import com.jtransc.io.ProcessResult2
import com.jtransc.io.ProcessUtils
import com.jtransc.text.Indenter
import com.jtransc.vfs.LocalVfs
import jtransc.JTranscVersion
import java.io.File

object HaxeLimeGenDescriptor : GenTargetDescriptor() {
	override val name = "lime"
	override val longName = "haxe-lime"
	override val sourceExtension = "hx"
	override val outputExtension = "bin"

	override val subtargets = GenHaxeLime.LimeSubtarget.values().map {
		GenTargetSubDescriptor(HaxeLimeGenDescriptor, it.name.toLowerCase(), it.name)
	}

	override val defaultSubtarget = GenTargetSubDescriptor(HaxeLimeGenDescriptor, "html5", "js")

	override fun getGenerator() = GenHaxeLime
}

object GenHaxeLime : GenTarget {
	override val runningAvailable: Boolean = true

	val GenTargetInfo.mergedAssetsFolder: File get() = File("${this.targetDirectory}/merged-assets")

	fun createLimeProjectFromSettings(tinfo: GenTargetInfo, program: AstProgram, info: GenHaxe.ProgramInfo, settings: AstBuildSettings) = Indenter.gen {
		val tempAssetsDir = tinfo.mergedAssetsFolder
		val tempAssetsVfs = LocalVfs(tempAssetsDir)

		line("""<?xml version="1.0" encoding="utf-8"?>""")
		line("""<project>""")
		indent {
			line("""<meta title="${settings.title}" package="${settings.package_}" version="${settings.version}" company="${settings.company}" />""")
			//line("""<app main="${info.getEntryPointFq(program)}" path="out" file="${settings.name}" />""")
			line("""<app main="${info.entryPointClass}_" path="out" file="${settings.name}" />""")
			line("""<app swf-version="11.8" />""")

			line("""<window width="${settings.initialWidth}" height="${settings.initialHeight}" background="#FFFFFF" />""")
			line("""<window fullscreen="false" resizable="true" borderless="false" vsync="false" hardware="true" allow-shaders="true" require-shaders="true" depth-buffer="false" stencil-buffer="true" orientation="${settings.orientation.lowName}" />""")
			line("""<window fullscreen="true" if="mobile" />""")
			line("""<window fps="60" unless="js" />""")
			line("""<window fps="0" if="js" />""")

			for (flag in program.haxeExtraFlags) {
				line("""<haxeflag name="${flag.first}" value="${flag.second}" />""")
			}

			for (define in program.haxeExtraDefines) {
				line("""<haxedef name="$define" />""")
			}

			line("""<source path="src" />""")
			line("""<assets path="${tempAssetsDir.absolutePath}" rename="assets" embed="${settings.embedResources}" exclude="*.ttf|*.fla|*.zip|*.swf" />""")

			for (asset in settings.assets) {
				LocalVfs(asset).copyTreeTo(tempAssetsVfs)
			}
			if (!settings.icon.isNullOrEmpty()) {
				line("""<icon path="${settings.icon}" />""")
			}
			line("""<haxelib name="lime" />""")
			for (lib in settings.libraries) {
				if (lib.version != null) {
					line("""<haxelib name="${lib.name}" version="${lib.version}" />""")
				} else {
					line("""<haxelib name="${lib.name}" />""")
				}
			}
			//line("""<assets path="assets" rename="assets" />""")
		}
		line("""</project>""")
	}.toString()

	fun createAdobeAirDescriptor(name: String): String {
		return Indenter.gen {
			line("""<?xml version="1.0" encoding="utf-8" ?>""")
			line("""<application xmlns="http://ns.adobe.com/air/application/18.0">""")
			indent {
				line("""<id>program</id>""")
				line("""<version>0.0</version>""")
				line("""<versionNumber>0</versionNumber>""")
				line("""<filename>$name</filename>""")
				line("""<initialWindow>""")
				indent {
					line("""<content>$name.swf</content>""")
					line("""<visible>true</visible>""")
					line("""<aspectRatio>landscape</aspectRatio>""")
					line("""<autoOrients>true</autoOrients>""")
					line("""<fullScreen>true</fullScreen>""")
					line("""<renderMode>direct</renderMode>""")
					line("""<depthAndStencil>true</depthAndStencil>""")
				}
				line("""</initialWindow>""")
			}
			line("""</application>""")
			//<renderMode>direct</renderMode>
		}.toString()
	}

	enum class LimeSubtarget(val type: String) {
		ANDROID("android"),
		BLACKBERRY("blackberry"),
		DESKTOP("desktop"),
		EMSCRIPTEN("emscripten"),
		FLASH("flash"),
		HTML5("html5"),
		IOS("ios"),
		LINUX("linux"),
		MAC("mac"),
		TIZEN("tizen"),
		TVOS("tvos"),
		WEBOS("webos"),
		WINDOWS("windows"),
		NEKO("neko")
		;

		companion object {
			fun fromString(name: String) = when (name.toLowerCase()) {
				"android" -> ANDROID
				"blackberry" -> BLACKBERRY
				"desktop" -> DESKTOP
				"emscripten" -> EMSCRIPTEN
				"flash", "swf", "as3" -> FLASH
				"js", "html5" -> HTML5
				"ios" -> IOS
				"linux" -> LINUX
				"mac" -> MAC
				"tizen" -> TIZEN
				"tvos" -> TVOS
				"webos" -> WEBOS
				"windows" -> WINDOWS
				"neko" -> NEKO
				else -> throw InvalidOperationException("Unknown lime subtarget '$name'")
			}
		}
	}

	override fun getProcessor(tinfo: GenTargetInfo): GenTargetProcessor {
		val actualSubtarget = LimeSubtarget.fromString(tinfo.subtarget)

		val outputFile2 = File(File(tinfo.outputFile).absolutePath)
		//val tempdir = System.getProperty("java.io.tmpdir")
		val tempdir = tinfo.targetDirectory
		var info: GenHaxe.ProgramInfo? = null
		val projectDir = LocalVfs(File("$tempdir/jtransc-haxe/"))
		val program = tinfo.program

		File("$tempdir/jtransc-haxe/src").mkdirs()
		val srcFolder = projectDir["src"]

		println("Temporal haxe files: $tempdir/jtransc-haxe")


		return object : GenTargetProcessor {
			override fun buildSource() {
				info = GenHaxeGen(
					program = tinfo.program,
					features = AstFeatures(),
					srcFolder = srcFolder,
					featureSet = HaxeFeatures
				)._write()
				projectDir["program.xml"] = createLimeProjectFromSettings(tinfo, tinfo.program, info!!, tinfo.settings)
			}

			override fun compile(): Boolean {
				if (info == null) throw InvalidOperationException("Must call .buildSource first")
				outputFile2.delete()
				println("lime.build (" + JTranscVersion.getVersion() + ") source path: " + srcFolder.realpathOS)

				program.haxeInstallRequiredLibs()

				tinfo.haxeCopyEmbeddedResourcesToFolder(tinfo.mergedAssetsFolder)

				val switches = if (tinfo.settings.release) listOf() else listOf("-debug")

				println("Compiling...")
				val args = listOf("run", "lime") + switches + listOf("build", actualSubtarget.type)
				println("haxelib " + args.joinToString(" "))
				return ProcessUtils.runAndRedirect(projectDir.realfile, "haxelib", args).success
			}

			override fun run(redirect: Boolean): ProcessResult2 {
				if (!outputFile2.exists()) {
					return ProcessResult2("file $outputFile2 doesn't exist", -1)
				}
				println("run: ${outputFile2.absolutePath}")
				val parentDir = outputFile2.parentFile

				//return ProcessUtils.run(parentDir, runner, listOf(outputFile2.absolutePath), redirect = redirect)
				return ProcessResult2("not run", 0)
			}
		}
	}
}
