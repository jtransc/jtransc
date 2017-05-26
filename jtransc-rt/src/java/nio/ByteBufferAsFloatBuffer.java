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

import com.jtransc.JTranscBits;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import libcore.io.Memory;

import java.nio.internal.ByteBufferAs;
import java.nio.internal.SizeOf;

@HaxeAddMembers("public var iarray:haxe.io.Int32Array = null; public var farray:haxe.io.Float32Array = null;")
@JTranscAddMembers(target = "dart", value = "Int32List iarray; Float32List farray;")
@JTranscAddMembers(target = "cpp", value = "int32_t* iarray = nullptr; float32_t* farray = nullptr;")
@JTranscAddMembers(target = "cs", value = "public byte[] tarray;")
class ByteBufferAsFloatBuffer extends FloatBuffer implements ByteBufferAs {
	final ByteBuffer byteBuffer;
	final byte[] bytes;

	static FloatBuffer asFloatBuffer(ByteBuffer byteBuffer) {
		ByteBuffer slice = byteBuffer.slice();
		slice.order(byteBuffer.order());
		return create(slice, byteBuffer.isLittleEndian);

	}

	static private ByteBufferAsFloatBuffer create(ByteBuffer byteBuffer, boolean isLittleEndian) {
		return isLittleEndian ? new ByteBufferAsFloatBuffer.LE(byteBuffer) : new ByteBufferAsFloatBuffer.BE(byteBuffer);
	}

	private ByteBufferAsFloatBuffer createWithSameOrder(ByteBuffer byteBuffer) {
		return create(byteBuffer, order() == ByteOrder.LITTLE_ENDIAN);
	}

	ByteBufferAsFloatBuffer(ByteBuffer byteBuffer) {
		super(byteBuffer.capacity() / SizeOf.FLOAT);
		this.byteBuffer = byteBuffer;
		this.byteBuffer.clear();
		this.bytes = byteBuffer.array();
		init(byteBuffer.array());
	}

	@HaxeMethodBody("this.iarray = haxe.io.Int32Array.fromBytes(p0.data); this.farray = haxe.io.Float32Array.fromBytes(p0.data);")
	@JTranscMethodBody(target = "js", value = "this.iarray = new Int32Array(p0.data.buffer); this.farray = new Float32Array(p0.data.buffer);")
	@JTranscMethodBody(target = "dart", value = "this.iarray = new Int32List.view(p0.data.buffer); this.farray = new Float32List.view(p0.data.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->iarray = (int32_t *)(GET_OBJECT(JA_B, p0)->_data); this->farray = (float32_t *)(GET_OBJECT(JA_B, p0)->_data);")
	@JTranscMethodBody(target = "cs", value = "unchecked { this.tarray = (byte[])(Array)p0.data; }")
	private void init(byte[] data) {
	}

	@Override
	public FloatBuffer asReadOnlyBuffer() {
		ByteBufferAsFloatBuffer buf = (ByteBufferAsFloatBuffer) byteBuffer.asReadOnlyBuffer().asFloatBuffer();
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		buf.byteBuffer.order = byteBuffer.order;
		return buf;
	}

	@Override
	public FloatBuffer compact() {
		if (byteBuffer.isReadOnly()) throw new ReadOnlyBufferException();
		byteBuffer.limit(limit * SizeOf.FLOAT);
		byteBuffer.position(position * SizeOf.FLOAT);
		byteBuffer.compact();
		byteBuffer.clear();
		position = limit - position;
		limit = capacity;
		mark = UNSET_MARK;
		return this;
	}

	@Override
	public FloatBuffer duplicate() {
		ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
		ByteBufferAsFloatBuffer buf = createWithSameOrder(bb);
		buf.limit = limit;
		buf.position = position;
		buf.mark = mark;
		return buf;
	}

	@Override
	public float get() {
		if (position == limit) throw new BufferUnderflowException();
		return get(position++);
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
	float[] protectedArray() {
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
	public FloatBuffer put(float c) {
		return put(position++, c);
	}

	@Override
	public FloatBuffer slice() {
		byteBuffer.limit(limit * SizeOf.FLOAT);
		byteBuffer.position(position * SizeOf.FLOAT);
		ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
		FloatBuffer result = createWithSameOrder(bb);
		byteBuffer.clear();
		return result;
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	@Override
	@HaxeMethodBody("return this.farray.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.farray[p0];")
	@JTranscMethodBody(target = "dart", value = "return this.farray[p0];")
	@JTranscMethodBody(target = "cpp", value = "return this->farray[p0];")
	@JTranscMethodBody(target = "cs", value = "return BitConverter.ToSingle(this.tarray, p0);")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { return ((float *)ptr)[p0]; } }")
	public float get(int index) {
		return Memory.peekAlignedFloatLE(bytes, index);
	}

	@Override
	@HaxeMethodBody("this.farray.set(p0, p1); return this;")
	@JTranscMethodBody(target = "js", value = "this.farray[p0] = p1; return this;")
	@JTranscMethodBody(target = "dart", value = "this.farray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cpp", value = "this->farray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { ((float *)ptr)[p0] = p1; } } return this;")
	public FloatBuffer put(int index, float c) {
		Memory.pokeAlignedFloatLE(bytes, index, c);
		return this;
	}

	@HaxeMethodBody("return this.iarray.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.iarray[p0];")
	@JTranscMethodBody(target = "dart", value = "return this.iarray[p0];")
	@JTranscMethodBody(target = "cpp", value = "return this->iarray[p0];")
	@JTranscMethodBody(target = "cs", value = "return BitConverter.ToSingle(this.iarray, p0);")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { return ((int *)ptr)[p0]; } }")
	public int getInt(int index) {
		return Memory.peekAlignedIntLE(bytes, index);
	}

	@HaxeMethodBody("this.iarray.set(p0, p1); return this;")
	@JTranscMethodBody(target = "js", value = "this.iarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "dart", value = "this.iarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cpp", value = "this->iarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { ((int *)ptr)[p0] = p1; } } return this;")
	public FloatBuffer putInt(int index, int c) {
		Memory.pokeAlignedIntLE(bytes, index, c);
		return this;
	}

	final static public class LE extends ByteBufferAsFloatBuffer {
		LE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}
	}

	final static public class BE extends ByteBufferAsFloatBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		public float get(int index) {
			return Float.intBitsToFloat(Integer.reverseBytes(getInt(index)));
		}

		public FloatBuffer put(int index, float c) {
			return putInt(index, Integer.reverseBytes(Float.floatToRawIntBits(c)));
		}
	}
}