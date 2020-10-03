JTRANSC
-------

![JTransc](extra/logo-256.png)

[![Maven Version](https://img.shields.io/github/tag/jtransc/jtransc.svg?style=flat&label=maven)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jtransc-maven-plugin%22)
[![Build Status](https://secure.travis-ci.org/jtransc/jtransc.svg)](http://travis-ci.org/jtransc/jtransc)
[![Build status](https://ci.appveyor.com/api/projects/status/qnd0g966t1b54q4a?svg=true)](https://ci.appveyor.com/project/soywiz/jtransc)
[![Code coverage](https://codecov.io/gh/jtransc/jtransc/branch/master/graph/badge.svg)](https://codecov.io/gh/jtransc/jtransc)
[![gitter](https://img.shields.io/gitter/room/jtransc/general.svg)](https://gitter.im/jtransc/general)

# Documentation

You can find documentation at the [wiki](https://jtransc.soywiz.com/).

# What is this?

JTransc (Java Trans Compiler) is an AOT (ahead of time compiler) that compiles .class and .jar files
into a target programming language / executable bundling all the required dependencies in a single file or folder,
without requiring a jitter or an external runtime.

# Why using JTransc?

There are a lot of technologies in order to convert languages into other ones. For example, converting Java into JavaScript.
Or KotlinJS Kotlin backend that already targets JavaScript. So why using JTransc?

### Mixed input code:

One reason is that JTransc allows mixed projects. You can use Java libraries with Kotlin code for example.

### Multiple targets:

Instead of using several technologies, JTransc allows you to target to several languages and platforms.

### Consistency:

Using just one technology guarantees consistency between targets. For example, Kotlin JS doesn't support full Java reflection.

### Native:

Instead of generating C++ and then using emscripten or other technologies, JTransc allows you to generate code that is
native to your platform. For example: when targeting JS you will use native JS best-in-class GC instead of a GC
emulated in C++ & emscripten. And no need to know a proper heap size beforehand targeting JS.

### Native facilities:

Some classes like String, StringBuilder or ArrayList are implemented in a way that it uses native JavaScript/AS3/Dart... facilities.
Like JavaScript String, Array and so on.

### Treeshaking:

Instead of generating huge projects including everything, or having complex proguard configuration files.
JTransc includes treeshaking in a simple way. You can use annotations to keep methods, classes and fields or another annotations.
It works for all targets and it is fully integrated in the workflow.

### Thread and sync I/O support in JS:

JTransc supports plain Java applications using Threads and sync I/O in JS converting that into an asynchronous version in JS
using await/async detecting branches not using Threads/sync I/O for fastest performance.

# Support this project

Along JTransc, I'm writting a set of libraries to be able to use JTransc.

[https://github.com/soywiz/korlibs](https://github.com/soywiz/korlibs)

Kotlin Game Engine that uses JTransc for multiplatform: [https://github.com/soywiz/korge](https://github.com/soywiz/korge)

Also there is a GDX backend using JTransc+Haxe: [https://github.com/jtransc/gdx-backend-jtransc](https://github.com/jtransc/gdx-backend-jtransc)

JTransc

# Detailed: What is this?

JTransc (Java Trans Compiler) is an AOT (ahead of time compiler) that compiles .class and .jar files
into a target programming language / executable bundling all the required dependencies in a single file or folder, without requiring
a jitter or an external runtime.

At the beginning it generated as3 and javascript, but right now there are several targets: Javascript, Haxe, C++, and D.
Haxe itself allow to target several other languages: JS, AS3, C++, C#, Java, PHP and Python.
This in turn allows running the program on different platforms such as desktop, browsers and mobile.

The aim of this project is to bring the high productivity of Kotlin (and other JVM languages)
to the highly portable Haxe platform.
It already supports most of the core Java APIs and plain Java reflection API out of the box.

The initial focus is on JVM6, Kotlin and Games, but it will get better in the future supporting newer jvm versions,
and other use cases like frontend and backend applications.

Right now JTransc support Java8 lambdas and default methods.

There is a module called jtransc-rt-core that could be included in any project (whether using JTransc or not).
It provides the `com.jtransc` package, specific annotations, fast memory access and asynchronous APIs,
that will use JTransc features when compiling using JTransc.

These is also a couple of projects for multimedia applications using jtransc:
* [jtransc-media](https://github.com/jtransc/jtransc-media) - Which provides a very simple and portable high-level API for multimedia
* [gdx-backend-jtransc](https://github.com/jtransc/gdx-backend-jtransc) - Which provides a gdx-compatible backend so any gdx project will be able to work (still some rough edges)

# How to use it?

You can find examples here [jtransc/jtransc-examples](https://github.com/jtransc/jtransc-examples).

## Plain:
```
# jtransc script can be found under the jtransc-main-run folder
javac com/test/Main.java -d target/classes
jtransc dependency.jar target/classes -main com.test.Main -out program.js
node program.js
```

## Gradle:

This is the preferred way of using JTransc. You can include it from maven central or directly from gradle plugins repository:

```
plugins {
  id "com.jtransc" version "0.6.8"
}
```

This will add several tasks for building your application.
There is a gradle block called jtransc, that allows you to specify stuff for the build.

For example `gradle runJs` would generate a JS file at `build/jtransc-js/program.js` and run it using node.js.

[You can see how to use it in the documentation](https://jtransc.soywiz.com/usage/gradle).

## Maven:

You can also use Maven, though due to the nature of JTransc gradle fits better.
[You can see how to use it in the documentation](https://jtransc.soywiz.com/usage/maven).

## intelliJ:

There is a plugin in the works that will allow to run and debug within intelliJ. Though it is not ready yet.
You can find it in [jtransc-intellij-plugin](https://github.com/jtransc/jtransc/tree/master/jtransc-intellij-plugin) folder.

# How does it work internally?

* It locates all the required dependencies (specifying dependencies using gradle, maven, intelliJ or CLI)
* It includes jtransc-rt-core and jtransc-rt which is a java-6-like (with some Java8 support) RT with some of its methods marked as native
* Other dependencies than the RT are included without modifications
* It uses ASM library to load class files, and has code to generate a class-method-statement-expression AST
    * That AST is easily serializable
    * That AST allows feature stripping
    * Your target language don't support gotos? It will generate an AST without gotos. Just plain if/while/switch...
* It generates your target source code, replacing some classes like String, ArrayList and so on, to make them fast in your target language.
* It joins or compiles that code into your final runnable program (using available compilers when required)
* It allows to run directly executing the executable or using an appropiate interpreter (php, node, electron...) depending on the build

Eventually that intermediate AST will be able to be generated or consumed.
So others could generate that without JVM and others could generate other targets from that AST directly without all the complexities of stack-based IRs.

## Tool dependencies:

### Java:
- Oracle JDK8 (Verified 8u131)
- Android SDK Tools  (Verified 25.2.3)

### For the Haxe target:
- Haxe 3.4.2
- NekoVM 2.1.0
- Lime 5.5.0
- hxcpp 3.4.64

### For Node.JS running:
- NodeJS 7.10.0 with npm 4.2.0

### For D target:
- DMD2 or GDC or LDC (Verified dmd-2.074.0)

### For C++ target:
- Clang++ or g++ (At least gcc 4.8)

### For Dart target:
- Tested with Dart VM version: 1.23.0

### Verified on Windows 10.0.15063 and macOS Sierra 10.12.4

#### Installing JDK
- Install [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/)
- Setup $JAVA_HOME
This sample, but maybe not working for you, be careful
```
echo "export JAVA_HOME=$(/usr/libexec/java_home)" >> ~/.bash_profile
source ~/.bash_profile
```

- Install [Android SDK Tools](https://developer.android.com/studio/index.html)
- Setup Android SDK with install NDK.

#### Installing Haxe
- Install [Haxe](https://haxe.org/download/) from here via installer
- Set path to haxelib running "haxelib setup" command
- Install [Lime](http://www.openfl.org/builds/lime/) `haxelib install lime 5.5.0`
- Install [hxcpp](http://nmehost.com/hxcpp/) `haxelib install hxcpp 3.4.64`
- Run `haxelib run lime setup`

######https://github.com/openfl/lime/issues/831
- Run `lime setup android`
If try install you gain Out of Memory, choose manual mode.
```
Download and install the Android SDK? [y/n/a] ? n
Download and install the Android NDK? [y/n/a] ? n
Download and install the Java JDK? [y/n/a] ? n

Path to Android SDK [C:\tools\android-sdk]:
Path to Android NDK []: C:\tools\android-ndk
Path to Java JDK [C:\Program Files\Java\jdk1.8.0_131]:
```

#### Setup AppleTV
- Run `lime rebuild hxcpp tvos`
- Remove stable lime `haxelib remove lime`
- Install lime from GitHub `git clone --recursive https://github.com/openfl/lime`
```
haxelib dev lime lime
haxelib install format
lime rebuild mac
lime rebuild ios
lime rebuild tvos
```

#### Installing Node.JS
- Install [NodeJS + npm](https://nodejs.org/en/)

#### Installing D
- Download [DMD](https://dlang.org/download.html#dmd)
- Install DMD with all extras

#### Installing C++ suitable compiler
__WINDOWS__
- Install [mingw](http://www.mingw.org/)
- Add "path/to/mingw/bin" to `PATH` environment variable strongly on first line
- Install cmake [https://cmake.org/download/](https://cmake.org/download/)

Workaround for big projects: haxe 3.4.2 can failed with out of memory. You can try replace haxe files
 [from develop](http://hxbuilds.s3-website-us-east-1.amazonaws.com/builds/haxe/windows/),
 but this dangerous way, and get only stable, see history on github.
 My current choose: 2017-03-23 05:39:01 >> 4876859 >> haxe_2017-03-23_development_ada466c.tar.gz

__WINDOWS 64__

- go to hxcpp/3.4.64 folder in console `neko run.n Build.xml -Dwindows -DHXCPP_M64 -Dstatic_link ./project/`
- Remove stable lime `haxelib remove lime`
- Install lime from GitHub `git clone --recursive https://github.com/openfl/lime -b master`
```
haxelib dev lime lime
haxelib install format
```
if use MSVC 2013 with not fully supported c99 standard, then replace in lib\openal all inline to __inline, and snprintf to _snprintf
```
lime rebuild windows -64
```

__MAC__
- Install Xcode 8.3.3
- This XCode have problem with logs for iPhone with iOS 10.3.2. Please see [solution](https://github.com/flutter/flutter/issues/4326#issuecomment-308249455)
- Related to pure C++ target on mac: `brew install automake libtool pkg-config`
- Install [Mono](http://www.mono-project.com/download/) 5.0.1.1  ??? https://github.com/jtransc/jtransc/issues/167
- For upload on iPhone/iPad from command line `sudo npm install -g ios-deploy --unsafe-perm --allow-root`

#### Installing Dart
- [https://www.dartlang.org/install](https://www.dartlang.org/install)
