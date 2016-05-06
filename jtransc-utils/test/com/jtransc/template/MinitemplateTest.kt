package com.jtransc.template

import org.junit.Assert
import org.junit.Test

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

	@Test fun testForAccess() {
		Assert.assertEquals("ZardBallesteros", Minitemplate("{% for n in persons %}{{ n.surname }}{% end %}")(mapOf("persons" to listOf(Person("Soywiz", "Zard"), Person("Carlos", "Ballesteros")))))
	}

	data class Person(val name:String, val surname:String)
}