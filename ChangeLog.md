# CHANGELOG

## 0.6.1 (WIP)

- Added `@JTranscAddIncludes` and `@JTranscAddDefines` annotations
- Honor `@JTranscAddLibraries` and `@JTranscAddIncludes` annotations in C++ target
- Added `@JTranscAddTemplateVars` annotation to add list variables to templates from code
- CMAKE now supports list template variables `CMAKE` and `CMAKE_PROJECT` for adding custom lines before and after project
- Added `@JTranscAddMembers.cond` to conditionally add members
- Initial support of class `@JTranscNativeName` types in C++. Implemented Int32x4 SIMD to check. Works even without ENABLE_TYPING enabled.
- Fxied compilation in windows (b2.exe not executing)

## 0.6.0

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

## 0.5.16

...