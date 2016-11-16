package java.awt;

import java.lang.reflect.InvocationTargetException;
import java.util.EmptyStackException;

public class EventQueue {
	public EventQueue() {

	}

	public void postEvent(AWTEvent theEvent) {

	}

	public AWTEvent getNextEvent() throws InterruptedException {
		return null;
	}

	public AWTEvent peekEvent() {
		return null;
	}

	public AWTEvent peekEvent(int id) {
		return null;
	}

	public static long getMostRecentEventTime() {
		return 0L;
	}

	public static AWTEvent getCurrentEvent() {
		return null;
	}

	public void push(EventQueue newEventQueue) {

	}

	protected void pop() throws EmptyStackException {
	}

	public SecondaryLoop createSecondaryLoop() {
		return null;
	}

	public static boolean isDispatchThread() {
		return false;
	}

	public static void invokeLater(Runnable runnable) {

	}

	public static void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException {
	}


}