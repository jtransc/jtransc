import haxe.ds.Vector;
import haxe.Int64;

#if (cpp)
import cpp.Int64;
import cpp.Pointer;
import cpp.NativeArray;
typedef __JA_J_Item = NativeInt64;
#else
typedef __JA_J_Item = NativeInt64;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_J extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }} public var data:Vector<__JA_J_Item> = null;
	{{ HAXE_FIELD_ANNOTATIONS }} static private var ZERO:__JA_J_Item  = 0;

	//#if cpp {{ HAXE_FIELD_ANNOTATIONS }} public var ptr:Pointer<__JA_J_Item> = null; #end

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int) {
        super();
        this.data = new Vector<__JA_J_Item>(length);
        var zero = ZERO;
        for (n in 0 ... length) this.data[n] = zero;
        this.length = length;
        this.elementShift = 3;
        this.desc = "[J";
		//#if cpp ptr = NativeArray.address(data.toData(), 0); #end
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

	//#if cpp
	//{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):__JA_J_Item return ptr[checkBounds(index)];
	//{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:__JA_J_Item):Void ptr[checkBounds(index)] = value;
	//#else
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function get(index:Int):__JA_J_Item { return this.data[checkBounds(index)]; }
	{{ HAXE_METHOD_ANNOTATIONS }} inline public function set(index:Int, value:__JA_J_Item):Void { this.data[checkBounds(index)] = value; }
	//#end

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
    public override function clone() return fromArray(this.data.toArray());

	{{ HAXE_METHOD_ANNOTATIONS }}
	public function fill(from: Int, to: Int, value: __JA_J_Item) {
		for (n in from ... to) set(n, value);
	}

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_J, to:JA_J, fromPos:Int, toPos:Int, length:Int) {
 		#if (cpp || flash)
 		Vector.blit(from.data, fromPos, to.data, toPos, length);
 		#else
	   	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
	        for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
	    }
	    #end
    }

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_J), srcPos, dstPos, length); }
}