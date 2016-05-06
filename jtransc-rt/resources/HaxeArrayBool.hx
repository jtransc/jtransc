import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;

class HaxeArrayBool extends HaxeArrayByte {
    public function new(length:Int) {
        super(length);
        this.desc = "[Z";
    }

	public override function clone() {
		var out = new HaxeArrayBool(length);
		HaxeArrayByte.copy(this, out, 0, 0, length);
		return out;
	}
}