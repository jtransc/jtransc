package com.jtransc.gen.haxe

import com.jtransc.ast.*
import org.junit.Assert
import org.junit.Test

class HaxeNamesTest {
	val names = HaxeNames(object : AstResolver {
		override fun get(ref: AstMethodRef): AstMethod? = null
		override fun get(ref: AstFieldRef): AstField? = null
		override fun get(name: FqName): AstClass? = null
	})

	@Test
	fun testFqnames() {
		Assert.assertEquals("java_.lang.String_", names.getHaxeClassFqName(FqName("java.lang.String")))
		Assert.assertEquals("java_.uppercasePackage.LowercaseClass_", names.getHaxeClassFqName(FqName("java.UppercasePackage.lowercaseClass")))
		Assert.assertEquals("java_/uppercasePackage/LowercaseClass_.hx", names.getHaxeFilePath(FqName("java.UppercasePackage.lowercaseClass")))
	}

	@Test
	fun testFieldName() {
		Assert.assertEquals(
			"catch_",
			names.getHaxeFieldName(AstFieldRef(FqName("com.test"), "catch", AstType.INT))
		)
	}
}