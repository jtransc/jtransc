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
@JTranscAddMembers(target = "dart", value = "Uint16List tarray;")
@JTranscAddMembers(target = "cpp", value = "uint16_t* tarray = nullptr;")
@JTranscAddMembers(target = "cs", value = "public byte[] tarray;")
abstract class ByteBufferAsCharBuffer extends CharBuffer implements ByteBufferAs {
    final ByteBuffer byteBuffer;
	final byte[] bytes;

    static CharBuffer asCharBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
		return create(slice, byteBuffer.isLittleEndian);
    }

    static private ByteBufferAsCharBuffer create(ByteBuffer byteBuffer, boolean isLittleEndian) {
		return isLittleEndian ? new ByteBufferAsCharBuffer.LE(byteBuffer) : new ByteBufferAsCharBuffer.BE(byteBuffer);
	}

	private ByteBufferAsCharBuffer createWithSameOrder(ByteBuffer byteBuffer) {
		return create(byteBuffer, order() == ByteOrder.LITTLE_ENDIAN);
	}

	private ByteBufferAsCharBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.CHAR);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
		this.bytes = byteBuffer.array();
		init(byteBuffer.array());
    }

	@HaxeMethodBody("this.tarray = haxe.io.UInt16Array.fromBytes(p0.data);")
	@JTranscMethodBody(target = "js", value = "this.tarray = new Uint16Array(p0.data.buffer);")
	@JTranscMethodBody(target = "dart", value = "this.tarray = new Uint16List.view(p0.data.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->tarray = (uint16_t *)(GET_OBJECT(JA_B, p0)->_data);")
	@JTranscMethodBody(target = "cs", value = "unchecked { this.tarray = p0.u(); }")
	private void init(byte[] data) {
	}

	@Override
    public CharBuffer asReadOnlyBuffer() {
        ByteBufferAsCharBuffer buf = createWithSameOrder(byteBuffer.asReadOnlyBuffer());
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        buf.byteBuffer.order = byteBuffer.order;
        return buf;
    }

    @Override
    public CharBuffer compact() {
        if (byteBuffer.isReadOnly()) throw new ReadOnlyBufferException();
        byteBuffer.limit(limit * SizeOf.CHAR);
        byteBuffer.position(position * SizeOf.CHAR);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    @Override
    public CharBuffer duplicate() {
        ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
        ByteBufferAsCharBuffer buf = createWithSameOrder(bb);
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    @Override
    public char get() {
        if (position == limit) throw new BufferUnderflowException();
        return byteBuffer.getChar(position++ * SizeOf.CHAR);
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

    @Override char[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

    @Override
    public CharBuffer put(char c) {
        if (position == limit) throw new BufferOverflowException();
        byteBuffer.putChar(position++ * SizeOf.CHAR, c);
        return this;
    }


    @Override
    public CharBuffer slice() {
        byteBuffer.limit(limit * SizeOf.CHAR);
        byteBuffer.position(position * SizeOf.CHAR);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        CharBuffer result = createWithSameOrder(bb);
        byteBuffer.clear();
        return result;
    }

    @Override public CharBuffer subSequence(int start, int end) {
        checkStartEndRemaining(start, end);
        CharBuffer result = duplicate();
        result.limit(position + end);
        result.position(position + start);
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
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { return ((ushort *)ptr)[p0]; } }")
	public char get(int index) {
		return Memory.peekAlignedCharLE(bytes, index);
	}

	@Override
	@HaxeMethodBody("this.tarray.set(p0, p1); return this;")
	@JTranscMethodBody(target = "js", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "dart", value = "this.tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cpp", value = "this->tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { ((ushort *)ptr)[p0] = p1; } } return this;")
	public CharBuffer put(int index, char c) {
		Memory.pokeAlignedCharLE(bytes, index, c);
		return this;
	}

	final static public class LE extends ByteBufferAsCharBuffer {
		LE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}
    }

	final static public class BE extends ByteBufferAsCharBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public char get(int index) {
			return Character.reverseBytes(super.get(index));
		}

		@Override
		public CharBuffer put(int index, char c) {
			return super.put(index, Character.reverseBytes(c));
		}
	}
}
