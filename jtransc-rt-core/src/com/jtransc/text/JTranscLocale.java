package com.jtransc.text;

import java.util.Locale;

public class JTranscLocale {
	static public String getDecimalSeparator(Locale l) {
		switch ((l != null) ? l.getLanguage() : "en") {
			case "en":
			case "ja":
			case "zh":
				return ".";
			default:
				return ",";
		}
	}
}
