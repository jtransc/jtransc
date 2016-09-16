/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util;

import com.jtransc.io.JTranscIoTools;

import java.io.*;
import java.nio.charset.Charset;

public final class FormatterFull implements Closeable, Flushable {
	private static final char[] ZEROS = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0'};

	/**
	 * The enumeration giving the available styles for formatting very large
	 * decimal numbers.
	 */
	public enum BigDecimalLayoutForm {
		/**
		 * Use scientific style for BigDecimals.
		 */
		SCIENTIFIC,
		/**
		 * Use normal decimal/float style for BigDecimals.
		 */
		DECIMAL_FLOAT
	}

	// User-settable parameters.
	private Appendable out;
	private Locale locale;

	// Implementation details.
	private Object arg;
	private boolean closed = false;
	private FormatToken formatToken;
	private IOException lastIOException;
	//private LocaleData localeData;

	//private static class CachedDecimalFormat {
	//	//public NativeDecimalFormat decimalFormat;
	//	//public LocaleData currentLocaleData;
	//	public String currentPattern;
//
	//	public CachedDecimalFormat() {
	//	}
//
	//	public NativeDecimalFormat update(LocaleData localeData, String pattern) {
	//		if (decimalFormat == null) {
	//			currentPattern = pattern;
	//			currentLocaleData = localeData;
	//			decimalFormat = new NativeDecimalFormat(currentPattern, currentLocaleData);
	//		}
	//		if (!pattern.equals(currentPattern)) {
	//			decimalFormat.applyPattern(pattern);
	//			currentPattern = pattern;
	//		}
	//		if (localeData != currentLocaleData) {
	//			decimalFormat.setDecimalFormatSymbols(localeData);
	//			currentLocaleData = localeData;
	//		}
	//		return decimalFormat;
	//	}
	//}

	//private static final ThreadLocal<CachedDecimalFormat> cachedDecimalFormat = new ThreadLocal<CachedDecimalFormat>() {
	//	@Override protected CachedDecimalFormat initialValue() {
	//		return new CachedDecimalFormat();
	//	}
	//};

	/**
	 * Creates a native peer if we don't already have one, or reconfigures an existing one.
	 * This means we get to reuse the peer in cases like "x=%.2f y=%.2f".
	 */
	//private NativeDecimalFormat getDecimalFormat(String pattern) {
	//	return cachedDecimalFormat.get().update(localeData, pattern);
	//}

	/**
	 * Constructs a {@code Formatter}.
	 * <p>
	 * <p>The output is written to a {@code StringBuilder} which can be acquired by invoking
	 * {@link #out()} and whose content can be obtained by calling {@code toString}.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 */
	public FormatterFull() {
		this(new StringBuilder(), Locale.getDefault());
	}

	/**
	 * Constructs a {@code Formatter} whose output will be written to the
	 * specified {@code Appendable}.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param a the output destination of the {@code Formatter}. If {@code a} is {@code null},
	 *          then a {@code StringBuilder} will be used.
	 */
	public FormatterFull(Appendable a) {
		this(a, Locale.getDefault());
	}

	/**
	 * Constructs a {@code Formatter} with the specified {@code Locale}.
	 * <p>
	 * <p>The output is written to a {@code StringBuilder} which can be acquired by invoking
	 * {@link #out()} and whose content can be obtained by calling {@code toString}.
	 *
	 * @param l the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
	 *          then no localization will be used.
	 */
	public FormatterFull(Locale l) {
		this(new StringBuilder(), l);
	}

	/**
	 * Constructs a {@code Formatter} with the specified {@code Locale}
	 * and whose output will be written to the
	 * specified {@code Appendable}.
	 *
	 * @param a the output destination of the {@code Formatter}. If {@code a} is {@code null},
	 *          then a {@code StringBuilder} will be used.
	 * @param l the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
	 *          then no localization will be used.
	 */
	public FormatterFull(Appendable a, Locale l) {
		if (a == null) {
			out = new StringBuilder();
		} else {
			out = a;
		}
		locale = l;
	}

	/**
	 * Constructs a {@code Formatter} whose output is written to the specified file.
	 * <p>
	 * <p>The charset of the {@code Formatter} is the default charset.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param fileName the filename of the file that is used as the output
	 *                 destination for the {@code Formatter}. The file will be truncated to
	 *                 zero size if the file exists, or else a new file will be
	 *                 created. The output of the {@code Formatter} is buffered.
	 * @throws FileNotFoundException if the filename does not denote a normal and writable file,
	 *                               or if a new file cannot be created, or if any error arises when
	 *                               opening or creating the file.
	 */
	public FormatterFull(String fileName) throws FileNotFoundException {
		this(new File(fileName));

	}

	/**
	 * Constructs a {@code Formatter} whose output is written to the specified file.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param fileName the filename of the file that is used as the output
	 *                 destination for the {@code Formatter}. The file will be truncated to
	 *                 zero size if the file exists, or else a new file will be
	 *                 created. The output of the {@code Formatter} is buffered.
	 * @param csn      the name of the charset for the {@code Formatter}.
	 * @throws FileNotFoundException        if the filename does not denote a normal and writable file,
	 *                                      or if a new file cannot be created, or if any error arises when
	 *                                      opening or creating the file.
	 * @throws UnsupportedEncodingException if the charset with the specified name is not supported.
	 */
	public FormatterFull(String fileName, String csn) throws FileNotFoundException,
		UnsupportedEncodingException {
		this(new File(fileName), csn);
	}

	/**
	 * Constructs a {@code Formatter} with the given {@code Locale} and charset,
	 * and whose output is written to the specified file.
	 *
	 * @param fileName the filename of the file that is used as the output
	 *                 destination for the {@code Formatter}. The file will be truncated to
	 *                 zero size if the file exists, or else a new file will be
	 *                 created. The output of the {@code Formatter} is buffered.
	 * @param csn      the name of the charset for the {@code Formatter}.
	 * @param l        the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
	 *                 then no localization will be used.
	 * @throws FileNotFoundException        if the filename does not denote a normal and writable file,
	 *                                      or if a new file cannot be created, or if any error arises when
	 *                                      opening or creating the file.
	 * @throws UnsupportedEncodingException if the charset with the specified name is not supported.
	 */
	public FormatterFull(String fileName, String csn, Locale l)
		throws FileNotFoundException, UnsupportedEncodingException {

		this(new File(fileName), csn, l);
	}

	/**
	 * Constructs a {@code Formatter} whose output is written to the specified {@code File}.
	 * <p>
	 * The charset of the {@code Formatter} is the default charset.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param file the {@code File} that is used as the output destination for the
	 *             {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
	 *             exists, or else a new {@code File} will be created. The output of the
	 *             {@code Formatter} is buffered.
	 * @throws FileNotFoundException if the {@code File} is not a normal and writable {@code File}, or if a
	 *                               new {@code File} cannot be created, or if any error rises when opening or
	 *                               creating the {@code File}.
	 */
	public FormatterFull(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}

