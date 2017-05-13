package {
	public class Long {
		static public var ZERO: Long = new Long(0, 0);

		public var high: int;
		public var low: int;

		public function Long(high: int, low: int) {
			this.high = high;
			this.low = low;
		}
	}
}