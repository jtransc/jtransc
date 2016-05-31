package com.jtransc.io;

import java.io.*;

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

	static public byte[] readFile(File file) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		try {
			return readStreamFully(fileInputStream);
		} finally {
			fileInputStream.close();
		}
	}

	static public <TOutputStream extends OutputStream> TOutputStream copy(InputStream is, TOutputStream os) throws IOException {
		byte[] chunk = new byte[64 * 1024];
		while (is.available() > 0) {
			int readed = is.read(chunk);
			os.write(chunk, 0, readed);
		}
		return os;
	}
}
