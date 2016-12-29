package java.nio.channels;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public interface AsynchronousByteChannel extends AsynchronousChannel {
	<A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler);

	Future<Integer> read(ByteBuffer dst);

	<A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler);

	Future<Integer> write(ByteBuffer src);
}
