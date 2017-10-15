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

package com.jtransc;

import com.jtransc.annotation.JTranscSync;

public class JTranscEndian {
	static private boolean calculated = false;
	static private boolean _le = false;

	@JTranscSync
	static private void calculateOnce() {
		if (calculated) return;
		FastMemory m = FastMemory.alloc(4);
		m.setAlignedInt32(0, 0);
		m.setAlignedInt8(0, (byte) 1);
		_le = m.getAlignedInt32(0) == 1;
		calculated = true;
	}

	@JTranscSync
	static public boolean isLittleEndian() {
		calculateOnce();
		return _le;
	}

	@JTranscSync
	static public boolean isBigEndian() {
		calculateOnce();
		return !_le;
	}

	@JTranscSync
	static public short int16LE(short value) {
		return isLittleEndian() ? value : Short.reverseBytes(value);
	}

	@JTranscSync
	static public int int32LE(int value) {
		return isLittleEndian() ? value : Integer.reverseBytes(value);
	}

	@JTranscSync
	static public long int64LE(long value) {
		return isLittleEndian() ? value : Long.reverseBytes(value);
	}

	/*
	static public int int32BE(int value) {
		return isBigEndian() ? value : Integer.reverseBytes(value);
	}
	*/
}
