package javax.sound.sampled;

@SuppressWarnings({"WeakerAccess", "unused"})
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

		private final AudioFormat[] formats;
		private final int minBufferSize;
		private final int maxBufferSize;

		public Info(Class<?> lineClass, AudioFormat format, int bufferSize) {
			this(lineClass, new AudioFormat[]{format}, bufferSize, bufferSize);
		}

		public Info(Class<?> lineClass, AudioFormat[] formats, int minBufferSize, int maxBufferSize) {
			super(lineClass);
			this.formats = formats;
			this.minBufferSize = minBufferSize;
			this.maxBufferSize = maxBufferSize;
		}

		public Info(Class<?> lineClass, AudioFormat format) {
			this(lineClass, format, AudioSystem.NOT_SPECIFIED);
		}

		public AudioFormat[] getFormats() {
			return formats;
		}

		public boolean isFormatSupported(AudioFormat format) {
			return true;
		}

		public int getMinBufferSize() {
			return minBufferSize;
		}

		public int getMaxBufferSize() {
			return maxBufferSize;
		}

		public boolean matches(Line.Info info) {
			return true;
		}
	}
}
