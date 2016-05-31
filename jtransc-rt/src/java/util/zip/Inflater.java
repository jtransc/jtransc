package java.util.zip;

import com.jtransc.compression.JTranscInflater;

public class Inflater {
	private final JTranscInflater n;

	public Inflater(boolean nowrap) {
		n = new JTranscInflater(nowrap);
	}

	public Inflater() {
		this(false);
	}

	public void setInput(byte[] b, int off, int len) {
		this.n.setInput(b, off, len);
	}

	public void setInput(byte[] b) {
		this.n.setInput(b);
	}

	public void setDictionary(byte[] b, int off, int len) {
		this.n.setDictionary(b, off, len);
	}

	public void setDictionary(byte[] b) {
		this.n.setDictionary(b);
	}

	public int getRemaining() {
		return this.n.getRemaining();
	}

	public boolean needsInput() {
		return this.n.needsInput();
	}

	public boolean needsDictionary() {
		return this.n.needsDictionary();
	}

	public boolean finished() {
		return this.n.finished();
	}

	public int inflate(byte[] b, int off, int len) throws DataFormatException {
		return this.n.inflate(b, off, len);
	}

	public int inflate(byte[] b) throws DataFormatException {
		return this.n.inflate(b);
	}

	public int getAdler() {
		return this.n.getAdler();
	}

	public int getTotalIn() {
		return this.n.getTotalIn();
	}

	public long getBytesRead() {
		return this.n.getBytesRead();
	}

	public int getTotalOut() {
		return this.n.getTotalOut();
	}

	public long getBytesWritten() {
		return this.n.getBytesWritten();
	}

	public void reset() {
		this.n.reset();
	}

	public void end() {
		this.n.end();
	}

	protected void finalize() {
		//this.n.finalize();
	}
}
