/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

import java.nio.internal.MemoryBlock;

public abstract class Buffer {
	static final int UNSET_MARK = -1;
	final int capacity;
	int limit;
	int mark = UNSET_MARK;
	int position = 0;
	final int _elementSizeShift;
	final MemoryBlock block;

	Buffer(int elementSizeShift, int capacity, MemoryBlock block) {
		this._elementSizeShift = elementSizeShift;
		if (capacity < 0) throw new IllegalArgumentException("capacity < 0: " + capacity);
		this.capacity = this.limit = capacity;
		this.block = block;
	}

	public abstract Object array();

	public abstract int arrayOffset();

	public final int capacity() {
		return capacity;
	}

	void checkIndex(int index) {
		if (index < 0 || index >= limit) throw new IndexOutOfBoundsException("index=" + index + ", limit=" + limit);
	}

	void checkIndex(int index, int sizeOfType) {
		if (index < 0 || index > limit - sizeOfType)
			throw new IndexOutOfBoundsException("index=" + index + ", limit=" + limit +
				", size of type=" + sizeOfType);
	}

	int checkGetBounds(int bytesPerElement, int length, int offset, int count) {
		int byteCount = bytesPerElement * count;
		if ((offset | count) < 0 || offset > length || length - offset < count)
			throw new IndexOutOfBoundsException("offset=" + offset +
				", count=" + count + ", length=" + length);
		if (byteCount > remaining()) throw new BufferUnderflowException();
		return byteCount;
	}

	int checkPutBounds(int bytesPerElement, int length, int offset, int count) {
		int byteCount = bytesPerElement * count;
		if ((offset | count) < 0 || offset > length || length - offset < count)
			throw new IndexOutOfBoundsException("offset=" + offset +
				", count=" + count + ", length=" + length);
		if (byteCount > remaining()) throw new BufferOverflowException();
		if (isReadOnly()) throw new ReadOnlyBufferException();
		return byteCount;
	}

	void checkStartEndRemaining(int start, int end) {
		if (end < start || start < 0 || end > remaining())
			throw new IndexOutOfBoundsException("start=" + start + ", end=" + end +
				", remaining()=" + remaining());
	}

	public final Buffer clear() {
		position = 0;
		mark = UNSET_MARK;
		limit = capacity;
		return this;
	}

	public final Buffer flip() {
		limit = position;
		position = 0;
		mark = UNSET_MARK;
		return this;
	}

	public abstract boolean hasArray();

	public final boolean hasRemaining() {
		return position < limit;
	}

	public abstract boolean isDirect();

	public abstract boolean isReadOnly();

	final void checkWritable() {
		if (isReadOnly()) throw new IllegalArgumentException("Read-only buffer");
	}

	public final int limit() {
		return limit;
	}

	public final Buffer limit(int newLimit) {
		if (newLimit < 0 || newLimit > capacity)
			throw new IllegalArgumentException("Bad limit (capacity " + capacity + "): " + newLimit);

		limit = newLimit;
		if (position > newLimit) position = newLimit;
		if ((mark != UNSET_MARK) && (mark > newLimit)) mark = UNSET_MARK;
		return this;
	}

	public final Buffer mark() {
		mark = position;
		return this;
	}

	public final int position() {
		return position;
	}

	public final Buffer position(int newPosition) {
		positionImpl(newPosition);
		return this;
	}

	void positionImpl(int newPosition) {
		if (newPosition < 0 || newPosition > limit)
			throw new IllegalArgumentException("Bad position (limit " + limit + "): " + newPosition);

		position = newPosition;
		if ((mark != UNSET_MARK) && (mark > position)) mark = UNSET_MARK;
	}

	public final int remaining() {
		return limit - position;
	}

	public final Buffer reset() {
		if (mark == UNSET_MARK) throw new InvalidMarkException();
		position = mark;
		return this;
	}

	public final Buffer rewind() {
		position = 0;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[position=" + position + ",limit=" + limit + ",capacity=" + capacity + "]";
	}
}
