package java.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;

public class SimpleFormatter extends Formatter {
	public SimpleFormatter() {
	}

	@Override
	public String format(LogRecord r) {
		StringBuilder sb = new StringBuilder();
		sb.append(MessageFormat.format("{0, date} {0, time} ",
			new Object[]{new Date(r.getMillis())}));
		sb.append(r.getSourceClassName()).append(" ");
		sb.append(r.getSourceMethodName()).append(System.lineSeparator());
		sb.append(r.getLevel().getName()).append(": ");
		sb.append(formatMessage(r)).append(System.lineSeparator());
		if (r.getThrown() != null) {
			sb.append("Throwable occurred: ");
			Throwable t = r.getThrown();
			PrintWriter pw = null;
			try {
				StringWriter sw = new StringWriter();
				pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				sb.append(sw.toString());
			} finally {
				pw.close();
			}
		}
		return sb.toString();
	}
}