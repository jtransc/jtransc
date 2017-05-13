package {
public class Int32 {
	static public function compare(a: int, b: int): int {
		a |= 0;
		b |= 0;
		if (a == b) {
			return 0;
		} else if (a > b) {
			return 1;
		} else {
			return -1;
		}
	}

	static public function ucompare(a: int, b: int): int {
		if (a < 0) {
			if(b < 0) {
				return ~b - ~a | 0;
			} else {
				return 1;
			}
		}
		if (b < 0) {
			return -1;
		} else {
			return a - b | 0;
		}
	}

	static public function mul(a: int, b: int): int {
		return a * b;
	}
}
}