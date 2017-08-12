package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

public class AudioInputStream extends InputStream {
	protected AudioFormat format;
	protected long frameLength;
	protected int frameSize;
	protected long framePos;

	public AudioInputStream(InputStream stream, AudioFormat format, long length) {
		super();
		throw new RuntimeException("Not implemented");
	}

	public AudioInputStream(TargetDataLine line) {
		throw new RuntimeException("Not implemented");
	}


	native public AudioFormat getFormat();

	native public long getFrameLength();

	native public int read() throws IOException;

	native public int read(byte[] b) throws IOException;

	native public int read(byte[] b, int off, int len) throws IOException;

	native public long skip(long n) throws IOException;

	native public int available() throws IOException;

	native public void close() throws IOException;

	native public void mark(int readlimit);

	native public void reset() throws IOException;

	native public boolean markSupported();
}
