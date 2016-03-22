package jtransc.io;

import jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscConsole {
	@HaxeMethodBody(
		"var msg = p0._str;\n" +
		"#if js var _msg = msg; untyped __js__(\"console.log(_msg);\");\n" +
		"#elseif sys Sys.stdout().writeString(msg + \"\\n\");\n" +
		"#else  trace(msg);\n" +
		"#end\n"
	)
	static public void log(String msg) {
		System.out.println(msg);
	}

	@HaxeMethodBody(
		"var msg = p0._str;\n" +
		"#if js  var _msg = msg; untyped __js__(\"console.error(_msg);\");\n" +
		"#elseif sys Sys.stderr().writeString(msg + \"\\n\");\n" +
		"#else trace(msg);\n" +
		"#end\n"
	)
	static public void error(String msg) {
		System.err.println(msg);
	}

	static public void logOrError(String msg, boolean error) {
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
