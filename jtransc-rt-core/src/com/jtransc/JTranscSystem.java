package com.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.haxe.*;

import java.lang.instrument.Instrumentation;

public class JTranscSystem {
	static double start = -1;

	@HaxeMethodBody("return N.getTime();")
	static public double fastTime() {
		return System.currentTimeMillis();
	}

	@HaxeMethodBody(target = "sys", value = "Sys.sleep(p0 / 1000.0);")
	@HaxeMethodBody("var start = N.getTime(); while (N.getTime() - start < p0) { }") // BUSY WAIT!
	static public void sleep(double ms) {
		try {
			Thread.sleep((long) ms);
		} catch (Throwable t) {
		}
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
	static public void gcDisable() {
	}

	@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.enable(true);")
	@HaxeMethodBody("")
	static public void gcEnable() {
	}

	@HaxeMethodBody(target = "cpp", value = "cpp.vm.Gc.compact();")
	@HaxeMethodBody("")
	static public void gc() {
		System.gc();
	}

	@HaxeMethodBody("return true;")
	static public boolean usingJTransc() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody("HaxeNatives.debugger();")
	static public void debugger() {
		System.out.println("debugger");
		throw new Error("Debugger");
	}

	/**
	 * Assertion for debug builds
	 *
	 * @param trueCond
	 */
	@JTranscInline
	@HaxeMethodBody("if (!p0) HaxeNatives.debugger();")
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
		if (isJava()) return "java";
		if (isCsharp()) return "csharp";
		if (isCpp()) return "cpp";
		if (isNeko()) return "neko";
		if (isPhp()) return "php";
		if (isPython()) return "python";
		return "unknown";
	}

	@JTranscInline
	@HaxeMethodBody(target = "sys", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isSys() {
		return true;
	}

	@JTranscInline
	@HaxeMethodBody(target = "cpp", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isCpp() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody(target = "cs", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isCsharp() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody(target = "java", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isJava() {
		return true;
	}

	@JTranscInline
	@HaxeMethodBody(target = "js", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isJs() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody(target = "flash", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isSwf() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody(target = "neko", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isNeko() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody(target = "php", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isPhp() {
		return false;
	}

	@JTranscInline
	@HaxeMethodBody(target = "python", value = "return true;")
	@HaxeMethodBody("return false;")
	public static boolean isPython() {
		return false;
	}

	@HaxeMethodBody(target = "sys", value = "return HaxeNatives.str(Sys.systemName());")
	@HaxeMethodBody(target = "js", value = "return HaxeNatives.str(untyped __js__(\"(typeof navigator != 'undefined' ? navigator.platform : process.platform)\"));")
	@HaxeMethodBody("return HaxeNatives.str('unknown');")
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
	static public String getArch() {
		// x86, i386, ppc, sparc, arm
		return System.getProperty("os.arch");
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

	public static String fileSeparator() {
		//return isWindows() ? "\\" : "/";
		return "/";
	}

	public static String pathSeparator() {
		//return isWindows() ? ";" : ":";
		return ":";
	}

	public static String lineSeparator() {
		//return isWindows() ? "\r\n" : "\n";
		return "\n";
	}

	static public String getUserHome() {
		return System.getProperty("user.home");
	}

	public static boolean isPosix() {
		return !isWindows();
	}
}
