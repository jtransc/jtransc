package java.net;

import java.io.UnsupportedEncodingException;

import com.jtransc.annotation.haxe.HaxeMethodBody;

public class URLDecoder {
	private URLDecoder() {
	}

	@Deprecated
	public static String decode(String s) {
		try {
			return decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@HaxeMethodBody("return N.str(StringTools.urlDecode(p0._str));")
	public native static String decode(String s, String enc) throws UnsupportedEncodingException;
}
