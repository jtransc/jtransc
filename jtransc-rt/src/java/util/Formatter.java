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

package java.util;

import com.jtransc.internal.IntJTranscStrings;

import java.io.*;
import java.nio.charset.Charset;

public final class Formatter implements Closeable, Flushable {
	private Appendable a;
	private final Locale l;
	private IOException lastException;

	private static Charset toCharset(String csn) throws UnsupportedEncodingException {
		Objects.requireNonNull(csn, "charsetName");
		try {
			return Charset.forName(csn);
		} catch (Throwable unused) {
			throw new UnsupportedEncodingException(csn);
		}
	}

	private static Appendable nonNullAppendable(Appendable a) {
		return (a != null) ? a : new StringBuilder();
	}

	private Formatter(Locale l, Appendable a) {
		this.a = a;
		this.l = l;
	}

	private Formatter(Charset charset, Locale l, File file) throws FileNotFoundException {
		this(l, new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset)));
	}

	public Formatter() {
		this(Locale.getDefault(Locale.Category.FORMAT), new StringBuilder());
	}

	public Formatter(Appendable a) {
		this(Locale.getDefault(Locale.Category.FORMAT), nonNullAppendable(a));
	}

	public Formatter(Locale l) {
		this(l, new StringBuilder());
	}

	public Formatter(Appendable a, Locale l) {
		this(l, nonNullAppendable(a));
	}

	public Formatter(String fileName) throws FileNotFoundException {
		this(Locale.getDefault(Locale.Category.FORMAT), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName))));
	}

	public Formatter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(fileName, csn, Locale.getDefault(Locale.Category.FORMAT));
	}

	public Formatter(String fileName, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
		this(toCharset(csn), l, new File(fileName));
	}

	public Formatter(File file) throws FileNotFoundException {
		this(Locale.getDefault(Locale.Category.FORMAT), new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file))));
	}

	public Formatter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(file, csn, Locale.getDefault(Locale.Category.FORMAT));
	}

	public Formatter(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
		this(toCharset(csn), l, file);
	}

	public Formatter(PrintStream ps) {
		this(Locale.getDefault(Locale.Category.FORMAT), Objects.requireNonNull(ps));
	}

	public Formatter(OutputStream os) {
		this(Locale.getDefault(Locale.Category.FORMAT), new BufferedWriter(new OutputStreamWriter(os)));
	}

	public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
		this(os, csn, Locale.getDefault(Locale.Category.FORMAT));
	}

	public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
		this(l, new BufferedWriter(new OutputStreamWriter(os, csn)));
	}

	private void ensureOpen() {
		if (a == null) throw new FormatterClosedException();
	}

	public Locale locale() {
		ensureOpen();
		return l;
	}

	public Appendable out() {
		ensureOpen();
		return a;
	}

	public String toString() {
		ensureOpen();
		return a.toString();
	}

	public void flush() {
		ensureOpen();
		if (a instanceof Flushable) {
			try {
				((Flushable) a).flush();
			} catch (IOException ioe) {
				lastException = ioe;
			}
		}
	}

	public void close() {
		if (a == null) return;
		try {
			if (a instanceof Closeable) ((Closeable) a).close();
		} catch (IOException ioe) {
			lastException = ioe;
		} finally {
			a = null;
		}
	}

	public IOException ioException() {
		return lastException;
	}

	public Formatter format(String format, Object... args) {
		return format(l, format, args);
	}

	public Formatter format(Locale l, String format, Object... args) {
		try {
			this.a.append(IntJTranscStrings.format(l, format, args));
		} catch (IOException e) {
			lastException = e;
		}
		return this;
	}
}
