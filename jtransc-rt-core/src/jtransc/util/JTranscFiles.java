package jtransc.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
}
