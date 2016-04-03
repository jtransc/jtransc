package jtransc.io;

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({"" +
	"static public function _log(p0:Dynamic) {\n" +
	"  var msg = '' + p0;\n" +
	"  #if js var _msg = msg; untyped __js__(\"console.log(_msg);\");\n" +
	"  #elseif sys Sys.stdout().writeString(msg + \"\\n\");\n" +
	"  #else  trace(msg);\n" +
	"  #end\n" +
	"}\n"
})
public class JTranscConsole {
	@HaxeMethodBody("_log(p0);")
	static public void log(Object obj) {
		System.out.println(obj);
	}

	@HaxeMethodBody("_log(p0);")
	static public void log(int i) {
		System.out.println(i);
	}

	@HaxeMethodBody("" +
		"var msg = '' + p0;\n" +
		"#if js  var _msg = msg; untyped __js__(\"console.error(_msg);\");\n" +
		"#elseif sys Sys.stderr().writeString(msg + \"\\n\");\n" +
		"#else trace(msg);\n" +
		"#end\n"
	)
	static public void error(Object msg) {
		System.err.println(msg);
	}

	static public void logOrError(Object msg, boolean error) {
		if (error) {
			JTranscConsole.error(msg);
		} else {
			JTranscConsole.log(msg);
		}
	}
}

/*
new PrintStream(new OutputStream() {
		@Override
		@HaxeMethodBody("HaxeNatives.outputChar(p0);")
		native public void write(int b) throws IOException;
	});new PrintStream(new OutputStream() {
		@Override
		@HaxeMethodBody("HaxeNatives.outputErrorChar(p0);")
		native public void write(int b) throws IOException;
	});
 */
