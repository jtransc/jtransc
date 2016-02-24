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


import jtransc.annotation.*;
import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;
import jtransc.annotation.haxe.HaxeRemoveField;

// Flash-compatible intrinsics that will use fastest inlined memory access
@HaxeAddMembers({ "public var data:haxe.io.BytesData;" })
final public class Mem {
    @HaxeRemoveField
	static private FastMemory mem;

	@JTranscInline
    @HaxeMethodBody("HaxeNatives.memSelect(p0._data);")
	static public void select(FastMemory mem) {
		Mem.mem = mem;
	}

    @JTranscInline
    @HaxeMethodBody("return HaxeNatives.memLi8(p0);")
	static public byte li8(int address) {
		return (byte) mem.getAlignedInt8(address);
	}

	@JTranscInline
    @HaxeMethodBody("return HaxeNatives.memLi16(p0);")
	static public short li16(int address2) {
		return (short) mem.getAlignedInt16(address2);
	}

	@JTranscInline
    @HaxeMethodBody("return HaxeNatives.memLi32(p0);")
	static public int li32(int address4) {
		return mem.getAlignedInt32(address4);
	}

	@JTranscInline
    @HaxeMethodBody("return HaxeNatives.memLf32(p0);")
	static public float lf32(int address4) {
		return mem.getAlignedFloat32(address4);
	}

	@JTranscInline
    @HaxeMethodBody("return HaxeNatives.memLf64(p0);")
	static public double lf64(int address8) {
		return mem.getAlignedFloat64(address8);
	}

	@JTranscInline
    @HaxeMethodBody("HaxeNatives.memSi8(p0, p1);")
	static public void si8(int address, byte value) {
		mem.setAlignedInt8(address, value);
	}

	@JTranscInline
    @HaxeMethodBody("HaxeNatives.memSi16(p0, p1);")
	static public void si16(int address2, short value) {
		mem.setAlignedInt16(address2, value);
	}

	@JTranscInline
    @HaxeMethodBody("HaxeNatives.memSi32(p0, p1);")
	static public void si32(int address4, int value) {
		mem.setAlignedInt32(address4, value);
	}

	@JTranscInline
    @HaxeMethodBody("HaxeNatives.memSf32(p0, p1);")
	static public void sf32(int address4, float value) {
		mem.setAlignedFloat32(address4, value);
	}

	@JTranscInline
    @HaxeMethodBody("HaxeNatives.memSf64(p0, p1);")
	static public void sf64(int address8, double value) {
		mem.setAlignedFloat64(address8, value);
	}

    @JTranscInline
    @HaxeMethodBody("return HaxeNatives.memSxi1(p0);")
	static public int sxi1(int value) {
		return (value << 31) >> 31;
	}

	@JTranscInline
    @HaxeMethodBody("return HaxeNatives.memSxi8(p0);")
	static public int sxi8(int value) {
		return (value << 24) >> 24;
	}

	@JTranscInline
    @HaxeMethodBody("return HaxeNatives.memSxi16(p0);")
	static public int sxi16(int value) {
		return (value << 16) >> 16;
	}
}
