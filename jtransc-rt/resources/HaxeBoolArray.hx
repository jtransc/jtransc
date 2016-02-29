import haxe.io.UInt8Array;
import haxe.io.Bytes;

class HaxeBoolArray extends HaxeBaseArray {
    public var data:UInt8Array = null;

    public function new(length:Int) {
        super();
        this.data = new UInt8Array(length);
        this.length = length;
        this.desc = "[Z";
    }

    static public function fromArray(items:Array<Dynamic>) {
        var out = new HaxeBoolArray (items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    static public function fromUInt8Array(items:UInt8Array) {
        var out = new HaxeBoolArray (items.length);
        for (n in 0 ... items.length) out.set(n, items[n] != 0);
        return out;
    }

    static public function fromBytes(bytes:Bytes) {
        var out = new HaxeBoolArray (bytes.length);
        var bytesData = bytes.getData();
        for (n in 0 ... bytes.length) out.set(n, Bytes.fastGet(bytesData, n) != 0);
        return out;
    }

    inline public function get(index:Int):Bool {
        return this.data[index] != 0;
    }

    inline public function set(index:Int, value:Bool):Void {
        this.data[index] = value ? 1 : 0;
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
        var out = new HaxeBoolArray (length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:HaxeBoolArray , to:HaxeBoolArray , fromPos:Int, toPos:Int, length:Int) {
        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
    }
}