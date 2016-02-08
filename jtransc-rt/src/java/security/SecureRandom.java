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

public class SecureRandom extends java.util.Random {
	public SecureRandom() {
		super(0);
	}

	public SecureRandom(byte seed[]) {
		super(0);
	}

	public native static SecureRandom getInstance(String algorithm) throws NoSuchAlgorithmException;

	public native static SecureRandom getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException;

	public native static SecureRandom getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException;

	public native final Provider getProvider();

	public native String getAlgorithm();

	synchronized public native void setSeed(byte[] seed);

	public native void setSeed(long seed);

	synchronized native public void nextBytes(byte[] bytes);

	public native static byte[] getSeed(int numBytes);

	public native byte[] generateSeed(int numBytes);
}
