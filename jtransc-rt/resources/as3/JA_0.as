package {
	public class JA_0 extends {% CLASS java.lang.Object %} {
		public var length: int;
		public var descriptor: String = "";

		public function JA_0(length: int, descriptor: String) {
			this.length = length;
			this.descriptor = descriptor;
		}

		public function arraycopy(srcPos: int, dst: JA_0, dstPos: int, len: int): void {
			throw 'Must override JA_0.arraycopy for ' + this;
		}
	}
}