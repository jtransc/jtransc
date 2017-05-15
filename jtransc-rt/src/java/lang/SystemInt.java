package java.lang;

public class SystemInt {
	static private int $$lastId = 1;

	static public int identityHashCode(Object x) {
		if (x != null) {
			if (x.$$id == 0) x.$$id = $$lastId++;
			return x.$$id;
		}
		return 0;
	}
}
