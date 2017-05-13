package {

import DivModResult;

public class Long {
	static public var MAX_INT64: Long = new Long(0x7FFFFFFF, 0xFFFFFFFF);
	static public var MIN_INT64: Long = new Long(0x80000000, 0x00000000);
	static public var zero: Long = new Long(0, 0);
	static public var one: Long = new Long(0, 1);
	static public var MIN_VALUE: Long = MIN_INT64;
	static public var MAX_VALUE: Long = MAX_INT64;

	public var high: int;
	public var low: int;

	public function Long(high: int, low: int) {
		this.high = high;
		this.low = low;
	}

	static public function make(high: int, low: int): Long {
		if (high == 0) {
			if (low == 0) return Long.zero;
			if (low == 1) return Long.one;
		}
		return new Long(high, low);
	}

	static public function ofInt(value: int): Long {
		return Long.make(value >> 31, value);
	}

	static public function ofFloat(f: Number): Long {
		if (isNaN(f) || !isFinite(f)) throw new Error("Number is NaN or Infinite");
		var noFractions: Number = f - (f % 1);
		// 2^53-1 and -2^53: these are parseable without loss of precision
		if (noFractions > 9007199254740991.0) throw new Error("Conversion overflow");
		if (noFractions < -9007199254740991.0) throw new Error("Conversion underflow");

		var result: Long = Long.ofInt(0);
		var neg: Boolean = noFractions < 0;
		var rest: Number = neg ? -noFractions : noFractions;

		var i: int = 0;
		while (rest >= 1) {
			var curr: Number = rest % 2;
			rest = rest / 2;
			if (curr >= 1) result = Long.add(result, Long.shl(Long.ofInt(1), i));
			i++;
		}

		return neg ? Long.neg(result) : result;
	}

	static public function ofString(sParam: String): Long {
		var base: Long = Long.ofInt(10);
		var current: Long = Long.ofInt(0);
		var multiplier: Long = Long.ofInt(1);
		var sIsNegative: Boolean = false;

		var s: String = sParam.replace(/^\s+/, '').replace(/\s+$/, '');
		if (s.charAt(0) == '-') {
			sIsNegative = true;
			s = s.substring(1, s.length());
		}
		var len: int = s.length();

		for (var i: int = 0; i < len; i++) {
			var digitInt: int = parseInt(s.substr(len - 1 - i, 1));

			var digit: Long = Long.ofInt(digitInt);
			if (sIsNegative) {
				current = Long.sub(current, Long.mul(multiplier, digit));
				if (!Long.isNeg(current)) throw new Error("NumberFormatError: Underflow");
			} else {
				current = Long.add(current, Long.mul(multiplier, digit));
				if (Long.isNeg(current)) throw new Error("NumberFormatError: Overflow");
			}
			multiplier = Long.mul(multiplier, base);
		}
		return current;
	}

	static public function toInt(a: Long): int {
		return a.low;
	}

	static public function toFloat(v: Long): Number {
		if (Long.isNeg(v)) {
			return Long.eq(v, MIN_INT64) ? -9223372036854775808.0 : -Long.toFloat(Long.neg(v));
		} else {
			var lowf: int = v.low;
			var highf: int = v.high;
			return lowf + highf * Math.pow(2, 32);
		}
	}

	static public function isNeg(a: Long): Boolean {
		return a.high < 0;
	}

	static public function isZero(a: Long): Boolean {
		return a.high == 0 && a.low == 0;
	}

	static public function isNotZero(a: Long): Boolean {
		return a.high != 0 || a.low != 0;
	}

// Comparisons

	static private function Integer_compareUnsigned(a: int, b: int): int {
		var au: uint = a;
		var bu: uint = b;
		if (au < bu) return -1;
		if (au > bu) return +1;
		return 0;
	}


	static public function compare(a: Long, b: Long): int {
		var v: int = a.high - b.high;
		if (v == 0) v = Integer_compareUnsigned(a.low, b.low);
		return (a.high < 0) ? ((b.high < 0) ? v : -1) : ((b.high >= 0) ? v : 1);
	}

	static public function ucompare(a: Long, b: Long): int {
		var v: int = Integer_compareUnsigned(a.high, b.high);
		return (v != 0) ? v : Integer_compareUnsigned(a.low, b.low);
	}

	static public function eq(a: Long, b: Long): Boolean {
		return (a.high == b.high) && (a.low == b.low);
	}

	static public function ne(a: Long, b: Long): Boolean {
		return (a.high != b.high) || (a.low != b.low);
	}

	static public function lt(a: Long, b: Long): Boolean {
		return Long.compare(a, b) < 0;
	}

	static public function le(a: Long, b: Long): Boolean {
		return Long.compare(a, b) <= 0;
	}

	static public function gt(a: Long, b: Long): Boolean {
		return Long.compare(a, b) > 0;
	}

	static public function ge(a: Long, b: Long): Boolean {
		return Long.compare(a, b) >= 0;
	}

	// Strings
	public function toString(): String {
		var i: Long = this;
		if (Long.isZero(i)) return "0";
		var str: String = "";
		var neg: Boolean = false;
		if (Long.isNeg(i)) {
			neg = true;
			// i = -i; cannot negate here as --9223372036854775808 = -9223372036854775808
		}
		var ten: Long = Long.ofInt(10);
		while (Long.isNotZero(i)) {
			var r: DivModResult = Long.divMod(i, ten);
			if (Long.isNeg(r.modulus)) {
				str = Long.neg(r.modulus).low + str;
				i = Long.neg(r.quotient);
			} else {
				str = r.modulus.low + str;
				i = r.quotient;
			}
		}
		if (neg) str = "-" + str;
		return str;
	}

	static public function divMod(dividend: Long, divisor: Long): DivModResult {
		if (divisor.high == 0) {
			switch (divisor.low) {
				case 0:
					throw new Error("divide by zero");
				case 1:
					return new DivModResult(Long.make(dividend.high, dividend.low), Long.ofInt(0));
			}
		}
		var divSign: Boolean = Long.isNeg(dividend) != Long.isNeg(divisor);
		var modulus: Long = Long.isNeg(dividend) ? Long.neg(dividend) : Long.make(dividend.high, dividend.low);
		divisor = Long.abs(divisor);

		var quotient: Long = Long.ofInt(0);
		var mask: Long = Long.ofInt(1);
		while (!Long.isNeg(divisor)) {
			var cmp: int = Long.ucompare(divisor, modulus);
			divisor = Long.shl(divisor, 1);
			mask = Long.shl(mask, 1);
			if (cmp >= 0) {
				break;
			}
		}
		while (Long.ne(mask, Long.ofInt(0))) {
			if (Long.ucompare(modulus, divisor) >= 0) {
				quotient = Long.or(quotient, mask);
				modulus = Long.sub(modulus, divisor);
			}
			mask = Long.ushr(mask, 1);
			divisor = Long.ushr(divisor, 1);
		}
		if (divSign) quotient = Long.neg(quotient);
		if (Long.isNeg(dividend)) modulus = Long.neg(modulus);
		return new DivModResult(quotient, modulus);
	}

	static public function neg(x: Long): Long {
		var high: int = ~x.high;
		var low: int = -x.low;
		if (low == 0) high = high + 1;
		return Long.make(high, low);
	}

	static public function add(a: Long, b: Long): Long {
		var high: int = a.high + b.high;
		var low: int = a.low + b.low;
		if (Integer_compareUnsigned(low, a.low) < 0) {
			high = high + 1;
		}
		return Long.make(high, low);
	}

	static public function sub(a: Long, b: Long): Long {
		var high: int = a.high - b.high;
		var low: int = a.low - b.low;
		if (Integer_compareUnsigned(a.low, b.low) < 0) {
			high = high - 1;
		}
		return Long.make(high, low);
	}

	static public function mul(a: Long, b: Long): Long {
		var al : int= a.low & 0xffff;
		var ah : int= a.low >>> 16;
		var bl : int= b.low & 0xffff;
		var bh : int= b.low >>> 16;
		var p00 : int= al * bl;
		var p10 : int= ah * bl;
		var p01 : int= al * bh;
		var p11 : int= ah * bh;
		var low: int;
		var high : int= (p11 + (p01 >>> 0x10)) + (p10 >>> 0x10);
		p01 = p01 << 0x10;
		low = p00 + p01;
		if (Integer_compareUnsigned(low, p01) < 0) high = high + 1;
		p10 = p10 << 0x10;
		low = low + p10;
		if (Integer_compareUnsigned(low, p10) < 0) high = high + 1;
		high = high + ((a.low * b.high) + (a.high * b.low));
		return Long.make(high, low);
	}

	static public function div(a: Long, b: Long): Long {
		return Long.divMod(a, b).quotient;
	}

	static public function mod(a: Long, b: Long): Long {
		return Long.divMod(a, b).modulus;
	}

	static public function rem(a: Long, b: Long): Long {
		return Long.divMod(a, b).modulus;
	}

	// BIT-WISE
	static public function not(x: Long): Long {
		return Long.make(~x.high, ~x.low);
	}

	static public function and(a: Long, b: Long): Long {
		return Long.make(a.high & b.high, a.low & b.low);
	}

	static public function or(a: Long, b: Long): Long {
		return Long.make(a.high | b.high, a.low | b.low);
	}

	static public function xor(a: Long, b: Long): Long {
		return Long.make(a.high ^ b.high, a.low ^ b.low);
	}

	static public function shl(a: Long, b: int): Long {
		b &= 63;
		if (b == 0) {
			return Long.make(a.high, a.low);
		} else if (b < 32) {
			return Long.make(a.high << b | a.low >>> 32 - b, a.low << b);
		} else {
			return Long.make(a.low << b - 32, 0);
		}
	}

	static public function shr(a: Long, b: int): Long {
		b &= 63;
		if (b == 0) {
			return Long.make(a.high, a.low);
		} else if (b < 32) {
			return Long.make(a.high >> b, a.high << 32 - b | a.low >>> b);
		} else {
			return Long.make(a.high >> 31, a.high >> b - 32);
		}
	}

	static public function ushr(a: Long, b: int): Long {
		b &= 63;
		if (b == 0) {
			return Long.make(a.high, a.low);
		} else if (b < 32) {
			return Long.make(a.high >>> b, a.high << 32 - b | a.low >>> b);
		} else {
			return Long.make(0, a.high >>> b - 32);
		}
	}

	static public function sign(a: Long): int {
		if (Long.isNeg(a)) return -1;
		if (Long.isNotZero(a)) return +1;
		return 0;
	}

	static public function abs(a: Long): Long {
		return Long.isNeg(a) ? Long.neg(a) : a;
	}

	static public function getInternal(value: Long): Long {
		return value;
	}

}

}
