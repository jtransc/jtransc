package {
import flash.utils.ByteArray;
import avm2.intrinsics.memory.sxi8;

public class JA_B extends JA_0 {
	public var data: ByteArray;
	public function JA_B(length: int, descriptor: String = '[B') {
		super(length, descriptor);
		data = new ByteArray()
		data.length = length;
	}

	override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int, overlapping: Boolean): void {
		var rdst: ByteArray = (dst as JA_B).data;
		rdst.position = dstPos;
		rdst.writeBytes(this.data, srcPos, len);
	}

	public function set(index: int, value: int): void {
		data[index] = value;
	}

	public function get(index: int): int {
		return sxi8(data[index]);
	}
}
}