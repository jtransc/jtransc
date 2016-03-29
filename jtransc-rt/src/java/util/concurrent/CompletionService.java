package java.util.concurrent;

public interface CompletionService<V> {
	Future<V> submit(Callable<V> task);

	Future<V> submit(Runnable task, V result);

	Future<V> take() throws InterruptedException;

	Future<V> poll();

	Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
}
