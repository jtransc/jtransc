package java.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ThreadPoolExecutor implements ExecutorService {

	private static final int TASK_WAITING    = 1;
	private static final int TASK_RUNNING    = 2;
	private static final int TASK_STOPPING    = 3;

	private static final int EX_WORKING       = 1;
	private static final int EX_STOP       = 2;
	private static final int EX_SHUTDOWN   = 3;

	private static final int MAX_POOL_SIZE = Integer.MAX_VALUE / 2;

	private int state = EX_WORKING;
	private int poolSize;
	private int maxPoolSize;
	final private BlockingQueue<Runnable> workQueue;
	private ThreadFactory factory;
	private RejectedExecutionHandler rejectHandler;

	private ArrayList<Task> taskPool;

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, null, null);
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, null);
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
		this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, null, handler);
	}

	public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
		if (corePoolSize < 0 ||
			maximumPoolSize <= 0 ||
			maximumPoolSize < corePoolSize)
				throw new IllegalArgumentException();
		poolSize = corePoolSize;
		maxPoolSize = Math.min(maximumPoolSize, MAX_POOL_SIZE);
		this.workQueue = workQueue == null ? new LinkedBlockingDeque<>() : workQueue;
		factory = threadFactory == null ? new Executors.DefaultThreadFactory() : threadFactory;
		rejectHandler = handler;

		taskPool = new ArrayList<>();
		for (int i = 0; i < poolSize; i++){
			taskPool.add(new Task());
		}
	}

	@Override
	public void shutdown() {
		state = EX_SHUTDOWN;
	}

	@Override
	public List<Runnable> shutdownNow() {
		state = EX_SHUTDOWN;
		for (Task t : taskPool) {
			if (t.state == TASK_RUNNING)
				t.state = TASK_STOPPING;
		}
		List<Runnable> l;
		synchronized (workQueue) {
			l = new ArrayList<>(workQueue);
			workQueue.clear();
		}
		return l;
	}

	@Override
	public boolean isShutdown() {
		return state == EX_SHUTDOWN;
	}

	@Override
	public boolean isTerminated() {
		return state == EX_SHUTDOWN;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return true;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		try {
			return new ImmediateFuture<T>(task.call());
		} catch (Exception e) {
			return new ImmediateFuture<T>(null);
		}
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		task.run();
		return new ImmediateFuture<T>(result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return submit(task, null);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		ArrayList<Future<T>> out = new ArrayList<Future<T>>();
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
		if (state != EX_WORKING) {
			if (rejectHandler != null)
				rejectHandler.rejectedExecution(command, this);
			return;
		}

		synchronized (workQueue) {
			try {
				workQueue.put(command);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		execute();
	}

	private void execute(){
		for (Task t : taskPool){
			if (t.state == TASK_WAITING){
				factory.newThread(t).start();
				return;
			}
		}
		if (poolSize < maxPoolSize) {
			poolSize++;
			taskPool.add(new Task());
			execute();
		}
	}

	private class Task implements Runnable {
		public int state = TASK_WAITING;
		@Override
		public void run() {
			state = TASK_RUNNING;
			while (state < TASK_STOPPING) {
				Runnable command;
				synchronized (workQueue) {
					command = workQueue.poll();
				}
				if (command != null) {
					command.run();
				} else {
					state = TASK_STOPPING;
				}
			}
			state = TASK_WAITING;
		}
	}
}
