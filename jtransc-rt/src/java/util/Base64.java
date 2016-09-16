package java.util;

import com.jtransc.util.JTranscBase64;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Base64 {
	static private Base64.Decoder decoder;
	static private Base64.Encoder encoder;

	static private Base64.Decoder mimeDecoder;
	static private Base64.Encoder mimeEncoder;

	static private Base64.Decoder urlDecoder;
	static private Base64.Encoder urlEncoder;

	static public Base64.Decoder getDecoder() {
		if (decoder == null) decoder = new Base64.Decoder();
		return decoder;
	}

	static public Base64.Encoder getEncoder() {
		if (encoder == null) encoder = new Base64.Encoder();
		return encoder;
	}

	static public Base64.Decoder getMimeDecoder() {
		if (mimeDecoder == null) mimeDecoder = new Base64.Decoder();
		return mimeDecoder;
	}

	static public Base64.Encoder getMimeEncoder() {
		if (mimeEncoder == null) mimeEncoder = new Base64.Encoder();
		return mimeEncoder;
	}

	static public Base64.Encoder getMimeEncoder(int lineLength, byte[] lineSeparator) {
		return new Base64.Encoder();
	}

	static public Base64.Decoder getUrlDecoder() {
		if (urlDecoder == null) urlDecoder = new Base64.Decoder();
		return urlDecoder;
	}

	static public Base64.Encoder getUrlEncoder() {
		if (urlEncoder == null) urlEncoder = new Base64.Encoder();
		return urlEncoder;
	}


	static public class Decoder {

		public byte[] decode(byte[] src) {
			byte[] out = new byte[src.length];
			return Arrays.copyOf(out, decode(src, out));
		}

		public int decode(byte[] src, byte[] dst) {
			return JTranscBase64.decode(src, dst);
		}

		native public ByteBuffer decode(ByteBuffer buffer);

		public byte[] decode(String src) {
			return decode(src.getBytes());
		}

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
