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

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class InputStreamReader extends Reader {
	private InputStream in;
	private Charset cs;

	public InputStreamReader(InputStream in) {
		this(in, Charset.forName("UTF-8"));
	}

	public InputStreamReader(InputStream in, String charsetName) throws UnsupportedEncodingException {
		this(in, Charset.forName(charsetName));
	}

	public InputStreamReader(InputStream in, Charset cs) {
		super(in);
		this.in = in;
		this.cs = cs;
	}

	public InputStreamReader(InputStream in, CharsetDecoder dec) {
		super(in);
		this.in = in;
		this.cs = dec.charset();
	}

	public String getEncoding() {
		return cs.displayName();
	}

	public int read() throws IOException {
		return in.read(); // @TODO: Use charset!
	}

	public int read(char cbuf[], int offset, int length) throws IOException {
		for (int n = 0; n < length; n++) {
			int ch = read();
			if (ch < 0) {
				return (n == 0) ? -1 : n;
			}
			cbuf[offset + n] = (char) ch;
		}
		return length;
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
