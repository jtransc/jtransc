---
layout: default
title: "Haxe Embed C"
---

It is possible to embed C/C++/Objective-C code in JTransc. Using `@HaxeMeta` and using `hxcpp` tricks.

Example: [https://github.com/jtransc/jtransc-examples/blob/master/cpp/src/example/Test.java](https://github.com/jtransc/jtransc-examples/blob/master/cpp/src/example/Test.java)

{% highlight java %}{% raw %}
public class Test {
    static public void main(String[] args) {
        System.out.println(Demo.mysum(7, 3));
    }
}

@HaxeMeta("@:cppInclude('./../test.c')")
@HaxeAddFilesTemplate("test.c")
class Demo {
    @HaxeMeta("@:noStack")
    @HaxeMethodBody(target = "cpp", value = "return untyped __cpp__('::sum({0}, {1})', p0, p1);")
    static native public int mysum(int a, int b);
}
{% endraw %}{% endhighlight %}
