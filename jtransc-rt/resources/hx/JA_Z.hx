import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;

class JA_Z extends JA_B {
    public function new(length:Int) {
        super(length);
        this.desc = "[Z";
    }

	public override function clone() {
		var out = new JA_Z(length);
		JA_B.copy(this, out, 0, 0, length);
		return out;
	}
}