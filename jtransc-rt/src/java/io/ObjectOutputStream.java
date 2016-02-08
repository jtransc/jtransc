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

public class ObjectOutputStream extends OutputStream implements ObjectOutput {
	public ObjectOutputStream(OutputStream out) throws IOException {
	}

	protected ObjectOutputStream() throws IOException {
	}

	native public void useProtocolVersion(int version) throws IOException;

	native public final void writeObject(Object obj) throws IOException;

	//native protected void writeObjectOverride(Object obj) throws IOException;

	native public void writeUnshared(Object obj) throws IOException;

	native public void defaultWriteObject() throws IOException;

	native public ObjectOutputStream.PutField putFields() throws IOException;

	native public void writeFields() throws IOException;

	native public void reset() throws IOException;

	//native protected void annotateClass(Class<?> cl) throws IOException;

	//native protected void annotateProxyClass(Class<?> cl) throws IOException;

	//native protected Object replaceObject(Object obj);

	//native protected boolean enableReplaceObject(boolean enable);

	//native protected void writeStreamHeader() throws IOException;

	//native protected void writeClassDescriptor(ObjectStreamClass desc);

	native public void write(int val) throws IOException;

	native public void write(byte[] buf) throws IOException;

	native public void write(byte[] buf, int off, int len) throws IOException;

	native public void flush() throws IOException;

	//native protected void drain() throws IOException;

	native public void close() throws IOException;

	native public void writeBoolean(boolean val) throws IOException;

	native public void writeByte(int val) throws IOException;

	native public void writeShort(int val) throws IOException;

	native public void writeChar(int val) throws IOException;

	native public void writeInt(int val) throws IOException;

	native public void writeLong(long val) throws IOException;

	native public void writeFloat(float val) throws IOException;

	native public void writeDouble(double val) throws IOException;

	native public void writeBytes(String str) throws IOException;

	native public void writeChars(String str) throws IOException;

	native public void writeUTF(String str) throws IOException;

	native int getProtocolVersion();

	public static abstract class PutField {
		public abstract void put(String name, boolean val);

		public abstract void put(String name, byte val);

		public abstract void put(String name, char val);

		public abstract void put(String name, short val);

		public abstract void put(String name, int val);

		public abstract void put(String name, long val);

		public abstract void put(String name, float val);

		public abstract void put(String name, double val);

		public abstract void put(String name, Object val);

		public abstract void write(ObjectOutput out) throws IOException;
	}
}
