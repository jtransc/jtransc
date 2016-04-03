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

import jtransc.JTranscSystem;

import java.nio.charset.Charset;
import java.util.Locale;

public class PrintStream extends FilterOutputStream implements Appendable, Closeable {

	private final boolean autoFlush;
	private boolean trouble = false;

	//private BufferedWriter textOut;
	//private OutputStreamWriter charOut;
	private Charset charset;

	static private String lineSeparator = JTranscSystem.fileSeparator();

	private static <T> T requireNonNull(T obj, String message) {
		if (obj == null) throw new NullPointerException(message);
		return obj;
	}

	private static Charset toCharset(String csn) throws UnsupportedEncodingException {
		requireNonNull(csn, "charsetName");
		try {
			return Charset.forName(csn);
		} catch (IllegalArgumentException unused) {
			throw new UnsupportedEncodingException(csn);
		}
	}

	private PrintStream(boolean autoFlush, OutputStream out) {
		super(out);
		this.autoFlush = autoFlush;
		this.charset = Charset.defaultCharset();
		//this.charOut = new OutputStreamWriter(this);
		//this.textOut = new BufferedWriter(charOut);
	}

	private PrintStream(boolean autoFlush, OutputStream out, Charset charset) {
		super(out);
		this.autoFlush = autoFlush;
		this.charset = charset;
		//this.charOut = new OutputStreamWriter(this, charset);
		//this.textOut = new BufferedWriter(charOut);
	}

	private PrintStream(boolean autoFlush, Charset charset, OutputStream out) throws UnsupportedEncodingException {
		this(autoFlush, out, charset);
	}

	public PrintStream(OutputStream out) {
		this(out, false);
	}

	public PrintStream(OutputStream out, boolean autoFlush) {
		this(autoFlush, requireNonNull(out, "Null output stream"));
	}

	public PrintStream(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException {
		this(autoFlush, requireNonNull(out, "Null output stream"), toCharset(encoding));
	}

	public PrintStream(String fileName) throws FileNotFoundException {
		this(false, new FileOutputStream(fileName));
	}

	public PrintStream(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(false, toCharset(csn), new FileOutputStream(fileName));
	}

	public PrintStream(File file) throws FileNotFoundException {
		this(false, new FileOutputStream(file));
	}

	public PrintStream(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(false, toCharset(csn), new FileOutputStream(file));
	}

	private void ensureOpen() throws IOException {
		if (out == null) throw new IOException("Stream closed");
	}

	public void flush() {
		try {
			ensureOpen();
			out.flush();
		} catch (IOException x) {
			trouble = true;
		}
	}

	private boolean closing = false; /* To avoid recursive closing */

	public void close() {
		if (closing) return;
		closing = true;
		try {
			//textOut.close();
			out.close();
		} catch (IOException x) {
			trouble = true;
		}
		//textOut = null;
		//charOut = null;
		out = null;
	}

	public boolean checkError() {
		if (out != null) flush();
		if (out instanceof PrintStream) {
			PrintStream ps = (PrintStream) out;
			return ps.checkError();
		}
		return trouble;
	}

	protected void setError() {
		trouble = true;
	}

	protected void clearError() {
		trouble = false;
	}

	public void write(int value) {
		try {
			ensureOpen();
			out.write(value);
			if ((value == '\n') && autoFlush) out.flush();
		} catch (IOException x) {
			trouble = true;
		}
	}

	public void write(byte value[], int offset, int length) {
		write(new String(value, offset, length));
	}

	private void write(char buf[]) {
		write(new String(buf));
	}

	private void write(String s) {
		try {
			ensureOpen();
			out.write(s.getBytes(charset));
			//textOut.write(s);
			//textOut.flushBuffer();
			//charOut.flushBuffer();
			//if (autoFlush && (s.indexOf('\n') >= 0)) out.flush();
		} catch (IOException x) {
			trouble = true;
		}
	}

	private void newLine() {
		write(lineSeparator);
	}

	public void print(boolean b) {
		write(String.valueOf(b));
	}

	public void print(char c) {
		write((int) c);
	}

	public void print(int i) {
		write(String.valueOf(i));
	}

	public void print(long l) {
		write(String.valueOf(l));
	}

	public void print(float f) {
		write(String.valueOf(f));
	}

	public void print(double d) {
		write(String.valueOf(d));
	}

	public void print(char s[]) {
		write(s);
	}

	public void print(String s) {
		write((s != null) ? s : "null");
	}

	public void print(Object obj) {
		write(String.valueOf(obj));
	}

	public void println() {
		newLine();
	}

	public void println(boolean v) {
		println(String.valueOf(v));
	}

	public void println(char v) {
		println(String.valueOf(v));
	}

	public void println(int v) {
		println(String.valueOf(v));
	}

	public void println(long v) {
		println(String.valueOf(v));
	}

	public void println(float v) {
		println(String.valueOf(v));
	}

	public void println(double v) {
		println(String.valueOf(v));
	}

	public void println(char v[]) {
		println(String.valueOf(v));
	}

	public void println(String x) {
		print(x);
		newLine();
	}

	public void println(Object x) {
		println(String.valueOf(x));
	}

	public PrintStream printf(String format, Object... args) {
		return format(format, args);
	}

	public PrintStream printf(Locale l, String format, Object... args) {
		return format(l, format, args);
	}

	public PrintStream format(String format, Object... args) {
		return format(Locale.getDefault(), format, args);
	}

	public PrintStream format(Locale l, String format, Object... args) {
		write(String.format(l, format, args));
		return this;
	}

	public PrintStream append(CharSequence csq) {
		write((csq != null) ? csq.toString() : "null");
		return this;
	}

	public PrintStream append(CharSequence csq, int start, int end) {
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}

	public PrintStream append(char c) {
		write((int) c);
		return this;
	}
}
