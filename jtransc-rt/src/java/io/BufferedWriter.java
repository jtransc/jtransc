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

public class BufferedWriter extends Writer {
	private Writer out;

	private char cb[];
	private int nChars, nextChar;

	private static int defaultCharBufferSize = 8192;

	private String lineSeparator;

	public BufferedWriter(Writer out) {
		this(out, defaultCharBufferSize);
	}

	public BufferedWriter(Writer out, int sz) {
		super(out);
		if (sz <= 0) throw new IllegalArgumentException("Buffer size <= 0");
		this.out = out;
		cb = new char[sz];
		nChars = sz;
		nextChar = 0;
		lineSeparator = System.getProperty("line.separator");
	}

	private void ensureOpen() throws IOException {
		if (out == null) throw new IOException("Stream closed");
	}

	void flushBuffer() throws IOException {
		ensureOpen();
		if (nextChar == 0) return;
		out.write(cb, 0, nextChar);
		nextChar = 0;
	}

	public void write(int value) throws IOException {
		ensureOpen();
		if (nextChar >= nChars) flushBuffer();
		cb[nextChar++] = (char) value;
	}

	private int min(int a, int b) {
		if (a < b) return a;
		return b;
	}

	public void write(char value[], int offset, int length) throws IOException {
		ensureOpen();
		if ((offset < 0) || (offset > value.length) || (length < 0) || ((offset + length) > value.length) || ((offset + length) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (length == 0) {
			return;
		}

		if (length >= nChars) {
			flushBuffer();
			out.write(value, offset, length);
			return;
		}

		int b = offset, t = offset + length;
		while (b < t) {
			int d = min(nChars - nextChar, t - b);
			System.arraycopy(value, b, cb, nextChar, d);
			b += d;
			nextChar += d;
			if (nextChar >= nChars) flushBuffer();
		}
	}

	public void write(String s, int off, int len) throws IOException {
		ensureOpen();

		int b = off, t = off + len;
		while (b < t) {
			int d = min(nChars - nextChar, t - b);
			s.getChars(b, b + d, cb, nextChar);
			b += d;
			nextChar += d;
			if (nextChar >= nChars) flushBuffer();
		}
	}

	public void newLine() throws IOException {
		write(lineSeparator);
	}

	public void flush() throws IOException {
		flushBuffer();
		out.flush();
	}

	public void close() throws IOException {
		if (out == null) {
			return;
		}
		Writer w = out;
		try {
			flushBuffer();
		} finally {
			w.close();
			out = null;
			cb = null;
		}
	}
}
