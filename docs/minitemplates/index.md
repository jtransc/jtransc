---
layout: default
title: Minitemplates
---

JTransc uses Minitemplates in several places.

Minitemplates is an embedded simple template engine created for JTransc based on [twig](http://twig.sensiolabs.org/documentation), my project [atpl.js](https://github.com/soywiz/atpl.js), [liquid](https://shopify.github.io/liquid/) among others.

## Use cases:
* When embedding external code in anotations
* When including target language files
* When specifying a custom command line for building
* When defining a target custom bootstrap main
* When generating project files requiring for build tools

## What can I do with Minitemplates?

Inside Minitemplates you have access to [parameters/configuration variables](/usage/maven) for that build.
Also you can get actual target generated names for: `classes`, `methods` and `fields`.
Generated names could not match ones from the original source in some targets. Also minimizing/obfuscating names could lead to totally different names.

## Syntax

Minitemplates allow you to use tags and expressions.

Basic syntax:
{% highlight django %}{% raw %}
{% tag ... %}                - A self-closed tag
{% tag ... %}...{% end %}    - A tag that opens and then ends (and can contain other meaningful tags inside)
{{ expression }}             - A expression that evaluates
{% endraw %}{% endhighlight %}

## Expressions:

Expressions are like other's language expressions. In there you can use normal unary and binary operators, access fields/elements with `.` or `[]` just like in ecmascript based languages.
And there is an addition called filters. Filters are in postfix positions and are similar to extension methods, and are denoted with pipe `|` symbol. So for example the upper filter could be used like this `{{ "mystring"|upper }}`.
You can have literals: strings, numbers and arrays.
Also you can reference variables available in its current context. Inside the `for` tag you can access the iteration variable.

## Available Standard Tags:

<pre>{% raw %}
{% if expression %}...{% end %}                 - Just evaluates fragment inside tag in the case expression evaluates as truthful
{% if expression %}...{% else %}...{% end %}    - Evaluates if or else blocks depending on the expression result
{% for var in expression %}...{% end %}         - Loops over an iterable expression and holds each value in the specified var
{% assign var = expression %}                   - Assigns the result of an expression to a variable
{% debug expression %}                          - Outputs debug information
{% endraw %}</pre>

## Tags to statically reference classes, fields and methods

<pre>{% raw %}
{% SINIT fqname.to.Class %}                   - Replaces with a complete statement with a static initializer for a class. You must call this before calling or accessing static methods and fields.
{% CONSTRUCTOR fqname.to.Class:descriptor %}  - Replaces with a class instantiation code for a specific constructor, requires appending brackets later.
{% CLASS fqname.to.Class %}                   - Replaces with the fully qualified name of the class.
{% METHOD fqname.to.Class:name:descriptor %}  - Replaces with the method name.
{% FIELD fqname.to.Class:name:descriptor %}   - Replaces with the field name.
{% IMETHOD fqname.to.Class:name:descriptor %}  - Replaces with '.' plus the method name (in the case of javascript can be replaced with 'name' too).
{% IFIELD fqname.to.Class:name:descriptor %}   - Replaces with '.' plus the field name (in the case of javascript can be replaced with 'name' too).
{% SMETHOD fqname.to.Class:name:descriptor %} - It is a shortcut for CLASS + METHOD tags for calling static methods.
{% SFIELD fqname.to.Class:name:descriptor %}  - It is a shortcut for CLASS + FIELD tags for accessing static fields.
{% endraw %}</pre>

**Note:** On METHOD and SMETHOD descriptor is optional when there is just one single method overloading. Otherwise a method descriptor looks like: `(II)Ljava/lang/String;` and you can find more about [Method Descriptors](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3) in the java specification.

An example using this for Haxe generated code could be:

<pre>{% raw %}
static public function init() {
    {% SINIT com.jtransc.media.JTranscInput %}
}

private function mouseInfo() return {% SFIELD com.jtransc.media.JTranscInput:mouseInfo %};

override public function onMouseUp(window:Window, x:Float, y:Float, button:Int):Void {
	mouseInfo(){% IMETHOD com.jtransc.media.JTranscInput$MouseInfo:setScreenXY %}(Std.int(x), Std.int(y));
	mouseInfo(){% IFIELD com.jtransc.media.JTranscInput$MouseInfo:buttons %} &= ~(1 << button);
	inputImpl(){% IMETHOD com.jtransc.media.JTranscInput$Handler:onMouseUp %}(mouseInfo());
}
{% endraw %}</pre>

## Available filters

<pre>{% raw %}
|length              - Evaluates to the length of a list or string
|upper               - Uppercases the whole string
|lower               - Lowercases the whole string
|capitalize          - Uppercases the first character of the string and lowercases the rest
|trim                - Strips starting and ending whitespace characters
|join('separator')   - Joins something iterable with the specified separator
|file_exists('file') - Evaluates to true in the case the specified file exists
|quote               - Quotes a string "like \"this\""
|escape              - Escapes a string (like quotes but without wrapping within "") like \"this\"
{% endraw %}</pre>

## Available identifiers in templates

* `srcFolder`- Operating system full path where target sources (haxe for example) are generated. Example: `target/jtransc-haxe/src`
* `buildFolder` -  Operating system full path with the jtransc target folder. Example: `target/jtransc-haxe`
* `haxeExtraFlags` - A List<Pair<String, String>> with flags FLAG=VALUE. Example: `listOf("-lib" to "haxe-ws:0.0.6")`
* `haxeExtraDefines` - A List<String> with additional defines. Example: `listOf("analyzer=1")`
* `actualSubtarget` - An object representing the [actual subtarget](https://github.com/jtransc/jtransc/blob/master/jtransc-rt-core/src/com/jtransc/annotation/haxe/HaxeAddSubtarget.java) (that object contains name, alias)
* `outputFile` - Absolute Path to the expected output file Example: `target/program.js`
* `release` - Boolean indicating wether this is a release build or not. Example: `true`
* `debug` - Boolean indicating wether this is a debug build or not. Example: `false`
* `releasetype` - String indicating kind of release. Possible values: "release" or "debug". Example: `debug`
* `settings` - A [AstBuildSettings](https://github.com/jtransc/jtransc/blob/master/jtransc-core/src/com/jtransc/ast/ast.kt) object with the whole settings
* `title` - Specified title in maven configuration
* `name` - Specified name in maven configuration
* `package` - Specified package in maven configuration
* `version` - Specified version in maven configuration
* `company` - Specified company in maven configuration
* `initialWidth` - Specified initialWidth in maven configuration
* `initialHeight` - Specified initialHeightin maven configuration
* `orientation` - Specified orientation maven configuration. Possible values: (auto, portrait, landscape)
* `tempAssetsDir` - (Deprecated use mergedAssetsdir)
* `mergedAssetsDir` - Path to the temporal directory that contains all the assets merged together
* `embedResources` - Boolean indicating wether to embed resources or not
* `assets` - Folder containing assets
* `hasIcon` - Boolean indicating if there was specified an icon or not
* `icon` - Path to icon file
* `libraries` - List of target libraries to include
* `extra` - Map<String, String> containing all the extra defined configurations
* `JTRANSC_VERSION` - Version of JTransc eg. 0.6.0

After building sources, inside for example HaxeCustomMain:
* `entryPointFile` - File that holds the entrypoint
* `entryPointClass` - Fully qualified name for the entry point class

Specific targets can define custom template variables. To prevent them being outdated, please locate `CommonGenerator.params` field and find references:

![](/minitemplates/CommonGenerator_params_references.png)
