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
import com.jtransc.annotation.JTranscAddIncludes;
import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@JTranscAddMembers(target = "d", value = "static {% CLASS java.lang.Thread %} _dCurrentThread; Thread thread;")
@JTranscAddMembers(target = "cs", value = "System.Threading.Thread _cs_thread;")
@JTranscAddIncludes(target = "cpp", cond = "USE_BOOST", value = {"thread", "map", "boost/thread.hpp", "boost/chrono.hpp"})
@JTranscAddIncludes(target = "cpp", value = {"thread", "map"})
@JTranscAddMembers(target = "cpp", cond = "USE_BOOST", value = "boost::thread t_;")
@JTranscAddMembers(target = "cpp", cond = "!USE_BOOST", value = "std::thread t_;")
@JTranscAddMembers(target = "cpp", cond = "USE_BOOST", value = "static std::map<boost::thread::id, {% CLASS java.lang.Thread %}*> ###_cpp_threads;")
@JTranscAddMembers(target = "cpp", cond = "!USE_BOOST", value = "static std::map<std::thread::id, {% CLASS java.lang.Thread %}*> ###_cpp_threads;")
@HaxeAddMembers({
	"private static var threadsMap = new haxe.ds.ObjectMap<Dynamic, {% CLASS java.lang.Thread %}>();",
	"#if cpp var _cpp_thread: cpp.vm.Thread; #end",
})
public class Thread implements Runnable {
	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;

	//@JTranscMethodBody(target = "js", value = "return _jc.threadId;")
	//public static int currentThreadId() {return (int) currentThread().getId();}

	public static Thread currentThread() {
		lazyPrepareThread();
		Thread out = _getCurrentThreadOrNull();
		return (out != null) ? out : _mainThread;
	}

