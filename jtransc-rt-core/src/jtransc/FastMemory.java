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

package jtransc;

import jtransc.annotation.JTranscInline;
import jtransc.annotation.JTranscInvisible;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

@JTranscInvisible
final public class FastMemory {
    static private Unsafe unsafe;

    private int length;
    private byte[] data;

    public FastMemory(int size) {
        if (unsafe == null) {
            try {
                Field f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (Exception e) {
            }
        }
        this.length = size;
        this.data = new byte[(size + 7) & ~7];
    }

    @JTranscInline
    final public int getLength() {
        return this.length;
    }

    @JTranscInline
    final public int getAllocatedLength() {
        return this.data.length;
    }

    // Unaligned
    @JTranscInline
    final public byte getInt8(int index) {
        return unsafe.getByte(data, (long) index);
    }

    @JTranscInline
    final public short getInt16(int index) {
        return unsafe.getShort(data, (long) index);
    }

    @JTranscInline
    final public int getInt32(int index) {
        return unsafe.getInt(data, (long) index);
    }

    @JTranscInline
    final public long getInt64(int index) {
        return unsafe.getLong(data, (long) index);
    }

    @JTranscInline
    final public float getFloat32(int index) {
        return unsafe.getFloat(data, (long) index);
    }

    @JTranscInline
    final public double getFloat64(int index) {
        return unsafe.getDouble(data, (long) index);
    }

    @JTranscInline
    final public void setInt8(int index, byte value) {
        unsafe.putByte(data, (long) index, value);
    }

    @JTranscInline
    final public void setInt16(int index, short value) {
        unsafe.putShort(data, (long) index, value);
    }

    @JTranscInline
    final public void setInt32(int index, int value) {
        unsafe.putInt(data, (long) index, value);
    }

    @JTranscInline
    final public void setInt64(int index, long value) {
        unsafe.putLong(data, (long) index, value);
    }

    @JTranscInline
    final public void setFloat32(int index, float value) {
        unsafe.putFloat(data, (long) index, value);
    }

    @JTranscInline
    final public void setFloat64(int index, double value) {
        unsafe.putDouble(data, (long) index, value);
    }

    // Aligned

    @JTranscInline
    final public byte getAlignedInt8(int index) {
        return unsafe.getByte(data, (long) index << 0);
    }

    @JTranscInline
    final public short getAlignedInt16(int index2) {
        return unsafe.getShort(data, (long) index2 << 1);
    }

    @JTranscInline
    final public int getAlignedInt32(int index4) {
        return unsafe.getInt(data, (long) index4 << 2);
    }

    @JTranscInline
    final public long getAlignedInt64(int index8) {
        return unsafe.getLong(data, (long) index8 << 3);
    }

    @JTranscInline
    final public float getAlignedFloat32(int index4) {
        return unsafe.getFloat(data, (long) index4 << 2);
    }

    @JTranscInline
    final public double getAlignedFloat64(int index8) {
        return unsafe.getDouble(data, (long) index8 << 3);
    }

    @JTranscInline
    final public void setAlignedInt8(int index, byte value) {
        unsafe.putByte(data, (long) index << 0, value);
    }

    @JTranscInline
    final public void setAlignedInt16(int index2, short value) {
        unsafe.putShort(data, (long) index2 << 1, value);
    }

    @JTranscInline
    final public void setAlignedInt32(int index4, int value) {
        unsafe.putInt(data, (long) index4 << 2, value);
    }

    @JTranscInline
    final public void setAlignedInt64(int index8, long value) {
        unsafe.putLong(data, (long) index8 << 3, value);
    }

    @JTranscInline
    final public void setAlignedFloat32(int index4, float value) {
        unsafe.putFloat(data, (long) index4 << 2, value);
    }

    @JTranscInline
    final public void setAlignedFloat64(int index8, double value) {
        unsafe.putDouble(data, (long) index8 << 3, value);
    }

    @JTranscInline
    static public void copy(FastMemory from, int fromOffset, byte[] to, int toOffset, int length) {
        for (int n = 0; n < length; n++) {
            to[toOffset + n] = (byte)from.getInt8(fromOffset + n);
        }
    }
}
