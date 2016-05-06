package com.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.haxe.*;

public class JTranscSystem {
	static double start = -1;

	@HaxeMethodBody("return N.getTime();")
	static public double fastTime() {
		return System.currentTimeMillis();
	}

	@HaxeMethodBodySys("Sys.sleep(p0 / 1000.0);")
	@HaxeMethodBody("var start = N.getTime(); while (N.getTime() - start < p0) { }") // BUSY WAIT!
	static public void sleep(double ms) {
		try {
			Thread.sleep((long) ms);
		} catch (Throwable t) {
		}
	}

	static public int stamp() {
		if (start < 0) start = fastTime();
		return (int) (fastTime() - start);
	}

	static public int elapsedTime(int first, int second) {
		// @TODO: Check overflow!
		return second - first;
	}

	@HaxeMethodBodyCpp("cpp.vm.Gc.enable(false);")
	@HaxeMethodBody("")
	static public void gcDisable() {
	}

	@HaxeMethodBodyCpp("cpp.vm.Gc.enable(true);")
	@HaxeMethodBody("")
	static public void gcEnable() {
	}

	@HaxeMethodBodyCpp("cpp.vm.Gc.compact();")
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
	@HaxeMethodBodySys("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isSys();

	@JTranscInline
	@HaxeMethodBodyCpp("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isCpp();

	@JTranscInline
	@HaxeMethodBodyCSharp("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isCsharp();

	@JTranscInline
	@HaxeMethodBodyJava("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isJava();

	@JTranscInline
	@HaxeMethodBodyJs("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isJs();

	@JTranscInline
	@HaxeMethodBodyFlash("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isSwf();

	@JTranscInline
	@HaxeMethodBodyNeko("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isNeko();

	@JTranscInline
	@HaxeMethodBodyPhp("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isPhp();

	@JTranscInline
	@HaxeMethodBodyPython("return true;")
	@HaxeMethodBody("return false;")
	native public static boolean isPython();

	@HaxeMethodBodySys("return HaxeNatives.str(Sys.systemName());")
	@HaxeMethodBodyJs("return HaxeNatives.str(untyped __js__(\"(typeof navigator != 'undefined' ? navigator.platform : process.platform)\"));")
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

	@HaxeMethodBody("return HaxeNatives.str('x86');")
	static public String getArch() {
		// x86, i386, ppc, sparc, arm
		return System.getProperty("os.arch");
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
		return isWindows() ? "\\" : "/";
	}

	public static String pathSeparator() {
		return isWindows() ? ";" : ":";
	}

	public static String lineSeparator() {
		return isWindows() ? "\r\n" : "\n";
	}

}
