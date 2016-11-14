package java.util.logging;

public abstract class Formatter {
	protected Formatter() {
	}

	public abstract String format(LogRecord r);

	public String formatMessage(LogRecord r) {
		String pattern = r.getMessage();
		//ResourceBundle rb = null;
		//// try to localize the message string first
		//if ((rb = r.getResourceBundle()) != null) {
		//	try {
		//		pattern = rb.getString(pattern);
		//	} catch (Exception e) {
		//		pattern = r.getMessage();
		//	}
		//}
		//if (pattern != null) {
		//	Object[] params = r.getParameters();
		//	if (pattern.indexOf("{0") >= 0 && params != null && params.length > 0) {
		//		try {
		//			pattern = MessageFormat.format(pattern, params);
		//		} catch (IllegalArgumentException e) {
		//			pattern = r.getMessage();
		//		}
		//	}
		//}
		return pattern;
	}

	public String getHead(Handler h) {
		return "";
	}

	public String getTail(Handler h) {
		return "";
	}
}