package java.util.logging;

import java.util.List;

public interface LoggingMXBean {
	String getLoggerLevel(String loggerName);

	List<String> getLoggerNames();

	String getParentLoggerName(String loggerName);

	void setLoggerLevel(String loggerName, String levelName);
}