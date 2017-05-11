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
import com.jtransc.annotation.JTranscAddHeader;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.thread.JTranscThreading;

import java.util.HashMap;
import java.util.Map;

@JTranscAddMembers(target = "d", value = {
	"static {% CLASS java.lang.Thread %} _dCurrentThread;",
	"Thread thread;",
})

@JTranscAddHeader(target = "cpp", value = {
	"#include <boost/thread.hpp>"
})

@JTranscAddMembers(target = "cpp", value = {
	"boost::thread t_;"
})

public class Thread implements Runnable {
	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;

	static private Thread _currentThread;

	@JTranscMethodBody(target = "d", value = {
		"if (_dCurrentThread is null) {",
		"	_dCurrentThread = new {% CLASS java.lang.Thread %}();",
		"}",
		"return _dCurrentThread;",
	})
	//@JTranscMethodBody(target = "cpp", value = {
	//	"return boost::this_thread;",
	//})

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
	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", value = "return N.getStackTrace(2);"),
		@JTranscMethodBody(target = "cs", value = "return N.getStackTrace(2);"),
	})
	private StackTraceElement[] _getStackTrace() {
		return null;
	}

	@JTranscMethodBody(target = "d", value = "Thread.yield();")
	@JTranscMethodBody(target = "cpp", value = "boost::this_thread::yield();")
	public static void yield() {
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0));")
	@JTranscMethodBody(target = "cpp", value = "boost::this_thread::sleep_for(boost::chrono::microseconds(p0));")
	public static void sleep(long millis) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0) + dur!(\"nsecs\")(p1));")
	@JTranscMethodBody(target = "cpp", value = "boost::this_thread::sleep_for(boost::chrono::microseconds(p0));")
	//FIXME
	public static void sleep(long millis, int nanos) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	public Thread() {
	}

	private ThreadGroup group;
	public String name;
	private long stackSize;
	public long _data;
	public boolean _isAlive;
	private Runnable target;

	public Thread(Runnable target) {
		this(null, target, null, 1024);
	}

	//Thread(Runnable target, AccessControlContext acc) {
	//}

	public Thread(ThreadGroup group, Runnable target) {
		this(group, target, null, 1024);
	}

	public Thread(String name) {
		this(null, null, name, 1024);
	}

	public Thread(ThreadGroup group, String name) {
		this(group, null, name, 1024);
	}

	public Thread(Runnable target, String name) {
		this(null, target, name, 1024);
	}

	public Thread(ThreadGroup group, Runnable target, String name) {
		this(group, target, name, 1024);
	}

	public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
		this.group = group;
		this.target = target;
		this.name = name;
		this.stackSize = stackSize;
		_init();
	}

	@JTranscMethodBody(target = "d", value = {
		"this.thread = new Thread(delegate () {",
		"	{% METHOD java.lang.Thread:runInternal:()V %}();",
		"});",
	})
	private void _init() {
	}

	@JTranscMethodBody(target = "d", value = "this.thread.start();")
	@JTranscMethodBody(target = "cpp", value = "t_ = boost::thread(&{% CLASS java.lang.Thread:runInternal %}::{% METHOD java.lang.Thread:runInternal:()V %}, this);")

	public synchronized void start() {
		JTranscThreading.impl.start(this);
	}


	@SuppressWarnings("unused")
	private void runInternal() {
		runInternalInit();
		run();
	}

	@JTranscMethodBody(target = "d", value = {
		"_dCurrentThread = this;",
	})
	private void runInternalInit() {
	}

	@Override
	public void run() {
		if (this.target != null) {
			this.target.run();
		}
	}

	@Deprecated
	@JTranscMethodBody(target = "d", value = "this.thread.stop();")
	native public final void stop();

	@Deprecated
	public final synchronized void stop(Throwable obj) {
	}

	public void interrupt() {

	}

	public static boolean interrupted() {
		return false;
	}

	public boolean isInterrupted() {
		return false;
	}

	@Deprecated
	public void destroy() {
	}

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

	public final ThreadGroup getThreadGroup() {
		return group;
	}

	public static int activeCount() {
		return 1;
	}

	native public static int enumerate(Thread tarray[]);

	@Deprecated
	native public int countStackFrames();

	native public final synchronized void join(long millis) throws InterruptedException;

	native public final synchronized void join(long millis, int nanos) throws InterruptedException;

	native public final void join() throws InterruptedException;

	native public static void dumpStack();

	@JTranscMethodBody(target = "d", value = "this.thread.isDaemon = p0;")
	native public final void setDaemon(boolean on);

	@JTranscMethodBody(target = "d", value = "return this.thread.isDaemon;")
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
		return new HashMap<Thread, StackTraceElement[]>();
	}

	@JTranscMethodBody(target = "d", value = "return this.thread.id;")
	public long getId() {
		return 0L;
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
