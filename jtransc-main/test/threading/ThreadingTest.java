package threading;

import java.util.ArrayList;
import java.util.Collections;

public class ThreadingTest {
	static public void main(String[] args) {
		ArrayList<String> logs = new ArrayList<String>();
		long start = System.currentTimeMillis();
		for (int n = 0; n < 3; n++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (logs) {
						logs.add(Thread.currentThread().getName());
					}
				}
			}, "thread-" + n);
			thread.start();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		logs.add("" + (end - start < 1000));
		for (Object log : logs) {
			System.out.println(log);
		}
	}
}
