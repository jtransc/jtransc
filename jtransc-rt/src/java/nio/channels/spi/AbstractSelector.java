package java.nio.channels.spi;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public abstract class AbstractSelector extends Selector {
	protected AbstractSelector(SelectorProvider provider) {
		
	}

	native public final void close() throws IOException;

	protected abstract void implCloseSelector() throws IOException;

	native public final boolean isOpen();

	native public final SelectorProvider provider();

	native protected final Set<SelectionKey> cancelledKeys();

	protected abstract SelectionKey register(AbstractSelectableChannel ch, int ops, Object att);

	native protected final void deregister(AbstractSelectionKey key);

	native protected final void begin();

	native protected final void end();

}
