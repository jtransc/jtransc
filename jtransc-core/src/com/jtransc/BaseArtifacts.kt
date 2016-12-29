package com.jtransc

data class Artifact(val group: String, val name: String, val version: String) {
	val str: String get() = "$group:$name:$version"
	override fun toString(): String = str
}

fun Iterable<Artifact>.toListString(): List<String> = this.map { it.str }

val BaseRuntimeArtifactsWithoutVersion = listOf(
	Artifact("com.jtransc", "jtransc-rt", JTranscVersion.getVersion()),
	Artifact("com.jtransc", "jtransc-rt-core", JTranscVersion.getVersion()),
	Artifact("com.jtransc", "jtransc-annotations", JTranscVersion.getVersion())
)

fun BaseRuntimeArtifactsForVersion(version: String = JTranscVersion.getVersion()) = BaseRuntimeArtifactsWithoutVersion.map { it.copy(version = version) }