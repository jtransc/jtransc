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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class OutputStreamWriter extends Writer {
    private OutputStream os;
    private final CharsetEncoder se;

    public OutputStreamWriter(OutputStream out, String charsetName) throws UnsupportedEncodingException {
        this(out, Charset.forName(charsetName).newEncoder());
    }

    public OutputStreamWriter(OutputStream out) {
        this(out, Charset.forName("UTF-8").newEncoder());
    }

    public OutputStreamWriter(OutputStream out, Charset cs) {
        this(out, cs.newEncoder());
    }

    public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out);
        if (enc == null) throw new NullPointerException("charset encoder");
        this.os = out;
        this.se = enc;
    }

    public String getEncoding() {
        return se.charset().name();
    }

    void flushBuffer() throws IOException {
        ByteBuffer data = se.encode(CharBuffer.wrap(sb));
        this.os.write(data.array(), 0, data.limit());
        this.os.flush();
        sb.setLength(0);
    }
    private StringBuilder sb = new StringBuilder();

    private void checkBuffer() throws IOException {
        if (sb.length() >= 16) flushBuffer();
    }

    public void write(int value) throws IOException {
        sb.append(value);
        checkBuffer();
    }

    public void write(char value[], int offset, int length) throws IOException {
        sb.append(value, offset, length);
        checkBuffer();
    }

    public void write(String str, int off, int len) throws IOException {
        sb.append(str, off, len);
        checkBuffer();
    }

    public void flush() throws IOException {
        flushBuffer();
    }

    public void close() throws IOException {
        flushBuffer();
        this.os.close();
    }
}

