package java.util.concurrent;

import java.util.Timer;
import java.util.TimerTask;

public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

	private Timer timer;

	public ScheduledThreadPoolExecutor(int corePoolSize) {
		this(corePoolSize, null, null);
	}

	public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
		this(corePoolSize, threadFactory, null);
	}

	public ScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
		this(corePoolSize, null, handler);
	}

	public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		super(corePoolSize, Integer.MAX_VALUE, 0, TimeUnit.NANOSECONDS, new DelayedWorkQueue(), threadFactory, handler);
		timer = new Timer("Scheduler-timer-");
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		ScheduledFutureTask<Object> f = new ScheduledFutureTask(command);
		timer.schedule(f, unit.toMillis(delay));
		return f;
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		ScheduledFutureTask<V> f = new ScheduledFutureTask(callable);
		timer.schedule(f, unit.toMillis(delay));
		return f;
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		ScheduledFutureTask<Object> f = new ScheduledFutureTask(command);
		timer.scheduleAtFixedRate(f, unit.toMillis(initialDelay), unit.toMillis(period));
		return f;
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		ScheduledFutureTask<Object> f = new ScheduledFutureTask(command);
		timer.scheduleAtFixedRate(f, unit.toMillis(initialDelay), unit.toMillis(delay));
		return f;
	}

	static private class DelayedWorkQueue extends SynchronousQueue {
	}

	private class ScheduledFutureTask<V> extends TimerTask implements ScheduledFuture<V> {
		Runnable runnable;
		Callable<V> callable;
		V res;
		boolean done = false;
		boolean cancelled = false;

		ScheduledFutureTask(Runnable r) {
			runnable = r;
		}

		ScheduledFutureTask(Callable c) {
			callable = c;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return 0;
		}

		@Override
		public int compareTo(Delayed o) {
			return 0;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			cancelled = true;
			return super.cancel();
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public boolean isDone() {
			return done;
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			if (callable == null) return null;
			while (res == null) {
				Thread.sleep(100);
			}
			return res;
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if (callable == null) return null;
			long time = unit.toMillis(timeout);
			while (res == null && time > 0) {
				time -= 100;
				Thread.sleep(100);
			}
			return res;
		}

		@Override
		public void run() {
			if (runnable != null) {
				execute(runnable);
			}

			if (callable != null) {
				execute(new Runnable() {
					@Override
					public void run() {
						try {
							res = callable.call();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
			done = true;
		}
	}
}
