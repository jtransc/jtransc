package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThreadPoolExecutor implements ExecutorService {
	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
	}
	@Override
	public void shutdown() {
	}

	@Override
	public List<Runnable> shutdownNow() {
		return new ArrayList<>();
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return true;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		try {
			return new ImmediateFuture<>(task.call());
		} catch (Exception e) {
			return new ImmediateFuture<>(null);
		}
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		task.run();
		return new ImmediateFuture<>(result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return submit(task, null);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		ArrayList<Future<T>> out = new ArrayList<>();
		for (Callable<T> task : tasks) {
			try {
				out.add(new ImmediateFuture(task.call()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return out;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		return invokeAll(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		for (Callable<T> task : tasks) {
			try {
				return task.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return invokeAny(tasks);
	}

	@Override
	public void execute(Runnable command) {

	}
}
