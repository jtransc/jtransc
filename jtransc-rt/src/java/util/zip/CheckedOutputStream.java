package java.util.zip;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CheckedOutputStream extends FilterOutputStream {
	private Checksum cksum;

	public CheckedOutputStream(OutputStream out, Checksum cksum) {
		super(out);
		this.cksum = cksum;
	}

	public void write(int b) throws IOException {
		out.write(b);
		cksum.update(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		cksum.update(b, off, len);
	}

	public Checksum getChecksum() {
		return cksum;
	}
}
