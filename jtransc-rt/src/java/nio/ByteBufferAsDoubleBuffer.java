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

import libcore.io.Memory;

import java.nio.internal.ByteBufferAs;
import java.nio.internal.SizeOf;

abstract class ByteBufferAsDoubleBuffer extends DoubleBuffer implements ByteBufferAs {
	final ByteBuffer byteBuffer;
	final byte[] bytes;

	static DoubleBuffer asDoubleBuffer(ByteBuffer byteBuffer) {
		ByteBuffer slice = byteBuffer.slice();
		slice.order(byteBuffer.order());
		return create(slice, byteBuffer.isLittleEndian);
	}

	static private ByteBufferAsDoubleBuffer create(ByteBuffer byteBuffer, boolean isLittleEndian) {
		return isLittleEndian ? new ByteBufferAsDoubleBuffer.LE(byteBuffer) : new ByteBufferAsDoubleBuffer.BE(byteBuffer);
	}

	private ByteBufferAsDoubleBuffer createWithSameOrder(ByteBuffer byteBuffer) {
		return create(byteBuffer, order() == ByteOrder.LITTLE_ENDIAN);
	}

	private ByteBufferAsDoubleBuffer(ByteBuffer byteBuffer) {
		super(byteBuffer.capacity() / SizeOf.DOUBLE);
		this.byteBuffer = byteBuffer;
		this.byteBuffer.clear();
		this.bytes = byteBuffer.array();
		init(byteBuffer.array());
	}

	private void init(byte[] data) {
	}

	@Override
	public DoubleBuffer asReadOnlyBuffer() {
		ByteBufferAsDoubleBuffer buf = (ByteBufferAsDoubleBuffer) byteBuffer.asReadOnlyBuffer().asDoubleBuffer();
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		buf.byteBuffer.order = byteBuffer.order;
		return buf;
	}

	@Override
	public DoubleBuffer compact() {
		if (byteBuffer.isReadOnly()) {
			throw new ReadOnlyBufferException();
		}
		byteBuffer.limit(limit * SizeOf.DOUBLE);
		byteBuffer.position(position * SizeOf.DOUBLE);
		byteBuffer.compact();
		byteBuffer.clear();
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public DoubleBuffer duplicate() {
		ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
		ByteBufferAsDoubleBuffer buf = createWithSameOrder(bb);
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public double get() {
		if (position == limit) throw new BufferUnderflowException();
		return byteBuffer.getDouble(position++ * SizeOf.DOUBLE);
	}

	@Override
	public boolean isDirect() {
		return byteBuffer.isDirect();
	}

	@Override
	public boolean isReadOnly() {
		return byteBuffer.isReadOnly();
	}

	@Override
	public ByteOrder order() {
		return byteBuffer.order();
	}

	@Override
	double[] protectedArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	int protectedArrayOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	boolean protectedHasArray() {
		return false;
	}

	@Override
	public DoubleBuffer put(double c) {
		if (position == limit) throw new BufferOverflowException();
		byteBuffer.putDouble(position++ * SizeOf.DOUBLE, c);
		return this;
	}

	@Override
	public DoubleBuffer slice() {
		byteBuffer.limit(limit * SizeOf.DOUBLE);
		byteBuffer.position(position * SizeOf.DOUBLE);
		ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
		DoubleBuffer result = createWithSameOrder(bb);
		byteBuffer.clear();
		return result;
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	final static public class LE extends ByteBufferAsDoubleBuffer {
		LE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public double get(int index) {
			return Memory.peekAlignedDoubleLE(bytes, index);
		}

		@Override
		public DoubleBuffer put(int index, double c) {
			Memory.pokeAlignedDoubleLE(bytes, index, c);
			return this;
		}
	}

	final static public class BE extends ByteBufferAsDoubleBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public double get(int index) {
			return Memory.peekAlignedDoubleBE(bytes, index);
		}

		@Override
		public DoubleBuffer put(int index, double c) {
			Memory.pokeAlignedDoubleBE(bytes, index, c);
			return this;
		}
	}
}
