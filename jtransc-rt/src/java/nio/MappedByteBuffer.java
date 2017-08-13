package java.nio;

import java.io.FileDescriptor;

public abstract class MappedByteBuffer extends ByteBuffer {
	MappedByteBuffer(int mark, int pos, int lim, int cap, FileDescriptor fd) {
		super(null);
		throw new RuntimeException("Not implemented");
	}

	MappedByteBuffer(int mark, int pos, int lim, int cap) {
		super(null);
		throw new RuntimeException("Not implemented");
	}

	native public final boolean isLoaded();

	native public final MappedByteBuffer load();

	native public final MappedByteBuffer force();
}
