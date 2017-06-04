package com.jtransc.io.ra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RAFile extends RAStream {
	public RandomAccessFile file;

	public RAFile(RandomAccessFile file) {
		this.file = file;
	}

	public RAFile(File file) {
		try {
			//this.file = new RandomAccessFile(new File(file.getAbsolutePath().replace("%20", " ")), "r");
			this.file = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setLength(long length) {
		try {
			this.file.setLength(length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getLength() {
		try {
			return this.file.length();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected int read(long position, byte[] ref, int pos, int len) {
		try {
			this.file.seek(position);
			return this.file.read(ref, pos, len);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void write(long position, byte[] ref, int pos, int len) {
		try {
			this.file.seek(position);
			this.file.write(ref, pos, len);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		try {
			this.file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
