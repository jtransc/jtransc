import haxe.io.Int32Array;
import haxe.io.Bytes;
import haxe.ds.Vector;

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_I extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }}
	public var data:Int32Array = null;

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, data:Int32Array = null) {
        super();
        if (data == null) data = new Int32Array(length); else length = data.length;
        this.data = data;
        this.length = length;
        this.desc = "[I";
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getElementBytesSize():Int return 4;
	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getArrayBufferView() return data.view;
	{{ HAXE_METHOD_ANNOTATIONS }}
    public function getTypedArray() return data;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_I(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function T(items:Array<Dynamic>) {
    	return fromArray(items);
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    override public function getBytes() return data.view.buffer;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromBytes(bytes:Bytes) {
        if (bytes == null) return null;
        var out = new JA_I(0);
        out.length = Std.int(bytes.length / 4);
        for (n in 0 ... out.length) out.set(n, bytes.getInt32(n * 4));
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    inline public function get(index:Int):Int return this.data[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }}
    inline public function set(index:Int, value:Int):Void this.data[checkBounds(index)] = value;
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
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
    }
}