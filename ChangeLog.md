# CHANGELOG

## 0.6.8 (2018-01-14)

New features:
- ALL: Improved generated code (now new + constructor is joined), this also improves calling native code and fixes #134
- JS: String concatenation (new ArrayBuilder+append+toString chain) is now optimized (less code and better performance)
- HAXE-CPP: Support Haxe-Cpp synchronized blocks
- JS: Support Threads in JS using await/async (disabled by default because of performance impact)
- JS: Now targets to ES6 with classes since it is mainstream already
- Implement Object.wait/notify/notifyAll + Semaphore using Object facilities
- ALL: Added JTranscTargetClassImpl to remap a class depending on the target (similar to ServiceLoader approach but simpler)

Fixes:
- Gradle: Fixed newer gradle versions that output different language classes in different directories.
- Fixed order of Side effects in several cases in D and C++ targets

Improvements:
- Thread improvements in all targets

Performance Improvements:
- @SergeyLabutin Reflection Cache

Misc:
- @SergeyLabutin Support lime.net.HTTPRequest
- @SergeyLabutin Missing ScheduledThreadPoolExecutor.submit
- @SergeyLabutin Updated lime to 5.4.0
- Updated Kotlin to 1.2.10
- Updated Gradle to 4.4.1
- Update plugin-publish-plugin to 0.9.9

## 0.6.7 (2017-08-13)

New features:
- @Intrigus Add almost complete jni implementation for cpp target 
- @fxjshm: Added synchronized function support (except Haxe target for now)
- @fxjshm: Support mutex in C++ target
- Added support for `Runtime.addShutdownHook()` to JS target
- Added `javax.sound.sampled` stub classes
- Added tons of `java.nio` stub classes
- Added `java.awt.Desktop` stub class
- Added `java.util.Optional`
- Added most `java.util.functional.*` interfaces
- Added `java.net.ServerSocket` stub class
- Added and implemented lots of `java.time` classes

Improvements:
- @SergeyLabutin: Split reflection information in several methods
- @SergeyLabutin: Thread improvements
- Implemented `String.format("%.f")` support with Locale support
- Honor `cond` in `JTranscAddLibraries`, `JTranscAddIncludes`, `JTranscAddDefines`, `JTranscAddImports`

Fixes:
- @intrigus: Compile Bdwgc lib statically instead of dynamically
- Fixed System.arraycopy undefined behaviour on overlapping
- Make more compatible `d2i` and `f2i` opcodes for float and double to int conversions in NaN, -Inf and +Inf

Misc:
- Moved each language test to its own project. Added jtransc-gen-common-tests
- Added codecov + jacoco to test coverage
- Make haxelib quiet to reduce log output in travis
- Updated gradle to 4.0.2
- Disabled C# tests on travis for now
- Updated travis to Ubuntu Trusty
- haxelib --always
- @SergeyLabutin: Updated lime to 5.3.0

## 0.6.6 (2017-07-23)

- @SergeyLabutin: Implemented Runtime.memory methods for Haxe+Cpp target
- @SergeyLabutin: Fix JTranscBits.read int64
- @SergeyLabutin: Fix DataInputStream.readUTF and DataOutputStream.writeUTF using modified utf-8
- Implemented array literals in all languages
- Split reflection getFieldAnnotations per class
- @fxjshm: Fix boolean literals on C#
- Added jtransc.optimize option to gradle and tests to disable optimizations
- Disabled variable inlining optimization for now since it caused some spurious bugs
- @fxjshm: Added AppVeyor CI support for testing on a Windows machine

## 0.6.5 (2017-07-07)

Improvements:
- [HAXE] [HAXE-CPP] Greatly improved performance of System.arraycopy and System.fill
- [HAXE-CPP] Improved performance of Object[] arrays
- [CPP] Improved performance of fill for 64-bit types (long and double) in 32-bit builds

Fixes:
- [TEMPLATES] Fixed if+else minitemplates

Missing API:
- @fxjshm: Added UnsupportedClassVersionError

Misc:
- [HAXE-CPP] Enabled travis-ci automated testing for haxe-cpp target
- @SergeyLabutin Documentation for Android Target in readme
- Updated Kotlin to 1.1.3-2
- Added JTRANSC_OS to templates
- Added `|image_info` minitemplate filter to get width and height from images from a byte array, File or String path

## 0.6.4 (2017-07-01)

New features:
- [ALL] Plugins are now able to process classes after applied features
- [ALL] @SergeyLabutin Added additional missing settings to CommonGenerator

Fixes:
- [CPP] Fixed float REM opcode on C++
- [ALL] Fixed INVOKE_DYNAMIC_METHOD with additional expressions + optimizer (problems with inner lambdas)
- [GRADLE] Fixes gradle `appendVar` when no annotation was provided already

Improvements:
- [JS] Now compiler strips .0 trailings on double literals to reduce output size

Cleanups:
- [ALL] Unified body features on all targets
- [CPP] Removed ClassLoader C++ specialized code. Since this is not going to work at least with the current reflection library.

Misc:
- [HAXE] Updated lime to 0.5.1
- [ALL] Updated to kotlin 1.1.3

## 0.6.3 (2017-06-11)

New features:
- [PHP] Requires PHP 7.1. PHP target now passes big tests though it is slower than other targets
- [GRADLE] Added appendVar support. Check: fdd58dbe26203703adfc64484c5a575f17dddb9f

