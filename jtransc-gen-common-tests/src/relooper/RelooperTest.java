package relooper;

import com.jtransc.annotation.JTranscRelooper;
import com.jtransc.io.JTranscConsole;

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
	}

	@JTranscRelooper(false)
	static public int simpleIf(int a, int b) {
		if (a < b) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper(false)
	static public int composedIfAnd(int a, int b) {
		if (a < b && a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper(value = true, debug = false)
	static public int composedIfOr(int a, int b) {
		if (a < b || a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper(value = true, debug = true)
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
}
