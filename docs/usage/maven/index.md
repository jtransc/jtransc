---
layout: default
title: Maven
---

<img src="/usage/maven/maven-logo-black-on-white.png" width="auto" height="96" />

Maven plugin code can be found here:
[https://github.com/jtransc/jtransc/blob/master/jtransc-maven-plugin/src/com/jtransc/maven/mojo.kt](https://github.com/jtransc/jtransc/blob/master/jtransc-maven-plugin/src/com/jtransc/maven/mojo.kt)

And the way to use it is to add this plugin to your `pom.xml` file, which hooks the package phase, so you can build it with `mvn package`. Generated output will be in the `target` folder, and when using gdx-backend-jtransc in `target/jtransc-haxe/export/<debug|release>/<target>/bin`:

## Example:
{% highlight xml %}
<plugins>
    <plugin>
        <groupId>com.jtransc</groupId>
        <artifactId>jtransc-maven-plugin</artifactId>
        <version>${jtransc.version}</version>
        <configuration>
            <mainClass>example.Test</mainClass>
            <targets>
                <param>haxe:js</param>
            </targets>
            <release>true</release>
            <minimizeNames>false</minimizeNames>
        </configuration>
        <executions><execution><goals><goal>jtransc</goal></goals></execution></executions>
    </plugin>
</plugins>
{% endhighlight %}

There are several configuration options you can add to the plugin:

## Mandatory:
* `target` allow to specify the output target. Example: `<target>haxe:js</target>`
* `targets` allow to specify several output targets. Example: `<targets><param>haxe:js</param><param>haxe:flash</param></targets>`
* `mainClass` the fully qualified class name containing a static main with the entry point for your application. Example: `<mainClass>com.jtransc.MyMainClass</mainClass>`

## Optional:
* `output` the file output name. Example: `<output>program.js</output>`
* `release` wether the release is release or not `<release>true</release>`. Default: **false**
* `title` title to be used when packaging (javascript title, apk/ipa title and so on). Example: `<title>My Awesome Game</title>`.
* `name` short name to be used when packaging. Example: `<name>MyGame</name>`.
* `company` company name to be used when packaging. Example: `<company>JTransc</company>`
* `packagePath` package of the application. Example: `<packagePath>com.jtransc.examples</packagePath>`
* `relooper` instructs to use a relooper algorithm to try to improve code quality. Experimental. Example: `<relooper>true</relooper>` Default: false
* `analyzer` instructs to use an external analyzer on some targets that could improve code size and quality. (Haxe analyzer). Experimental. Example: `<analyzer>true</analyzer>` Default: false
* `minimizeNames` instructs the compiler to generate minimized names for packages, classes, fields and methods. This reduces output size on some targets, specially javascript. But on others difficults incremental compilation as names could change between compilations. Example: `<minimizeNames>true</minimizeNames>`
* `assets` a list of folders to include along the application as assets/resources. Example: `<assets><param>path/to/assets</param></assets>`
* `extra` a custom map including key/value String pairs that are available at MiniTemplates places. Example: `<extra><key1>value1</key1><key2>value2</key2><certificatePath>path/to/certificate.cer</certificatePath></extra>`

## Optional for graphic apps:
Devices/platforms supporting multiple screen resolutions or windowed applications.
Ignored on non-graphical applications.
These options are available from Minitemplates so any engine can use this information when building.

* `initialWidth` initial width of the window. Example: `<initialWidth>1280</initialWidth>`.
* `initialHeight` initial height of the window. Example: `<initialHeight>720</initialHeight>`.
* `orientation` orientation of the device Possible values: auto, portrait, landscape. Example: `<orientation>landscape</orientation>`. Default: auto
* `fullscreen` run the application in fullscreen. Example: `<fullscreen>true</fullscreen>`. Default: false
* `borderless` the window doesn't have border. Example: `<borderless>true</borderless>`. Default: false
* `resizable` wether the application window can be resized or not on supported platforms. Example: <resizable>true</resizable>. Default: false.
* `vsync` wether the application should have vertical synchronization. Example: <vsync>false</vsync>. Default: true.
* `embedResources` instructs to embed all resources instead of reading them externally. This would allow a JavaScript target to be able to access resources synchronously with a preloader. Example: `<embedResources>true</embedResources>` Default: false
* `icon` path to the application's icon. It will be resized when required. It should be available as a resource. `<icon>path/to/myicon.png</icon>`

## Deprecated (optional):
* `library` target libraries that will be included in the output Example: `<library><param>haxe-ws:0.0.6</param></library>` **Deprecated** The recommended way is to use `@HaxeAddLibraries` annotation.
* `version` string to be used as version. Example: `<version>0.0.1-BETA</version>` **Deprecated** use project's version instead.
* `backend` specify the backend to use right now the only supported backend is ASM (some time ago SOOT was supported too). **Deprecated**
