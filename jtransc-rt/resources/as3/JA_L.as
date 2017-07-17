package {
public class JA_L extends JA_0 {
	public var data: Array;

	public function JA_L(length: int, descriptor: String) {
		super(length, descriptor);
		data = new Array(length);
		for (var n: int = 0; n < length; n++) data[n] = null;
	}

	public function set(index: int, value: {% CLASS java.lang.Object %}): void {
		data[index] = value;
	}

	public function get(index: int): {% CLASS java.lang.Object %} {
		return data[index];
	}

	override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int, overlapping: Boolean): void {
		var rdst: Array = (dst as JA_L).data;
		var n: int;
		if (overlapping) {
			for (n = len - 1; n >= 0; n--) rdst[dstPos + n] = this.data[srcPos + n];
		} else {
			for (n = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}

	static public function create(size: int, desc: String): JA_0  {
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

	static public function createMultiSure(desc: String, sizes: Array): JA_0 {
		return _createMultiSure(desc, 0, sizes);
	}

	static public function _createMultiSure(desc: String, index: int, sizes: Array): JA_0 {
		if (desc.substr(0, 1) != "[") return null;
		if (index >= sizes.length - 1) return JA_L.create(sizes[index], desc);
		var len: int = sizes[index];
		var o: JA_L = new JA_L(len, desc);
		var desc2: String = desc.substr(1);
		for (var n: int = 0; n < len; n++) {
			o.data[n] = JA_L._createMultiSure(desc2, index + 1, sizes);
		}
		return o;
	}

	static public function fromArray(desc: String, data: Array): JA_L {
		var len: int = data.length;
		var o: JA_L = new JA_L(len, desc);
		for (var n: int = 0; n < len; n++) {
			o.data[n] = data[n];
		}
		return o;
	}

	static public function T0(desc: String): JA_L { return fromArray(desc, []); }
	static public function T1(desc: String, a:*): JA_L { return fromArray(desc, [a]); }
	static public function T2(desc: String, a:*, b:*): JA_L { return fromArray(desc, [a, b]); }
	static public function T3(desc: String, a:*, b:*, c:*): JA_L { return fromArray(desc, [a, b, c]); }
	static public function T4(desc: String, a:*, b:*, c:*, d:*): JA_L { return fromArray(desc, [a, b, c, d]); }
}
}