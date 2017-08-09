import haxe.ds.Vector;
import haxe.Int64;

#if (cpp)
import cpp.Int64;
import cpp.Pointer;
import cpp.NativeArray;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_J extends JA_0 {
	#if cpp
		{{ HAXE_FIELD_ANNOTATIONS }} public var data:Vector<Int> = null;
		{{ HAXE_FIELD_ANNOTATIONS }} public var ptr:Pointer<Int> = null;
	#else
		{{ HAXE_FIELD_ANNOTATIONS }} public var data:Vector<haxe.Int64> = null;
		{{ HAXE_FIELD_ANNOTATIONS }} static public var ZERO:haxe.Int64 = 0;
	#end

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int) {
        super();
        this.length = length;
        this.elementShift = 3;
        this.desc = "[J";
		#if cpp
	        this.data = new Vector<Int>(length * 2);
			ptr = NativeArray.address(data.toData(), 0);
			rawPtr = ptr.rawCast();
		#else
	        this.data = new Vector<haxe.Int64>(length * 2);
			for (n in 0 ... length) data.set(n, ZERO);
		#end
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getElementBytesSize():Int return 8;
	{{ HAXE_METHOD_ANNOTATIONS }}
    public function getTypedArray() return data;

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>) {
        if (items == null) return null;
        var out = new JA_J(items.length);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	#if cpp
	// Little Endian!!!!

	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):haxe.Int64 {
		var i = checkBounds(index) * 2;
		var low = ptr[i + 0];
		var high = ptr[i + 1];
		return haxe.Int64.make(high, low);
	}

	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:haxe.Int64):Void {
		var i = checkBounds(index) * 2;
		ptr[i + 0] = value.low;
		ptr[i + 1] = value.high;
	}
	#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):haxe.Int64 { return this.data[checkBounds(index)]; }
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:haxe.Int64):Void { this.data[checkBounds(index)] = value; }
	#end

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getDynamic(index:Int):Dynamic {
	    return get(index);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function setDynamic(index:Int, value:Dynamic) {
	    set(index, value);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
    public function join(separator:String) {
        var out = '';
        for (n in 0 ... length) {
            if (n != 0) out += separator;
            out += get(n);
        }
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    public override function clone() {
    	var out = new JA_J(length);
    	Vector.blit(this.data, 0, out.data, 0, out.data.length); // does this support overlapping?
    	return out;
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function fill(from: Int, to: Int, value: haxe.Int64) {
		#if cpp
			N.memsetN8(this.rawPtr, from, to - from, value);
		#else
			for (n in from ... to) set(n, value);
		#end
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_J, to:JA_J, fromPos:Int, toPos:Int, length:Int) {
    	#if cpp
    	fromPos *= 2;
    	toPos *= 2;
    	length *= 2;
    	#end

 		#if (cpp || flash)
 		Vector.blit(from.data, fromPos, to.data, toPos, length); // does this support overlapping?
 		#else
	   	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
	    #end
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function setArraySlice(startIndex: Int, array: Array<haxe.Int64>) {
		for (n in 0...array.length) this.set(startIndex + n, array[n]);
	}

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_J), srcPos, dstPos, length); }
}