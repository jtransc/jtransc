package java.util.concurrent;

import java.util.*;

public abstract class AbstractExecutorService implements ExecutorService {
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		return new FutureTask<T>(runnable, value);
	}

	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		return new FutureTask<T>(callable);
	}

	public Future<?> submit(Runnable task) {
		Objects.requireNonNull(task);
		RunnableFuture<Void> ftask = newTaskFor(task, null);
		execute(ftask);
		return ftask;
	}

	public <T> Future<T> submit(Runnable task, T result) {
		Objects.requireNonNull(task);
		RunnableFuture<T> ftask = newTaskFor(task, result);
		execute(ftask);
		return ftask;
	}

	public <T> Future<T> submit(Callable<T> task) {
		Objects.requireNonNull(task);
		RunnableFuture<T> ftask = newTaskFor(task);
		execute(ftask);
		return ftask;
	}

	private <T> T doInvokeAny(Collection<? extends Callable<T>> tasks, boolean timed, long nanos)
		throws InterruptedException, ExecutionException, TimeoutException {
		if (tasks == null) throw new NullPointerException();
		int ntasks = tasks.size();
		if (ntasks == 0) throw new IllegalArgumentException();
		ArrayList<Future<T>> futures = new ArrayList<Future<T>>(ntasks);
		ExecutorCompletionService<T> ecs = new ExecutorCompletionService<T>(this);

		// For efficiency, especially in executors with limited
		// parallelism, check to see if previously submitted tasks are
		// done before submitting more of them. This interleaving
		// plus the exception mechanics account for messiness of main
		// loop.

		try {
			// Record exceptions so that if we fail to obtain any
			// result, we can throw the last exception we got.
			ExecutionException ee = null;
			final long deadline = timed ? System.nanoTime() + nanos : 0L;
			Iterator<? extends Callable<T>> it = tasks.iterator();

			// Start one task for sure; the rest incrementally
			futures.add(ecs.submit(it.next()));
			--ntasks;
			int active = 1;

			for (; ; ) {
				Future<T> f = ecs.poll();
				if (f == null) {
					if (ntasks > 0) {
						--ntasks;
						futures.add(ecs.submit(it.next()));
						++active;
					} else if (active == 0)
						break;
					else if (timed) {
						f = ecs.poll(nanos, TimeUnit.NANOSECONDS);
						if (f == null)
							throw new TimeoutException();
						nanos = deadline - System.nanoTime();
					} else
						f = ecs.take();
				}
				if (f != null) {
					--active;
					try {
						return f.get();
					} catch (ExecutionException eex) {
						ee = eex;
					} catch (RuntimeException rex) {
						ee = new ExecutionException(rex);
					}
				}
			}

			if (ee == null) ee = new ExecutionException();
			throw ee;

		} finally {
			for (int i = 0, size = futures.size(); i < size; i++) futures.get(i).cancel(true);
		}
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		try {
			return doInvokeAny(tasks, false, 0);
		} catch (TimeoutException cannotHappen) {
			assert false;
			return null;
		}
	}

	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
		throws InterruptedException, ExecutionException, TimeoutException {
		return doInvokeAny(tasks, true, unit.toNanos(timeout));
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		Objects.requireNonNull(tasks);
		ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		boolean done = false;
		try {
			for (Callable<T> t : tasks) {
				RunnableFuture<T> f = newTaskFor(t);
				futures.add(f);
				execute(f);
			}
			for (int i = 0, size = futures.size(); i < size; i++) {
				Future<T> f = futures.get(i);
				if (!f.isDone()) {
					try {
						f.get();
					} catch (CancellationException ignore) {
					} catch (ExecutionException ignore) {
					}
				}
			}
			done = true;
			return futures;
		} finally {
			if (!done)
				for (int i = 0, size = futures.size(); i < size; i++)
					futures.get(i).cancel(true);
		}
	}

	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
	                                     long timeout, TimeUnit unit)
		throws InterruptedException {
		if (tasks == null)
			throw new NullPointerException();
		long nanos = unit.toNanos(timeout);
		ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
		boolean done = false;
		try {
			for (Callable<T> t : tasks)
				futures.add(newTaskFor(t));

			final long deadline = System.nanoTime() + nanos;
			final int size = futures.size();

			// Interleave time checks and calls to execute in case
			// executor doesn't have any/much parallelism.
			for (int i = 0; i < size; i++) {
				execute((Runnable) futures.get(i));
				nanos = deadline - System.nanoTime();
				if (nanos <= 0L) return futures;
			}

			for (int i = 0; i < size; i++) {
				Future<T> f = futures.get(i);
				if (!f.isDone()) {
					if (nanos <= 0L) return futures;
					try {
						f.get(nanos, TimeUnit.NANOSECONDS);
					} catch (CancellationException ignore) {
					} catch (ExecutionException ignore) {
					} catch (TimeoutException toe) {
						return futures;
					}
					nanos = deadline - System.nanoTime();
				}
			}
			done = true;
			return futures;
		} finally {
			if (!done) for (int i = 0, size = futures.size(); i < size; i++) futures.get(i).cancel(true);
		}
	}

}
