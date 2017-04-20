package java.net;

import java.io.UnsupportedEncodingException;
import com.jtransc.annotation.haxe.HaxeMethodBody;

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

	@HaxeMethodBody("return N.str(StringTools.urlEncode(p0._str));")
	native public static String encode(String s, String enc) throws UnsupportedEncodingException;
}
