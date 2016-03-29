/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

// -- This file was mechanically generated: Do not edit! -- //

package java.nio;

class ByteBufferAsIntBuffer extends IntBuffer {

	private final boolean bigEndian;
	protected final ByteBuffer bb;
	protected final int offset;

	ByteBufferAsIntBuffer(ByteBuffer bb) {
		super(-1, 0, bb.remaining() >> 2, bb.remaining() >> 2);
		this.bigEndian = true;
		this.bb = bb;
		// enforce limit == capacity
		int cap = this.capacity();
		this.limit(cap);
		int pos = this.position();
		assert (pos <= cap);
		offset = pos;
	}

	ByteBufferAsIntBuffer(boolean bigEndian, ByteBuffer bb, int mark, int pos, int lim, int cap, int off) {
		super(mark, pos, lim, cap);
		this.bigEndian = bigEndian;
		this.bb = bb;
		offset = off;
	}

	public IntBuffer slice() {
		int pos = this.position();
		int lim = this.limit();
		assert (pos <= lim);
		int rem = (pos <= lim ? lim - pos : 0);
		int off = (pos << 2) + offset;
		assert (off >= 0);
		return new ByteBufferAsIntBuffer(bigEndian, bb, -1, 0, rem, rem, off);
	}

	public IntBuffer duplicate() {
		return new ByteBufferAsIntBuffer(bigEndian, bb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
	}

	public IntBuffer asReadOnlyBuffer() {
		return this;
	}

	protected int ix(int i) {
		return (i << 2) + offset;
	}

	public int get() {
		return Bits.getIntB(bb, ix(nextGetIndex()));
	}

	public int get(int i) {
		return Bits.getIntB(bb, ix(checkIndex(i)));
	}

	public IntBuffer put(int x) {
		Bits.putInt(bb, ix(nextPutIndex()), x, bigEndian);
		return this;
	}

	public IntBuffer put(int i, int x) {
		Bits.putInt(bb, ix(checkIndex(i)), x, bigEndian);
		return this;
	}

	public IntBuffer compact() {
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
