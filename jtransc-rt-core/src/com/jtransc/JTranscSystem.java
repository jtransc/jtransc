package com.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.time.JTranscClock;

public class JTranscSystem {
	// Try to avoid static analysis notifying that is** functions are constants.
	static private final boolean TRUE = true;
	static private final boolean FALSE = false;

	static double start = -1;

	static public double fastTime() {
		return JTranscClock.impl.fastTime();
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

	@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.enable(false);")
	@HaxeMethodBody("")
	@JTranscMethodBody(target = "js", value = "")
	static public void gcDisable() {
	}

	@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.enable(true);")
	@HaxeMethodBody("")
	@JTranscMethodBody(target = "js", value = "")
	static public void gcEnable() {
	}

	@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.compact();")
	@HaxeMethodBody("")
	@JTranscMethodBody(target = "js", value = "")
	static public void gc() {
		System.gc();
	}

	@HaxeMethodBody("return true;")
	@JTranscMethodBody(target = "js", value = "return true;")
	@JTranscMethodBody(target = "cpp", value = "return true;")
	@SuppressWarnings("all")
	static public boolean usingJTransc() {
		return FALSE;
	}

	// Alias for consistency
	static public boolean isJTransc() {
		return usingJTransc();
	}

	@JTranscInline
	@HaxeMethodBody("HaxeNatives.debugger();")
	@JTranscMethodBody(target = "js", value = "debugger;")
	static public void debugger() {
		//System.out.println("debugger");
		//throw new Error("Debugger");
	}

	/**
	 * Assertion for debug builds
	 *
	 * @param trueCond
	 */
	@JTranscInline
	@HaxeMethodBody("if (!p0) HaxeNatives.debugger();")
	@JTranscMethodBody(target = "js", value = "if (!p0) debugger;")
	static public void assert2(boolean trueCond) {
		if (!trueCond) {
			System.out.println("debugger");
			throw new Error("Debugger");
		}
	}

	@SuppressWarnings("all")
	static public String getRuntimeKind() {
		if (!usingJTransc()) return "java";
		if (isJs()) return "js";
		if (isSwf()) return "swf";
		if (isJvm()) return "java";
		if (isCsharp()) return "csharp";
		if (isCpp()) return "cpp";
		if (isNeko()) return "neko";
		if (isPhp()) return "php";
		if (isPython()) return "python";
		return "unknown";
	}

	@JTranscInline
	@HaxeMethodBody("return true;")
	public static boolean isHaxe() {
		return FALSE;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isPureJs() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "sys", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	@JTranscMethodBody(target = "cpp", value = "return true;")
	public static boolean isSys() {
		return TRUE;
	}

	@JTranscInline
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	@JTranscMethodBody(target = "cpp", value = "return true;")
	public static boolean isCpp() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "cs", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isCsharp() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "java", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	@JTranscMethodBody(target = "cpp", value = "return false;")
	public static boolean isJvm() {
		return TRUE;
	}

	public static boolean isJava() {
		return isJvm();
	}

	@JTranscInline
	@HaxeMethodBody(target = "js", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isJs() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "flash", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	public static boolean isSwf() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "neko", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	public static boolean isNeko() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "php", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	public static boolean isPhp() {
		return FALSE;
	}

	@JTranscInline
	@HaxeMethodBody(target = "python", value = "return true;")
	@HaxeMethodBody("return false;")
	@JTranscMethodBody(target = "js", value = "return false;")
	public static boolean isPython() {
		return FALSE;
	}

	@HaxeMethodBody(target = "sys", value = "return HaxeNatives.str(Sys.systemName());")
	@HaxeMethodBody(target = "js", value = "return HaxeNatives.str(untyped __js__(\"(typeof navigator != 'undefined' ? navigator.platform : process.platform)\"));")
	@HaxeMethodBody("return HaxeNatives.str('unknown');")
	@JTranscMethodBody(target = "js", value = "return N.str(typeof navigator != 'undefined' ? navigator.platform : process.platform);")
	@JTranscMethodBody(target = "cpp", value = "return N::str(L\"unknown\");")
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
	@HaxeMethodBody("return HaxeNatives.str('x86');")
	@JTranscMethodBody(target = "js", value = "return N.str('x86');")
	@JTranscMethodBody(target = "cpp", value = "return N::str(L\"x86\");")
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
			return (System.getenv("ProgramFiles(x86)") != null);
		} else {
			return (System.getProperty("os.arch").indexOf("64") != -1);
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
	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isDebug() {
		return FALSE;
	}

	@HaxeMethodBody("return N.str('jtransc-haxe');")
	@JTranscMethodBody(target = "js", value = "return N.str('jtransc-js');")
	@JTranscMethodBody(target = "cpp", value = "return N::str(L\"jtransc-cpp\");")
	public static String getRuntimeName() {
		return "java";
	}

	@HaxeMethodBody("return N.str('/jtransc-haxe');")
	@JTranscMethodBody(target = "js", value = "return N.str('/jtransc-js');")
	@JTranscMethodBody(target = "cpp", value = "return N::str(L\"/\");")
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

}
