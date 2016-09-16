package com.jtransc.io;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.lang.Int64;

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
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "N::log(p0.get() ? p0->M_toString___Ljava_lang_String_() : N::str(std::wstring(L\"null\")));")
	static public void log(Object v) { System.out.println(v); }

	@JTranscMethodBody(target = "js", value = "console.log(p0);")
	@JTranscMethodBody(target = "cpp", value = "N::log(p0.get() ? p0->M_toString___Ljava_lang_String_() : N::str(std::wstring(L\"null\")));")
	static public void dump(Object v) { System.out.println(v); }

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "N::log(p0 ? L\"true\" : L\"false\");")
	static public void log(boolean v) { System.out.println(v); }

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%d\\n\", (int32_t)p0); fflush(stdout);")
	static public void log(byte v) { System.out.println(v); }

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%d\\n\", (int32_t)p0); fflush(stdout);")
	static public void log(short v) { System.out.println(v); }

	@HaxeMethodBody("_log(String.fromCharCode(p0));")
	@JTranscMethodBody(target = "js", value = "console.log(N.ichar(p0));")
	@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%lc\\n\", (wchar_t)p0); fflush(stdout);")
	static public void log(char v) { System.out.println(v); }

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%d\\n\", (int32_t)p0); fflush(stdout);")
	static public void log(int v) { System.out.println(v); }

	@HaxeMethodBody("_log('Int64(' + p0.high + ',' + p0.low + ')');")
	static public void log(long v) {
		if (JTranscSystem.isEmulatedLong()) {
			Int64 internal = Int64.getInternal(v);
			logLong(internal.high, internal.low);
		} else {
			logLong((int)(v >> 32), (int)(v >> 0));
		}
	}

	static public void logLong(int high, int low) {
		System.out.println("Int64(" + high + "," + low + ")");
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%f\\n\", (float32_t)p0); fflush(stdout);")
	static public void log(float v) { System.out.println(v); }

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBody(target = "js", value = "console.log('' + p0);")
	@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%llf\\n\", (float64_t)p0); fflush(stdout);")
	static public void log(double v) { System.out.println(v); }

	@HaxeMethodBody(target = "js", value = "var _msg = '' + p0; untyped __js__(\"console.error(_msg);\");")
	@HaxeMethodBody(target = "sys", value = "var msg = '' + p0; Sys.stderr().writeString(msg + \"\\n\");")
	@HaxeMethodBody("trace('' + p0);")
	@JTranscMethodBody(target = "js", value = "console.error('' + p0);")
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
