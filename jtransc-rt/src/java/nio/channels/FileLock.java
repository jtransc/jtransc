package java.nio.channels;

import java.io.IOException;

public abstract class FileLock implements AutoCloseable {
	protected FileLock(FileChannel channel, long position, long size, boolean shared) {
	}

	protected FileLock(AsynchronousFileChannel channel, long position, long size, boolean shared) {
	}

	native public final FileChannel channel();

	native public Channel acquiredBy();

	native public final long position();

	native public final long size();

	native public final boolean isShared();

	native public final boolean overlaps(long position, long size);

	public abstract boolean isValid();

	public abstract void release() throws IOException;

	native public final void close() throws IOException;

	native public final String toString();
}
