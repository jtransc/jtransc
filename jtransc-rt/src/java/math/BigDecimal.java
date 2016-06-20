/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.math;

import java.io.Serializable;

public class BigDecimal extends Number implements Comparable<BigDecimal>, Serializable {
	public static final int ROUND_UP = 0;
	public static final int ROUND_DOWN = 1;
	public static final int ROUND_CEILING = 2;
	public static final int ROUND_FLOOR = 3;
	public static final int ROUND_HALF_UP = 4;
	public static final int ROUND_HALF_DOWN = 5;
	public static final int ROUND_HALF_EVEN = 6;
	public static final int ROUND_UNNECESSARY = 7;
	public static final BigDecimal ZERO = new BigDecimal(0, 0);
	public static final BigDecimal ONE = new BigDecimal(1, 0);
	public static final BigDecimal TEN = new BigDecimal(10, 0);

	private BigDecimal(int smallValue, int scale) {

	}

	private BigDecimal(long smallValue, int scale) {

	}

	public BigDecimal(char[] in, int offset, int len) {
	}

	public BigDecimal(char[] in, int offset, int len, MathContext mc) {
		this(in, offset, len);
		//inplaceRound(mc);
	}

	public BigDecimal(char[] in) {
		this(in, 0, in.length);
	}

	public BigDecimal(char[] in, MathContext mc) {
		this(in, 0, in.length);
		//inplaceRound(mc);
	}

	public BigDecimal(String val) {
		this(val.toCharArray(), 0, val.length());
	}

	public BigDecimal(String val, MathContext mc) {
		this(val.toCharArray(), 0, val.length());
		//inplaceRound(mc);
	}

	public BigDecimal(double val) {
	}

	public BigDecimal(double val, MathContext mc) {
		this(val);
		//inplaceRound(mc);
	}

	public BigDecimal(BigInteger val) {
		this(val, 0);
	}

	public BigDecimal(BigInteger val, MathContext mc) {
		this(val);
		//inplaceRound(mc);
	}

	public BigDecimal(BigInteger unscaledVal, int scale) {
		if (unscaledVal == null) {
			throw new NullPointerException("unscaledVal == null");
		}
		//this.scale = scale;
		//setUnscaledValue(unscaledVal);
	}

	public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
		this(unscaledVal, scale);
		//inplaceRound(mc);
	}

	public BigDecimal(int val) {
		this(val, 0);
	}

	public BigDecimal(int val, MathContext mc) {
		this(val, 0);
		//inplaceRound(mc);
	}

	public BigDecimal(long val) {
		this(val, 0);
	}

	public BigDecimal(long val, MathContext mc) {
		this(val);
		//inplaceRound(mc);
	}

	native public static BigDecimal valueOf(long unscaledVal, int scale);

	native public static BigDecimal valueOf(long unscaledVal);

	native public static BigDecimal valueOf(double val);

	native public BigDecimal add(BigDecimal augend);

	native public BigDecimal add(BigDecimal augend, MathContext mc);

	native public BigDecimal subtract(BigDecimal subtrahend);

	native public BigDecimal subtract(BigDecimal subtrahend, MathContext mc);

	native public BigDecimal multiply(BigDecimal multiplicand);

	native public BigDecimal multiply(BigDecimal multiplicand, MathContext mc);

	native public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode);

	native public BigDecimal divide(BigDecimal divisor, int scale, RoundingMode roundingMode);

	native public BigDecimal divide(BigDecimal divisor, int roundingMode);

	native public BigDecimal divide(BigDecimal divisor, RoundingMode roundingMode);

	native public BigDecimal divide(BigDecimal divisor);

	native public BigDecimal divide(BigDecimal divisor, MathContext mc);

	native public BigDecimal divideToIntegralValue(BigDecimal divisor);

	native public BigDecimal divideToIntegralValue(BigDecimal divisor, MathContext mc);

	native public BigDecimal remainder(BigDecimal divisor);

	native public BigDecimal remainder(BigDecimal divisor, MathContext mc);

	native public BigDecimal[] divideAndRemainder(BigDecimal divisor);

	native public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc);

	native public BigDecimal pow(int n);

	native public BigDecimal pow(int n, MathContext mc);

	native public BigDecimal abs();

	native public BigDecimal abs(MathContext mc);

	native public BigDecimal negate();

	native public BigDecimal negate(MathContext mc);

	native public BigDecimal plus();

	native public BigDecimal plus(MathContext mc);

	native public int signum();

	native public int scale();

	native public int precision();

	native public BigInteger unscaledValue();

	native public BigDecimal round(MathContext mc);

	native public BigDecimal setScale(int newScale, RoundingMode roundingMode);

	native public BigDecimal setScale(int newScale, int roundingMode);

	native public BigDecimal setScale(int newScale);

	native public BigDecimal movePointLeft(int n);

	native public BigDecimal movePointRight(int n);

	native public BigDecimal scaleByPowerOfTen(int n);

	native public BigDecimal stripTrailingZeros();

	native public int compareTo(BigDecimal val);

	native public boolean equals(Object x);

	native public BigDecimal min(BigDecimal val);

	native public BigDecimal max(BigDecimal val);

	native public int hashCode();

	native public String toString();

	native public String toEngineeringString();

	native public String toPlainString();

	native public BigInteger toBigInteger();

	native public BigInteger toBigIntegerExact();

	native public long longValue();

	native public long longValueExact();

	native public int intValue();

	native public int intValueExact();

	native public short shortValueExact();

	native public byte byteValueExact();

	native public float floatValue();

	native public double doubleValue();

	native public BigDecimal ulp();
}
