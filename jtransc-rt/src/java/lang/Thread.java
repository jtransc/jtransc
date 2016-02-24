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

import jtransc.annotation.haxe.HaxeMethodBody;

import java.util.Map;

public class Thread implements Runnable {
	public final static int MIN_PRIORITY = 1;
	public final static int NORM_PRIORITY = 5;
	public final static int MAX_PRIORITY = 10;

	public static native Thread currentThread();

	native public static void yield();

	native public static void sleep(long millis) throws InterruptedException;

	native public static void sleep(long millis, int nanos) throws InterruptedException;

	public Thread() {
	}

	public Thread(Runnable target) {
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
	}

	public Thread(ThreadGroup group, Runnable target, String name) {
	}

	public Thread(ThreadGroup group, Runnable target, String name, long stackSize) {
	}

	native public synchronized void start();

	@Override
	native public void run();

	@Deprecated
	native public final void stop();

	@Deprecated
	native public final synchronized void stop(Throwable obj);

	native public void interrupt();

	native public static boolean interrupted();

	native public boolean isInterrupted();

	@Deprecated
	native public void destroy();

	native public final boolean isAlive();

	@Deprecated
	native public final void suspend();

	@Deprecated
	native public final void resume();

	native public final void setPriority(int newPriority);

	native public final int getPriority();

	native public final synchronized void setName(String name);

	native public final String getName();

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
			return "Thread[" + getName() + "," + getPriority() + "," +
				group.getName() + "]";
		} else {
			return "Thread[" + getName() + "," + getPriority() + "," +
				"" + "]";
		}
	}

	native public ClassLoader getContextClassLoader();

	native public void setContextClassLoader(ClassLoader cl);

	public static native boolean holdsLock(Object obj);

	native public StackTraceElement[] getStackTrace();

	native public static Map<Thread, StackTraceElement[]> getAllStackTraces();

	native public long getId();

	public enum State {NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED;}

	native public State getState();

	public interface UncaughtExceptionHandler {
		void uncaughtException(Thread t, Throwable e);
	}

	native public static void setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler eh);

	native public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler();

	native public UncaughtExceptionHandler getUncaughtExceptionHandler();

	native public void setUncaughtExceptionHandler(UncaughtExceptionHandler eh);
}