	/**
	 * Constructs a {@code Formatter} with the given charset,
	 * and whose output is written to the specified {@code File}.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param file the {@code File} that is used as the output destination for the
	 *             {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
	 *             exists, or else a new {@code File} will be created. The output of the
	 *             {@code Formatter} is buffered.
	 * @param csn  the name of the charset for the {@code Formatter}.
	 * @throws FileNotFoundException        if the {@code File} is not a normal and writable {@code File}, or if a
	 *                                      new {@code File} cannot be created, or if any error rises when opening or
	 *                                      creating the {@code File}.
	 * @throws UnsupportedEncodingException if the charset with the specified name is not supported.
	 */
	public FormatterFull(File file, String csn) throws FileNotFoundException,
		UnsupportedEncodingException {
		this(file, csn, Locale.getDefault());
	}

	/**
	 * Constructs a {@code Formatter} with the given {@code Locale} and charset,
	 * and whose output is written to the specified {@code File}.
	 *
	 * @param file the {@code File} that is used as the output destination for the
	 *             {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
	 *             exists, or else a new {@code File} will be created. The output of the
	 *             {@code Formatter} is buffered.
	 * @param csn  the name of the charset for the {@code Formatter}.
	 * @param l    the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
	 *             then no localization will be used.
	 * @throws FileNotFoundException        if the {@code File} is not a normal and writable {@code File}, or if a
	 *                                      new {@code File} cannot be created, or if any error rises when opening or
	 *                                      creating the {@code File}.
	 * @throws UnsupportedEncodingException if the charset with the specified name is not supported.
	 */
	public FormatterFull(File file, String csn, Locale l)
		throws FileNotFoundException, UnsupportedEncodingException {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			out = new BufferedWriter(new OutputStreamWriter(fout, csn));
		} catch (RuntimeException e) {
			JTranscIoTools.closeQuietly(fout);
			throw e;
		}

