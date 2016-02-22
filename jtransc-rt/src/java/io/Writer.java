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

import jtransc.internal.JTranscTempBuffer;

public abstract class Writer implements Appendable, Closeable, Flushable {

	protected Object lock;

	protected Writer() {
		this.lock = this;
	}

	protected Writer(Object lock) {
		this.lock = lock;
	}

	public void write(int value) throws IOException {
		char[] temp = JTranscTempBuffer.tempChar(1);
		temp[0] = (char) value;
		write(temp, 0, 1);
	}

	public void write(char value[]) throws IOException {
		write(value, 0, value.length);
	}

	abstract public void write(char value[], int offset, int length) throws IOException;

	public void write(String str) throws IOException {
		write(str, 0, str.length());
	}

	//native public void write(String str, int off, int len) throws IOException;

	public void write(String str, int off, int len) throws IOException {
		char[] temp = JTranscTempBuffer.tempChar(1);
		str.getChars(off, (off + len), temp, 0);
		write(temp, 0, len);
	}

	public Writer append(CharSequence csq) throws IOException {
		write((csq != null) ? csq.toString() : "null");
		return this;
	}

	public Writer append(CharSequence csq, int start, int end) throws IOException {
		CharSequence cs = (csq != null ? csq : "null");
		write(cs.subSequence(start, end).toString());
		return this;
	}

	public Writer append(char c) throws IOException {
		write(c);
		return this;
	}

	abstract public void flush() throws IOException;

	abstract public void close() throws IOException;
}
