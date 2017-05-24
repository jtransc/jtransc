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

@JTranscAddMembers(target = "cpp", value = "int32_t* tarray = nullptr;")
class ByteBufferAsIntBuffer extends IntBuffer implements ByteBufferAs {
    final ByteBuffer byteBuffer;
	final byte[] bytes;
	final boolean isLittleEndian;

    static IntBuffer asIntBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
        return byteBuffer.isNativeOrder ? new ByteBufferAsIntBuffer(slice) : new ByteBufferAsIntBuffer.Reversed(slice);
    }

    ByteBufferAsIntBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.INT);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
        this.effectiveDirectAddress = byteBuffer.effectiveDirectAddress;
        this.isLittleEndian = byteBuffer.isLittleEndian;
        this.bytes = byteBuffer.array();
        init(byteBuffer.array());
    }

	@JTranscMethodBody(target = "js", value = "this.tarray = new Int32Array(p0.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->tarray = (int32_t *)(GET_OBJECT(JA_B, p0)->_data);")
	private void init(byte[] data) {
	}

	@Override
    public IntBuffer asReadOnlyBuffer() {
        ByteBufferAsIntBuffer buf = new ByteBufferAsIntBuffer(byteBuffer.asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        buf.byteBuffer.order = byteBuffer.order;
        return buf;
    }

    @Override
    public IntBuffer compact() {
        if (byteBuffer.isReadOnly()) throw new ReadOnlyBufferException();
        byteBuffer.limit(limit * SizeOf.INT);
        byteBuffer.position(position * SizeOf.INT);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    @Override
    public IntBuffer duplicate() {
        ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
        ByteBufferAsIntBuffer buf = new ByteBufferAsIntBuffer(bb);
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    @Override
    public int get() {
        if (position == limit) throw new BufferUnderflowException();
        return get(position++);
    }

    @Override
    public IntBuffer get(int[] dst, int dstOffset, int intCount) {
        byteBuffer.limit(limit * SizeOf.INT);
        byteBuffer.position(position * SizeOf.INT);
        ((ByteBuffer) byteBuffer).get(dst, dstOffset, intCount);
        this.position += intCount;
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

    @Override int[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

    @Override
    public IntBuffer put(int c) {
        return put(position++, c);
    }

    //@Override
    //public IntBuffer put(int[] src, int srcOffset, int intCount) {
    //    byteBuffer.limit(limit * SizeOf.INT);
    //    byteBuffer.position(position * SizeOf.INT);
    //    ((ByteBuffer) byteBuffer).put(src, srcOffset, intCount);
    //    this.position += intCount;
    //    return this;
    //}

    @Override
    public IntBuffer slice() {
        byteBuffer.limit(limit * SizeOf.INT);
        byteBuffer.position(position * SizeOf.INT);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        IntBuffer result = new ByteBufferAsIntBuffer(bb);
        byteBuffer.clear();
        return result;
    }

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	@Override
	@JTranscMethodBody(target = "js", value = "return this.tarray[p0];")
	@JTranscMethodBody(target = "cpp", value = "return this->tarray[p0];")
	public int get(int index) {
		checkIndex(index);
		//return byteBuffer.getInt(index * SizeOf.INT);
		return Memory.peekAlignedInt(bytes, index, isLittleEndian);
	}

	@Override
	@JTranscMethodBody(target = "js", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cpp", value = "this->tarray[p0] = p1; return this;")
	public IntBuffer put(int index, int c) {
		checkIndex(index);
		Memory.pokeInt(bytes, index * SizeOf.INT, c, isLittleEndian);
		return this;
	}

	static public class Reversed extends ByteBufferAsIntBuffer {
		Reversed(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public int get(int index) {
			return Integer.reverseBytes(super.get(index));
		}

		@Override
		public IntBuffer put(int index, int c) {
			return super.put(index, Integer.reverseBytes(c));
		}
	}

}