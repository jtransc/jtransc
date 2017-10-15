package javatest.sleep;

import com.jtransc.io.JTranscConsole;
import jtransc.jtransc.nativ.JTranscJsNativeMixedTest;

public class SleepTest {
	static public void main(String[] args) {
		JTranscConsole.log(1234560007);
		JTranscConsole.log("SleepTest.main:");

		long start = System.currentTimeMillis();

		try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();

		System.out.println(" - Slept for at least 100ms? " + ((end - start) >= 99));
	}
}
