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

import java.nio.ByteBuffer;

// MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512
public abstract class MessageDigest extends MessageDigestSpi {
	protected MessageDigest(String algorithm) {
	}

	native public static MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException;

	native public static MessageDigest getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException;

	native public static MessageDigest getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException;

	native public final Provider getProvider();

	native public void update(byte input);

	native public void update(byte[] input, int offset, int len);

	native public void update(byte[] input);

	native public final void update(ByteBuffer input);

	native public byte[] digest();

	native public int digest(byte[] buf, int offset, int len) throws DigestException;

	native public byte[] digest(byte[] input);

	native public String toString();

	native public static boolean isEqual(byte[] digesta, byte[] digestb);

	native public void reset();

	native public final String getAlgorithm();

	native public final int getDigestLength();

	native public Object clone() throws CloneNotSupportedException;
}