	@JTranscMethodBody(target = "d", value = {
		"if (_dCurrentThread is null) {",
		"	_dCurrentThread = new {% CLASS java.lang.Thread %}();",
		"}",
		"return _dCurrentThread;",
	})
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = "return _cpp_threads[boost::this_thread::get_id()];")
	@JTranscMethodBody(target = "cpp", value = "return _cpp_threads[std::this_thread::get_id()];")
	@HaxeMethodBody(target = "cpp", value = "return threadsMap.get(cpp.vm.Thread.current().handle);")
	@JTranscMethodBody(target = "js", value = {
		"{% if IS_JC %}return {% SMETHOD #CLASS:getThreadById %}({{ JC_COMMA }}_jc.threadId);{% else %}return {% SMETHOD #CLASS:getDefaultThread %}();{% end %}"
	})
	private static Thread _getCurrentThreadOrNull() {
		for (Thread t : getThreadsCopy()) return t; // Just valid for programs with just once thread
		return null;
	}

	private static Thread getThreadById(int id) {
		//JTranscConsole.log("getThreadById: " + id);
		return _threadsById.get((long)id);
	}

	private static Thread getDefaultThread() {
		lazyPrepareThread();
		return _mainThread;
	}

	public StackTraceElement[] getStackTrace() {
		return new Throwable().getStackTrace();
	}

	@SuppressWarnings("unused")
	@JTranscMethodBody(target = "d", value = "Thread.yield();")
	//@JTranscMethodBody(target = "cpp", value = "std::this_thread::yield();")
	public static void yield() {
		try {
			Thread.sleep(1L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0));")
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = "boost::this_thread::sleep_for(boost::chrono::milliseconds(p0));")
	@JTranscMethodBody(target = "cpp", value = "std::this_thread::sleep_for(std::chrono::milliseconds(p0));")
	//@JTranscMethodBody(target = "js", value = {
	//	"{% if IS_ASYNC %}return new Promise((resolve, reject) => { setTimeout(resolve, p0); });" +
	//	"{% else %}return {% SMETHOD com.jtransc.JTranscSystem:sleep %}(p0);" +
	//	"{% end %}"
	//}, async = true)
	@JTranscMethodBody(target = "js", cond = "IS_ASYNC", value = "return new Promise((resolve, reject) => { setTimeout(resolve, p0); });", async = true)
	public static void sleep(long millis) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	@JTranscMethodBody(target = "d", value = "Thread.sleep(dur!(\"msecs\")(p0) + dur!(\"nsecs\")(p1));")
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = "boost::this_thread::sleep_for(boost::chrono::milliseconds(p0));")
	@JTranscMethodBody(target = "cpp", value = "std::this_thread::sleep_for(std::chrono::milliseconds(p0));")
	//FIXME
	public static void sleep(long millis, int nanos) throws InterruptedException {
		JTranscSystem.sleep(millis);
	}

	public Thread() {
		this(null, null, null, 1024);
	}

	static private LinkedHashMap<Long, Thread> _threadsById;
	private ThreadGroup group;
	public String name;
	private long stackSize;
	private Runnable target;
	private int priority = MIN_PRIORITY;
	private int id;
	static private int lastId = 0;
	private UncaughtExceptionHandler uncaughtExceptionHandler = defaultUncaughtExceptionHandler;

	public Thread(Runnable target) {
		this(null, target, null, 1024);
	}

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
		this.group = (group != null) ? group : currentThread().getThreadGroup();
		this.target = target;
		this.id = lastId++;
		this.name = (name != null) ? name : ("thread-" + id++);
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

	private boolean _isAlive;

	static private final Object staticLock = new Object();
	static private ThreadGroup _mainThreadGroup = null;
	static private Thread _mainThread = null;

	synchronized static private Thread[] getThreadsCopy() {
		Collection<Thread> threads = getThreadSetInternal().values();
		synchronized (staticLock) {
			return threads.toArray(new Thread[0]);
		}
	}

	static private void lazyPrepareThread() {
		synchronized (staticLock) {
			if (_mainThreadGroup == null) {
				_mainThreadGroup = new ThreadGroup("main");
			}
			if (_mainThread == null) {
				_mainThread = new Thread(_mainThreadGroup, "main");
			}
			if (_threadsById == null) {
				_threadsById = new LinkedHashMap<>();
				_threadsById.put(_mainThread.getId(), _mainThread);
			}
		}
	}

	static private LinkedHashMap<Long, Thread> getThreadSetInternal() {
		lazyPrepareThread();
		return _threadsById;
	}

	public synchronized void start() {
		runInternalPreInit();
		_start(id);
	}

	@JTranscMethodBody(target = "d", value = "this.thread.start();")
	@JTranscMethodBody(target = "cs", value = {
		"_cs_thread = new System.Threading.Thread(new System.Threading.ThreadStart(delegate() { this{% IMETHOD java.lang.Thread:runInternal:()V %}();  }));",
		"_cs_thread.Start();",
	})
	@JTranscMethodBody(target = "cpp", cond = "USE_BOOST", value = {
		"t_ = std::thread(&{% SMETHOD java.lang.Thread:runInternalStatic:(Ljava/lang/Thread;)V %}, this);",
	})
	@JTranscMethodBody(target = "cpp", value = {
		"t_ = std::thread(&{% SMETHOD java.lang.Thread:runInternalStatic:(Ljava/lang/Thread;)V %}, this);",
	})
	@HaxeMethodBody(target = "cpp", value = "" +
		"var that = this;" +
		"cpp.vm.Thread.create(function():Void {" +
		"	that._cpp_thread = cpp.vm.Thread.current();" +
		"	that{% IMETHOD java.lang.Thread:runInternal:()V %}();" +
		"});"
	)
	@JTranscMethodBody(target = "js", value = {
		"{% if IS_JC %}this{% IMETHOD java.lang.Thread:runInternal:()V %}({ threadId: p0, global: _jc.global });{% else %}this{% IMETHOD java.lang.Thread:runInternal:()V %}();{% end %}"
	}) // NOTE: await missing intentionally
	@JTranscAsync
	private void _start(@SuppressWarnings("unused") int threadId) {
		System.err.println("WARNING: Threads not supported! Executing thread code in the parent's thread!");
		runInternal();
	}

	@SuppressWarnings("unused")
	private void runInternal() {
		try {
			runInternalInit();
			run();
		} catch (Throwable t) {
			uncaughtExceptionHandler.uncaughtException(this, t);
		} finally {
			runExit();
		}
	}

	@SuppressWarnings("unused")
	static private void runInternalStatic(Thread thread) {
		thread.runInternal();
	}


	@JTranscMethodBody(target = "cpp", value = "GC_init_pre_thread();")
	private void runInternalPreInitNative() {
	}

	private void runInternalPreInit() {
		runInternalPreInitNative();
		final LinkedHashMap<Long, Thread> set = getThreadSetInternal();
		synchronized (staticLock) {
			set.put(getId(), this);
			_isAlive = true;
		}
	}

	@JTranscMethodBody(target = "d", value = "_dCurrentThread = this;")
	@JTranscMethodBody(target = "cpp", value = "GC_init_thread(); _cpp_threads[t_.get_id()] = this;")
	@HaxeMethodBody(target = "cpp", value = "threadsMap.set(_cpp_thread.handle, this);")
	private void runInternalInit() {
	}

	@JTranscMethodBody(target = "cpp", value = "_cpp_threads.erase(t_.get_id()); GC_finish_thread();")
	@HaxeMethodBody(target = "cpp", value = "threadsMap.remove(_cpp_thread.handle);")
	private void runInternalExit() {
	}

	private void runExit() {
		final LinkedHashMap<Long, Thread> set = getThreadSetInternal();
		synchronized (this) {
			runInternalExit();
			set.remove(getId());
			_isAlive = false;
		}
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
		return Thread.currentThread().isInterrupted();
	}

	public boolean isInterrupted() {
		return false;
	}

	@Deprecated
	public void destroy() {
	}

	public final boolean isAlive() {
		//System.out.println("isAlive: " + _isAlive);
		return _isAlive;
	}

	@Deprecated
	native public final void suspend();

	@Deprecated
	native public final void resume();

	public final void setPriority(int newPriority) {
		this.priority = newPriority;
	}

	public final int getPriority() {
		return priority;
	}

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
		return getThreadsCopy().length;
	}

	public static int enumerate(Thread tarray[]) {
		int n = 0;
		for (Thread thread : getThreadsCopy()) {
			if (n >= tarray.length) break;
			tarray[n++] = thread;
		}
		return n;
	}

	@Deprecated
	public int countStackFrames() {
		return 0;
	}

	public final synchronized void join(long millis) throws InterruptedException {
		join(millis, 0);
	}

	public final synchronized void join(long millis, int nanos) throws InterruptedException {
		final long start = System.currentTimeMillis();
		while (isAlive()) {
			final long current = System.currentTimeMillis();
			final long elapsed = current - start;
			if (elapsed >= millis) break;
			Thread.sleep(1L);
		}
	}

	public final void join() throws InterruptedException {
		while (isAlive()) {
			Thread.sleep(1L);
		}
	}

	native public static void dumpStack();

	private boolean _isDaemon = false;

	@JTranscMethodBody(target = "d", value = "this.thread.isDaemon = p0;")
	public final void setDaemon(boolean on) {
		_isDaemon = on;
	}

	@JTranscMethodBody(target = "d", value = "return this.thread.isDaemon;")
	public final boolean isDaemon() {
		return _isDaemon;
	}

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

	//@JTranscMethodBody(target = "d", value = "return cast(long)this.thread.id;")
	public long getId() {
		return id;
	}

	public enum State {
		NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
	}

	public State getState() {
		return State.RUNNABLE;
	}

	public interface UncaughtExceptionHandler {
		void uncaughtException(Thread t, Throwable e);
	}

	static public UncaughtExceptionHandler defaultUncaughtExceptionHandler = (t, e) -> {
		System.out.println(t);
		System.out.println(e);
	};

	public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
		defaultUncaughtExceptionHandler = eh;
	}

	public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler() {
		return defaultUncaughtExceptionHandler;
	}

	public UncaughtExceptionHandler getUncaughtExceptionHandler() {
		return uncaughtExceptionHandler;
	}

	public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
		this.uncaughtExceptionHandler = eh;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
