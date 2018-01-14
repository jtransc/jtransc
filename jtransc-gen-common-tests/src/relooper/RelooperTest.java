package relooper;

import com.jtransc.annotation.JTranscRelooper;
import com.jtransc.io.JTranscConsole;

public class RelooperTest {
	static public void main(String[] args) {
		JTranscConsole.log("RelooperTest:");
		JTranscConsole.log(simpleIf(0, 1));
		JTranscConsole.log(simpleIf(1, 0));
		JTranscConsole.log(simpleIf(0, 0));

		JTranscConsole.log(composedIf(0, 1));
		JTranscConsole.log(composedIf(1, 0));
		JTranscConsole.log(composedIf(0, 0));
	}

	//@JTranscRelooper
	static public int simpleIf(int a, int b) {
		if (a < b) {
			return -1;
		} else {
			return +1;
		}
	}

	@JTranscRelooper
	static public int composedIf(int a, int b) {
		if (a < b && a >= 0) {
			return -1;
		} else {
			return +1;
		}
	}
}
