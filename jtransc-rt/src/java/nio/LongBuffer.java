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

public abstract class LongBuffer extends Buffer implements Comparable<LongBuffer> {
	final long[] hb;
	final int offset;
	boolean isReadOnly;

	LongBuffer(int mark, int pos, int lim, int cap, long[] hb, int offset) {
		super(mark, pos, lim, cap);
		this.hb = hb;
		this.offset = offset;
	}

	LongBuffer(int mark, int pos, int lim, int cap) { // package-private
		this(mark, pos, lim, cap, null, 0);
	}

	public static LongBuffer allocate(int capacity) {
		if (capacity < 0) throw new IllegalArgumentException();
		return new HeapLongBuffer(capacity, capacity);
	}

	public static LongBuffer wrap(long[] array, int offset, int length) {
		try {
			return new HeapLongBuffer(array, offset, length);
		} catch (IllegalArgumentException x) {
			throw new IndexOutOfBoundsException();
		}
	}

	public static LongBuffer wrap(long[] array) {
		return wrap(array, 0, array.length);
	}

	public abstract LongBuffer slice();

	public abstract LongBuffer duplicate();

	public abstract LongBuffer asReadOnlyBuffer();

	public abstract long get();

	public abstract LongBuffer put(long l);

	public abstract long get(int index);

	public abstract LongBuffer put(int index, long l);

	public LongBuffer get(long[] dst, int offset, int length) {
		checkBounds(offset, length, dst.length);
		if (length > remaining()) throw new BufferUnderflowException();
		for (int i = offset, end = offset + length; i < end; i++) dst[i] = get();
		return this;
	}

	public LongBuffer get(long[] dst) {
		return get(dst, 0, dst.length);
	}

	public LongBuffer put(LongBuffer src) {
		if (src == this) throw new IllegalArgumentException();
		if (isReadOnly()) throw new ReadOnlyBufferException();
		int n = src.remaining();
		if (n > remaining()) throw new BufferOverflowException();
		for (int i = 0; i < n; i++) put(src.get());
		return this;
	}

	public LongBuffer put(long[] src, int offset, int length) {
		checkBounds(offset, length, src.length);
		if (length > remaining()) throw new BufferOverflowException();
		for (int i = offset, end = offset + length; i < end; i++) this.put(src[i]);
		return this;
	}

	public final LongBuffer put(long[] src) {
		return put(src, 0, src.length);
	}

	public final boolean hasArray() {
		return (hb != null) && !isReadOnly;
	}

	public final long[] array() {
		check();
		return hb;
	}

	public final int arrayOffset() {
		check();
		return offset;
	}

	private void check() {
		if (hb == null) throw new UnsupportedOperationException();
		if (isReadOnly) throw new ReadOnlyBufferException();
	}

	public abstract LongBuffer compact();

	public abstract boolean isDirect();

	public String toString() {
		return getClass().getName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
	}

	public int hashCode() {
		int h = 1;
		for (int i = limit() - 1, p = position(); i >= p; i--) h = 31 * h + (int) get(i);
		return h;
	}

	public boolean equals(Object ob) {
		if (this == ob) return true;
		if (!(ob instanceof LongBuffer)) return false;
		LongBuffer that = (LongBuffer) ob;
		if (this.remaining() != that.remaining()) return false;
		int p = this.position();
		for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
			if (this.get(i) != that.get(j)) return false;
		}
		return true;
	}

	public int compareTo(LongBuffer that) {
		int n = this.position() + Math.min(this.remaining(), that.remaining());
		for (int i = this.position(), j = that.position(); i < n; i++, j++) {
			int cmp = Long.compare(this.get(i), that.get(j));
			if (cmp != 0) return cmp;
		}
		return this.remaining() - that.remaining();
	}

	public abstract ByteOrder order();
}
