package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AsynchronousFileChannel implements AsynchronousChannel {
	protected AsynchronousFileChannel() {
	}

	native public static AsynchronousFileChannel open(Path file, Set<? extends OpenOption> options, ExecutorService executor, FileAttribute<?>... attrs) throws IOException;

	@SuppressWarnings({"unchecked", "rawtypes"}) // generic array construction
	private static final FileAttribute<?>[] NO_ATTRIBUTES = new FileAttribute[0];

	native public static AsynchronousFileChannel open(Path file, OpenOption... options) throws IOException;

	public native long size() throws IOException;

	public native AsynchronousFileChannel truncate(long size) throws IOException;

	public native void force(boolean metaData) throws IOException;

	public native <A> void lock(long position, long size, boolean shared, A attachment, CompletionHandler<FileLock, ? super A> handler);

	public final <A> void lock(A attachment, CompletionHandler<FileLock, ? super A> handler) {
		lock(0L, Long.MAX_VALUE, false, attachment, handler);
	}

	public native Future<FileLock> lock(long position, long size, boolean shared);

	public final Future<FileLock> lock() {
		return lock(0L, Long.MAX_VALUE, false);
	}

	public native FileLock tryLock(long position, long size, boolean shared) throws IOException;

	native public final FileLock tryLock() throws IOException;

	public native <A> void read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler);

	public native Future<Integer> read(ByteBuffer dst, long position);

	public native <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler);

	public native Future<Integer> write(ByteBuffer src, long position);

	native public boolean isOpen();

	native public void close() throws IOException;
}
