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
import com.jtransc.annotation.JTranscSync;

// Flash-compatible intrinsics that will use fastest inlined memory access
final public class Mem {
	static private FastMemory mem;

	@JTranscSync
	static public void select(FastMemory mem) {
		Mem.mem = mem;
	}

	@JTranscInline
	@JTranscSync
	static public int li8(int address) {
		return mem.getAlignedInt8(address);
	}

	@JTranscInline
	@JTranscSync
	static public int li16(int address2) {
		return mem.getAlignedInt16(address2);
	}

	@JTranscInline
	@JTranscSync
	static public int li32(int address4) {
		return mem.getAlignedInt32(address4);
	}

	@JTranscInline
	@JTranscSync
	static public float lf32(int address4) {
		return mem.getAlignedFloat32(address4);
	}

	@JTranscInline
	@JTranscSync
	static public double lf64(int address8) {
		return mem.getAlignedFloat64(address8);
	}

	@JTranscInline
	@JTranscSync
	static public void si8(int address, int value) {
		mem.setAlignedInt8(address, value);
	}

	@JTranscInline
	@JTranscSync
	static public void si16(int address2, int value) {
		mem.setAlignedInt16(address2, value);
	}

	@JTranscInline
	@JTranscSync
	static public void si32(int address4, int value) {
		mem.setAlignedInt32(address4, value);
	}

	@JTranscInline
	@JTranscSync
	static public void sf32(int address4, float value) {
		mem.setAlignedFloat32(address4, value);
	}

	@JTranscInline
	@JTranscSync
	static public void sf64(int address8, double value) {
		mem.setAlignedFloat64(address8, value);
	}

	@JTranscInline
	@JTranscSync
	static public int sxi1(int value) {
		return JTranscBits.sxi1(value);
	}

	@JTranscInline
	@JTranscSync
	static public int sxi8(int value) {
		return JTranscBits.sxi8(value);
	}

	@JTranscInline
	@JTranscSync
	static public int sxi16(int value) {
		return JTranscBits.sxi16(value);
	}
}
