package {
	public class JA_D extends JA_0 {
		public var data: Vector.<Number>;

		public function JA_D(length: int, descriptor: String = '[D') {
			super(length, descriptor);
			this.data = new Vector.<Number>(length, true);
		}

		override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int): void {
			var rdst: Vector.<Number> = (dst as JA_D).data;
			for (var n: int = 0; n < len; n++) rdst[dstPos + n] = this.data[srcPos + n];
		}
	}
}