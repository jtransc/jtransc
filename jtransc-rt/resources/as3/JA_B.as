package {
	import flash.utils.ByteArray;

	public class JA_B extends JA_0 {
		public var data: ByteArray;
		public function JA_B(length: int, descriptor: String = '[B') {
			super(length, descriptor);
			data = new ByteArray()
			data.length = length;
		}

		override public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int): void {
			var rdst: ByteArray = (dst as JA_B).data;
			rdst.position = dstPos;
			rdst.writeBytes(this.data, srcPos, len);
		}
	}
}