Fixes:
- [ALL] Class.getMethod and getDeclaredMethod correct behaviour
- [HAXE-CPP] @SergeyLabutin - Implemented Object.wait
- [CPP+C#] Fixed float NAN
- [CPP] @intrigus Consistency and warnings fixes in ++ target
- [HAXE] Missing keywords

Improvements:
- [C#] Allow specify C# compiler. Check issue #156
- [ALL] Reduced output size combining several switch cases in one.
- [ALL] Support overlay methods in native methods
- [ALL] Simplify generating ClassInfo for reflection
- [HAXE] Improved integer array literal for small counts

Misc:
- Now snapshots versions are published by travis to bintray: https://dl.bintray.com/jtransc/jtransc/com/jtransc/


## 0.6.2 (2017-05-31)

Optimizations:
- [ALL] Optimized ByteBufferAs* in most targets with faster reading similar to JT's FastMemory
- [ALL] Proper behaviour of checkcast opcode
- [HAXE] JA_B and JA_Z now are backed by haxe.io.Bytes that will allow to do some optimizations
- [HAXE] Should help with https://github.com/jtransc/gdx-backend-jtransc/issues/41 Update to 0.6.0 (dropped reinterpret arrays) and  https://github.com/jtransc/gdx-backend-jtransc/pull/42 / https://github.com/jtransc/gdx-backend-jtransc/pull/43
- [CPP] @Intrigus: Use intrinsics for byte swappings

Fixes:
- [ALL] Fixed a bug in treeshaking creating extra methods that broke `super` functionality and makes code bigger
- [HAXE] Fixed indentation of the output classes
- [HAXE] Fixed haxe division + remainder (both integer and long, at least in C++ with edge cases)
- [HAXE] Fixed being able to execute c++ executables directly (at least in windows)
- [HAXE] Fixed com.jtransc.FastMemory in haxe-cpp
- [ALL] Some static init fixes + documentation about static initialization issues
- [HAXE-CPP] @SergeyLabutin Fixed StringBuilder with unicode characters in Haxe-C++
- [PHP] Some PHP issues. Still not passing big test and extremely slow due to lack of typed arrays
- [ALL] Fix static initialization problem with charsets (static initialization + ServiceLoader a bit tricky)
- [ALL] @SergeyLabutin `TimeUnit.convert` fix

New features:
- [ALL] @SergeyLabutin: Implemented ThreadPoolExecutor
- [ALL] @SergeyLabutin: Implemented ScheduledThreadPoolExecutor
- [DART] Allow add extra imports externally

Changes:
- [ALL] Passthru target compiler output
- [CPP] Allow setting GC roots manually by the GC implementation
- [CPP] Embed `bdwgc.zip` to ensure we have the library and the right format and version.
- [CPP] Configurable GC
- [ALL] Changed ZIP reader to be consistent between platforms/Java VMs
- [TIZEN] Remove from documentation since not supported by Lime anymore

Minor:
- [JS] Do not use float to avoid issues with Closure Optimizer
- [JS] Some refactorings
- [GITTER] Unified chat `slack/irc -> gitter`
- [TRAVIS] Reduce mono dependencies
- [TRAVIS] Disabled travis cache


## 0.6.1 (2017-05-21)

- Added `@JTranscAddIncludes` and `@JTranscAddDefines` annotations
- Honor `@JTranscAddLibraries` and `@JTranscAddIncludes` annotations in C++ target
- Added `@JTranscAddTemplateVars` annotation to add list variables to templates from code
- CMAKE now supports list template variables `CMAKE` and `CMAKE_PROJECT` for adding custom lines before and after project
- Added `@JTranscAddMembers.cond` to conditionally add members
- Initial support of class `@JTranscNativeName` types in C++. Implemented Int32x4 SIMD to check. Works even without ENABLE_TYPING enabled.
- Fxied compilation in windows (b2.exe not executing)

## 0.6.0 (2017-05-20)

### New features
- @Intrigus: C++ target with GC continuing the work on @dsp-omen that created the initial c++ target
- AS3 target (BigTest passes)
- Dart target (BigTest passes)
- Prevent generating runtime invisible annotations
- Added com.jtransc.io.async package to handle asynchronous I/O
- Improved com.jtransc.js.JsDynamic

### Changes
- Some refactorings at CommonGenerator
- `@JTranscNativeName` now supports a target field to have a class has different types on different targets

### Fixes
- @SergeyLabutin - Static initialization fixes
- @SergeyLabutin - Some thread work on Haxe/CPP
- Handle template references inside `@JTranscCallSiteBody`

### Implemented features
- @SergeyLabutin - Implemented Class.getGenericSuperclass

### Optimizations
- Better output quality (less casts), still left
- Lightly improved compile time
- Improved StringBuilder runtime in all targets
- Improved slightly performance on all targets (simplifying Object construction)
- D put strings at compile-time since Object construction no longer require runtime
- C# implemented genStmSetArrayLiterals reducing output size and startup time
- Rework on Haxe arrays and casts specially in C++ for a major performance boost
- Implemented Haxe-C++ goto hack for even better performance on loops

### Deprecations
- Array casts are now deprecated and removed, because that requirement was slowing down some implementations. In the future we will explore other options. For now, please use Mem/FastMemory* classes.
- Deprecated {% FIELD %} and {% METHOD %} (replaces to `NAME`) in favour of {% IFIELD %} and {% IMETHOD %} (replaces to `.NAME` or `['NAME']` in JS just when required and works with minification)
- Deprecated `@JTranscNativeClass` that was redundant due to `@JTranscNativeName`.

## 0.5.16 (2017-05-08)

...

## 0.5.0 (2016-12-04)

...

## 0.4.0 (2016-09-16)

...

## 0.3.0 (2016-06-20)

...

## 0.2.0 (2016-04-22)

...

## 0.1.0 (2016-02-24)

...

## First public commit (2016-02-08)

...