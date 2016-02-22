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

package jtransc.internal;

import jtransc.annotation.JTranscInvisible;

import java.security.DigestException;
import java.security.MessageDigestSpi;
import java.security.ProviderException;

@JTranscInvisible
abstract class DigestBase extends MessageDigestSpi implements Cloneable {
	private final String algorithm;
	private final int digestLength;
	private final int blockSize;
	byte[] buffer;
	private int bufOfs;
	long processedLength;
	static final byte[] padding = new byte[136];

	DigestBase(String algorithm, int digestLength, int blockSize) {
		padding[0] = (byte) (1 << 7);
		this.algorithm = algorithm;
		this.digestLength = digestLength;
		this.blockSize = blockSize;
		this.buffer = new byte[blockSize];
	}

	protected final int engineGetDigestLength() {
		return this.digestLength;
	}

	protected final void engineUpdate(byte b) {
		this.engineUpdate(new byte[]{b}, 0, 1);
	}

	protected final void engineUpdate(byte[] data, int offset, int length) {
		if (length == 0) {
			return;
		}

		if (offset < 0 || length < 0 || offset < data.length - length) {
			throw new ArrayIndexOutOfBoundsException();
		}

		if (this.processedLength < 0L) {
			this.engineReset();
		}

		this.processedLength += (long) length;
		if (this.bufOfs != 0) {
			int l = Math.min(length, this.blockSize - this.bufOfs);
			System.arraycopy(data, offset, this.buffer, this.bufOfs, l);
			this.bufOfs += l;
			offset += l;
			length -= l;
			if (this.bufOfs >= this.blockSize) {
				this.implCompress(this.buffer, 0);
				this.bufOfs = 0;
			}
		}

		while (length >= this.blockSize) {
			this.implCompress(data, offset);
			length -= this.blockSize;
			offset += this.blockSize;
		}

		if (length > 0) {
			System.arraycopy(data, offset, this.buffer, 0, length);
			this.bufOfs = length;
		}
	}

	protected final void engineReset() {
		if (this.processedLength == 0L) return;
		this.implReset();
		this.bufOfs = 0;
		this.processedLength = 0L;
	}

	protected final byte[] engineDigest() {
		byte[] data = new byte[this.digestLength];

		try {
			this.engineDigest(data, 0, data.length);
			return data;
		} catch (DigestException var3) {
			throw (ProviderException) (new ProviderException("Internal error")).initCause(var3);
		}
	}

	protected final int engineDigest(byte[] data, int offset, int length) throws DigestException {
		if (length < this.digestLength) {
			throw new DigestException("Length must be at least " + this.digestLength + " for " + this.algorithm + "digests");
		}

		if (offset < 0 || length < 0 || offset > data.length - length) {
			throw new DigestException("Buffer too short to store digest");
		}

		if (this.processedLength < 0L) {
			this.engineReset();
		}

		this.implDigest(data, offset);
		this.processedLength = -1L;
		return this.digestLength;
	}

	abstract void implCompress(byte[] data, int offset);

	abstract void implDigest(byte[] data, int offset);

	abstract void implReset();

	public Object clone() throws CloneNotSupportedException {
		DigestBase base = (DigestBase) super.clone();
		base.buffer = base.buffer.clone();
		return base;
	}
}
