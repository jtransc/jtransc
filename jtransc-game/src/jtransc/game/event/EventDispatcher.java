package jtransc.game.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventDispatcher {
    public interface Handler<T> {
        void handle(T value);
    }
    
    private Map<Class<Object>, ArrayList<Handler<Object>>> eventHandlers = new HashMap<Class<Object>, ArrayList<Handler<Object>>>(); 

    public <T> void addEventListener(Class<T> clazz, Handler<T> handler) {
        if (!eventHandlers.containsKey(clazz)) {
            eventHandlers.put((Class<Object>) clazz, new ArrayList<Handler<Object>>());
        }
        eventHandlers.get((Class<Object>) clazz).add((Handler<Object>) handler);
    }

    public <T> void removeEventListener(Class<T> clazz, Handler<T> handler) {
        if (!eventHandlers.containsKey(clazz)) return;
        eventHandlers.get(clazz).remove(handler);
    }

    public void dispatchEvent(Event event) {
        ArrayList<Handler<Object>> handlers = eventHandlers.get(event.getClass());
        if (handlers != null) {
            for (Handler<Object> objectHandler : handlers) {
                objectHandler.handle(event);
            }
        }
    }
}
