/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.nio;

import com.jtransc.JTranscBits;
import java.nio.internal.Memory;
import java.nio.internal.SizeOf;

/**
 * ByteArrayBuffer implements byte[]-backed ByteBuffers.
 */
final class ByteArrayBuffer extends ByteBuffer {

	/**
	 * These fields are non-private for NioUtils.unsafeArray.
	 */
	final byte[] backingArray;
	final int arrayOffset;

	private final boolean isReadOnly;
	private boolean isDirect;

	ByteArrayBuffer(byte[] backingArray) {
		this(backingArray.length, backingArray, 0, false);
		this.isDirect = false;
	}

	ByteArrayBuffer(byte[] backingArray, boolean isDirect) {
		this(backingArray.length, backingArray, 0, false);
		this.isDirect = isDirect;
	}

	private ByteArrayBuffer(int capacity, byte[] backingArray, int arrayOffset, boolean isReadOnly) {
		super(capacity, null);
		this.backingArray = backingArray;
		this.arrayOffset = arrayOffset;
		this.isReadOnly = isReadOnly;
		if (arrayOffset + capacity > backingArray.length) {
			throw new IndexOutOfBoundsException("backingArray.length=" + backingArray.length +
				", capacity=" + capacity + ", arrayOffset=" + arrayOffset);
		}
	}

	private static ByteArrayBuffer copy(ByteArrayBuffer other, int markOfOther, boolean isReadOnly) {
		ByteArrayBuffer buf = new ByteArrayBuffer(other.capacity(), other.backingArray, other.arrayOffset, isReadOnly);
		buf.limit = other.limit;
		buf.position = other.position();
		buf.mark = markOfOther;
		return buf;
	}

	@Override
	public ByteBuffer asReadOnlyBuffer() {
		return copy(this, mark, true);
	}

