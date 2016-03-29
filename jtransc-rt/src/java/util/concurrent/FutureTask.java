package java.util.concurrent;

public class FutureTask<V> implements RunnableFuture<V> {
	private V result;

	public FutureTask(Callable<V> callable) {
		try {
			result = callable.call();
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
	}

	public FutureTask(Runnable runnable, V result) {
		runnable.run();
		this.result = result;
	}

	@Override
	public void run() {
		// @TODO: Should call exeuction here instead of the constructor?
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return result;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return get();
	}
}
