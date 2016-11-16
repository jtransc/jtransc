package javax.sound.sampled;

@SuppressWarnings({"unused", "WeakerAccess"})
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
		private final Class lineClass;

		public Info(Class<?> lineClass) {
			this.lineClass = (lineClass != null) ? lineClass : Line.class;
		}

		public Class<?> getLineClass() {
			return lineClass;
		}

		public boolean matches(Info info) {
			return true;
		}
	}
}
