package java.util.concurrent;

/**
 * @TODO: Thread compatible
 */
public class CountDownLatch {
	private int count;

	public CountDownLatch(int count) {
		if (count < 0) throw new IllegalArgumentException("count < 0");
		this.count = count;
	}

	public void await() throws InterruptedException {
		await(Long.MAX_VALUE, TimeUnit.DAYS);
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		//while (count > 0) Thread.sleep(1L); // @TODO!
		return true;
	}

	public void countDown() {
		count--;
	}

	public long getCount() {
		return count;
	}

	public String toString() {
		return super.toString() + "[count = " + count + "]";
	}
}
