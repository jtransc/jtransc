package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.Int32;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

typedef Long = Int64;
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

class N {
	static private var MAX_INT64 = haxe.Int64.make(0x7FFFFFFF, 0xFFFFFFFF);
	static private var MIN_INT64 = haxe.Int64.make(0x80000000, 0x00000000);
	static public var MIN_INT32:Int32 = -2147483648;
	static private var M2P32_DBL = Math.pow(2, 32);

	inline static public function intToLong(v:Int):Long {
		return haxe.Int64.make(((v & 0x80000000) != 0) ? -1 : 0, v);
	}
	inline static public function floatToLong(v:Float64):Long {
		return haxe.Int64.make(Std.int(v / M2P32_DBL), Std.int(v % M2P32_DBL));
	}
	static public function longToInt(v:Int64):Int { return v.low; }
	static public function longToFloat(v:Int64):Float64 {
		if (v < 0) return (v == MIN_INT64) ? -9223372036854775808.0 : -longToFloat(-v);
		var lowf:Float64 = cast v.low;
		var highf:Float64 = cast v.high;
		return lowf + highf * M2P32_DBL;
	}

	static private var strLitCache = new Map<String, {% CLASS java.lang.String %}>();

    static public function strLit(str:String):{% CLASS java.lang.String %} {
    	if (!strLitCache.exists(str)) strLitCache[str] = {% CLASS java.lang.String %}.make(str);
        return strLitCache[str];
    }

    static public function strLitEscape(str:String):{% CLASS java.lang.String %} return strLit(str);

	static public function str(str:String):JavaString return (str != null) ? JavaString.make(str) : null;
    static public function istr(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;
    static public function i_str(str:{% CLASS java.lang.String %}):String return (str != null) ? str._str : null;

    static public function isNegativeZero(x:Float) return x == 0 && 1 / x == Math.NEGATIVE_INFINITY;

	static public inline function c<T, S> (value:T, c:Class<S>):S return cast value;
	//static public inline function c<T, S> (value:T, c:Class<S>):S return (value != null) ? cast value : null;

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
			return { hasNext : function() { return cur < arr.length; }, next : function() { return arr[cur++]; } };
		#else
			return arr.iterator();
		#end
	}

	static public function signExtend(v:Int, bits:Int):Int return (v << _shift(bits)) >> _shift(bits);
	static public function i2z(v:Int):Bool return v != 0;
	static public function i2b(v:Int):Int return (v << _shift(8)) >> _shift(8);
	static public function i2s(v:Int):Int return (v << _shift(16)) >> _shift(16);
	static public function i2c(v:Int):Int return v & 0xFFFF;

	static public function wrap(value:Dynamic):JtranscWrapped return JtranscWrapped.wrap(value);
	static public function toNativeString(str:JavaString):String return (str != null) ? str._str : null;

	static public function haxeStringArrayToJavaArray(strs:Array<String>):JA_L {
		var out = [];
		for (s in strs) out.push(N.str(s));
		return JA_L.fromArray(out, '[Ljava/lang/String;');
	}

	static public function toNativeStrArray(strs:JA_L):Array<String> {
		var list = strs.toArray();
		return [for (s in 0 ... list.length) toNativeString(cast list[s])];
	}

	static public function toNativeUnboxedArray(strs:JA_L):Array<Dynamic> {
		var list = strs.toArray();
		return [for (s in 0 ... list.length) unbox(cast list[s])];
	}

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

	//static public function int(value:Int):JavaInteger return boxInt(value);
	//static public function long(value:Int64):JavaLong return boxLong(value);
	//static public function float(value:Float32):JavaFloat return boxFloat(value);
	//static public function double(value:Float64):JavaDouble return boxDouble(value);

	static public function z2i(v:Bool):Int return v ? 1 : 0;

	inline static public function f2j(v:Float32):haxe.Int64 return haxe.Int64.fromFloat(v);
	inline static public function d2j(v:Float64):haxe.Int64 return haxe.Int64.fromFloat(v);

