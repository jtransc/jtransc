package relooper;

import com.jtransc.annotation.JTranscRelooper;
import com.jtransc.io.JTranscConsole;

import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class RelooperTest {
	static public void main(String[] args) {
		JTranscConsole.log("RelooperTest:");

		JTranscConsole.log(simpleIf(0, 1));
		JTranscConsole.log(simpleIf(1, 0));
		JTranscConsole.log(simpleIf(0, 0));

		JTranscConsole.log(composedIfAnd(0, 1));
		JTranscConsole.log(composedIfAnd(1, 0));
		JTranscConsole.log(composedIfAnd(0, 0));

		JTranscConsole.log(composedIfOr(0, 1));
		JTranscConsole.log(composedIfOr(1, 0));
		JTranscConsole.log(composedIfOr(0, 0));

		simpleDoWhile(0, 5);
		simpleWhile(0, 5);
		simpleFor(2, 5);
		JTranscConsole.log(Arrays.asList(split("hello world test", ' ', 2)));
		bufferEquals(ShortBuffer.allocate(1), ShortBuffer.allocate(1));

		JTranscConsole.log(demo(true, true, false));
	}

	@JTranscRelooper
	static public int simpleIf(int a, int b) {
		if (a < b) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int composedIfAnd(int a, int b) {
		if (a < b && a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int composedIfOr(int a, int b) {
		if (a < b || a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int simpleDoWhile(int a, int b) {
		b++;

		do {
			if (a % 2 == 0) {
				do {
					JTranscConsole.log(a);
					a++;
				} while (a < b);
			}
			a++;
		} while (a < b);

		return b;
	}

	@JTranscRelooper
	static public int simpleWhile(int a, int b) {
		b++;

		while (a < b) {
			JTranscConsole.log(a);
			a++;
		}
		JTranscConsole.log(a);
		JTranscConsole.log(b);

		return b;
	}

	@JTranscRelooper
	static public int simpleFor(int a, int b) {
		for (int n = 1; n < b; n++) {
			JTranscConsole.log(a + n);
		}
		return b;
	}

	@JTranscRelooper
	static private String[] split(String str, char ch, int limit) {
		ArrayList<String> out = new ArrayList<String>();
		int n = 0;
		int start = 0;
		for (; n < str.length(); n++) {
			if (str.charAt(n) == ch) {
				out.add(str.substring(start, n));
				start = n + 1;
				if (out.size() >= limit - 1) break;
			}
		}
		if (start < str.length()) out.add(str.substring(start));
		return out.toArray(new String[out.size()]);
	}

	@JTranscRelooper(debug = true)
	static private boolean bufferEquals(ShortBuffer t, Object other) {
		if (!(other instanceof ShortBuffer)) {
			return false;
		}
		ShortBuffer otherBuffer = (ShortBuffer) other;

		if (t.remaining() != otherBuffer.remaining()) {
			return false;
		}

		int myPosition = t.position();
		int otherPosition = otherBuffer.position();
		boolean equalSoFar = true;
		while (equalSoFar && (myPosition < t.limit())) {
			equalSoFar = t.get(myPosition++) == otherBuffer.get(otherPosition++);
		}

		return equalSoFar;
	}

	@JTranscRelooper
	static private boolean demo(boolean a, boolean b, boolean c) {
		boolean result = true;
		while (a && b != c) {
			a = !b;
			result = ((a ^ c) == b);
		}

		return result;
	}
}
