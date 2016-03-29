/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

class HeapByteBuffer extends ByteBuffer {

	HeapByteBuffer(int cap, int lim) {
		super(-1, 0, lim, cap, new byte[cap], 0);
	}

	HeapByteBuffer(byte[] buf, int off, int len) {
		super(-1, off, off + len, buf.length, buf, 0);
	}

	protected HeapByteBuffer(byte[] buf, int mark, int pos, int lim, int cap, int off) {
		super(mark, pos, lim, cap, buf, off);
	}

	public ByteBuffer slice() {
		return new HeapByteBuffer(hb, -1, 0, this.remaining(), this.remaining(), this.position() + offset);
	}

	public ByteBuffer duplicate() {
		return new HeapByteBuffer(hb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
	}

	public ByteBuffer asReadOnlyBuffer() {
		return this;
	}

	protected int ix(int i) {
		return i + offset;
	}

	public byte get() {
		return hb[ix(nextGetIndex())];
	}

	public byte get(int i) {
		return hb[ix(checkIndex(i))];
	}

	public ByteBuffer get(byte[] dst, int offset, int length) {
		checkBounds(offset, length, dst.length);
		if (length > remaining()) throw new BufferUnderflowException();
		System.arraycopy(hb, ix(position()), dst, offset, length);
		position(position() + length);
		return this;
	}

	public boolean isDirect() {
		return false;
	}

	public boolean isReadOnly() {
		return false;
	}

	public ByteBuffer put(byte x) {
		hb[ix(nextPutIndex())] = x;
		return this;
	}

	public ByteBuffer put(int i, byte x) {
		hb[ix(checkIndex(i))] = x;
		return this;
	}

	public ByteBuffer put(byte[] src, int offset, int length) {
		checkBounds(offset, length, src.length);
		if (length > remaining()) throw new BufferOverflowException();
		System.arraycopy(src, offset, hb, ix(position()), length);
		position(position() + length);
		return this;
	}

	public ByteBuffer put(ByteBuffer src) {
		if (src instanceof HeapByteBuffer) {
			if (src == this) throw new IllegalArgumentException();
			HeapByteBuffer sb = (HeapByteBuffer) src;
			int n = sb.remaining();
			if (n > remaining()) throw new BufferOverflowException();
			System.arraycopy(sb.hb, sb.ix(sb.position()), hb, ix(position()), n);
			sb.position(sb.position() + n);
			position(position() + n);
		} else if (src.isDirect()) {
			int n = src.remaining();
			if (n > remaining())
				throw new BufferOverflowException();
			src.get(hb, ix(position()), n);
			position(position() + n);
		} else {
			super.put(src);
		}
		return this;
	}

	public ByteBuffer compact() {
		System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
		position(remaining());
		limit(capacity());
		discardMark();
		return this;
	}

	byte _get(int i) {                          // package-private
		return hb[i];
	}

	void _put(int i, byte b) {                  // package-private
		hb[i] = b;
	}

	public char getChar() {
		return Bits.getChar(this, ix(nextGetIndex(2)), bigEndian);
	}

	public char getChar(int i) {
		return Bits.getChar(this, ix(checkIndex(i, 2)), bigEndian);
	}

	public ByteBuffer putChar(char x) {
		Bits.putChar(this, ix(nextPutIndex(2)), x, bigEndian);
		return this;
	}

	public ByteBuffer putChar(int i, char x) {
		Bits.putChar(this, ix(checkIndex(i, 2)), x, bigEndian);
		return this;
	}

	native public CharBuffer asCharBuffer();

	public short getShort() {
		return Bits.getShort(this, ix(nextGetIndex(2)), bigEndian);
	}

	public short getShort(int i) {
		return Bits.getShort(this, ix(checkIndex(i, 2)), bigEndian);
	}

	public ByteBuffer putShort(short x) {
		Bits.putShort(this, ix(nextPutIndex(2)), x, bigEndian);
		return this;
	}

	public ByteBuffer putShort(int i, short x) {
		Bits.putShort(this, ix(checkIndex(i, 2)), x, bigEndian);
		return this;
	}

	public ShortBuffer asShortBuffer() {
		int size = this.remaining() >> 1;
		int off = offset + position();
		return new ByteBufferAsShortBuffer(bigEndian, this, -1, 0, size, size, off);
	}

	public int getInt() {
		return Bits.getInt(this, ix(nextGetIndex(4)), bigEndian);
	}

	public int getInt(int i) {
		return Bits.getInt(this, ix(checkIndex(i, 4)), bigEndian);
	}

	public ByteBuffer putInt(int x) {
		Bits.putInt(this, ix(nextPutIndex(4)), x, bigEndian);
		return this;
	}

	public ByteBuffer putInt(int i, int x) {
		Bits.putInt(this, ix(checkIndex(i, 4)), x, bigEndian);
		return this;
	}

	public IntBuffer asIntBuffer() {
		int size = this.remaining() >> 2;
		int off = offset + position();
		return new ByteBufferAsIntBuffer(bigEndian, this, -1, 0, size, size, off);
	}

	public long getLong() {
		return Bits.getLong(this, ix(nextGetIndex(8)), bigEndian);
	}

	public long getLong(int i) {
		return Bits.getLong(this, ix(checkIndex(i, 8)), bigEndian);
	}

	public ByteBuffer putLong(long x) {
		Bits.putLong(this, ix(nextPutIndex(8)), x, bigEndian);
		return this;
	}

	public ByteBuffer putLong(int i, long x) {
		Bits.putLong(this, ix(checkIndex(i, 8)), x, bigEndian);
		return this;
	}

	native public LongBuffer asLongBuffer();

	public float getFloat() {
		return Bits.getFloat(this, ix(nextGetIndex(4)), bigEndian);
	}

	public float getFloat(int i) {
		return Bits.getFloat(this, ix(checkIndex(i, 4)), bigEndian);
	}

	public ByteBuffer putFloat(float x) {
		Bits.putFloat(this, ix(nextPutIndex(4)), x, bigEndian);
		return this;
	}

	public ByteBuffer putFloat(int i, float x) {
		Bits.putFloat(this, ix(checkIndex(i, 4)), x, bigEndian);
		return this;
	}

	public FloatBuffer asFloatBuffer() {
		int size = this.remaining() >> 2;
		int off = offset + position();
		return new ByteBufferAsFloatBuffer(bigEndian, this, -1, 0, size, size, off);
	}

	public double getDouble() {
		return Bits.getDouble(this, ix(nextGetIndex(8)), bigEndian);
	}

	public double getDouble(int i) {
		return Bits.getDouble(this, ix(checkIndex(i, 8)), bigEndian);
	}

	public ByteBuffer putDouble(double x) {
		Bits.putDouble(this, ix(nextPutIndex(8)), x, bigEndian);
		return this;
	}

	public ByteBuffer putDouble(int i, double x) {
		Bits.putDouble(this, ix(checkIndex(i, 8)), x, bigEndian);
		return this;
	}

	native public DoubleBuffer asDoubleBuffer();
}
