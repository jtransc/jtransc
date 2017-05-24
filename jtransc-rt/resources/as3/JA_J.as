package {
public class JA_J extends JA_0 {
	public var data: Array;

	public function JA_J(length: int) {
		super(length, '[J');
		data = new Array(length);
		for (var n: int = 0; n < length; n++) data[n] = Int64.zero;
	}

	public final function set(index: int, value: Int64): void {
		data[index] = value;
	}

	public function get(index: int): Int64 {
		return data[index];
	}

	override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int, overlapping: Boolean): void {
		var rdst: Array = (dst as JA_J).data;
		var n: int;
		if (overlapping) {
			for (n = len - 1; n >= 0; n--) rdst[dstPos + n] = this.data[srcPos + n];
		} else {
			for (n = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}
}
}