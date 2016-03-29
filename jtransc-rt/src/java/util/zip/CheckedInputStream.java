package java.util.zip;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CheckedInputStream extends FilterInputStream {
	private Checksum cksum;

	public CheckedInputStream(InputStream in, Checksum cksum) {
		super(in);
		this.cksum = cksum;
	}

	public int read() throws IOException {
		int b = in.read();
		if (b != -1) cksum.update(b);
		return b;
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		len = in.read(buf, off, len);
		if (len != -1) cksum.update(buf, off, len);
		return len;
	}

	public long skip(long n) throws IOException {
		byte[] buf = new byte[512];
		long total = 0;
		while (total < n) {
			long len = n - total;
			len = read(buf, 0, len < buf.length ? (int) len : buf.length);
			if (len == -1) return total;
			total += len;
		}
		return total;
	}

	public Checksum getChecksum() {
		return cksum;
	}
}