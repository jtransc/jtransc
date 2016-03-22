package jtransc;

import jtransc.annotation.JTranscInline;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({
        "static private var __start = -1.0;",
        "static private function __stamp() { #if sys return Sys.time() * 1000; #else return Date.now().getTime(); #end }"
})
public class JTranscSystem {
    static long start = -1;

    @HaxeMethodBody("if (__start < 0) __start = __stamp(); return Std.int(__stamp() - __start);")
    static public int stamp() {
        if (start < 0) start = System.currentTimeMillis();
        return (int) (System.currentTimeMillis() - start);
    }

	static public int elapsedTime(int first, int second) {
		// @TODO: Check overflow!
		return second - first;
	}

    @HaxeMethodBody("#if cpp cpp.vm.Gc.enable(false); #end")
    static public void gcDisable() {
    }

    @HaxeMethodBody("#if cpp cpp.vm.Gc.enable(true); #end")
    static public void gcEnable() {
    }

    @HaxeMethodBody("#if cpp cpp.vm.Gc.compact(); #end")
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

	@HaxeMethodBody(
		"var out = 'unknown';\n" +
		"#if js out = 'js'; #end\n" +
		"#if swf out = 'swf'; #end\n" +
		"#if java out = 'java'; #end\n" +
		"#if cs out = 'cs'; #end\n" +
		"#if cpp out = 'cpp'; #end\n" +
		"#if neko out = 'neko'; #end\n" +
		"#if php out = 'php'; #end\n" +
		"#if php python = 'python'; #end\n" +
		"return HaxeNatives.str(out);\n"
	)
	static public String getRuntimeKind() {
		return "java";
	}

	@HaxeMethodBody(
		"#if sys return HaxeNatives.str(Sys.systemName());\n" +
		"#elseif js return HaxeNatives.str(untyped __js__(\"(typeof navigator != 'undefined' ? navigator.platform : process.platform)\"));\n" +
		"#else return HaxeNatives.str('unknown');\n" +
		"#end"
	)
	static private String getOSRaw() {
		//#if sys
		//return Sys.systemName();
		//#elseif js
		//return "js"; // Use node process or navigator
		//#elseif flash
		//return "flash";
		//#else
		//return "haxe";
		//#end
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
}
