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
import com.jtransc.JTranscVersion
import com.jtransc.template.Minitemplate
import com.jtransc.vfs.parent
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

	val TEMPLATE = """<?xml version="1.0" encoding="utf-8"?>
<project>
	<meta title="{{ title }}" package="{{ package }}" version="{{ version }}" company="{{ company }}" />
	<app main="{{ entryPointClass }}" path="out" file="{{ name }}" />
	<app swf-version="11.8" />

	<window width="{{ initialWidth }}" height="{{ initialHeight }}" background="#FFFFFF" />
	<window fullscreen="false" resizable="true" borderless="false" vsync="false" hardware="true" allow-shaders="true" require-shaders="true" depth-buffer="false" stencil-buffer="true" orientation="{{ orientation }}" highdpi="true" allow-high-dpi="true" />
	<window fullscreen="true" if="mobile" />
	<window fps="60" unless="js" />
	<window fps="0" if="js" />
	<window width="0" height="0" if="html5" />

	{% for flag in haxeExtraFlags %}
		<haxeflag name="{{ flag.first }}" value="{{ flag.second }}" />
	{% end %}

	{% for define in haxeExtraDefines %}
		<haxedef name="{{ define }}" />
	{% end %}

	<source path="src" />
	<assets path="{{ tempAssetsDir.absolutePath }}" rename="assets" embed="{{ embedResources }}" exclude="*.ttf|*.fla|*.zip|*.swf" />

	{% for asset in assets %}
		LocalVfs(asset).copyTreeTo(tempAssetsVfs)
	{% end %}

	{% if hasIcon %}
		<icon path="{{ settings.icon }}" />
	{% end %}

	<haxelib name="lime" />
	{% for lib in libraries %}
		{% if version != null %}
			<haxelib name="{{ lib.name }}" version="{{ lib.version }}" />
		{% else %}
			<haxelib name="{{ lib.name }}" />
		{% end %}
	{% end %}
</project>
	"""

	fun createLimeProjectFromSettings(tinfo: GenTargetInfo, program: AstProgram, info: GenHaxe.ProgramInfo, haxegen:GenHaxeGen, settings: AstBuildSettings): String {
		val tempAssetsDir = tinfo.mergedAssetsFolder
		val tempAssetsVfs = LocalVfs(tempAssetsDir)
		val names = haxegen.names

		for (asset in settings.assets) {
			LocalVfs(asset).copyTreeTo(tempAssetsVfs)
		}

		return Minitemplate(TEMPLATE)(mapOf(
			"settings" to settings,
			"title" to settings.title,
			"name" to settings.name,
			"package" to settings.package_,
			"version" to settings.version,
			"company" to settings.company,
			"initialWidth" to settings.initialWidth,
			"initialHeight" to settings.initialHeight,
			"orientation" to settings.orientation.lowName,
			"entryPointClass" to names.getHaxeClassFqName(info.entryPointClass),
			"haxeExtraFlags" to program.haxeExtraFlags(settings),
			"haxeExtraDefines" to program.haxeExtraDefines(settings),
			"tempAssetsDir" to tempAssetsDir,
			"embedResources" to settings.embedResources,
			"assets" to settings.assets,
			"hasIcon" to !settings.icon.isNullOrEmpty(),
			"icon" to settings.icon,
			"libraries" to settings.libraries
		))
	}

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

	override fun getProcessor(tinfo: GenTargetInfo, settings: AstBuildSettings): GenTargetProcessor {
		val actualSubtarget = LimeSubtarget.fromString(tinfo.subtarget)

		val outputFile2 = File(File(tinfo.outputFile).absolutePath)
		//val tempdir = System.getProperty("java.io.tmpdir")
		val tempdir = tinfo.targetDirectory
		var info: GenHaxe.ProgramInfo? = null
		val srcFolder = HaxeGenTools.getSrcFolder(tempdir)
		val projectDir = srcFolder.parent
		val program = tinfo.program

		return object : GenTargetProcessor {
			override fun buildSource() {
				val haxegen = GenHaxeGen(
					program = tinfo.program,
					features = AstFeatures(),
					srcFolder = srcFolder,
					featureSet = HaxeFeatures,
					settings = settings
				)

				info = haxegen._write()
				projectDir["program.xml"] = createLimeProjectFromSettings(tinfo, tinfo.program, info!!, haxegen, tinfo.settings)
			}

			val BUILD_COMMAND = listOf("haxelib", "run", "lime", "@@SWITCHES", "build", "@@SUBTARGET")

			override fun compile(): Boolean {
				if (info == null) throw InvalidOperationException("Must call .buildSource first")
				outputFile2.delete()
				println("lime.build (" + JTranscVersion.getVersion() + ") source path: " + srcFolder.realpathOS)

				program.haxeInstallRequiredLibs(settings)

				tinfo.haxeCopyEmbeddedResourcesToFolder(tinfo.mergedAssetsFolder)

				val switches = if (tinfo.settings.release) listOf() else listOf("-debug")

				println("Compiling...")

				val CMD = BUILD_COMMAND.flatMap { when (it) {
					"@@SWITCHES" -> switches
					"@@SUBTARGET" -> listOf(actualSubtarget.type)
					else -> listOf(it)
				}  }

				println(CMD.joinToString(" "))

				return ProcessUtils.runAndRedirect(projectDir.realfile, CMD.first(), CMD.drop(1)).success
			}

			override fun run(redirect: Boolean): ProcessResult2 {
				if (!outputFile2.exists()) {
					return ProcessResult2(-1, "file $outputFile2 doesn't exist")
				}
				println("run: ${outputFile2.absolutePath}")
				val parentDir = outputFile2.parentFile

				//return ProcessUtils.run(parentDir, runner, listOf(outputFile2.absolutePath), redirect = redirect)
				return ProcessResult2(0, "not run")
			}
		}
	}
}
