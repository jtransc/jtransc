package javax.sound.sampled;

import java.util.Map;

public class AudioFileFormat {
	protected AudioFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {
		throw new RuntimeException("Not implemented");
	}

	public AudioFileFormat(Type type, AudioFormat format, int frameLength) {
		this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
	}

	public AudioFileFormat(Type type, AudioFormat format, int frameLength, Map<String, Object> properties) {
		this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
		throw new RuntimeException("Not implemented");
	}

	native public Type getType();

	native public int getByteLength();

	native public AudioFormat getFormat();

	native public int getFrameLength();

	native public Map<String, Object> properties();

	native public Object getProperty(String key);

	native public String toString();

	public static class Type {
		public static final Type WAVE = new Type("WAVE", "wav");
		public static final Type AU = new Type("AU", "au");
		public static final Type AIFF = new Type("AIFF", "aif");
		public static final Type AIFC = new Type("AIFF-C", "aifc");
		public static final Type SND = new Type("SND", "snd");

		private final String name;
		private final String ext;

		public Type(String name, String ext) {

			this.name = name;
			this.ext = ext;
		}

		public final boolean equals(Object obj) {
			return this == obj;
		}

		public final int hashCode() {
			return name.hashCode();
		}

		public final String toString() {
			return name;
		}

		public String getExtension() {
			return ext;
		}
	}
}
