package com.jtransc;

import com.jtransc.annotation.JTranscMethodBody;


import com.jtransc.io.JTranscConsole;
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
	@JTranscMethodBody(target = "cpp", value = "__GC_DISABLE();")
	static public void gcDisable() {
	}

	@JTranscMethodBody(target = "js", value = "")
	@JTranscMethodBody(target = "cpp", value = "__GC_ENABLE();")
	static public void gcEnable() {
	}

	@JTranscMethodBody(target = "js", value = "")
	@JTranscMethodBody(target = "cpp", value = "__GC_GC();")
	static public void gc() {
		System.gc();
	}

	@JTranscMethodBody(target = "cpp", value = "__GC_SHOW_STATS();")
	@JTranscMethodBody(target = "js", value = "console.info((typeof window !== 'undefined' ? window.performance.memory : undefined) || require('v8').getHeapStatistics());")
	static public void gcStats() {
		JTranscConsole.log("JTranscSystem.gcStats not available");
	}


	@JTranscMethodBody(target = "js", value = "return true;")
	@JTranscMethodBody(target = "cpp", value = "return true;")
	@JTranscMethodBody(target = "cs", value = "return true;")
	@JTranscMethodBody(target = "dart", value = "return true;")
	@SuppressWarnings("all")
	static public boolean usingJTransc() {
		return FALSE;
	}

	// Alias for consistency
	static public boolean isJTransc() {
		return usingJTransc();
	}


	@JTranscMethodBody(target = "js", value = "debugger;")
	@JTranscMethodBody(target = "cs", value = "System.Diagnostics.Debugger.Break();")
	static public void debugger() {
		//System.out.println("debugger");
		//throw new Error("Debugger");
	}


	@JTranscMethodBody(target = "js", value = "if (!p0) debugger;")
	@JTranscMethodBody(target = "cs", value = "if (!p0) System.Diagnostics.Debugger.Break();")
	static public void assert2(boolean trueCond) {
		if (!trueCond) {
			JTranscConsole.logString("debugger");
			throw new Error("Debugger");
		}
	}

	@SuppressWarnings("all")
	@JTranscMethodBody(target = "js", value = "return N.str(\"js\");")
	@JTranscMethodBody(target = "cpp", value = "return N::str(L\"cpp\");")
	@JTranscMethodBody(target = "cs", value = "return N.str(\"csharp\");")
	static public String getRuntimeKind() {
		if (!usingJTransc()) return "java";
		if (isJvm()) return "java";
		if (isCsharp()) return "csharp";
		if (isDart()) return "dart";
		if (isCpp()) return "cpp";
		return "unknown";
	}

	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isPureJs() {
		return FALSE;
	}

	@JTranscMethodBody(target = "js", value = "return false;")
	@JTranscMethodBody(target = "cpp", value = "return true;")
	@JTranscMethodBody(target = "cs", value = "return true;")
	public static boolean isSys() {
		return TRUE;
	}

	@JTranscMethodBody(target = "cpp", value = "return true;")
	public static boolean isCpp() {
		return FALSE;
	}

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

	@JTranscMethodBody(target = "dart", value = "return true;")
	public static boolean isDart() {
		return FALSE;
	}

	@JTranscMethodBody(target = "js", value = "return N.str(typeof navigator != 'undefined' ? navigator.platform : process.platform);")
	@JTranscMethodBody(target = "cpp", value = "return N::str(JT_OS);")
	@JTranscMethodBody(target = "cs", value = "return N.str(System.Environment.OSVersion.Platform.ToString());")
	@JTranscMethodBody(target = "dart", value = "return N.str(Platform.operatingSystem);")
	static private String getOSRaw() {
		if (JTranscSystem.isJTransc()) {
			return "unknown-os";
		} else {
			return System.getProperty("os.name");
		}
	}

	static public String getOS() {
		String os = getOSRaw().toLowerCase();
		if (os.startsWith("win")) return "windows";
		if (os.startsWith("lin")) return "linux";
		if (os.startsWith("mac") || os.startsWith("osx")) return "mac";
		if (os.startsWith("fuch")) return "fuchsia";
		return os;
	}

	// http://lopica.sourceforge.net/os.html

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
			return System.getenv("ProgramFiles(x86)") != null;
		} else {
			return System.getProperty("os.arch").contains("64");
		}
	}

	public static boolean isFuchsia() {
		return getOS().toLowerCase().startsWith("fuch");
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


	@JTranscMethodBody(target = "js", value = "return true;")
	public static boolean isDebug() {
		return FALSE;
	}


	@JTranscMethodBody(target = "js", value = "return N.str('jtransc-js');")
	@JTranscMethodBody(target = "cpp", value = "return N::str(L\"jtransc-cpp\");")
	public static String getRuntimeName() {
		return "java";
	}


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

	static public void checkInJVM(String reason) {
		if (JTranscSystem.isJTransc()) {
			throw new RuntimeException("Not expected JTransc: " + reason);
		}
	}

	static public boolean hasEventLoop() {
		return isDart() || isJs();
	}
}
