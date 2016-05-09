package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

class N {
	static private var strLitCache = new Map<String, {% CLASS java.lang.String %}>();

    static public function strLit(str:String):{% CLASS java.lang.String %} {
    	if (!strLitCache.exists(str)) strLitCache[str] = {% CLASS java.lang.String %}.make(str);
        return strLitCache[str];
    }

    static public function str(str:String):{% CLASS java.lang.String %} return (str != null) ? {% CLASS java.lang.String %}.make(str) : null;
    static public function i_str(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;

	static public inline function c<T, S> (value:T, c:Class<S>):S {
		return cast value;
		//return (value != null) ? cast value : null;
	}

	static inline private function _shift(count:Int) {
		#if php
			return untyped __php__("PHP_INT_SIZE") * 8 - count;
		#else
			return 32 - count;
		#end
	}

	static public function signExtend(v:Int, bits:Int):Int return (v << _shift(bits)) >> _shift(bits);
	static public function i2z(v:Int):Bool return v != 0;
	static public function i2b(v:Int):Int return (v << _shift(8)) >> _shift(8);
	static public function i2s(v:Int):Int return (v << _shift(16)) >> _shift(16);
	static public function i2c(v:Int):Int return v & 0xFFFF;

	static public function f2i(v:Float):Int {
		#if cpp
		return untyped __cpp__("((int)({0}))", v);
		#else
		return Std.int(v);
		#end
	}

	static public function int(v:Float):Int return f2i(v);

	static public function z2i(v:Bool):Int return v ? 1 : 0;

	static public function idiv(a:Int, b:Int):Int {
		#if cpp
		return untyped __cpp__("(({0})/({1}))", a, b);
		#else
		return Std.int(a / b);
		#end
	}

	// Long operators
	static public function lnew(a:Int, b:Int):Int64 return haxe.Int64.make(a, b);
	static public function ladd(a:Int64, b:Int64):Int64 return a + b;
	static public function lsub(a:Int64, b:Int64):Int64 return a - b;
	static public function lmul(a:Int64, b:Int64):Int64 return a * b;
	static public function ldiv(a:Int64, b:Int64):Int64 return a / b;
	static public function lrem(a:Int64, b:Int64):Int64 return a % b;
	static public function lband(a:Int64, b:Int64):Int64 return a & b;
	static public function lbor(a:Int64, b:Int64):Int64 return a | b;
	static public function land(a:Int64, b:Int64):Int64 return a & b;
	static public function lor(a:Int64, b:Int64):Int64 return a | b;
	static public function lxor(a:Int64, b:Int64):Int64 return a ^ b;
	static public function lshl(a:Int64, b:Int):Int64 return a << b;
	static public function lshr(a:Int64, b:Int):Int64 return a >> b;
	static public function lushr(a:Int64, b:Int):Int64 return a >>> b;
	static public function leq(a:Int64, b:Int64) return a == b;
	static public function lne(a:Int64, b:Int64) return a != b;
	static public function lge(a:Int64, b:Int64) return a >= b;
	static public function lle(a:Int64, b:Int64) return a <= b;
	static public function llt(a:Int64, b:Int64) return a < b;
	static public function lgt(a:Int64, b:Int64) return a > b;
	static public function llcmp(a:Int64, b:Int64) return llt(a, b) ? -1 : (lgt(a, b) ? 1 : 0);

	static public function getTime():Float {
		#if js return untyped __js__('Date.now()');
		#elseif sys return Sys.time() * 1000;
		#else return Date.now().getTime();
		#end
	}
}