package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public abstract class AbstractSelectableChannel extends SelectableChannel {
	protected AbstractSelectableChannel(SelectorProvider provider) {
		throw new RuntimeException("Not implemented");
	}

	native public final SelectorProvider provider();

	native public final boolean isRegistered();

	native public final SelectionKey keyFor(Selector sel);

	native public final SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException;

	native protected final void implCloseChannel() throws IOException;

	protected abstract void implCloseSelectableChannel() throws IOException;

	native public final boolean isBlocking();

	native public final Object blockingLock();

	public native final SelectableChannel configureBlocking(boolean block) throws IOException;

	protected abstract void implConfigureBlocking(boolean block) throws IOException;
}
