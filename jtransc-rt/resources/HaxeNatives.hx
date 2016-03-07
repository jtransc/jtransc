package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

typedef Long = Int64;

class HaxeNatives {
    static var outputBuffer = '';
    static public function outputChar(char:Int):Void {
        if (char == 10) {
        	#if js
			var _outputBuffer = outputBuffer;
        	untyped __js__("console.log(_outputBuffer);");
        	#else
            trace(outputBuffer);
            #end
            outputBuffer = '';
        } else {
            outputBuffer += String.fromCharCode(char);
        }
    }

	static var errorBuffer = '';
	static public function outputErrorChar(char:Int):Void {
		if (char == 10) {
			#if js
			var _errorBuffer = errorBuffer;
			untyped __js__("console.error(_errorBuffer);");
			#else
			trace(errorBuffer);
			#end
			errorBuffer = '';
		} else {
			errorBuffer += String.fromCharCode(char);
		}
	}

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

    static public function toNativeString(str:java_.lang.String_):String {
        return str._str;
    }

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
        end = Std.int(Math.min(end, chars.length - start));
        for (n in start ... end) out += String.fromCharCode(chars.get(n));
        return out;
    }

    static public function charArrayToString(chars:HaxeCharArray, start:Int = 0, count:Int = 999999999):String {
        var out = "";
        var end = start + count;
        end = Std.int(Math.min(end, chars.length - start));
        for (n in start ... end) out += String.fromCharCode(chars.get(n));
        return out;
    }

    static public function intArrayToString(chars:HaxeIntArray, start:Int = 0, count:Int = 999999999):String {
        var out = "";
        var end = start + count;
        end = Std.int(Math.min(end, chars.length - start));
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

    static public function getClassDescriptor(object:java_.lang.Object_):String {
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

    static public function objectToString(object:java_.lang.Object_):String {
        return getClass(object).getName__Ljava_lang_String_()._str + "@" + object.__ID__;
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

    static var BASE = "0123456789abcdefghijklmnopqrstuvwxyz";

    public static function intToString(value:Int, radix:Int = 10):String {
        #if (js || flash)
        return untyped (value | 0).toString(radix);
        #else
        if (radix == 10) return Std.string(value);
        if (value == 0) return '0';

        var sign = "";
        if (value < 0) {
            sign = '-';
            value = -value;
        }

        var result = '';
        while (value > 0) {
            result = BASE.charAt(value % radix) + result;
            value = Std.int(value / radix);
        }

        return sign + result;
        #end
    }

    static public function parseInt(value:String, radix:Int = 10) {
        #if js
        var v = untyped __js__("parseInt")(value, radix);
        return (untyped __js__("isNaN")(v)) ? null : v;
        #elseif flash9
        var v = untyped __global__["parseInt"](value, radix);
        return (untyped __global__["isNaN"](v)) ? null : v;
        #else
        value = StringTools.trim(value).toLowerCase();
        if (value.length == 0) return 0;

        var s = 0;
        var neg = false;
        if (value.indexOf("-") == 0) {
            neg = true;
            s = 1;
        } else if (value.indexOf("+") == 0) {
            s = 1;
        }

        if (s == 1 && value.length == 1) return 0;

        var j = value.indexOf("0x");
        if ((j == 0 || (j == 1 && s == 1)) && (radix == 0 || radix == 16)) {
            s += 2;
            if (radix == 0) radix = 16;
        } else if (radix == 0) {
            radix = 10;
        }

        var result = 0;
        for (i in s...value.length) {
            var x = BASE.indexOf(value.charAt(i));
            if (x == -1 || x >= radix) {
                if (i == s) return 0;
                else return neg ? -result : result;
            }
            result = (result * radix) + x;
        }

        return neg ? -result : result;
        #end
    }

    static public function arrayClone(array:Dynamic):Dynamic { return array.clone(); }
    static public function arrayGetClass(array:Dynamic):Dynamic { throw "Not implemented arrayGetClass"; return null; }
    static public function getProperty(prop:String):String {
        switch ((prop)) {
            case "os.arch": return getArch();
            case "os.name": return getOS();
            case "os.version": return "0.1";
            case "java.runtime.name": return "jtransc-haxe";
            case "file.separator": return "/";
            case "line.separator": return "\n";
            case "path.separator": return ":";
            case "file.encoding": return "UTF-8";
            case "java.home": return "/jtransc-haxe";
            case "java.specification.name": return "jtransc-haxe";
            case "java.specification.vendor": return "jtransc";
            case "java.specification.version": return "0.9";
            case "java.vendor": return "jtransc";
            case "java.vendor.url": return "http://github.com/jtransc/jtransc";
            case "java.vn.name": return "haxe";
            case "java.vm.specification.name": return "Jtransc/Haxe JVM emulator";
            case "java.vm.specification.vendor": return "jtransc-haxe";
            case "java.vm.specification.version": return "0.1";
            default: trace('Requested prop unknown ' + prop);
        }
        return "";
    }

    static public function getOS():String {
        #if sys
            return Sys.systemName();
        #elseif js
            return "js"; // Use node process or navigator
        #elseif flash
            return "flash";
        #else
            return "haxe";
        #end
    }

    static public function getArch():String {
        // x86, i386, ppc, sparc, arm
        return "x86";
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

    static public function box(value:Dynamic):java_.lang.Object_ {
        if (Std.is(value, Int)) return java_.lang.Integer_.valueOf_I_Ljava_lang_Integer_(cast(value, Int));
        if (Std.is(value, Float)) return java_.lang.Double_.valueOf_D_Ljava_lang_Double_(cast(value, Float));
        if (Int64.is(value)) return java_.lang.Long_.valueOf_J_Ljava_lang_Long_(cast value);
        if (Std.is(value, String)) return str(cast(value, String));
        if ((value == null) || Std.is(value, java_.lang.Object_)) return value;
		return jtransc.JTranscWrapped_.wrap(value);
    }

    static public function unbox(value:Dynamic):Dynamic {
        if (Std.is(value, java_.lang.Byte_)) return cast(value, java_.lang.Byte_).value;
        if (Std.is(value, java_.lang.Short_)) return cast(value, java_.lang.Short_).value;
        if (Std.is(value, java_.lang.Character_)) return cast(value, java_.lang.Character_).value;
        if (Std.is(value, java_.lang.Integer_)) return cast(value, java_.lang.Integer_).value;
        if (Std.is(value, java_.lang.Float_)) return cast(value, java_.lang.Float_).value;
        if (Std.is(value, java_.lang.Double_)) return cast(value, java_.lang.Double_).value;
        if (Std.is(value, java_.lang.Long_)) return cast(value, java_.lang.Long_).value;
        if (Std.is(value, java_.lang.String_)) return cast(value, java_.lang.String_)._str;
		if (Std.is(value, jtransc.JTranscWrapped_)) return cast(value, jtransc.JTranscWrapped_)._wrapped;
        throw 'Was not able to unbox "$value"';
    }

    static public function args():Array<String> {
        #if sys
        return Sys.args();
        #elseif js
        if (untyped __js__("typeof process !== 'undefined' && process.argv")) {
            return untyped __js__("process.argv.slice(2)");
        } else {
            return [];
        }
        #else
        return [];
        #end
    }

    static public function strCompare(a:String, b:String):Int {
        return if ( a < b ) -1 else if ( a > b ) 1 else 0;
    }

    static public function strCompareIgnoreCase(a:String, b:String):Int {
        return strCompare(a.toLowerCase(), b.toLowerCase());
    }

    static public inline function cast2<T, S> (value:T, c:Class<S>):S {
        return (value != null) ? (cast value) : null;
    }

    static public function formatBoxed(fmt:String, args:Array<Dynamic>):String {
        return HaxeFormat.format(fmt, args.map(function(v) { return unbox(v); }).array());
    }

    /*
    static private var _tempView = new haxe.io.ArrayBufferView(8);
    static private var _tempF32 = haxe.io.Float32Array.fromData(_tempView);
    static private var _tempI32 = haxe.io.Int32Array.fromData(_tempView);
    */
    static private var _tempBytes = haxe.io.Bytes.alloc(8);
    static private var _tempF32 = haxe.io.Float32Array.fromBytes(_tempBytes);
    static private var _tempI32 = haxe.io.Int32Array.fromBytes(_tempBytes);
    static private var _tempF64 = haxe.io.Float64Array.fromBytes(_tempBytes);

    static public function intBitsToFloat(value: Int) {
        #if cpp
        return untyped __cpp__("*(float *)(&{0})", value);
        #else
        _tempI32[0] = value;
        return _tempF32[0];
        #end
    }

    static public function floatToIntBits(value: Float) {
        #if cpp
        return untyped __cpp__("*(int *)(&{0})", value);
        #else
        _tempF32[0] = value;
        return _tempI32[0];
        #end
    }

    static public function longBitsToDouble(value: Long) {
        #if cpp
        return untyped __cpp__("*(double *)(&{0})", value);
        #else
		_tempI32[0] = value.low;
        _tempI32[1] = value.high;
        return _tempF64[0];
        #end
    }

    static public function doubleToLongBits(value: Float):Int64 {
        #if cpp
        return untyped __cpp__("*(long *)(&{0})", value);
        #else
        _tempF64[0] = value;
		var i1 = _tempI32[1];
		var i2 = _tempI32[0];
		return haxe.Int64.make(i1, i2);
        #end
    }

    static public function newException(msg:String) {
        var out = new java_.lang.Exception_();
        out._init__Ljava_lang_String__V(HaxeNatives.str(msg));
        return out;
    }

    static public function gcEnable() {
        #if cpp
            cpp.vm.Gc.enable(true);
        #end
    }

    static public function gcDisable() {
        #if cpp
            cpp.vm.Gc.enable(false);
        #end
    }

    static public function gc() {
        #if cpp
            cpp.vm.Gc.compact();
        #end
    }

    static public function syncioOpen(path:String, flags:Int) {
        trace('syncioOpen:$path:$flags');
        return -1;
    }

    static public function syncioRead(fd:Int) {
        return -1;
    }

    static public function syncioWrite(fd:Int, byte:Int) {
    }

    static public function syncioWriteBytes(fd:Int, data:HaxeByteArray, offset:Int, length:Int) {
        trace('syncioWriteBytes:$length');
    }

    static public function syncioClose(fd:Int) {
        trace('syncioClose');
    }

    static public function syncioLength(fd:Int):Int64 {
        return intToLong(0);
    }

    #if !flash
    static private var byteMem:haxe.io.Bytes;
    static private var shortMem:haxe.io.UInt16Array;
    static private var intMem:haxe.io.Int32Array;
    static private var floatMem:haxe.io.Float32Array;
    static private var doubleMem:haxe.io.Float64Array;
    #end

    static public function memSelect(mem:haxe.io.Bytes) {
        #if flash
        flash.Memory.select(mem.getData());
        #else
        HaxeNatives.byteMem   = mem;
        HaxeNatives.shortMem  = haxe.io.UInt16Array.fromBytes(mem);
        HaxeNatives.intMem    = haxe.io.Int32Array.fromBytes(mem);
        HaxeNatives.floatMem  = haxe.io.Float32Array.fromBytes(mem);
        HaxeNatives.doubleMem = haxe.io.Float64Array.fromBytes(mem);
        #end
    }
    static public inline function memLi8(address:Int) {
        return #if flash flash.Memory.getByte(address << 0); #else byteMem.get(address); #end
    }
    static public inline function memLi16(address2:Int) {
        return #if flash flash.Memory.getUI16(address2 << 1); #else shortMem.get(address2); #end
    }
    static public inline function memLi32(address4:Int) {
        return #if flash flash.Memory.getI32(address4 << 2); #else intMem.get(address4); #end
    }
    static public inline function memLf32(address4:Int) {
        return #if flash flash.Memory.getFloat(address4 << 2); #else floatMem.get(address4); #end
    }
    static public inline function memLf64(address8:Int) {
        return #if flash flash.Memory.getDouble(address8 << 3); #else floatMem.get(address8); #end
    }

    static public inline function memSi8(address:Int, value:Int) {
        #if flash flash.Memory.setByte(address << 0, value); #else byteMem.set(address, value); #end
    }
    static public inline function memSi16(address2:Int, value:Int) {
        #if flash flash.Memory.setI16(address2 << 1, value); #else shortMem.set(address2, value); #end
    }
    static public inline function memSi32(address4:Int, value:Int) {
        #if flash flash.Memory.setI32(address4 << 2, value); #else intMem.set(address4, value); #end
    }
    static public inline function memSf32(address4:Int, value:Float) {
        #if flash flash.Memory.setFloat(address4 << 2, value); #else floatMem.set(address4, value); #end
    }
    static public inline function memSf64(address8:Int, value:Float) {
        #if flash flash.Memory.setDouble(address8 << 3, value); #else floatMem.set(address8, value); #end
    }

    static public inline function sxi1(value:Int) {
        return #if flash flash.Memory.signExtend1(value); #else ((value << 31) >> 31); #end
    }
    static public inline function sxi8(value:Int) {
        return #if flash flash.Memory.signExtend8(value); #else ((value << 24) >> 24); #end
    }
    static public inline function sxi16(value:Int) {
        return #if flash flash.Memory.signExtend16(value); #else ((value << 16) >> 16); #end
    }

    static public inline function rethrow(__i__exception__:Dynamic) {
        #if js
			#if (haxe_ver > 3.2)
			js.Lib.rethrow();
			#else
			untyped __js__('if (__i__exception__ && __i__exception__.stack) console.error(__i__exception__.stack);');
			throw __i__exception__;
			#end
		#else
			throw __i__exception__;
        #end
        //#if js
        //if (untyped __js__('typeof haxe_CallStack !== "undefined"')) {
        //	untyped __js__('throw haxe_CallStack.lastException');
        //} else {
        //	throw __i__exception__;
        //}
        //#else

        //#end
    }

	static public function createStackItem(className:String, methodName:String, fileName:String, line:Int):java_.lang.StackTraceElement_ {
		var item = new java_.lang.StackTraceElement_();
		item._init__Ljava_lang_String_Ljava_lang_String_Ljava_lang_String_I_V(
			HaxeNatives.str(className),
			HaxeNatives.str(methodName),
			HaxeNatives.str(fileName),
			line
		);
		return item;
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

	static public function fillSecureRandomBytes(bytes:HaxeByteArray) {
		var length = bytes.length;

		#if js
		try {
			var _bytes = bytes.data;
			untyped __js__("crypto.getRandomValues(_bytes);");
			return;
		} catch (e:Dynamic) {

		}
		#end

		for (n in 0 ... length) {
			bytes.set(n, Std.int(Math.random() * 255));
		}
	}

	static public inline function throwRuntimeException(msg:String) {
		var e = new java_.lang.RuntimeException_();
		e._init__Ljava_lang_String__V(HaxeNatives.str(msg));
		throw e;
	}

	static public function getField(clazz:Class<Dynamic>, obj:Dynamic, name:String):java_.lang.Object_ {
		//Reflect.callMethod(clazz, Reflect.field(clazz, "__hx_static__init__"), []);
		return HaxeNatives.box(Reflect.field((obj != null) ? obj : clazz, name));
	}

	static public function getFieldInt(clazz:Class<Dynamic>, obj:Dynamic, name:String):Int {
		return Reflect.field((obj != null) ? obj : clazz, name);
	}

	static public function getFieldBool(clazz:Class<Dynamic>, obj:Dynamic, name:String):Bool {
		return Reflect.field((obj != null) ? obj : clazz, name);
	}

	static public function getFieldLong(clazz:Class<Dynamic>, obj:Dynamic, name:String):Long {
		return Reflect.field((obj != null) ? obj : clazz, name);
	}

	static public function getFieldDouble(clazz:Class<Dynamic>, obj:Dynamic, name:String):Float {
		return Reflect.field((obj != null) ? obj : clazz, name);
	}

	static public function swap32(p0:Int):Int {
		return ((p0 >>> 24)) | ((p0 >> 8) & 0xFF00) | ((p0 << 8) & 0xFF0000) | ((p0 << 24));
	}

	static public function swap16(p0:Int):Int {
		return (((p0 & 0xFF00) >> 8) | (p0 << 8));
	}

	static public function numberToString(number:Float):String {
		var str = '$number';
		if (str.indexOf('.') < 0) str += ".0";
		return str;
	}

	static public function reverseString(p0:String):String {
		var reversed = '';
		for (n in 0 ... p0.length) reversed += p0.charAt(p0.length - n - 1);
		return reversed;
	}


}
