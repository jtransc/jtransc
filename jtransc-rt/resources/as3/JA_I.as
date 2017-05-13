package {
	public class JA_I extends JA_0 {
		public var data: Vector.<int> = new Vector.<int>();

		public function JA_I(size: int, descriptor: String = '[I') {
			super(size, descriptor);
			data = new Vector.<int>(size, true);
		}

		static public function T(data: Vector.<int>): JA_I {
			var out: JA_I = new JA_I(data.length);
			var outd: Vector.<int> = out.data;
			for (var n: int = 0; n < data.length; n++) outd[n] = data[n];
			return out;
		}

		override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int): void {
			var rdst: Vector.<int> = (dst as JA_I).data;
			for (var n: int = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}
}