package java.util.logging;

public interface Filter {
	boolean isLoggable(LogRecord record);
}