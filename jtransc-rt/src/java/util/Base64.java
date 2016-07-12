package java.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Base64 {
	static private Base64.Decoder decoder;
	static private Base64.Encoder encoder;

	static public Base64.Decoder getDecoder() {
		if (decoder == null) decoder = new Base64.Decoder();
		return decoder;
	}

	static public Base64.Encoder getEncoder() {
		if (encoder == null) encoder = new Base64.Encoder();
		return encoder;
	}

	native static public Base64.Decoder getMimeDecoder();

	native static public Base64.Encoder getMimeEncoder();

	native static public Base64.Encoder getMimeEncoder(int lineLength, byte[] lineSeparator);

	native static public Base64.Decoder getUrlDecoder();

	native static public Base64.Encoder getUrlEncoder();

	static public class Decoder {
		native public byte[] decode(byte[] src);

		native public int decode(byte[] src, byte[] dst);

		native public ByteBuffer decode(ByteBuffer buffer);

		native public byte[] decode(String src);

		native public InputStream wrap(InputStream is);
	}

	static public class Encoder {
		public native byte[] encode(byte[] src);

		public native int encode(byte[] src, byte[] dst);

		public native ByteBuffer encode(ByteBuffer buffer);

		public native String encodeToString(byte[] src);

		public native Base64.Encoder withoutPadding();

		public native OutputStream wrap(OutputStream os);
	}
}
