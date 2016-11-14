---
layout: default
title: Languages
---

<img src="/languages/java/java-square.svg" style="width:96px;height:96px;" />
<img src="/languages/kotlin/kotlin-square.svg" style="width:96px;height:96px;" />
<img src="/languages/scala/scala-square.svg" style="width:96px;height:96px;" />

JTransc instead of being a source-to-source compiler, its input is JVM bytecode, so
it can potentially support several JVM languages, and indeed it supports them!

## Java

An Android compatible RT is provided. It should support Java6 and has partial support for Java7 and Java8.

## Kotlin

Kotlin targets to Java6. Also JTransc itself is written in Kotlin, and our games are written in Kotlin too,
so we want to support Kotlin completely.

## Scala

Scala as a compiled JVM language should work out of the box, but not tested.