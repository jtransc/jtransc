import haxe.ds.Vector;
import haxe.Int64;

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_J extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }} public var data:Vector<Int64> = null;
	{{ HAXE_FIELD_ANNOTATIONS }} static private var ZERO = Int64.make(0,0);

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int) {
        super();
        this.data = new Vector<Int64>(length);
        var zero = ZERO;
        for (n in 0 ... length) this.data[n] = zero;
        this.length = length;
        this.desc = "[J";
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getElementBytesSize():Int return 8;
	{{ HAXE_METHOD_ANNOTATIONS }}
    public function getTypedArray() return data;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_J(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    inline public function get(index:Int):Int64 {
		checkBounds(index);
        return this.data[index];
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    inline public function set(index:Int, value:Int64):Void {
		checkBounds(index);
        this.data[index] = value;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getDynamic(index:Int):Dynamic {
	    return get(index);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function setDynamic(index:Int, value:Dynamic) {
	    set(index, value);
	}

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
    public override function clone() return fromArray(this.data.toArray());

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_J, to:JA_J, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
    }

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_J), srcPos, dstPos, length); }
}