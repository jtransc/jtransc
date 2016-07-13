import haxe.io.Int32Array;
import haxe.io.Bytes;
import haxe.ds.Vector;

class HaxeArrayInt extends HaxeArrayBase {
	#if flash
	public var data:Vector<Int> = null;
	#else
	public var data:Int32Array = null;
	#end

    public function new(length:Int) {
        super();
        #if flash
        this.data = new Vector<Int>(length);
        #else
        this.data = new Int32Array(length);
        #end
        this.length = length;
        this.desc = "[I";
    }

    public function getTypedArray() {
        #if flash
        var out = new Int32Array(this.length);
        for (n in 0 ... this.length) out[n] = this.get(n);
        return out;
        #else
        return data;
        #end
    }

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new HaxeArrayInt(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    public function getBytes() {
    	#if flash
    	var out = Bytes.alloc(this.length * 4);
    	for (n in 0 ... this.length) out.setInt32(n * 4, this.get(n));
    	return out;
    	#else
    	return data.view.buffer;
    	#end
    }

    static public function fromBytes(bytes:Bytes) {
        if (bytes == null) return null;
        var out = new HaxeArrayInt(0);
        out.length = Std.int(bytes.length / 4);
        for (n in 0 ... out.length) out.set(n, bytes.getInt32(n * 4));
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
        var out = new HaxeArrayInt(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:HaxeArrayInt, to:HaxeArrayInt, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
    }
}