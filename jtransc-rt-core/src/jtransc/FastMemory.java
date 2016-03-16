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
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;
import jtransc.annotation.haxe.HaxeRemoveField;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// USING ByteBuffer
@JTranscInvisible
@HaxeAddMembers({
	"public var _length:Int;",
	"public var _data:haxe.io.Bytes;",
	"public var shortData:haxe.io.UInt16Array;",
	"public var intData:haxe.io.Int32Array;",
	"public var floatData:haxe.io.Float32Array;",
	"public var doubleData:haxe.io.Float64Array;"
})
final public class FastMemory {
	@HaxeRemoveField
    private int length;

	@HaxeRemoveField
    private ByteBuffer data;

	@HaxeMethodBody(
		"this._length = p0;\n" +
			"this._data = haxe.io.Bytes.alloc((p0 + 7) & ~7);\n" +
			"this.shortData = haxe.io.UInt16Array.fromBytes(this._data);\n" +
			"this.intData = haxe.io.Int32Array.fromBytes(this._data);\n" +
			"this.floatData = haxe.io.Float32Array.fromBytes(this._data);\n" +
			"this.doubleData = haxe.io.Float64Array.fromBytes(this._data);\n"
	)
    public FastMemory(int size) {
        this.length = size;
        this.data = ByteBuffer.allocateDirect((size + 7) & ~7).order(ByteOrder.nativeOrder());
    }

    @JTranscInline
	@HaxeMethodBody("return this._length;")
    final public int getLength() {
        return this.length;
    }

    @JTranscInline
	@HaxeMethodBody("return this._data.length;")
    final public int getAllocatedLength() {
        return this.data.limit();
    }

    // Unaligned
    @JTranscInline
	@HaxeMethodBody("return this._data.get(p0);")
    final public byte getInt8(int index) {
        return data.get(index);
    }

    @JTranscInline
	@HaxeMethodBody("return (this._data.getUInt16(p0) << 16) >> 16;")
    final public short getInt16(int index) {
        return data.getShort(index);
    }

    @JTranscInline
	@HaxeMethodBody("return this._data.getInt32(p0);")
    final public int getInt32(int index) {
        return data.getInt(index);
    }

    @JTranscInline
	@HaxeMethodBody("return this._data.getInt64(p0);")
    final public long getInt64(int index) {
        return data.getLong(index);
    }

    @JTranscInline
	@HaxeMethodBody("return this._data.getFloat(p0);")
    final public float getFloat32(int index) {
        return data.getFloat(index);
    }

    @JTranscInline
	@HaxeMethodBody("return this._data.getDouble(p0);")
    final public double getFloat64(int index) {
        return data.getDouble(index);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.set(p0, p1);")
    final public void setInt8(int index, byte value) {
        data.put(index, value);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.setUInt16(p0, p1);")
    final public void setInt16(int index, short value) {
        data.putShort(index, value);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.setInt32(p0, p1);")
    final public void setInt32(int index, int value) {
        data.putInt(index, value);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.setInt64(p0, p1);")
    final public void setInt64(int index, long value) {
        data.putLong(index, value);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.setFloat(p0, p1);")
    final public void setFloat32(int index, float value) {
        data.putFloat(index, value);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.setDouble(p0, p1);")
    final public void setFloat64(int index, double value) {
        data.putDouble(index, value);
    }

    // Aligned

    @JTranscInline
	@HaxeMethodBody("return this._data.get(p0);")
    final public byte getAlignedInt8(int index) {
        return data.get(index << 0);
    }

    @JTranscInline
	@HaxeMethodBody("return (this.shortData.get(p0) << 16) >> 16;")
    final public short getAlignedInt16(int index2) {
        return data.getShort(index2 << 1);
    }

    @JTranscInline
	@HaxeMethodBody("return this.intData.get(p0);")
    final public int getAlignedInt32(int index4) {
        return data.getInt(index4 << 2);
    }

    @JTranscInline
	@HaxeMethodBody("return this._data.getInt64(p0 << 3);") // @TODO: Optimize
    final public long getAlignedInt64(int index8) {
        return data.getLong(index8 << 3);
    }

    @JTranscInline
	@HaxeMethodBody("return this.floatData.get(p0);")
    final public float getAlignedFloat32(int index4) {
        return data.getFloat(index4 << 2);
    }

    @JTranscInline
	@HaxeMethodBody("return this.doubleData.get(p0);")
    final public double getAlignedFloat64(int index8) {
        return data.getDouble(index8 << 3);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.set(p0, p1);")
    final public void setAlignedInt8(int index, byte value) {
        data.put(index << 0, value);
    }

    @JTranscInline
	@HaxeMethodBody("this.shortData.set(p0, p1);")
    final public void setAlignedInt16(int index2, short value) {
        data.putShort(index2 << 1, value);
    }

    @JTranscInline
	@HaxeMethodBody("this.intData.set(p0, p1);")
    final public void setAlignedInt32(int index4, int value) {
        data.putInt(index4 << 2, value);
    }

    @JTranscInline
	@HaxeMethodBody("this._data.setInt64(p0 << 3, p1);") // @TODO: Optimize
    final public void setAlignedInt64(int index8, long value) {
        data.putLong(index8 << 3, value);
    }

    @JTranscInline
	@HaxeMethodBody("this.floatData.set(p0, p1);")
    final public void setAlignedFloat32(int index4, float value) {
        data.putFloat(index4 << 2, value);
    }

    @JTranscInline
	@HaxeMethodBody("this.doubleData.set(p0, p1);")
    final public void setAlignedFloat64(int index8, double value) {
        data.putDouble(index8 << 3, value);
    }

	// UNALIGNED REVERSED

	@JTranscInline
	@HaxeMethodBody("return this._data.get(p0);")
	final public byte getInt8_REV(int index) {
		return data.get(index);
	}

	@JTranscInline
	@HaxeMethodBody("return HaxeNatives.swap16((this._data.getUInt16(p0) << 16) >> 16);")
	final public short getInt16_REV(int index) {
		return Short.reverseBytes(data.getShort(index));
	}

	@JTranscInline
	@HaxeMethodBody("return HaxeNatives.swap32(this._data.getInt32(p0));")
	final public int getInt32_REV(int index) {
		return Integer.reverseBytes(data.getInt(index));
	}

	@JTranscInline
	@HaxeMethodBody("return HaxeNatives.intBitsToFloat(HaxeNatives.swap32(this._data.getInt32(p0)));")
	final public float getFloat32_REV(int index) {
		return Float.intBitsToFloat(Integer.reverseBytes(data.getInt(index)));
	}

	static public void copy(byte[] from, int fromOffset, byte[] to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to[toOffset + n] = from[fromOffset + n];
	}

    static public void copy(FastMemory from, int fromOffset, byte[] to, int toOffset, int length) {
        for (int n = 0; n < length; n++) to[toOffset + n] = from.getInt8(fromOffset + n);
    }

	static public void copy(byte[] from, int fromOffset, FastMemory to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to.setInt8(toOffset + n, from[fromOffset + n]);
	}

	static public void copy(FastMemory from, int fromOffset, FastMemory to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to.setInt8(toOffset + n, from.getInt8(fromOffset + n));
	}
}
