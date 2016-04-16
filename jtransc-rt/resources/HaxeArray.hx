import haxe.ds.Vector;

class HaxeArray extends HaxeBaseArray {
    public var data:Vector<Dynamic> = null;

    public function new(length:Int, desc:String) {
        super();
        this.data = new Vector<Dynamic>(length);
        this.length = length;
        this.desc = desc;
    }

    static public function fromArray(items:Array<Dynamic>, desc:String):HaxeArray {
        var out = new HaxeArray(items.length, desc);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    static public function fromArray1(items:Array<Dynamic>, desc:String):HaxeBaseArray {
        var out = create(items.length, desc);
        for (n in 0 ... items.length) out.setDynamic(n, items[n]);
        return out;
    }

    static public function fromArray2(items:Array<Array<Dynamic>>, desc:String) {
        var out = new HaxeArray(items.length, desc);
        for (n in 0 ... items.length) out.set(n, fromArray1(items[n], desc.substr(1)));
        return out;
    }

    override public function toArray():Array<Dynamic> return data.toArray();

    public function get(index:Int):Dynamic return this.data[checkBounds(index)];
    public function set(index:Int, value:Dynamic):Void this.data[checkBounds(index)] = value;

	override public function getDynamic(index:Int):Dynamic return get(index);
	override public function setDynamic(index:Int, value:Dynamic) set(index, value);

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

    static public function create(size:Int, desc:String):HaxeBaseArray {
		switch (desc) {
			case "[Z": return new HaxeBoolArray(size);
			case "[B": return new HaxeByteArray(size);
			case "[C": return new HaxeCharArray(size);
			case "[S": return new HaxeShortArray(size);
			case "[I": return new HaxeIntArray(size);
			case "[J": return new HaxeLongArray(size);
			case "[F": return new HaxeFloatArray(size);
			case "[D": return new HaxeDoubleArray(size);
			default: return new HaxeArray(size, desc);
		}
    }

    static public function createMulti(sizes:Array<Int>, desc:String):HaxeBaseArray {
        if (sizes.length == 0) return null;
	    var size = sizes[0];
		if (sizes.length == 1) return create(size, desc);
	    var sizes2 = sizes.slice(1);
	    var desc2 = desc.substr(1);
		return HaxeArray.fromArray([for (n in 0 ... size) createMulti(sizes2, desc2)], desc);
    }

    static public function createMultiSure(sizes:Array<Int>, desc:String):HaxeArray {
    	if (sizes.length < 2) throw 'Invalid multidimensional array';
    	return cast(createMulti(sizes, desc), HaxeArray);
    }
}