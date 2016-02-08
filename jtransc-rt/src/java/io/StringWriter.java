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

public class StringWriter extends Writer {
	private StringBuffer buffer;

	public StringWriter() {
		buffer = new StringBuffer();
		lock = buffer;
	}

	public StringWriter(int initialSize) {
		if (initialSize < 0) throw new IllegalArgumentException("Negative buffer size");
		buffer = new StringBuffer(initialSize);
		lock = buffer;
	}

	public void write(int value) {
		buffer.append((char) value);
	}

	public void write(char value[], int offset, int length) {
		if (
			(offset < 0) || (offset > value.length) || (length < 0) ||
				((offset + length) > value.length) || ((offset + length) < 0)
			) {
			throw new IndexOutOfBoundsException();
		}
		if (length == 0) return;
		buffer.append(value, offset, length);
	}

	public void write(String str) {
		buffer.append(str);
	}

	public void write(String str, int off, int len) {
		buffer.append(str.substring(off, off + len));
	}

	public StringWriter append(CharSequence csq) {
		write((csq != null) ? csq.toString() : "null");
		return this;
	}

	public StringWriter append(CharSequence csq, int start, int end) {
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}

	public StringWriter append(char c) {
		write(c);
		return this;
	}

	public String toString() {
		return buffer.toString();
	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void flush() {
	}

	public void close() throws IOException {
	}
}
