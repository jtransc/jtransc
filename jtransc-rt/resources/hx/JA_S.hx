import haxe.io.UInt16Array;
import haxe.ds.Vector;

#if cpp
import cpp.Int16;
import cpp.Pointer;
import cpp.NativeArray;
typedef __JA_S_Item = Vector<Int16>;
#elseif flash
typedef __JA_S_Item = Vector<Int>;
#else
typedef __JA_S_Item = UInt16Array;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_S extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }}
    public var data:__JA_S_Item = null;

	#if cpp
	{{ HAXE_FIELD_ANNOTATIONS }} public var ptr:Pointer<Int16> = null;
	#end

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, data: __JA_S_Item = null) {
        super();
        if (data == null) {
        	data = new __JA_S_Item(length);
		} else {
			length = data.length;
		}
        this.data = data;
        this.length = length;
        this.desc = "[S";
		#if cpp
		this.ptr = NativeArray.address(data.toData(), 0);
		#end
    }

	{{ HAXE_METHOD_ANNOTATIONS }} override public function getElementBytesSize():Int return 2;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_S(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	#if cpp
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Int  return ptr[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Int):Void ptr[checkBounds(index)] = value;
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Int  return N.i2s(this.data[checkBounds(index)]);
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Int):Void this.data[checkBounds(index)] = value;
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
        var out = new JA_S(length);
        copy(this, out, 0, 0, length);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_S, to:JA_S, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
    }

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_S), srcPos, dstPos, length); }
}