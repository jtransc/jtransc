JTRANSC
-------

![JTransc](extra/logo-256.png)

[![Maven Version](https://img.shields.io/github/tag/jtransc/jtransc.svg?style=flat&label=maven)](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22jtransc-maven-plugin%22)
[![Build Status](https://secure.travis-ci.org/jtransc/jtransc.svg)](http://travis-ci.org/#!/jtransc/jtransc)
[![irc](https://img.shields.io/badge/irc:%20chat.freenode.net-%23jtransc-green.svg)](http://webchat.freenode.net/?channels=#jtransc)
[![Join the JTransc Community on Slack](http://jtransc-slack.herokuapp.com/badge.svg)](http://jtransc-slack.herokuapp.com/)

# Documentation

You can find documentation at the [wiki](http://docs.jtransc.com/).

# What is this?

JTransc (Java Trans Compiler) is an AOT (ahead of time compiler) that compiles .class and .jar files
into a target executable bundling all the required dependencies in a single file, without requiring
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
  id "com.jtransc" version "0.5.12"
}
```

This will add several tasks for building your application.
There is a gradle block called jtransc, that allows you to specify stuff for the build.

For example `gradle runJs` would generate a JS file at `build/jtransc-js/program.js` and run it using node.js.

[You can see how to use it in the documentation](http://docs.jtransc.com/usage/gradle).

## Maven:

You can also use Maven, though due to the nature of JTransc gradle fits better.
[You can see how to use it in the documentation](http://docs.jtransc.com/usage/maven).

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

## Sponsored by:

![Akamon Entertainment](extra/akamon.png)
