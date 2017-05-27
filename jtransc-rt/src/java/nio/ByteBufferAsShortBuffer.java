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

@HaxeAddMembers("public var tarray:haxe.io.UInt16Array = null;")
@JTranscAddMembers(target = "dart", value = "Int16List tarray;")
@JTranscAddMembers(target = "cpp", value = "int16_t* tarray = nullptr;")
@JTranscAddMembers(target = "cs", value = "public byte[] tarray;")
class ByteBufferAsShortBuffer extends ShortBuffer implements ByteBufferAs {
    final ByteBuffer byteBuffer;
	final byte[] bytes;

    static ShortBuffer asShortBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
		return create(slice, byteBuffer.isLittleEndian);
    }

	static private ByteBufferAsShortBuffer create(ByteBuffer byteBuffer, boolean isLittleEndian) {
		return isLittleEndian ? new ByteBufferAsShortBuffer.LE(byteBuffer) : new ByteBufferAsShortBuffer.BE(byteBuffer);
	}

	private ByteBufferAsShortBuffer createWithSameOrder(ByteBuffer byteBuffer) {
		return create(byteBuffer, order() == ByteOrder.LITTLE_ENDIAN);
	}

    private ByteBufferAsShortBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.SHORT);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
		this.bytes = byteBuffer.array();
        init(byteBuffer.array());
    }

	@HaxeMethodBody("this.tarray = haxe.io.UInt16Array.fromBytes(p0.data);")
	@JTranscMethodBody(target = "js", value = "this.tarray = new Int16Array(p0.data.buffer);")
	@JTranscMethodBody(target = "dart", value = "this.tarray = new Int16List.view(p0.data.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->tarray = (int16_t *)(GET_OBJECT(JA_B, p0)->_data);")
	@JTranscMethodBody(target = "cs", value = "unchecked { this.tarray = p0.u(); }")
	private void init(byte[] data) {
	}

	@Override
    public ShortBuffer asReadOnlyBuffer() {
        ByteBufferAsShortBuffer buf = (ByteBufferAsShortBuffer) byteBuffer.asReadOnlyBuffer().asShortBuffer();
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        buf.byteBuffer.order = byteBuffer.order;
        return buf;
    }

    @Override
    public ShortBuffer compact() {
        if (byteBuffer.isReadOnly()) throw new ReadOnlyBufferException();
        byteBuffer.limit(limit * SizeOf.SHORT);
        byteBuffer.position(position * SizeOf.SHORT);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    @Override
    public ShortBuffer duplicate() {
        ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
        ByteBufferAsShortBuffer buf = new ByteBufferAsShortBuffer(bb);
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    @Override
    public short get() {
        if (position == limit) throw new BufferUnderflowException();
        return byteBuffer.getShort(position++ * SizeOf.SHORT);
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

    @Override short[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

    @Override
    public ShortBuffer put(short c) {
        if (position == limit) throw new BufferOverflowException();
        byteBuffer.putShort(position++ * SizeOf.SHORT, c);
        return this;
    }

    @Override
    public ShortBuffer slice() {
        byteBuffer.limit(limit * SizeOf.SHORT);
        byteBuffer.position(position * SizeOf.SHORT);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        ShortBuffer result = new ByteBufferAsShortBuffer(bb);
        byteBuffer.clear();
        return result;
    }

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	@Override
	@HaxeMethodBody("return N.i2s(this.tarray.get(p0));")
	@JTranscMethodBody(target = "js", value = "return this.tarray[p0];")
	@JTranscMethodBody(target = "dart", value = "return this.tarray[p0];")
	@JTranscMethodBody(target = "cpp", value = "return this->tarray[p0];")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { return ((short *)ptr)[p0]; } }")
	public short get(int index) {
		return Memory.peekAlignedShortLE(bytes, index);
	}

	@Override
	@HaxeMethodBody("this.tarray.set(p0, p1); return this;")
	@JTranscMethodBody(target = "js", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "dart", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cpp", value = "this->tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { ((short *)ptr)[p0] = p1; } } return this;")
	public ShortBuffer put(int index, short c) {
		Memory.pokeAlignedShortLE(bytes, index, c);
		return this;
	}

	final static public class LE extends ByteBufferAsShortBuffer {
		LE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}
	}

	final static public class BE extends ByteBufferAsShortBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public short get(int index) {
			return Short.reverseBytes(super.get(index));
		}

		@Override
		public ShortBuffer put(int index, short c) {
			return super.put(index, Short.reverseBytes(c));
		}
	}

}