plugins {
    kotlin("multiplatform") version "1.4.10"
}
group = "me.soywi"
version = "1.0-SNAPSHOT"

repositories {
	mavenLocal()
    mavenCentral()
}
kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    //js(org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.BOTH) {
	js(org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType.IR) {
	//js("nodeJs") {
        nodejs {
            binaries.executable()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
        val jsMain by getting
        val nativeMain by getting
    }
}