package java.net;

import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

public class URLEncoder {
	static private BitSet normal = new BitSet(0x100);
	static private String normalTable = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 -_.*";

	static {
		for (char c : normalTable.toCharArray()) normal.set(c);
	}

	private URLEncoder() {
	}

	@Deprecated
	public static String encode(String s) {
		try {
			return encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String s, String enc) throws UnsupportedEncodingException {
		final StringBuilder sb = new StringBuilder(s.length());
		byte[] data = s.getBytes(enc);
		//for (byte c : data) System.out.printf("%02X\n", c & 0xFF);
		for (byte c : data) {
			if (c == (byte) ' ') {
				sb.append('+');
			} else if (normal.get(c & 0xFF)) {
				sb.append((char) c);
			} else {
				sb.append('%');
				sb.append(Character.toUpperCase(Character.forDigit((c >>> 4) & 0xF, 16)));
				sb.append(Character.toUpperCase(Character.forDigit((c >>> 0) & 0xF, 16)));
			}
		}
		return sb.toString();
	}
}
