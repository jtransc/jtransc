package java.nio.charset;

public class ModifiedUtf8 {
	public static void encode(byte[] out, int offset, String str) {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		System.arraycopy(bytes, 0, out, offset, bytes.length);
	}

	public static long countBytes(String str, boolean b) {
		return str.getBytes(StandardCharsets.UTF_8).length;
	}

	public static byte[] encode(String str) {
		return str.getBytes(StandardCharsets.UTF_8);
	}

	public static String decode(byte[] in, char[] temp, int offset, int bytes) {
		return new String(in, offset, bytes, StandardCharsets.UTF_8);
	}
}