	static public function idiv(a:Int, b:Int):Int {
		#if cpp return untyped __cpp__("(({0})/({1}))", a, b);
		#else return Std.int(a / b);
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
	static public function llow(a:Int64):Int return a.low;
	static public function lhigh(a:Int64):Int return a.high;
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

	static public function boxVoid(value:Dynamic):JavaVoid { return null; }
	static public function boxBool(value:Bool):JavaBoolean { return JavaBoolean.{% METHOD java.lang.Boolean:valueOf:(Z)Ljava/lang/Boolean; %}(value); }
	static public function boxByte(value:Int):JavaByte { return JavaByte.{% METHOD java.lang.Byte:valueOf:(B)Ljava/lang/Byte; %}(value); }
	static public function boxShort(value:Int):JavaShort { return JavaShort.{% METHOD java.lang.Short:valueOf:(S)Ljava/lang/Short; %}(value); }
	static public function boxChar(value:Int):JavaCharacter { return JavaCharacter.{% METHOD java.lang.Character:valueOf:(C)Ljava/lang/Character; %}(value); }
	static public function boxInt(value:Int):JavaInteger { return JavaInteger.{% METHOD java.lang.Integer:valueOf:(I)Ljava/lang/Integer; %}(value); }
	static public function boxLong(value:Long):JavaLong { return JavaLong.{% METHOD java.lang.Long:valueOf:(J)Ljava/lang/Long; %}(value); }
	static public function boxFloat(value:Float32):JavaFloat { return JavaFloat.{% METHOD java.lang.Float:valueOf:(F)Ljava/lang/Float; %}(value); }
	static public function boxDouble(value:Float64):JavaDouble { return JavaDouble.{% METHOD java.lang.Double:valueOf:(D)Ljava/lang/Double; %}(value); }
	static public function boxString(value:String):JavaString { return (value != null) ? JavaString.make(value) : null; }
	static public function boxWrapped(value:Dynamic):JtranscWrapped { return JtranscWrapped.wrap(value); }
	static public function boxByteArray(value:Bytes):JA_B { return JA_B.fromBytes(value); }

	static public function unboxVoid(value:JavaObject):Void { return cast null; }
	static public function unboxBool(value:JavaObject):Bool { return cast(value, JavaBoolean).{% FIELD java.lang.Boolean:value:Z %}; }
	static public function unboxByte(value:JavaObject):Int { return cast(value, JavaByte).{% FIELD java.lang.Byte:value:B %}; }
	static public function unboxShort(value:JavaObject):Int { return cast(value, JavaShort).{% FIELD java.lang.Short:value:S %}; }
	static public function unboxChar(value:JavaObject):Int { return cast(value, JavaCharacter).{% FIELD java.lang.Character:value:C %}; }
	static public function unboxInt(value:JavaObject):Int { return cast(value, JavaInteger).{% FIELD java.lang.Integer:value:I %}; }
	static public function unboxLong(value:JavaObject):Long { return cast(value, JavaLong).{% FIELD java.lang.Long:value:J %}; }
	static public function unboxFloat(value:JavaObject):Float32 { return cast(value, JavaFloat).{% FIELD java.lang.Float:value:F %}; }
	static public function unboxDouble(value:JavaObject):Float64 { return cast(value, JavaDouble).{% FIELD java.lang.Double:value:D %}; }
	static public function unboxString(value:JavaObject):String { return cast(value, JavaString)._str; }
	static public function unboxWrapped(value:JavaObject):Dynamic { return cast(value, JtranscWrapped)._wrapped; }
	static public function unboxByteArray(value:JavaObject):Bytes { return cast(value, JA_B).getBytes(); }

	static public function swap32(p0:Int32):Int32 { return ((p0 >>> 24)) | ((p0 >> 8) & 0xFF00) | ((p0 << 8) & 0xFF0000) | ((p0 << 24)); }
	static public function swap16(p0:Int32):Int32 { return ((((p0 & 0xFF00) >> 8) | ((p0 & 0xFF) << 8)) << 16) >> 16; }
	static public function swap16u(p0:Int32):Int32 { return (((p0 & 0xFF00) >> 8) | ((p0 & 0xFF) << 8)); }

	static public inline function throwRuntimeException(msg:String) {
		throw {% CONSTRUCTOR java.lang.RuntimeException:(Ljava/lang/String;)V %}(N.str(msg));
	}

	#if debug
		static public function checkNotNull<T>(item:T):T {
			if (item == null) throw {% CONSTRUCTOR java.lang.NullPointerException:()V %}();
			return item;
		}
	#else
		static inline public function checkNotNull<T>(item:T):T {
			return item;
		}
	#end

	static public function isNode():Bool {
		#if js
		return untyped __js__("typeof process != 'undefined'");
		#else
		return false;
		#end
	}

	static public function getStackTrace(skip:Int):JA_L {
		var out = [];
		var callStack = CallStack.callStack();
		for (n in 0 ... callStack.length) {
			out.push(convertStackItem(callStack[n]));
		}
		return JA_L.fromArray(out.slice(skip), "[Ljava.lang.StackTraceElement;");
	}

	static public function hashMap(obj:Dynamic):{% CLASS java.util.HashMap %} {
		var out = new {% CLASS java.util.HashMap %}().{% METHOD java.util.HashMap:<init>:()V %}();
		var fields = Reflect.fields(obj);
		for (n in 0 ... fields.length) {
			var key = fields[n];
			var value = Reflect.field(obj, key);
			out.{% METHOD java.util.HashMap:put:(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object; %}(box(key), box(value));
		}
		return out;
	}

	static public function mapToObject(map:{% CLASS java.util.Map %}):Dynamic {
		if (map == null) return null;
		var obj = {};
		var array = iteratorToArray(map.{% METHOD java.util.Map:entrySet %}().{% METHOD java.util.Set:iterator %}());
		for (n in 0 ... array.length) {
			var item = array[n];
			var key:JavaObject = item.{% METHOD java.util.Map$Entry:getKey %}();
			var value:JavaObject = item.{% METHOD java.util.Map$Entry:getValue %}();
			Reflect.setField(obj, unbox(key), unbox(value));
		}
		return obj;
	}

	static public function iteratorToArray(it:{% CLASS java.util.Iterator %}):Array<Dynamic> {
		if (it == null) return null;
		var out = [];
		while (it.{% METHOD java.util.Iterator:hasNext:()Z %}()) {
			out.push(it.{% METHOD java.util.Iterator:next:()Ljava/lang/Object; %}());
		}
		return out;
	}

	// BOX alias

	static public function strArray(strs:Array<String>):JA_L {
		var out = new JA_L(strs.length, "[Ljava.lang.String;");
		for (n in 0 ... strs.length) out.set(n, str(strs[n]));
		return out;
	}

	static public function byteArrayToString(chars:JA_B, start:Int = 0, count:Int = -1, charset:String = "UTF-8"):String {
		if (count < 0) count = chars.length;
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar(chars.get(n));
		return out.toString();
	}

	static public function byteArrayWithHiToString(chars:JA_B, start:Int, count:Int, hi:Int):String {
		if (count < 0) count = chars.length;
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar((chars.get(n) & 0xFF) | ((hi & 0xFF) << 8));
		return out.toString();
	}

	static public function haxeIteratorToArray<T>(iterator: Iterator<T>):Array<T> {
		var out = [];
		for (v in iterator) out.push(v);
		return out;
	}

	static public function charArrayToString(chars:JA_C, start:Int = 0, count:Int = 999999999):String {
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar(chars.get(n));
		return out.toString();
	}

	static public function intArrayToString(chars:JA_I, start:Int = 0, count:Int = 999999999):String {
		var end = start + count;
		end = Std.int(Math.min(end, chars.length));
		var out = new haxe.Utf8();
		for (n in start ... end) out.addChar(chars.get(n));
		return out.toString();
	}

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

	static public function getFunction(obj:Dynamic):Dynamic { return obj._execute; }

	static public function toArray(obj:Dynamic):Vector<Dynamic> {
		var out = new Vector(obj.length);
		for (n in 0 ... obj.length) out[n] = obj[n];
		return out;
	}

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

	static public function newInstance(javaInternalClassName:String) {
		if (javaInternalClassName == null) trace('N.newInstance::javaInternalClassName == null');
		var clazz = Type.resolveClass(javaInternalClassName);
		if (clazz == null) trace('N.newInstance::clazz == null ; javaInternalClassName = $javaInternalClassName');
		// HaxeReflectionInfo
		return Type.createInstance(clazz, []);
	}

	static public function newEmptyInstance(javaInternalClassName:String) {
		if (javaInternalClassName == null) trace('N.newEmptyInstance::javaInternalClassName == null');
		var clazz = Type.resolveClass(javaInternalClassName);
		if (clazz == null) trace('N.newEmptyInstance::clazz == null ; javaInternalClassName = $javaInternalClassName');
		// HaxeReflectionInfo
		return Type.createEmptyInstance(clazz);
	}

	static public function arraycopy(src:JavaObject, srcPos:Int, dest:JavaObject, destPos:Int, length:Int) {
		if (Std.is(src, JA_L)) {
			JA_L.copy(cast(src, JA_L), cast(dest, JA_L), srcPos, destPos, length);
		} else if (Std.is(src, JA_B)) {
			 JA_B.copy(cast(src, JA_B), cast(dest, JA_B), srcPos, destPos, length);
		 } else if (Std.is(src, JA_I)) {
			JA_I.copy(cast(src, JA_I), cast(dest, JA_I), srcPos, destPos, length);
		} else if (Std.is(src, JA_J)) {
			JA_J.copy(cast(src, JA_J), cast(dest, JA_J), srcPos, destPos, length);
		} else if (Std.is(src, JA_F)) {
			JA_F.copy(cast(src, JA_F), cast(dest, JA_F), srcPos, destPos, length);
		} else if (Std.is(src, JA_D)) {
			JA_D.copy(cast(src, JA_D), cast(dest, JA_D), srcPos, destPos, length);
		} else if (Std.is(src, JA_S)) {
			 JA_S.copy(cast(src, JA_S), cast(dest, JA_S), srcPos, destPos, length);
		} else if (Std.is(src, JA_C)) {
			JA_C.copy(cast(src, JA_C), cast(dest, JA_C), srcPos, destPos, length);
		} else {
			trace("arraycopy failed unsupported array type! " + src + ", " + dest);
			throw "arraycopy failed unsupported array type! " + src + ", " + dest;
		}
	}

	static public function args():Array<String> {
		#if sys return Sys.args();
		#elseif js if (untyped __js__("typeof process !== 'undefined' && process.argv")) return untyped __js__("process.argv.slice(2)"); else return [];
		#else return [];
		#end
	}

	static public inline function cast2<T, S> (value:T, c:Class<S>):S return N.c(value, c);

	static public function box(value:Dynamic):JavaObject {
		if (Std.is(value, Int)) return boxInt(cast value);
		if (Std.is(value, Float)) return boxFloat(cast value);
		if (Int64.is(value)) return boxLong(cast value);
		if (Std.is(value, String)) return str(cast value);
		if ((value == null) || Std.is(value, JavaObject)) return value;
		if (Std.is(value, haxe.io.Bytes)) return JA_B.fromBytes(value);
		return JtranscWrapped.wrap(value);
	}

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

	static public function boxWithType(clazz:JavaClass, value:Dynamic):JavaObject {
		if (Std.is(value, JavaObject)) return cast value;
		var clazzName:String = N.istr(clazz.{% FIELD java.lang.Class:name %});

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

	static public function unboxWithTypeWhenRequired(clazz:JavaClass, value:Dynamic):Dynamic {
		var clazzName:String = N.istr(clazz.{% FIELD java.lang.Class:name %});

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

	static private var _tempBytes = haxe.io.Bytes.alloc(8);
	static private var _tempF32 = haxe.io.Float32Array.fromBytes(_tempBytes);
	static private var _tempI32 = haxe.io.Int32Array.fromBytes(_tempBytes);
	static private var _tempF64 = haxe.io.Float64Array.fromBytes(_tempBytes);

	static public function intBitsToFloat(value: Int): Float32 {
		#if js _tempI32[0] = value; return _tempF32[0];
		#else return haxe.io.FPHelper.i32ToFloat(value);
		#end
	}

	static public function floatToIntBits(value: Float32): Int {
		#if js _tempF32[0] = value; return _tempI32[0];
		#else return haxe.io.FPHelper.floatToI32(value); #end
	}

	static public function longBitsToDouble(value: Int64): Float64 {
		#if js _tempI32[0] = value.low; _tempI32[1] = value.high; return _tempF64[0];
		#else return haxe.io.FPHelper.i64ToDouble(value.low, value.high); #end
	}

	static public function doubleToLongBits(value: Float64):Int64 {
		#if js _tempF64[0] = value; var i1 = _tempI32[1]; var i2 = _tempI32[0]; return haxe.Int64.make(i1, i2);
		#else return haxe.io.FPHelper.doubleToI64(value); #end
	}

	static public function newException(msg:String) return {% CONSTRUCTOR java.lang.Exception:(Ljava/lang/String;)V %}(N.str(msg));

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

	static public function createStackItem(className:String, methodName:String, fileName:String, line:Int):{% CLASS java.lang.StackTraceElement %} {
		return {% CONSTRUCTOR java.lang.StackTraceElement:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V %}(
			N.str(className),
			N.str(methodName),
			N.str(fileName),
			line
		);
	}

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
}