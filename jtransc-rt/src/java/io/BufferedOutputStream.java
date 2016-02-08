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

public class BufferedOutputStream extends FilterOutputStream {
	protected byte buf[];
	protected int count;

	public BufferedOutputStream(OutputStream out) {
		this(out, 8192);
	}

	public BufferedOutputStream(OutputStream out, int size) {
		super(out);
		buf = new byte[size];
	}

	public synchronized void write(int value) throws IOException {
		if (count >= buf.length) flushbuf();
		buf[count++] = (byte) value;
	}

	public synchronized void write(byte value[], int offset, int length) throws IOException {
		if (length >= buf.length) {
			flushbuf();
			out.write(value, offset, length);
			return;
		}
		if (length > buf.length - count) flushbuf();
		System.arraycopy(value, offset, buf, count, length);
		count += length;
	}

	public synchronized void flush() throws IOException {
		flushbuf();
		out.flush();
	}

	private void flushbuf() throws IOException {
		if (count <= 0) return;
		out.write(buf, 0, count);
		count = 0;
	}
}
