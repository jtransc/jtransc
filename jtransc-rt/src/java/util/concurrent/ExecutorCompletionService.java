package java.util.concurrent;

import java.util.Objects;

public class ExecutorCompletionService<V> implements CompletionService<V> {
	private final Executor executor;
	private final AbstractExecutorService aes;
	private final BlockingQueue<Future<V>> completionQueue;

	private class QueueingFuture extends FutureTask<Void> {
		QueueingFuture(RunnableFuture<V> task) {
			super(task, null);
			this.task = task;
		}

		protected void done() {
			completionQueue.add(task);
		}

		private final Future<V> task;
	}

	private RunnableFuture<V> newTaskFor(Callable<V> task) {
		return (aes != null) ? aes.newTaskFor(task) : new FutureTask<V>(task);
	}

	private RunnableFuture<V> newTaskFor(Runnable task, V result) {
		return (aes != null) ? aes.newTaskFor(task, result) : new FutureTask<V>(task, result);
	}

	public ExecutorCompletionService(Executor executor) {
		Objects.requireNonNull(executor);
		this.executor = executor;
		this.aes = (executor instanceof AbstractExecutorService) ? (AbstractExecutorService) executor : null;
		this.completionQueue = new LinkedBlockingQueue<Future<V>>();
	}

	public ExecutorCompletionService(Executor executor, BlockingQueue<Future<V>> completionQueue) {
		Objects.requireNonNull(executor);
		Objects.requireNonNull(completionQueue);
		this.executor = executor;
		this.aes = (executor instanceof AbstractExecutorService) ? (AbstractExecutorService) executor : null;
		this.completionQueue = completionQueue;
	}

	public Future<V> submit(Callable<V> task) {
		Objects.requireNonNull(task);
		RunnableFuture<V> f = newTaskFor(task);
		executor.execute(new QueueingFuture(f));
		return f;
	}

	public Future<V> submit(Runnable task, V result) {
		Objects.requireNonNull(task);
		RunnableFuture<V> f = newTaskFor(task, result);
		executor.execute(new QueueingFuture(f));
		return f;
	}

	public Future<V> take() throws InterruptedException {
		return completionQueue.take();
	}

	public Future<V> poll() {
		return completionQueue.poll();
	}

	public Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException {
		return completionQueue.poll(timeout, unit);
	}

}
