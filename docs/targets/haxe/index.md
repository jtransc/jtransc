---
layout: default
title: "Target: haxe"
---

<img src="/targets/haxe/haxe.png" width="auto" height="96" />

You can see [Haxe custom annotations here](/jtransc-rt-core/annotations).

## Haxe classes with useful stuff

Haxe target has some support native classes here: [https://github.com/jtransc/jtransc/tree/master/jtransc-rt/resources](https://github.com/jtransc/jtransc/tree/master/jtransc-rt/resources)

* `HaxeArray*` classes wraps `boolean[]`, `byte[]`, `short[]`... so they extend `java.lang.Object` and provide useful methods for reading, writting or constructing/deconstructing from/to Haxe primitive types.
* `HaxeDynamicLoad` class provides some methods for dynamic library loading
* `HaxeFfiLibrary` interface works as internal interface for FFI loading
* `HaxeFormat` provides a portable C/Java like String.format.
* `HaxeIO` provides portable IO utilities for all haxe targets.

# HaxeNatives.hx and N.hx

[HaxeNatives](https://github.com/jtransc/jtransc/blob/master/jtransc-rt/resources/HaxeNatives.hx) and [N](https://github.com/jtransc/jtransc/blob/master/jtransc-rt/resources/N.hx) classes, provides some utility methods used by the runtime and that can be used by libraries.
You can find which methods contains just examining those files.

# R.hx

`R` class provides an internal class for doing reflection stuff. Since there are potentially tons of calls to this class methods, it has a short name.
