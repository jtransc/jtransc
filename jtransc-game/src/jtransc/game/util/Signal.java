package jtransc.game.util;

import java.util.ArrayList;

public class Signal<T> {
	ArrayList<SignalHandler<T>> handlers = null;

	public void add(SignalHandler<T> value) {
        if (handlers == null) {
            handlers = new ArrayList<SignalHandler<T>>();
        }
		handlers.add(value);
	}

	public void remove(SignalHandler<T> value) {
        if (handlers != null) {
            handlers.remove(value);
        }
	}

	public void dispatch(T value) {
        if (handlers != null) {
            for (SignalHandler<T> handler : handlers) {
                handler.handle(value);
            }
        }
	}
}
