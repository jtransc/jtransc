package java.util.concurrent.locks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ReentrantLock implements Lock, java.io.Serializable {
	public ReentrantLock() {
	}

	public ReentrantLock(boolean fair) {
	}

	public void lock() {

	}

	public void lockInterruptibly() throws InterruptedException {
	}

	public boolean tryLock() {
		return true;
	}

	public boolean tryLock(long timeout, TimeUnit unit) {
		return true;
	}

	public void unlock() {

	}

	native public Condition newCondition();

	public int getHoldCount() {
		return 0;
	}

	public boolean isHeldByCurrentThread() {
		return false;
	}

	public boolean isLocked() {
		return false;
	}

	public final boolean isFair() {
		return true;
	}

	protected Thread getOwner() {
		return Thread.currentThread();
	}

	public final boolean hasQueuedThreads() {
		return false;
	}

	public final boolean hasQueuedThread(Thread thread) {
		return false;
	}

	public final int getQueueLength() {
		return 0;
	}

	protected Collection<Thread> getQueuedThreads() {
		return new ArrayList<Thread>();
	}

	public boolean hasWaiters(Condition condition) {
		return false;
	}

	public int getWaitQueueLength(Condition condition) {
		return 0;
	}

	protected Collection<Thread> getWaitingThreads(Condition condition) {
		return new ArrayList<Thread>();
	}

	public String toString() {
		Thread o = Thread.currentThread();
		return super.toString() + ((o == null) ?
			"[Unlocked]" :
			"[Locked by thread " + o.getName() + "]");
	}
}
