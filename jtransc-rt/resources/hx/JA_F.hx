import haxe.io.Float32Array;
import haxe.ds.Vector;

class JA_F extends JA_0 {
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

    public function getTypedArray() {
        #if flash
        var out = new Float32Array(this.length);
        for (n in 0 ... this.length) out[n] = this.get(n);
        return out;
        #else
        return data;
        #end
    }

    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_F(items.length);
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
        var out = new JA_F(length);
        copy(this, out, 0, 0, length);
        return out;
    }

    static public function copy(from:JA_F, to:JA_F, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
    }
}