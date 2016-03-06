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

    @HaxeMethodBody("HaxeNatives.gcDisable();")
    static public void gcDisable() {
    }

    @HaxeMethodBody("HaxeNatives.gcEnable();")
    static public void gcEnable() {
    }

    @HaxeMethodBody("HaxeNatives.gc();")
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
}
