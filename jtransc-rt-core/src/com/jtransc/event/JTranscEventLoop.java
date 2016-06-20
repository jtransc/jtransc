package com.jtransc.event;

final public class JTranscEventLoop {
	native static public void frame();
	native static public Timer setImmediate(Runnable task);
	native static public Timer setTimeout(Runnable task, int ms);
	native static public Timer setInterval(Runnable task, int ms);

	static public final class Timer {
		native public void cancel();
	}
}
