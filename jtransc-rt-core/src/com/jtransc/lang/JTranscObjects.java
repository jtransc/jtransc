package com.jtransc.lang;

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscSync;

@SuppressWarnings("RedundantIfStatement")
public class JTranscObjects {
	@JTranscAsync
	static public String toStringOrNull(Object obj) {
		return (obj != null) ? obj.toString() : null;
	}

	@JTranscSync
	static public boolean equalsShape(Object a, Object b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		if (a.getClass() != b.getClass()) return false;
		return true;
	}
}
