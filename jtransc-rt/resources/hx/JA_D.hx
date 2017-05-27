import haxe.io.*;
import haxe.ds.Vector;

#if (sys || flash)
typedef __JA_D_Item = Vector<Float64>;
#else
typedef __JA_D_Item = Float64Array;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_D extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }}
    public var data:__JA_D_Item = null;

	#if cpp
	{{ HAXE_FIELD_ANNOTATIONS }} public var ptr:cpp.Pointer<Float64> = null;
	#end

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, data:__JA_D_Item = null) {
        super();
        if (data == null) data = new __JA_D_Item(length); else length = data.length;
        this.data = data;
        this.length = length;
        this.desc = "[D";
		#if cpp
		ptr = cpp.NativeArray.address(data.toData(), 0);
		#end
    }

	{{ HAXE_METHOD_ANNOTATIONS }} override public function getElementBytesSize():Int return 8;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_D(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	#if cpp
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Float64 return ptr[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Float64):Void { ptr[checkBounds(index)] = value; }
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Float64 return this.data[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Float64):Void { this.data[checkBounds(index)] = value; }
	#end
	{{ HAXE_METHOD_ANNOTATIONS }} override public function getDynamic(index:Int):Dynamic return get(index);
	{{ HAXE_METHOD_ANNOTATIONS }} override public function setDynamic(index:Int, value:Dynamic) set(index, value);

	{{ HAXE_METHOD_ANNOTATIONS }}
    public function join(separator:String) {
        var out = '';
        for (n in 0 ... length) {
            if (n != 0) out += separator;
            out += get(n);
        }
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    public override function clone() {
        var out = new JA_D(length);
        copy(this, out, 0, 0, length);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_D, to:JA_D, fromPos:Int, toPos:Int, length:Int) {
		#if (sys || flash)
		Vector.blit(from.data, fromPos, to.data, toPos, length);
		#else
		var _from:Float64Array = from.data;
		var _to:Float64Array = to.data;
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) _to[toPos + n] = _from[fromPos + n];
    	} else {
	        for (n in 0 ... length) _to[toPos + n] = _from[fromPos + n];
		}
		#end
    }

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_D), srcPos, dstPos, length); }
}