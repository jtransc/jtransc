package jtransc.bug;

import java.util.Arrays;
import java.util.List;

public class JTranscBug127 {
	static public void main(String[] args) {
		testNonNull("hello");
		testNonNull(null);
		testNonNull(10);
	}

	static public void testNonNull(Object test) {
		try {
			Integer t = (Integer) test;
			System.out.println("No exception");
			System.out.println(t);
		} catch (ClassCastException e) {
			System.out.println("ClassCastException");
		}

		try {
			List t = (List) test;
			System.out.println("No exception");
			System.out.println(t);
		} catch (ClassCastException e) {
			System.out.println("ClassCastException");
		}

		try {
			byte[] t = (byte[]) test;
			System.out.println("No exception");
			System.out.println(Arrays.toString(t));
		} catch (ClassCastException e) {
			System.out.println("ClassCastException");
		}
	}
}
