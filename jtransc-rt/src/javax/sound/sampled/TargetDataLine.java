package javax.sound.sampled;

public interface TargetDataLine extends DataLine {
	void open(AudioFormat format, int bufferSize) throws LineUnavailableException;

	void open(AudioFormat format) throws LineUnavailableException;

	int read(byte[] b, int off, int len);
}
