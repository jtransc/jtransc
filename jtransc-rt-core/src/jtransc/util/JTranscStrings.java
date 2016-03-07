package jtransc.util;

import jtransc.JTranscBits;
import jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public class JTranscStrings {
	static public String substr(String subject, int offset) {
		return substr(subject, offset, Integer.MAX_VALUE);
	}

	static public String substr(String subject, int offset, int count) {
		int subjectLen = subject.length();
		int realStart = (offset < 0) ? JTranscBits.clamp(JTranscBits.unsignedMod(offset, subjectLen), 0, subjectLen) : JTranscBits.clamp(offset, 0, subjectLen);
		int realEnd = (count < 0) ? JTranscBits.unsignedMod(subjectLen + count, subjectLen) : JTranscBits.clamp(realStart + count, 0, subjectLen);
		return subject.substring(realStart, realEnd);
	}
}
