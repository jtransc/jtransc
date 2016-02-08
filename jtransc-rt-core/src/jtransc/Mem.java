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


import jtransc.annotation.JTranscInline;

// Flash-compatible intrinsics that will use fastest inlined memory access
final public class Mem {
	static private FastMemory mem;

	@JTranscInline
	static public void select(FastMemory mem) {
		Mem.mem = mem;
	}

	@JTranscInline
	static public byte li8(int address) {
		return (byte) mem.getAlignedInt8(address);
	}

	@JTranscInline
	static public short li16(int address2) {
		return (short) mem.getAlignedInt16(address2);
	}

	@JTranscInline
	static public int li32(int address4) {
		return mem.getAlignedInt32(address4);
	}

	@JTranscInline
	static public float lf32(int address4) {
		return mem.getAlignedFloat32(address4);
	}

	@JTranscInline
	static public double lf64(int address8) {
		return mem.getAlignedFloat64(address8);
	}

	@JTranscInline
	static public void si8(int address, byte value) {
		mem.setAlignedInt8(address, value);
	}

	@JTranscInline
	static public void si16(int address2, short value) {
		mem.setAlignedInt16(address2, value);
	}

	@JTranscInline
	static public void si32(int address4, int value) {
		mem.setAlignedInt32(address4, value);
	}

	@JTranscInline
	static public void sf32(int address4, float value) {
		mem.setAlignedFloat32(address4, value);
	}

	@JTranscInline
	static public void sf64(int address8, double value) {
		mem.setAlignedFloat64(address8, value);
	}

	@JTranscInline
	static public int sxi1(int value) {
		return (value << 31) >> 31;
	}

	@JTranscInline
	static public int sxi8(int value) {
		return (value << 24) >> 24;
	}

	@JTranscInline
	static public int sxi16(int value) {
		return (value << 16) >> 16;
	}
}
