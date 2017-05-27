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
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import libcore.io.Memory;

import java.nio.internal.SizeOf;

import java.nio.internal.ByteBufferAs;

@HaxeAddMembers("public var tarray:haxe.io.Int32Array = null;")
@JTranscAddMembers(target = "dart", value = "Int32List tarray;")
@JTranscAddMembers(target = "cpp", value = "int32_t* tarray = nullptr;")
@JTranscAddMembers(target = "cs", value = "public byte[] tarray;")
abstract class ByteBufferAsIntBuffer extends IntBuffer implements ByteBufferAs {
    final ByteBuffer byteBuffer;
	final byte[] bytes;

    static IntBuffer asIntBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
        return create(slice, byteBuffer.isLittleEndian);
    }

	static private ByteBufferAsIntBuffer create(ByteBuffer byteBuffer, boolean isLittleEndian) {
		return isLittleEndian ? new ByteBufferAsIntBuffer.LE(byteBuffer) : new ByteBufferAsIntBuffer.BE(byteBuffer);
	}

	private ByteBufferAsIntBuffer createWithSameOrder(ByteBuffer byteBuffer) {
		return create(byteBuffer, order() == ByteOrder.LITTLE_ENDIAN);
	}

	ByteBufferAsIntBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.INT);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
        this.bytes = byteBuffer.array();
        init(byteBuffer.array());
    }

	@HaxeMethodBody("this.tarray = haxe.io.Int32Array.fromBytes(p0.data);")
	@JTranscMethodBody(target = "js", value = "this.tarray = new Int32Array(p0.data.buffer);")
	@JTranscMethodBody(target = "dart", value = "this.tarray = new Int32List.view(p0.data.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->tarray = (int32_t *)(GET_OBJECT(JA_B, p0)->_data);")
	@JTranscMethodBody(target = "cs", value = "unchecked { this.tarray = p0.u(); }")
	private void init(byte[] data) {
	}

	@Override
    public IntBuffer asReadOnlyBuffer() {
        ByteBufferAsIntBuffer buf = (ByteBufferAsIntBuffer) byteBuffer.asReadOnlyBuffer().asIntBuffer();
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
        ByteBufferAsIntBuffer buf = createWithSameOrder(bb);
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

    @Override
    public IntBuffer slice() {
        byteBuffer.limit(limit * SizeOf.INT);
        byteBuffer.position(position * SizeOf.INT);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        IntBuffer result = createWithSameOrder(bb);
        byteBuffer.clear();
        return result;
    }

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	@Override
	@HaxeMethodBody("return this.tarray.get(p0);")
	@JTranscMethodBody(target = "js", value = "return this.tarray[p0];")
	@JTranscMethodBody(target = "dart", value = "return this.tarray[p0];")
	@JTranscMethodBody(target = "cpp", value = "return this->tarray[p0];")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { return ((int *)ptr)[p0]; } }")
	public int get(int index) {
		checkIndex(index);
		//return byteBuffer.getInt(index * SizeOf.INT);
		return Memory.peekAlignedIntLE(bytes, index);
	}

	@Override
	@HaxeMethodBody("this.tarray.set(p0, p1); return this;")
	@JTranscMethodBody(target = "js", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "dart", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cpp", value = "this->tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { ((int *)ptr)[p0] = p1;; } } return this;")
	public IntBuffer put(int index, int c) {
		checkIndex(index);
		Memory.pokeAlignedIntLE(bytes, index, c);
		return this;
	}

	final static public class LE extends ByteBufferAsIntBuffer {
		LE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}
	}

	final static public class BE extends ByteBufferAsIntBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public int get(int index) {
			return Memory.peekAlignedIntBE(bytes, index);
		}

		@Override
		public IntBuffer put(int index, int c) {
			checkIndex(index);
			Memory.pokeAlignedInt(bytes, index, c, false);
			return this;
		}
	}
}