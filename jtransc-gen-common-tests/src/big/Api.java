package big;

import com.jtransc.JTranscSystem;

public abstract class Api {
	static public Api create() {
		try {
			if (JTranscSystem.isJTransc()) {
				return new CppApi();
			} else {
				return (Api) Class.forName("big.JavaApi").newInstance();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

	abstract public int demo();
}
