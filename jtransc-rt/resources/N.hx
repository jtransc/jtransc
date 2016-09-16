package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.Int32;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

class N {
	static public var MIN_INT32:Int32 = -2147483648;
	static private var M2P32_DBL = Math.pow(2, 32);

	static private var strLitCache = new Map<String, {% CLASS java.lang.String %}>();

    static public function strLit(str:String):{% CLASS java.lang.String %} {
    	if (!strLitCache.exists(str)) strLitCache[str] = {% CLASS java.lang.String %}.make(str);
        return strLitCache[str];
    }

    static public function strLitEscape(str:String):{% CLASS java.lang.String %} return strLit(str);

    static public function str(str:String):{% CLASS java.lang.String %} return (str != null) ? {% CLASS java.lang.String %}.make(str) : null;
    static public function istr(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;
    static public function i_str(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;

    static public function isNegativeZero(x:Float) {
    	return x == 0 && 1 / x == Math.NEGATIVE_INFINITY;
    }

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

	static public function arrayInsert(array: Array<Dynamic>, index:Int, value: Dynamic): Void {
		#if flash
			var arr: Dynamic = array;
			arr.splice(index, 0, value);
		#else
			array.insert(index, value);
		#end
	}

	static public function arrayIterator(arr: Array<Dynamic>) : Iterator<Dynamic> {
		#if flash
		var cur = 0;
		return {
			hasNext : function() {
				return cur < arr.length;
			},
			next : function() {
				return arr[cur++];
			}
		}
		#else
		return arr.iterator();
		#end
	}

	static public function signExtend(v:Int, bits:Int):Int return (v << _shift(bits)) >> _shift(bits);
	static public function i2z(v:Int):Bool return v != 0;
	static public function i2b(v:Int):Int return (v << _shift(8)) >> _shift(8);
	static public function i2s(v:Int):Int return (v << _shift(16)) >> _shift(16);
	static public function i2c(v:Int):Int return v & 0xFFFF;

	#if js
	inline static public function imul(a:Int32, b:Int32):Int32 return untyped __js__("Math.imul({0}, {1})", a, b);
	#else
	inline static public function imul(a:Int32, b:Int32):Int32 return a * b;
	#end

	#if (js || cpp || flash)
	inline static public function i(v:Int):Int32 return v | 0;
	#else
	static public function i(v:Int):Int32 return (v << _shift(32)) >> _shift(32);
	#end

	static public function f2i(v:Float):Int {
		#if cpp
		return untyped __cpp__("((int)({0}))", v);
		#else
		return Std.int(v);
		#end
	}

	static public function int(v:Float):Int return f2i(v);

	static public function z2i(v:Bool):Int return v ? 1 : 0;

	inline static public function f2j(v:Float32):haxe.Int64 {
		return haxe.Int64.make(Std.int(v / M2P32_DBL), Std.int(v % M2P32_DBL));
	}

	inline static public function d2j(v:Float64):haxe.Int64 {
		return haxe.Int64.make(Std.int(v / M2P32_DBL), Std.int(v % M2P32_DBL));
	}

	static public function idiv(a:Int, b:Int):Int {
		#if cpp
		return untyped __cpp__("(({0})/({1}))", a, b);
		#else
		return Std.int(a / b);
		#end
	}

	static public function umod(a:Int32, b:Int32):Int32 return ((a % b) + b) % b;

	#if php
	static private function fixshift(v:Int32):Int32 return (v >= 0) ? (v) : umod(32 + v, 32);
	static public function ishl(a:Int32, b:Int32):Int32 return a << fixshift(b);
	static public function ishr(a:Int32, b:Int32):Int32 return a >> fixshift(b);
	static public function iushr(a:Int32, b:Int32):Int32 return a >>> fixshift(b);
	#else
	inline static public function ishl(a:Int32, b:Int32):Int32 return a << b;
	inline static public function ishr(a:Int32, b:Int32):Int32 return a >> b;
	inline static public function iushr(a:Int32, b:Int32):Int32 return a >>> b;
	#end

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

	static public function lcmp(a:Int64, b:Int64):Int return N.llcmp(a, b);
	static public function cmp(a:Float, b:Float):Int { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
	static public function cmpl(a:Float, b:Float):Int { return (Math.isNaN(a) || Math.isNaN(b)) ? -1 : cmp(a, b); }
	static public function cmpg(a:Float, b:Float):Int { return (Math.isNaN(a) || Math.isNaN(b)) ?  1 : cmp(a, b); }
	static inline public function eq(a:Dynamic, b:Dynamic):Bool { return a == b; }
	static inline public function ne(a:Dynamic, b:Dynamic):Bool { return a != b; }

	static public function getTime():Float {
		#if js return untyped __js__('Date.now()');
		#elseif sys return Sys.time() * 1000;
		#else return Date.now().getTime();
		#end
	}

	static public function resolveClass(name:String):{% CLASS java.lang.Class %} return HaxeNatives.resolveClass(name);

	static public function boxVoid(value:Dynamic):{% CLASS java.lang.Void %} return HaxeNatives.boxVoid(value);
	static public function boxBool(value:Bool):{% CLASS java.lang.Boolean %} return HaxeNatives.boxBool(value);
	static public function boxByte(value:Int):{% CLASS java.lang.Byte %} return HaxeNatives.boxByte(value);
	static public function boxShort(value:Int):{% CLASS java.lang.Short %} return HaxeNatives.boxShort(value);
	static public function boxChar(value:Int):{% CLASS java.lang.Character %} return HaxeNatives.boxChar(value);
	static public function boxInt(value:Int):{% CLASS java.lang.Integer %} return HaxeNatives.boxInt(value);
	static public function boxLong(value:Int64):{% CLASS java.lang.Long %} return HaxeNatives.boxLong(value);
	static public function boxFloat(value:Float32):{% CLASS java.lang.Float %} return HaxeNatives.boxFloat(value);
	static public function boxDouble(value:Float64):{% CLASS java.lang.Double %} return HaxeNatives.boxDouble(value);
	static public function boxString(value:String):{% CLASS java.lang.String %} return HaxeNatives.boxString(value);
	static public function boxWrapped(value:Dynamic):{% CLASS com.jtransc.JTranscWrapped %} return HaxeNatives.boxWrapped(value);
	static public function boxByteArray(value:Bytes):HaxeArrayByte return HaxeNatives.boxByteArray(value);

	static public function unboxVoid(value:{% CLASS java.lang.Object %}):Void return HaxeNatives.unboxVoid(value);
	static public function unboxBool(value:{% CLASS java.lang.Object %}):Bool return HaxeNatives.unboxBool(value);
	static public function unboxByte(value:{% CLASS java.lang.Object %}):Int return HaxeNatives.unboxByte(value);
	static public function unboxShort(value:{% CLASS java.lang.Object %}):Int return HaxeNatives.unboxShort(value);
	static public function unboxChar(value:{% CLASS java.lang.Object %}):Int return HaxeNatives.unboxChar(value);
	static public function unboxInt(value:{% CLASS java.lang.Object %}):Int return HaxeNatives.unboxInt(value);
	static public function unboxLong(value:{% CLASS java.lang.Object %}):Int64 return HaxeNatives.unboxLong(value);
	static public function unboxFloat(value:{% CLASS java.lang.Object %}):Float32 return HaxeNatives.unboxFloat(value);
	static public function unboxDouble(value:{% CLASS java.lang.Object %}):Float64 return HaxeNatives.unboxDouble(value);
	static public function unboxString(value:{% CLASS java.lang.Object %}):String return HaxeNatives.unboxString(value);
	static public function unboxWrapped(value:{% CLASS java.lang.Object %}):Dynamic return HaxeNatives.unboxWrapped(value);
	static public function unboxByteArray(value:{% CLASS java.lang.Object %}):Bytes return HaxeNatives.unboxByteArray(value);
}