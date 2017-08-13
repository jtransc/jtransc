package com.jtransc.text;

import java.util.Locale;

public class JTranscLocale {
	static public int getIntNumberOfDigits(Locale l) {
		return 3;
	}

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

	static public String getGroupSeparator(Locale l) {
		switch ((l != null) ? l.getLanguage() : "en") {
			case "fr":
				return "\u00a0";
			case "en":
			case "ja":
			case "zh":
				return ",";
			default:
				return ".";
		}
	}
}
