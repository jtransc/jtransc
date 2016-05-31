package java.util.zip;

public class CRC32 implements Checksum {
	private com.jtransc.compression.jzlib.CRC32 impl = new com.jtransc.compression.jzlib.CRC32();

	public CRC32() {
	}

	static private byte[] temp = new byte[1];

	public void update(int b) {
		temp[0] = (byte) b;
		update(temp, 0, 1);
	}

	public void update(byte[] b) {
		update(b, 0, b.length);
	}

	public void update(byte[] b, int off, int len) {
		impl.update(b, off, len);
	}

	public void reset() {
		impl.reset();
	}

	public long getValue() {
		return impl.getValue();
	}
}