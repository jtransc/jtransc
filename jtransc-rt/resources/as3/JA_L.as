package {
	public class JA_L extends JA_0 {
		public var data: Array;

		public function JA_L(length: int, descriptor: String) {
			super(length, descriptor);
			data = new Array(length);
			for (var n: int = 0; n < length; n++) data[n] = null;
		}

		override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int): void {
			var rdst: Array = (dst as JA_L).data;
			for (var n: int = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}
}