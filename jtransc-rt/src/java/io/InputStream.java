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

import com.jtransc.annotation.haxe.HaxeAddFilesTemplate;
import com.jtransc.annotation.haxe.HaxeNativeConversion;

@HaxeAddFilesTemplate({ "JavaHaxeInput.hx" })
@HaxeNativeConversion(haxeType = "haxe.io.Input", toHaxe = "new JavaHaxeInput.Haxe(@self)", toJava = "new JavaHaxeInput.Java(@self)")
public abstract class InputStream implements Closeable {
	private static final int MAX_SKIP_BUFFER_SIZE = 2048;

	public abstract int read() throws IOException;

	public int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte data[], int offset, int length) throws IOException {
		if (length == 0) return 0;

		int c = read();
		if (c == -1) return -1;
		data[offset] = (byte) c;

		int n = 1;
		try {
			while (n < length) {
				c = read();
				if (c == -1) break;
				data[offset + n] = (byte) c;
				n++;
			}
		} catch (IOException e) {
		}
		return n;
	}

	public long skip(long n) throws IOException {
		long remaining = n;
		int nr;

		if (n <= 0) return 0;

		int size = (int) Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
		byte[] skipBuffer = new byte[size];
		while (remaining > 0) {
			nr = read(skipBuffer, 0, (int) Math.min(size, remaining));
			if (nr < 0) break;
			remaining -= nr;
		}

		return n - remaining;
	}

	public int available() throws IOException {
		return 0;
	}

	public void close() throws IOException {
	}

	public synchronized void mark(int readlimit) {
	}

	public synchronized void reset() throws IOException {
		throw new IOException("mark/reset not supported");
	}

	public boolean markSupported() {
		return false;
	}

}
