package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

class N {
	static private var strLitCache = new Map<String, java_.lang.String_>();

    static public function strLit(str:String):java_.lang.String_ {
    	if (!strLitCache.exists(str)) strLitCache[str] = java_.lang.String_.make(str);
        return strLitCache[str];
    }

    static public function str(str:String):java_.lang.String_ {
        return (str != null) ? java_.lang.String_.make(str) : null;
    }

	static public inline function c<T, S> (value:T, c:Class<S>):S {
		return cast value;
		//return (value != null) ? cast value : null;
	}

	static public function i2z(v:Int):Bool return v != 0;
	static public function i2b(v:Int):Int return (v << 24) >> 24;
	static public function i2s(v:Int):Int return (v << 16) >> 16;
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
}