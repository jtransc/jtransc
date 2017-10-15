package java.util.concurrent;

import java.util.Collection;

public class Semaphore {
	private int permits;
	private final boolean fair;

	public Semaphore(int permits) {
		this(permits, false);
	}

	public Semaphore(int permits, boolean fair) {
		this.permits = permits;
		this.fair = fair;
	}

	public boolean isFair() {
		return fair;
	}

	native public void acquire() throws InterruptedException;
	native public void acquireUninterruptibly();
	native public boolean tryAcquire();
	native public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;
	native public void release();
	native public void acquire(int permits) throws InterruptedException;
	native public void acquireUninterruptibly(int permits);
	native public boolean tryAcquire(int permits);
	native public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException;
	native public void release(int permits);
	native public int availablePermits();
	native public int drainPermits();
	native public final boolean hasQueuedThreads();
	native public final int getQueueLength();

	native protected void reducePermits(int reduction);
	native protected Collection<Thread> getQueuedThreads();

	public String toString() {
		return super.toString() + "[Permits = " + permits + "]";
	}
}
