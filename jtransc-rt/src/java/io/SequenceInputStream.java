package java.io;

import java.util.Enumeration;
import java.util.Objects;
import java.util.Vector;

public class SequenceInputStream extends InputStream {
	Enumeration<? extends InputStream> e;
	InputStream in;

	public SequenceInputStream(Enumeration<? extends InputStream> e) {
		this.e = e;
		try {
			nextStream();
		} catch (IOException ex) {
			throw new Error("panic");
		}
	}

	public SequenceInputStream(InputStream s1, InputStream s2) {
		Vector<InputStream> v = new Vector<>(2);

		v.addElement(s1);
		v.addElement(s2);
		e = v.elements();
		try {
			nextStream();
		} catch (IOException ex) {
			throw new Error("panic");
		}
	}

	final void nextStream() throws IOException {
		if (in != null) in.close();

		if (e.hasMoreElements()) {
			in = (InputStream) e.nextElement();
			Objects.requireNonNull(in);
		} else {
			in = null;
		}

	}

	public int available() throws IOException {
		return (in != null) ? in.available() : 0;
	}

	public int read() throws IOException {
		if (in == null) return -1;
		int c = in.read();
		if (c == -1) {
			nextStream();
			return read();
		}
		return c;
	}

	public int read(byte b[], int off, int len) throws IOException {
		if (in == null) return -1;
		Objects.requireNonNull(b);
		if (off < 0 || len < 0 || len > b.length - off) throw new IndexOutOfBoundsException();
		if (len == 0) return 0;

		int n = in.read(b, off, len);
		if (n <= 0) {
			nextStream();
			return read(b, off, len);
		}
		return n;
	}

	public void close() throws IOException {
		do {
			nextStream();
		} while (in != null);
	}
}
