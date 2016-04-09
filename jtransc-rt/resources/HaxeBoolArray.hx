import haxe.io.UInt8Array;
import haxe.io.Bytes;
import haxe.io.BytesData;

class HaxeBoolArray extends HaxeByteArray {
    public function new(length:Int) {
        super(length);
        this.desc = "[Z";
    }
}