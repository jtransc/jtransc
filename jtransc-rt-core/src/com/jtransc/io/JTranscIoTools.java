package com.jtransc.io;

import java.io.*;

public class JTranscIoTools {
	static public synchronized byte[] readStreamFully(InputStream ios) {
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

	static public synchronized byte[] readFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		try {
			return readStreamFully(fis);
		} finally {
			fis.close();
		}
	}

	static public synchronized void writeFile(File file, byte[] data) throws IOException {
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

	public static synchronized long skipByReading(InputStream in, long byteCount) throws IOException {
		byte[] buffer = new byte[4096];
		long skipped = 0;
		while (skipped < byteCount) {
			int toRead = (int) Math.min(byteCount - skipped, buffer.length);
			int read = in.read(buffer, 0, toRead);
			if (read == -1) break;
			skipped += read;
			if (read < toRead) break;
		}
		return skipped;
	}

	public static synchronized void readFully(InputStream in, byte[] out, int offset, int length) throws IOException {
		int left = length;
		while (left > 0) {
			int readed = in.read(out, offset, left);
			if (readed < 0) throw new IOException();
			offset += readed;
			left -= readed;
		}
	}
}
