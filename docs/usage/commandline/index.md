---
layout: default
title: Command-line
---

You can use JTransc from the command line.

You can find more information here:
[https://github.com/jtransc/jtransc/tree/master/jtransc-main-run](https://github.com/jtransc/jtransc/tree/master/jtransc-main-run)

There is a `jtransc` script in `jtransc-main-run` module. You can call it directly.
Or you can use maven to execute jtransc from the command line too: `mvn -f /path/to/jtransc-main-run/pom.xml exec:java`

You can create a fatjar with dependencies:
`mvn package` at `jtransc-main-run` folder. It will create. `target/jtransc-main-run-VERSION-jar-with-dependencies.jar` and since that jar is executable you can execute jtransc with `java -jar jtransc.jar`.

{% highlight text %}
JTransc 0.2.5 - (C) 2016

jtransc <list of class paths or jar files>

Performs an aot compilation that transform a java/kotlin compiled program (class and jar files)
into an executable file (bin) file at the moment.

  -main   <fqname> - Specifies class with static void main method that will be the entry point of the app
  -target <target> - Language target to do the AOT possible values (haxe)
  -out    <file>   - Output file that will hold the generated aot result file
  -release         - Optimizes and performs compression minimization to the output

  -run             - Runs generated executable

  -help            - Displays help
  -version         - Displays jtransc version
  -status          - Generates a report of the jtransc runtime
  -debugenv        - Shows some environment debug variables for debug purposes
  -v               - Verbose

Examples:

  jtransc dependency.jar target/classes -main com.test.Main -out program.js
  jtransc dependency.jar target/classes -main com.test.Main -target as3 -out program.swf
{% endhighlight %}
