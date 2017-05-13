package {
public class JA_D extends JA_0 {
	public var data: Vector.<Number>;

	public function JA_D(length: int, descriptor: String = '[D') {
		super(length, descriptor);
		this.data = new Vector.<Number>(length, true);
	}

	public function set(index: int, value: int): void {
		data[index] = value;
	}

	public function get(index: int): Number {
		return data[index];
	}

	override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int, overlapping: Boolean): void {
		var rdst: Vector.<Number> = (dst as JA_D).data;
		if (overlapping) {
			for (var n: int = len - 1; n >= 0; n--) rdst[dstPos + n] = this.data[srcPos + n];
		} else {
			for (var n: int = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}
}
}