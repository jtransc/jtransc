package java.net;

import com.jtransc.util.JTranscHex;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class URLDecoder {
	@Deprecated
	public static String encode(String s) {
		try {
			return decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decode(String s, String enc) throws UnsupportedEncodingException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream(s.length());
		final int len = s.length();
		for (int n = 0; n < len; n++) {
			final char c = s.charAt(n);
			if (c == '%') {
				bos.write(JTranscHex.decodeInt(s, n + 1, 2));
				n += 2;
			} else if (c == '+') {
				bos.write(' ');
			} else {
				bos.write(c);
			}
		}
		return new String(bos.toByteArray(), enc);
	}
}
