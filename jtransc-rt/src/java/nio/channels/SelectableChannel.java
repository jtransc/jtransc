package java.nio.channels;

import java.io.IOException;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.channels.spi.SelectorProvider;

public abstract class SelectableChannel extends AbstractInterruptibleChannel implements Channel {
	protected SelectableChannel() {
	}

	public abstract SelectorProvider provider();

	public abstract int validOps();

	public abstract boolean isRegistered();

	public abstract SelectionKey keyFor(Selector sel);

	public abstract SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException;

	public final SelectionKey register(Selector sel, int ops) throws ClosedChannelException {
		return register(sel, ops, null);
	}

	public abstract SelectableChannel configureBlocking(boolean block) throws IOException;

	public abstract boolean isBlocking();

	public abstract Object blockingLock();
}
