
package java.nio;

class ByteBufferAsShortBuffer extends ShortBuffer {
	private final boolean bigEndian;
	protected final ByteBuffer bb;
	protected final int offset;

	ByteBufferAsShortBuffer(ByteBuffer bb) {
		super(-1, 0, bb.remaining() >> 1, bb.remaining() >> 1);
		this.bb = bb;
		this.bigEndian = true;

		int cap = this.capacity();
		this.limit(cap);
		int pos = this.position();
		assert (pos <= cap);
		offset = pos;

	}

	ByteBufferAsShortBuffer(boolean bigEndian, ByteBuffer bb, int mark, int pos, int lim, int cap, int off) {
		super(mark, pos, lim, cap);
		this.bigEndian = bigEndian;
		this.bb = bb;
		offset = off;
	}

	public ShortBuffer slice() {
		int pos = this.position();
		int lim = this.limit();
		assert (pos <= lim);
		int rem = (pos <= lim ? lim - pos : 0);
		int off = (pos << 1) + offset;
		assert (off >= 0);
		return new ByteBufferAsShortBuffer(bigEndian, bb, -1, 0, rem, rem, off);
	}

	public ShortBuffer duplicate() {
		return new ByteBufferAsShortBuffer(bigEndian, bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
	}

	public ShortBuffer asReadOnlyBuffer() {
		return this;
	}

	protected int ix(int i) {
		return (i << 1) + offset;
	}

	public short get() {
		return Bits.getShort(bb, ix(nextGetIndex()), bigEndian);
	}

	public short get(int i) {
		return Bits.getShort(bb, ix(checkIndex(i)), bigEndian);
	}

	public ShortBuffer put(short x) {
		Bits.putShort(bb, ix(nextPutIndex()), x, bigEndian);
		return this;
	}

	public ShortBuffer put(int i, short x) {
		Bits.putShort(bb, ix(checkIndex(i)), x, bigEndian);
		return this;
	}

	public ShortBuffer compact() {
		int pos = position();
		int lim = limit();
		assert (pos <= lim);
		int rem = (pos <= lim ? lim - pos : 0);
		ByteBuffer db = bb.duplicate();
		db.limit(ix(lim));
		db.position(ix(0));
		ByteBuffer sb = db.slice();
		sb.position(pos << 1);
		sb.compact();
		position(rem);
		limit(capacity());
		discardMark();
		return this;
	}

	public boolean isDirect() {
		return bb.isDirect();
	}

	public boolean isReadOnly() {
		return false;
	}

	public ByteOrder order() {
		return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}
}