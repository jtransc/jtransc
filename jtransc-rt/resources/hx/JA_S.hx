import haxe.io.UInt16Array;

class JA_S extends JA_0 {
    public var data:UInt16Array = null;

    public function new(length:Int) {
        super();
        this.data = new UInt16Array(length);
        this.length = length;
        this.desc = "[S";
    }

    public function getTypedArray() return data;

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_S(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    inline public function get(index:Int):Int {
        return N.i2s(this.data[checkBounds(index)]);
    }

    inline public function set(index:Int, value:Int):Void {
		checkBounds(index);
        this.data[checkBounds(index)] = value;
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
        var out = new JA_S(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:JA_S, to:JA_S, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
    }
}