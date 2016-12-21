package com.jtransc.text;

import java.io.IOException;
import java.io.Reader;

public class JTranscReaderTools {
	static public String readAllOrNull(Reader reader) {
		try {
			return readAll(reader);
		} catch (IOException e) {
			return null;
		}
	}

	static public String readAll(Reader reader) throws IOException {
		char[] arr = new char[8 * 1024];
		StringBuilder buffer = new StringBuilder();
		int numCharsRead;
		while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
			buffer.append(arr, 0, numCharsRead);
		}
		reader.close();
		return buffer.toString();
	}
}
