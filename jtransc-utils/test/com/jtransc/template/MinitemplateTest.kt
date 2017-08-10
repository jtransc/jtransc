package com.jtransc.template

import com.jtransc.text.captureStdout
import com.jtransc.vfs.getResourceBytes
import org.junit.Assert
import org.junit.Test
import java.io.File

class MinitemplateTest {
	@Test fun testDummy() {
		Assert.assertEquals("hello", Minitemplate("hello")(null))
	}

	@Test fun testSimple() {
		Assert.assertEquals("hello soywiz", Minitemplate("hello {{ name }}")(mapOf("name" to "soywiz")))
		Assert.assertEquals("soywizsoywiz", Minitemplate("{{name}}{{ name }}")(mapOf("name" to "soywiz")))
	}

	@Test fun testFor() {
		Assert.assertEquals("123", Minitemplate("{% for n in numbers %}{{ n }}{% end %}")(mapOf("numbers" to listOf(1, 2, 3))))
	}

	@Test fun testDebug() {
		var result: String? = null
		val stdout = captureStdout {
			result = Minitemplate("a {% debug 'hello ' + name %} b")(mapOf("name" to "world"))
		}
		Assert.assertEquals("hello world", stdout.trim())
		Assert.assertEquals("a  b", result)
	}

	@Test fun testSimpleIf() {
		Assert.assertEquals("true", Minitemplate("{% if cond %}true{% else %}false{% end %}")(mapOf("cond" to 1)))
		Assert.assertEquals("false", Minitemplate("{% if cond %}true{% else %}false{% end %}")(mapOf("cond" to 0)))
		Assert.assertEquals("false", Minitemplate("{% if cond %}true{% else %}false{% end %}")(null))
		Assert.assertEquals("true", Minitemplate("{% if cond %}true{% end %}")(mapOf("cond" to 1)))
		Assert.assertEquals("", Minitemplate("{% if cond %}true{% end %}")(mapOf("cond" to 0)))
	}

	@Test fun testEval() {
		Assert.assertEquals("-5", Minitemplate("{{ -(1 + 4) }}")(null))
		Assert.assertEquals("false", Minitemplate("{{ 1 == 2 }}")(null))
		Assert.assertEquals("true", Minitemplate("{{ 1 < 2 }}")(null))
		Assert.assertEquals("true", Minitemplate("{{ 1 <= 1 }}")(null))
	}

	@Test fun testExists() {
		Assert.assertEquals("false", Minitemplate("{% if prop %}true{% else %}false{% end %}")(null))
		Assert.assertEquals("true", Minitemplate("{% if prop %}true{% else %}false{% end %}")(mapOf("prop" to "any")))
		Assert.assertEquals("false", Minitemplate("{% if prop %}true{% else %}false{% end %}")(mapOf("prop" to "")))
	}

	@Test fun testForAccess() {
		Assert.assertEquals("ZardBallesteros", Minitemplate("{% for n in persons %}{{ n.surname }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
		Assert.assertEquals("ZardBallesteros", Minitemplate("{% for n in persons %}{{ n['sur'+'name'] }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
		Assert.assertEquals("ZardBallesteros", Minitemplate("{% for nin in persons %}{{ nin['sur'+'name'] }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
	}

	@Test fun testFilters() {
		Assert.assertEquals("CARLOS", Minitemplate("{{ name|upper }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("carlos", Minitemplate("{{ name|lower }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("Carlos", Minitemplate("{{ name|capitalize }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("Carlos", Minitemplate("{{ (name)|capitalize }}")(mapOf("name" to "caRLos")))
		Assert.assertEquals("Carlos", Minitemplate("{{ 'caRLos'|capitalize }}")(null))
		Assert.assertEquals(" Carlos ", Minitemplate("{{ name }}")(mapOf("name" to " Carlos ")))
		Assert.assertEquals("Carlos", Minitemplate("{{ name|trim }}")(mapOf("name" to " Carlos ")))
	}

	@Test fun testArrayLiterals() {
		Assert.assertEquals("1234", Minitemplate("{% for n in [1, 2, 3, 4] %}{{ n }}{% end %}")(null))
		Assert.assertEquals("", Minitemplate("{% for n in [] %}{{ n }}{% end %}")(null))
		Assert.assertEquals("1, 2, 3, 4", Minitemplate("{{ [1, 2, 3, 4]|join(', ') }}")(null))
	}

	@Test fun testSet() {
		Assert.assertEquals("1,2,3", Minitemplate("{% set a = [1,2,3] %}{{ a|join(',') }}")(null))
	}

	@Test fun testIfElse() {
		Assert.assertEquals(" return true; ", Minitemplate("{% if extra.showFPS %} return {{ extra.showFPS }}; {% else %} return false; {% end %}").invoke(mapOf("extra" to mapOf("showFPS" to "true"))))
		Assert.assertEquals(" return true; ", Minitemplate("{% if extra.showFPS %} return {{ extra.showFPS }}; {% end %}{% if !extra.showFPS %} return false; {% end %}")(mapOf("extra" to mapOf("showFPS" to "true"))))
	}

	@Test fun testAccessGetter() {
		val success = "success!"

		class Test1 {
			val a: String get() = "$success"
		}

		Assert.assertEquals("$success", Minitemplate("{{ test.a }}")(mapOf("test" to Test1())))
	}

	@Test fun testCustomTag() {
		class CustomNode(val text: String) : Minitemplate.BlockNode {
			override fun eval(context: Minitemplate.Context) = Unit.apply { context.write("CUSTOM($text)") }
		}

		val CustomTag = Minitemplate.Tag("custom", setOf(), null) {
			CustomNode(it.first().token.content)
		}

		Assert.assertEquals(
			"CUSTOM(test)CUSTOM(demo)",
			Minitemplate("{% custom test %}{% custom demo %}", Minitemplate.Config(extraTags = listOf(CustomTag))).invoke(null)
		)
	}

	@Test fun testImageInfoFilter() {
		val resourceBytes = this.javaClass.classLoader.getResourceBytes("jtransc-icon.png")
		val tempFile = File.createTempFile("jtransc_image_info", "jtransc_image_info").apply {
			writeBytes(resourceBytes)
			deleteOnExit()
		}

		Assert.assertEquals("32,32,32",
			Minitemplate("{% set image = resourceBytes|image_info %}{{ image.width }},{{ image.height }},{{ image.bitsPerPixel }}")(mapOf(
				"resourceBytes" to resourceBytes
			))
		)
		Assert.assertEquals("32,32,32",
			Minitemplate("{% set image = resourcePath|image_info %}{{ image.width }},{{ image.height }},{{ image.bitsPerPixel }}")(mapOf(
				"resourcePath" to tempFile.absolutePath
			))
		)
		Assert.assertEquals("32,32,32",
			Minitemplate("{% set image = resourcePath|image_info %}{{ image.width }},{{ image.height }},{{ image.bitsPerPixel }}")(mapOf(
				"resourcePath" to tempFile
			))
		)
	}

	data class Person(val name: String, val surname: String)
}