		locale = l;
	}

	/**
	 * Constructs a {@code Formatter} whose output is written to the specified {@code OutputStream}.
	 * <p>
	 * <p>The charset of the {@code Formatter} is the default charset.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param os the stream to be used as the destination of the {@code Formatter}.
	 */
	public FormatterFull(OutputStream os) {
		out = new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset()));
		locale = Locale.getDefault();
	}

	/**
	 * Constructs a {@code Formatter} with the given charset,
	 * and whose output is written to the specified {@code OutputStream}.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param os  the stream to be used as the destination of the {@code Formatter}.
	 * @param csn the name of the charset for the {@code Formatter}.
	 * @throws UnsupportedEncodingException if the charset with the specified name is not supported.
	 */
	public FormatterFull(OutputStream os, String csn) throws UnsupportedEncodingException {
		this(os, csn, Locale.getDefault());
	}

	/**
	 * Constructs a {@code Formatter} with the given {@code Locale} and charset,
	 * and whose output is written to the specified {@code OutputStream}.
	 *
	 * @param os  the stream to be used as the destination of the {@code Formatter}.
	 * @param csn the name of the charset for the {@code Formatter}.
	 * @param l   the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
	 *            then no localization will be used.
	 * @throws UnsupportedEncodingException if the charset with the specified name is not supported.
	 */
	public FormatterFull(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
		out = new BufferedWriter(new OutputStreamWriter(os, csn));
		locale = l;
	}

	/**
	 * Constructs a {@code Formatter} whose output is written to the specified {@code PrintStream}.
	 * <p>
	 * <p>The charset of the {@code Formatter} is the default charset.
	 * <p>
	 * <p>The {@code Locale} used is the user's default locale.
	 * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
	 *
	 * @param ps the {@code PrintStream} used as destination of the {@code Formatter}. If
	 *           {@code ps} is {@code null}, then a {@code NullPointerException} will
	 *           be raised.
	 */
	public FormatterFull(PrintStream ps) {
		if (ps == null) {
			throw new NullPointerException("ps == null");
		}
		out = ps;
		locale = Locale.getDefault();
	}

	private void checkNotClosed() {
		if (closed) {
			throw new FormatterClosedException();
		}
	}

	/**
	 * Returns the {@code Locale} of the {@code Formatter}.
	 *
	 * @return the {@code Locale} for the {@code Formatter} or {@code null} for no {@code Locale}.
	 * @throws FormatterClosedException if the {@code Formatter} has been closed.
	 */
	public Locale locale() {
		checkNotClosed();
		return locale;
	}

	/**
	 * Returns the output destination of the {@code Formatter}.
	 *
	 * @return the output destination of the {@code Formatter}.
	 * @throws FormatterClosedException if the {@code Formatter} has been closed.
	 */
	public Appendable out() {
		checkNotClosed();
		return out;
	}

	/**
	 * Returns the content by calling the {@code toString()} method of the output
	 * destination.
	 *
	 * @return the content by calling the {@code toString()} method of the output
	 * destination.
	 * @throws FormatterClosedException if the {@code Formatter} has been closed.
	 */
	@Override
	public String toString() {
		checkNotClosed();
		return out.toString();
	}

	/**
	 * Flushes the {@code Formatter}. If the output destination is {@link Flushable},
	 * then the method {@code flush()} will be called on that destination.
	 *
	 * @throws FormatterClosedException if the {@code Formatter} has been closed.
	 */
	public void flush() {
		checkNotClosed();
		if (out instanceof Flushable) {
			try {
				((Flushable) out).flush();
			} catch (IOException e) {
				lastIOException = e;
			}
		}
	}

	/**
	 * Closes the {@code Formatter}. If the output destination is {@link Closeable},
	 * then the method {@code close()} will be called on that destination.
	 * <p>
	 * If the {@code Formatter} has been closed, then calling the this method will have no
	 * effect.
	 * <p>
	 * Any method but the {@link #ioException()} that is called after the
	 * {@code Formatter} has been closed will raise a {@code FormatterClosedException}.
	 */
	public void close() {
		if (!closed) {
			closed = true;
			try {
				if (out instanceof Closeable) {
					((Closeable) out).close();
				}
			} catch (IOException e) {
				lastIOException = e;
			}
		}
	}

	/**
	 * Returns the last {@code IOException} thrown by the {@code Formatter}'s output
	 * destination. If the {@code append()} method of the destination does not throw
	 * {@code IOException}s, the {@code ioException()} method will always return {@code null}.
	 *
	 * @return the last {@code IOException} thrown by the {@code Formatter}'s output
	 * destination.
	 */
	public IOException ioException() {
		return lastIOException;
	}

	/**
	 * Writes a formatted string to the output destination of the {@code Formatter}.
	 *
	 * @param format a format string.
	 * @param args   the arguments list used in the {@code format()} method. If there are
	 *               more arguments than those specified by the format string, then
	 *               the additional arguments are ignored.
	 * @return this {@code Formatter}.
	 * @throws IllegalFormatException   if the format string is illegal or incompatible with the
	 *                                  arguments, or if fewer arguments are sent than those required by
	 *                                  the format string, or any other illegal situation.
	 * @throws FormatterClosedException if the {@code Formatter} has been closed.
	 */
	public FormatterFull format(String format, Object... args) {
		return format(this.locale, format, args);
	}

	/**
	 * Writes a formatted string to the output destination of the {@code Formatter}.
	 *
	 * @param l      the {@code Locale} used in the method. If {@code locale} is
	 *               {@code null}, then no localization will be applied. This
	 *               parameter does not change this Formatter's default {@code Locale}
	 *               as specified during construction, and only applies for the
	 *               duration of this call.
	 * @param format a format string.
	 * @param args   the arguments list used in the {@code format()} method. If there are
	 *               more arguments than those specified by the format string, then
	 *               the additional arguments are ignored.
	 * @return this {@code Formatter}.
	 * @throws IllegalFormatException   if the format string is illegal or incompatible with the
	 *                                  arguments, or if fewer arguments are sent than those required by
	 *                                  the format string, or any other illegal situation.
	 * @throws FormatterClosedException if the {@code Formatter} has been closed.
	 */
	public FormatterFull format(Locale l, String format, Object... args) {
		Locale originalLocale = locale;
		try {
			this.locale = (l == null ? Locale.US : l);
			//this.localeData = LocaleData.get(locale);
			doFormat(format, args);
		} finally {
			this.locale = originalLocale;
		}
		return this;
	}

	private void doFormat(String format, Object... args) {
		checkNotClosed();

		FormatSpecifierParser fsp = new FormatSpecifierParser(format);
		int currentObjectIndex = 0;
		Object lastArgument = null;
		boolean hasLastArgumentSet = false;

		int length = format.length();
		int i = 0;
		while (i < length) {
			// Find the maximal plain-text sequence...
			int plainTextStart = i;
			int nextPercent = format.indexOf('%', i);
			int plainTextEnd = (nextPercent == -1) ? length : nextPercent;
			// ...and output it.
			if (plainTextEnd > plainTextStart) {
				outputCharSequence(format, plainTextStart, plainTextEnd);
			}
			i = plainTextEnd;
			// Do we have a format specifier?
			if (i < length) {
				FormatToken token = fsp.parseFormatToken(i + 1);

				Object argument = null;
				if (token.requireArgument()) {
					int index = token.getArgIndex() == FormatToken.UNSET ? currentObjectIndex++ : token.getArgIndex();
					argument = getArgument(args, index, fsp, lastArgument, hasLastArgumentSet);
					lastArgument = argument;
					hasLastArgumentSet = true;
				}

				CharSequence substitution = transform(token, argument);
				// The substitution is null if we called Formattable.formatTo.
				if (substitution != null) {
					outputCharSequence(substitution, 0, substitution.length());
				}
				i = fsp.i;
			}
		}
	}

	// Fixes http://code.google.com/p/android/issues/detail?id=1767.
	private void outputCharSequence(CharSequence cs, int start, int end) {
		try {
			out.append(cs, start, end);
		} catch (IOException e) {
			lastIOException = e;
		}
	}

	private Object getArgument(Object[] args, int index, FormatSpecifierParser fsp,
							   Object lastArgument, boolean hasLastArgumentSet) {
		if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
			throw new MissingFormatArgumentException("<");
		}

		if (args == null) {
			return null;
		}

		if (index >= args.length) {
			throw new MissingFormatArgumentException(fsp.getFormatSpecifierText());
		}

		if (index == FormatToken.LAST_ARGUMENT_INDEX) {
			return lastArgument;
		}

		return args[index];
	}

	/*
	 * Complete details of a single format specifier parsed from a format string.
     */
	private static class FormatToken {
		static final int LAST_ARGUMENT_INDEX = -2;

		static final int UNSET = -1;

		static final int FLAGS_UNSET = 0;

		static final int DEFAULT_PRECISION = 6;

		static final int FLAG_ZERO = 1 << 4;

		private int argIndex = UNSET;

		// These have package access for performance. They used to be represented by an int bitmask
		// and accessed via methods, but Android's JIT doesn't yet do a good job of such code.
		// Direct field access, on the other hand, is fast.
		boolean flagComma;
		boolean flagMinus;
		boolean flagParenthesis;
		boolean flagPlus;
		boolean flagSharp;
		boolean flagSpace;
		boolean flagZero;

		private char conversionType = (char) UNSET;
		private char dateSuffix;

		private int precision = UNSET;
		private int width = UNSET;

		private StringBuilder strFlags;

		// Tests whether there were no flags, no width, and no precision specified.
		boolean isDefault() {
			return !flagComma && !flagMinus && !flagParenthesis && !flagPlus && !flagSharp &&
				!flagSpace && !flagZero && width == UNSET && precision == UNSET;
		}

		boolean isPrecisionSet() {
			return precision != UNSET;
		}

		int getArgIndex() {
			return argIndex;
		}

		void setArgIndex(int index) {
			argIndex = index;
		}

		int getWidth() {
			return width;
		}

		void setWidth(int width) {
			this.width = width;
		}

		int getPrecision() {
			return precision;
		}

		void setPrecision(int precise) {
			this.precision = precise;
		}

		String getStrFlags() {
			return (strFlags != null) ? strFlags.toString() : "";
		}

		/*
		 * Sets qualified char as one of the flags. If the char is qualified,
         * sets it as a flag and returns true. Or else returns false.
         */
		boolean setFlag(int ch) {
			boolean dupe = false;
			switch (ch) {
				case ',':
					dupe = flagComma;
					flagComma = true;
					break;
				case '-':
					dupe = flagMinus;
					flagMinus = true;
					break;
				case '(':
					dupe = flagParenthesis;
					flagParenthesis = true;
					break;
				case '+':
					dupe = flagPlus;
					flagPlus = true;
					break;
				case '#':
					dupe = flagSharp;
					flagSharp = true;
					break;
				case ' ':
					dupe = flagSpace;
					flagSpace = true;
					break;
				case '0':
					dupe = flagZero;
					flagZero = true;
					break;
				default:
					return false;
			}
			if (dupe) {
				// The RI documentation implies we're supposed to report all the flags, not just
				// the first duplicate, but the RI behaves the same as we do.
				throw new DuplicateFormatFlagsException(String.valueOf(ch));
			}
			if (strFlags == null) {
				strFlags = new StringBuilder(7); // There are seven possible flags.
			}
			strFlags.append((char) ch);
			return true;
		}

		char getConversionType() {
			return conversionType;
		}

		void setConversionType(char c) {
			conversionType = c;
		}

		char getDateSuffix() {
			return dateSuffix;
		}

		void setDateSuffix(char c) {
			dateSuffix = c;
		}

		boolean requireArgument() {
			return conversionType != '%' && conversionType != 'n';
		}

		void checkFlags(Object arg) {
			// Work out which flags are allowed.
			boolean allowComma = false;
			boolean allowMinus = true;
			boolean allowParenthesis = false;
			boolean allowPlus = false;
			boolean allowSharp = false;
			boolean allowSpace = false;
			boolean allowZero = false;
			// Precision and width?
			boolean allowPrecision = true;
			boolean allowWidth = true;
			// Argument?
			boolean allowArgument = true;
			switch (conversionType) {
				// Character and date/time.
				case 'c':
				case 'C':
				case 't':
				case 'T':
					// Only '-' is allowed.
					allowPrecision = false;
					break;

				// String.
				case 's':
				case 'S':
					if (arg instanceof Formattable) {
						allowSharp = true;
					}
					break;

				// Floating point.
				case 'g':
				case 'G':
					allowComma = allowParenthesis = allowPlus = allowSpace = allowZero = true;
					break;
				case 'f':
					allowComma = allowParenthesis = allowPlus = allowSharp = allowSpace = allowZero = true;
					break;
				case 'e':
				case 'E':
					allowParenthesis = allowPlus = allowSharp = allowSpace = allowZero = true;
					break;
				case 'a':
				case 'A':
					allowPlus = allowSharp = allowSpace = allowZero = true;
					break;

				// Integral.
				case 'd':
					allowComma = allowParenthesis = allowPlus = allowSpace = allowZero = true;
					allowPrecision = false;
					break;
				case 'o':
				case 'x':
				case 'X':
					allowSharp = allowZero = true;
					//if (arg == null || arg instanceof BigInteger) {
					if (arg == null) {
						allowParenthesis = allowPlus = allowSpace = true;
					}
					allowPrecision = false;
					break;

				// Special.
				case 'n':
					// Nothing is allowed.
					allowMinus = false;
					allowArgument = allowPrecision = allowWidth = false;
					break;
				case '%':
					// The only flag allowed is '-', and no argument or precision is allowed.
					allowArgument = false;
					allowPrecision = false;
					break;

				// Booleans and hash codes.
				case 'b':
				case 'B':
				case 'h':
				case 'H':
					break;

				default:
					throw unknownFormatConversionException();
			}

			// Check for disallowed flags.
			String mismatch = null;
			if (!allowComma && flagComma) {
				mismatch = ",";
			} else if (!allowMinus && flagMinus) {
				mismatch = "-";
			} else if (!allowParenthesis && flagParenthesis) {
				mismatch = "(";
			} else if (!allowPlus && flagPlus) {
				mismatch = "+";
			} else if (!allowSharp && flagSharp) {
				mismatch = "#";
			} else if (!allowSpace && flagSpace) {
				mismatch = " ";
			} else if (!allowZero && flagZero) {
				mismatch = "0";
			}
			if (mismatch != null) {
				if (conversionType == 'n') {
					// For no good reason, %n is a special case...
					throw new IllegalFormatFlagsException(mismatch);
				} else {
					throw new FormatFlagsConversionMismatchException(mismatch, conversionType);
				}
			}

			// Check for a missing width with flags that require a width.
			if ((flagMinus || flagZero) && width == UNSET) {
				throw new MissingFormatWidthException("-" + conversionType);
			}

			// Check that no-argument conversion types don't have an argument.
			// Note: the RI doesn't enforce this.
			if (!allowArgument && argIndex != UNSET) {
				throw new IllegalFormatFlagsException("%" + conversionType +
					" doesn't take an argument");
			}

			// Check that we don't have a precision or width where they're not allowed.
			if (!allowPrecision && precision != UNSET) {
				throw new IllegalFormatPrecisionException(precision);
			}
			if (!allowWidth && width != UNSET) {
				throw new IllegalFormatWidthException(width);
			}

			// Some combinations make no sense...
			if (flagPlus && flagSpace) {
				throw new IllegalFormatFlagsException("the '+' and ' ' flags are incompatible");
			}
			if (flagMinus && flagZero) {
				throw new IllegalFormatFlagsException("the '-' and '0' flags are incompatible");
			}
		}

		public UnknownFormatConversionException unknownFormatConversionException() {
			if (conversionType == 't' || conversionType == 'T') {
				throw new UnknownFormatConversionException(String.format("%c%c",
					conversionType, dateSuffix));
			}
			throw new UnknownFormatConversionException(String.valueOf(conversionType));
		}
	}

	/*
     * Gets the formatted string according to the format token and the
     * argument.
     */
	private CharSequence transform(FormatToken token, Object argument) {
		this.formatToken = token;
		this.arg = argument;

		// There are only two format specifiers that matter: "%d" and "%s".
		// Nothing else is common in the wild. We fast-path these two to
		// avoid the heavyweight machinery needed to cope with flags, width,
		// and precision.
		if (token.isDefault()) {
			switch (token.getConversionType()) {
				case 's':
					if (arg == null) {
						return "null";
					} else if (!(arg instanceof Formattable)) {
						return arg.toString();
					}
					break;
				case 'd':
					boolean needLocalizedDigits = (zeroDigit() != '0');
					if (out instanceof StringBuilder && !needLocalizedDigits) {
						if (arg instanceof Integer || arg instanceof Short || arg instanceof Byte || arg instanceof Long) {
							try {
								out.append(arg.toString());
							} catch (IOException e) {
								e.printStackTrace();
							}
							return null;
						}
					}
					if (arg instanceof Integer || arg instanceof Long || arg instanceof Short || arg instanceof Byte) {
						String result = arg.toString();
						return needLocalizedDigits ? localizeDigits(result) : result;
					}
			}
		}

		formatToken.checkFlags(arg);
		CharSequence result;
		switch (token.getConversionType()) {
			case 'B':
			case 'b':
				result = transformFromBoolean();
				break;
			case 'H':
			case 'h':
				result = transformFromHashCode();
				break;
			case 'S':
			case 's':
				result = transformFromString();
				break;
			case 'C':
			case 'c':
				result = transformFromCharacter();
				break;
			case 'd':
			case 'o':
			case 'x':
			case 'X':
				//if (arg == null || arg instanceof BigInteger) {
				//	result = transformFromBigInteger();
				//} else {
				result = transformFromInteger();
				//}
				break;
			case 'A':
			case 'a':
			case 'E':
			case 'e':
			case 'f':
			case 'G':
			case 'g':
				result = transformFromFloat();
				break;
			case '%':
				result = transformFromPercent();
				break;
			case 'n':
				result = System.lineSeparator();
				break;
			case 't':
			case 'T':
				//result = transformFromDateTime();
				result = "/transformFromDateTime/";
				break;
			default:
				throw token.unknownFormatConversionException();
		}

		if (Character.isUpperCase(token.getConversionType())) {
			if (result != null) {
				result = result.toString().toUpperCase(locale);
			}
		}
		return result;
	}

	private IllegalFormatConversionException badArgumentType() {
		throw new IllegalFormatConversionException(formatToken.getConversionType(), arg.getClass());
	}

	/**
	 * Returns a CharSequence corresponding to {@code s} with all the ASCII digits replaced
	 * by digits appropriate to this formatter's locale. Other characters remain unchanged.
	 */
	private CharSequence localizeDigits(CharSequence s) {
		int length = s.length();
		int offsetToLocalizedDigits = zeroDigit() - '0';
		StringBuilder result = new StringBuilder(length);
		for (int i = 0; i < length; ++i) {
			char ch = s.charAt(i);
			if (ch >= '0' && ch <= '9') {
				ch += offsetToLocalizedDigits;
			}
			result.append(ch);
		}
		return result;
	}

	/**
	 * Inserts the grouping separator every 3 digits. DecimalFormat lets you configure grouping
	 * size, but you can't access that from Formatter, and the default is every 3 digits.
	 */
	private CharSequence insertGrouping(CharSequence s) {
		StringBuilder result = new StringBuilder(s.length() + s.length() / 3);

		// A leading '-' doesn't want to be included in the grouping.
		int digitsLength = s.length();
		int i = 0;
		if (s.charAt(0) == '-') {
			--digitsLength;
			++i;
			result.append('-');
		}

		// Append the digits that come before the first separator.
		int headLength = digitsLength % 3;
		if (headLength == 0) {
			headLength = 3;
		}
		result.append(s, i, i + headLength);
		i += headLength;

		// Append the remaining groups.
		for (; i < s.length(); i += 3) {
			result.append(groupingSeparator());
			result.append(s, i, i + 3);
		}
		return result;
	}

	private CharSequence transformFromBoolean() {
		CharSequence result;
		if (arg instanceof Boolean) {
			result = arg.toString();
		} else if (arg == null) {
			result = "false";
		} else {
			result = "true";
		}
		return padding(result, 0);
	}

	private CharSequence transformFromHashCode() {
		CharSequence result;
		if (arg == null) {
			result = "null";
		} else {
			result = Integer.toHexString(arg.hashCode());
		}
		return padding(result, 0);
	}

	private CharSequence transformFromString() {
		if (arg instanceof Formattable) {
			int flags = 0;
			if (formatToken.flagMinus) {
				flags |= FormattableFlags.LEFT_JUSTIFY;
			}
			if (formatToken.flagSharp) {
				flags |= FormattableFlags.ALTERNATE;
			}
			if (Character.isUpperCase(formatToken.getConversionType())) {
				flags |= FormattableFlags.UPPERCASE;
			}


			//((Formattable) arg).formatTo(this, flags, formatToken.getWidth(), formatToken.getPrecision());
			throw new RuntimeException("((Formattable) arg).formatTo(this, flags, formatToken.getWidth(), formatToken.getPrecision());");
			//return null;
		}
		CharSequence result = arg != null ? arg.toString() : "null";
		return padding(result, 0);
	}

	private CharSequence transformFromCharacter() {
		if (arg == null) {
			return padding("null", 0);
		}
		if (arg instanceof Character) {
			return padding(String.valueOf(arg), 0);
		} else if (arg instanceof Byte || arg instanceof Short || arg instanceof Integer) {
			int codePoint = ((Number) arg).intValue();
			if (!Character.isValidCodePoint(codePoint)) {
				throw new IllegalFormatCodePointException(codePoint);
			}
			CharSequence result = (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT)
				? String.valueOf((char) codePoint)
				: String.valueOf(Character.toChars(codePoint));
			return padding(result, 0);
		} else {
			throw badArgumentType();
		}
	}

	private CharSequence transformFromPercent() {
		return padding("%", 0);
	}

	private CharSequence padding(CharSequence source, int startIndex) {
		int start = startIndex;
		int width = formatToken.getWidth();
		int precision = formatToken.getPrecision();

		int length = source.length();
		if (precision >= 0) {
			length = Math.min(length, precision);
			if (source instanceof StringBuilder) {
				((StringBuilder) source).setLength(length);
			} else {
				source = source.subSequence(0, length);
			}
		}
		if (width > 0) {
			width = Math.max(source.length(), width);
		}
		if (length >= width) {
			return source;
		}

		char paddingChar = '\u0020'; // space as padding char.
		if (formatToken.flagZero) {
			if (formatToken.getConversionType() == 'd') {
				paddingChar = zeroDigit();
			} else {
				paddingChar = '0'; // No localized digits for bases other than decimal.
			}
		} else {
			// if padding char is space, always pad from the start.
			start = 0;
		}
		char[] paddingChars = new char[width - length];
		Arrays.fill(paddingChars, paddingChar);

		boolean paddingRight = formatToken.flagMinus;
		StringBuilder result = toStringBuilder(source);
		if (paddingRight) {
			result.append(paddingChars);
		} else {
			result.insert(start, paddingChars);
		}
		return result;
	}

	private StringBuilder toStringBuilder(CharSequence cs) {
		return cs instanceof StringBuilder ? (StringBuilder) cs : new StringBuilder(cs);
	}

	private StringBuilder wrapParentheses(StringBuilder result) {
		result.setCharAt(0, '('); // Replace the '-'.
		if (formatToken.flagZero) {
			formatToken.setWidth(formatToken.getWidth() - 1);
			result = (StringBuilder) padding(result, 1);
			result.append(')');
		} else {
			result.append(')');
			result = (StringBuilder) padding(result, 0);
		}
		return result;
	}

	private CharSequence transformFromInteger() {
		int startIndex = 0;
		StringBuilder result = new StringBuilder();
		char currentConversionType = formatToken.getConversionType();

		long value;
		if (arg instanceof Long) {
			value = ((Long) arg).longValue();
		} else if (arg instanceof Integer) {
			value = ((Integer) arg).longValue();
		} else if (arg instanceof Short) {
			value = ((Short) arg).longValue();
		} else if (arg instanceof Byte) {
			value = ((Byte) arg).longValue();
		} else {
			throw badArgumentType();
		}

		if (formatToken.flagSharp) {
			if (currentConversionType == 'o') {
				result.append("0");
				startIndex += 1;
			} else {
				result.append("0x");
				startIndex += 2;
			}
		}

		if (currentConversionType == 'd') {
			CharSequence digits = Long.toString(value);
			if (formatToken.flagComma) {
				digits = insertGrouping(digits);
			}
			if (zeroDigit() != '0') {
				digits = localizeDigits(digits);
			}
			result.append(digits);

			if (value < 0) {
				if (formatToken.flagParenthesis) {
					return wrapParentheses(result);
				} else if (formatToken.flagZero) {
					startIndex++;
				}
			} else {
				if (formatToken.flagPlus) {
					result.insert(0, '+');
					startIndex += 1;
				} else if (formatToken.flagSpace) {
					result.insert(0, ' ');
					startIndex += 1;
				}
			}
		} else {
			// Undo sign-extension, since we'll be using Long.to(Octal|Hex)String.
			if (arg instanceof Byte) {
				value &= 0xffL;
			} else if (arg instanceof Short) {
				value &= 0xffffL;
			} else if (arg instanceof Integer) {
				value &= 0xffffffffL;
			}
			if (currentConversionType == 'o') {
				result.append(Long.toOctalString(value));
			} else {
				result.append(Long.toHexString(value));
			}
		}

		return padding(result, startIndex);
	}

	private CharSequence transformFromNull() {
		formatToken.flagZero = false;
		return padding("null", 0);
	}

	//private CharSequence transformFromBigInteger() {
	//	int startIndex = 0;
	//	StringBuilder result = new StringBuilder();
	//	BigInteger bigInt = (BigInteger) arg;
	//	char currentConversionType = formatToken.getConversionType();
