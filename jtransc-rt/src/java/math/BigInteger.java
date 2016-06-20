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
import java.util.Random;

public class BigInteger extends Number implements Comparable<BigInteger>, Serializable {
	public static final BigInteger ZERO = new BigInteger(0, 0);
	public static final BigInteger ONE = new BigInteger(1, 1);
	public static final BigInteger TEN = new BigInteger(1, 10);

	BigInteger(int sign, long value) {
	}

	BigInteger(int sign, int numberLength, int[] digits) {
		//setJavaRepresentation(sign, numberLength, digits);
	}

	public BigInteger(int numBits, Random random) {
	}

	public BigInteger(int bitLength, int certainty, Random random) {
	}

	public BigInteger(String value) {
	}

	public BigInteger(String value, int radix) {
	}

	public BigInteger(int signum, byte[] magnitude) {
	}

	public BigInteger(byte[] value) {
	}

	native public static BigInteger valueOf(long value);

	native public byte[] toByteArray();

	native public BigInteger abs();

	native public BigInteger negate();

	native public BigInteger add(BigInteger value);

	native public BigInteger subtract(BigInteger value);

	native public int signum();

	native public BigInteger shiftRight(int n);

	native public BigInteger shiftLeft(int n);

	native public int bitLength();

	native public boolean testBit(int n);

	native public BigInteger setBit(int n);

	native public BigInteger clearBit(int n);

	native public BigInteger flipBit(int n);

	native public int getLowestSetBit();

	native public int bitCount();

	native public BigInteger not();

	native public BigInteger and(BigInteger value);

	native public BigInteger or(BigInteger value);

	native public BigInteger xor(BigInteger value);

	native public BigInteger andNot(BigInteger value);

	native public int intValue();

	native public long longValue();

	native public float floatValue();

	native public double doubleValue();

	native public int compareTo(BigInteger value);

	native public BigInteger min(BigInteger value);

	native public BigInteger max(BigInteger value);

	native public int hashCode();

	native public boolean equals(Object x);

	native public String toString();

	native public String toString(int radix);

	native public BigInteger gcd(BigInteger value);

	native public BigInteger multiply(BigInteger value);

	native public BigInteger pow(int exp);

	native public BigInteger[] divideAndRemainder(BigInteger divisor);

	native public BigInteger divide(BigInteger divisor);

	native public BigInteger remainder(BigInteger divisor);

	native public BigInteger modInverse(BigInteger m);

	native public BigInteger modPow(BigInteger exponent, BigInteger modulus);

	native public BigInteger mod(BigInteger m);

	native public boolean isProbablePrime(int certainty);

	native public BigInteger nextProbablePrime();

	native public static BigInteger probablePrime(int bitLength, Random random);
}