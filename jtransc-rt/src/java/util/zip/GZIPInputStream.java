package java.util.zip;

import java.io.*;

public class GZIPInputStream extends InflaterInputStream {

	protected CRC32 crc = new CRC32();

	protected boolean eos;

	private boolean closed = false;

	private void ensureOpen() throws IOException {
		if (closed) {
			throw new IOException("Stream closed");
		}
	}

	public GZIPInputStream(InputStream in, int size) throws IOException {
		super(in, new Inflater(true), size);
		usesDefaultInflater = true;
		readHeader(in);
	}

	public GZIPInputStream(InputStream in) throws IOException {
		this(in, 512);
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		ensureOpen();
		if (eos) {
			return -1;
		}
		int n = super.read(buf, off, len);
		if (n == -1) {
			if (readTrailer())
				eos = true;
			else
				return this.read(buf, off, len);
		} else {
			crc.update(buf, off, n);
		}
		return n;
	}

	public void close() throws IOException {
		if (!closed) {
			super.close();
			eos = true;
			closed = true;
		}
	}

	public final static int GZIP_MAGIC = 0x8b1f;

	private final static int FTEXT = 1;
	private final static int FHCRC = 2;
	private final static int FEXTRA = 4;
	private final static int FNAME = 8;
	private final static int FCOMMENT = 16;

	private int readHeader(InputStream this_in) throws IOException {
		CheckedInputStream in = new CheckedInputStream(this_in, crc);
		crc.reset();

		if (readUShort(in) != GZIP_MAGIC) {
			throw new ZipException("Not in GZIP format");
		}

		if (readUByte(in) != 8) {
			throw new ZipException("Unsupported compression method");
		}

		int flg = readUByte(in);

		skipBytes(in, 6);
		int n = 2 + 2 + 6;

		if ((flg & FEXTRA) == FEXTRA) {
			int m = readUShort(in);
			skipBytes(in, m);
			n += m + 2;
		}

		if ((flg & FNAME) == FNAME) {
			do {
				n++;
			} while (readUByte(in) != 0);
		}

		if ((flg & FCOMMENT) == FCOMMENT) {
			do {
				n++;
			} while (readUByte(in) != 0);
		}

		if ((flg & FHCRC) == FHCRC) {
			int v = (int) crc.getValue() & 0xffff;
			if (readUShort(in) != v) {
				throw new ZipException("Corrupt GZIP header");
			}
			n += 2;
		}
		crc.reset();
		return n;
	}

	private boolean readTrailer() throws IOException {
		InputStream in = this.in;
		int n = inf.getRemaining();
		if (n > 0) {
			in = new SequenceInputStream(
					new ByteArrayInputStream(buf, len - n, n),
					new FilterInputStream(in) {
						public void close() throws IOException {
						}
					});
		}

		if ((readUInt(in) != crc.getValue()) ||

				(readUInt(in) != (inf.getBytesWritten() & 0xffffffffL)))
			throw new ZipException("Corrupt GZIP trailer");

		if (this.in.available() > 0 || n > 26) {
			int m = 8;
			try {
				m += readHeader(in);
			} catch (IOException ze) {
				return true;
			}
			inf.reset();
			if (n > m)
				inf.setInput(buf, len - n + m, n - m);
			return false;
		}
		return true;
	}

	private long readUInt(InputStream in) throws IOException {
		long s = readUShort(in);
		return ((long) readUShort(in) << 16) | s;
	}

	private int readUShort(InputStream in) throws IOException {
		int b = readUByte(in);
		return (readUByte(in) << 8) | b;
	}

	private int readUByte(InputStream in) throws IOException {
		int b = in.read();
		if (b == -1) {
			throw new EOFException();
		}
		if (b < -1 || b > 255) {

			throw new IOException(this.in.getClass().getName()
					+ ".read() returned value out of range -1..255: " + b);
		}
		return b;
	}

	private byte[] tmpbuf = new byte[128];

	private void skipBytes(InputStream in, int n) throws IOException {
		while (n > 0) {
			int len = in.read(tmpbuf, 0, n < tmpbuf.length ? n : tmpbuf.length);
			if (len == -1) {
				throw new EOFException();
			}
			n -= len;
		}
	}
}
