package java.nio;

class HeapLongBuffer extends LongBuffer {
	public HeapLongBuffer(int mark, int pos, int lim, int cap, long[] hb, int offset) {
		super(mark, pos, lim, cap, hb, offset);
	}

	public HeapLongBuffer(int mark, int pos, int lim, int cap) {
		super(mark, pos, lim, cap);
	}

	@Override
	native public LongBuffer slice();

	@Override
	native public LongBuffer duplicate();

	@Override
	native public LongBuffer asReadOnlyBuffer();

	@Override
	native public long get();

	@Override
	native public LongBuffer put(long l);

	@Override
	native public long get(int index);

	@Override
	native public LongBuffer put(int index, long l);

	@Override
	native public LongBuffer compact();

	@Override
	native public boolean isReadOnly();

	@Override
	native public boolean isDirect();

	@Override
	native public ByteOrder order();
}
