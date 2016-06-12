Pure/Plain/Direct Javascript Target:
------------------------------------

## Why bother at all having haxe:js target?

Using haxe mean write a file per class in disk, using an external compiler that parses that code.

It would be great a OCaml to JVM converter, that would allow to use compiler directly in the JVM.
Maybe even removing the requirement to write to disk at all.

### Compilation times:

Also Haxe doesn't have incremental compilation due to having preprocessor and macros.
So one reason is compilation time. Writing directly to javascript is fast as hell and we can do incremental compilation.
Since the AST generation is per class and doesn't require knowing about external stuff.

Right now (for the benchmark using gradle):

* haxe-js: **13.101** seconds
* js: **2.435** secs

### Reduced output size:

Being able to generate custom code, allows to simplify stuff.

Right now (for the benchmark using gradle):

* haxe-js: **3.0** MB
* js: **2.2** MB

### Direct metadata registering:

Since we have full control on how javascript is generated, we can bind function definition with metadata definition.
Which reduces output size and makes things better and easier to understand.

### Improved debugging:

Because having full control about generated javascript, we can assign right names to functions and classes that completely
matches

### Direct sourcemaps:

Since Haxe doesn't have a `#line` preprocessor we have to create double sourcemaps: one per each haxe file + javascript generated sourcemap mapping haxe.
So it is far more complex and doesn't work quite well.
Generating javascript directly allows to generate sourcemaps directly mapping .js -> java/kotlin.

### Additional optimizations

We can decide which code to execute at runtime, but once, depending on runtime feature detection.

So for:

```
@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+0, +0, +0, +0);")
public MutableFloat32x4() {
	setTo(0f, 0f, 0f, 0f);
}
```

Instead of generating (which would be the Haxe option):

```
var hasSIMD = typeof SIMD !== "undefined";

this.registerConstructor(null, "()V", null, 1, function () {
	if (hasSIMD) {
		this.simd = SIMD.Float32x4(+0, +0, +0, +0);
		return this;
	} else {
		this.registerConstructor(null, "()V", null, 1, function () {
			this["java.lang.Object<init>()V"]();
			this["setTo(FFFF)V"](0.0, 0.0, 0.0, 0.0);
			return this;
			return this;
		});
	}
});
```

we can generate this, which is not possible on Haxe directly, because it is a runtime detected feature:

```
var hasSIMD = typeof SIMD !== "undefined";

var com_jtransc_simd_MutableFloat32x4 = program.registerType("com.jtransc.simd.MutableFloat32x4", 33, "java.lang.Object", [], function() {
	this.registerField("_x", "x", "F", null, 2, 0.0);
	this.registerField("_y", "y", "F", null, 2, 0.0);
	this.registerField("_z", "z", "F", null, 2, 0.0);
	this.registerField("_w", "w", "F", null, 2, 0.0);
	if (hasSIMD) {
		this.registerConstructor(null, "()V", null, 1, function () {
			this.simd = SIMD.Float32x4(+0, +0, +0, +0);
			return this;
		});
	}
	else {
		this.registerConstructor(null, "()V", null, 1, function () {
			this["java.lang.Object<init>()V"]();
			this["setTo(FFFF)V"](0.0, 0.0, 0.0, 0.0);
			return this;
			return this;
		});
	}
	...
}
```

Also having full control of the runtime allows to have lazy static initialization while being able to replace it
with a dummy function after fist call, and reduced posterior overhead.
