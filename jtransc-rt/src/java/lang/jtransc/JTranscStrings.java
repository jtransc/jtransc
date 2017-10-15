package java.lang.jtransc;

import com.jtransc.annotation.JTranscSync;

public class JTranscStrings {
	@JTranscSync
	static public char[] getData(String str) {
		return str.getNativeCharArray();
	}

	@JTranscSync
	static public boolean equals(char[] a, int aoffset, char[] b, int boffset, int len) {
		for (int n = 0; n < len; n++) if (a[aoffset + n] != b[boffset + n]) return false;
		return true;
	}

	@JTranscSync
	static public int indexOf(char[] base, int offset, int haystack) {
		int end = base.length;
		for (int n = offset; n < end; n++) if (base[n] == haystack) return n;
		return -1;
	}

	@JTranscSync
	static public int lastIndexOf(char[] base, int offset, int haystack) {
		int start = Math.min(offset, base.length - 1);
		for (int n = start; n >= 0; n--) if (base[n] == haystack) return n;
		return -1;
	}

	@JTranscSync
	static public int indexOf(char[] base, int offset, char[] haystack) {
		int end = base.length - haystack.length;
		for (int n = offset; n < end; n++) if (JTranscStrings.equals(base, n, haystack, 0, haystack.length)) return n;
		return -1;
	}

	@JTranscSync
	static public int lastIndexOf(char[] base, int offset, char[] haystack) {
		int start = Math.min(offset, base.length - haystack.length);
		for (int n = start; n >= 0; n--) if (JTranscStrings.equals(base, n, haystack, 0, haystack.length)) return n;
		return -1;
	}

	@JTranscSync
	public static String valueOf(String value) {
		return (value != null) ? value : "null";
	}
}
