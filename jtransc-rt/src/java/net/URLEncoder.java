package java.net;

import java.io.UnsupportedEncodingException;

public class URLEncoder {
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

	native public static String encode(String s, String enc) throws UnsupportedEncodingException;
}
