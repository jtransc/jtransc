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

package jtransc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JTranscCrypto {
	static public byte[] md5(byte[] data) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			return md5.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("");
		}
	}

	static public byte[] sha1(byte[] data) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			return sha1.digest(data);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("");
		}
	}
}
