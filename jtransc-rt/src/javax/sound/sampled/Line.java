package javax.sound.sampled;

public interface Line extends AutoCloseable {
	Line.Info getLineInfo();

	void open() throws LineUnavailableException;

	void close();

	boolean isOpen();

	Control[] getControls();

	boolean isControlSupported(Control.Type control);

	Control getControl(Control.Type control);

	void addLineListener(LineListener listener);

	void removeLineListener(LineListener listener);

	class Info {
		public Info(Class<?> lineClass) {
			throw new RuntimeException("Not implemented");
		}

		native public Class<?> getLineClass();

		native public boolean matches(Info info);

		native public String toString();
	}
}
