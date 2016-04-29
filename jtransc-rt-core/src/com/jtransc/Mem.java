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
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeRemoveField;

// Flash-compatible intrinsics that will use fastest inlined memory access
@HaxeAddMembers({"" +
	"public var data:haxe.io.BytesData;\n" +
	"#if !flash\n" +
	"static private var byteMem:haxe.io.Bytes;\n" +
	"static private var shortMem:haxe.io.UInt16Array;\n" +
	"static private var intMem:haxe.io.Int32Array;\n" +
	"static private var floatMem:haxe.io.Float32Array;\n" +
	"static private var doubleMem:haxe.io.Float64Array;\n" +
	"#end"
})
final public class Mem {
	@HaxeRemoveField
	static private FastMemory mem;

	@JTranscInline
	@HaxeMethodBody("" +
		"var mem = p0._data;\n" +
		"#if flash\n" +
		"flash.Memory.select(mem.getData());\n" +
		"#else\n" +
		"byteMem   = mem;\n" +
		"shortMem  = haxe.io.UInt16Array.fromBytes(mem);\n" +
		"intMem    = haxe.io.Int32Array.fromBytes(mem);\n" +
		"floatMem  = haxe.io.Float32Array.fromBytes(mem);\n" +
		"doubleMem = haxe.io.Float64Array.fromBytes(mem);\n" +
		"#end"
	)
	static public void select(FastMemory mem) {
		Mem.mem = mem;
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.getByte(p0 << 0); #else byteMem.get(p0); #end")
	static public byte li8(int address) {
		return mem.getAlignedInt8(address);
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.getUI16(p0 << 1); #else shortMem.get(p0); #end")
	static public short li16(int address2) {
		return mem.getAlignedInt16(address2);
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.getI32(p0 << 2); #else intMem.get(p0); #end")
	static public int li32(int address4) {
		return mem.getAlignedInt32(address4);
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.getFloat(p0 << 2); #else floatMem.get(p0); #end")
	static public float lf32(int address4) {
		return mem.getAlignedFloat32(address4);
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.getDouble(p0 << 3); #else floatMem.get(p0); #end\n")
	static public double lf64(int address8) {
		return mem.getAlignedFloat64(address8);
	}

	@JTranscInline
	@HaxeMethodBody("#if flash flash.Memory.setByte(p0 << 0, p1); #else byteMem.set(p0, p1); #end")
	static public void si8(int address, byte value) {
		mem.setAlignedInt8(address, value);
	}

	@JTranscInline
	@HaxeMethodBody("#if flash flash.Memory.setI16(p0 << 1, p1); #else shortMem.set(p0, p1); #end")
	static public void si16(int address2, short value) {
		mem.setAlignedInt16(address2, value);
	}

	@JTranscInline
	@HaxeMethodBody("#if flash flash.Memory.setI32(p0 << 2, p1); #else intMem.set(p0, p1); #end")
	static public void si32(int address4, int value) {
		mem.setAlignedInt32(address4, value);
	}

	@JTranscInline
	@HaxeMethodBody("#if flash flash.Memory.setFloat(p0 << 2, p1); #else floatMem.set(p0, p1); #end")
	static public void sf32(int address4, float value) {
		mem.setAlignedFloat32(address4, value);
	}

	@JTranscInline
	@HaxeMethodBody("#if flash flash.Memory.setDouble(p0 << 3, p1); #else floatMem.set(p0, p1); #end")
	static public void sf64(int address8, double value) {
		mem.setAlignedFloat64(address8, value);
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.signExtend1(p0); #else N.signExtend(p0, 1); #end")
	static public int sxi1(int value) {
		return (value << 31) >> 31;
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.signExtend8(p0); #else N.i2b(p0); #end")
	static public int sxi8(int value) {
		return (value << 24) >> 24;
	}

	@JTranscInline
	@HaxeMethodBody("return #if flash flash.Memory.signExtend16(p0); #else N.i2s(p0); #end")
	static public int sxi16(int value) {
		return (value << 16) >> 16;
	}
}
