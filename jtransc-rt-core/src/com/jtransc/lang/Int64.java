package com.jtransc.lang;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.internal.JTranscCType;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Int64 {
	static public final Int64 MAX_INT64 = new Int64(0x7FFFFFFF, 0xFFFFFFFF);
	static public final Int64 MIN_INT64 = new Int64(0x80000000, 0x00000000);
	static public final Int64 zero = new Int64(0, 0);
	static public final Int64 one = new Int64(0, 1);
	static public final Int64 MIN_VALUE = MIN_INT64;
	static public final Int64 MAX_VALUE = MAX_INT64;

	public int high;
	public int low;

	public Int64(int high, int low) {
		this.high = high;
		this.low = low;
	}

	static public Int64 make(int high, int low) {
		if (high == 0) {
			if (low == 0) return Int64.zero;
			if (low == 1) return Int64.one;
		}
		return new Int64(high, low);
	}

	static public Int64 ofInt(int value) {
		return Int64.make(value >> 31, value);
	}

	static public Int64 ofFloat(double f) {
		if (Double.isNaN(f) || !Double.isFinite(f)) throw new RuntimeException("Number is NaN or Infinite");
		double noFractions = f - (f % 1);
		// 2^53-1 and -2^53: these are parseable without loss of precision
		if (noFractions > 9007199254740991.0) throw new RuntimeException("Conversion overflow");
		if (noFractions < -9007199254740991.0) throw new RuntimeException("Conversion underflow");

		Int64 result = Int64.ofInt(0);
		boolean neg = noFractions < 0;
		double rest = neg ? -noFractions : noFractions;

		int i = 0;
		while (rest >= 1) {
			double curr = rest % 2;
			rest = rest / 2;
			if (curr >= 1) result = Int64.add(result, Int64.shl(Int64.ofInt(1), i));
			i++;
		}

		return neg ? Int64.neg(result) : result;
	}

	static public Int64 ofString(String sParam) {
		Int64 base = Int64.ofInt(10);
		Int64 current = Int64.ofInt(0);
		Int64 multiplier = Int64.ofInt(1);
		boolean sIsNegative = false;

		String s = sParam.trim();
		if (s.charAt(0) == '-') {
			sIsNegative = true;
			s = s.substring(1, s.length());
		}
		int len = s.length();

		for (int i = 0; i < len; i++) {
			int digitInt = JTranscCType.decodeDigit(s.charAt(len - 1 - i));

			Int64 digit = Int64.ofInt(digitInt);
			if (sIsNegative) {
				current = Int64.sub(current, Int64.mul(multiplier, digit));
				if (!Int64.isNeg(current)) throw new RuntimeException("NumberFormatError: Underflow");
			} else {
				current = Int64.add(current, Int64.mul(multiplier, digit));
				if (Int64.isNeg(current)) throw new RuntimeException("NumberFormatError: Overflow");
			}
			multiplier = Int64.mul(multiplier, base);
		}
		return current;
	}

	static public int toInt(Int64 a) {
		return a.low;
	}

	static public double toFloat(Int64 v) {
		if (Int64.isNeg(v)) {
			return Int64.eq(v, MIN_INT64) ? -9223372036854775808.0 : -Int64.toFloat(Int64.neg(v));
		} else {
			int lowf = v.low;
			int highf = v.high;
			return lowf + highf * Math.pow(2, 32);
		}
	}

	@JTranscMethodBody(target = "js", value = "return p0['{% FIELD com.jtransc.lang.Int64:high %}'] < 0;")
	static public boolean isNeg(Int64 a) {
		return a.high < 0;
	}

	@JTranscMethodBody(target = "js", value = "return (p0['{% FIELD com.jtransc.lang.Int64:high %}'] == 0) && (p0['{% FIELD com.jtransc.lang.Int64:low %}'] == 0);")
	static public boolean isZero(Int64 a) {
		return a.high == 0 && a.low == 0;
	}

	@JTranscMethodBody(target = "js", value = "return (p0['{% FIELD com.jtransc.lang.Int64:high %}'] != 0) || (p0['{% FIELD com.jtransc.lang.Int64:low %}'] != 0);")
	static public boolean isNotZero(Int64 a) {
		return a.high != 0 || a.low != 0;
	}

// Comparisons

	static public int compare(Int64 a, Int64 b) {
		int v = a.high - b.high;
		if (v == 0) v = Integer.compareUnsigned(a.low, b.low);
		return (a.high < 0) ? ((b.high < 0) ? v : -1) : ((b.high >= 0) ? v : 1);
	}

	static public int ucompare(Int64 a, Int64 b) {
		int v = Integer.compareUnsigned(a.high, b.high);
		return (v != 0) ? v : Integer.compareUnsigned(a.low, b.low);
	}

	@JTranscMethodBody(target = "js", value = "return (p0['{% FIELD com.jtransc.lang.Int64:high %}'] == p1['{% FIELD com.jtransc.lang.Int64:high %}']) && (p0['{% FIELD com.jtransc.lang.Int64:low %}'] == p1['{% FIELD com.jtransc.lang.Int64:low %}']);")
	static public boolean eq(Int64 a, Int64 b) {
		return (a.high == b.high) && (a.low == b.low);
	}

	@JTranscMethodBody(target = "js", value = "return (p0['{% FIELD com.jtransc.lang.Int64:high %}'] != p1['{% FIELD com.jtransc.lang.Int64:high %}']) || (p0['{% FIELD com.jtransc.lang.Int64:low %}'] != p1['{% FIELD com.jtransc.lang.Int64:low %}']);")
	static public boolean ne(Int64 a, Int64 b) {
		return (a.high != b.high) || (a.low != b.low);
	}

	static public boolean lt(Int64 a, Int64 b) {
		return Int64.compare(a, b) < 0;
	}

	static public boolean le(Int64 a, Int64 b) {
		return Int64.compare(a, b) <= 0;
	}

	static public boolean gt(Int64 a, Int64 b) {
		return Int64.compare(a, b) > 0;
	}

	static public boolean ge(Int64 a, Int64 b) {
		return Int64.compare(a, b) >= 0;
	}

// Strings

	@Override
	public String toString() {
		Int64 i = this;
		if (Int64.isZero(i)) return "0";
		String str = "";
		boolean neg = false;
		if (Int64.isNeg(i)) {
			neg = true;
			// i = -i; cannot negate here as --9223372036854775808 = -9223372036854775808
		}
		Int64 ten = Int64.ofInt(10);
		while (Int64.isNotZero(i)) {
			DivModResult r = Int64.divMod(i, ten);
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

	// Arithmetic
	static private class DivModResult {
		public final Int64 quotient;
		public final Int64 modulus;

		public DivModResult(final Int64 quotient, final Int64 modulus) {
			this.quotient = quotient;
			this.modulus = modulus;
		}
	}

	static public DivModResult divMod(Int64 dividend, Int64 divisor) {
		if (divisor.high == 0) {
			switch (divisor.low) {
				case 0:
					throw new RuntimeException("divide by zero");
				case 1:
					return new DivModResult(Int64.make(dividend.high, dividend.low), Int64.ofInt(0));
			}
		}
		boolean divSign = Int64.isNeg(dividend) != Int64.isNeg(divisor);
		Int64 modulus = Int64.isNeg(dividend) ? Int64.neg(dividend) : Int64.make(dividend.high, dividend.low);
		divisor = Int64.abs(divisor);

		Int64 quotient = Int64.ofInt(0);
		Int64 mask = Int64.ofInt(1);
		while (!Int64.isNeg(divisor)) {
			int cmp = Int64.ucompare(divisor, modulus);
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

	static public Int64 neg(Int64 x) {
		int high = ~x.high;
		int low = -x.low;
		if (low == 0) high = high + 1;
		return Int64.make(high, low);
	}

	static public Int64 add(Int64 a, Int64 b) {
		int high = a.high + b.high;
		int low = a.low + b.low;
		if (Integer.compareUnsigned(low, a.low) < 0) {
			high = high + 1;
		}
		return Int64.make(high, low);
	}

	static public Int64 sub(Int64 a, Int64 b) {
		int high = a.high - b.high;
		int low = a.low - b.low;
		if (Integer.compareUnsigned(a.low, b.low) < 0) {
			high = high - 1;
		}
		return Int64.make(high, low);
	}

	static public Int64 mul(Int64 a, Int64 b) {
		int al = a.low & 65535;
		int ah = a.low >>> 16;
		int bl = b.low & 65535;
		int bh = b.low >>> 16;
		int p00 = al * bl;
		int p10 = ah * bl;
		int p01 = al * bh;
		int p11 = ah * bh;
		int low;
		int high = (p11 + (p01 >>> 16)) + (p10 >>> 16);
		p01 = p01 << 16;
		low = p00 + p01;
		if (Integer.compareUnsigned(low, p01) < 0) high = high + 1;
		p10 = p10 << 16;
		low = low + p10;
		if (Integer.compareUnsigned(low, p10) < 0) high = high + 1;
		high = high + ((a.low * b.high) + (a.high * b.low));
		return Int64.make(high, low);
	}

	static public Int64 div(Int64 a, Int64 b) {
		return Int64.divMod(a, b).quotient;
	}

	static public Int64 mod(Int64 a, Int64 b) {
		return Int64.divMod(a, b).modulus;
	}

	static public Int64 rem(Int64 a, Int64 b) {
		return Int64.divMod(a, b).modulus;
	}

	// BIT-WISE
	static public Int64 not(Int64 x) {
		return Int64.make(~x.high, ~x.low);
	}

	static public Int64 and(Int64 a, Int64 b) {
		return Int64.make(a.high & b.high, a.low & b.low);
	}

	static public Int64 or(Int64 a, Int64 b) {
		return Int64.make(a.high | b.high, a.low | b.low);
	}

	static public Int64 xor(Int64 a, Int64 b) {
		return Int64.make(a.high ^ b.high, a.low ^ b.low);
	}

	static public Int64 shl(Int64 a, int b) {
		b &= 63;
		if (b == 0) {
			return Int64.make(a.high, a.low);
		} else if (b < 32) {
			return Int64.make(a.high << b | a.low >>> 32 - b, a.low << b);
		} else {
			return Int64.make(a.low << b - 32, 0);
		}
	}

	static public Int64 shr(Int64 a, int b) {
		b &= 63;
		if (b == 0) {
			return Int64.make(a.high, a.low);
		} else if (b < 32) {
			return Int64.make(a.high >> b, a.high << 32 - b | a.low >>> b);
		} else {
			return Int64.make(a.high >> 31, a.high >> b - 32);
		}
	}

	static public Int64 ushr(Int64 a, int b) {
		b &= 63;
		if (b == 0) {
			return Int64.make(a.high, a.low);
		} else if (b < 32) {
			return Int64.make(a.high >>> b, a.high << 32 - b | a.low >>> b);
		} else {
			return Int64.make(0, a.high >>> b - 32);
		}
	}

	static public int sign(Int64 a) {
		if (Int64.isNeg(a)) return -1;
		if (Int64.isNotZero(a)) return +1;
		return 0;
	}

	static public Int64 abs(Int64 a) {
		return Int64.isNeg(a) ? Int64.neg(a) : a;
	}

	@JTranscMethodBody(target = "js", value = "return p0;")
	native static public Int64 getInternal(long value);
}