//
	//	if (bigInt == null) {
	//		return transformFromNull();
	//	}
//
	//	boolean isNegative = (bigInt.compareTo(BigInteger.ZERO) < 0);
//
	//	if (currentConversionType == 'd') {
	//		CharSequence digits = bigInt.toString(10);
	//		if (formatToken.flagComma) {
	//			digits = insertGrouping(digits);
	//		}
	//		result.append(digits);
	//	} else if (currentConversionType == 'o') {
	//		// convert BigInteger to a string presentation using radix 8
	//		result.append(bigInt.toString(8));
	//	} else {
	//		// convert BigInteger to a string presentation using radix 16
	//		result.append(bigInt.toString(16));
	//	}
	//	if (formatToken.flagSharp) {
	//		startIndex = isNegative ? 1 : 0;
	//		if (currentConversionType == 'o') {
	//			result.insert(startIndex, "0");
	//			startIndex += 1;
	//		} else if (currentConversionType == 'x' || currentConversionType == 'X') {
	//			result.insert(startIndex, "0x");
	//			startIndex += 2;
	//		}
	//	}
//
	//	if (!isNegative) {
	//		if (formatToken.flagPlus) {
	//			result.insert(0, '+');
	//			startIndex += 1;
	//		}
	//		if (formatToken.flagSpace) {
	//			result.insert(0, ' ');
	//			startIndex += 1;
	//		}
	//	}
