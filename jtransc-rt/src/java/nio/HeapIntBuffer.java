package java.nio;

class HeapIntBuffer extends IntBuffer {
	HeapIntBuffer(int mark, int pos, int lim, int cap, int[] hb, int offset) {
		super(mark, pos, lim, cap, hb, offset);
	}

	HeapIntBuffer(int mark, int pos, int lim, int cap) {
		super(mark, pos, lim, cap);
	}

	@Override
	native public IntBuffer slice();

	@Override
	native public IntBuffer duplicate();

	@Override
	native public IntBuffer asReadOnlyBuffer();

	@Override
	native public int get();

	@Override
	native public IntBuffer put(int i);

	@Override
	native public int get(int index);

	@Override
	native public IntBuffer put(int index, int i);

	@Override
	native public IntBuffer compact();

	@Override
	native public boolean isReadOnly();

	@Override
	native public boolean isDirect();

	@Override
	native public ByteOrder order();
}
