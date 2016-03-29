package java.util.zip;

public interface Checksum {
	public void update(int b);

	public void update(byte[] b, int off, int len);

	public long getValue();

	public void reset();
}
