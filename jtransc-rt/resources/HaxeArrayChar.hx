import haxe.io.UInt16Array;

class HaxeArrayChar extends HaxeArrayBase {
    public var data:UInt16Array = null;

    public function new(length:Int) {
        super();
        this.data = new UInt16Array(length);
        this.length = length;
        this.desc = "[C";
    }

    public function getTypedArray() return data;

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new HaxeArrayChar(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    inline public function get(index:Int):Int return this.data[checkBounds(index)];
    inline public function set(index:Int, value:Int):Void this.data[checkBounds(index)] = value;

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

    public override function clone() {
        var out = new HaxeArrayChar(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:HaxeArrayChar, to:HaxeArrayChar, fromPos:Int, toPos:Int, length:Int) {
        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
    }
}