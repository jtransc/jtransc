---
layout: default
title: Gradle
---

<img src="/usage/gradle/gradle.png" width="auto" height="96" />

Gradle supported. [Example here](https://github.com/jtransc/jtransc/blob/master/jtransc-gradle-plugin/example/build.gradle) and [other here](https://github.com/jtransc/jtransc-examples/blob/master/libgdx/cuboc/build.gradle)

Template:

{% highlight groovy %}
apply plugin: "java"
apply plugin: 'application'
apply plugin: "jtransc"

ext.jtranscVersion = "0.2.5"

mainClassName = "CubocJTransc"

sourceSets.main {
	java {
		srcDirs = ['src', 'src_jtransc']
	}
	resources {
		srcDir 'assets'
	}
}

buildscript {
	repositories {
		mavenLocal()
		mavenCentral()
	}
	dependencies {
		classpath "com.jtransc:jtransc-gradle-plugin:0.2.5"
	}
}

repositories {
	mavenLocal()
	mavenCentral()
}

dependencies {
	//compile group: 'com.jtransc.media', name: 'jtransc-media', version: '$jtranscVersion'
	compile "com.jtransc.gdx:gdx-backend-jtransc:$jtranscVersion"
	testCompile group: 'junit', name: 'junit', version: '4.+'
}

jtransc {
	// Optional properties (https://github.com/jtransc/jtransc/blob/master/jtransc-gradle-plugin/src/com/jtransc/gradle/JTranscExtension.kt)
	// title = "App Title"
	// name = "AppName"
	// version = "0.0.1"
	// target = "haxe:js"
	// output: String? = null
	// extra = hashMapOf<String?, String?>()
	// company = "MyCompany"
	// package_ = "com.test"
	embedResources = true
	// libraries = arrayListOf<String>()
	assets = ["assets"]
	// debug = true
	// initialWidth = 1280
	// initialHeight = 720
	// vsync = true
	// resizable = true
	// borderless = false
	// fullscreen = false
	// icon: String? = null
	// orientation = "auto"
	// relooper = false
	minimizeNames = false
	// analyzer = false
}

// Defined tasks

// Building tasks:
// - distribution:distCpp
// - distribution:distJs
// - distribution:distNeko
// - distribution:distPhp

// Building and running tasks:
// - distribution:runCpp
// - distribution:runNeko
// - distribution:runNodeJs
// - distribution:runPhp
// - distribution:runSwd

// Custom Building/running tasks:
// - jtransc:distJtransc (must specify target and outputFile options)
// - jtransc:runJtransc (must specify target and outputFile options)

import com.jtransc.gradle.tasks.JTranscDistTask
import com.jtransc.gradle.tasks.JTranscRunTask

task distWindows(type: JTranscDistTask) {
	target = "haxe:cpp"
	outputFile = "program.exe"
	minimizedNames = false
	debug = false
}

task runWindows(type: JTranscRunTask) {
	target = "haxe:cpp"
	//outputFile = "program.exe"
	minimizedNames = false
	debug = true
}

task distHtml(type: JTranscDistTask) {
	target = "haxe:js"
	outputFile = "program.js"
	minimizedNames = true
	debug = false
}

task runHtml(type: JTranscRunTask) {
	target = "haxe:js"
	//outputFile = "program.js"
	minimizedNames = false
	debug = true
}
{% endhighlight %}
