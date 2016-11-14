---
layout: default
title: What is JTransc
---

Jtransc (Java Trans Compiler) is an AOT (ahead of time compiler) that compiles .class and .jar files into a target executable file bundling all the required dependencies in a single file, without requiring a jitter or an external runtime. At the beggining it generated as3 and javascript, but right now there is a single target: haxe. This allows targeting js, as3, c++, c#, java, php and python. This in turn allows running the program on different platforms such as desktop, browsers and mobile.

The aim of this project is to bring the high productivity of Kotlin (and other JVM languages) to the highly portable haxe platform and other direct targets in the future. It already supports some APIs and plain Java reflection API out of the box.

The initial focus is on jvm6, kotlin, haxe and games, but it will get better in the future supporting newer jvm versions, and other use cases like frontend and backend applications.

There is a module called jtransc-rt-core that could be included in any project (whether using jtransc or not). It provides the com.jtransc package, specific annotations, fast memory access and asynchronous APIs, that will use jtransc features when compiling using jtransc.

These is also a couple of projects for multimedia applications using jtransc:

* [jtransc-media](/libraries/jtransc-media) - Which provides a very simple and portable high-level API for multimedia
* [gdx-backend-jtransc](/libraries/jtransc-media) - Which provides a gdx-compatible backend so any gdx project will be able to work (still some rough edges)

## How does it work internally?

* It locates all the required dependencies (specifying dependencies, using maven or intelliJ)
* It includes jtransc-rt-core and jtransc-rt which is a java-6-like rt with some of their methods marked as natives
* Other dependencies than the RT are included without modifications
* It uses ASM to generate a class-method-statement-expression AST
  * That AST is easily serializable
  * That AST allows feature stripping
* Your target language don't support gotos? It will generate an AST without gotos. Just plain if/while/switch...
* It generates your target source code, replacing some classes like String, ArrayList and so on, to make them fast in your target * language.
* It joins or compiles that code into your final runnable program
* Eventually that intermediate AST will be able to be generated or consumed. So others could generate that without JVM and others could generate other targets from that AST directly without all the complexities of stack-based IRs.
