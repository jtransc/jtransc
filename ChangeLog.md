# CHANGELOG

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