import haxe.io.Float32Array;

class HaxeFloatArray extends HaxeBaseArray {
    public var data:Float32Array = null;

    public function new(length:Int) {
        super();
        this.data = new Float32Array(length);
        this.length = length;
        this.desc = "[F";
    }

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new HaxeFloatArray(items.length);
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
        var out = new HaxeFloatArray(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:HaxeFloatArray, to:HaxeFloatArray, fromPos:Int, toPos:Int, length:Int) {
        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
    }
}