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

package java.security;

import com.jtransc.crypto.org.bouncycastle.GeneralDigestAdaptor;
import com.jtransc.crypto.org.bouncycastle.MD5Digest;
import com.jtransc.crypto.org.bouncycastle.SHA1Digest;

import java.nio.ByteBuffer;
import java.util.Arrays;

// MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512
public abstract class MessageDigest extends MessageDigestSpi {
	final private String algorithm;

	protected MessageDigest(String algorithm) {
		this.algorithm = algorithm;
	}

	public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException {
		if ("MD5".equalsIgnoreCase(algorithm)) return new GeneralDigestAdaptor(new MD5Digest());
		if ("SHA1".equalsIgnoreCase(algorithm)) return new GeneralDigestAdaptor(new SHA1Digest());
		throw new NoSuchAlgorithmException(algorithm);
	}

	public static MessageDigest getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
		return getInstance(algorithm);
	}

	public static MessageDigest getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
		return getInstance(algorithm);
	}

	native public final Provider getProvider();

	private byte[] tempBuffer = new byte[1];

	public void update(byte input) {
		tempBuffer[0] = input;
		update(tempBuffer, 0, 1);
	}

	public void update(byte[] input, int offset, int len) {
		engineUpdate(input, offset, len);
	}

	public void update(byte[] input) {
		update(input, 0, input.length);
	}

	public final void update(ByteBuffer input) {
		engineUpdate(input);
	}

	public byte[] digest() {
		return engineDigest();
	}

	public int digest(byte[] buf, int offset, int len) throws DigestException {
		return engineDigest(buf, offset, len);
	}

	public byte[] digest(byte[] input) {
		engineUpdate(input, 0, input.length);
		return engineDigest();
	}

	public String toString() {
		return algorithm;
	}

	public static boolean isEqual(byte[] digesta, byte[] digestb) {
		// @TODO: This should execute in constant time either it works or not to avoid timing attacks on secure contexts
		return Arrays.equals(digesta, digestb);
	}

	public void reset() {
		engineReset();
	}

	public final String getAlgorithm() {
		return algorithm;
	}

	public final int getDigestLength() {
		return engineGetDigestLength();
	}

	native public Object clone() throws CloneNotSupportedException;
}
