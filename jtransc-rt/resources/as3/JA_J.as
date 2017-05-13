package {
	public class JA_J extends JA_L {
		public function JA_J(length: int) {
			super(length, '[J');
			for (var n: int = 0; n < length; n++) data[n] = Long.ZERO;
		}
	}
}