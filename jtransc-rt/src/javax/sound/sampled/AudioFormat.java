package javax.sound.sampled;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class AudioFormat {
	protected Encoding encoding;
	protected float sampleRate;
	protected int sampleSizeInBits;
	protected int channels;
	protected int frameSize;
	protected float frameRate;
	protected boolean bigEndian;
	private HashMap<String, Object> properties;

	public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian) {
		this(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian, new HashMap<>(0));
	}

	public AudioFormat(Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, Map<String, Object> properties) {
		this.encoding = encoding;
		this.sampleRate = sampleRate;
		this.sampleSizeInBits = sampleSizeInBits;
		this.channels = channels;
		this.frameSize = frameSize;
		this.frameRate = frameRate;
		this.bigEndian = bigEndian;
		this.properties = new HashMap<>(properties);
	}

	public AudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian) {
		this(signed ? Encoding.PCM_SIGNED : Encoding.PCM_UNSIGNED,
			sampleRate,
			sampleSizeInBits,
			channels,
			(channels == AudioSystem.NOT_SPECIFIED || sampleSizeInBits == AudioSystem.NOT_SPECIFIED) ? AudioSystem.NOT_SPECIFIED : ((sampleSizeInBits + 7) / 8) * channels,
			sampleRate,
			bigEndian
		);
	}

	public Encoding getEncoding() {
		return encoding;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public int getSampleSizeInBits() {
		return sampleSizeInBits;
	}

	public int getChannels() {
		return channels;
	}

	public int getFrameSize() {
		return frameSize;
	}

	public float getFrameRate() {
		return frameRate;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public Map<String, Object> properties() {
		return properties;
	}


	public Object getProperty(String key) {
		return (properties != null) ? properties.get(key) : null;
	}

	public boolean matches(AudioFormat format) {
		return true;
	}

	public static class Encoding {
		public static final Encoding PCM_SIGNED = new Encoding("PCM_SIGNED");
		public static final Encoding PCM_UNSIGNED = new Encoding("PCM_UNSIGNED");
		public static final Encoding PCM_FLOAT = new Encoding("PCM_FLOAT");
		public static final Encoding ULAW = new Encoding("ULAW");
		public static final Encoding ALAW = new Encoding("ALAW");

		private String name;

		public Encoding(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
