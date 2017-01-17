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

package com.jtransc;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeRemoveField;

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
	"public var doubleData:haxe.io.Float64Array;",
	"public function getBytes():haxe.io.Bytes return _data;"
})
final public class FastMemory {
	@HaxeRemoveField
	private int length;

	@HaxeRemoveField
	private ByteBuffer data;

	private FastMemory(int size) {
		_initWithSize(size);
		_createViews();
	}

	private FastMemory(byte[] data) {
		if (data.length % 8 != 0) throw new RuntimeException("ByteArray must be multiple of 8!");
		_initWithBytes(data);
		_createViews();
	}

	@HaxeMethodBody("return null;")
	@JTranscMethodBody(target = "js", value = "return null;")
	public ByteBuffer getByteBufferOrNull() {
		return data;
	}

	@HaxeMethodBody("this._length = p0.length; this._data = p0.getBytes();")
	@JTranscMethodBody(target = "js", value = "this._length = p0.length; this.buffer = p0.data.buffer;")
	private void _initWithBytes(byte[] data) {
		this.length = data.length;
		this.data = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
	}

	@HaxeMethodBody("this._length = p0; this._data = haxe.io.Bytes.alloc((p0 + 7) & ~7);")
	@JTranscMethodBody(target = "js", value = "this._length = p0; this.buffer = new ArrayBuffer((this._length + 7) & ~7);")
	private void _initWithSize(int size) {
		this.length = size;
		this.data = ByteBuffer.allocateDirect((size + 7) & ~7).order(ByteOrder.nativeOrder());
	}

	@HaxeMethodBody("" +
		"this.shortData = haxe.io.UInt16Array.fromBytes(this._data);\n" +
		"this.intData = haxe.io.Int32Array.fromBytes(this._data);\n" +
		"this.floatData = haxe.io.Float32Array.fromBytes(this._data);\n" +
		"this.doubleData = haxe.io.Float64Array.fromBytes(this._data);\n"
	)
	@JTranscMethodBody(target = "js", value = {
		"this.view   = new DataView(this.buffer);",
		"this.s8     = new Int8Array(this.buffer);",
		"this.u8     = new Uint8Array(this.buffer);",
		"this.s16    = new Int16Array(this.buffer);",
		"this.u16    = new Uint16Array(this.buffer);",
		"this.s32    = new Int32Array(this.buffer);",
		"this.f32    = new Float32Array(this.buffer);",
		"this.f64    = new Float64Array(this.buffer);",
	})
	private void _createViews() {
	}

	static public FastMemory alloc(int size) {
		return new FastMemory(size);
	}

	static public FastMemory wrap(byte[] bytes) {
		return new FastMemory(bytes);
	}

	static public FastMemory wrap(ByteBuffer buffer) {
		return new FastMemory(buffer.array());
	}

	@JTranscInline
	@HaxeMethodBody("return this._length;")
	@JTranscMethodBody(target = "js", value = "return this._length;")
	final public int getLength() {
		return this.length;
	}

	@JTranscInline
	@HaxeMethodBody("return this._data.length;")
	@JTranscMethodBody(target = "js", value = "return this.buffer.byteLength;")
	final public int getAllocatedLength() {
		return this.data.limit();
	}

	// Unaligned
	@JTranscInline
	@HaxeMethodBody("return this._data.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.u8[p0];")
	final public int getInt8(int index) {
		return data.get(index) & 0xFF;
	}

	@JTranscInline
	@HaxeMethodBody("return (this._data.getUInt16(p0) << 16) >> 16;")
	@JTranscMethodBody(target = "js", value = "return this.view.getUint16(p0, true);")
	final public int getInt16(int index) {
		return data.getShort(index) & 0xFFFF;
	}

	@JTranscInline
	@HaxeMethodBody("return this._data.getInt32(p0);")
	@JTranscMethodBody(target = "js", value = "return this.view.getInt32(p0, true);")
	final public int getInt32(int index) {
		return data.getInt(index);
	}

	@JTranscInline
	@HaxeMethodBody("return this._data.getInt64(p0);")
	@JTranscMethodBody(target = "js", value = "return Int64.make(this.view.getInt32(p0, true), this.view.getInt32(p0 + 4, true));")
	final public long getInt64(int index) {
		return data.getLong(index);
	}

	@JTranscInline
	@HaxeMethodBody("return this._data.getFloat(p0);")
	@JTranscMethodBody(target = "js", value = "return this.view.getFloat32(p0, true);")
	final public float getFloat32(int index) {
		return data.getFloat(index);
	}

