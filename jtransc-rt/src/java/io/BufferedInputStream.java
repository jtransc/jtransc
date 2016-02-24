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

public class BufferedInputStream extends FilterInputStream {
	protected volatile byte buf[];
	protected int count;
	protected int pos;
	protected int markpos = -1;
	protected int marklimit;

	private InputStream getInIfOpen() throws IOException {
		InputStream input = in;
		if (input == null) throw new IOException("Stream closed");
		return input;
	}

	private byte[] getBufIfOpen() throws IOException {
		byte[] buffer = buf;
		if (buffer == null) throw new IOException("Stream closed");
		return buffer;
	}

	public BufferedInputStream(InputStream in) {
		this(in, 8192);
	}

	public BufferedInputStream(InputStream in, int size) {
		super(in);
		if (size <= 0) throw new IllegalArgumentException("Buffer size <= 0");
		buf = new byte[size];
	}

	private void fill() throws IOException {
		byte[] buffer = getBufIfOpen();
		if (markpos < 0) pos = 0;
		else if (pos >= buffer.length) {
			if (markpos > 0) {
				int sz = pos - markpos;
				System.arraycopy(buffer, markpos, buffer, 0, sz);
				pos = sz;
				markpos = 0;
			} else if (buffer.length >= marklimit) {
				markpos = -1;
				pos = 0;
			} else {
				int nsz = pos * 2;
				if (nsz > marklimit) nsz = marklimit;
				byte nbuf[] = new byte[nsz];
				System.arraycopy(buffer, 0, nbuf, 0, pos);
				buffer = nbuf;
			}
		}
		count = pos;
		int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
		if (n > 0) count = n + pos;
	}

	public synchronized int read() throws IOException {
		if (pos >= count) {
			fill();
			if (pos >= count) return -1;
		}
		return getBufIfOpen()[pos++] & 0xff;
	}

	private int read1(byte[] b, int off, int len) throws IOException {
		int avail = count - pos;
		if (avail <= 0) {
			if (len >= getBufIfOpen().length && markpos < 0) return getInIfOpen().read(b, off, len);
			fill();
			avail = count - pos;
			if (avail <= 0) return -1;
		}
		int cnt = (avail < len) ? avail : len;
		System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
		pos += cnt;
		return cnt;
	}

	public synchronized int read(byte data[], int offset, int length) throws IOException {
		getBufIfOpen();
		if ((offset | length | (offset + length) | (data.length - (offset + length))) < 0)
			throw new IndexOutOfBoundsException();
		if (length == 0) return 0;

		int n = 0;
		for (; ; ) {
			int nread = read1(data, offset + n, length - n);
			if (nread <= 0) return (n == 0) ? nread : n;
			n += nread;
			if (n >= length) return n;
			InputStream input = in;
			if (input != null && input.available() <= 0) return n;
		}
	}

	public synchronized long skip(long n) throws IOException {
		getBufIfOpen();
		if (n <= 0) return 0;
		long avail = count - pos;

		if (avail <= 0) {
			if (markpos < 0) return getInIfOpen().skip(n);
			fill();
			avail = count - pos;
			if (avail <= 0) return 0;
		}

		long skipped = (avail < n) ? avail : n;
		pos += skipped;
		return skipped;
	}

	public synchronized int available() throws IOException {
		int n = count - pos;
		int avail = getInIfOpen().available();
		return n > (Integer.MAX_VALUE - avail) ? Integer.MAX_VALUE : n + avail;
	}

	public synchronized void mark(int readlimit) {
		marklimit = readlimit;
		markpos = pos;
	}

	public synchronized void reset() throws IOException {
		getBufIfOpen();
		if (markpos < 0) throw new IOException("Resetting to invalid mark");
		pos = markpos;
	}

	public boolean markSupported() {
		return true;
	}

	public void close() throws IOException {
		byte[] buffer;
		while ((buffer = buf) != null) {
			InputStream input = in;
			in = null;
			if (input != null) input.close();
			return;
		}
	}
}
