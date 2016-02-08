/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.math;

import java.io.Serializable;

public final class MathContext implements Serializable {
	private static final int DEFAULT_DIGITS = 9;
	private static final RoundingMode DEFAULT_ROUNDINGMODE = RoundingMode.HALF_UP;
	private static final int MIN_DIGITS = 0;
	public static final MathContext UNLIMITED = new MathContext(0, RoundingMode.HALF_UP);
	public static final MathContext DECIMAL32 = new MathContext(7, RoundingMode.HALF_EVEN);
	public static final MathContext DECIMAL64 = new MathContext(16, RoundingMode.HALF_EVEN);
	public static final MathContext DECIMAL128 = new MathContext(34, RoundingMode.HALF_EVEN);
	final int precision;
	final RoundingMode roundingMode;

	public MathContext(int setPrecision) {
		this(setPrecision, DEFAULT_ROUNDINGMODE);
	}

	public MathContext(int setPrecision, RoundingMode setRoundingMode) {
		if (setPrecision < MIN_DIGITS) throw new IllegalArgumentException("Digits < 0");
		if (setRoundingMode == null) throw new NullPointerException("null RoundingMode");
		precision = setPrecision;
		roundingMode = setRoundingMode;
	}

	public MathContext(String val) {
		boolean bad = false;
		int setPrecision;
		if (val == null) throw new NullPointerException("null String");
		try {
			if (!val.startsWith("precision=")) throw new RuntimeException();
			int fence = val.indexOf(' ');
			int off = 10;
			setPrecision = Integer.parseInt(val.substring(10, fence));
			if (!val.startsWith("roundingMode=", fence + 1)) throw new RuntimeException();
			off = fence + 1 + 13;
			String str = val.substring(off, val.length());
			roundingMode = RoundingMode.valueOf(str);
		} catch (RuntimeException re) {
			throw new IllegalArgumentException("bad string format");
		}

		if (setPrecision < MIN_DIGITS) throw new IllegalArgumentException("Digits < 0");
		precision = setPrecision;
	}

	public int getPrecision() {
		return precision;
	}

	public RoundingMode getRoundingMode() {
		return roundingMode;
	}

	public boolean equals(Object x) {
		MathContext mc;
		if (!(x instanceof MathContext)) return false;
		mc = (MathContext) x;
		return mc.precision == this.precision && mc.roundingMode == this.roundingMode;
	}

	public int hashCode() {
		return this.precision + roundingMode.hashCode() * 59;
	}

	public String toString() {
		return "precision=" + precision + " " + "roundingMode=" + roundingMode.toString();
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		if (precision < MIN_DIGITS) {
			String message = "MathContext: invalid digits in stream";
			throw new java.io.StreamCorruptedException(message);
		}
		if (roundingMode == null) {
			String message = "MathContext: null roundingMode in stream";
			throw new java.io.StreamCorruptedException(message);
		}
	}

}
