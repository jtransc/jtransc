```
jtransc <list of class paths or jar files>

Performs an aot compilation that transform a java/kotlin compiled program (class and jar files)
into an executable file (bin, bin) file at the moment.

  -main   <fqname> - Specifies class with static void main method that will be the entry point of the app
  -target <target> - Language target to do the AOT possible values (haxe, lime)
  -out    <file>   - Output file that will hold the generated aot result file
  -release         - Optimizes and performs compression minimization to the output

  -run             - Runs generated executable

  -help            - Displays help
  -v               - Verbose

Examples:

  jtransc dependency.jar target/classes -main com.test.Main -out program.js
  jtransc dependency.jar target/classes -main com.test.Main -target as3 -out program.swf
```