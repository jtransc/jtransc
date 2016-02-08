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

package jtransc;

public class JTranscData {
	static public byte[] getInt64BE(byte[] data, long v) {
		data[0] = (byte)(v >> 56);
		data[1] = (byte)(v >> 48);
		data[2] = (byte)(v >> 40);
		data[3] = (byte)(v >> 32);
		data[4] = (byte)(v >> 24);
		data[5] = (byte)(v >> 16);
		data[6] = (byte)(v >> 8);
		data[7] = (byte)(v >> 0);
		return data;
	}

	static public byte[] getInt32BE(byte[] data, int v) {
		data[0] = (byte)(v >> 24);
		data[1] = (byte)(v >> 16);
		data[2] = (byte)(v >> 8);
		data[3] = (byte)(v >> 0);
		return data;
	}

	static public byte[] getInt16BE(byte[] data, short v) {
		data[0] = (byte)(v >> 8);
		data[1] = (byte)(v >> 0);
		return data;
	}

	static public byte[] getInt8BE(byte[] data, byte v) {
		data[0] = v;
		return data;
	}

	static public short readInt16BE(byte[] data) {
		return (short)((data[0] << 8) | (data[1] & 0xFF));
	}

	static public int readInt32BE(byte[] data) {
		return (int)((data[0] << 24) + (data[1] << 16) + (data[2] << 8) + (data[3] << 0));
	}
}
