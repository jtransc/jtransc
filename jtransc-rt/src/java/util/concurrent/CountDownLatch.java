package java.util.concurrent;

public class CountDownLatch {
	private int count;
	public CountDownLatch(int count) {
		this.count = count;
	}

	public void await() {
	}

	public boolean await(long timeout, TimeUnit unit) {
		return false;
	}

	public void countDown() {
	}

	public long getCount() {
		return this.count;
	}
}
