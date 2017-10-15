package big;

import com.jtransc.io.JTranscConsole;

import java.util.concurrent.Semaphore;

@SuppressWarnings("ConstantConditions")
public class ThreadTest {
	static public void main(String[] args) throws InterruptedException {
		System.out.println("ThreadTest.main:");
		//final Semaphore sema = new Semaphore(1);
		//sema.acquire();
		//final Thread t = new Thread(() -> {
		//	try {
		//		sema.acquire();
		//	} catch (InterruptedException e) {
		//		e.printStackTrace();
		//	}
		//	System.out.println("Thread end!");
		//});
		//t.start();
		interlock();
		synchronizedBlock();
		synchronizedMethod();
		//sema.release();
		//t.join();
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
			final SynchronizedTraits sync = new SynchronizedTraits();
			JTranscConsole.log("START1");
			Thread t1 = new Thread(() -> sync.synchronizedBlock());
			Thread t2 = new Thread(() -> sync.synchronizedBlock());
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

	static private void synchronizedMethod() {
		JTranscConsole.log("ThreadTest.synchronizedMethod:");
		JTranscConsole.log(Thread.currentThread() != null);
		try {
			final SynchronizedTraits sync = new SynchronizedTraits();
			JTranscConsole.log("START1");
			Thread t1 = new Thread(() -> sync.synchronizedMethod());
			Thread t2 = new Thread(() -> sync.synchronizedMethod());
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

class SynchronizedTraits {
	void synchronizedBlock() {
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

	synchronized void synchronizedMethod() {
		System.out.println("BEGIN");
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("END");
	}
}
