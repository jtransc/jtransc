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

class HeapCharBuffer extends CharBuffer {

	HeapCharBuffer(int cap, int lim) {
		super(-1, 0, lim, cap, new char[cap], 0);
	}

	HeapCharBuffer(char[] buf, int off, int len) {
		super(-1, off, off + len, buf.length, buf, 0);
	}

	protected HeapCharBuffer(char[] buf, int mark, int pos, int lim, int cap, int off) {
		super(mark, pos, lim, cap, buf, off);
	}

	public CharBuffer slice() {
		return new HeapCharBuffer(hb, -1, 0, this.remaining(), this.remaining(), this.position() + offset);
	}

	public CharBuffer duplicate() {
		return new HeapCharBuffer(hb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
	}

	public CharBuffer asReadOnlyBuffer() {
		return this;
		//return new HeapCharBufferR(hb, this.markValue(), this.position(), this.limit(), this.capacity(), offset);
	}

	protected int ix(int i) {
		return i + offset;
	}

	public char get() {
		return hb[ix(nextGetIndex())];
	}

	public char get(int i) {
		return hb[ix(checkIndex(i))];
	}

	char getUnchecked(int i) {
		return hb[ix(i)];
	}

	public CharBuffer get(char[] dst, int offset, int length) {
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

	public CharBuffer put(char x) {
		hb[ix(nextPutIndex())] = x;
		return this;
	}

	public CharBuffer put(int i, char x) {
		hb[ix(checkIndex(i))] = x;
		return this;
	}

	public CharBuffer put(char[] src, int offset, int length) {
		checkBounds(offset, length, src.length);
		if (length > remaining()) throw new BufferOverflowException();
		System.arraycopy(src, offset, hb, ix(position()), length);
		position(position() + length);
		return this;
	}

	public CharBuffer put(CharBuffer src) {
		if (src instanceof HeapCharBuffer) {
			if (src == this) throw new IllegalArgumentException();
			HeapCharBuffer sb = (HeapCharBuffer) src;
			int n = sb.remaining();
			if (n > remaining()) throw new BufferOverflowException();
			System.arraycopy(sb.hb, sb.ix(sb.position()), hb, ix(position()), n);
			sb.position(sb.position() + n);
			position(position() + n);
		} else if (src.isDirect()) {
			int n = src.remaining();
			if (n > remaining()) throw new BufferOverflowException();
			src.get(hb, ix(position()), n);
			position(position() + n);
		} else {
			super.put(src);
		}
		return this;
	}

	public CharBuffer compact() {
		System.arraycopy(hb, ix(position()), hb, ix(0), remaining());
		position(remaining());
		limit(capacity());
		discardMark();
		return this;
	}

	String toString(int start, int end) {
		try {
			return new String(hb, start + offset, end - start);
		} catch (StringIndexOutOfBoundsException x) {
			throw new IndexOutOfBoundsException();
		}
	}

	public CharBuffer subSequence(int start, int end) {
		if ((start < 0) || (end > length()) || (start > end)) throw new IndexOutOfBoundsException();
		int pos = position();
		return new HeapCharBuffer(hb, -1, pos + start, pos + end, capacity(), offset);
	}

	public ByteOrder order() {
		return ByteOrder.nativeOrder();
	}
}
