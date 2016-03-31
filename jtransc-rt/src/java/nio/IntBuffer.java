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

public abstract class IntBuffer extends Buffer implements Comparable<IntBuffer> {
	final int[] hb;
	final int offset;
	boolean isReadOnly;

	IntBuffer(int mark, int pos, int lim, int cap, int[] hb, int offset) {
		super(mark, pos, lim, cap);
		this.hb = hb;
		this.offset = offset;
	}

	IntBuffer(int mark, int pos, int lim, int cap) { // package-private
		this(mark, pos, lim, cap, null, 0);
	}

	native public static IntBuffer allocate(int capacity);
	native public static IntBuffer wrap(int[] array, int offset, int length);

	/*
	public static IntBuffer allocate(int capacity) {
		if (capacity < 0) throw new IllegalArgumentException();
		return new HeapIntBuffer(capacity, capacity);
	}

	public static IntBuffer wrap(int[] array, int offset, int length) {
		try {
			return new HeapIntBuffer(array, offset, length);
		} catch (IllegalArgumentException x) {
			throw new IndexOutOfBoundsException();
		}
	}
	*/

	public static IntBuffer wrap(int[] array) {
		return wrap(array, 0, array.length);
	}

	public abstract IntBuffer slice();

	public abstract IntBuffer duplicate();

	public abstract IntBuffer asReadOnlyBuffer();

	public abstract int get();

	public abstract IntBuffer put(int i);

	public abstract int get(int index);

	public abstract IntBuffer put(int index, int i);

	public IntBuffer get(int[] dst, int offset, int length) {
		checkBounds(offset, length, dst.length);
		if (length > remaining()) throw new BufferUnderflowException();
		for (int i = offset, end = offset + length; i < end; i++) dst[i] = get();
		return this;
	}

	public IntBuffer get(int[] dst) {
		return get(dst, 0, dst.length);
	}

	public IntBuffer put(IntBuffer src) {
		if (src == this) throw new IllegalArgumentException();
		if (isReadOnly()) throw new ReadOnlyBufferException();
		int n = src.remaining();
		if (n > remaining()) throw new BufferOverflowException();
		for (int i = 0; i < n; i++) put(src.get());
		return this;
	}

	public IntBuffer put(int[] src, int offset, int length) {
		checkBounds(offset, length, src.length);
		if (length > remaining()) throw new BufferOverflowException();
		for (int i = offset, end = offset + length; i < end; i++) this.put(src[i]);
		return this;
	}

	public final IntBuffer put(int[] src) {
		return put(src, 0, src.length);
	}

	public final boolean hasArray() {
		return (hb != null) && !isReadOnly;
	}

	public final int[] array() {
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

	public abstract IntBuffer compact();

	public abstract boolean isDirect();

	public String toString() {
		return getClass().getName() + "[pos=" + position() + " lim=" + limit() + " cap=" + capacity() + "]";
	}

	public int hashCode() {
		int h = 1, p = position();
		for (int i = limit() - 1; i >= p; i--) h = 31 * h + get(i);
		return h;
	}

	public boolean equals(Object ob) {
		if (this == ob) return true;
		if (!(ob instanceof IntBuffer)) return false;
		return compareTo((IntBuffer) ob) == 0;
	}

	public int compareTo(IntBuffer that) {
		int n = this.position() + Math.min(this.remaining(), that.remaining());
		for (int i = this.position(), j = that.position(); i < n; i++, j++) {
			int cmp = Integer.compare(this.get(i), that.get(j));
			if (cmp != 0) return cmp;
		}
		return this.remaining() - that.remaining();
	}

	public abstract ByteOrder order();
}
