package java.nio;

class ByteBufferAsFloatBuffer extends FloatBuffer {
	protected final ByteBuffer bb;
	protected final int offset;
	private boolean bigEndian;

	ByteBufferAsFloatBuffer(ByteBuffer bb) {   // package-private
		super(-1, 0, bb.remaining() >> 2, bb.remaining() >> 2);
		this.bb = bb;
		// enforce limit == capacity
		int cap = this.capacity();
		this.limit(cap);
		int pos = this.position();
		assert (pos <= cap);
		offset = pos;
	}

	ByteBufferAsFloatBuffer(boolean bigEndian, ByteBuffer bb, int mark, int pos, int lim, int cap, int off) {
		super(mark, pos, lim, cap);
		this.bigEndian = bigEndian;
		this.bb = bb;
		offset = off;
	}

	public FloatBuffer slice() {
		int pos = this.position();
		int lim = this.limit();
		assert (pos <= lim);
		int rem = (pos <= lim ? lim - pos : 0);
		int off = (pos << 2) + offset;
		assert (off >= 0);
		return new ByteBufferAsFloatBuffer(bigEndian, bb, -1, 0, rem, rem, off);
	}

	public FloatBuffer duplicate() {
		return new ByteBufferAsFloatBuffer(bigEndian, bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
	}

	public FloatBuffer asReadOnlyBuffer() {
		return this;
	}

	protected int ix(int i) {
		return (i << 2) + offset;
	}

	public float get() {
		return get(nextGetIndex());
	}

	public float get(int i) {
		return Bits.getFloat(bb, ix(checkIndex(i)), bigEndian);
	}

	public FloatBuffer put(float x) {
		return put(nextPutIndex(), x);
	}

	public FloatBuffer put(int i, float x) {
		Bits.putFloat(bb, ix(checkIndex(i)), x, bigEndian);
		return this;
	}

	public FloatBuffer compact() {
		int pos = position();
		int lim = limit();
		assert (pos <= lim);
		int rem = (pos <= lim ? lim - pos : 0);

		ByteBuffer db = bb.duplicate();
		db.limit(ix(lim));
		db.position(ix(0));
		ByteBuffer sb = db.slice();
		sb.position(pos << 2);
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
