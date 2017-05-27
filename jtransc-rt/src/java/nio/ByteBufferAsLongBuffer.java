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

import java.nio.internal.SizeOf;

import java.nio.internal.ByteBufferAs;

@HaxeAddMembers("public var tarray:haxe.io.Int32Array = null;")
@JTranscAddMembers(target = "dart", value = "Int32List tarray;")
@JTranscAddMembers(target = "cpp", value = "int64_t* tarray = nullptr;")
@JTranscAddMembers(target = "cs", value = "public byte[] tarray;")
abstract class ByteBufferAsLongBuffer extends LongBuffer implements ByteBufferAs {
    final ByteBuffer byteBuffer;
	final byte[] bytes;

    static LongBuffer asLongBuffer(ByteBuffer byteBuffer) {
        ByteBuffer slice = byteBuffer.slice();
        slice.order(byteBuffer.order());
		return create(slice, byteBuffer.isLittleEndian);
    }

	static private ByteBufferAsLongBuffer create(ByteBuffer byteBuffer, boolean isLittleEndian) {
		return isLittleEndian ? new ByteBufferAsLongBuffer.LE(byteBuffer) : new ByteBufferAsLongBuffer.BE(byteBuffer);
	}

	private ByteBufferAsLongBuffer createWithSameOrder(ByteBuffer byteBuffer) {
		return create(byteBuffer, order() == ByteOrder.LITTLE_ENDIAN);
	}

	private ByteBufferAsLongBuffer(ByteBuffer byteBuffer) {
        super(byteBuffer.capacity() / SizeOf.LONG);
        this.byteBuffer = byteBuffer;
        this.byteBuffer.clear();
		this.bytes = byteBuffer.array();
		init(byteBuffer.array());
	}

	@HaxeMethodBody("this.tarray = haxe.io.Int32Array.fromBytes(p0.data);")
	@JTranscMethodBody(target = "js", value = "this.tarray = new Int32Array(p0.data.buffer);")
	@JTranscMethodBody(target = "dart", value = "this.tarray = new Int64List.view(p0.data.buffer);")
	@JTranscMethodBody(target = "cpp", value = "this->tarray = (int64_t *)(GET_OBJECT(JA_B, p0)->_data);")
	@JTranscMethodBody(target = "cs", value = "unchecked { this.tarray = p0.u(); }")
	private void init(byte[] data) {
	}

    @Override
    public LongBuffer asReadOnlyBuffer() {
        ByteBufferAsLongBuffer buf = (ByteBufferAsLongBuffer) byteBuffer.asReadOnlyBuffer().asLongBuffer();
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        buf.byteBuffer.order = byteBuffer.order;
        return buf;
    }

    @Override
    public LongBuffer compact() {
        if (byteBuffer.isReadOnly()) throw new ReadOnlyBufferException();
        byteBuffer.limit(limit * SizeOf.LONG);
        byteBuffer.position(position * SizeOf.LONG);
        byteBuffer.compact();
        byteBuffer.clear();
        position = limit - position;
        limit = capacity;
        mark = UNSET_MARK;
        return this;
    }

    @Override
    public LongBuffer duplicate() {
        ByteBuffer bb = byteBuffer.duplicate().order(byteBuffer.order());
        ByteBufferAsLongBuffer buf = createWithSameOrder(bb);
        buf.limit = limit;
        buf.position = position;
        buf.mark = mark;
        return buf;
    }

    @Override
    public long get() {
        if (position == limit) throw new BufferUnderflowException();
        return byteBuffer.getLong(position++ * SizeOf.LONG);
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

    @Override long[] protectedArray() {
        throw new UnsupportedOperationException();
    }

    @Override int protectedArrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override boolean protectedHasArray() {
        return false;
    }

	@Override
	public LongBuffer put(long c) {
		if (position == limit) {
			throw new BufferOverflowException();
		}
		byteBuffer.putLong(position++ * SizeOf.LONG, c);
		return this;
	}

    @Override
    public LongBuffer slice() {
        byteBuffer.limit(limit * SizeOf.LONG);
        byteBuffer.position(position * SizeOf.LONG);
        ByteBuffer bb = byteBuffer.slice().order(byteBuffer.order());
        LongBuffer result = createWithSameOrder(bb);
        byteBuffer.clear();
        return result;
    }

	@Override
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	@Override
	@HaxeMethodBody("var low = this.tarray.get(p0 * 2 + 0); var high = this.tarray.get(p0 * 2 + 1); return N.lnew(high, low);")
	@JTranscMethodBody(target = "js", value = "var low = this.tarray[p0 * 2 + 0]; var high = this.tarray[p0 * 2 + 1]; return N.lnew(high, low);")
	@JTranscMethodBody(target = "dart", value = "return N.lnew(this.tarray[p0]);")
	@JTranscMethodBody(target = "cpp", value = "return this->tarray[p0];")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { return ((long *)ptr)[p0]; } }")
	public long get(int index) {
		return Memory.peekAlignedLongLE(bytes, index);
	}

	@Override
	@HaxeMethodBody("this.tarray.set(p0 * 2 + 0, p1.low); this.tarray.set(p0 * 2 + 1, p1.high); return this;")
	@JTranscMethodBody(target = "js", value = "this.tarray[p0 * 2 + 0] = p1.low; this.tarray[p0 * 2 + 1] = p1.high; return this;")
	@JTranscMethodBody(target = "dart", value = "this.tarray[p0] = p1.toInt(); return this;")
	@JTranscMethodBody(target = "cpp", value = "this->tarray[p0] = p1; return this;")
	@JTranscMethodBody(target = "cs", value = "unsafe { fixed (byte* ptr = this.tarray) { ((long *)ptr)[p0] = p1; } } return this;")
	public LongBuffer put(int index, long c) {
		Memory.pokeAlignedLongLE(bytes, index, c);
		return this;
	}

	final static public class LE extends ByteBufferAsLongBuffer {
		LE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}
	}

	final static public class BE extends ByteBufferAsLongBuffer {
		BE(ByteBuffer byteBuffer) {
			super(byteBuffer);
		}

		@Override
		public long get(int index) {
			return JTranscBits.reverseBytes(super.get(index));
		}

		@Override
		public LongBuffer put(int index, long c) {
			return super.put(index, JTranscBits.reverseBytes(c));
		}
	}
}