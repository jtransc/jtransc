import haxe.ds.Vector;

class HaxeArray extends HaxeBaseArray {
    public var data:Vector<Dynamic> = null;

    public function new(length:Int, desc:String) {
        super();
        this.data = new Vector<Dynamic>(length);
        this.length = length;
        this.desc = desc;
    }

    static public function fromArray(items:Array<Dynamic>, desc:String) {
        var out = new HaxeArray(items.length, desc);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    public function toArray():Array<Dynamic> {
        return data.toArray();
    }

    public function get(index:Int):Dynamic { return this.data[checkBounds(index)]; }
    public function set(index:Int, value:Dynamic):Void { this.data[checkBounds(index)] = value; }

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
        return fromArray(this.data.toArray(), this.desc);
    }

    static public function copy(from:HaxeArray, to:HaxeArray, fromPos:Int, toPos:Int, length:Int) {
        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
    }
}