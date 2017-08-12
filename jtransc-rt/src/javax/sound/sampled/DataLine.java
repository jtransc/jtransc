package javax.sound.sampled;

public interface DataLine extends Line {
	void drain();

	void flush();

	void start();

	void stop();

	boolean isRunning();

	boolean isActive();

	AudioFormat getFormat();

	int getBufferSize();

	int available();

	int getFramePosition();

	long getLongFramePosition();

	long getMicrosecondPosition();

	float getLevel();

	class Info extends Line.Info {
		public Info(Class<?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {
			super(lineClass);
			throw new RuntimeException("Not implemented");
		}

		public Info(Class<?> lineClass, AudioFormat format, int bufferSize) {
			super(lineClass);
			throw new RuntimeException("Not implemented");
		}

		public Info(Class<?> lineClass, AudioFormat format) {
			this(lineClass, format, AudioSystem.NOT_SPECIFIED);
		}

		native public AudioFormat[] getFormats();

		native public boolean isFormatSupported(AudioFormat format);

		native public int getMinBufferSize();

		native public int getMaxBufferSize();

		native public boolean matches(Line.Info info);

		native public String toString();
	}
}
