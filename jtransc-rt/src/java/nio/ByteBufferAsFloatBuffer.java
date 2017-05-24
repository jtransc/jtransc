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

import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import libcore.io.Memory;

import java.nio.internal.SizeOf;

import java.nio.internal.ByteBufferAs;

@JTranscAddMembers(target = "cpp", value = "int32_t* iarray = nullptr;")
class ByteBufferAsFloatBuffer extends FloatBuffer implements ByteBufferAs {

    final ByteBuffer byteBuffer;
	final byte[] bytes;

    static FloatBuffer asFloatBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
		boolean isLittleEndian = byteBuffer.isLittleEndian;
		return isLittleEndian ? new ByteBufferAsFloatBuffer(slice) : new ByteBufferAsFloatBuffer.BE(slice);

	}

    ByteBufferAsFloatBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.FLOAT);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
		this.bytes = byteBuffer.array();
		init(byteBuffer.array());
    }

    @JTranscMethodBody(target = "js", value = "this.iarray = new Int32Array(p0.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->iarray = (int32_t *)(GET_OBJECT(JA_B, p0)->_data);")
    private void init(byte[] data) {
	}

    @Override
    public FloatBuffer asReadOnlyBuffer() {
        ByteBufferAsFloatBuffer buf = new ByteBufferAsFloatBuffer(byteBuffer.asReadOnlyBuffer());
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
        ByteBufferAsFloatBuffer buf = new ByteBufferAsFloatBuffer(bb);
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
    public FloatBuffer get(float[] dst, int dstOffset, int floatCount) {
        byteBuffer.limit(limit * SizeOf.FLOAT);
        byteBuffer.position(position * SizeOf.FLOAT);
        ((ByteBuffer) byteBuffer).get(dst, dstOffset, floatCount);
        this.position += floatCount;
        return this;
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

    @Override float[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

    @Override
    public FloatBuffer put(float c) {
        return put(position++, c);
    }

	@Override
	public float get(int index) {
		return Float.intBitsToFloat(_getInt(index));
	}

	@Override
    public FloatBuffer put(int index, float c) {
    	_putInt(index, Float.floatToIntBits(c));
        return this;
    }

	@Override
    public FloatBuffer put(float[] src, int srcOffset, int floatCount) {
        byteBuffer.limit(limit * SizeOf.FLOAT);
		byteBuffer.position(position * SizeOf.FLOAT);
		int offset = position * SizeOf.FLOAT;
		for (int n = 0; n < floatCount; n++) {
			byteBuffer.putFloat(offset, src[srcOffset + n]);
			offset += 4;
		}
		position += floatCount;
        return this;
    }

    @Override
    public FloatBuffer slice() {
        byteBuffer.limit(limit * SizeOf.FLOAT);
        byteBuffer.position(position * SizeOf.FLOAT);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        FloatBuffer result = new ByteBufferAsFloatBuffer(bb);
        byteBuffer.clear();
        return result;
    }

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	@JTranscMethodBody(target = "js", value = "this.iarray[p0] = p1;")
	@JTranscMethodBody(target = "cpp", value = "this->iarray[p0] = p1;")
	protected void _putInt(int index, int c) {
		checkIndex(index);
		//byteBuffer.putInt(index * SizeOf.INT, c);
		Memory.pokeInt(bytes, index, c, true);
		//return Memory.peekAlignedFloatLE(bytes, index);
	}

	@JTranscMethodBody(target = "js", value = "return this.iarray[p0];")
	@JTranscMethodBody(target = "cpp", value = "return this->iarray[p0];")
	protected int _getInt(int index) {
		checkIndex(index);
		return Memory.peekAlignedIntLE(bytes, index);
	}

	static public class BE extends ByteBufferAsFloatBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		protected void _putInt(int index, int c) {
			super._putInt(index, Integer.reverseBytes(c));
		}

		@Override
		protected int _getInt(int index) {
			return Integer.reverseBytes(super._getInt(index));
		}
	}
}