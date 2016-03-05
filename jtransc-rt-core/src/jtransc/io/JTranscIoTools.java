package jtransc.io;

import java.io.IOException;
import java.io.InputStream;

public class JTranscIoTools {
	static public byte[] readStreamFully(InputStream ios) {
		int length = 0;
		try {
			length = ios.available();
			byte[] out = new byte[length];
			int offset = 0;
			while (offset < length) {
				int readed = ios.read(out, offset, length - offset);
				if (readed < 0) throw new IOException("Can't read file");
				offset += readed;
			}
			ios.close();
			return out;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
