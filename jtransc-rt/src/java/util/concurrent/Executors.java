package java.util.concurrent;

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Executors {
	private Executors() {
	}

	public static ExecutorService newFixedThreadPool(int nThreads) {
		return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
		return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
	}

	public static ExecutorService newSingleThreadExecutor() {
		return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()));
	}

	public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
		return new FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory));
	}

	public static ExecutorService newCachedThreadPool() {
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
		return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
	}

	public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
		return new DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1));
	}

	public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
		return new DelegatedScheduledExecutorService(new ScheduledThreadPoolExecutor(1, threadFactory));
	}

	public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
		return new ScheduledThreadPoolExecutor(corePoolSize);
	}

	public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
		return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
	}

	public static ExecutorService unconfigurableExecutorService(ExecutorService executor) {
		Objects.requireNonNull(executor);
		return new DelegatedExecutorService(executor);
	}

	public static ScheduledExecutorService unconfigurableScheduledExecutorService(ScheduledExecutorService executor) {
		Objects.requireNonNull(executor);
		return new DelegatedScheduledExecutorService(executor);
	}

	public static ThreadFactory defaultThreadFactory() {
		return new DefaultThreadFactory();
	}

	public static ThreadFactory privilegedThreadFactory() {
		return new DefaultThreadFactory();
	}

	public static <T> Callable<T> callable(Runnable task, T result) {
		Objects.requireNonNull(task);
		return new RunnableAdapter<T>(task, result);
	}

	public static Callable<Object> callable(Runnable task) {
		Objects.requireNonNull(task);
		return new RunnableAdapter<Object>(task, null);
	}

	public static Callable<Object> callable(final PrivilegedAction<?> action) {
		Objects.requireNonNull(action);
		return new Callable<Object>() {
			public Object call() {
				return action.run();
			}
		};
	}

	public static Callable<Object> callable(final PrivilegedExceptionAction<?> action) {
		Objects.requireNonNull(action);
		return new Callable<Object>() {
			public Object call() throws Exception {
				return action.run();
			}
		};
	}

	public static <T> Callable<T> privilegedCallable(Callable<T> callable) {
		Objects.requireNonNull(callable);
		return callable;
	}

	public static <T> Callable<T> privilegedCallableUsingCurrentClassLoader(Callable<T> callable) {
		Objects.requireNonNull(callable);
		return callable;
	}

	static final class RunnableAdapter<T> implements Callable<T> {
		final Runnable task;
		final T result;

		RunnableAdapter(Runnable task, T result) {
			this.task = task;
			this.result = result;
		}

		public T call() {
			task.run();
			return result;
		}
	}

	static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		DefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	static class DelegatedExecutorService extends AbstractExecutorService {
		private final ExecutorService e;

		DelegatedExecutorService(ExecutorService executor) {
			e = executor;
		}

		public void execute(Runnable command) {
			e.execute(command);
		}

		public void shutdown() {
			e.shutdown();
		}

		public List<Runnable> shutdownNow() {
			return e.shutdownNow();
		}

		public boolean isShutdown() {
			return e.isShutdown();
		}

		public boolean isTerminated() {
			return e.isTerminated();
		}

		public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
			return e.awaitTermination(timeout, unit);
		}

		public Future<?> submit(Runnable task) {
			return e.submit(task);
		}

		public <T> Future<T> submit(Callable<T> task) {
			return e.submit(task);
		}

		public <T> Future<T> submit(Runnable task, T result) {
			return e.submit(task, result);
		}

		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
			return e.invokeAll(tasks);
		}

		public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
			return e.invokeAll(tasks, timeout, unit);
		}

		public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
			return e.invokeAny(tasks);
		}

		public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
			return e.invokeAny(tasks, timeout, unit);
		}
	}

	static class FinalizableDelegatedExecutorService extends DelegatedExecutorService {
		FinalizableDelegatedExecutorService(ExecutorService executor) {
			super(executor);
		}

		protected void finalize() {
			super.shutdown();
		}
	}

	static class DelegatedScheduledExecutorService extends DelegatedExecutorService implements ScheduledExecutorService {
		private final ScheduledExecutorService e;

		DelegatedScheduledExecutorService(ScheduledExecutorService executor) {
			super(executor);
			e = executor;
		}

		public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
			return e.schedule(command, delay, unit);
		}

		public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
			return e.schedule(callable, delay, unit);
		}

		public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
			return e.scheduleAtFixedRate(command, initialDelay, period, unit);
		}

		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
			return e.scheduleWithFixedDelay(command, initialDelay, delay, unit);
		}
	}
}
