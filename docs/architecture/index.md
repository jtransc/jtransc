---
layout: default
title: "Architecture"
---

## A simplification of the process

1. JTransc reads all the required dependencies from Maven, Classpaths or wathever.
2. It converts JVM Bytecode from those classes into a fully featured AST.
3. Then that AST is reduced to the features supported for the target language generating a simplified AST.
4. Later, that AST is transformed into the actual target language. Annotations allows to modulate how and which code is generated.

In the case of Haxe as target language, using a subtarget allows you to instruct Haxe to convert it to the specified language.

## Modules:

JTransc is split into several modules:

### jtransc-core

Here is where most of the jtransc code is found. It is written in Kotlin. And this project contains the AST, graph analyzing and transformation stuff. This part doesn't know anything about target languages. Just handles JVM Bytecode and transforms it into an easy-to-handle AST.
It is possible that this part alone would evolve into an standalone piece. Something like: AstVM that defines the AST. And then utilities that can serialize/unserialize and transform java bytecode into that AST. So anyone can consume or generate that AST code. Just as happens on WebAssembly but for higher level languages.

## jtransc-utils

This project is a dependency of several JTransc modules. And it contains utility classes.

## jtransc-gen-haxe

This module handles the Haxe generation. It knows about the haxe tooling (haxe and haxelib commands) and haxe specific annotations. And knows how to transform JTransc AST into Haxe sourcecode. But doesn't know anything about JVM bytecode.

## jtransc-debugger

Contains generic code to handle several debug protocols, sourcemaps and provides a common interface for consume any of the supported debug protocols easily.

## jtransc-intellij-plugin

A plugin for intelliJ that can compile code using JTransc and implements the intelliJ debugging API using the `jtransc-debugger` module.

## jtransc-main and jtransc-main-run

`jtransc-main` contains a command line interface for using JTransc without maven, and allows to build a fatjar with all the dependencies included.
`jtransc-main-run` provides a terminal command `jtransc` using maven fast and easily.

## jtransc-maven-plugin

This is the maven plugin. The one that handles compiling stuff with maven which is the recommended way for using JTransc.

## jtransc-rt

This contains a custom RT compatible with the Java Runtime. This will eventually reference OpenJDK overriding just a small amount of specific classes.

## jtransc-rt-core

This module is very important for JTransc and has [its own section](jtransc-rt-core). This module can be referenced in any project even in those that are not using JTransc directly. It provides a set of annotations that JTransc can understand: some generic and some other target-specific.
Also provides some classes that works out of the box with the JVM, but that provides an optimized implementation for some targets.