	@Override
	public ByteBuffer compact() {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		System.arraycopy(backingArray, position + arrayOffset, backingArray, arrayOffset, remaining());
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public ByteBuffer duplicate() {
		return copy(this, mark, isReadOnly);
	}

	@Override
	public ByteBuffer slice() {
		return new ByteArrayBuffer(remaining(), backingArray, arrayOffset + position, isReadOnly);
	}

	@Override
	public boolean isReadOnly() {
		return isReadOnly;
	}

	private void _checkWritable() {
		if (isReadOnly) throw new ReadOnlyBufferException();
	}

	@Override
	byte[] protectedArray() {
		_checkWritable();
		return backingArray;
	}

	@Override
	int protectedArrayOffset() {
		_checkWritable();
		return arrayOffset;
	}

	@Override
	boolean protectedHasArray() {
		return !isReadOnly;
	}

	@Override
	public final ByteBuffer get(byte[] dst, int dstOffset, int byteCount) {
		checkGetBounds(1, dst.length, dstOffset, byteCount);
		System.arraycopy(backingArray, arrayOffset + position, dst, dstOffset, byteCount);
		position += byteCount;
		return this;
	}

	final void get(char[] dst, int dstOffset, int charCount) {
		int byteCount = checkGetBounds(SizeOf.CHAR, dst.length, dstOffset, charCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.CHAR, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(double[] dst, int dstOffset, int doubleCount) {
		int byteCount = checkGetBounds(SizeOf.DOUBLE, dst.length, dstOffset, doubleCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.DOUBLE, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(float[] dst, int dstOffset, int floatCount) {
		int byteCount = checkGetBounds(SizeOf.FLOAT, dst.length, dstOffset, floatCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.FLOAT, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(int[] dst, int dstOffset, int intCount) {
		int byteCount = checkGetBounds(SizeOf.INT, dst.length, dstOffset, intCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.INT, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	final void get(long[] dst, int dstOffset, int longCount) {
		int byteCount = checkGetBounds(SizeOf.LONG, dst.length, dstOffset, longCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.LONG, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");

	}

	final void get(short[] dst, int dstOffset, int shortCount) {
		int byteCount = checkGetBounds(SizeOf.SHORT, dst.length, dstOffset, shortCount);
		//Memory.unsafeBulkGet(dst, dstOffset, byteCount, backingArray, arrayOffset + position, SizeOf.SHORT, order.needsSwap);
		position += byteCount;
		throw new RuntimeException("Not implemented");
	}

	@Override
	public final byte get() {
		if (position == limit) {
			throw new BufferUnderflowException();
		}
		return backingArray[arrayOffset + position++];
	}

	@Override
	public final byte get(int index) {
		checkIndex(index);
		return backingArray[arrayOffset + index];
	}

	@Override
	public final char getChar() {
		int newPosition = position + SizeOf.CHAR;
		if (newPosition > limit) {
			throw new BufferUnderflowException();
		}
		char result = (char) Memory.peekShort(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	@Override
	public final char getChar(int index) {
		checkIndex(index, SizeOf.CHAR);
		return (char) Memory.peekShort(backingArray, arrayOffset + index, order);
	}

	@Override
	public final double getDouble() {
		return Double.longBitsToDouble(getLong());
	}

	@Override
	public final double getDouble(int index) {
		return Double.longBitsToDouble(getLong(index));
	}

	@Override
	public final float getFloat() {
		return Float.intBitsToFloat(getInt());
	}

	@Override
	public final float getFloat(int index) {
		return Float.intBitsToFloat(getInt(index));
	}

	@Override
	public final int getInt() {
		int newPosition = position + SizeOf.INT;
		if (newPosition > limit) {
			throw new BufferUnderflowException();
		}
		int result = Memory.peekInt(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	@Override
	public final int getInt(int index) {
		checkIndex(index, SizeOf.INT);
		return Memory.peekInt(backingArray, arrayOffset + index, order);
	}

	@Override
	public final long getLong() {
		int newPosition = position + SizeOf.LONG;
		if (newPosition > limit) {
			throw new BufferUnderflowException();
		}
		long result = Memory.peekLong(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	@Override
	public final long getLong(int index) {
		checkIndex(index, SizeOf.LONG);
		return Memory.peekLong(backingArray, arrayOffset + index, order);
	}

	@Override
	public final short getShort() {
		int newPosition = position + SizeOf.SHORT;
		if (newPosition > limit) {
			throw new BufferUnderflowException();
		}
		short result = Memory.peekShort(backingArray, arrayOffset + position, order);
		position = newPosition;
		return result;
	}

	@Override
	public final short getShort(int index) {
		checkIndex(index, SizeOf.SHORT);
		return Memory.peekShort(backingArray, arrayOffset + index, order);
	}

	@Override
	public final boolean isDirect() {
		return isDirect;
	}

	@Override
	public ByteBuffer put(byte b) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		if (position == limit) {
			throw new BufferOverflowException();
		}
		backingArray[arrayOffset + position++] = b;
		return this;
	}

	@Override
	public ByteBuffer put(int index, byte b) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		checkIndex(index);
		backingArray[arrayOffset + index] = b;
		return this;
	}

	@Override
	public ByteBuffer put(byte[] src, int srcOffset, int byteCount) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		checkPutBounds(1, src.length, srcOffset, byteCount);
		System.arraycopy(src, srcOffset, backingArray, arrayOffset + position, byteCount);
		position += byteCount;
		return this;
	}

	private byte[] temp = new byte[8];

	private void _put(byte b) {
		backingArray[arrayOffset + position++] = b;
	}

	private void _putTemp(byte[] bb, int count) {
		for (int n = 0; n < count; n++) _put(bb[n]);
	}

	private boolean isLittleEndian() {
		return this.order() == ByteOrder.LITTLE_ENDIAN;
	}

	private void _tempShort(short v) {
		JTranscBits.writeShort(temp, 0, v, isLittleEndian());
	}

	private void _tempInt(int v) {
		JTranscBits.writeInt(temp, 0, v, isLittleEndian());
	}

	private void _tempLong(int v) {
		JTranscBits.writeLong(temp, 0, v, isLittleEndian());
	}

	private void _tempFloat(float v) {
		JTranscBits.writeInt(temp, 0, Float.floatToIntBits(v), isLittleEndian());
	}

	private void _tempDouble(double v) {
		JTranscBits.writeLong(temp, 0, Double.doubleToLongBits(v), isLittleEndian());
	}

	final void put(char[] src, int srcOffset, int charCount) {
		checkPutBounds(SizeOf.CHAR, src.length, srcOffset, charCount);
		for (int n = 0; n < charCount; n++) {
			_tempShort((short)src[srcOffset + n]);
			_putTemp(temp, SizeOf.CHAR);
		}
	}

	final void put(double[] src, int srcOffset, int doubleCount) {
		checkPutBounds(SizeOf.DOUBLE, src.length, srcOffset, doubleCount);
		for (int n = 0; n < doubleCount; n++) {
			_tempDouble((short)src[srcOffset + n]);
			_putTemp(temp, SizeOf.DOUBLE);
		}
	}

	final void put(float[] src, int srcOffset, int floatCount) {
		checkPutBounds(SizeOf.FLOAT, src.length, srcOffset, floatCount);
		for (int n = 0; n < floatCount; n++) {
			_tempFloat((short)src[srcOffset + n]);
			_putTemp(temp, SizeOf.FLOAT);
		}
	}

	final void put(int[] src, int srcOffset, int intCount) {
		checkPutBounds(SizeOf.INT, src.length, srcOffset, intCount);
		for (int n = 0; n < intCount; n++) {
			_tempInt((short)src[srcOffset + n]);
			_putTemp(temp, SizeOf.INT);
		}
	}

	final void put(long[] src, int srcOffset, int longCount) {
		checkPutBounds(SizeOf.LONG, src.length, srcOffset, longCount);
		for (int n = 0; n < longCount; n++) {
			_tempLong((short)src[srcOffset + n]);
			_putTemp(temp, SizeOf.LONG);
		}
	}

	final void put(short[] src, int srcOffset, int shortCount) {
		checkPutBounds(SizeOf.SHORT, src.length, srcOffset, shortCount);
		for (int n = 0; n < shortCount; n++) {
			_tempShort((short)src[srcOffset + n]);
			_putTemp(temp, SizeOf.SHORT);
		}
	}

	@Override
	public ByteBuffer putChar(int index, char value) {
		_checkWritable();
		checkIndex(index, SizeOf.CHAR);
		Memory.pokeShort(backingArray, arrayOffset + index, (short) value, order);
		return this;
	}

	@Override
	public ByteBuffer putChar(char value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		int newPosition = position + SizeOf.CHAR;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		Memory.pokeShort(backingArray, arrayOffset + position, (short) value, order);
		position = newPosition;
		return this;
	}

	@Override
	public ByteBuffer putDouble(double value) {
		return putLong(Double.doubleToRawLongBits(value));
	}

	@Override
	public ByteBuffer putDouble(int index, double value) {
		return putLong(index, Double.doubleToRawLongBits(value));
	}

	@Override
	public ByteBuffer putFloat(float value) {
		return putInt(Float.floatToRawIntBits(value));
	}

	@Override
	public ByteBuffer putFloat(int index, float value) {
		return putInt(index, Float.floatToRawIntBits(value));
	}

	@Override
	public ByteBuffer putInt(int value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		int newPosition = position + SizeOf.INT;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		Memory.pokeInt(backingArray, arrayOffset + position, value, order);
		position = newPosition;
		return this;
	}

	@Override
	public ByteBuffer putInt(int index, int value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		checkIndex(index, SizeOf.INT);
		Memory.pokeInt(backingArray, arrayOffset + index, value, order);
		return this;
	}

	@Override
	public ByteBuffer putLong(int index, long value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		checkIndex(index, SizeOf.LONG);
		Memory.pokeLong(backingArray, arrayOffset + index, value, order);
		return this;
	}

	@Override
	public ByteBuffer putLong(long value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		int newPosition = position + SizeOf.LONG;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		Memory.pokeLong(backingArray, arrayOffset + position, value, order);
		position = newPosition;
		return this;
	}

	@Override
	public ByteBuffer putShort(int index, short value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		checkIndex(index, SizeOf.SHORT);
		Memory.pokeShort(backingArray, arrayOffset + index, value, order);
		return this;
	}

	@Override
	public ByteBuffer putShort(short value) {
		if (isReadOnly) {
			throw new ReadOnlyBufferException();
		}
		int newPosition = position + SizeOf.SHORT;
		if (newPosition > limit) {
			throw new BufferOverflowException();
		}
		Memory.pokeShort(backingArray, arrayOffset + position, value, order);
		position = newPosition;
		return this;
	}

	@Override
	public final CharBuffer asCharBuffer() {
		return ByteBufferAsCharBuffer.asCharBuffer(this);
	}

	@Override
	public final DoubleBuffer asDoubleBuffer() {
		return ByteBufferAsDoubleBuffer.asDoubleBuffer(this);
	}

	@Override
	public final FloatBuffer asFloatBuffer() {
		return ByteBufferAsFloatBuffer.asFloatBuffer(this);
	}

	@Override
	public final IntBuffer asIntBuffer() {
		return ByteBufferAsIntBuffer.asIntBuffer(this);
	}

	@Override
	public final LongBuffer asLongBuffer() {
		return ByteBufferAsLongBuffer.asLongBuffer(this);
	}

	@Override
	public final ShortBuffer asShortBuffer() {
		return ByteBufferAsShortBuffer.asShortBuffer(this);
	}
}
