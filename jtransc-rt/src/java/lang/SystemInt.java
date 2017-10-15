package java.lang;

import com.jtransc.annotation.JTranscSync;

public class SystemInt {
	static private int $$lastId = 1;

	@JTranscSync
	static public int identityHashCode(Object x) {
		if (x != null) {
			if (x.$$id == 0) x.$$id = $$lastId++;
			return x.$$id;
		}
		return 0;
	}
}
