package com.jtransc.io;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.*;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyList;

import java.io.ObjectInputStream;
import java.util.Objects;

@HaxeAddMembers({"" +
	"static public function _log(p0:Dynamic) {\n" +
	"  var msg = '' + p0;\n" +
	"  #if js var _msg = msg; untyped __js__(\"console.log(_msg);\");\n" +
	"  #elseif sys Sys.stdout().writeString(msg + \"\\n\");\n" +
	"  #else trace(msg);\n" +
	"  #end\n" +
	"}\n"
})
public class JTranscConsole {
	@JTranscSync
	static public void log(char[] v) {
		logString(new String(v));
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo ($p0 !== null) ? \"$p0\" : 'null', \"\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log(N.istr(p0));"),
		@JTranscMethodBody(target = "cpp", value = "N::log(p0 ? p0->{% METHOD java.lang.Object:toString %}() : N::str(std::wstring(L\"null\")));"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "cs", value = "Console.WriteLine((p0 != null) ? p0.ToString() : \"null\");"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	@JTranscSync
	static public void logString(String v) {
		JTranscSystem.checkInJVM("logObject");
		System.out.println(v);
	}

	//@HaxeMethodBody("_log(p0);")
	//@JTranscMethodBodyList({
	//	@JTranscMethodBody(target = "php", value = "echo ($p0 !== null) ? \"$p0\" : 'null', \"\\n\";"),
	//	@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
	//	@JTranscMethodBody(target = "cpp", value = "N::log(p0 ? p0->{% METHOD java.lang.Object:toString %}() : N::str(std::wstring(L\"null\")));"),
	//	@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
	//	@JTranscMethodBody(target = "cs", value = "Console.WriteLine((p0 != null) ? p0.ToString() : \"null\");"),
	//	@JTranscMethodBody(target = "as3", value = "trace(p0);"),
	//	@JTranscMethodBody(target = "dart", value = "print(p0);"),
	//})
	@JTranscAsync
	static public void log(Object v) {
		logString(Objects.toString(v));
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log(p0);"),
		@JTranscMethodBody(target = "cpp", value = "N::log(p0 ? p0->{% METHOD java.lang.Object:toString %}() : N::str(std::wstring(L\"null\")));"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "cs", value = "Console.WriteLine((p0 != null) ? p0.ToString() : \"null\");"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	@JTranscAsync
	static public void dump(Object v) {
		log(v);
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo ($p0 ? 'true' : 'false') . \"\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
		@JTranscMethodBody(target = "cpp", value = "N::log(p0 ? L\"true\" : L\"false\");"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(boolean v) {
		log(Boolean.toString(v));
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
		@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%d\\n\", (int32_t)p0); fflush(stdout);"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "cs", value = "Console.WriteLine(p0);"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(byte v) {
		log(Byte.toString(v));
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
		@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%d\\n\", (int32_t)p0); fflush(stdout);"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "cs", value = "Console.WriteLine(p0);"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(short v) {
		log(Short.toString(v));
	}

	@HaxeMethodBody("_log(N.ichar(p0));")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo chr($p0), \"\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log(N.ichar(p0));"),
		@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%lc\\n\", (wchar_t)p0); fflush(stdout);"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%s\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "cs", value = "Console.WriteLine((char)p0);"),
		@JTranscMethodBody(target = "as3", value = "trace(N.ichar(p0));"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(char v) {
		log(Character.toString(v));
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
		@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%d\\n\", (int32_t)p0); fflush(stdout);"),
		@JTranscMethodBody(target = "d", value = "writefln(\"%d\", p0); std.stdio.stdout.flush();"),
		@JTranscMethodBody(target = "cs", value = "Console.WriteLine(p0);"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(int v) {
		log(Integer.toString(v));
	}

	@SuppressWarnings("PointlessBitwiseExpression")
	@HaxeMethodBody("_log('Int64(' + N.lhigh(p0) + ',' + N.llow(p0) + ')');")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "console.log('Int64(' + N.lhigh(p0) + ',' + N.llow(p0) + ')');"),
		@JTranscMethodBody(target = "as3", value = "trace('Int64(' + N.lhigh(p0) + ',' + N.llow(p0) + ')');"),
	})
	static public void log(long v) {
		logLong((int) (v >> 32), (int) (v >> 0));
	}

	static public void logLong(int high, int low) {
		log("Int64(" + high + "," + low + ")");
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
		@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%f\\n\", (float32_t)p0); fflush(stdout);"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(float v) {
		JTranscSystem.checkInJVM("logFloat");
		System.out.println(v);
	}

	@HaxeMethodBody("_log(p0);")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.log({{ AWAIT }} N.asyncAsyncStr({{ JC_COMMA }}p0));"),
		@JTranscMethodBody(target = "cpp", value = "wprintf(L\"%llf\\n\", (float64_t)p0); fflush(stdout);"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	static public void log(double v) {
		JTranscSystem.checkInJVM("logDouble");
		System.out.println(v);
	}

	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "js", value = "var _msg = '' + p0; untyped __js__(\"console.error(_msg);\");"),
		@HaxeMethodBody(target = "sys", value = "var msg = '' + p0; Sys.stderr().writeString(msg + \"\\n\");"),
		@HaxeMethodBody("trace('' + p0);"),
	})
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
		@JTranscMethodBody(target = "js", value = "console.error(N.istr(p0));"),
		@JTranscMethodBody(target = "cs", value = "Console.Error.WriteLine(p0);"),
		@JTranscMethodBody(target = "as3", value = "trace(p0);"),
		@JTranscMethodBody(target = "dart", value = "print(p0);"),
	})
	@JTranscSync
	static public void errorString(String msg) {
		JTranscSystem.checkInJVM("logError");
		System.err.println(msg);
	}

	@JTranscSync
	static public void error(String msg) {
		errorString(msg);
	}

	//@HaxeMethodBodyList({
	//	@HaxeMethodBody(target = "js", value = "var _msg = '' + p0; untyped __js__(\"console.error(_msg);\");"),
	//	@HaxeMethodBody(target = "sys", value = "var msg = '' + p0; Sys.stderr().writeString(msg + \"\\n\");"),
	//	@HaxeMethodBody("trace('' + p0);"),
	//})
	//@JTranscMethodBodyList({
	//	@JTranscMethodBody(target = "php", value = "echo \"$p0\\n\";"),
	//	@JTranscMethodBody(target = "js", value = "console.error('' + p0);"),
	//	@JTranscMethodBody(target = "cs", value = "Console.Error.WriteLine(p0);"),
	//	@JTranscMethodBody(target = "as3", value = "trace(p0);"),
	//	@JTranscMethodBody(target = "dart", value = "print(p0);"),
	//})
	@JTranscAsync
	static public void error(Object msg) {
		errorString(Objects.toString(msg));
	}

	static public void logOrError(Object msg, boolean error) {
		if (error) {
			JTranscConsole.error(msg);
		} else {
			JTranscConsole.log(msg);
		}
	}

	@JTranscSync
	public static void syncPrintStackTrace(Throwable e) {
		error("JTranscConsole.printStackTrace:");
		error(e.getMessage());
	}
}

/*
new PrintStream(new OutputStream() {
		@Override
		@HaxeMethodBody("N.outputChar(p0);")
		native public void write(int b) throws IOException;
	});new PrintStream(new OutputStream() {
		@Override
		@HaxeMethodBody("N.outputErrorChar(p0);")
		native public void write(int b) throws IOException;
	});
 */
