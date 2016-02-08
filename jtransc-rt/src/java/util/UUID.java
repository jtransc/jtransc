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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class UUID implements java.io.Serializable, Comparable<UUID> {
	private final long mostSigBits;
	private final long leastSigBits;

	private static class Holder {
		static final SecureRandom numberGenerator = new SecureRandom();
	}

	private UUID(byte[] data) {
		long msb = 0;
		long lsb = 0;
		assert data.length == 16 : "data must be 16 bytes in length";
		for (int i = 0; i < 8; i++) msb = (msb << 8) | (data[i] & 0xff);
		for (int i = 8; i < 16; i++) lsb = (lsb << 8) | (data[i] & 0xff);
		this.mostSigBits = msb;
		this.leastSigBits = lsb;
	}

	public UUID(long mostSigBits, long leastSigBits) {
		this.mostSigBits = mostSigBits;
		this.leastSigBits = leastSigBits;
	}

	public static UUID randomUUID() {
		SecureRandom ng = Holder.numberGenerator;

		byte[] randomBytes = new byte[16];
		ng.nextBytes(randomBytes);
		randomBytes[6] &= 0x0f;
		randomBytes[6] |= 0x40;
		randomBytes[8] &= 0x3f;
		randomBytes[8] |= 0x80;
		return new UUID(randomBytes);
	}

	public static UUID nameUUIDFromBytes(byte[] name) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("MD5 not supported", nsae);
		}
		byte[] md5Bytes = md.digest(name);
		md5Bytes[6] &= 0x0f;
		md5Bytes[6] |= 0x30;
		md5Bytes[8] &= 0x3f;
		md5Bytes[8] |= 0x80;
		return new UUID(md5Bytes);
	}

	public static UUID fromString(String name) {
		String[] components = name.split("-");
		if (components.length != 5) throw new IllegalArgumentException("Invalid UUID string: " + name);
		for (int i = 0; i < 5; i++) components[i] = "0x" + components[i];

		long mostSigBits = Long.decode(components[0]).longValue();
		mostSigBits <<= 16;
		mostSigBits |= Long.decode(components[1]).longValue();
		mostSigBits <<= 16;
		mostSigBits |= Long.decode(components[2]).longValue();

		long leastSigBits = Long.decode(components[3]).longValue();
		leastSigBits <<= 48;
		leastSigBits |= Long.decode(components[4]).longValue();

		return new UUID(mostSigBits, leastSigBits);
	}

	public long getLeastSignificantBits() {
		return leastSigBits;
	}

	public long getMostSignificantBits() {
		return mostSigBits;
	}

	public int version() {
		return (int) ((mostSigBits >> 12) & 0x0f);
	}

	public int variant() {
		return (int) ((leastSigBits >>> (64 - (leastSigBits >>> 62))) & (leastSigBits >> 63));
	}

	public long timestamp() {
		if (version() != 1) throw new UnsupportedOperationException("Not a time-based UUID");
		return (mostSigBits & 0x0FFFL) << 48 | ((mostSigBits >> 16) & 0x0FFFFL) << 32 | mostSigBits >>> 32;
	}

	public int clockSequence() {
		if (version() != 1) throw new UnsupportedOperationException("Not a time-based UUID");
		return (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);
	}

	public long node() {
		if (version() != 1) throw new UnsupportedOperationException("Not a time-based UUID");
		return leastSigBits & 0x0000FFFFFFFFFFFFL;
	}

	public String toString() {
		return (digits(mostSigBits >> 32, 8) + "-" + digits(mostSigBits >> 16, 4) + "-" + digits(mostSigBits, 4) + "-" + digits(leastSigBits >> 48, 4) + "-" + digits(leastSigBits, 12));
	}

	private static String digits(long val, int digits) {
		long hi = 1L << (digits * 4);
		return Long.toHexString(hi | (val & (hi - 1))).substring(1);
	}

	public int hashCode() {
		long hilo = mostSigBits ^ leastSigBits;
		return ((int) (hilo >> 32)) ^ (int) hilo;
	}

	public boolean equals(Object obj) {
		if ((null == obj) || (obj.getClass() != UUID.class)) return false;
		UUID id = (UUID) obj;
		return (mostSigBits == id.mostSigBits && leastSigBits == id.leastSigBits);
	}

	public int compareTo(UUID val) {
		return (this.mostSigBits < val.mostSigBits ? -1 : (this.mostSigBits > val.mostSigBits ? 1 : (this.leastSigBits < val.leastSigBits ? -1 : (this.leastSigBits > val.leastSigBits ? 1 : 0))));
	}
}
