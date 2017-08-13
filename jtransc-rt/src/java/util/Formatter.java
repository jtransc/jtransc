package java.util;

import com.jtransc.JTranscSystem;
import com.jtransc.internal.JTranscCType;
import com.jtransc.text.JTranscLocale;
import com.jtransc.util.JTranscStrings;

import java.io.*;
import java.nio.charset.Charset;

public class Formatter implements Closeable, Flushable {
	private final Appendable out;
	private IOException ioException;
	private final Locale l;

	public Formatter(Appendable a, Locale l) {
		this.out = a;
		this.l = l;
	}

	public Formatter() {
		this(new StringBuilder(), Locale.getDefault());
	}

	public Formatter(Appendable a) {
		this(a, Locale.getDefault());
	}

	public Formatter(Locale l) {
		this(new StringBuilder(), l);
	}

	public Formatter(String fileName) throws FileNotFoundException {
		this(new File(fileName));
	}

	public Formatter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(new File(fileName), csn);
	}

	public Formatter(String fileName, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
		this(new File(fileName), csn, l);
	}

	public Formatter(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}

	public Formatter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), csn)));
	}

	public Formatter(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
		this(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), csn)), l);
	}

	public Formatter(OutputStream os) {
		this(new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset())));
	}

	public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
		this(os, csn, Locale.getDefault());
	}

	public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
		this(new BufferedWriter(new OutputStreamWriter(os, csn)));
	}

	public Formatter(PrintStream ps) {
		this(ps, Locale.getDefault());
	}

	@Override
	public void close() {
		if (out instanceof Closeable) {
			try {
				((Closeable) out).close();
			} catch (Throwable t) {
			}
		}
	}

	@Override
	public void flush() {
		if (out instanceof Flushable) {
			try {
				((Flushable) out).flush();
			} catch (Throwable t) {
			}
		}
	}

	public Locale locale() {
		return Locale.getDefault();
	}

	public Appendable out() {
		return out;
	}

	public IOException ioException() {
		return ioException;
	}

	public Formatter format(String format, Object... args) {
		doFormat(format, args);
		return this;
	}

	public Formatter format(Locale l, String format, Object... args) {
		doFormat(format, args);
		return this;
	}

	private void doFormat(String format, Object... args) {
		try {
			doFormat0(format, args);
		} catch (IOException t) {
			ioException = t;
		}
	}

	private void doFormat0(String format, Object... args) throws IOException {
		int n = 0;
		int argn = 0;
		int len = format.length();
		while (n < len) {
			char c = format.charAt(n++);
			if (c == '%') {
				char pad = ' ';
				int step = 0;
				boolean right = false;
				boolean readingDecimals = false;
				int width = 0;
				int decimalWidth = 0;
				while (true) {
					char cc = format.charAt(n++);
					if (cc == '%') {
						out.append('%');
						break;
					} else if (cc == '-') {
						right = true;
					} else if (step == 0 && cc == '0') {
						pad = '0';
						step = 1;
					} else if (cc >= '0' && cc <= '9') {
						if (readingDecimals) {
							decimalWidth *= 10;
							decimalWidth += JTranscCType.decodeDigit(cc);
						} else {
							width *= 10;
							width += JTranscCType.decodeDigit(cc);
						}
					} else if (cc == '.') {
						readingDecimals = true;
					} else if (cc == 'n') { // %n == \n, \r\n, or \r
						out.append(JTranscSystem.lineSeparator());
						break;
					} else {
						out.append(formatValue(right, width, decimalWidth, pad, cc, args[argn++]));
						break;
					}
				}
			} else {
				out.append(c);
			}
		}
	}

	private String doNormalPad(String str, boolean right, int width, char pad) {
		if (width <= 0) return str;
		while (str.length() < width) {
			if (right) {
				str = "" + str + pad;
			} else {
				str = "" + pad + str;
			}
		}
		return str;
	}

	private String doDecimalPad(String str, int width) {
		if (str.length() > width) {
			return str.substring(0, width);
		} else {
			StringBuilder out = new StringBuilder(str);
			while (out.length() < width) out.append('0');
			return out.toString();
		}
	}

	private String formatValue(boolean right, int width, int decimalWidth, char pad, char c, Object value) {
		switch (c) {
			case 'x':
			case 'X': {
				String out;
				if (value instanceof Long) {
					out = "" + Long.toUnsignedString((Long) value, 16);
				} else {
					out = "" + Integer.toUnsignedString((Integer) value, 16);
				}
				if (c == 'X') out = out.toUpperCase();
				return doNormalPad(out, right, width, pad);
			}
			case 'f': {
				final double v = ((Number) value).doubleValue();
				String[] parts = JTranscStrings.split(String.valueOf(v), '.');
				if (parts.length <= 0) {
					return "";
				} else if (parts.length <= 1) {
					return parts[0];
				} else {
					String integral = doNormalPad(parts[0], right, width, pad);
					String decimal = doDecimalPad(parts[1], decimalWidth);
					if (decimal.length() == 0) {
						return integral;
					} else {
						return integral + JTranscLocale.getDecimalSeparator(l) + decimal;
					}
				}
			}
			default:
				return doNormalPad(String.valueOf(value), right, width, pad);
		}
	}

	public String toString() {
		return out.toString();
	}

	public enum BigDecimalLayoutForm {
		SCIENTIFIC,
		DECIMAL_FLOAT
	}
}
