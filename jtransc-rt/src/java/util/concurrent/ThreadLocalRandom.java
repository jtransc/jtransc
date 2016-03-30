package java.util.concurrent;

import java.util.Random;

public class ThreadLocalRandom extends Random {
	static private ThreadLocalRandom _current = null;

	public static ThreadLocalRandom current() {
		if (_current == null) _current = new ThreadLocalRandom();
		return _current;
	}

}
