import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;

class HaxeBoolArray extends HaxeByteArray {
    public function new(length:Int) {
        super(length);
        this.desc = "[Z";
    }

	public override function clone() {
		var out = new HaxeBoolArray(length);
		HaxeByteArray.copy(this, out, 0, 0, length);
		return out;
	}
}