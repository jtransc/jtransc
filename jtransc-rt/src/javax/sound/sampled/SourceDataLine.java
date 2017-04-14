package javax.sound.sampled;

@SuppressWarnings("unused")
public interface SourceDataLine extends DataLine {
	void open(AudioFormat format, int bufferSize) throws LineUnavailableException;

	void open(AudioFormat format) throws LineUnavailableException;

	int write(byte[] b, int off, int len);
}
