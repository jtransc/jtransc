package com.jtransc.util;

import com.jtransc.io.JTranscConsole;

import java.io.*;
import java.util.Arrays;

public class JTranscFiles {
	static public boolean write(File file, byte[] data) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	static public byte[] read(File file) throws IOException {
		byte[] out = new byte[(int) file.length()];
		FileInputStream s = new FileInputStream(file);
		int readed = s.read(out);
		s.close();

		return (readed != out.length) ? Arrays.copyOf(out, readed) : out;
	}
}
