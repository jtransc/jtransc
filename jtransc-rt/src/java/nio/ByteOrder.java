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

package java.nio;

import jtransc.JTranscBits;

public final class ByteOrder {
	public static final ByteOrder BIG_ENDIAN = new ByteOrder("BIG_ENDIAN");
	public static final ByteOrder LITTLE_ENDIAN = new ByteOrder("LITTLE_ENDIAN");

	public static ByteOrder nativeOrder() {
		return JTranscBits.isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
	}

	private String name;

	private ByteOrder(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
}
