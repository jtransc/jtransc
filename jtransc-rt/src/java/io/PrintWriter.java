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

import jtransc.JTranscStrings;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Locale;
import java.util.Objects;

public class PrintWriter extends Writer {
	static public char[] temp = new char[8];

	protected Writer out;

	private final boolean autoFlush;
	private boolean trouble = false;
	private PrintStream psOut = null;

	private final String lineSeparator;

	private static Charset toCharset(String csn) throws UnsupportedEncodingException {
		Objects.requireNonNull(csn, "charsetName");
		try {
			return Charset.forName(csn);
		} catch (IllegalCharsetNameException unused) {
			throw new UnsupportedEncodingException(csn);
		} catch (UnsupportedCharsetException unused) {
			throw new UnsupportedEncodingException(csn);
		}
	}

	public PrintWriter(Writer out) {
		this(out, false);
	}

	public PrintWriter(Writer out, boolean autoFlush) {
		super(out);
		this.out = out;
		this.autoFlush = autoFlush;
		lineSeparator = System.getProperty("line.separator");
	}

	public PrintWriter(OutputStream out) {
		this(out, false);
	}

	public PrintWriter(OutputStream out, boolean autoFlush) {
		this(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
		if (out instanceof PrintStream) psOut = (PrintStream) out;
	}

	public PrintWriter(String fileName) throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))), false);
	}

	private PrintWriter(Charset charset, File file)
		throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)),
			false);
	}

	public PrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(toCharset(csn), new File(fileName));
	}

	public PrintWriter(File file) throws FileNotFoundException {
		this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))), false);
	}

	public PrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(toCharset(csn), file);
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

	public void close() {
		try {
			if (out == null) return;
			out.close();
			out = null;
		} catch (IOException x) {
			trouble = true;
		}
	}

	public boolean checkError() {
		if (out != null) flush();
		if (out instanceof PrintWriter) {
			PrintWriter pw = (PrintWriter) out;
			return pw.checkError();
		} else if (psOut != null) {
			return psOut.checkError();
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
		write(new char[] { (char)value }, 0, 1);
	}

	public void write(char value[], int offset, int length) {
		try {
			ensureOpen();
			out.write(value, offset, length);
		} catch (IOException x) {
			trouble = true;
		}
	}

	public void write(char value[]) {
		write(value, 0, value.length);
	}

	public void write(String s, int off, int len) {
		write(JTranscStrings.getChars(s, off, len));
	}

	public void write(String s) {
		write(s, 0, s.length());
	}

	private void _printNewLine() {
		write(lineSeparator);
		if (autoFlush) flush();
	}

	public void print(boolean b) {
		write(String.valueOf(b));
	}

	public void print(char value) {
		write(value);
	}

	public void print(int value) {
		write(String.valueOf(value));
	}

	public void print(long value) {
		write(String.valueOf(value));
	}

	public void print(float value) {
		write(String.valueOf(value));
	}

	public void print(double value) {
		write(String.valueOf(value));
	}

	public void print(char value[]) {
		write(value);
	}

	public void print(String value) {
		write((value != null) ? value : "null");
	}

	public void print(Object obj) {
		write(String.valueOf(obj));
	}

	public void println() {
		_println("");
	}

	public void println(boolean x) {
		_println(String.valueOf(x));
	}

	public void println(char x) {
		_println(String.valueOf(x));
	}

	public void println(int x) {
		_println(String.valueOf(x));
	}

	public void println(long x) {
		_println(String.valueOf(x));
	}

	public void println(float x) {
		_println(String.valueOf(x));
	}

	public void println(double x) {
		_println(String.valueOf(x));
	}

	public void println(char x[]) {
		_println(String.valueOf(x));
	}

	public void println(String x) {
		_println(x);
	}

	public void println(Object x) {
		_println(String.valueOf(x));
	}

	private void _println(String x) {
		print(x);
		_printNewLine();
	}


	public PrintWriter printf(String format, Object... args) {
		return format(format, args);
	}

	public PrintWriter printf(Locale l, String format, Object... args) {
		return format(l, format, args);
	}

	public PrintWriter format(String format, Object... args) {
		write(String.format(format, args));
		return this;
	}

	public PrintWriter format(Locale l, String format, Object... args) {
		write(String.format(l, format, args));
		return this;
	}

	public PrintWriter append(CharSequence csq) {
		write((csq != null) ? csq.toString() : "null");
		return this;
	}

	public PrintWriter append(CharSequence csq, int start, int end) {
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());
		return this;
	}

	public PrintWriter append(char c) {
		write(c);
		return this;
	}
}
