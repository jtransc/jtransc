package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.Int32;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

typedef JavaVoid = {% CLASS java.lang.Void %}
typedef JavaClass = {% CLASS java.lang.Class %}
typedef JavaString = {% CLASS java.lang.String %}
typedef JavaObject = {% CLASS java.lang.Object %}
typedef JtranscWrapped = {% CLASS com.jtransc.JTranscWrapped %}
typedef JavaBoolean = {% CLASS java.lang.Boolean %}
typedef JavaByte = {% CLASS java.lang.Byte %}
typedef JavaShort = {% CLASS java.lang.Short %}
typedef JavaCharacter = {% CLASS java.lang.Character %}
typedef JavaInteger = {% CLASS java.lang.Integer %}
typedef JavaLong = {% CLASS java.lang.Long %}
typedef JavaFloat = {% CLASS java.lang.Float %}
typedef JavaDouble = {% CLASS java.lang.Double %}

// https://haxe.io/roundups/wwx/c++-magic/
{{ HAXE_CLASS_ANNOTATIONS }}
#if cpp
@:headerClassCode('
	inline static int _i2b(int v) { return (char)v; };
	inline static int _i2s(int v) { return (short)v; };
	inline static int _i2c(int v) { return (unsigned short)v; };
	inline static void _memcpy(unsigned char *dst, int dstPos, unsigned char *src, int srcPos, int shift, int count) {
		::memcpy(
			&dst[dstPos << shift],
			&src[srcPos << shift],
			count << shift
		);
	};

	inline static void _memmove(unsigned char *dst, int dstPos, unsigned char *src, int srcPos, int shift, int count) {
		::memmove(
			&dst[dstPos << shift],
			&src[srcPos << shift],
			count << shift
		);
	};

	inline static void _memset_N2(unsigned char *dst, int dstPos, int count, int value) {
		short *ptr = &(((short *)dst)[dstPos]);
		for (int n = 0; n < count; n++) ptr[n] = (short)value;
	};

	inline static void _memset_N4(unsigned char *dst, int dstPos, int count, int value) {
		int *ptr = &(((int *)dst)[dstPos]);
		for (int n = 0; n < count; n++) ptr[n] = (int)value;
	};

	inline static void _memset_N8(unsigned char *dst, int dstPos, int count, long long int value) {
		long long int *ptr = &(((long long int *)dst)[dstPos]);
		for (int n = 0; n < count; n++) ptr[n] = (long long int)value;
	};

	inline static void _memset_N4f(unsigned char *dst, int dstPos, int count, float value) {
		float *ptr = &(((float *)dst)[dstPos]);
		for (int n = 0; n < count; n++) ptr[n] = (float)value;
	};
')
#end
class N {
	{{ HAXE_FIELD_ANNOTATIONS }} static private var MAX_INT64 = N.lnew(0x7FFFFFFF, 0xFFFFFFFF);
	{{ HAXE_FIELD_ANNOTATIONS }} static private var MIN_INT64 = N.lnew(0x80000000, 0x00000000);
	{{ HAXE_FIELD_ANNOTATIONS }} static private var ZERO_INT64 = N.lnew(0x00000000, 0x00000000);
	{{ HAXE_FIELD_ANNOTATIONS }} static public var MIN_INT32:Int32 = -2147483648;
	{{ HAXE_FIELD_ANNOTATIONS }} static public var MAX_INT32:Int32 = 2147483647;
	{{ HAXE_FIELD_ANNOTATIONS }} static private var M2P32_DBL = Math.pow(2, 32);
	{{ HAXE_FIELD_ANNOTATIONS }} static private var strLitCache = new Map<String, {% CLASS java.lang.String %}>();

	#if cpp
		@:native("N_obj::_memcpy") static public function memcpy(dst: cpp.RawPointer<cpp.UInt8>, dstPos: Int, src: cpp.RawPointer<cpp.UInt8>, srcPos: Int, shift: Int, count: Int):Void return;
		@:native("N_obj::_memmove") static public function memmove(dst: cpp.RawPointer<cpp.UInt8>, dstPos: Int, src: cpp.RawPointer<cpp.UInt8>, srcPos: Int, shift: Int, count: Int):Void return;
		@:native("N_obj::_memset_N2") static public function memsetN2(dst: cpp.RawPointer<cpp.UInt8>, dstPos: Int, count: Int, value: Int):Void return;
		@:native("N_obj::_memset_N4") static public function memsetN4(dst: cpp.RawPointer<cpp.UInt8>, dstPos: Int, count: Int, value: Int):Void return;
		@:native("N_obj::_memset_N8") static public function memsetN8(dst: cpp.RawPointer<cpp.UInt8>, dstPos: Int, count: Int, value: haxe.Int64):Void return;
		@:native("N_obj::_memset_N4f") static public function memsetN4f(dst: cpp.RawPointer<cpp.UInt8>, dstPos: Int, count: Int, value: Float):Void return;
	#end

	{{ HAXE_METHOD_ANNOTATIONS }}
	inline static public function intToLong(v:Int):haxe.Int64 {
		return N.lnew(((v & 0x80000000) != 0) ? -1 : 0, v);
	}
	{{ HAXE_METHOD_ANNOTATIONS }}
	inline static public function floatToLong(v:Float64):haxe.Int64 {
		return N.lnew(Std.int(v / M2P32_DBL), Std.int(v % M2P32_DBL));
	}
	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function longToInt(v:haxe.Int64):Int { return N.llow(v); }
	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function longToFloat(v:haxe.Int64):Float64 {
		if (v < 0) return (v == MIN_INT64) ? -9223372036854775808.0 : -longToFloat(-v);
		var lowf:Float64 = N.llow(v);
		var highf:Float64 = N.lhigh(v);
		return lowf + highf * M2P32_DBL;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function strLit(str:String):{% CLASS java.lang.String %} {
    	if (!strLitCache.exists(str)) strLitCache[str] = {% CLASS java.lang.String %}.make(str);
        return strLitCache[str];
    }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function strLitEscape(str:String):{% CLASS java.lang.String %} return strLit(str);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function str(str:String):JavaString return (str != null) ? JavaString.make(str) : null;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function istr(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function i_str(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function isNegativeZero(x:Float) return x == 0 && 1 / x == Math.NEGATIVE_INFINITY;
	{{ HAXE_METHOD_ANNOTATIONS }} static public inline function c<T, S> (value:T, c:Class<S>):S return cast value;

	//static public inline function c<T, S> (value:T, c:Class<S>):S return (value != null) ? cast value : null;

	{{ HAXE_METHOD_ANNOTATIONS }}
	static inline private function _shift(count:Int) {
		#if php
			return untyped __php__("PHP_INT_SIZE") * 8 - count;
		#else
			return 32 - count;
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function arrayInsert(array: Array<Dynamic>, index:Int, value: Dynamic): Void {
		#if flash
			var arr: Dynamic = array;
			arr.splice(index, 0, value);
		#else
			array.insert(index, value);
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function arrayIterator(arr: Array<Dynamic>) : Iterator<Dynamic> {
		#if flash
			var cur = 0;
			return { hasNext : function() { return cur < arr.length; }, next : function() { return arr[cur++]; } };
		#else
			return arr.iterator();
		#end
	}

	#if cpp
	@:native("N_obj::_i2b") static public function i2b(v:Int):Int return ((v << 24) >> 24);
	@:native("N_obj::_i2s") static public function i2s(v:Int):Int return ((v << 16) >> 16);
	@:native("N_obj::_i2c") static public function i2c(v:Int):Int return v & 0xFFFF;
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function i(v:Int):Int32 return v;
	#elseif (js || flash)
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function i2b(v:Int):Int return ((v << 24) >> 24);
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function i2s(v:Int):Int return ((v << 16) >> 16);
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function i2c(v:Int):Int return v & 0xFFFF;
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function i(v:Int):Int32 return v | 0;
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline static private function _signExtend(v:Int, bits:Int):Int return (v << _shift(bits)) >> _shift(bits);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function i2b(v:Int):Int return _signExtend(v, 8);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function i2s(v:Int):Int return _signExtend(v, 16);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function i2c(v:Int):Int return v & 0xFFFF;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function i(v:Int):Int32 return _signExtend(v, 32);
	#end

	{{ HAXE_METHOD_ANNOTATIONS }} static public function i2z(v:Int):Bool return v != 0;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function wrap(value:Dynamic):JtranscWrapped return JtranscWrapped.wrap(value);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function toNativeString(str:JavaString):String return (str != null) ? str._str : null;

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function haxeStringArrayToJavaArray(strs:Array<String>):JA_L {
		var out = [];
		for (s in strs) out.push(N.str(s));
		return JA_L.fromArray('[Ljava/lang/String;', out);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function toNativeStrArray(strs:JA_L):Array<String> {
		var list = strs.toArray();
		return [for (s in 0 ... list.length) toNativeString(cast list[s])];
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function toNativeUnboxedArray(strs:JA_L):Array<Dynamic> {
		var list = strs.toArray();
		return [for (s in 0 ... list.length) unbox(cast list[s])];
	}

	#if js
	{{ HAXE_METHOD_ANNOTATIONS }}
	inline static public function imul(a:Int32, b:Int32):Int32 return untyped __js__("Math.imul({0}, {1})", a, b);
	#else
	{{ HAXE_METHOD_ANNOTATIONS }}
	inline static public function imul(a:Int32, b:Int32):Int32 return a * b;
	#end

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function d2i(v:Float):Int {
		if (Math.isFinite(v)) {
			#if cpp
			return untyped __cpp__("((int)({0}))", v);
			#else
			return Std.int(v);
			#end
		} else {
			if (Math.isNaN(v)) {
				return 0;
			} else if (v >= 0) {
				return MAX_INT32;
			} else {
				return MIN_INT32;
			}
		}
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function f2i(v:Float):Int {
		return d2i(v);
	}

	{{ HAXE_METHOD_ANNOTATIONS }} static public function int(v:Float):Int return f2i(v);

	//static public function int(value:Int):JavaInteger return boxInt(value);
	//static public function long(value:haxe.Int64):JavaLong return boxLong(value);
	//static public function float(value:Float32):JavaFloat return boxFloat(value);
	//static public function double(value:Float64):JavaDouble return boxDouble(value);

	{{ HAXE_METHOD_ANNOTATIONS }} static public function z2i(v:Bool):Int return v ? 1 : 0;

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function idiv(a:Int32, b:Int32):Int32 {
		if (a == 0) return 0;
    	if (b == 0) return 0; // CRASH
    	if (a == N.MIN_INT32 && b == -1) return N.MIN_INT32; // CRASH TOO?

		#if cpp return untyped __cpp__("(({0})/({1}))", a, b);
		#else return Std.int(a / b);
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function irem(a:Int32, b:Int32): Int32 {
    	if (a == 0) return 0;
    	if (b == 0) return 0; // CRASH
    	if (a == N.MIN_INT32 && b == -1) return 0; // CRASH TOO?
    	return a % b;
    }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function umod(a:Int32, b:Int32):Int32 return ((a % b) + b) % b;

	#if php
	{{ HAXE_METHOD_ANNOTATIONS }} static private function fixshift(v:Int32):Int32 return (v >= 0) ? (v) : umod(32 + v, 32);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function ishl(a:Int32, b:Int32):Int32 return a << fixshift(b);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function ishr(a:Int32, b:Int32):Int32 return a >> fixshift(b);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function iushr(a:Int32, b:Int32):Int32 return a >>> fixshift(b);
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline static public function ishl(a:Int32, b:Int32):Int32 return a << b;
	{{ HAXE_METHOD_ANNOTATIONS }} inline static public function ishr(a:Int32, b:Int32):Int32 return a >> b;
	{{ HAXE_METHOD_ANNOTATIONS }} inline static public function iushr(a:Int32, b:Int32):Int32 return a >>> b;
	#end

	// Long operators

	{{ HAXE_METHOD_ANNOTATIONS }} static public function lnew(a:Int, b:Int):haxe.Int64 return haxe.Int64.make(a, b);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function llow(v: haxe.Int64): Int return v.low;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lhigh(v: haxe.Int64): Int return v.high;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function f2j(v:Float32):haxe.Int64 {
		if (Math.isFinite(v)) {
			return haxe.Int64.fromFloat(v);
		} else {
			if (Math.isNaN(v)) {
    			return ZERO_INT64;
    		} else if (v >= 0) {
    			return MAX_INT64;
    		} else {
    			return MIN_INT64;
    		}
		}
	}
	{{ HAXE_METHOD_ANNOTATIONS }} static public function d2j(v:Float64):haxe.Int64 {
		if (Math.isFinite(v)) {
			return haxe.Int64.fromFloat(v);
		} else {
			if (Math.isNaN(v)) {
    			return ZERO_INT64;
    		} else if (v >= 0) {
    			return MAX_INT64;
    		} else {
    			return MIN_INT64;
    		}
		}
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function ldiv(a:haxe.Int64, b:haxe.Int64):haxe.Int64 {
		if (a == 0) return 0;
    	if (b == 0) return 0; // CRASH
    	if (a == N.MIN_INT64 && b == -1) return N.MIN_INT64; // CRASH TOO?
		return a / b;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function lrem(a:haxe.Int64, b:haxe.Int64): haxe.Int64 {
    	if (a == 0) return 0;
    	if (b == 0) return 0; // CRASH
    	if (a == N.MIN_INT64 && b == -1) return 0; // CRASH TOO?
    	return a % b;
    }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function lneg(a:haxe.Int64):haxe.Int64 return -a;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function linv(a:haxe.Int64):haxe.Int64 return ~a;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function ladd(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a + b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lsub(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a - b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lmul(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a * b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lband(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a & b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lbor(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a | b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function land(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a & b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lor(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a | b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lxor(a:haxe.Int64, b:haxe.Int64):haxe.Int64 return a ^ b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lshl(a:haxe.Int64, b:Int):haxe.Int64 return a << b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lshr(a:haxe.Int64, b:Int):haxe.Int64 return a >> b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lushr(a:haxe.Int64, b:Int):haxe.Int64 return a >>> b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function leq(a:haxe.Int64, b:haxe.Int64) return a == b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lne(a:haxe.Int64, b:haxe.Int64) return a != b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lge(a:haxe.Int64, b:haxe.Int64) return a >= b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lle(a:haxe.Int64, b:haxe.Int64) return a <= b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function llt(a:haxe.Int64, b:haxe.Int64) return a < b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function lgt(a:haxe.Int64, b:haxe.Int64) return a > b;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function llcmp(a:haxe.Int64, b:haxe.Int64) return llt(a, b) ? -1 : (lgt(a, b) ? 1 : 0);

	{{ HAXE_METHOD_ANNOTATIONS }} static public function lcmp(a:haxe.Int64, b:haxe.Int64):Int return N.llcmp(a, b);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function cmp(a:Float, b:Float):Int { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function cmpl(a:Float, b:Float):Int { return (Math.isNaN(a) || Math.isNaN(b)) ? -1 : cmp(a, b); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function cmpg(a:Float, b:Float):Int { return (Math.isNaN(a) || Math.isNaN(b)) ?  1 : cmp(a, b); }
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function eq(a:Dynamic, b:Dynamic):Bool { return a == b; }
	{{ HAXE_METHOD_ANNOTATIONS }} static inline public function ne(a:Dynamic, b:Dynamic):Bool { return a != b; }

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function getTime():Float {
		#if js return untyped __js__('Date.now()');
		#elseif sys return Sys.time() * 1000;
		#else return Date.now().getTime();
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function resolveClass(name:String):{% CLASS java.lang.Class %} {
		if (name == null) {
			trace('resolveClass:name:null');
			debugger();
			return null;
		}
		var result = {% SMETHOD java.lang.Class:forName:(Ljava/lang/String;)Ljava/lang/Class; %}(N.str(name));
		if (result == null) {
			trace('resolveClass:result:null');
			debugger();
		}
		return result;
	}

	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxVoid(value:Dynamic):JavaVoid { return null; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxBool(value:Bool):JavaBoolean { return JavaBoolean{% IMETHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxByte(value:Int):JavaByte { return JavaByte{% IMETHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxShort(value:Int):JavaShort { return JavaShort{% IMETHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxChar(value:Int):JavaCharacter { return JavaCharacter{% IMETHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxInt(value:Int):JavaInteger { return JavaInteger{% IMETHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxLong(value:haxe.Int64):JavaLong { return JavaLong{% IMETHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxFloat(value:Float32):JavaFloat { return JavaFloat{% IMETHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxDouble(value:Float64):JavaDouble { return JavaDouble{% IMETHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxString(value:String):JavaString { return (value != null) ? JavaString.make(value) : null; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxWrapped(value:Dynamic):JtranscWrapped { return JtranscWrapped.wrap(value); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function boxByteArray(value:Bytes):JA_B { return JA_B.fromBytes(value); }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxVoid(value:JavaObject):Void { return cast null; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxBool(value:JavaObject):Bool { return cast(value, JavaBoolean){% IFIELD java.lang.Boolean:value:Z %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxByte(value:JavaObject):Int { return cast(value, JavaByte){% IFIELD java.lang.Byte:value:B %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxShort(value:JavaObject):Int { return cast(value, JavaShort){% IFIELD java.lang.Short:value:S %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxChar(value:JavaObject):Int { return cast(value, JavaCharacter){% IFIELD java.lang.Character:value:C %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxInt(value:JavaObject):Int { return cast(value, JavaInteger){% IFIELD java.lang.Integer:value:I %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxLong(value:JavaObject):haxe.Int64 { return cast(value, JavaLong){% IFIELD java.lang.Long:value:J %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxFloat(value:JavaObject):Float32 { return cast(value, JavaFloat){% IFIELD java.lang.Float:value:F %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxDouble(value:JavaObject):Float64 { return cast(value, JavaDouble){% IFIELD java.lang.Double:value:D %}; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxString(value:JavaObject):String { return cast(value, JavaString)._str; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxWrapped(value:JavaObject):Dynamic { return cast(value, JtranscWrapped)._wrapped; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function unboxByteArray(value:JavaObject):Bytes { return cast(value, JA_B).getBytes(); }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function swap32(p0:Int32):Int32 { return ((p0 >>> 24)) | ((p0 >> 8) & 0xFF00) | ((p0 << 8) & 0xFF0000) | ((p0 << 24)); }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function swap16(p0:Int32):Int32 { return ((((p0 & 0xFF00) >> 8) | ((p0 & 0xFF) << 8)) << 16) >> 16; }
	{{ HAXE_METHOD_ANNOTATIONS }} static public function swap16u(p0:Int32):Int32 { return (((p0 & 0xFF00) >> 8) | ((p0 & 0xFF) << 8)); }

	{{ HAXE_METHOD_ANNOTATIONS }} static public inline function throwRuntimeException(msg:String) throw {% CONSTRUCTOR java.lang.RuntimeException:(Ljava/lang/String;)V %}(N.str(msg));

	#if debug
		{{ HAXE_METHOD_ANNOTATIONS }}
		static public function checkNotNull<T>(item:T):T {
			if (item == null) throw {% CONSTRUCTOR java.lang.NullPointerException:()V %}();
			return item;
		}
	#else
		{{ HAXE_METHOD_ANNOTATIONS }}
		static inline public function checkNotNull<T>(item:T):T {
			return item;
		}
	#end

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function isNode():Bool {
		#if js
		return untyped __js__("typeof process != 'undefined'");
		#else
		return false;
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function getStackTrace(skip:Int):JA_L {
		var out = [];
		var callStack = CallStack.callStack();
		for (n in 0 ... callStack.length) {
			out.push(convertStackItem(callStack[n]));
		}
		return JA_L.fromArray("[Ljava.lang.StackTraceElement;", out.slice(skip));
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function hashMap(obj:Dynamic):{% CLASS java.util.HashMap %} {
		var out = new {% CLASS java.util.HashMap %}(){% IMETHOD java.util.HashMap:<init>:()V %}();
		var fields = Reflect.fields(obj);
		for (n in 0 ... fields.length) {
			var key = fields[n];
			var value = Reflect.field(obj, key);
			out{% IMETHOD java.util.HashMap:put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; %}(box(key), box(value));
		}
		return out;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function mapToObject(map:{% CLASS java.util.Map %}):Dynamic {
		if (map == null) return null;
		var obj = {};
		var array = iteratorToArray(map{% IMETHOD java.util.Map:entrySet %}(){% IMETHOD java.util.Set:iterator %}());
		for (n in 0 ... array.length) {
			var item = array[n];
			var key:JavaObject = item{% IMETHOD java.util.Map$Entry:getKey %}();
			var value:JavaObject = item{% IMETHOD java.util.Map$Entry:getValue %}();
			Reflect.setField(obj, unbox(key), unbox(value));
		}
		return obj;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function iteratorToArray(it:{% CLASS java.util.Iterator %}):Array<Dynamic> {
		if (it == null) return null;
		var out = [];
		while (it{% IMETHOD java.util.Iterator:hasNext:()Z %}()) {
			out.push(it{% IMETHOD java.util.Iterator:next:()Ljava/lang/Object; %}());
		}
		return out;
	}

	// BOX alias

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function strArray(strs:Array<String>):JA_L {
		var out = new JA_L(strs.length, "[Ljava.lang.String;");
		for (n in 0 ... strs.length) out.set(n, str(strs[n]));
		return out;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function byteArrayToString(chars:JA_B, start:Int = 0, count:Int = -1, charset:String = "UTF-8"):String {
		if (count < 0) count = chars.length;
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar(chars.get(n));
		return out.toString();
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function byteArrayWithHiToString(chars:JA_B, start:Int, count:Int, hi:Int):String {
		if (count < 0) count = chars.length;
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar((chars.get(n) & 0xFF) | ((hi & 0xFF) << 8));
		return out.toString();
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function haxeIteratorToArray<T>(iterator: Iterator<T>):Array<T> {
		var out = [];
		for (v in iterator) out.push(v);
		return out;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function charArrayToString(chars:JA_C, start:Int = 0, count:Int = 999999999):String {
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar(chars.get(n));
		return out.toString();
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function intArrayToString(chars:JA_I, start:Int = 0, count:Int = 999999999):String {
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar(chars.get(n));
		return out.toString();
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function stringToCharArray(str:String):JA_C {
		#if (js || flash || java || cs)
		var out = new JA_C(str.length);
		for (n in 0 ... str.length) out.set(n, str.charCodeAt(n));
		return out;
		#else
		var out = new JA_C(haxe.Utf8.length(str));
		var n = 0;
		haxe.Utf8.iter(str, function(c) { out.set(n++, c); });
		return out;
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function ichar(code: Int): String {
		#if cpp
			var out = new haxe.Utf8();
			out.addChar(code);
			return out.toString();
		#else
			return String.fromCharCode(code);
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function getFunction(obj:Dynamic):Dynamic { return obj._execute; }

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function toArray(obj:Dynamic):Vector<Dynamic> {
		var out = new Vector(obj.length);
		for (n in 0 ... obj.length) out[n] = obj[n];
		return out;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public inline function debugger() {
		#if js
		untyped __js__("console.trace('debugger;');");
		untyped __js__("debugger;");
		#elseif flash
		flash.system.ApplicationDomain.currentDomain.getDefinition("flash.debugger::enterDebugger")();
		#end
	}

	//static public function getClassDescriptor(object:JavaObject):String {
	//	if (Std.is(object, JA_0)) return cast(object, JA_0).desc;
	//	var haxeClass = Type.getClass(object);
	//	if (haxeClass == null) trace('haxeClass == null');
	//	var haxeClassName = Type.getClassName(haxeClass);
	//	if (haxeClassName == null) trace('haxeClassName == null');
	//	var javaClassName = R.internalClassNameToName(haxeClassName);
	//	if (javaClassName == null) trace('javaClassName == null :: $haxeClassName');
	//	return javaClassName;
	//}
	//
	//static public function getClass(object:JavaObject):{% CLASS java.lang.Class %} {
	//	return resolveClass(getClassDescriptor(object));
	//}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function newInstance(javaInternalClassName:String) {
		if (javaInternalClassName == null) trace('N.newInstance::javaInternalClassName == null');
		var clazz = Type.resolveClass(javaInternalClassName);
		if (clazz == null) trace('N.newInstance::clazz == null ; javaInternalClassName = $javaInternalClassName');
		// HaxeReflectionInfo
		return Type.createInstance(clazz, []);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function newEmptyInstance(javaInternalClassName:String) {
		if (javaInternalClassName == null) trace('N.newEmptyInstance::javaInternalClassName == null');
		var clazz = Type.resolveClass(javaInternalClassName);
		if (clazz == null) trace('N.newEmptyInstance::clazz == null ; javaInternalClassName = $javaInternalClassName');
		// HaxeReflectionInfo
		return Type.createEmptyInstance(clazz);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function arraycopy(src:JavaObject, srcPos:Int, dest:JavaObject, destPos:Int, length:Int) {
		var srcArray = cast(src, JA_0);
		var dstArray = cast(dest, JA_0);
		var elementShift = srcArray.elementShift;
		if (srcArray != null && dstArray != null) {
			#if cpp
				if (srcArray.rawPtr != null && elementShift >= 0) {
					memmove(dstArray.rawPtr, destPos, srcArray.rawPtr, srcPos, elementShift, length);
					return;
				}
			#end
			srcArray.copyTo(srcPos, dstArray, destPos, length);
		} else {
			var str = "arraycopy failed unsupported array type! " + src + ", " + dest;
			trace(str);
			throw str;
		}
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function args():Array<String> {
		#if sys return Sys.args();
		#elseif js if (untyped __js__("typeof process !== 'undefined' && process.argv")) return untyped __js__("process.argv.slice(2)"); else return [];
		#else return [];
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public inline function cast2<T, S> (value:T, c:Class<S>):S return N.c(value, c);

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function box(value:Dynamic):JavaObject {
		if (Std.is(value, Int)) return boxInt(cast value);
		if (Std.is(value, Float)) return boxFloat(cast value);
		if (haxe.Int64.is(value)) return boxLong(cast value);
		if (Std.is(value, String)) return str(cast value);
		if ((value == null) || Std.is(value, JavaObject)) return value;
		if (Std.is(value, haxe.io.Bytes)) return JA_B.fromBytes(value);
		return JtranscWrapped.wrap(value);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function unbox(value:JavaObject):Dynamic {
		if (Std.is(value, JavaBoolean)) return unboxBool(value);
		if (Std.is(value, JavaByte)) return unboxByte(value);
		if (Std.is(value, JavaShort)) return unboxShort(value);
		if (Std.is(value, JavaCharacter)) return unboxChar(value);
		if (Std.is(value, JavaInteger)) return unboxInt(value);
		if (Std.is(value, JavaLong)) return unboxLong(value);
		if (Std.is(value, JavaFloat)) return unboxFloat(value);
		if (Std.is(value, JavaDouble)) return unboxDouble(value);
		if (Std.is(value, JavaString)) return unboxString(value);
		if (Std.is(value, JA_B)) return unboxByteArray(value);
		if (Std.is(value, JtranscWrapped)) return unboxWrapped(value);
		throw 'Was not able to unbox "$value"';
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function boxWithType(clazz:JavaClass, value:Dynamic):JavaObject {
		if (Std.is(value, JavaObject)) return cast value;
		var clazzName:String = N.istr(clazz{% IFIELD java.lang.Class:name %});

		switch (clazzName) {
			case 'void': return boxVoid(cast value);
			case 'boolean': return boxBool(cast value);
			case 'byte': return boxByte(cast value);
			case 'short': return boxShort(cast value);
			case 'char': return boxChar(cast value);
			case 'int': return boxInt(cast value);
			case 'long': return boxLong(cast value);
			case 'float': return boxFloat(cast value);
			case 'double': return boxDouble(cast value);
		}

		throwRuntimeException("Don't know how to box '" + clazzName + "' with value '" + value + "'");
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function unboxWithTypeWhenRequired(clazz:JavaClass, value:Dynamic):Dynamic {
		var clazzName:String = N.istr(clazz{% IFIELD java.lang.Class:name %});

		switch (clazzName) {
			case 'void'   : return null;
			case 'boolean': return unboxBool(value);
			case 'byte'   : return unboxByte(value);
			case 'short'  : return unboxShort(value);
			case 'char'   : return unboxChar(value);
			case 'int'    : return unboxInt(value);
			case 'long'   : return unboxLong(value);
			case 'float'  : return unboxFloat(value);
			case 'double' : return unboxDouble(value);
		}

		return value;
	};

	{{ HAXE_FIELD_ANNOTATIONS }}
	static private var _tempBytes = haxe.io.Bytes.alloc(8);
	{{ HAXE_FIELD_ANNOTATIONS }}
	static private var _tempF32 = haxe.io.Float32Array.fromBytes(_tempBytes);
	{{ HAXE_FIELD_ANNOTATIONS }}
	static private var _tempI32 = haxe.io.Int32Array.fromBytes(_tempBytes);
	{{ HAXE_FIELD_ANNOTATIONS }}
	static private var _tempF64 = haxe.io.Float64Array.fromBytes(_tempBytes);

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function intBitsToFloat(value: Int): Float32 {
		#if js _tempI32[0] = value; return _tempF32[0];
		#else return haxe.io.FPHelper.i32ToFloat(value);
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function floatToIntBits(value: Float32): Int {
		#if js _tempF32[0] = value; return _tempI32[0];
		#else return haxe.io.FPHelper.floatToI32(value); #end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function longBitsToDouble(value: haxe.Int64): Float64 {
		#if js _tempI32[0] = value.low; _tempI32[1] = value.high; return _tempF64[0];
		#else return haxe.io.FPHelper.i64ToDouble(N.llow(value), N.lhigh(value)); #end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function doubleToLongBits(value: Float64):haxe.Int64 {
		#if js _tempF64[0] = value; var i1 = _tempI32[1]; var i2 = _tempI32[0]; return N.lnew(i1, i2);
		#else return cast haxe.io.FPHelper.doubleToI64(value); #end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function newException(msg:String) return {% CONSTRUCTOR java.lang.Exception:(Ljava/lang/String;)V %}(N.str(msg));

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public inline function rethrow(J__i__exception__:Dynamic) {
		#if js
			#if (haxe_ver >= 3.3)
			js.Lib.rethrow();
			#else
			untyped __js__('if (J__i__exception__ && J__i__exception__.stack) console.error(J__i__exception__.stack);');
			throw J__i__exception__;
			#end
		#elseif neko neko.Lib.rethrow(J__i__exception__);
		#elseif cpp cpp.Lib.rethrow(J__i__exception__);
		#elseif php php.Lib.rethrow(J__i__exception__);
		#else throw J__i__exception__;
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function createStackItem(className:String, methodName:String, fileName:String, line:Int):{% CLASS java.lang.StackTraceElement %} {
		return {% CONSTRUCTOR java.lang.StackTraceElement:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V %}(
			N.str(className),
			N.str(methodName),
			N.str(fileName),
			line
		);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	static public function convertStackItem(i):{% CLASS java.lang.StackTraceElement %} {
		var className = "DummyClass";
		var methodName = "dummyMethod";
		var fileName = "DummyClass.java";
		var line = 0;

		function handle(i) {
			switch (i) {
				case CFunction:
				case Module(m):
				case FilePos(s, _file, _line):
					if (s != null) handle(s);
					fileName = _file;
					line = _line;
				case Method(_classname, _method):
					className = _classname;
					methodName = _method;
				case LocalFunction(v):
					methodName = '_$v';
				default:
			}
		}

		handle(i);

		return createStackItem(className, methodName, fileName, line);
	}

	public static function CHECK_CAST<TIn, TOut>(i : TIn, cls : Class<TOut>) : TOut {
		if (i == null) return null;
		//trace('CHECK_CAST:' + i + ' : ' + cls);
        if (Std.is(i, cls)) {
			//trace('a');
            var res: TOut = cast i;
            if (res == null) {
            	//trace('b');
				throw {% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N.str("Class cast error"));
            }
            //trace('c');
            return res;
        } else {
        	//trace('d');
			throw {% CONSTRUCTOR java.lang.ClassCastException:(Ljava/lang/String;)V %}(N.str("Class cast error"));
        }
        return null;
    }
    
    public static function tryFinallyVoid(tryBlock: Void -> Void, finallyBlock: Void -> Void): Void {
        try {
            tryBlock();
        } catch (e: Dynamic) {
            finallyBlock();
            throw e;
        }
        finallyBlock();
    }
    
    public static function tryFinallyDynamic(tryBlock: Void -> Dynamic, finallyBlock: Void -> Void): Dynamic {
        var ret;
        try {
            ret = tryBlock();
        } catch (e: Dynamic) {
            finallyBlock();
            throw e;
        }
        finallyBlock();
        return ret;
    }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function monitorCommon(obj: JavaObject) {
		#if cpp
			if (obj._hx_mutex == null) obj._hx_mutex = new cpp.vm.Mutex();
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }} static public function monitorEnter(obj: JavaObject) {
		if (obj == null) return;
		monitorCommon(obj);
		#if cpp
		//trace('acquire1');
		obj._hx_mutex.acquire();
		//trace('acquire2');
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }} static public function monitorExit(obj: JavaObject) {
		if (obj == null) return;
		monitorCommon(obj);
		#if cpp
		//trace('release1');
		obj._hx_mutex.release();
		//trace('release2');
		#end
	}
}