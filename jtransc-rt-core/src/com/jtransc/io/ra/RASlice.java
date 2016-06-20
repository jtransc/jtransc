package com.jtransc.io.ra;

public class RASlice extends RAStream {
	private final RAStream parent;
	private final long start;
	private final long end;

	public RASlice(RAStream parent, long start, long end) {
		this.parent = parent;
		this.start = start;
		this.end = end;
	}

	@Override
	public void setLength(long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLength() {
		return end - start;
	}

	@Override
	protected int read(long position, byte[] ref, int pos, int len) {
		long spos = this.start + position;
		long epos = Math.min(this.end, spos + len);
		int finalLen = (int)(epos - spos);
		return parent.read(spos, ref, pos, finalLen);
	}

	@Override
	protected void write(long position, byte[] ref, int pos, int len) {
		long spos = this.start + position;
		long epos = Math.min(this.end, spos + len);
		int finalLen = (int)(epos - spos);
		parent.write(spos, ref, pos, finalLen);
	}
}