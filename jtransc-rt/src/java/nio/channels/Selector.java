package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public abstract class Selector implements Closeable {
	protected Selector() {
	}

	public static Selector open() throws IOException {
		return SelectorProvider.provider().openSelector();
	}

	public abstract boolean isOpen();

	public abstract SelectorProvider provider();

	public abstract Set<SelectionKey> keys();

	public abstract Set<SelectionKey> selectedKeys();

	public abstract int selectNow() throws IOException;

	public abstract int select(long timeout) throws IOException;

	public abstract int select() throws IOException;

	public abstract Selector wakeup();

	public abstract void close() throws IOException;
}
