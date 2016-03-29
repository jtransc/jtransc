package java.util.zip;

public class Inflater {
	private final ZStreamRef zsRef;
	private byte[] buf = defaultBuf;
	private int off, len;
	private boolean finished;
	private boolean needDict;
	private long bytesRead;
	private long bytesWritten;

	private static final byte[] defaultBuf = new byte[0];

	public Inflater(boolean nowrap) {
		zsRef = new ZStreamRef(init(nowrap));
	}

	public Inflater() {
		this(false);
	}

	public void setInput(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		synchronized (zsRef) {
			this.buf = b;
			this.off = off;
			this.len = len;
		}
	}

	public void setInput(byte[] b) {
		setInput(b, 0, b.length);
	}

	public void setDictionary(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		synchronized (zsRef) {
			ensureOpen();
			setDictionary(zsRef.address(), b, off, len);
			needDict = false;
		}
	}

	public void setDictionary(byte[] b) {
		setDictionary(b, 0, b.length);
	}

	public int getRemaining() {
		synchronized (zsRef) {
			return len;
		}
	}

	public boolean needsInput() {
		synchronized (zsRef) {
			return len <= 0;
		}
	}

	public boolean needsDictionary() {
		synchronized (zsRef) {
			return needDict;
		}
	}

	public boolean finished() {
		synchronized (zsRef) {
			return finished;
		}
	}

	public int inflate(byte[] b, int off, int len)
			throws DataFormatException {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		synchronized (zsRef) {
			ensureOpen();
			int thisLen = this.len;
			int n = inflateBytes(zsRef.address(), b, off, len);
			bytesWritten += n;
			bytesRead += (thisLen - this.len);
			return n;
		}
	}

	public int inflate(byte[] b) throws DataFormatException {
		return inflate(b, 0, b.length);
	}

	public int getAdler() {
		synchronized (zsRef) {
			ensureOpen();
			return getAdler(zsRef.address());
		}
	}

	public int getTotalIn() {
		return (int) getBytesRead();
	}

	public long getBytesRead() {
		synchronized (zsRef) {
			ensureOpen();
			return bytesRead;
		}
	}

	public int getTotalOut() {
		return (int) getBytesWritten();
	}

	public long getBytesWritten() {
		synchronized (zsRef) {
			ensureOpen();
			return bytesWritten;
		}
	}

	public void reset() {
		synchronized (zsRef) {
			ensureOpen();
			reset(zsRef.address());
			buf = defaultBuf;
			finished = false;
			needDict = false;
			off = len = 0;
			bytesRead = bytesWritten = 0;
		}
	}

	public void end() {
		synchronized (zsRef) {
			long addr = zsRef.address();
			zsRef.clear();
			if (addr != 0) {
				end(addr);
				buf = null;
			}
		}
	}

	protected void finalize() {
		end();
	}

	private void ensureOpen() {
		assert Thread.holdsLock(zsRef);
		if (zsRef.address() == 0)
			throw new NullPointerException("Inflater has been closed");
	}

	boolean ended() {
		synchronized (zsRef) {
			return zsRef.address() == 0;
		}
	}

	private native static long init(boolean nowrap);

	private native static void setDictionary(long addr, byte[] b, int off, int len);

	private native int inflateBytes(long addr, byte[] b, int off, int len) throws DataFormatException;

	private native static int getAdler(long addr);

	private native static void reset(long addr);

	private native static void end(long addr);
}
