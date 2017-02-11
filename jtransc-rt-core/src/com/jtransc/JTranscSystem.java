package com.jtransc;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyList;
import com.jtransc.target.Js;
import com.jtransc.time.JTranscClock;

@SuppressWarnings("WeakerAccess")
public class JTranscSystem {
	// Try to avoid static analysis notifying that is** functions are constants.
	static private final boolean TRUE = true;
	static private final boolean FALSE = false;

	static double start = -1;

	static public double fastTime() {
		return JTranscClock.impl.fastTime();
	}

	static public long nanoTime() {
		return JTranscClock.impl.nanoTime();
	}

	static public void sleep(double ms) {
		JTranscClock.impl.sleep(ms);
	}

	static public double stamp() {
		if (start < 0) start = fastTime();
		return elapsedTime(start, fastTime());
	}

	static public int elapsedTime(int first, int second) {
		// @TODO: Check overflow!
		return second - first;
	}

	static public double elapsedTime(double first, double second) {
		// @TODO: Check overflow!
		return second - first;
	}

	@JTranscMethodBody(target = "js", value = "")
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.enable(false);"),
		@HaxeMethodBody(target = "d", value = ""),
		@HaxeMethodBody(""),
	})
	static public void gcDisable() {
	}

	@JTranscMethodBody(target = "js", value = "")
	@HaxeMethodBodyList({
		@HaxeMethodBody(""),
		@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.enable(true);"),
		@HaxeMethodBody(target = "d", value = ""),
	})
	static public void gcEnable() {
	}

	@JTranscMethodBody(target = "js", value = "")
	@HaxeMethodBodyList({
		@HaxeMethodBody(""),
		@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.compact();"),
		@HaxeMethodBody(target = "d", value = ""),
		@HaxeMethodBody(target = "cs", value = "System.GC.Collect()"),
	})
	static public void gc() {
		System.gc();
	}

	@HaxeMethodBody("return true;")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return true;"),
		@JTranscMethodBody(target = "cpp", value = "return true;"),
		@JTranscMethodBody(target = "d", value = "return true;"),
		@JTranscMethodBody(target = "cs", value = "return true;"),
	})
	@SuppressWarnings("all")
	static public boolean usingJTransc() {
		return FALSE;
	}

	// Alias for consistency
	static public boolean isJTransc() {
		return usingJTransc();
	}

	@HaxeMethodBody("N.debugger();")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "debugger;"),
		@JTranscMethodBody(target = "cs", value = "System.Diagnostics.Debugger.Break();"),
	})
	static public void debugger() {
		//System.out.println("debugger");
		//throw new Error("Debugger");
	}

	@HaxeMethodBody("if (!p0) N.debugger();")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "if (!p0) debugger;"),
		@JTranscMethodBody(target = "cs", value = "if (!pc) System.Diagnostics.Debugger.Break();"),
	})
	static public void assert2(boolean trueCond) {
		if (!trueCond) {
			System.out.println("debugger");
			throw new Error("Debugger");
		}
	}

	@SuppressWarnings("all")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return N.str(\"js\");"),
		@JTranscMethodBody(target = "cpp", value = "return N::str(\"cpp\");"),
		@JTranscMethodBody(target = "d", value = "return N.str(\"d\");"),
		@JTranscMethodBody(target = "cs", value = "return N.str(\"csharp\");"),
	})
	static public String getRuntimeKind() {
		if (!usingJTransc()) return "java";
		if (isSwf()) return "swf";
		if (isJvm()) return "java";
		if (isCsharp()) return "csharp";
		if (isNeko()) return "neko";
		if (isPhp()) return "php";
		if (isPython()) return "python";
		return "unknown";
	}

	@HaxeMethodBody("return true;")
	public static boolean isHaxe() {
		return FALSE;
	}

	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isPureJs() {
		return FALSE;
	}

	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "sys", value = "return true;"),
		@HaxeMethodBody("return false;"),
	})
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return false;"),
		@JTranscMethodBody(target = "cpp", value = "return true;"),
		@JTranscMethodBody(target = "cs", value = "return true;"),
	})
	public static boolean isSys() {
		return TRUE;
	}

	@JTranscMethodBody(target = "cpp", value = "return true;")
	public static boolean isCpp() {
		return FALSE;
	}

	@JTranscMethodBody(target = "d", value = "return true;")
	public static boolean isD() {
		return FALSE;
	}

	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "cs", value = "return true;"),
		@HaxeMethodBody("return false;"),
	})
	@JTranscMethodBody(target = "cs", value = "return true;")
	public static boolean isCsharp() {
		return FALSE;
	}

	public static boolean isJvm() {
		return !isJTransc();
	}

	public static boolean isJava() {
		return isJvm();
	}

	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "js", value = "return true;"),
		@HaxeMethodBody("return false;"),
	})
	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isJs() {
		return FALSE;
	}

	public static boolean isJsNode() {
		if (isJs()) {
			return Js.z_raw("(typeof module !== 'undefined' && module.exports) != false");
		} else {
			return FALSE;
		}
	}

	public static boolean isJsBrowser() {
		if (isJs()) {
			return Js.z_raw("typeof window != \"undefined\"");
		} else {
			return FALSE;
		}
	}

	@HaxeMethodBody(target = "flash", value = "return true;")
	public static boolean isSwf() {
		return FALSE;
	}

	@HaxeMethodBody(target = "neko", value = "return true;")
	public static boolean isNeko() {
		return FALSE;
	}

	@HaxeMethodBody(target = "php", value = "return true;")
	public static boolean isPhp() {
		return FALSE;
	}

	@HaxeMethodBody(target = "python", value = "return true;")
	public static boolean isPython() {
		return FALSE;
	}

	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "sys", value = "return N.str(Sys.systemName());"),
		@HaxeMethodBody(target = "js", value = "return N.str(untyped __js__(\"(typeof navigator != 'undefined' ? navigator.platform : process.platform)\"));"),
		@HaxeMethodBody("return N.str('unknown');"),
	})
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return N.str(typeof navigator != 'undefined' ? navigator.platform : process.platform);"),
		@JTranscMethodBody(target = "cpp", value = "return N::str(L\"unknown\");"),
		@JTranscMethodBody(target = "d", value = "return N.str(N.getOS());"),
		@JTranscMethodBody(target = "cs", value = "return N.str(System.Environment.OSVersion.Platform.ToString());"),
	})
	static private String getOSRaw() {
		return System.getProperty("os.name");
	}

	static public String getOS() {
		String os = getOSRaw().toLowerCase();
		if (os.startsWith("win")) return "windows";
		if (os.startsWith("lin")) return "linux";
		if (os.startsWith("mac") || os.startsWith("osx")) return "mac";
		return os;
	}

	// http://lopica.sourceforge.net/os.html
	@HaxeMethodBody("return N.str('x86');")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return N.str('x86');"),
		@JTranscMethodBody(target = "cpp", value = "return N::str(L\"x86\");"),
	})
	static public String getArch() {
		// x86, i386, ppc, sparc, arm
		if (isJvm()) {
			return System.getProperty("os.arch");
		} else {
			return "unknown";
		}
	}

	public static boolean isOs32() {
		return !isOs64();
	}

	public static boolean isOs64() {
		if (System.getProperty("os.name").contains("Windows")) {
			return System.getenv("ProgramFiles(x86)") != null;
		} else {
			return System.getProperty("os.arch").contains("64");
		}
	}

	public static boolean isWindows() {
		return getOS().toLowerCase().startsWith("win");
	}

	public static boolean isLinux() {
		return getOS().toLowerCase().startsWith("linux");
	}

	public static boolean isMac() {
		return getOS().toLowerCase().startsWith("mac");
	}

	public static boolean isPosix() {
		return !isWindows();
	}

	public static boolean isNotWindows() {
		return !isWindows();
	}

	static public String getTimeZone() {
		return "GMT";
	}

	@HaxeMethodBody(target = "debug", value = "return true;")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return true;"),
		@JTranscMethodBody(target = "d", value = "debug { return true; } return false;"),
	})
	public static boolean isDebug() {
		return FALSE;
	}

	@HaxeMethodBody("return N.str('jtransc-haxe');")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return N.str('jtransc-js');"),
		@JTranscMethodBody(target = "cpp", value = "return N::str(L\"jtransc-cpp\");"),
		@JTranscMethodBody(target = "d", value = "return N.str(\"jtransc-d\");"),
	})
	public static String getRuntimeName() {
		return "java";
	}

	@HaxeMethodBody("return N.str('/jtransc-haxe');")
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return N.str('/jtransc-js');"),
		@JTranscMethodBody(target = "cpp", value = "return N::str(L\"/\");"),
		@JTranscMethodBody(target = "d", value = "return N.str(\"/\");"),
	})
	public static String getJavaHome() {
		return System.getenv("java.home");
	}

	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isEmulatedLong() {
		return FALSE;
	}


	public static String fileSeparator() {
		return JTranscSystemProperties.fileSeparator();
	}

	public static String pathSeparator() {
		return JTranscSystemProperties.pathSeparator();
	}

	public static String lineSeparator() {
		return JTranscSystemProperties.lineSeparator();
	}

	static public String getUserHome() {
		return JTranscSystemProperties.userHome();
	}

	static public void checkInJVM(String reason) {
		if (JTranscSystem.isJTransc()) {
			throw new RuntimeException("Not expected JTransc: " + reason);
		}
	}
}
