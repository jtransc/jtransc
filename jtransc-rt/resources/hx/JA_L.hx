import haxe.ds.Vector;

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_L extends JA_0 {
	{{ HAXE_FIELD_ANNOTATIONS }}
    public var data:Vector<Dynamic> = null;

	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, desc:String) {
        super();
        this.data = new Vector<Dynamic>(length);
        this.length = length;
        this.desc = desc;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray(items:Array<Dynamic>, desc:String):JA_L {
        if (items == null) return null;
        var out = new JA_L(items.length, desc);
        for (n in 0 ... items.length) out.set(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray1(items:Array<Dynamic>, desc:String):JA_0 {
        if (items == null) return null;
        var out = create(items.length, desc);
        for (n in 0 ... items.length) out.setDynamic(n, items[n]);
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function fromArray2(items:Array<Array<Dynamic>>, desc:String) {
        if (items == null) return null;
        var out = new JA_L(items.length, desc);
        for (n in 0 ... items.length) out.set(n, fromArray1(items[n], desc.substr(1)));
        return out;
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    override public function toArray():Array<Dynamic> return data.toArray();

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function toArrayOrEmpty(v:JA_L):Array<Dynamic> {
    	return (v != null) ? v.toArray() : [];
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    public function get(index:Int):Dynamic return this.data[checkBounds(index)];
	{{ HAXE_METHOD_ANNOTATIONS }}
    public function set(index:Int, value:Dynamic):Void this.data[checkBounds(index)] = value;

	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function getDynamic(index:Int):Dynamic return get(index);
	{{ HAXE_METHOD_ANNOTATIONS }}
	override public function setDynamic(index:Int, value:Dynamic) set(index, value);

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
    public override function clone() return fromArray(this.data.toArray(), this.desc);

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function copy(from:JA_L, to:JA_L, fromPos:Int, toPos:Int, length:Int) {
    	if (from == to && toPos > fromPos) {
			var n = length;
			while (--n >= 0) to.set(toPos + n, from.get(fromPos + n));
    	} else {
        	for (n in 0 ... length) to.set(toPos + n, from.get(fromPos + n));
		}
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function create(size:Int, desc:String):JA_0 {
		switch (desc) {
			case "[Z": return new JA_Z(size);
			case "[B": return new JA_B(size);
			case "[C": return new JA_C(size);
			case "[S": return new JA_S(size);
			case "[I": return new JA_I(size);
			case "[J": return new JA_J(size);
			case "[F": return new JA_F(size);
			case "[D": return new JA_D(size);
			default: return new JA_L(size, desc);
		}
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function createMulti(sizes:Array<Int>, desc:String):JA_0 {
        if (sizes.length == 0) return null;
	    var size = sizes[0];
		if (sizes.length == 1) return create(size, desc);
	    var sizes2 = sizes.slice(1);
	    var desc2 = desc.substr(1);
		return JA_L.fromArray([for (n in 0 ... size) createMulti(sizes2, desc2)], desc);
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
    static public function createMultiSure(sizes:Array<Int>, desc:String):JA_L {
    	if (sizes.length < 2) throw 'Invalid multidimensional array';
    	return cast(createMulti(sizes, desc), JA_L);
    }

    {{ HAXE_METHOD_ANNOTATIONS }} override public function copyTo(srcPos: Int, dst: JA_0, dstPos: Int, length: Int) { copy(this, cast(dst, JA_L), srcPos, dstPos, length); }
}