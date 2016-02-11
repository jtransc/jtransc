package jtransc.game.util;

import java.util.ArrayList;

public class Signal<T> {
	ArrayList<SignalHandler<T>> handlers = new ArrayList<SignalHandler<T>>();

	public void add(SignalHandler<T> value) {
		handlers.add(value);
	}

	public void remove(SignalHandler<T> value) {
		handlers.remove(value);
	}

	public void dispatch(T value) {
		for (SignalHandler<T> handler : handlers) {
			handler.handle(value);
		}
	}
}
