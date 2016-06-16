package com.jtransc.text;

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.text.internal.RealToString;

public class JTranscStringTools {

	public static String toString(float v) {
		return RealToString.getInstance().floatToString(v);
		//return toString((double) v);
	}

	public static String toString(double d) {
		//return JTranscStringTools.toString(d);
		String out = _toString(d);
		boolean hasSymbols = false;
		for (int n = 0; n < out.length(); n++) {
			char c = out.charAt(n);
			if (!Character.isDigit(c) && c != '-') {
				hasSymbols = true;
				break;
			}
		}
		if (out.indexOf("e+") >= 0) out = out.replace("e+", "E");
		if (out.indexOf("e-") >= 0) out = out.replace("e-", "E-");
		return hasSymbols ? out : (out + ".0");
	}

	@HaxeMethodBody("return HaxeNatives.str('' + p0);")
	@JTranscMethodBody(target = "js", value = "return N.str(String(Number(p0)));")
	//@JTranscMethodBody(target = "js", value = "return N.str(Number(p0).toPrecision(2));")
	native static public String _toString(double v);
}
