---
layout: default
title: Annotations
---

JTransc unsderstand some annotations that are defined in the `jtransc-rt-core` repository. You can find the [complete list of annotations here](https://github.com/jtransc/jtransc/tree/master/jtransc-rt-core/src/com/jtransc/annotation).

## General

* `@JTranscIgnoreDependencies()` - It will ignore referenced classes, methods and fields in that method because it will include a specific method body. Use this carefully.
* `@JTranscInline` - Marks a specific method as inline. When supported it will inline its body in the callee. Use this carefully, on small one-liner methods, as this could lead to worse performance in some cases.
* `@JTranscInvisible` - Marks the class, method or field as invisible reflection-wise, so no reflection information is available at runtime, reducing output size.
* `@JTranscInvisibleExternal({"fq.class1","fq.class2"})` - From another class, externally allows to mark several other classes as invisible reflection-wise.
* `@JTranscKeep` - Keeps all methods/fields in this class, or a specific field or method on the output when performing dead code elimination.
* `@JTranscKeepName` - Keeps the original name of a class, method or field. Unadvised. Use Minitemplates to reference stuff from the outside.
* `@JTranscNative` - Keeps the original name of a class, method or field. Unadvised. Use Minitemplates to reference stuff from the outside.
* `@JTranscNativeClass` - Marks a class as Native and specifies an external fqname for it.
* `@KeepConstructors`  - Keep all the constructors of the class and inherited classes even when not referenced. Used when instantiating a class reflectively at runtime.
* `@KeepFields` - Keep all fields in the class and inherited classes, so they can be written or readed reflectively.
* `@KeepMethods` - Keep all methods in the class and inherited classes, so they can be called at runtime reflectively

## Haxe specific

When targeting to Haxe, there are some annotations you can use:

* `@HaxeAddAssets({"file1", "file2})` - Include some files as assets
* `@HaxeAddFilesBeforeBuildTemplate({"file1", "file2"})` - Include to the target generated sources folder some files from resources folder and process using [Minitemplates](/minitemplates). Just before building, after knowing everything from the program.
* `@HaxeAddFilesRaw({"file1", "file2"})` - Include to the target generated sources folder some files from resources folder without processing them just as they are.
* `@HaxeAddFilesTemplate` - Include to the target generated sources folder some files from resources folder and process using [Minitemplates](/minitemplates)
* `@HaxeAddLibraries({"haxe-ws:0.0.6"})` - Include one or more haxe libraries. In the form: `name:version`
* `@HaxeAddMembers({"var field:Int;"})` - Add some lines at class level. This could include fields, or methods.
* `@HaxeMeta` - Adds text to the output at meta places. So you can add custom haxe metas.
* `@HaxeMethodBody(target=name, value=...)` - Body of the method for the specified target. This annotation is @Repeteable using HaxeMethodBodyList annotation. Argument names are `p0`, `p1`, `p2`...
* `@HaxeMethodBodyPre` - Haxe Source to be prepended to the method for all haxe subtargets
* `@HaxeMethodBodyPost` - Haxe Source to be appended to the method for all haxe subtargets
* `@HaxeMethodBodyList({...bodies})` - A list of method bodies
* `@HaxeNativeConversion` - Specifies a custom Haxe <-> Java conversion for the annotated class.
* `@HaxeRemoveField` - The specified field won't be included in the generated output source.

## Haxe specific for custom targets

JTransc allows to define custom builds. Instead of calling Haxe, you can call other command line.

[jtransc-media-lime](https://github.com/jtransc/jtransc-media/tree/master/jtransc-media-lime) is a great example about how to create custom targets.

* `@HaxeAddSubtarget(...)` - Add a haxe custom target that can be used with some custom tooling
* `@HaxeAddSubtargetList{...targets}` - Add a list of haxe custom targets
* [`@HaxeCustomBuildCommandLine`](https://github.com/jtransc/jtransc/blob/master/jtransc-rt-core/src/com/jtransc/annotation/haxe/HaxeCustomBuildCommandLine.java) - Specify a custom command line for building
* `@HaxeCustomMain` - Defines a custom main, in the case your tooling requires the bootstrap entry point for example to extend a class.

## Deprecated

* `@JTranscField("name")` - Specify a field name on external classes.
* `@JTranscMethod("name")` - Specify a field name on external classes.
* `@JTranscGetter("name")` - Specify that a method is the getter of an specific field on an external class.
* `@JTranscSetter("name")` - Specify that a method is the setter of an specific field on an external class.
* `@JTranscNativeClassImpl` - Deprecated
* `@JTranscPackageClass` - Deprecated
* `@JTranscPackageClassImpl` - Deprecated
* `@JTranscReferenceClass` - Deprecated
* `@HaxeImports` - Deprecated
