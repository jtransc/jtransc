package java.util.concurrent;

public interface ScheduledExecutorService extends ExecutorService {
	ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);

	<V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

	ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

	ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);
}
