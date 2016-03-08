package jtransc.util;

import jtransc.annotation.JTranscInvisible;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@JTranscInvisible
public class JTranscCollections {
	static public <T> ArrayList<T> distinct(List<T> items) {
		return new ArrayList<T>(new LinkedHashSet<T>(items));
	}
}
