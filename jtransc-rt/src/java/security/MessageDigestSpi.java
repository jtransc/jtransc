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

public abstract class MessageDigestSpi {
	protected int engineGetDigestLength() {
		return 0;
	}

	protected abstract void engineUpdate(byte input);

	protected abstract void engineUpdate(byte[] input, int offset, int len);

	native public static int getTempArraySize(int len);

	native protected void engineUpdate(ByteBuffer input);

	protected abstract byte[] engineDigest();

	native protected int engineDigest(byte[] buf, int offset, int len) throws DigestException;

	protected abstract void engineReset();

	native public Object clone() throws CloneNotSupportedException;
}
