import haxe.io.*;
import haxe.ds.Vector;
import haxe.Int32;

#if cpp
import cpp.Int32;
import cpp.Pointer;
import cpp.NativeArray;
typedef __JA_I_Item = Vector<Int32>;
#elseif flash
typedef __JA_I_Item = Vector<Int>;
#else
typedef __JA_I_Item = Int32Array;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_I extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }}
    static public var T0cst = new JA_I(0);

	{{ HAXE_FIELD_ANNOTATIONS }}
	public var data:__JA_I_Item = null;

	#if cpp
	{{ HAXE_FIELD_ANNOTATIONS }} public var ptr:Pointer<Int32> = null;
	#end

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, data:__JA_I_Item = null) {
        super();
        if (data == null) {
        	data = new __JA_I_Item(length);
		} else {
			length = data.length;
		}
        this.data = data;
        this.length = length;
        this.desc = "[I";
		#if cpp
		ptr = NativeArray.address(data.toData(), 0);
		#end
    }

	{{ HAXE_METHOD_ANNOTATIONS }} override public function getElementBytesSize():Int return 4;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        if (items.length == 0) return T0cst;
        var out = new JA_I(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }} static public function T(items:Array<Dynamic>) return fromArray(items);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T0() return T0cst;
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T1(a: Int) return fromArray([a]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T2(a: Int, b: Int) return fromArray([a, b]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T3(a: Int, b: Int, c: Int) return fromArray([a, b, c]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T4(a: Int, b: Int, c: Int, d: Int) return fromArray([a, b, c, d]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T5(a: Int, b: Int, c: Int, d: Int, e: Int) return fromArray([a, b, c, d, e]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T6(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) return fromArray([a, b, c, d, e, f]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T7(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int) return fromArray([a, b, c, d, e, f, g]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T8(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int) return fromArray([a, b, c, d, e, f, g, h]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T9(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int, i: Int) return fromArray([a, b, c, d, e, f, g, h, i]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T10(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int, i: Int, j: Int) return fromArray([a, b, c, d, e, f, g, h, i, j]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T11(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int, i: Int, j: Int, k: Int) return fromArray([a, b, c, d, e, f, g, h, i, j, k]);
	{{ HAXE_METHOD_ANNOTATIONS }} static public function T12(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int, i: Int, j: Int, k: Int, l: Int) return fromArray([a, b, c, d, e, f, g, h, i, j, k, l]);

	{{ HAXE_METHOD_ANNOTATIONS }} override public function getBytes(): Bytes throw 'getBytes';

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromBytes(bytes:Bytes) {
        if (bytes == null) return null;
        var out = new JA_I(0);
        out.length = Std.int(bytes.length / 4);
        for (n in 0 ... out.length) out.set(n, bytes.getInt32(n * 4));
        return out;
    }

	#if cpp
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Int return ptr[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Int):Void ptr[checkBounds(index)] = value;
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Int return this.data[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Int):Void this.data[checkBounds(index)] = value;
	#end

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getDynamic(index:Int):Dynamic return get(index);
	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function setDynamic(index:Int, value:Dynamic) set(index, value);

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
        var out = new JA_I(length);
        copy(this, out, 0, 0, length);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_I, to:JA_I, fromPos:Int, toPos:Int, length:Int) {
		#if (cpp || flash)
		Vector.blit(from.data, fromPos, to.data, toPos, length);
		#else
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
		#end
    }

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_I), srcPos, dstPos, length); }
}