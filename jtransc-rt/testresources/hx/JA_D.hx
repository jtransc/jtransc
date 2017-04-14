import haxe.io.Float64Array;

class JA_D extends JA_0 {
    public var data:Float64Array = null;

    public function new(length:Int, data:Float64Array = null) {
        super();
        if (data == null) data = new Float64Array(length); else length = data.length;
        this.data = data;
        this.length = length;
        this.desc = "[D";
    }

	override public function getElementBytesSize():Int return 8;
	override public function getArrayBufferView() return data.view;
    public function getTypedArray() return data;

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_D(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    inline public function get(index:Int):Float {
		checkBounds(index);
        return this.data[index];
    }

    inline public function set(index:Int, value:Float):Void {
		checkBounds(index);
        this.data[index] = value;
    }

	override public function getDynamic(index:Int):Dynamic {
	    return get(index);
	}

	override public function setDynamic(index:Int, value:Dynamic) {
	    set(index, value);
	}

    public function join(separator:String) {
        var out = '';
        for (n in 0 ... length) {
            if (n != 0) out += separator;
            out += get(n);
        }
        return out;
    }

    public override function clone() {
        var out = new JA_D(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:JA_D, to:JA_D, fromPos:Int, toPos:Int, length:Int) {
		var _from:Float64Array = from.data;
		var _to:Float64Array = to.data;
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) _to[toPos + n] = _from[fromPos + n];
    	} else {
	        for (n in 0 ... length) _to[toPos + n] = _from[fromPos + n];
		}
    }
}