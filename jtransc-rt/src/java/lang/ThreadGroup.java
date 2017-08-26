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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;

public class ThreadGroup implements Thread.UncaughtExceptionHandler {
	private ThreadGroup parent;
	private String name;
	private LinkedHashSet<Thread> threads;
	private LinkedHashSet<ThreadGroup> children;
	private int maxPriority = Thread.MAX_PRIORITY;
	private boolean isDaemon = false;
	private boolean isDestroyed = false;

	synchronized private Thread[] getThreadsCopy() {
		return (threads != null) ? threads.toArray(new Thread[0]) : new Thread[0];
	}

	synchronized private ArrayList<Thread> getAllThreads(ArrayList<Thread> out) {
		for (Thread thread : getThreadsCopy()) out.add(thread);
		for (ThreadGroup group : getChildrenCopy()) group.getAllThreads(out);
		return out;
	}

	synchronized private ArrayList<Thread> getAllThreads() {
		return getAllThreads(new ArrayList<>());
	}

	synchronized private ThreadGroup[] getChildrenCopy() {
		return (children != null) ? children.toArray(new ThreadGroup[0]) : new ThreadGroup[0];
	}

	synchronized private ArrayList<ThreadGroup> getAllChildren(ArrayList<ThreadGroup> out) {
		out.add(this);
		for (ThreadGroup group : getChildrenCopy()) group.getAllChildren(out);
		return out;
	}

	synchronized private ArrayList<ThreadGroup> getAllChildren() {
		return getAllChildren(new ArrayList<>());
	}

	private ThreadGroup() {
		this("ThreadGroup");
	}

	public ThreadGroup(String name) {
		this(null, name);
	}

	public ThreadGroup(ThreadGroup parent, String name) {
		this.parent = parent;
		this.name = (name != null) ? name : "ThreadGroup";
	}

	public final String getName() {
		return name;
	}

	public final ThreadGroup getParent() {
		return parent;
	}

	public final int getMaxPriority() {
		return maxPriority;
	}

	public final boolean isDaemon() {
		return this.isDaemon;
	}

	public synchronized boolean isDestroyed() {
		return isDestroyed;
	}

	public final void setDaemon(boolean daemon) {
		this.isDaemon = daemon;
	}

	public final void setMaxPriority(int priority) {
		this.maxPriority = priority;
	}

	public final boolean parentOf(ThreadGroup g) {
		return (g == this.parent) || ((this.parent != null) && this.parent.parentOf(g));
	}

	public final void checkAccess() {
	}

	public int activeGroupCount() {
		int count = 1;
		for (ThreadGroup child : getChildrenCopy()) {
			count += child.activeGroupCount();
		}
		return count;
	}

	public int activeCount() {
		return getAllThreads().size();
	}

	public int enumerate(Thread list[]) {
		return enumerate(list, true);
	}

	public int enumerate(ThreadGroup list[]) {
		return enumerate(list, true);
	}

	public int enumerate(Thread list[], boolean recurse) {
		int n = 0;
		for (Thread item : recurse ? getAllThreads() : Arrays.asList(getThreadsCopy())) {
			if (n >= list.length) break;
			list[n++] = item;
		}
		return n;
	}

	public int enumerate(ThreadGroup list[], boolean recurse) {
		int n = 0;
		for (ThreadGroup item : recurse ? getAllChildren() : Arrays.asList(getChildrenCopy())) {
			if (n >= list.length) break;
			list[n++] = item;
		}
		return n;
	}

	@Deprecated
	public final void stop() {
		for (Thread thread : getThreadsCopy()) thread.stop();
		for (ThreadGroup group : getChildrenCopy()) group.stop();
	}

	public final void interrupt() {
		for (Thread thread : getThreadsCopy()) thread.interrupt();
		for (ThreadGroup group : getChildrenCopy()) group.interrupt();
	}

	@Deprecated
	@SuppressWarnings("deprecation")
	public final void suspend() {
		for (Thread thread : getThreadsCopy()) thread.suspend();
		for (ThreadGroup group : getChildrenCopy()) group.suspend();
	}

	@Deprecated
	@SuppressWarnings("deprecation")
	public final void resume() {
		for (Thread thread : getThreadsCopy()) thread.resume();
		for (ThreadGroup group : getChildrenCopy()) group.resume();
	}

	synchronized public final void destroy() {
		isDestroyed = true;
		for (Thread thread : getThreadsCopy()) thread.destroy();
		for (ThreadGroup group : getChildrenCopy()) group.destroy();
	}

	public void list() {
		System.out.println("Unimplemented ThreadGroup.list()");
	}

	native public void uncaughtException(Thread t, Throwable e);

	@Deprecated
	public boolean allowThreadSuspension(boolean b) {
		return false;
	}

	public String toString() {
		return getClass().getName() + "[name=" + getName() + ",maxpri=" + getMaxPriority() + "]";
	}
}
