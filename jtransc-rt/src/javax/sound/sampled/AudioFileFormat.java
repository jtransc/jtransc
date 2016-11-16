package javax.sound.sampled;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public class AudioFileFormat {
	private Type type;
	private int byteLength;
	private AudioFormat format;
	private int frameLength;
	private HashMap<String, Object> properties;

	protected AudioFileFormat(Type type, int byteLength, AudioFormat format, int frameLength) {
		this(type, format, frameLength, new HashMap<>());
		this.byteLength = byteLength;
	}

	public AudioFileFormat(Type type, AudioFormat format, int frameLength) {
		this(type, AudioSystem.NOT_SPECIFIED, format, frameLength);
	}

	public AudioFileFormat(Type type, AudioFormat format, int frameLength, Map<String, Object> properties) {
		this.type = type;
		this.format = format;
		this.frameLength = frameLength;
		this.properties = new HashMap<>(properties);
	}

	public Type getType() {
		return type;
	}

	public int getByteLength() {
		return byteLength;
	}

	public AudioFormat getFormat() {
		return format;
	}

	public int getFrameLength() {
		return frameLength;
	}

	public Map<String, Object> properties() {
		return properties;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

	public static class Type {
		public static final Type WAVE = new Type("WAVE", "wav");
		public static final Type AU = new Type("AU", "au");
		public static final Type AIFF = new Type("AIFF", "aif");
		public static final Type AIFC = new Type("AIFF-C", "aifc");
		public static final Type SND = new Type("SND", "snd");

		private final String name;
		private final String extension;

		public Type(String name, String extension) {

			this.name = name;
			this.extension = extension;
		}

		public final String toString() {
			return name;
		}

		public String getExtension() {
			return extension;
		}
	}
}