	@JTranscInline
	@HaxeMethodBody("return this._data.getDouble(p0);")
	@JTranscMethodBody(target = "js", value = "return this.view.getFloat64(p0, true);")
	final public double getFloat64(int index) {
		return data.getDouble(index);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.u8[p0] = p1;")
	final public void setInt8(int index, int value) {
		data.put(index, (byte) value);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.setUInt16(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.view.setInt16(p0, p1, true);")
	final public void setInt16(int index, int value) {
		data.putShort(index, (short) value);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.setInt32(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.view.setInt32(p0, p1, true);")
	final public void setInt32(int index, int value) {
		data.putInt(index, value);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.setInt64(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.view.setInt32(p0, p1.high); this.view.setInt32(p0 + 4, p1.low, true);")
	final public void setInt64(int index, long value) {
		data.putLong(index, value);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.setFloat(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.view.setFloat32(p0, p1, true);")
	final public void setFloat32(int index, float value) {
		data.putFloat(index, value);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.setDouble(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.view.setFloat64(p0, p1, true);")
	final public void setFloat64(int index, double value) {
		data.putDouble(index, value);
	}

	// Aligned

	@JTranscInline
	@HaxeMethodBody("return this._data.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.u8[p0];")
	final public int getAlignedInt8(int index) {
		return data.get(index << 0) & 0xFF;
	}

	@JTranscInline
	@HaxeMethodBody("return (this.shortData.get(p0) << 16) >> 16;")
	@JTranscMethodBody(target = "js", value = "return this.u16[p0];")
	final public int getAlignedInt16(int index2) {
		return data.getShort(index2 << 1) & 0xFFFF;
	}

	@JTranscInline
	@HaxeMethodBody("return this.intData.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.s32[p0];")
	final public int getAlignedInt32(int index4) {
		return data.getInt(index4 << 2);
	}

	@JTranscInline
	@HaxeMethodBody("return this._data.getInt64(p0 << 3);") // @TODO: Optimize
	@JTranscMethodBody(target = "js", value = "return Int64.make(this.s32[p0 << 1 + 0], this.s32[p0 << 1 +1]);")
	final public long getAlignedInt64(int index8) {
		return data.getLong(index8 << 3);
	}

	@JTranscInline
	@HaxeMethodBody("return this.floatData.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.f32[p0];")
	final public float getAlignedFloat32(int index4) {
		return data.getFloat(index4 << 2);
	}

	@JTranscInline
	@HaxeMethodBody("return this.doubleData.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.f64[p0];")
	final public double getAlignedFloat64(int index8) {
		return data.getDouble(index8 << 3);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.u8[p0] = p1;")
	final public void setAlignedInt8(int index, int value) {
		data.put(index << 0, (byte) value);
	}

	@JTranscInline
	@HaxeMethodBody("this.shortData.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.u16[p0] = p1;")
	final public void setAlignedInt16(int index2, int value) {
		data.putShort(index2 << 1, (short) value);
	}

	@JTranscInline
	@HaxeMethodBody("this.intData.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.s32[p0] = p1;")
	final public void setAlignedInt32(int index4, int value) {
		data.putInt(index4 << 2, value);
	}

	@JTranscInline
	@HaxeMethodBody("this._data.setInt64(p0 << 3, p1);") // @TODO: Optimize
	@JTranscMethodBody(target = "js", value = "this.s32[p0 << 1 + 0] = p1.low; this.s32[p0 << 1 + 1] = p1.high;")
	final public void setAlignedInt64(int index8, long value) {
		data.putLong(index8 << 3, value);
	}

	@JTranscInline
	@HaxeMethodBody("this.floatData.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.f32[p0] = p1;")
	final public void setAlignedFloat32(int index4, float value) {
		data.putFloat(index4 << 2, value);
	}

	@JTranscInline
	@HaxeMethodBody("this.doubleData.set(p0, p1);")
	@JTranscMethodBody(target = "js", value = "this.f64[p0] = p1;")
	final public void setAlignedFloat64(int index8, double value) {
		data.putDouble(index8 << 3, value);
	}

	// UNALIGNED REVERSED

	@JTranscInline
	@HaxeMethodBody("return this._data.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.u8[p0];")
	final public int getInt8_REV(int index) {
		return data.get(index) & 0xFF;
	}

	@JTranscInline
	@HaxeMethodBody("return N.swap16((this._data.getUInt16(p0) << 16) >> 16);")
	@JTranscMethodBody(target = "js", value = "return this.view.getUint16(p0, false);")
	final public int getInt16_REV(int index) {
		return Short.reverseBytes(data.getShort(index)) & 0xFFFF;
	}

	@JTranscInline
	@HaxeMethodBody("return N.swap32(this._data.getInt32(p0));")
	@JTranscMethodBody(target = "js", value = "return this.view.getInt32(p0, false);")
	final public int getInt32_REV(int index) {
		return Integer.reverseBytes(data.getInt(index));
	}

	@JTranscInline
	@HaxeMethodBody("return N.intBitsToFloat(N.swap32(this._data.getInt32(p0)));")
	@JTranscMethodBody(target = "js", value = "return this.view.getFloat32(p0, false);")
	final public float getFloat32_REV(int index) {
		return Float.intBitsToFloat(Integer.reverseBytes(data.getInt(index)));
	}

	// @TODO: provide faster implementations for supported targets
	final public void setArrayInt8(int index, ByteBuffer data, int offset, int len) {
		for (int n = 0; n < len; n++) setInt8(index + n, data.get(offset + n));
	}

	@JTranscMethodBody(target = "js", value = "var index = p0, data = p1.data.buffer, offset = p2, len = p3; new Int8Array(this.buffer, index, len).set(new Int8Array(data, offset, len));")
	final public void setArrayInt8(int index, byte[] data, int offset, int len) {
		for (int n = 0; n < len; n++) setInt8(index + n, data[offset + n]);
	}

	@JTranscMethodBody(target = "js", value = "var index = p0, data = p1.data.buffer, offset = p2, len = p3; new Int8Array(this.buffer, index, len * 2).set(new Int8Array(data, offset * 2, len * 2));")
	final public void setArrayInt16(int index, short[] data, int offset, int len) {
		for (int n = 0; n < len; n++) setInt16(index + n * 2, data[offset + n]);
	}

	@JTranscMethodBody(target = "js", value = "var index = p0, data = p1.data.buffer, offset = p2, len = p3; new Int8Array(this.buffer, index, len * 4).set(new Int8Array(data, offset * 4, len * 4));")
	final public void setArrayInt32(int index, int[] data, int offset, int len) {
		for (int n = 0; n < len; n++) setInt32(index + n * 4, data[offset + n]);
	}

	final public void setArrayInt64(int index, long[] data, int offset, int len) {
		for (int n = 0; n < len; n++) setInt64(index + n * 8, data[offset + n]);
	}

	@JTranscMethodBody(target = "js", value = "var index = p0, data = p1.data.buffer, offset = p2, len = p3; new Int8Array(this.buffer, index, len * 4).set(new Int8Array(data, offset * 4, len * 4));")
	final public void setArrayFloat32(int index, float[] data, int offset, int len) {
		for (int n = 0; n < len; n++) setFloat32(index + n * 4, data[offset + n]);
	}

	@JTranscMethodBody(target = "js", value = "var index = p0, data = p1.data.buffer, offset = p2, len = p3; new Int8Array(this.buffer, index, len * 8).set(new Int8Array(data, offset * 8, len * 8));")
	final public void setArrayFloat64(int index, float[] data, int offset, int len) {
		for (int n = 0; n < len; n++) setFloat32(index + n * 4, data[offset + n]);
	}

	@HaxeMethodBody("p2.getBytes().blit(p3, p0.getBytes(), p1, p4);")
	static public void copy(byte[] from, int fromOffset, byte[] to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to[toOffset + n] = from[fromOffset + n];
	}

	@HaxeMethodBody("p2.getBytes().blit(p3, p0.getBytes(), p1, p4);")
	static public void copy(FastMemory from, int fromOffset, byte[] to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to[toOffset + n] = (byte) from.getInt8(fromOffset + n);
	}

	@HaxeMethodBody("p2.getBytes().blit(p3, p0.getBytes(), p1, p4);")
	static public void copy(byte[] from, int fromOffset, FastMemory to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to.setInt8(toOffset + n, from[fromOffset + n]);
	}

	@HaxeMethodBody("p2.getBytes().blit(p3, p0.getBytes(), p1, p4);")
	static public void copy(FastMemory from, int fromOffset, FastMemory to, int toOffset, int length) {
		for (int n = 0; n < length; n++) to.setInt8(toOffset + n, from.getInt8(fromOffset + n));
	}
}
