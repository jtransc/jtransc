package com.jtransc.io.ra;

import com.jtransc.JTranscBits;
import com.jtransc.io.SizeOf;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

abstract public class RAStream {
	private long position = 0;

	public final void setPosition(long position) {
		this.position = position;
	}

	public final long getPosition() {
		return this.position;
	}

	public final long getAvailable() {
		return getLength() - getPosition();
	}

	public final void skip(long count) {
		this.position += Math.min(getAvailable(), count);
	}

	public final int read(byte[] ref, int pos, int len) {
		int readed = read(position, ref, pos, len);
		if (readed > 0) position += readed;
		return readed;
	}

	public final void write(byte[] ref, int pos, int len) {
		write(position, ref, pos, len);
		position += len;
	}

	private byte[] scratch = new byte[8];

	public int readU8() {
		return readS8() & 0xFF;
	}

	public int readU8_LE() {
		return readU8();
	}

	public int readU8_BE() {
		return readS8_BE() & 0xFF;
	}

	public int readU16_LE() {
		return readS16_LE() & 0xFFFF;
	}

	public int readU16_BE() {
		return readS16_BE() & 0xFFFF;
	}

	public long readU32_LE() {
		return (long) readS32_LE() & 0xFFFFFFFFL;
	}

	public long readU32_BE() {
		return (long) readS32_BE() & 0xFFFFFFFFL;
	}

	public byte readS8_LE() {
		return readS8();
	}

	public short readS16_LE() {
		this.read(scratch, 0, SizeOf.SHORT);
		return JTranscBits.readInt16LE(scratch, 0);
	}

	public int readS32_LE() {
		this.read(scratch, 0, SizeOf.INT);
		return JTranscBits.readInt32LE(scratch, 0);
	}

	public long readS64_LE() {
		this.read(scratch, 0, SizeOf.LONG);
		return JTranscBits.readInt64LE(scratch, 0);
	}

	public byte readS8() {
		this.read(scratch, 0, SizeOf.BYTE);
		return scratch[0];
	}

	public byte readS8_BE() {
		return readS8();
	}

	public short readS16_BE() {
		this.read(scratch, 0, SizeOf.SHORT);
		return JTranscBits.readInt16BE(scratch, 0);
	}

	public int readS32_BE() {
		this.read(scratch, 0, SizeOf.INT);
		return JTranscBits.readInt32BE(scratch, 0);
	}

	public long readS64_BE() {
		this.read(scratch, 0, SizeOf.LONG);
		return JTranscBits.readInt64BE(scratch, 0);
	}

	public byte[] readBytes(int count) {
		byte[] out = new byte[count];
		int readed = read(out, 0, out.length);
		return Arrays.copyOf(out, readed);
	}

	public byte[] readBytesExact(int count) {
		byte[] out = new byte[count];
		int pos = 0;
		while (pos < count) {
			int readed = read(out, pos, out.length - pos);
			if (readed <= 0) break;
			pos += readed;
		}
		if (pos < count) throw new RuntimeException("Can't read bytes " + count + " just able to read " + pos);
		return out;
	}

	public byte[] readBytes(long count) {
		return readBytes((int)count);
	}

	public String readStringz(int count, Charset charset) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		while (count > 0) {
			int c = this.readS8_LE();
			count--;
			if (c <= 0) break;
			out.write(c);
		}

		skip(count);

		return new String(out.toByteArray(), charset);
	}

	public RASlice readSlice(long count) {
		long actualCount = Math.min(count, getAvailable());
		RASlice out = new RASlice(this, getPosition(), getPosition() + actualCount);
		setPosition(getPosition() + actualCount);
		return out;
	}

	public RASlice slice() {
		return new RASlice(this, 0L, length());
	}

	public RASlice slice(long start, long end) {
		return new RASlice(this, start, end);
	}

	public RASlice sliceWithSize(long start, long size) {
		return new RASlice(this, start, start + size);
	}

	public RASlice sliceAvailable(long start) {
		return new RASlice(this, start, this.getLength());
	}

	public byte[] readAvailableBytes() {
		return readBytesExact((int)getAvailable());
	}

	abstract public void setLength(long length);
	abstract public long getLength();
	abstract protected int read(long position, byte[] ref, int pos, int len);
	abstract protected void write(long position, byte[] ref, int pos, int len);

	public void close() {
	}

	public final InputStream createInputStream() {
		return new InputStream() {
			byte[] scratch = new byte[1];

			@Override
			public int read() throws IOException {
				int count = read(scratch, 0, 1);
				return (count <= 0) ? -1 : scratch[0];
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return RAStream.this.read(b, off, len);
			}
		};
	}

	public byte[] getAllBytes() {
		int length = (int)this.getLength();
		byte[] out = new byte[length];
		read(0L, out, 0, length);
		return out;
	}

	public final OutputStream createOutputStream() {
		// @TODO
		throw new RuntimeException("RAStream.createOutputStream not implemented!!");
	}

	public final DataInputStream createDataInput() {
		return new DataInputStream(createInputStream());
	}

	public final DataOutputStream createDataOutput() {
		return new DataOutputStream(createOutputStream());
	}

	public final long length() {
		return getLength();
	}

	public boolean eof() {
		return getAvailable() <= 0;
	}
}
