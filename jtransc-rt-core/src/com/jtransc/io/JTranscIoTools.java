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
		FileInputStream fis = new FileInputStream(file);
		try {
			return readStreamFully(fis);
		} finally {
			fis.close();
		}
	}

	static public void writeFile(File file, byte[] data) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);
		try {
			fos.write(data);
		} finally {
			fos.close();
		}
	}

	static public <TOutputStream extends OutputStream> TOutputStream copy(InputStream is, TOutputStream os) throws IOException {
		copyLarge(is, os, new byte[64 * 1024]);
		return os;
	}

	public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
		throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	private static final int EOF = -1;

	public static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
