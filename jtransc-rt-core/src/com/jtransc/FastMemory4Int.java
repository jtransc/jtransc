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
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@JTranscInvisible
final public class FastMemory4Int {
	private FastMemory mem;

	@JTranscSync
	public FastMemory4Int(FastMemory mem) {
		this.mem = mem;
	}

	@JTranscInline
    @HaxeMethodBody("return N.idiv(this{% IFIELD com.jtransc.FastMemory4Int:mem %}._length, 4);")
	@JTranscSync
	final public int getLength() {
		return mem.getLength() / 4;
	}

	@JTranscInline
    @HaxeMethodBody("return this{% IFIELD com.jtransc.FastMemory4Int:mem %}.intData.get(p0);")
	@JTranscSync
	final public int get(int index) {
		return mem.getAlignedInt32(index);
	}

	@JTranscInline
    @HaxeMethodBody("this{% IFIELD com.jtransc.FastMemory4Int:mem %}.intData.set(p0, p1);")
	@JTranscSync
	final public void set(int index, int value) {
		mem.setAlignedInt32(index, value);
	}
}
