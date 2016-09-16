package com.jtransc;

public class JTranscSystemProperties {
	static public String fileEncoding() {
		return "UTF-8";
	}

	static public String userLanguage() {
		return "en";
	}

	static public String userRegion() {
		return "US";
	}

	static public String userVariant() {
		return "";
	}

	public static String tmpdir() {
		return getenvs(new String[]{"TMPDIR", "TEMP", "TMP"}, "/tmp");
	}

	public static String getenvs(String[] names, String defaultValue) {
		for (String name : names) {
			String out = System.getenv(name);
			if (out != null) return out;
		}
		return defaultValue;
	}

	public static String userHome() {
		return JTranscSystemProperties.getenvs(new String[]{"HOME"}, "/tmp");
	}

	public static String userDir() {
		return JTranscSystemProperties.getenvs(new String[]{"HOME"}, "/tmp");
	}

	public static String userName() {
		return JTranscSystemProperties.getenvs(new String[]{"USERNAME", "USER"}, "username");
	}

	public static String javaHome() {
		return "/tmp";
	}

	public static String fileSeparator() {
		//return isWindows() ? "\\" : "/";
		return "/";
	}

	public static String pathSeparator() {
		//return isWindows() ? ";" : ":";
		return ":";
	}

	public static String lineSeparator() {
		//return isWindows() ? "\r\n" : "\n";
		return "\n";
	}
}
