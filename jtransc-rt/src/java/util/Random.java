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

package java.util;

public class Random implements java.io.Serializable {
	private long seed;

	private static final long multiplier = 0x5DEECE66DL;
	private static final long addend = 0xBL;
	private static final long mask = (1L << 48) - 1;

	private static final double DOUBLE_UNIT = 0x1.0p-53; // 1.0 / (1L << 53)

	public Random() {
		this(seedUniquifier() ^ System.nanoTime());
	}

	private static long seedUniquifier() {
		return seedUniquifier = seedUniquifier * 181783497276652981L;
	}

	private static long seedUniquifier = 8682522807148012L;

	public Random(long seed) {
		if (getClass() == Random.class) {
			this.seed = initialScramble(seed);
		} else {
			this.seed = 0L;
			setSeed(seed);
		}
	}

	private static long initialScramble(long seed) {
		return (seed ^ multiplier) & mask;
	}

	synchronized public void setSeed(long seed) {
		this.seed = initialScramble(seed);
		haveNextNextGaussian = false;
	}

	protected int next(int bits) {
		this.seed = (this.seed * multiplier + addend) & mask;
		return (int) (this.seed >>> (48 - bits));
	}

	public void nextBytes(byte[] bytes) {
		for (int i = 0, len = bytes.length; i < len; ) {
			for (int rnd = nextInt(), n = Math.min(len - i, Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE) {
				bytes[i++] = (byte) rnd;
			}
		}
	}

	public int nextInt() {
		return next(32);
	}

	public int nextInt(int bound) {
		if (bound <= 0) throw new IllegalArgumentException("bound must be positive");

		int r = next(31);
		int m = bound - 1;
		if ((bound & m) == 0) {
			r = (int) ((bound * (long) r) >> 31);
		} else {
			for (int u = r; u - (r = u % bound) + m < 0; u = next(31)) ;
		}
		return r;
	}

	public long nextLong() {
		return ((long) (next(32)) << 32) + next(32);
	}

	public boolean nextBoolean() {
		return next(1) != 0;
	}

	public float nextFloat() {
		return next(24) / ((float) (1 << 24));
	}

	public double nextDouble() {
		return (((long) (next(26)) << 27) + next(27)) * DOUBLE_UNIT;
	}

	private double nextNextGaussian;
	private boolean haveNextNextGaussian = false;

	synchronized public double nextGaussian() {
		// See Knuth, ACP, Section 3.4.1 Algorithm C.
		if (haveNextNextGaussian) {
			haveNextNextGaussian = false;
			return nextNextGaussian;
		} else {
			double v1, v2, s;
			do {
				v1 = 2 * nextDouble() - 1;
				v2 = 2 * nextDouble() - 1;
				s = v1 * v1 + v2 * v2;
			} while (s >= 1 || s == 0);
			double multiplier = StrictMath.sqrt(-2 * StrictMath.log(s) / s);
			nextNextGaussian = v2 * multiplier;
			haveNextNextGaussian = true;
			return v1 * multiplier;
		}
	}
}
