package com.jtransc.gen.haxe

import com.jtransc.ConfigTargetDirectory
import com.jtransc.ast.AstFieldRef
import com.jtransc.ast.AstType
import com.jtransc.ast.FqName
import com.jtransc.gen.common.genCommonGeneratorDouble
import com.jtransc.injector.Injector
import com.jtransc.plugin.reflection.createClass
import com.jtransc.plugin.reflection.createField
import org.junit.Assert
import org.junit.Test

class HaxeNamesTest {
	val generator = Injector().apply {
		//mapInstances(ConfigHaxeAddSubtarget(HaxeAddSubtarget("js")))
		mapInstance(HaxeConfigMergedAssetsFolder(ConfigTargetDirectory(".")))
	}.genCommonGeneratorDouble(HaxeGenerator::class.java, "haxe", "js").apply {
		program.createClass(FqName("com.test")) {
			createField("catch", AstType.INT)
			createField("unix", AstType.INT)
		}
	}

	@Test
	fun testFqnames() {
		Assert.assertEquals("java_.lang.String_", generator.getClassFqName(FqName("java.lang.String")))
		Assert.assertEquals("java_.uppercasePackage.LowercaseClass_", generator.getClassFqName(FqName("java.UppercasePackage.lowercaseClass")))
		Assert.assertEquals("java_/uppercasePackage/LowercaseClass_.hx", generator.getFilePath(FqName("java.UppercasePackage.lowercaseClass")))
	}

	@Suppress("NOTHING_TO_INLINE")
	inline private fun testFieldName(name: String, expected: String) = Assert.assertEquals(
		expected,
		generator.apply { AstFieldRef(FqName("com.test"), name, AstType.INT).targetName }
	)

	@Test
	fun testFieldName() {
		testFieldName("catch", "catch_");
		testFieldName("unix", "unix_"); // @TODO: Bug. HXCPP is not prefixing fields, so any #define will fail.
	}
}