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

public class ObjectInputStream extends InputStream implements ObjectInput {
	public ObjectInputStream(InputStream in) throws IOException {
	}

	native public final Object readObject() throws IOException, ClassNotFoundException;

	native public Object readUnshared() throws IOException, ClassNotFoundException;

	native public void defaultReadObject() throws IOException, ClassNotFoundException;

	//native public ObjectInputStream.GetField readFields() throws IOException, ClassNotFoundException;

	native public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException;

	native public int read() throws IOException;

	native public int read(byte[] data, int offset, int length) throws IOException;

	native public int available() throws IOException;

	native public void close() throws IOException;

	native public boolean readBoolean() throws IOException;

	native public byte readByte() throws IOException;

	native public int readUnsignedByte() throws IOException;

	native public char readChar() throws IOException;

	native public short readShort() throws IOException;

	native public int readUnsignedShort() throws IOException;

	native public int readInt() throws IOException;

	native public long readLong() throws IOException;

	native public float readFloat() throws IOException;

	native public double readDouble() throws IOException;

	native public void readFully(byte[] buf) throws IOException;

	native public void readFully(byte[] buf, int off, int len) throws IOException;

	native public int skipBytes(int len) throws IOException;

	@Deprecated
	native public String readLine() throws IOException;

	native public String readUTF() throws IOException;
}
