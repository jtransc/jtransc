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

public abstract class ByteBuffer extends Buffer implements Comparable<ByteBuffer> {
    final byte[] hb;
    final int offset;
    boolean isReadOnly;

    ByteBuffer(int mark, int pos, int lim, int cap, byte[] hb, int offset) {
        super(mark, pos, lim, cap);
        this.hb = hb;
        this.offset = offset;
    }

    ByteBuffer(int mark, int pos, int lim, int cap) { // package-private
        this(mark, pos, lim, cap, null, 0);
    }

    public static ByteBuffer allocateDirect(int capacity) {
        return allocate(capacity);
    }

    public static ByteBuffer allocate(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException();
        return new HeapByteBuffer(capacity, capacity);
    }

    public static ByteBuffer wrap(byte[] array, int offset, int length) {
        try {
            return new HeapByteBuffer(array, offset, length);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    public static ByteBuffer wrap(byte[] array) {
        return wrap(array, 0, array.length);
    }

    public abstract ByteBuffer slice();

    public abstract ByteBuffer duplicate();

    public abstract ByteBuffer asReadOnlyBuffer();

    public abstract byte get();

    public abstract ByteBuffer put(byte b);

    public abstract byte get(int index);

    public abstract ByteBuffer put(int index, byte b);

    public ByteBuffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        if (length > remaining()) throw new BufferUnderflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++) dst[i] = get();
        return this;
    }

    public ByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    public ByteBuffer put(ByteBuffer src) {
        if (src == this) throw new IllegalArgumentException();
        if (isReadOnly()) throw new ReadOnlyBufferException();
        int n = src.remaining();
        if (n > remaining()) throw new BufferOverflowException();
        for (int i = 0; i < n; i++) put(src.get());
        return this;
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        checkBounds(offset, length, src.length);
        if (length > remaining()) throw new BufferOverflowException();
        int end = offset + length;
        for (int i = offset; i < end; i++) this.put(src[i]);
        return this;
    }

    public final ByteBuffer put(byte[] src) {
        return put(src, 0, src.length);
    }

    public final boolean hasArray() {
        return (hb != null) && !isReadOnly;
    }

    public final byte[] array() {
        if (hb == null) throw new UnsupportedOperationException();
        if (isReadOnly) throw new ReadOnlyBufferException();
        return hb;
    }

    public final int arrayOffset() {
        if (hb == null) throw new UnsupportedOperationException();
        if (isReadOnly) throw new ReadOnlyBufferException();
        return offset;
    }

    public abstract ByteBuffer compact();

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
        if (!(ob instanceof ByteBuffer)) return false;
        ByteBuffer that = (ByteBuffer) ob;
        if (this.remaining() != that.remaining()) return false;
        int p = this.position();
        for (int i = this.limit() - 1, j = that.limit() - 1; i >= p; i--, j--) {
            if (!equals(this.get(i), that.get(j))) return false;
        }
        return true;
    }

    private static boolean equals(byte x, byte y) {
        return x == y;
    }

    public int compareTo(ByteBuffer that) {
        int n = this.position() + Math.min(this.remaining(), that.remaining());
        for (int i = this.position(), j = that.position(); i < n; i++, j++) {
            int cmp = compare(this.get(i), that.get(j));
            if (cmp != 0) return cmp;
        }
        return this.remaining() - that.remaining();
    }

    private static int compare(byte x, byte y) {
        return Byte.compare(x, y);
    }

    boolean bigEndian = true;
    //boolean nativeByteOrder = (Bits.byteOrder() == ByteOrder.BIG_ENDIAN);
    boolean nativeByteOrder = false;

    public final ByteOrder order() {
        return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    public final ByteBuffer order(ByteOrder bo) {
        bigEndian = (bo == ByteOrder.BIG_ENDIAN);
        nativeByteOrder = (bigEndian == nativeByteOrder);
        return this;
    }

    abstract byte _get(int i);

    abstract void _put(int i, byte b);

    public abstract char getChar();

    public abstract ByteBuffer putChar(char value);

    public abstract char getChar(int index);

    public abstract ByteBuffer putChar(int index, char value);

    public abstract CharBuffer asCharBuffer();

    public abstract short getShort();

    public abstract ByteBuffer putShort(short value);

    public abstract short getShort(int index);

    public abstract ByteBuffer putShort(int index, short value);

    public abstract ShortBuffer asShortBuffer();

    public abstract int getInt();

    public abstract ByteBuffer putInt(int value);

    public abstract int getInt(int index);

    public abstract ByteBuffer putInt(int index, int value);

    public abstract IntBuffer asIntBuffer();

    public abstract long getLong();

    public abstract ByteBuffer putLong(long value);

    public abstract long getLong(int index);

    public abstract ByteBuffer putLong(int index, long value);

    public abstract LongBuffer asLongBuffer();

    public abstract float getFloat();

    public abstract ByteBuffer putFloat(float value);

    public abstract float getFloat(int index);

    public abstract ByteBuffer putFloat(int index, float value);

    public abstract FloatBuffer asFloatBuffer();

    public abstract double getDouble();

    public abstract ByteBuffer putDouble(double value);

    public abstract double getDouble(int index);

    public abstract ByteBuffer putDouble(int index, double value);

    public abstract DoubleBuffer asDoubleBuffer();
}
