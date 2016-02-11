package jtransc;

public class JTranscSystem {
	static long start = -1;

	static public int stamp() {
		if (start < 0) start = System.currentTimeMillis();
		return (int) (System.currentTimeMillis() - start);
	}
}
