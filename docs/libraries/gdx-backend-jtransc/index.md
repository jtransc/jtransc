---
layout: default
title: gdx-backend-jtransc
---

`gdx-backend-jtransc` is a backend for gdx that works on html5 and C++ (desktop and mobile [android and ios]) with just one target.

It uses internally [lime](https://github.com/openfl/lime) crossplatform Haxe library.

Benefits over other alternatives:

* On **HTML5**: Javascript. Provides full reflection support. And allows to mix Java, Kotlin or Scala code directly. And without requiring source code.
* On **Android**: Native C++. No method count limit. Predictable speed (not depending on Dalvik, ART versions). Not require crosswalk 40MB or javascript JIT overhead.
* On **iOS**: Native C++. Can target several iOS versions. Not javascript so no interpreter or not webgl support depending on version. Predictable performance and no javascript JIT overhead.
* On **Desktop**: Native C++. No JVM required. Package in a single executable. Fast startup.
* On **Flash**: Not yet ready. Will have to emulate shaders (or provide somehow as binary).

You can see the library here: [https://github.com/jtransc/gdx-backend-jtransc](https://github.com/jtransc/gdx-backend-jtransc)
