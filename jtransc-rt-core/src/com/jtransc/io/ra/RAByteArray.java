package com.jtransc.io.ra;

public class RAByteArray extends RAStream {
	byte[] data;

	public RAByteArray(byte[] data) {
		this.data = data;
	}

	@Override
	public void setLength(long length) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLength() {
		return data.length;
	}

	@Override
	protected int read(long position, byte[] ref, int pos, int len) {
		long epos = Math.min(data.length, position + len);
		int finalLen = (int)(epos - position);
		System.arraycopy(data, (int) position, ref, pos, finalLen);
		return finalLen;
	}

	@Override
	protected void write(long position, byte[] ref, int pos, int len) {
		throw new UnsupportedOperationException();
	}
}
