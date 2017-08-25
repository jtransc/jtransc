package big;

import com.jtransc.io.JTranscConsole;

@SuppressWarnings("ConstantConditions")
public class ThreadTest {
	static public void main(String[] args) {
		System.out.println("ThreadTest.main:");
		interlock();
		synchronizedBlock();
	}

	synchronized static private void a() {
		b();
	}

	synchronized static private void b() {
	}

	synchronized private void c() {
		synchronized (this) {
			synchronized (this) {
				d();
			}
		}
	}

	synchronized private void d() {
		synchronized (this) {
			System.out.println("not interlocked!");
			System.out.flush();
		}
	}

	static private void interlock() {
		a();
		new ThreadTest().c();
	}

	static private void synchronizedBlock() {
		JTranscConsole.log("ThreadTest.synchronizedBlock:");
		JTranscConsole.log(Thread.currentThread() != null);
		try {
			final SynchronizedBlock synchronizedBlock = new SynchronizedBlock();
			JTranscConsole.log("START1");
			Thread t1 = new Thread(() -> synchronizedBlock.demo());
			Thread t2 = new Thread(() -> synchronizedBlock.demo());
			System.out.println("START2");
			long start = System.currentTimeMillis();
			t1.start();
			t2.start();
			long end = System.currentTimeMillis();
			t1.join();
			t2.join();
			System.out.println("Not waited: " + ((end - start) < 500));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}

class SynchronizedBlock {
	void demo() {
		synchronized (this) {
			System.out.println("BEGIN");
			try {
				Thread.sleep(500L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("END");
		}
	}
}
