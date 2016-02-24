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

package java.io;

public class ByteArrayInputStream extends InputStream {
	protected byte buf[];
	protected int pos;
	protected int mark = 0;
	protected int count;

	public ByteArrayInputStream(byte data[]) {
		this.buf = data;
		this.pos = 0;
		this.count = data.length;
	}

	public ByteArrayInputStream(byte data[], int offset, int length) {
		this.buf = data;
		this.pos = offset;
		this.count = Math.min(offset + length, data.length);
		this.mark = offset;
	}

	public synchronized int read() {
		return (pos < count) ? (buf[pos++] & 0xff) : -1;
	}

	public synchronized int read(byte data[], int offset, int length) {
		if (data == null) throw new NullPointerException();
		if (offset < 0 || length < 0 || length > data.length - offset) throw new IndexOutOfBoundsException();

		if (pos >= count) return -1;
		int avail = count - pos;
		if (length > avail) length = avail;
		if (length <= 0) return 0;
		System.arraycopy(buf, pos, data, offset, length);
		pos += length;
		return length;
	}

	public synchronized long skip(long n) {
		long left = count - pos;
		if (n < left) left = n < 0 ? 0 : n;
		pos += left;
		return left;
	}

	public synchronized int available() {
		return count - pos;
	}

	public boolean markSupported() {
		return true;
	}

	public void mark(int readAheadLimit) {
		mark = pos;
	}

	public synchronized void reset() {
		pos = mark;
	}

	public void close() throws IOException {
	}
}
