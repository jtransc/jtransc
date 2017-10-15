package java.lang;

import com.jtransc.annotation.JTranscSync;

class IntegerTools {
	@JTranscSync
	static public int countDigits(int i, int radix) {
		if (radix == 10) return countDigits10(i);
		if (radix == 16) return countDigits16(i);
		if (radix == 2) return countDigits2(i);
		return countDigitsGeneric(i, radix);
	}

	@JTranscSync
	static public int countDigitsGeneric(int i, int radix) {
		boolean negative = (i < 0);
		int count = 0;
		if (negative) {
			i = -i;
			count++;
		}

		if (i == 0) {
			count++;
		} else {
			while (i != 0) {
				count++;
				i = Integer.divideUnsigned(i, radix);
			}
		}

		return count;
	}

	@JTranscSync
	static private int countDigits2(int n) {
		if (n < 0x0) return (n == Integer.MIN_VALUE) ? 33 : (1 + countDigits2(-n));
		return 32 - Integer.numberOfLeadingZeros(n);
	}

	@JTranscSync
	static private int countDigits16(int n) {
		if (n < 0x0) return (n == Integer.MIN_VALUE) ? 9 : (1 + countDigits16(-n));
		if (n < 0x10) return 1;
		if (n < 0x100) return 2;
		if (n < 0x1000) return 3;
		if (n < 0x10000) return 4;
		if (n < 0x100000) return 5;
		if (n < 0x1000000) return 6;
		if (n < 0x10000000) return 7;
		return 8;
	}

	@JTranscSync
	static private int countDigits10(int x) {
		if (x < 0) return (x == Integer.MIN_VALUE) ? 11 : (1 + countDigits10(-x));
		if (x < 10) return 1;
		if (x < 100) return 2;
		if (x < 1000) return 3;
		if (x < 10000) return 4;
		if (x < 100000) return 5;
		if (x < 1000000) return 6;
		if (x < 10000000) return 7;
		if (x < 100000000) return 8;
		if (x < 1000000000) return 9;
		return 10;


		//if (x > 99) {
		//	if (x < 1000000) {
		//		if (x < 10000) {
		//			return 3 + (x - 1000 >>> 31);
		//		} else {
		//			return 5 + (x - 100000 >>> 31);
		//		}
		//	} else {
		//		if (x < 100000000) {
		//			return 7 + (x - 10000000 >>> 31);
		//		} else {
		//			return 9 + (((x - 1000000000) & ~x) >>> 31);
		//		}
		//	}
		//} else if (x > 9) {
		//	return 1;
		//} else {
		//	return (x - 1 >>> 31);
		//}
	}

	@JTranscSync
	static public int writeInt(char[] out, int offset, int i, int radix) {
		if (radix < 2) throw new RuntimeException("Invalid radix");
		if (i == Integer.MIN_VALUE) {
			int count = countDigits(i, radix);
			int o = offset + count;
			while (i != 0) {
				out[--o] = Character.forDigit(Integer.remainderUnsigned(i, radix), radix);
				i = Integer.divideUnsigned(i, radix);
			}
			out[--o] = '-';
			return count;
		} else {
			boolean negative = (i < 0);
			int count = countDigits(i, radix);
			if (negative) i = -i;
			int o = offset + count;
			if (i == 0) {
				out[--o] = '0';
			} else {
				if (radix == 10) {
					while (i != 0) {
						final int v = i % 10;
						i = i / 10;
						out[--o] = Character.forDigit(v, radix);
					}
				} else {
					while (i != 0) {
						final int v = i % radix;
						i = i / radix;
						out[--o] = Character.forDigit(v, radix);
					}
				}
			}
			if (negative) {
				out[--o] = '-';
			}
			return count;
		}
	}
}
