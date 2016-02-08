JTRANSC
-------

# What is this?
Jtransc is AOT (an ahead of time compiler) that compiles .class and .jar files into a target executable file bundling
all the required dependencies in a single file and without requiring a jitter or an external runtime.
At this point it generates: flash swf files, javascript

# Name meaning?
Jtransc - Java Trans Compiler

# How to use?

## Plain:
```
# jtransc script can be found under the jtransc-main-run folder
javac com/test/Main.java -d target/classes
jtransc dependency.jar target/classes -main com.test.Main -out program.js
node program.js
```

## Maven:
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

* It locates all the required dependencies.
* It includes the jtransc-rt that is a java-6-like rt with most of their methods marked as natives
* Depending on your target it includes for example: jtransc-rt-as3, that allows you to use flash API in your java/kotlin/wathever code
* Other dependencies than the RT are includes without modifications
* It uses ASM and/or Soot to generate a class-method-statement-expression AST
    * That AST is easily serializable
    * That AST allows feature stripping
    * Your target language don't support gotos? It will generate an AST without gotos. Just plain if/while/switch...
* It generates your target source code, replacing some classes like String, ArrayList and so on, to make them fast in your target language.
* It joins or compiles that code into your final runnable program

