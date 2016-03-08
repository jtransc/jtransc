package jtransc.io;

import jtransc.annotation.haxe.HaxeMethodBody;

public class JTranscConsole {
	@HaxeMethodBody("HaxeNatives.outputLog(p0._str);")
	static public void log(String msg) {
		System.out.println(msg);
	}

	@HaxeMethodBody("HaxeNatives.outputError(p0._str);")
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
