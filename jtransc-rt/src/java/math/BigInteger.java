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

import java.util.Random;

public class BigInteger extends Number implements Comparable<BigInteger> {
	public BigInteger(byte[] val) {
	}

	public BigInteger(int signum, byte[] magnitude) {
	}

	public BigInteger(String val, int radix) {
	}

	BigInteger(char[] val, int sign, int len) {
	}

	public BigInteger(String val) {
		this(val, 10);
	}

	public BigInteger(int numBits, Random rnd) {

	}

	public BigInteger(int bitLength, int certainty, Random rnd) {
	}

	native public static BigInteger probablePrime(int bitLength, Random rnd);

	native public BigInteger nextProbablePrime();

	native public static BigInteger valueOf(long val);

	public static final BigInteger ZERO = valueOf(0);
	public static final BigInteger ONE = valueOf(1);
	//private static final BigInteger TWO = valueOf(2);
	//private static final BigInteger NEGATIVE_ONE = valueOf(-1);
	public static final BigInteger TEN = valueOf(10);

	native public BigInteger add(BigInteger val);

	native public BigInteger subtract(BigInteger val);

	native public BigInteger multiply(BigInteger val);

	native public BigInteger divide(BigInteger val);

	native public BigInteger[] divideAndRemainder(BigInteger val);

	native public BigInteger remainder(BigInteger val);

	native public BigInteger pow(int exponent);

	native public BigInteger gcd(BigInteger val);

	native public BigInteger abs();

	native public BigInteger negate();

	native public int signum();

	native public BigInteger mod(BigInteger m);

	native public BigInteger modPow(BigInteger exponent, BigInteger m);

	native public BigInteger modInverse(BigInteger m);

	native public BigInteger shiftLeft(int n);

	native public BigInteger shiftRight(int n);

	native public BigInteger and(BigInteger val);

	native public BigInteger or(BigInteger val);

	native public BigInteger xor(BigInteger val);

	native public BigInteger not();

	native public BigInteger andNot(BigInteger val);

	native public boolean testBit(int n);

	native public BigInteger setBit(int n);

	native public BigInteger clearBit(int n);

	native public BigInteger flipBit(int n);

	native public int getLowestSetBit();

	native public int bitLength();

	native public int bitCount();

	native public boolean isProbablePrime(int certainty);

	native public int compareTo(BigInteger val);

	native public boolean equals(Object x);

	native public BigInteger min(BigInteger val);

	native public BigInteger max(BigInteger val);

	native public int hashCode();

	native public String toString(int radix);

	native public String toString();

	native public byte[] toByteArray();

	native public int intValue();

	native public long longValue();

	native public float floatValue();

	native public double doubleValue();
}
