package java.nio.channels.spi;

import java.nio.channels.SelectionKey;

public abstract class AbstractSelectionKey extends SelectionKey {
	protected AbstractSelectionKey() {
	}

	native public final boolean isValid();

	native public final void cancel();
}
