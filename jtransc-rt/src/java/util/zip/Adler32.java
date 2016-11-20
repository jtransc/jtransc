/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.zip;

/**
 * The Adler-32 class is used to compute the {@code Adler32} checksum from a set
 * of data. Compared to {@link CRC32} it trades reliability for speed.
 * Refer to RFC 1950 for the specification.
 */
public class Adler32 implements Checksum {
	private com.jtransc.compression.jzlib.Adler32 impl = new com.jtransc.compression.jzlib.Adler32();

	/**
	 * Returns the {@code Adler32} checksum for all input received.
	 *
	 * @return The checksum for this instance.
	 */
	public long getValue() {
		return impl.getValue() & 0xFFFFFFFFL;
	}

	/**
	 * Reset this instance to its initial checksum.
	 */
	public void reset() {
		impl.reset();
	}

	private byte[] scratch = new byte[1];

	/**
	 * Update this {@code Adler32} checksum with the single byte provided as
	 * argument.
	 *
	 * @param i the byte to update checksum with.
	 */
	public void update(int i) {
		scratch[0] = (byte) i;
		impl.update(scratch, 0, 1);
	}

	/**
	 * Update this {@code Adler32} checksum using the contents of {@code buf}.
	 *
	 * @param buf bytes to update checksum with.
	 */
	public void update(byte[] buf) {
		impl.update(buf, 0, buf.length);
	}

	/**
	 * Update this {@code Adler32} checksum with the contents of {@code buf},
	 * starting from {@code offset} and reading {@code byteCount} bytes of data.
	 */
	public void update(byte[] buf, int offset, int byteCount) {
		impl.update(buf, offset, byteCount);
	}
}
