package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

public class AudioInputStream extends InputStream {
	private InputStream stream;
	protected AudioFormat format;
	protected long frameLength;
	protected int frameSize;
	protected long framePos;
	private long markpos;
	private byte[] pushBackBuffer = null;
	private int pushBackLen = 0;
	private byte[] markPushBackBuffer = null;
	private int markPushBackLen = 0;

	public AudioInputStream(InputStream stream, AudioFormat format, long length) {
		super();
		this.format = format;
		this.frameLength = length;
		this.frameSize = format.getFrameSize();
		this.stream = stream;
		framePos = 0;
		markpos = 0;
	}

	public AudioInputStream(TargetDataLine line) {
		format = line.getFormat();
		frameLength = AudioSystem.NOT_SPECIFIED;
		frameSize = format.getFrameSize();
		this.stream = new InputStream() {
			@Override
			native public int read() throws IOException;
		};
		framePos = 0;
		markpos = 0;
	}

	public AudioFormat getFormat() {
		return format;
	}

	public long getFrameLength() {
		return frameLength;
	}

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
