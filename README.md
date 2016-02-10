JTRANSC
-------

# What is this?

Jtransc (Java Trans Compiler) is an AOT (an ahead of time compiler) that compiles .class and .jar files
into a target executable file bundling all the required dependencies in a single file and without requiring
a jitter or an external runtime.
Initially it generated as3 and javascript, but now there is just a target: haxe.
Haxe allows to target js, as3, c++, c#, java, php and python. And runs on desktop, browsers and mobile.

The aim of this project is to bring high productivity of Kotlin (JVM language)
to the high portable haxe platform and other direct targets in the future.
It already supports some APIs and out of the box plain Java reflection API.

The initial focus is for jvm6, kotlin, haxe and games.
But it will get better in the future supporting newer jvm versions,
and other use cases like frontend and backend applications.

There is a module called jtransc-rt-core, that could be included in any project (using or not using jtransc).
That provides in jtransc package, specific annotations, rendering, fast memory access and asynchronous APIs,
that will use jtransc features when compiling using jtransc.

# How to use it?

You can found examples here [jtransc/jtransc-examples](https://github.com/jtransc/jtransc-examples).
*Please note* that it is possible that you will have to `mvn install` jtransc (this project) before
packaging examples if has been not uploaded to sonatype already.

## Plain:
```
# jtransc script can be found under the jtransc-main-run folder
javac com/test/Main.java -d target/classes
jtransc dependency.jar target/classes -main com.test.Main -out program.js
node program.js
```

## Maven:

`pom.xml` file should include:
```
<plugins>
    <plugin>
        <groupId>com.jtransc</groupId>
        <artifactId>jtransc-maven-plugin</artifactId>
        <version>0.0.1</version>
        <configuration>
            <mainClass>example.Test</mainClass>
            <output>program.swf</output>
            <release>true</release>
        </configuration>
        <executions><execution><goals><goal>build-as3</goal></goals></execution></executions>
    </plugin>
</plugins>

<pluginRepositories>
    <pluginRepository>
        <id>sonatype.oss.snapshots</id>
        <name>Sonatype OSS Snapshot Repository</name>
        <url>http://oss.sonatype.org/content/repositories/snapshots</url>
        <releases>
            <enabled>false</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </pluginRepository>
</pluginRepositories>

```

```
mvn package # it should generate program.swf
```

# How it works internally?

* It locates all the required dependencies
* It includes jtransc-rt-core and jtransc-rt which is a java-6-like rt with some of their methods marked as natives
* Other dependencies than the RT are includes without modifications
* It uses ASM and/or Soot to generate a class-method-statement-expression AST
    * That AST is easily serializable
    * That AST allows feature stripping
    * Your target language don't support gotos? It will generate an AST without gotos. Just plain if/while/switch...
* It generates your target source code, replacing some classes like String, ArrayList and so on, to make them fast in your target language.
* It joins or compiles that code into your final runnable program

## Sponsored by:

![Akamon Entertainment](extra/akamon.png)
