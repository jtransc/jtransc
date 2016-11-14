package java.util.logging;

public class ConsoleHandler extends StreamHandler {
	public ConsoleHandler() {
		super(System.err);
	}

	@Override
	public void close() {
		super.close(false);
	}

	@Override
	public void publish(LogRecord record) {
		super.publish(record);
		super.flush();
	}
}