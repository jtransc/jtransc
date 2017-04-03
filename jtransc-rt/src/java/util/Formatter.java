package java.util;

import com.jtransc.JTranscSystem;
import com.jtransc.internal.JTranscCType;

import java.io.*;
import java.nio.charset.Charset;

public class Formatter implements Closeable, Flushable {
	Appendable out;
	IOException ioException;

	public Formatter() {
		this.out = new StringBuilder();
	}

	public Formatter(Appendable a) {
		this.out = a;
	}

	public Formatter(Locale l) {
		this.out = new StringBuilder();
	}

	public Formatter(Appendable a, Locale l) {
		this.out = a;
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
		out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), csn));
	}

	public Formatter(File file, String csn, Locale l) throws FileNotFoundException, UnsupportedEncodingException {
		this(file, csn);
	}

	public Formatter(OutputStream os) {
		out = new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset()));
	}

	public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
		this(os, csn, Locale.getDefault());
	}

	public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
		out = new BufferedWriter(new OutputStreamWriter(os, csn));
	}

	public Formatter(PrintStream ps) {
		out = ps;
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
				int width = 0;
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
						width *= 10;
						width += JTranscCType.decodeDigit(cc);
					} else if (cc == 'n') { // %n == \n, \r\n, or \r
						out.append(JTranscSystem.lineSeparator());
						break;
					} else {
						out.append(formatValue(right, width, pad, cc, args[argn++]));
						break;
					}
				}
			} else {
				out.append(c);
			}
		}
	}

	private String formatValue(boolean right, int width, char pad, char c, Object value) {
		String out;
		switch (c) {
			case 'x':
			case 'X':
				if (value instanceof Long) {
					out = "" + Long.toUnsignedString((Long) value, 16);
				} else {
					out = "" + Integer.toUnsignedString((Integer) value, 16);
				}
				if (c == 'X') out = out.toUpperCase();
				break;
			default:
				out = "" + value;
				break;
		}
		if (width > 0) {
			while (out.length() < width) {
				if (right) {
					out = "" + out + pad;
				} else {
					out = "" + pad + out;
				}
			}
		}
		return out;
	}

	public String toString() {
		return out.toString();
	}

	public enum BigDecimalLayoutForm {
		SCIENTIFIC,
		DECIMAL_FLOAT
	}
}
