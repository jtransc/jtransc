package {

import DivModResult;
import Int32;

public class Int64 {
	static public var MAX_INT64: Int64 = new Int64(0x7FFFFFFF, 0xFFFFFFFF);
	static public var MIN_INT64: Int64 = new Int64(0x80000000, 0x00000000);
	static public var zero: Int64 = new Int64(0, 0);
	static public var one: Int64 = new Int64(0, 1);
	static public var MIN_VALUE: Int64 = MIN_INT64;
	static public var MAX_VALUE: Int64 = MAX_INT64;

	public var high: int;
	public var low: int;

	public function Int64(high: int, low: int) {
		this.high = high;
		this.low = low;
	}

	static public function make(high: int, low: int): Int64 {
		if (high == 0) {
			if (low == 0) return Int64.zero;
			if (low == 1) return Int64.one;
		}
		return new Int64(high, low);
	}

	static public function ofInt(value: int): Int64 {
		return Int64.make(value >> 31, value);
	}

	static public function ofFloat(f: Number): Int64 {
		if (isNaN(f) || !isFinite(f)) throw new Error("Number is NaN or Infinite");
		var noFractions: Number = f - (f % 1);
		// 2^53-1 and -2^53: these are parseable without loss of precision
		if (noFractions > 9007199254740991.0) throw new Error("Conversion overflow");
		if (noFractions < -9007199254740991.0) throw new Error("Conversion underflow");

		var result: Int64 = Int64.ofInt(0);
		var neg: Boolean = noFractions < 0;
		var rest: Number = neg ? -noFractions : noFractions;

		var i: int = 0;
		while (rest >= 1) {
			var curr: Number = rest % 2;
			rest = rest / 2;
			if (curr >= 1) result = Int64.add(result, Int64.shl(Int64.ofInt(1), i));
			i++;
		}

		return neg ? Int64.neg(result) : result;
	}

	static public function ofString(sParam: String): Int64 {
		var base: Int64 = Int64.ofInt(10);
		var current: Int64 = Int64.ofInt(0);
		var multiplier: Int64 = Int64.ofInt(1);
		var sIsNegative: Boolean = false;

		var s: String = sParam.replace(/^\s+/, '').replace(/\s+$/, '');
		if (s.charAt(0) == '-') {
			sIsNegative = true;
			s = s.substring(1, s.length());
		}
		var len: int = s.length();

		for (var i: int = 0; i < len; i++) {
			var digitInt: int = parseInt(s.substr(len - 1 - i, 1));

			var digit: Int64 = Int64.ofInt(digitInt);
			if (sIsNegative) {
				current = Int64.sub(current, Int64.mul(multiplier, digit));
				if (!Int64.isNeg(current)) throw new Error("NumberFormatError: Underflow");
			} else {
				current = Int64.add(current, Int64.mul(multiplier, digit));
				if (Int64.isNeg(current)) throw new Error("NumberFormatError: Overflow");
			}
			multiplier = Int64.mul(multiplier, base);
		}
		return current;
	}

	static public function toInt(a: Int64): int {
		return a.low;
	}

	static public function toFloat(v: Int64): Number {
		if (Int64.isNeg(v)) {
			return Int64.eq(v, MIN_INT64) ? -9223372036854775808.0 : -Int64.toFloat(Int64.neg(v));
		} else {
			var lowf: int = v.low;
			var highf: int = v.high;
			return lowf + highf * Math.pow(2, 32);
		}
	}

	static public function isNeg(a: Int64): Boolean {
		return a.high < 0;
	}

	static public function isZero(a: Int64): Boolean {
		return a.high == 0 && a.low == 0;
	}

	static public function isNotZero(a: Int64): Boolean {
		return a.high != 0 || a.low != 0;
	}

// Comparisons

	static private function Integer_compare(a: int, b: int): int { return Int32.compare(a, b); }
	static private function Integer_compareUnsigned(a: int, b: int): int { return Int32.ucompare(a, b); }

	//static private function Integer_compareUnsigned(a: int, b: int): int {
	//	var au: uint = a;
	//	var bu: uint = b;
	//	if (au < bu) return -1;
	//	if (au > bu) return +1;
	//	return 0;
	//}


	static public function compare(a: Int64, b: Int64): int {
		var v: int = a.high - b.high;
		if (v == 0) v = Integer_compareUnsigned(a.low, b.low);
		return (a.high < 0) ? ((b.high < 0) ? v : -1) : ((b.high >= 0) ? v : 1);
	}

	static public function ucompare(a: Int64, b: Int64): int {
		var v: int = Integer_compareUnsigned(a.high, b.high);
		return (v != 0) ? v : Integer_compareUnsigned(a.low, b.low);
	}

	static public function eq(a: Int64, b: Int64): Boolean {
		return (a.high == b.high) && (a.low == b.low);
	}

	static public function ne(a: Int64, b: Int64): Boolean {
		return (a.high != b.high) || (a.low != b.low);
	}

	static public function lt(a: Int64, b: Int64): Boolean {
		return Int64.compare(a, b) < 0;
	}

	static public function le(a: Int64, b: Int64): Boolean {
		return Int64.compare(a, b) <= 0;
	}

	static public function gt(a: Int64, b: Int64): Boolean {
		return Int64.compare(a, b) > 0;
	}

	static public function ge(a: Int64, b: Int64): Boolean {
		return Int64.compare(a, b) >= 0;
	}

	// Strings
	public function toString(): String {
		var i: Int64 = this;
		if (Int64.isZero(i)) return "0";
		var str: String = "";
		var neg: Boolean = false;
		if (Int64.isNeg(i)) {
			neg = true;
			// i = -i; cannot negate here as --9223372036854775808 = -9223372036854775808
		}
		var ten: Int64 = Int64.ofInt(10);
		while (Int64.isNotZero(i)) {
			var r: DivModResult = Int64.divMod(i, ten);
			if (Int64.isNeg(r.modulus)) {
				str = Int64.neg(r.modulus).low + str;
				i = Int64.neg(r.quotient);
			} else {
				str = r.modulus.low + str;
				i = r.quotient;
			}
		}
		if (neg) str = "-" + str;
		return str;
	}

	static public function divMod(dividend: Int64, divisor: Int64): DivModResult {
		if (divisor.high == 0) {
			switch (divisor.low) {
				case 0:
					throw new Error("divide by zero");
				case 1:
					return new DivModResult(Int64.make(dividend.high, dividend.low), Int64.ofInt(0));
			}
		}
		var divSign: Boolean = Int64.isNeg(dividend) != Int64.isNeg(divisor);
		var modulus: Int64 = Int64.isNeg(dividend) ? Int64.neg(dividend) : Int64.make(dividend.high, dividend.low);
		divisor = Int64.abs(divisor);

		var quotient: Int64 = Int64.ofInt(0);
		var mask: Int64 = Int64.ofInt(1);
		while (!Int64.isNeg(divisor)) {
			var cmp: int = Int64.ucompare(divisor, modulus);
			divisor = Int64.shl(divisor, 1);
			mask = Int64.shl(mask, 1);
			if (cmp >= 0) {
				break;
			}
		}
		while (Int64.ne(mask, Int64.ofInt(0))) {
			if (Int64.ucompare(modulus, divisor) >= 0) {
				quotient = Int64.or(quotient, mask);
				modulus = Int64.sub(modulus, divisor);
			}
			mask = Int64.ushr(mask, 1);
			divisor = Int64.ushr(divisor, 1);
		}
		if (divSign) quotient = Int64.neg(quotient);
		if (Int64.isNeg(dividend)) modulus = Int64.neg(modulus);
		return new DivModResult(quotient, modulus);
	}

	static public function neg(x: Int64): Int64 {
		var high: int = ~x.high;
		var low: int = -x.low;
		if (low == 0) high = high + 1;
		return Int64.make(high, low);
	}

	static public function add(a: Int64, b: Int64): Int64 {
		var high: int = a.high + b.high;
		var low: int = a.low + b.low;
		if (Integer_compareUnsigned(low, a.low) < 0) {
			high = high + 1;
		}
		return Int64.make(high, low);
	}

	static public function sub(a: Int64, b: Int64): Int64 {
		var high: int = a.high - b.high;
		var low: int = a.low - b.low;
		if (Integer_compareUnsigned(a.low, b.low) < 0) {
			high = high - 1;
		}
		return Int64.make(high, low);
	}

	static public function mul(a: Int64, b: Int64): Int64 {
		var al:int = a.low & 65535;
		var ah:int = a.low >>> 16;
		var bl:int = b.low & 65535;
		var bh:int = b.low >>> 16;
		var p00:int = Int32.mul(al,bl);
		var p10:int = Int32.mul(ah,bl);
		var p01:int = Int32.mul(al,bh);
		var p11:int = Int32.mul(ah,bh);
		var low:int = p00;
		var high:int = (p11 + (p01 >>> 16) | 0) + (p10 >>> 16) | 0;
		p01 = p01 << 16;
		low = p00 + p01 | 0;
		if (Int32.ucompare(low,p01) < 0) high = high + 1 | 0;
		p10 = p10 << 16;
		low = low + p10 | 0;
		if (Int32.ucompare(low,p10) < 0) high = high + 1 | 0;
		high = high + (Int32.mul(a.low,b.high) + Int32.mul(a.high,b.low) | 0) | 0;
		return Int64.make(high,low);

	}

	static public function div(a: Int64, b: Int64): Int64 {
		return Int64.divMod(a, b).quotient;
	}

	static public function mod(a: Int64, b: Int64): Int64 {
		return Int64.divMod(a, b).modulus;
	}

	static public function rem(a: Int64, b: Int64): Int64 {
		return Int64.divMod(a, b).modulus;
	}

	// BIT-WISE
	static public function not(x: Int64): Int64 {
		return Int64.make(~x.high, ~x.low);
	}

	static public function and(a: Int64, b: Int64): Int64 {
		return Int64.make(a.high & b.high, a.low & b.low);
	}

	static public function or(a: Int64, b: Int64): Int64 {
		return Int64.make(a.high | b.high, a.low | b.low);
	}

	static public function xor(a: Int64, b: Int64): Int64 {
		return Int64.make(a.high ^ b.high, a.low ^ b.low);
	}

	static public function shl(a: Int64, b: int): Int64 {
		b &= 63;
		if (b == 0) {
			return Int64.make(a.high, a.low);
		} else if (b < 32) {
			return Int64.make(a.high << b | a.low >>> 32 - b, a.low << b);
		} else {
			return Int64.make(a.low << b - 32, 0);
		}
	}

	static public function shr(a: Int64, b: int): Int64 {
		b &= 63;
		if (b == 0) {
			return Int64.make(a.high, a.low);
		} else if (b < 32) {
			return Int64.make(a.high >> b, a.high << 32 - b | a.low >>> b);
		} else {
			return Int64.make(a.high >> 31, a.high >> b - 32);
		}
	}

	static public function ushr(a: Int64, b: int): Int64 {
		b &= 63;
		if (b == 0) {
			return Int64.make(a.high, a.low);
		} else if (b < 32) {
			return Int64.make(a.high >>> b, a.high << 32 - b | a.low >>> b);
		} else {
			return Int64.make(0, a.high >>> b - 32);
		}
	}

	static public function sign(a: Int64): int {
		if (Int64.isNeg(a)) return -1;
		if (Int64.isNotZero(a)) return +1;
		return 0;
	}

	static public function abs(a: Int64): Int64 {
		return Int64.isNeg(a) ? Int64.neg(a) : a;
	}

	static public function getInternal(value: Int64): Int64 {
		return value;
	}

}

}
