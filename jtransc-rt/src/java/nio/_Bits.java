package java.nio;

class _Bits {
	static void checkBounds(int off, int len, int size) { // package-private
		if ((off | len | (off + len) | (size - (off + len))) < 0)
			throw new IndexOutOfBoundsException();
	}
}
