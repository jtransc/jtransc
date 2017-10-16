package java.util.concurrent;

import com.jtransc.io.JTranscConsole;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class Semaphore {
	private int permits;
	private final boolean fair;
	Queue<Thread> threadQueue = new LinkedList<>();

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

	protected void reducePermits(int reduction) {
		this.permits -= reduction;
	}

	public int drainPermits() {
		int permits = this.permits;
		try {
			acquire(permits);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return permits;
	}

	public void acquire(int permits) throws InterruptedException {
		while (true) {
			//JTranscConsole.log("acquire: " + permits);
			synchronized (this) {
				if (this.permits >= permits) {
					this.permits -= permits;
					return;
				}
			}

			Thread currentThread = Thread.currentThread();
			threadQueue.add(currentThread);
			try {
				//JTranscConsole.log(777700033);
				this.wait();
			} finally {
				threadQueue.remove(currentThread);
			}
		}
	}

	public void release(int permits) {
		//JTranscConsole.log("release: " + permits);
		this.permits += permits;
		this.notifyAll();
	}

	public void acquire() throws InterruptedException {
		acquire(1);
	}

	public void release() {
		release(1);
	}

	public void acquireUninterruptibly(int permits) {
		while (true) {
			try {
				acquire(permits);
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	native public boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException;

	public void acquireUninterruptibly() {
		acquireUninterruptibly(1);
	}

	public boolean tryAcquire() {
		return tryAcquire(1);
	}

	public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
		return tryAcquire(1, timeout, unit);
	}

	public boolean tryAcquire(int permits) {
		while (true) {
			try {
				return tryAcquire(permits, 0L, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public int availablePermits() {
		return this.permits;
	}

	public final boolean hasQueuedThreads() {
		return getQueueLength() > 0;
	}

	public final int getQueueLength() {
		return threadQueue.size();
	}

	protected Collection<Thread> getQueuedThreads() {
		return threadQueue;
	}

	public String toString() {
		return super.toString() + "[Permits = " + permits + "]";
	}
}