//
	//    /* pad paddingChar to the output */
	//	if (isNegative && formatToken.flagParenthesis) {
	//		return wrapParentheses(result);
	//	}
	//	if (isNegative && formatToken.flagZero) {
	//		startIndex++;
	//	}
	//	return padding(result, startIndex);
	//}

	private CharSequence transformFromDateTime() {
		if (arg == null) {
			return transformFromNull();
		}

		Calendar calendar;
		if (arg instanceof Calendar) {
			calendar = (Calendar) arg;
		} else {
			Date date = null;
			if (arg instanceof Long) {
				date = new Date(((Long) arg).longValue());
			} else if (arg instanceof Date) {
				date = (Date) arg;
			} else {
				throw badArgumentType();
			}
			calendar = Calendar.getInstance(locale);
			calendar.setTime(date);
		}

		StringBuilder result = new StringBuilder();
		if (!appendT(result, formatToken.getDateSuffix(), calendar)) {
			throw formatToken.unknownFormatConversionException();
		}
		return padding(result, 0);
	}

	private boolean appendT(StringBuilder result, char conversion, Calendar calendar) {
		switch (conversion) {
			case 'A':
				result.append(longWeekdayNames(calendar.get(Calendar.DAY_OF_WEEK)));
				return true;
			case 'a':
				result.append(shortWeekdayNames(calendar.get(Calendar.DAY_OF_WEEK)));
				return true;
			case 'B':
				result.append(longMonthNames(calendar.get(Calendar.MONTH)));
				return true;
			case 'b':
			case 'h':
				result.append(shortMonthNames(calendar.get(Calendar.MONTH)));
				return true;
			case 'C':
				appendLocalized(result, calendar.get(Calendar.YEAR) / 100, 2);
				return true;
			case 'D':
				appendT(result, 'm', calendar);
				result.append('/');
				appendT(result, 'd', calendar);
				result.append('/');
				appendT(result, 'y', calendar);
				return true;
			case 'F':
				appendT(result, 'Y', calendar);
				result.append('-');
				appendT(result, 'm', calendar);
				result.append('-');
				appendT(result, 'd', calendar);
				return true;
			case 'H':
				appendLocalized(result, calendar.get(Calendar.HOUR_OF_DAY), 2);
				return true;
			case 'I':
				appendLocalized(result, to12Hour(calendar.get(Calendar.HOUR)), 2);
				return true;
			case 'L':
				appendLocalized(result, calendar.get(Calendar.MILLISECOND), 3);
				return true;
			case 'M':
				appendLocalized(result, calendar.get(Calendar.MINUTE), 2);
				return true;
			case 'N':
				appendLocalized(result, calendar.get(Calendar.MILLISECOND) * 1000000L, 9);
				return true;
			case 'Q':
				appendLocalized(result, calendar.getTimeInMillis(), 0);
				return true;
			case 'R':
				appendT(result, 'H', calendar);
				result.append(':');
				appendT(result, 'M', calendar);
				return true;
			case 'S':
				appendLocalized(result, calendar.get(Calendar.SECOND), 2);
				return true;
			case 'T':
				appendT(result, 'H', calendar);
				result.append(':');
				appendT(result, 'M', calendar);
				result.append(':');
				appendT(result, 'S', calendar);
				return true;
			case 'Y':
				appendLocalized(result, calendar.get(Calendar.YEAR), 4);
				return true;
			case 'Z':
				TimeZone timeZone = calendar.getTimeZone();
				result.append(timeZone.getDisplayName(timeZone.inDaylightTime(calendar.getTime()),
					TimeZone.SHORT, locale));
				return true;
			case 'c':
				appendT(result, 'a', calendar);
				result.append(' ');
				appendT(result, 'b', calendar);
				result.append(' ');
				appendT(result, 'd', calendar);
				result.append(' ');
				appendT(result, 'T', calendar);
				result.append(' ');
				appendT(result, 'Z', calendar);
				result.append(' ');
				appendT(result, 'Y', calendar);
				return true;
			case 'd':
				appendLocalized(result, calendar.get(Calendar.DAY_OF_MONTH), 2);
				return true;
			case 'e':
				appendLocalized(result, calendar.get(Calendar.DAY_OF_MONTH), 0);
				return true;
			case 'j':
				appendLocalized(result, calendar.get(Calendar.DAY_OF_YEAR), 3);
				return true;
			case 'k':
				appendLocalized(result, calendar.get(Calendar.HOUR_OF_DAY), 0);
				return true;
			case 'l':
				appendLocalized(result, to12Hour(calendar.get(Calendar.HOUR)), 0);
				return true;
			case 'm':
				// Calendar.JANUARY is 0; humans want January represented as 1.
				appendLocalized(result, calendar.get(Calendar.MONTH) + 1, 2);
				return true;
			case 'p':
				result.append(amPm(calendar.get(Calendar.AM_PM)).toLowerCase(locale));
				return true;
			case 'r':
				appendT(result, 'I', calendar);
				result.append(':');
				appendT(result, 'M', calendar);
				result.append(':');
				appendT(result, 'S', calendar);
				result.append(' ');
				result.append(amPm(calendar.get(Calendar.AM_PM)));
				return true;
			case 's':
				appendLocalized(result, calendar.getTimeInMillis() / 1000, 0);
				return true;
			case 'y':
				appendLocalized(result, calendar.get(Calendar.YEAR) % 100, 2);
				return true;
			case 'z':
				long offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
				char sign = '+';
				if (offset < 0) {
					sign = '-';
					offset = -offset;
				}
				result.append(sign);
				appendLocalized(result, offset / 3600000, 2);
				appendLocalized(result, (offset % 3600000) / 60000, 2);
				return true;
		}
		return false;
	}

	private int to12Hour(int hour) {
		return hour == 0 ? 12 : hour;
	}

	private void appendLocalized(StringBuilder result, long value, int width) {
		int paddingIndex = result.length();
		char zeroDigit = zeroDigit();
		if (zeroDigit == '0') {
			result.append(value);
		} else {
			result.append(localizeDigits(Long.toString(value)));
		}
		int zeroCount = width - (result.length() - paddingIndex);
		if (zeroCount <= 0) {
			return;
		}
		if (zeroDigit == '0') {
			result.insert(paddingIndex, ZEROS, 0, zeroCount);
		} else {
			for (int i = 0; i < zeroCount; ++i) {
				result.insert(paddingIndex, zeroDigit);
			}
		}
	}

	private CharSequence transformFromSpecialNumber(double d) {
		String source = null;
		if (Double.isNaN(d)) {
			source = "NaN";
		} else if (d == Double.POSITIVE_INFINITY) {
			if (formatToken.flagPlus) {
				source = "+Infinity";
			} else if (formatToken.flagSpace) {
				source = " Infinity";
			} else {
				source = "Infinity";
			}
		} else if (d == Double.NEGATIVE_INFINITY) {
			if (formatToken.flagParenthesis) {
				source = "(Infinity)";
			} else {
				source = "-Infinity";
			}
		} else {
			return null;
		}

		formatToken.setPrecision(FormatToken.UNSET);
		formatToken.flagZero = false;
		return padding(source, 0);
	}

	private CharSequence transformFromFloat() {
		if (arg == null) {
			return transformFromNull();
		} else if (arg instanceof Float || arg instanceof Double) {
			Number number = (Number) arg;
			double d = number.doubleValue();
			if (d != d || d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY) {
				return transformFromSpecialNumber(d);
			}
			//} else if (arg instanceof BigDecimal) {
			//	// BigDecimal can't represent NaN or infinities, but its doubleValue method will return
			//	// infinities if the BigDecimal is too big for a double.
		} else {
			throw badArgumentType();
		}

		char conversionType = formatToken.getConversionType();
		if (conversionType != 'a' && conversionType != 'A' && !formatToken.isPrecisionSet()) {
			formatToken.setPrecision(FormatToken.DEFAULT_PRECISION);
		}

		StringBuilder result = new StringBuilder();
		switch (conversionType) {
			case 'a':
			case 'A':
				transformA(result);
				break;
			case 'e':
			case 'E':
				transformE(result);
				break;
			case 'f':
				transformF(result);
				break;
			case 'g':
			case 'G':
				transformF(result);
				//transformG(result);
				break;
			default:
				throw formatToken.unknownFormatConversionException();
		}

		formatToken.setPrecision(FormatToken.UNSET);

		int startIndex = 0;
		if (result.charAt(0) == minusSign()) {
			if (formatToken.flagParenthesis) {
				return wrapParentheses(result);
			}
		} else {
			if (formatToken.flagSpace) {
				result.insert(0, ' ');
				startIndex++;
			}
			if (formatToken.flagPlus) {
				result.insert(0, '+');
				startIndex++;
			}
		}

		char firstChar = result.charAt(0);
		if (formatToken.flagZero && (firstChar == '+' || firstChar == minusSign())) {
			startIndex = 1;
		}

		if (conversionType == 'a' || conversionType == 'A') {
			startIndex += 2;
		}
		return padding(result, startIndex);
	}

	private void transformE(StringBuilder result) {
		// All zeros in this method are *pattern* characters, so no localization.
		final int precision = formatToken.getPrecision();
		String pattern = "0E+00";
		if (precision > 0) {
			StringBuilder sb = new StringBuilder("0.");
			char[] zeros = new char[precision];
			Arrays.fill(zeros, '0');
			sb.append(zeros);
			sb.append("E+00");
			pattern = sb.toString();
		}

		//NativeDecimalFormat nf = getDecimalFormat(pattern);
		//char[] chars;
		//if (arg instanceof BigDecimal) {
		//	chars = nf.formatBigDecimal((BigDecimal) arg, null);
		//} else {
		//	chars = nf.formatDouble(((Number) arg).doubleValue(), null);
		//}
		String chars = arg.toString();
		// Unlike %f, %e uses 'e' (regardless of what the DecimalFormatSymbols would have us use).
		//for (int i = 0; i < chars.length(); ++i) {
		//	if (chars.charAt(i) == 'E') {
		//		chars.charAt(i) = 'e';
		//	}
		//}
		result.append(chars);
		// The # flag requires that we always output a decimal separator.
		if (formatToken.flagSharp && precision == 0) {
			int indexOfE = result.indexOf("e");
			result.insert(indexOfE, decimalSeparator());
		}
	}

	//private void transformG(StringBuilder result) {
	//	int precision = formatToken.getPrecision();
	//	if (precision == 0) {
	//		precision = 1;
	//	}
	//	formatToken.setPrecision(precision);
