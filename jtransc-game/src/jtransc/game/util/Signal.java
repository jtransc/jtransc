package jtransc.game.util;

import java.util.ArrayList;

public class Signal<T> {
	public interface Handler<T> {
		void handle(T value);
	}

	ArrayList<Handler<T>> handlers = null;

	public void add(Handler<T> value) {
        if (handlers == null) {
            handlers = new ArrayList<Handler<T>>();
        }
		handlers.add(value);
	}

	public void remove(Handler<T> value) {
        if (handlers != null) {
            handlers.remove(value);
        }
	}

	public void dispatch(T value) {
        if (handlers != null) {
            for (Handler<T> handler : handlers) {
                handler.handle(value);
            }
        }
	}
}
