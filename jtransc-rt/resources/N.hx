package ;

import haxe.ds.Vector;
import haxe.Int64;
import haxe.io.Bytes;
import Lambda;
import haxe.CallStack;

using Lambda;

class N {
    static public function str(str:String):java_.lang.String_ {
        return (str != null) ? java_.lang.String_.make(str) : null;
    }

	static public inline function c<T, S> (value:T, c:Class<S>):S {
		return cast value;
		//return (value != null) ? cast value : null;
	}

	static public inline function byte(v:Int):Int return (v << 24) >> 24;
	static public inline function short(v:Int):Int return (v << 16) >> 16;
	static public inline function char(v:Int):Int return v & 0xFFFF;
}