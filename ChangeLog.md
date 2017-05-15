# CHANGELOG

## 0.5.17 (WIP)

### New features
- AS3 target (BigTest passes)
- Prevent generating runtime invisible annotations

### Changes
- Some refactorings at CommonGenerator

### Fixes
- SergeyLabutin - Static initialization fixes
- SergeyLabutin - Some thread work on Haxe/CPP

### Implemented features
- SergeyLabutin - Implemented Class.getGenericSuperclass

### Optimizations
- Better output quality (less casts), still left
- Lightly improved compile time
- Improved StringBuilder runtime in all targets
- Improved slightly performance on all targets (simplifying Object construction)
- D put strings at compile-time since Object construction no longer require runtime
- C# implemented genStmSetArrayLiterals reducing output size and startup time


## 0.5.16

...