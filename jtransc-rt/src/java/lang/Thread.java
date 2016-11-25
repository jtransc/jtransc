/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.thread.JTranscThreading;

import java.util.HashMap;
import java.util.Map;

public class Thread implements Runnable {
	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;

	static private Thread _currentThread;

	public static Thread currentThread() {
		if (_currentThread == null) {
			_currentThread = new Thread();
		}
		return _currentThread;
	}

	public StackTraceElement[] getStackTrace() {
		StackTraceElement[] stackTrace = _getStackTrace();
		if ((stackTrace == null) || (stackTrace.length == 0)) {
			return new StackTraceElement[]{
				new StackTraceElement("Dummy", "dummy", "Dummy.java", 1),
				new StackTraceElement("Dummy", "dummy", "Dummy.java", 1),
				new StackTraceElement("Dummy", "dummy", "Dummy.java", 1)
			};
		} else {
			return stackTrace;
		}
	}

	static private final StackTraceElement[] ST_NULL = null;

	@HaxeMethodBody("return N.getStackTrace(2);")
	@JTranscMethodBody(target = "js", value = "return N.getStackTrace(2);")
	private StackTraceElement[] _getStackTrace() {
		return ST_NULL;
	}

	public static void yield() {

	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0));")
	public static void sleep(long millis) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0) + dur!(\"nsecs\")(p1));")
	public static void sleep(long millis, int nanos) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	public Thread() {
	}

	public String name;
	public long _data;
	public boolean _isAlive;
	private Runnable target;

	public Thread(Runnable target) {
		this.target = target;
	}

	//Thread(Runnable target, AccessControlContext acc) {
	//}

	public Thread(ThreadGroup group, Runnable target) {
	}

	public Thread(String name) {
	}

	public Thread(ThreadGroup group, String name) {
	}

	public Thread(Runnable target, String name) {
		this.target = target;
	}

	public Thread(ThreadGroup group, Runnable target, String name) {
		this.target = target;
	}

	public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
		this.target = target;
	}

	public synchronized void start() {
		JTranscThreading.impl.start(this);
	}

	@Override
	public void run() {
		if (this.target != null) {
			this.target.run();
		}
	}

	@Deprecated
	native public final void stop();

	@Deprecated
	native public final synchronized void stop(Throwable obj);

	native public void interrupt();

	native public static boolean interrupted();

	native public boolean isInterrupted();

	@Deprecated
	native public void destroy();

	public final boolean isAlive() {
		return JTranscThreading.impl.isAlive(this);
	}

	@Deprecated
	native public final void suspend();

	@Deprecated
	native public final void resume();

	native public final void setPriority(int newPriority);

	native public final int getPriority();

	public final synchronized void setName(String name) {
		this.name = name;
	}

	public final String getName() {
		return this.name;
	}

	native public final ThreadGroup getThreadGroup();

	native public static int activeCount();

	native public static int enumerate(Thread tarray[]);

	@Deprecated
	native public int countStackFrames();

	native public final synchronized void join(long millis) throws InterruptedException;

	native public final synchronized void join(long millis, int nanos) throws InterruptedException;

	native public final void join() throws InterruptedException;

	native public static void dumpStack();

	native public final void setDaemon(boolean on);

	native public final boolean isDaemon();

	native public final void checkAccess();

	public String toString() {
		ThreadGroup group = getThreadGroup();
		if (group != null) {
			return "Thread[" + getName() + "," + getPriority() + "," + group.getName() + "]";
		} else {
			return "Thread[" + getName() + "," + getPriority() + "," + "]";
		}
	}

	private ClassLoader classLoader = null;

	public ClassLoader getContextClassLoader() {
		if (this.classLoader == null) {
			this.classLoader = _ClassInternalUtils.getSystemClassLoader();
		}
		return this.classLoader;
	}

	public void setContextClassLoader(ClassLoader cl) {
		this.classLoader = cl;
	}

	public static boolean holdsLock(Object obj) {
		return false;
	}

	public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
		return new HashMap<>();
	}

	public long getId() {
		return 0;
	}

	public enum State {NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED}

	public State getState() {
		return State.RUNNABLE;
	}

	public interface UncaughtExceptionHandler {
		void uncaughtException(Thread t, Throwable e);
	}

	native public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh);

	native public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler();

	native public UncaughtExceptionHandler getUncaughtExceptionHandler();

	native public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh);
}
