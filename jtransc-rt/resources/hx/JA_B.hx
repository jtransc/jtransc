import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;
import haxe.ds.Vector;

#if cpp
import cpp.Int8;
import cpp.Pointer;
import cpp.NativeArray;

typedef __JA_B_Item = Vector<Int8>;
#else
typedef __JA_B_Item = Bytes;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_B extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }} public var data:__JA_B_Item = null;
	#if cpp
	{{ HAXE_FIELD_ANNOTATIONS }} public var ptr:Pointer<Int8> = null;
	#end

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, data: __JA_B_Item = null) {
        super();
        if (data == null) {
	        #if cpp
        		data = new __JA_B_Item(length);
        	#else
        		data = Bytes.alloc(length);
        	#end
		} else {
        	length = data.length;
        }
        this.data = data;
        this.length = length;
        this.desc = "[B";
        #if cpp
        	ptr = NativeArray.address(data.toData(), 0);
		#end
    }

	{{ HAXE_METHOD_ANNOTATIONS }} override public function getElementBytesSize():Int return 1;

	{{ HAXE_METHOD_ANNOTATIONS }}
    override public function getBytes():Bytes {
    	#if cpp
    	var out = Bytes.alloc(length);
    	for (n in 0 ... out.length) out.set(n, data.get(n));
    	return out;
    	#else
		return data;
		#end
    }

	{{ HAXE_METHOD_ANNOTATIONS }} public function getBytesData():BytesData return getBytes().getData();

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_B(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromUInt8Array(items:UInt8Array) {
        if (items == null) return null;
        var out = new JA_B(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromBytes(bytes:Bytes) {
        if (bytes == null) return null;
        var out = new JA_B(bytes.length);
        var bytesData = bytes.getData();
        for (n in 0 ... bytes.length) out.set(n, Bytes.fastGet(bytesData, n));
        return out;
    }

	#if cpp
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):Int return ptr[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:Int):Void ptr[checkBounds(index)] = value;
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} public function get(index:Int):Int return N.i2b(this.data.get(checkBounds(index)));
	{{ HAXE_METHOD_ANNOTATIONS }} public function set(index:Int, value:Int):Void this.data.set(checkBounds(index), value);
	#end

	{{ HAXE_METHOD_ANNOTATIONS }} public function getBool(index:Int):Bool return get(index) != 0;
	{{ HAXE_METHOD_ANNOTATIONS }} public function setBool(index:Int, value:Bool):Void set(index, value ? 1 : 0);

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
        var out = new JA_B(length);
        copy(this, out, 0, 0, length);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_B, to:JA_B, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
		}
    }

	{{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_B), srcPos, dstPos, length); }
}