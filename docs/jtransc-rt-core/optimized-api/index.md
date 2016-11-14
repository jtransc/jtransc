---
layout: default
title: Optimized API
---

`jtransc-rt-core` declares several classes that have optimized implementations on some targets.

You can see [all those classes here](https://github.com/jtransc/jtransc/tree/master/jtransc-rt-core/src/com/jtransc).

# Annotations

## com.jtransc.io.annotations package

See [Annotations](/jtransc-rt-core/annotations)

# System

## com.jtransc.JTranscVersion

This class allows to determine the JTransc version that is being used at runtime.

## com.jtransc.JTranscSystem

* `double fastTime()` - Allows to obtain the current time in milliseconds using a double instead of a long, totally avoiding allocation on targets that doesn't support 64-bit long integers, like JavaScript or Flash.
* `double stamp()` - Gets elapsed time in milliseconds between calls
* `void gcDisable()` - Disables GC if targets support it
* `void gcEnable()` - Enables GC if targets support it
* `void gc()` - Forces a GC call if targets support it
* `boolean usingJTransc()` - Determines if the code is being run on JVM or using JTransc
* `void debugger()` - If there is a debugger plug in, and it supports it, this will break the execution at the current position.
* `void assert2(boolean)` - Will check that the assertion is true, otherwise it will break the execution running the debugger
* `String getRuntimeKind()` - Will get a string with the kind of platform that it is executing the code. Possible values: `java`, `js`, `swf`, `csharp`, `cpp`, `neko`, `php`, `python`, `unknown`.
* `isSys, isCpp, isCSharp, isJava, isJs, isSwf, isNeko, isPhp, isPython` - Will return true if the code is running in that target
* `String getOSRaw()` - Will return the name of the operating system.
* `String getOS()` - Will return the normalized operating system: "windows", "linux", "mac"...
* `String getArch()` - Will return the name of the architecture being used: `x86`, `i386`, `ppc`, `sparc`, `arm`.
* `String getOS()` - Will return the normalized operating system: "windows", "linux", "mac"...
* `isWindows, isLinux, isMac`  - Will return true if the code is running in that operating system
* `String fileSeparator()` - Will return the string that is used to separate files '\\' (windows) or '/' (rest).
* `String pathSeparator()` - Will return the string that is used to separate paths ';' (windows) or ':' (rest).
* `String lineSeparator()` - Will return the string that is used to separate lines '\r\n' (windows), '\r' (old mac) or '\n' (linux).

# Memory and arrays

## com.jtransc.FastMemory

FastMemory class uses `java.nio.Buffer` in JVM, but it is optimized on some Haxe targets using Typed Arrays when available.
It allows to read/write from that memory chunk aligned or unaligned the fastest way the target allows. Except on flash target where Mem class is even faster.
This class also have methods to copy memory fast from byte[] or FastMemory chunks. Also provide some methods to reverse 32bit and 16bit integers.

## com.jtransc.FastMemory4Float and com.jtransc.FastInt4Float

32-bit views for integers and floats used along FastMemory. Define `set` and `get` methods that can be used with Kotlin as indexers.

## com.jtransc.Mem

Mem class uses `java.nio.Buffer` in JVM, but it is optimized on Haxe targets to use Typed Arrays (JavaScript, C++...). But it is even more optimized in the flash target, where it uses the special AVM2 memory opcodes.

This class has a `select` method that you have to call to specify which FastMemory object are you going to write/read from. `FastMemory` on flash target uses a ByteArray internally and `lX` (load methods), `sX` (store methods) read or write from it.

This should be the fastest way to use memory on all targets. But it just allows to use one buffer at a time.

## com.jtransc.JTranscBits

Class with some bit-level operations that can be optimized on JTransc.

## com.jtransc.JTranscArrays

Defines some methods to swizzle `int[]`. Or to reinterpret some arrays as other arrays. For example: `int[]` reinterpreted as `byte[]`.

# DS

## com.jtransc.FastIntMap

Map class that will avoid allocation for keys on some Haxe targets.

# Input/Output

`com.jtransc.io` package has several classes to do special IO on jtransc.

## com.jtransc.io.JTranscConsole

Allows to write lines directly to the console/error. This would lead to traces in flash target and console.* in js target directly without a buffering Stream.

## com.jtransc.io.JTranscIoTools

Some utility methods to work with streams.

## com.jtransc.io.JTranscSyncIO

This class allows to overwrite how synchronous filesystem I/O operations work when targeting to JTransc. It doesn't not have effect on JVM.

There is a static field called `impl` that has a implementation of the abstract class `JTranscSyncIO.Impl`. In the case you want to overwrite the default filesystem you can replace that field with yours. Also there is a `JTranscSyncIO.ImplStream` that return some methods from Impl.

**Impl** has the following methods: `open`, `getLength`, `getTotalSpace`, `getFreeSpace`, `getUsableSpace`, `setReadOnly`, `setLastModifiedTime`, `rename`, `createDirectory`, `list`, `delete`, `createFileExclusively`, `setPermission`, `getLastModifiedTime`, `checkAccess`, `getBooleanAttributes`, `getCwd`, `setCwd`.

**ImplStream** has the following methods: `setPosition`, `getPosition`, `setLength`, `getLength`, `read`, `write`, `close`

# FFI / JNA

## com.sun.jna.Native

`Native.loadLibrary` is available on JTransc and internally uses `com.jtransc.ffi.JTranscFFI`. On windows you have to mark methods using the stdcall convention with `com.jtransc.ffi.StdCall` for them to work on JTransc.

# SIMD

Though still there is no target benefiting from this, those classes will be optimized on some targets. Specially C++, C# and JavaScript on ecmascript SIMD.

## com.jtransc.simd.MutableFloat32x4

Represents a tuple of four 32bit float elements. And has mutable operations over it.
This class will be optimized on supported platforms.
