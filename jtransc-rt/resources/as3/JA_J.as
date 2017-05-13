package {
public class JA_J extends JA_0 {
	public var data: Array;

	public function JA_J(length: int) {
		super(length, '[J');
		data = new Array(length);
		for (var n: int = 0; n < length; n++) data[n] = Long.zero;
	}

	public function set(index: int, value: Long): void {
		data[index] = value;
	}

	public function get(index: int): Long {
		return data[index];
	}

	override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int, overlapping: Boolean): void {
		var rdst: Array = (dst as JA_J).data;
		if (overlapping) {
			for (var n: int = len - 1; n >= 0; n--) rdst[dstPos + n] = this.data[srcPos + n];
		} else {
			for (var n: int = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}
}
}