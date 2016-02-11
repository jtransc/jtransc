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
import jtransc.internal.JTranscTempBuffer;

public class DataInputStream extends FilterInputStream implements DataInput {
	static private byte[] temp = new byte[8];

	public DataInputStream(InputStream in) {
		super(in);
	}

	public final int read(byte data[]) throws IOException {
		return in.read(data, 0, data.length);
	}

	public final int read(byte data[], int offset, int length) throws IOException {
		return in.read(data, offset, length);
	}

	public final void readFully(byte data[]) throws IOException {
		readFully(data, 0, data.length);
	}

	public final void readFully(byte data[], int offset, int length) throws IOException {
		if (length < 0) throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < length) {
			int count = in.read(data, offset + n, length - n);
			if (count < 0) throw new EOFException();
			n += count;
		}
	}

	public final int skipBytes(int n) throws IOException {
		int totalSkipped = 0;
		while (totalSkipped < n) {
			int currentSkipped = (int) in.skip(n - totalSkipped);
			if (currentSkipped <= 0) break;
			totalSkipped += currentSkipped;
		}
		return totalSkipped;
	}

	private int _readUnsignedByte() throws IOException {
		int ch = in.read();
		if (ch < 0) throw new EOFException();
		return (ch);
	}

	private int _readUnsignedShort() throws IOException {
		readFully(temp, 0, 2);
		return JTranscBits.makeShort(temp);
	}

	private int _readInt() throws IOException {
		readFully(temp, 0, 4);
		return JTranscBits.makeInt(temp);
	}

	public final boolean readBoolean() throws IOException {
		return _readUnsignedByte() != 0;
	}

	public final byte readByte() throws IOException {
		return (byte) _readUnsignedByte();
	}

	public final int readUnsignedByte() throws IOException {
		return _readUnsignedByte();
	}

	public final short readShort() throws IOException {
		return (short) _readUnsignedShort();
	}

	public final int readUnsignedShort() throws IOException {
		return _readUnsignedShort();
	}

	public final char readChar() throws IOException {
		return (char) _readUnsignedShort();
	}

	public final int readInt() throws IOException {
		return _readInt();
	}

	public final long readLong() throws IOException {
		byte[] temp = JTranscTempBuffer.tempByte(8);
		readFully(temp, 0, 8);
		return JTranscBits.makeLong(temp);
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Deprecated
	native public final String readLine() throws IOException;

	public final String readUTF() throws IOException {
		return readUTF(this);
	}

	public static String readUTF(DataInput in) throws IOException {
		int len = in.readUnsignedShort();
		byte[] temp = JTranscTempBuffer.tempByte(len);
		in.readFully(temp, 0, len);
		return new String(temp, "utf-8");
	}
}
