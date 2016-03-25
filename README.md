JTRANSC
-------

![JTransc](extra/logo-256.png)

[![Build Status](https://secure.travis-ci.org/jtransc/jtransc.svg)](http://travis-ci.org/#!/jtransc/jtransc)

# What is this?

Jtransc (Java Trans Compiler) is an AOT (ahead of time compiler) that compiles .class and .jar files
into a target executable file bundling all the required dependencies in a single file, without requiring
a jitter or an external runtime.
At the beggining it generated as3 and javascript, but right now there is a single target: haxe.
This allows targeting js, as3, c++, c#, java, php and python. This in turn allows running the program on different platforms such as desktop, browsers and mobile.

The aim of this project is to bring the high productivity of Kotlin (and other JVM languages)
to the highly portable haxe platform and other direct targets in the future.
It already supports some APIs and plain Java reflection API out of the box.

The initial focus is on jvm6, kotlin, haxe and games, but it will get better in the future supporting newer jvm versions,
and other use cases like frontend and backend applications.

There is a module called jtransc-rt-core that could be included in any project (whether using jtransc or not).
It provides the jtransc package,  specific annotations, rendering, fast memory access and asynchronous APIs,
that will use jtransc features when compiling using jtransc.

# How to use it?

You can find examples here [jtransc/jtransc-examples](https://github.com/jtransc/jtransc-examples).
*Please note* that it is possible that you will have to `mvn install` jtransc (this project) before
packaging examples if it has been not uploaded to sonatype already.

## Plain:
```
# jtransc script can be found under the jtransc-main-run folder
javac com/test/Main.java -d target/classes
jtransc dependency.jar target/classes -main com.test.Main -out program.js
node program.js
```

## Maven:

You can search for artifacts for it in maven central with [com.jtransc groupId](http://search.maven.org/#search%7Cga%7C1%7Cg%3Acom.jtransc).

`pom.xml` file should include:

```
<plugins>
    <plugin>
        <groupId>com.jtransc</groupId>
        <artifactId>jtransc-maven-plugin</artifactId>
        <version>0.1.5</version>
        <configuration>
			<mainClass>example.Test</mainClass>
			<targets>
				<param>lime:swf</param>
				<param>lime:js</param>
				<param>lime:neko</param>
				<param>lime:android</param>
			</targets>
			<release>true</release>
        </configuration>
        <executions><execution><goals><goal>jtransc</goal></goals></execution></executions>
    </plugin>
</plugins>

```

```
mvn package # it should generate program.swf
```

# How does it work internally?

* It locates all the required dependencies
* It includes jtransc-rt-core and jtransc-rt which is a java-6-like rt with some of their methods marked as natives
* Other dependencies than the RT are included without modifications
* It uses ASM to generate a class-method-statement-expression AST
    * That AST is easily serializable
    * That AST allows feature stripping
    * Your target language don't support gotos? It will generate an AST without gotos. Just plain if/while/switch...
* It generates your target source code, replacing some classes like String, ArrayList and so on, to make them fast in your target language.
* It joins or compiles that code into your final runnable program

## Sponsored by:

![Akamon Entertainment](extra/akamon.png)