//
	//	double d = ((Number) arg).doubleValue();
	//	if (d == 0.0) {
	//		precision--;
	//		formatToken.setPrecision(precision);
	//		transformF(result);
	//		return;
	//	}
//
	//	boolean requireScientificRepresentation = true;
	//	d = Math.abs(d);
	//	if (Double.isInfinite(d)) {
	//		precision = formatToken.getPrecision();
	//		precision--;
	//		formatToken.setPrecision(precision);
	//		transformE(result);
	//		return;
	//	}
	//	BigDecimal b = new BigDecimal(d, new MathContext(precision));
	//	d = b.doubleValue();
	//	long l = b.longValue();
//
	//	if (d >= 1 && d < Math.pow(10, precision)) {
	//		if (l < Math.pow(10, precision)) {
	//			requireScientificRepresentation = false;
	//			precision -= String.valueOf(l).length();
	//			precision = precision < 0 ? 0 : precision;
	//			l = Math.round(d * Math.pow(10, precision + 1));
	//			if (String.valueOf(l).length() <= formatToken.getPrecision()) {
	//				precision++;
	//			}
	//			formatToken.setPrecision(precision);
	//		}
	//	} else {
	//		l = b.movePointRight(4).longValue();
	//		if (d >= Math.pow(10, -4) && d < 1) {
	//			requireScientificRepresentation = false;
	//			precision += 4 - String.valueOf(l).length();
	//			l = b.movePointRight(precision + 1).longValue();
	//			if (String.valueOf(l).length() <= formatToken.getPrecision()) {
	//				precision++;
	//			}
	//			l = b.movePointRight(precision).longValue();
	//			if (l >= Math.pow(10, precision - 4)) {
	//				formatToken.setPrecision(precision);
	//			}
	//		}
	//	}
	//	if (requireScientificRepresentation) {
	//		precision = formatToken.getPrecision();
	//		precision--;
	//		formatToken.setPrecision(precision);
	//		transformE(result);
	//	} else {
	//		transformF(result);
	//	}
	//}

	private void transformF(StringBuilder result) {
		// All zeros in this method are *pattern* characters, so no localization.
		String pattern = "0.000000";
		final int precision = formatToken.getPrecision();
		if (formatToken.flagComma || precision != FormatToken.DEFAULT_PRECISION) {
			StringBuilder patternBuilder = new StringBuilder();
			if (formatToken.flagComma) {
				patternBuilder.append(',');
				int groupingSize = 3;
				char[] sharps = new char[groupingSize - 1];
				Arrays.fill(sharps, '#');
				patternBuilder.append(sharps);
			}
			patternBuilder.append('0');
			if (precision > 0) {
				patternBuilder.append('.');
				for (int i = 0; i < precision; ++i) {
					patternBuilder.append('0');
				}
			}
			pattern = patternBuilder.toString();
		}

		//NativeDecimalFormat nf = getDecimalFormat(pattern);
		//if (arg instanceof BigDecimal) {
		//	result.append(nf.formatBigDecimal((BigDecimal) arg, null));
		//} else {
		//	result.append(nf.formatDouble(((Number) arg).doubleValue(), null));
		//}
		//// The # flag requires that we always output a decimal separator.
		//if (formatToken.flagSharp && precision == 0) {
		//	result.append(localeData.decimalSeparator);
		//}
		result.append(arg.toString());
	}

	private void transformA(StringBuilder result) {
		if (arg instanceof Float) {
			result.append(Float.toHexString(((Float) arg).floatValue()));
		} else if (arg instanceof Double) {
			result.append(Double.toHexString(((Double) arg).doubleValue()));
		} else {
			throw badArgumentType();
		}

		if (!formatToken.isPrecisionSet()) {
			return;
		}

		int precision = formatToken.getPrecision();
		if (precision == 0) {
			precision = 1;
		}
		int indexOfFirstFractionalDigit = result.indexOf(".") + 1;
		int indexOfP = result.indexOf("p");
		int fractionalLength = indexOfP - indexOfFirstFractionalDigit;

		if (fractionalLength == precision) {
			return;
		}

		if (fractionalLength < precision) {
			char[] zeros = new char[precision - fractionalLength];
			Arrays.fill(zeros, '0'); // %a shouldn't be localized.
			result.insert(indexOfP, zeros);
			return;
		}
		result.delete(indexOfFirstFractionalDigit + precision, indexOfP);
	}

	private static class FormatSpecifierParser {
		private String format;
		private int length;

		private int startIndex;
		private int i;

		/**
		 * Constructs a new parser for the given format string.
		 */
		FormatSpecifierParser(String format) {
			this.format = format;
			this.length = format.length();
		}

		/**
		 * Returns a FormatToken representing the format specifier starting at 'offset'.
		 *
		 * @param offset the first character after the '%'
		 */
		FormatToken parseFormatToken(int offset) {
			this.startIndex = offset;
			this.i = offset;
			return parseArgumentIndexAndFlags(new FormatToken());
		}

		/**
		 * Returns a string corresponding to the last format specifier that was parsed.
		 * Used to construct error messages.
		 */
		String getFormatSpecifierText() {
			return format.substring(startIndex, i);
		}

		private int peek() {
			return (i < length) ? format.charAt(i) : -1;
		}

		private char advance() {
			if (i >= length) {
				throw unknownFormatConversionException();
			}
			return format.charAt(i++);
		}

		private UnknownFormatConversionException unknownFormatConversionException() {
			throw new UnknownFormatConversionException(getFormatSpecifierText());
		}

		private FormatToken parseArgumentIndexAndFlags(FormatToken token) {
			// Parse the argument index, if there is one.
			int position = i;
			int ch = peek();
			if (Character.isDigit(ch)) {
				int number = nextInt();
				if (peek() == '$') {
					// The number was an argument index.
					advance(); // Swallow the '$'.
					if (number == FormatToken.UNSET) {
						throw new MissingFormatArgumentException(getFormatSpecifierText());
					}
					// k$ stands for the argument whose index is k-1 except that
					// 0$ and 1$ both stand for the first element.
					token.setArgIndex(Math.max(0, number - 1));
				} else {
					if (ch == '0') {
						// The digit zero is a format flag, so reparse it as such.
						i = position;
					} else {
						// The number was a width. This means there are no flags to parse.
						return parseWidth(token, number);
					}
				}
			} else if (ch == '<') {
				token.setArgIndex(FormatToken.LAST_ARGUMENT_INDEX);
				advance();
			}

			// Parse the flags.
			while (token.setFlag(peek())) {
				advance();
			}

			// What comes next?
			ch = peek();
			if (Character.isDigit(ch)) {
				return parseWidth(token, nextInt());
			} else if (ch == '.') {
				return parsePrecision(token);
			} else {
				return parseConversionType(token);
			}
		}

		// We pass the width in because in some cases we've already parsed it.
		// (Because of the ambiguity between argument indexes and widths.)
		private FormatToken parseWidth(FormatToken token, int width) {
			token.setWidth(width);
			int ch = peek();
			if (ch == '.') {
				return parsePrecision(token);
			} else {
				return parseConversionType(token);
			}
		}

		private FormatToken parsePrecision(FormatToken token) {
			advance(); // Swallow the '.'.
			int ch = peek();
			if (Character.isDigit(ch)) {
				token.setPrecision(nextInt());
				return parseConversionType(token);
			} else {
				// The precision is required but not given by the format string.
				throw unknownFormatConversionException();
			}
		}

		private FormatToken parseConversionType(FormatToken token) {
			char conversionType = advance(); // A conversion type is mandatory.
			token.setConversionType(conversionType);
			if (conversionType == 't' || conversionType == 'T') {
				char dateSuffix = advance(); // A date suffix is mandatory for 't' or 'T'.
				token.setDateSuffix(dateSuffix);
			}
			return token;
		}

		// Parses an integer (of arbitrary length, but typically just one digit).
		private int nextInt() {
			long value = 0;
			while (i < length && Character.isDigit(format.charAt(i))) {
				value = 10 * value + (format.charAt(i++) - '0');
				if (value > Integer.MAX_VALUE) {
					return failNextInt();
				}
			}
			return (int) value;
		}

		// Swallow remaining digits to resync our attempted parse, but return failure.
		private int failNextInt() {
			while (Character.isDigit(peek())) {
				advance();
			}
			return FormatToken.UNSET;
		}
	}


	private char decimalSeparator() {
		//localeData.decimalSeparator;
		return '.';
	}

	private char groupingSeparator() {
		//localeData.decimalSeparator;
		return ',';
	}

	private char minusSign() {
		//localeData.decimalSeparator;
		return '-';
	}

	private char zeroDigit() {
		//localeData.decimalSeparator;
		return '0';
	}

	private String amPm(int index) {
		//localeData.decimalSeparator;
		return (index == 0) ? "am" : "pm";
	}

	String[] longWeekdayNames = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
	String[] longMonthNames = {"january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december"};

	private String longWeekdayNames(int index) {
		return longWeekdayNames[index];
	}

	private String shortWeekdayNames(int index) {
		return longWeekdayNames(index).substring(0, 3);
	}

	private String longMonthNames(int index) {
		return longMonthNames[index];
	}

	private String shortMonthNames(int index) {
		return longMonthNames(index).substring(0, 3);
	}
}
