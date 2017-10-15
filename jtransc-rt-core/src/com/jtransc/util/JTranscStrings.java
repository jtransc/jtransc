package com.jtransc.util;

import com.jtransc.JTranscBits;
import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscSync;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
@JTranscInvisible
public class JTranscStrings {
	@JTranscSync
	static public String substr(String subject, int offset) {
		return substr(subject, offset, Integer.MAX_VALUE);
	}

	@JTranscSync
	static public String substr(String subject, int offset, int count) {
		int subjectLen = subject.length();
		int realStart = (offset < 0) ? JTranscBits.clamp(JTranscBits.unsignedMod(offset, subjectLen), 0, subjectLen) : JTranscBits.clamp(offset, 0, subjectLen);
		int realEnd = (count < 0) ? JTranscBits.unsignedMod(subjectLen + count, subjectLen) : JTranscBits.clamp(realStart + count, 0, subjectLen);
		return subject.substring(realStart, realEnd);
	}

	@JTranscSync
	static public String[] splitInChunks(String subject, int count) {
		return splitInChunksDirection(subject, +count);
	}

	@JTranscSync
	static public String[] splitInChunksRightToLeft(String subject, int count) {
		return splitInChunksDirection(subject, -count);
	}

	@JTranscSync
	static public String[] splitInChunksDirection(String subject, int count) {
		final boolean leftToRight = (count > 0);
		final int slen = subject.length();
		final int acount = Math.abs(count);
		//System.out.println("subject: " + subject + " : " + subject.length() + " : " + acount);
		final double dpartCount = Math.ceil((double) subject.length() / (double) acount);
		final int partCount = (int) Math.ceil((double) subject.length() / (double) acount);
		//System.out.println("dpartCount: " + dpartCount);
		//System.out.println("partCount: " + partCount);
		final String[] out = new String[partCount];
		for (int n = 0; n < partCount; n++) {
			int pos;
			if (leftToRight) {
				pos = n * acount;
			} else {
				pos = subject.length() - ((n + 1) * acount);
			}
			int index = leftToRight ? n : (partCount - n - 1);
			int len = acount;
			if (pos < 0) {
				len += pos;
				pos = 0;
			}
			int end = Math.min(slen, pos + len);
			//System.out.println("substring: " + pos + ", " + end);
			out[index] = subject.substring(pos, end);
		}
		return out;
	}

	@JTranscSync
	static public String join(String[] parts) {
		int count = 0;
		for (String part : parts) count += part.length();
		StringBuilder out = new StringBuilder(count);
		for (String part : parts) out.append(part);
		return out.toString();
	}

	@JTranscSync
	static public String join(String[] parts, String separator) {
		if (parts.length == 0) return "";
		int count = 0;
		for (String part : parts) count += part.length();
		count += (parts.length - 1) * separator.length();
		StringBuilder out = new StringBuilder(count);
		for (int n = 0; n < parts.length; n++) {
			if (n != 0) out.append(separator);
			out.append(parts[n]);
		}
		return out.toString();
	}

	@JTranscSync
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
