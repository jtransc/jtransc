package java.nio;

public class HeapShortBuffer extends ShortBuffer {
	HeapShortBuffer(int mark, int pos, int lim, int cap, short[] hb, int offset) {
		super(mark, pos, lim, cap, hb, offset);
	}

	HeapShortBuffer(int mark, int pos, int lim, int cap) {
		super(mark, pos, lim, cap);
	}

	@Override
	native public ShortBuffer slice();

	@Override
	native public ShortBuffer duplicate();

	@Override
	native public ShortBuffer asReadOnlyBuffer();

	@Override
	native public short get();

	@Override
	native public ShortBuffer put(short s);

	@Override
	native public short get(int index);

	@Override
	native public ShortBuffer put(int index, short s);

	@Override
	native public ShortBuffer compact();

	@Override
	native public boolean isReadOnly();

	@Override
	native public boolean isDirect();

	@Override
	native public ByteOrder order();
}
