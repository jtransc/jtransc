import haxe.io.Int32Array;

class HaxeIntArray extends HaxeBaseArray {
    public var data:Int32Array = null;

    public function new(length:Int) {
        super();
        this.data = new Int32Array(length);
        this.length = length;
    }

    static public function fromArray(items:Array<Dynamic>) {
        var out = new HaxeIntArray(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    inline public function get(index:Int):Int {
        return this.data[index];
    }

    inline public function set(index:Int, value:Int):Void {
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

    public override function clone__Ljava_lang_Object_():java_.lang.Object_ {
        var out = new HaxeIntArray(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:HaxeIntArray, to:HaxeIntArray, fromPos:Int, toPos:Int, length:Int) {
        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
    }
}