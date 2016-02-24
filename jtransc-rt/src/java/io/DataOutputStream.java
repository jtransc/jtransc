/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.io;

import jtransc.JTranscBits;

public class DataOutputStream extends FilterOutputStream implements DataOutput {
	protected int written;
	static private byte[] temp = new byte[8];

	public DataOutputStream(OutputStream out) {
		super(out);
	}

	public synchronized void write(int value) throws IOException {
		out.write(value);
		written++;
	}

	public synchronized void write(byte value[], int offset, int length) throws IOException {
		out.write(value, offset, length);
		written += length;
	}

	public void flush() throws IOException {
		out.flush();
	}

	private void _writeByte(int value) throws IOException {
		out.write(value);
		written++;
	}

	public final void writeBoolean(boolean value) throws IOException {
		_writeByte(value ? 1 : 0);
	}

	public final void writeByte(int value) throws IOException {
		_writeByte(value);
	}

	public final void writeShort(int value) throws IOException {
		JTranscBits.writeShort(temp, (short) value);
		out.write(temp, 0, 2);
		written += 2;
	}

	public final void writeChar(int value) throws IOException {
		JTranscBits.writeShort(temp, (short) value);
		out.write(temp, 0, 2);
		written += 2;
	}

	public final void writeInt(int value) throws IOException {
		JTranscBits.writeInt(temp, value);
		out.write(temp, 0, 4);
		written += 4;
	}

	public final void writeLong(long value) throws IOException {
		JTranscBits.writeLong(temp, value);
		out.write(temp, 0, 8);
		written += 8;
	}

	public final void writeFloat(float value) throws IOException {
		writeInt(Float.floatToIntBits(value));
	}

	public final void writeDouble(double value) throws IOException {
		writeLong(Double.doubleToLongBits(value));
	}

	public final void writeBytes(String value) throws IOException {
		for (int i = 0; i < value.length(); i++) writeByte((byte) value.charAt(i));
	}

	public final void writeChars(String value) throws IOException {
		for (int i = 0; i < value.length(); i++) writeChar(value.charAt(i));
	}

	public final void writeUTF(String value) throws IOException {
		writeUTF(value, this);
	}

	static int writeUTF(String value, DataOutput out) throws IOException {
		byte[] bytes = value.getBytes("UTF-8");
		out.write(bytes);
		return bytes.length;
	}

	public final int size() {
		return written;
	}
}
