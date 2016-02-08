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

public class ThreadGroup implements Thread.UncaughtExceptionHandler {
	private ThreadGroup() {
	}

	public ThreadGroup(String name) {

	}

	public ThreadGroup(ThreadGroup parent, String name) {

	}

	private ThreadGroup(Void unused, ThreadGroup parent, String name) {
	}

	native public final String getName();

	native public final ThreadGroup getParent();

	native public final int getMaxPriority();

	native public final boolean isDaemon();

	native public synchronized boolean isDestroyed();

	native public final void setDaemon(boolean daemon);

	native public final void setMaxPriority(int pri);

	native public final boolean parentOf(ThreadGroup g);

	native public final void checkAccess();

	native public int activeCount();

	native public int enumerate(Thread list[]);

	native public int enumerate(Thread list[], boolean recurse);

	native public int activeGroupCount();

	native public int enumerate(ThreadGroup list[]);

	native public int enumerate(ThreadGroup list[], boolean recurse);

	@Deprecated
	native public final void stop();

	native public final void interrupt();

	@Deprecated
	@SuppressWarnings("deprecation")
	native public final void suspend();


	@Deprecated
	@SuppressWarnings("deprecation")
	native public final void resume();

	native public final void destroy();

	native public void list();

	native public void uncaughtException(Thread t, Throwable e);

	@Deprecated
	native public boolean allowThreadSuspension(boolean b);

	public String toString() {
		return getClass().getName() + "[name=" + getName() + ",maxpri=" + getMaxPriority() + "]";
	}
}
