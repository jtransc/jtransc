package com.jtransc.util;

import com.jtransc.annotation.JTranscInvisible;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@JTranscInvisible
public class JTranscCollections {
	static public <T> ArrayList<T> distinct(List<T> items) {
		return new ArrayList<T>(new LinkedHashSet<T>(items));
	}

	static public <T> T[] sliceArray(List<T> list, int offset, T[] out) {
		List<T> ts = list.subList(offset, offset + out.length);
		return ts.toArray(out);
	}
}
