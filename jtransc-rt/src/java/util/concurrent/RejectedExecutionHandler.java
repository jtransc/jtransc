package java.util.concurrent;

public interface RejectedExecutionHandler {
	void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
