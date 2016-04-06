package com.jtransc.input

import com.jtransc.ast.AstAnnotation
import com.jtransc.ast.AstType

val ANNOTATIONS_BLACKLIST = listOf(
	"java.lang.annotation.Documented", "java.lang.Deprecated",
	"java.lang.annotation.Target", "java.lang.annotation.Retention",
	"kotlin.jvm.internal.KotlinLocalClass", "kotlin.jvm.internal.KotlinSyntheticClass",
	"kotlin.jvm.internal.KotlinClass", "kotlin.jvm.internal.KotlinFunction",
	"kotlin.jvm.internal.KotlinFileFacade", "kotlin.jvm.internal.KotlinMultifileClassPart",
	"kotlin.jvm.internal.KotlinMultifileClass", "kotlin.annotation.MustBeDocumented",
	"kotlin.annotation.Target", "kotlin.annotation.Retention",
	"kotlin.jvm.JvmStatic", "kotlin.Deprecated", "kotlin.Metadata", "org.jetbrains.annotations.NotNull",
	"kotlin.internal.InlineExposed"
).map { AstType.REF(it) }.toSet()

fun List<AstAnnotation>.filterBlackList(): List<AstAnnotation> {
	return this.filter { it.type !in ANNOTATIONS_BLACKLIST }
}