package com.jtransc.lang;

@SuppressWarnings("RedundantIfStatement")
public class JTranscObjects {
	static public String toStringOrNull(Object obj) {
		return (obj != null) ? obj.toString() : null;
	}

	static public boolean equalsShape(Object a, Object b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		if (a.getClass() != b.getClass()) return false;
		return true;
	}
}
