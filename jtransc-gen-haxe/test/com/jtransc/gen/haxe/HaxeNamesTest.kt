package com.jtransc.gen.haxe

import com.jtransc.ast.*
import org.junit.Assert
import org.junit.Test

class HaxeNamesTest {
	val names = HaxeNames(object : AstResolver {
		override fun get(ref: AstMethodRef): AstMethod? = null
		override fun get(ref: AstFieldRef): AstField? = null
		override fun get(name: FqName): AstClass? = null
		override fun contains(name: FqName): Boolean = false
	})

	@Test
	fun testFqnames() {
		Assert.assertEquals("java_.lang.String_", names.getHaxeClassFqName(FqName("java.lang.String")))
		Assert.assertEquals("java_.uppercasePackage.LowercaseClass_", names.getHaxeClassFqName(FqName("java.UppercasePackage.lowercaseClass")))
		Assert.assertEquals("java_/uppercasePackage/LowercaseClass_.hx", names.getHaxeFilePath(FqName("java.UppercasePackage.lowercaseClass")))
	}

	inline private fun testFieldName(name:String, expected:String) = Assert.assertEquals(
		expected,
		names.getHaxeFieldName(AstFieldRef(FqName("com.test"), name, AstType.INT))
	)

	@Test
	fun testFieldName() {
		testFieldName("catch", "catch_");
		testFieldName("unix", "unix_"); // @TODO: Bug. HXCPP is not prefixing fields, so any #define will fail.
	}
}