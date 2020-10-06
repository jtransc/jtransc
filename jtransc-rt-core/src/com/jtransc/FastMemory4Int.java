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

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscInvisible;


@JTranscInvisible
final public class FastMemory4Int {
	private FastMemory mem;

	public FastMemory4Int(FastMemory mem) {
		this.mem = mem;
	}

	@JTranscInline

	final public int getLength() {
		return mem.getLength() / 4;
	}

	@JTranscInline

	final public int get(int index) {
		return mem.getAlignedInt32(index);
	}

	@JTranscInline

	final public void set(int index, int value) {
		mem.setAlignedInt32(index, value);
	}
}
