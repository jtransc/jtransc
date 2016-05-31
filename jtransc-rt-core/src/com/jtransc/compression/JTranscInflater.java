package com.jtransc.compression;

import com.jtransc.compression.jzlib.GZIPException;
import com.jtransc.compression.jzlib.Inflater;
import com.jtransc.compression.jzlib.JZlib;

import java.util.Arrays;
import java.util.Objects;
import java.util.zip.DataFormatException;

public class JTranscInflater {
	private com.jtransc.compression.jzlib.Inflater inf;
	private boolean needDict;
	private long bytesRead;
	private long bytesWritten;

	private static final byte[] defaultBuf = new byte[0];

	private boolean nowrap;

	public JTranscInflater(boolean nowrap) {
		try {
			this.nowrap = nowrap;
			inf = new com.jtransc.compression.jzlib.Inflater(nowrap);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public JTranscInflater() {
		this(false);
	}

	public void setInput(byte[] b, int off, int len) {
		Objects.requireNonNull(b);
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		inf.setInput(b, off, len, true);
	}

	public void setInput(byte[] b) {
		setInput(b, 0, b.length);
	}

	public void setDictionary(byte[] b, int off, int len) {
		Objects.requireNonNull(b);
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		inf.setDictionary(b, off, len);
		needDict = false;
	}

	public void setDictionary(byte[] b) {
		setDictionary(b, 0, b.length);
	}

	public int getRemaining() {
		return inf.getAvailIn();
	}

	public boolean needsInput() {
		return getRemaining() <= 0;
	}

	public boolean needsDictionary() {
		return needDict;
	}

	public boolean finished() {
		return inf.finished();
	}

	public int inflate(byte[] b, int off, int len)
		throws DataFormatException {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0 || len < 0 || off > b.length - len) {
			throw new ArrayIndexOutOfBoundsException();
		}
		int thisLen = getRemaining();
		inf.setOutput(b, off, len);

		long outstart = inf.getTotalOut();
		inf.inflate(len);
		long outend = inf.getTotalOut();
		int n = (int)(outend - outstart);
		bytesWritten += n;
		bytesRead += (thisLen - getRemaining());
		return n;
	}

	public int inflate(byte[] b) throws DataFormatException {
		return inflate(b, 0, b.length);
	}

	public int getAdler() {
		return (int) inf.getAdler();
	}

	public int getTotalIn() {
		return (int) getBytesRead();
	}

	public long getBytesRead() {
		return bytesRead;
	}

	public int getTotalOut() {
		return (int) getBytesWritten();
	}

	public long getBytesWritten() {
		return bytesWritten;
	}

	public void reset() {
		inf.free();
		inf.init(nowrap);
		needDict = true;
		bytesRead = 0;
		bytesWritten = 0;
	}

	public void end() {
		//inf.inflateEnd()
		inf.end();
	}
}
