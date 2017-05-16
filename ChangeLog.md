# CHANGELOG

## 0.6.0 (WIP)

### New features
- @Intrigus: C++ target with GC
- AS3 target (BigTest passes)
- Prevent generating runtime invisible annotations

### Changes
- Some refactorings at CommonGenerator

### Fixes
- @SergeyLabutin - Static initialization fixes
- @SergeyLabutin - Some thread work on Haxe/CPP

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

## 0.5.16

...