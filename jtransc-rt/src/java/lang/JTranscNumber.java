package java.lang;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscSync;

@JTranscInvisible
public class JTranscNumber {
	@JTranscInvisible
	@JTranscSync
	static public void checkNumber(String str, int radix, boolean allowFloat) {
		if (!JTranscNumber.validateNumber(str, radix, allowFloat)) {
			throw new NumberFormatException("For input string: \"" + str + "\"");
		}
	}

	@JTranscInvisible
	@JTranscSync
	static public boolean validateNumber(String str, int radix, boolean allowFloat) {
		final int len = str.length();
		int n = 0;
		int numpart = 0;
		int denpart = 0;
		int epart = 0;
		// Sign
		if ((n < len) && (str.charAt(n) == '-' || str.charAt(n) == '+')) n++;
		// Non-fractional digits
		while ((n < len) && isDigit(str.charAt(n), radix)) {
			n++;
			numpart++;
		}
		if (numpart <= 0) return false;
		if (allowFloat) {
			// Optional '.' notation
			if ((n < len) && (str.charAt(n) == '.')) {
				n++;
				while (n < len && isDigit(str.charAt(n), radix)) {
					n++;
					denpart++;
				}
				if (denpart <= 0) return false;
			}
			// Optional 'e' notation
			if ((n < len) && ((str.charAt(n) == 'e') || (str.charAt(n) == 'E'))) {
				n++;
				if ((n < len) && (str.charAt(n) == '-' || str.charAt(n) == '+')) n++;
				while ((n < len) && isDigit(str.charAt(n), radix)) {
					n++;
					epart++;
				}
				if (epart <= 0) return false;
			}
		}
		return (n >= len);
	}

	@JTranscInvisible
	@JTranscSync
	static public boolean isDigit(char c, int radix) {
		int d = digit(c);
		return d >= 0 && d < radix;
	}

	@JTranscInvisible
	@JTranscSync
	static public int digit(char c) {
		if (c >= '0' && c <= '9') return (c - '0');
		if (c >= 'a' && c <= 'z') return (c - 'a') + 10;
		if (c >= 'A' && c <= 'Z') return (c - 'A') + 10;
		return -1;
	}
}
