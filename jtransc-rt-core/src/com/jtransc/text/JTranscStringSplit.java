package com.jtransc.text;

import java.util.ArrayList;

public class JTranscStringSplit {
	static public String[] split(String str, char c) {
		ArrayList<String> out = new ArrayList<>();
		int pivot = 0;
		int length = str.length();
		int n = 0;
		for (; n < length; n++) {
			if (str.charAt(n) == c) {
				out.add(str.substring(pivot, n));
				pivot = n + 1;
			}
		}
		if (pivot <= length) {
			out.add(str.substring(pivot, n));
		}
		return out.toArray(new String[out.size()]);
	}
}
