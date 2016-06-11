import haxe.io.Float32Array;
import haxe.ds.Vector;

class HaxeArrayFloat extends HaxeArrayBase {
	#if flash
	public var data:Vector<Float32> = null;
	#else
	public var data:Float32Array = null;
	#end

    public function new(length:Int) {
        super();
		#if flash
		this.data = new Vector<Float32>(length);
		#else
		this.data = new Float32Array(length);
		#end
        this.length = length;
        this.desc = "[F";
    }

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new HaxeArrayFloat(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

    inline public function get(index:Int):Float32 {
		checkBounds(index);
        return this.data[index];
    }

    inline public function set(index:Int, value:Float32):Void {
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
        var out = new HaxeArrayFloat(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:HaxeArrayFloat, to:HaxeArrayFloat, fromPos:Int, toPos:Int, length:Int) {
        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
    }
}