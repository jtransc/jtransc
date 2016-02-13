## Plugin for Maven

`pom.xml` file should include:
```
<plugins>
    <plugin>
        <groupId>com.jtransc</groupId>
        <artifactId>jtransc-maven-plugin</artifactId>
        <version>0.0.4</version>
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
