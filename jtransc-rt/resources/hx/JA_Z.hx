import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;
import haxe.ds.Vector;

#if cpp
import cpp.Int8;
import cpp.Pointer;
import cpp.NativeArray;

typedef __JA_Z_Item = Vector<Int8>;
#else
typedef __JA_Z_Item = Bytes;
#end

{{ HAXE_CLASS_ANNOTATIONS }}
class JA_Z extends JA_B {
	{{ HAXE_CONSTRUCTOR_ANNOTATIONS }}
    public function new(length:Int, data: __JA_Z_Item = null) {
        super(length, data);
        this.desc = "[Z";
    }

	{{ HAXE_METHOD_ANNOTATIONS }}
	public override function clone() {
		var out = new JA_Z(length);
		JA_B.copy(this, out, 0, 0, length);
		return out;
	}
}