package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

typedef Long = Int64;
typedef JavaObject = java_.lang.Object_;

class HaxeNatives {
	static private var M2P32_DBL = Math.pow(2, 32);
    inline static public function intToLong(v:Int):Long {
		return haxe.Int64.make(((v & 0x80000000) != 0) ? -1 : 0, v);
	}
    inline static public function floatToLong(v:Float):Long {
		return haxe.Int64.make(Std.int(v / M2P32_DBL), Std.int(v % M2P32_DBL));
	}
    static public function longToInt(v:Long):Int { return v.low; }
    static public function longToFloat(v:Long):Float {
        var lowf:Float = cast v.low;
        var highf:Float = cast v.high;
        return lowf + highf * M2P32_DBL;
    }

    static public function lcmp(a:Long, b:Long):Int { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
    static public function cmp(a:Float, b:Float):Int { return (a < b) ? -1 : ((a > b) ? 1 : 0); }
    static public function cmpl(a:Float, b:Float):Int { return (Math.isNaN(a) || Math.isNaN(b)) ? -1 : cmp(a, b); }
    static public function cmpg(a:Float, b:Float):Int { return (Math.isNaN(a) || Math.isNaN(b)) ? 1 : cmp(a, b); }

    static public function wrap(value:Dynamic):jtransc.JTranscWrapped_ { return jtransc.JTranscWrapped_.wrap(value); }

    static public function toNativeString(str:java_.lang.String_):String {
        return (str != null) ? str._str : null;
    }

    static public function toNativeStrArray(strs:HaxeArray):Array<String> {
        return [for (s in strs.toArray()) toNativeString(cast s)];
    }

    static public function toNativeUnboxedArray(strs:HaxeArray):Array<Dynamic> {
        return [for (s in strs.toArray()) unbox(cast s)];
    }

    static public function hashMap(obj:Dynamic):java_.util.HashMap_ {
	    var out = new java_.util.HashMap_().java_util_HashMap_init___V();
	    for (key in Reflect.fields(obj)) {
	    	var value = Reflect.field(obj, key);
	    	out.put_Ljava_lang_Object_Ljava_lang_Object__Ljava_lang_Object_(box(key), box(value));
	    }
	    return out;
    }

	// BOX alias
    static public function str(str:String):java_.lang.String_ {
        return (str != null) ? java_.lang.String_.make(str) : null;
    }
    static public function int(value:Int):java_.lang.Integer_ {
        return java_.lang.Integer_.valueOf_I_Ljava_lang_Integer_(value);
    }
    static public function long(value:Long):java_.lang.Long_ {
        return java_.lang.Long_.valueOf_J_Ljava_lang_Long_(value);
    }
    static public function float(value:Float):java_.lang.Float_ {
        return java_.lang.Float_.valueOf_F_Ljava_lang_Float_(value);
    }
    static public function double(value:Float):java_.lang.Double_ {
        return java_.lang.Double_.valueOf_D_Ljava_lang_Double_(value);
    }

    static public function strArray(strs:Array<String>):HaxeArray {
        return HaxeArray.fromArray(strs.map(function(s) { return str(s); }).array(), "[Ljava.lang.String;");
    }

    static public function byteArrayToString(chars:HaxeByteArray, start:Int = 0, count:Int = -1, charset:String = "UTF-8"):String {
        if (count < 0) count = chars.length;
        var out = "";
        var end = start + count;
        end = Std.int(Math.min(end, chars.length));
        for (n in start ... end) out += String.fromCharCode(chars.get(n));
        return out;
    }

    static public function charArrayToString(chars:HaxeCharArray, start:Int = 0, count:Int = 999999999):String {
        var out = "";
        var end = start + count;
        end = Std.int(Math.min(end, chars.length));
        for (n in start ... end) out += String.fromCharCode(chars.get(n));
        return out;
    }

    static public function intArrayToString(chars:HaxeIntArray, start:Int = 0, count:Int = 999999999):String {
        var out = "";
        var end = start + count;
        end = Std.int(Math.min(end, chars.length));
        for (n in start ... end) out += String.fromCharCode(chars.get(n));
        return out;
    }

    static public function stringToByteArray(str:String, charset:String = "UTF-8"):HaxeByteArray {
        var out = new HaxeByteArray(str.length);
        for (n in 0 ... str.length) out.set(n, str.charCodeAt(n));
        return out;
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

    static public function resolveClass(name:String):java_.lang.Class_ {
        if (name == null) {
            trace('resolveClass:name:null');
			debugger();
            return null;
        }
        var result = java_.lang.Class_.forName_Ljava_lang_String__Ljava_lang_Class_(HaxeNatives.str(name));
        if (result == null) {
            trace('resolveClass:result:null');
			debugger();
        }
        return result;
    }

    static public function getClassDescriptor(object:JavaObject):String {
        if (Std.is(object, HaxeBaseArray)) return cast(object, HaxeBaseArray).desc;
        var haxeClass = Type.getClass(object);
        if (haxeClass == null) trace('haxeClass == null');
        var haxeClassName = Type.getClassName(haxeClass);
        if (haxeClassName == null) trace('haxeClassName == null');
        var javaClassName = HaxeReflectionInfo.internalClassNameToName(haxeClassName);
        if (javaClassName == null) trace('javaClassName == null :: $haxeClassName');
        return javaClassName;
    }

    static public function getClass(object:java_.lang.Object_):java_.lang.Class_ {
        return resolveClass(getClassDescriptor(object));
    }

    static public function newInstance(javaInternalClassName:String) {
        if (javaInternalClassName == null) trace('HaxeNatives.newInstance::javaInternalClassName == null');
        var clazz = Type.resolveClass(javaInternalClassName);
        if (clazz == null) trace('HaxeNatives.newInstance::clazz == null ; javaInternalClassName = $javaInternalClassName');
        // HaxeReflectionInfo
        return Type.createInstance(clazz, []);
    }

    static public function newEmptyInstance(javaInternalClassName:String) {
        if (javaInternalClassName == null) trace('HaxeNatives.newEmptyInstance::javaInternalClassName == null');
        var clazz = Type.resolveClass(javaInternalClassName);
        if (clazz == null) trace('HaxeNatives.newEmptyInstance::clazz == null ; javaInternalClassName = $javaInternalClassName');
        // HaxeReflectionInfo
        return Type.createEmptyInstance(clazz);
    }

    static public function arraycopy(src:java_.lang.Object_, srcPos:Int, dest:java_.lang.Object_, destPos:Int, length:Int) {
        if (Std.is(src, HaxeArray)) {
            HaxeArray.copy(cast(src, HaxeArray), cast(dest, HaxeArray), srcPos, destPos, length);
        } else if (Std.is(src, HaxeByteArray)) {
             HaxeByteArray.copy(cast(src, HaxeByteArray), cast(dest, HaxeByteArray), srcPos, destPos, length);
         } else if (Std.is(src, HaxeIntArray)) {
            HaxeIntArray.copy(cast(src, HaxeIntArray), cast(dest, HaxeIntArray), srcPos, destPos, length);
        } else if (Std.is(src, HaxeLongArray)) {
            HaxeLongArray.copy(cast(src, HaxeLongArray), cast(dest, HaxeLongArray), srcPos, destPos, length);
        } else if (Std.is(src, HaxeFloatArray)) {
            HaxeFloatArray.copy(cast(src, HaxeFloatArray), cast(dest, HaxeFloatArray), srcPos, destPos, length);
        } else if (Std.is(src, HaxeDoubleArray)) {
            HaxeDoubleArray.copy(cast(src, HaxeDoubleArray), cast(dest, HaxeDoubleArray), srcPos, destPos, length);
        } else if (Std.is(src, HaxeShortArray)) {
             HaxeShortArray.copy(cast(src, HaxeShortArray), cast(dest, HaxeShortArray), srcPos, destPos, length);
        } else {
            trace("arraycopy failed unsupported array type!");
            throw "arraycopy failed unsupported array type!";
        }
    }

    static public function args():Array<String> {
        #if sys return Sys.args();
        #elseif js if (untyped __js__("typeof process !== 'undefined' && process.argv")) return untyped __js__("process.argv.slice(2)"); else return [];
        #else return [];
        #end
    }

    static public inline function cast2<T, S> (value:T, c:Class<S>):S {
        return (value != null) ? (cast value) : null;
    }

    static public function formatBoxed(fmt:String, args:Array<Dynamic>):String {
        return HaxeFormat.format(fmt, args.map(function(v) { return unbox(v); }).array());
    }

	static public function box(value:Dynamic):JavaObject {
		if (Std.is(value, Int)) return java_.lang.Integer_.valueOf_I_Ljava_lang_Integer_(cast(value, Int));
		if (Std.is(value, Float)) return java_.lang.Double_.valueOf_D_Ljava_lang_Double_(cast(value, Float));
		if (Int64.is(value)) return java_.lang.Long_.valueOf_J_Ljava_lang_Long_(cast value);
		if (Std.is(value, String)) return str(cast(value, String));
		if ((value == null) || Std.is(value, java_.lang.Object_)) return value;
		return jtransc.JTranscWrapped_.wrap(value);
	}

	static public function unbox(value:JavaObject):Dynamic {
		if (Std.is(value, java_.lang.Boolean_)) return unboxBool(value);
		if (Std.is(value, java_.lang.Byte_)) return unboxByte(value);
		if (Std.is(value, java_.lang.Short_)) return unboxShort(value);
		if (Std.is(value, java_.lang.Character_)) return unboxChar(value);
		if (Std.is(value, java_.lang.Integer_)) return unboxInt(value);
		if (Std.is(value, java_.lang.Long_)) return unboxLong(value);
		if (Std.is(value, java_.lang.Float_)) return unboxFloat(value);
		if (Std.is(value, java_.lang.Double_)) return unboxDouble(value);
		if (Std.is(value, java_.lang.String_)) return unboxString(value);
		if (Std.is(value, jtransc.JTranscWrapped_)) return unboxWrapped(value);
		throw 'Was not able to unbox "$value"';
	}

	static public function boxVoid(value:Dynamic):java_.lang.Void_ { return null; }
	static public function boxBool(value:Bool):java_.lang.Boolean_ { return java_.lang.Boolean_.valueOf_Z_Ljava_lang_Boolean_(value); }
	static public function boxByte(value:Int):java_.lang.Byte_ { return java_.lang.Byte_.valueOf_B_Ljava_lang_Byte_(value); }
	static public function boxShort(value:Int):java_.lang.Short_ { return java_.lang.Short_.valueOf_S_Ljava_lang_Short_(value); }
	static public function boxChar(value:Int):java_.lang.Character_ { return java_.lang.Character_.valueOf_C_Ljava_lang_Character_(value); }
	static public function boxInt(value:Int):java_.lang.Integer_ { return java_.lang.Integer_.valueOf_I_Ljava_lang_Integer_(value); }
	static public function boxLong(value:Long):java_.lang.Long_ { return java_.lang.Long_.valueOf_J_Ljava_lang_Long_(value); }
	static public function boxFloat(value:Float):java_.lang.Float_ { return java_.lang.Float_.valueOf_F_Ljava_lang_Float_(value); }
	static public function boxDouble(value:Float):java_.lang.Double_ { return java_.lang.Double_.valueOf_D_Ljava_lang_Double_(value); }
	static public function boxString(value:String):java_.lang.String_ { return (value != null) ? java_.lang.String_.make(value) : null; }
	static public function boxWrapped(value:Dynamic):jtransc.JTranscWrapped_ { return jtransc.JTranscWrapped_.wrap(value); }

	static public function unboxVoid(value:JavaObject):Void { return cast null; }
	static public function unboxBool(value:JavaObject):Bool { return cast(value, java_.lang.Boolean_).value; }
	static public function unboxByte(value:JavaObject):Int { return cast(value, java_.lang.Byte_).value; }
	static public function unboxShort(value:JavaObject):Int { return cast(value, java_.lang.Short_).value; }
	static public function unboxChar(value:JavaObject):Int { return cast(value, java_.lang.Character_).value; }
	static public function unboxInt(value:JavaObject):Int { return cast(value, java_.lang.Integer_).value; }
	static public function unboxLong(value:JavaObject):Long { return cast(value, java_.lang.Long_).value; }
	static public function unboxFloat(value:JavaObject):Float { return cast(value, java_.lang.Float_).value; }
	static public function unboxDouble(value:JavaObject):Float { return cast(value, java_.lang.Double_).value; }
	static public function unboxString(value:JavaObject):String { return cast(value, java_.lang.String_)._str; }
	static public function unboxWrapped(value:JavaObject):Dynamic { return cast(value, jtransc.JTranscWrapped_)._wrapped; }

    static private var _tempBytes = haxe.io.Bytes.alloc(8);
    static private var _tempF32 = haxe.io.Float32Array.fromBytes(_tempBytes);
    static private var _tempI32 = haxe.io.Int32Array.fromBytes(_tempBytes);
    static private var _tempF64 = haxe.io.Float64Array.fromBytes(_tempBytes);

    static public function intBitsToFloat(value: Int) {
        #if cpp return untyped __cpp__("*(float *)(&{0})", value);
        #else _tempI32[0] = value; return _tempF32[0];
        #end
    }

    static public function floatToIntBits(value: Float) {
        #if cpp return untyped __cpp__("*(int *)(&{0})", value);
        #else _tempF32[0] = value; return _tempI32[0];
        #end
    }

    static public function longBitsToDouble(value: Long) {
        #if cpp return untyped __cpp__("*(double *)(&{0})", value);
        #else _tempI32[0] = value.low; _tempI32[1] = value.high; return _tempF64[0];
        #end
    }

    static public function doubleToLongBits(value: Float):Int64 {
        #if cpp return untyped __cpp__("*(long *)(&{0})", value);
        #else _tempF64[0] = value; var i1 = _tempI32[1]; var i2 = _tempI32[0]; return haxe.Int64.make(i1, i2);
        #end
    }

    static public function newException(msg:String) {
        return new java_.lang.Exception_().java_lang_Exception_init__Ljava_lang_String__V(HaxeNatives.str(msg));
    }

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

	static public function createStackItem(className:String, methodName:String, fileName:String, line:Int):java_.lang.StackTraceElement_ {
		return new java_.lang.StackTraceElement_().java_lang_StackTraceElement_init__Ljava_lang_String_Ljava_lang_String_Ljava_lang_String_I_V(
			HaxeNatives.str(className),
			HaxeNatives.str(methodName),
			HaxeNatives.str(fileName),
			line
		);
	}

	static public function convertStackItem(i):java_.lang.StackTraceElement_ {
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

	static public function getStackTrace(skip:Int):HaxeArray {
		var out = [];
		for (stack in CallStack.callStack()) {
			out.push(convertStackItem(stack));
		}
		return HaxeArray.fromArray(out.slice(skip), "[Ljava.lang.StackTraceElement;");
	}

	static public inline function throwRuntimeException(msg:String) {
		throw new java_.lang.RuntimeException_().java_lang_RuntimeException_init__Ljava_lang_String__V(HaxeNatives.str(msg));
	}

	static public function swap32(p0:Int):Int { return ((p0 >>> 24)) | ((p0 >> 8) & 0xFF00) | ((p0 << 8) & 0xFF0000) | ((p0 << 24)); }
	static public function swap16(p0:Int):Int { return ((((p0 & 0xFF00) >> 8) | ((p0 & 0xFF) << 8)) << 16) >> 16; }
	static public function swap16u(p0:Int):Int { return (((p0 & 0xFF00) >> 8) | ((p0 & 0xFF) << 8)); }
}

