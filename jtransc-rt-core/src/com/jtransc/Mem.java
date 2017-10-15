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
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeMethodBodyList;
import com.jtransc.annotation.haxe.HaxeRemoveField;

// Flash-compatible intrinsics that will use fastest inlined memory access
@HaxeAddMembers({"" +
	"static private var bytes:haxe.io.Bytes;\n" +
	"static private var byteMem:haxe.io.UInt8Array;\n" +
	"static private var shortMem:haxe.io.UInt16Array;\n" +
	"static private var intMem:haxe.io.Int32Array;\n" +
	"static private var floatMem:haxe.io.Float32Array;\n" +
	"static private var doubleMem:haxe.io.Float64Array;\n"
})
final public class Mem {
	@HaxeRemoveField
	static private FastMemory mem;

	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "flash.Memory.select(p0._data.getData());"),
		@HaxeMethodBody("" +
			"var mem   = p0._data;\n" +
			"bytes     = mem;\n" +
			"byteMem   = haxe.io.UInt8Array.fromBytes(mem);\n" +
			"shortMem  = haxe.io.UInt16Array.fromBytes(mem);\n" +
			"intMem    = haxe.io.Int32Array.fromBytes(mem);\n" +
			"floatMem  = haxe.io.Float32Array.fromBytes(mem);\n" +
			"doubleMem = haxe.io.Float64Array.fromBytes(mem);\n"
		),
	})
	@JTranscSync
	static public void select(FastMemory mem) {
		Mem.mem = mem;
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.getByte(p0 << 0);"),
		@HaxeMethodBody("return byteMem.get(p0);"),
	})
	@JTranscSync
	static public int li8(int address) {
		return mem.getAlignedInt8(address);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.getUI16(p0 << 1);"),
		@HaxeMethodBody("return shortMem.get(p0);"),
	})
	@JTranscSync
	static public int li16(int address2) {
		return mem.getAlignedInt16(address2);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.getI32(p0 << 2);"),
		@HaxeMethodBody("return intMem.get(p0);"),
	})
	@JTranscSync
	static public int li32(int address4) {
		return mem.getAlignedInt32(address4);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.getFloat(p0 << 2);"),
		@HaxeMethodBody("return floatMem.get(p0);"),
	})
	@JTranscSync
	static public float lf32(int address4) {
		return mem.getAlignedFloat32(address4);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "return flash.Memory.getDouble(p0 << 3);"),
		@HaxeMethodBody("return floatMem.get(p0);"),
	})
	@JTranscSync
	static public double lf64(int address8) {
		return mem.getAlignedFloat64(address8);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "flash.Memory.setByte(p0 << 0, p1);"),
		@HaxeMethodBody("byteMem.set(p0, p1);"),
	})
	@JTranscSync
	static public void si8(int address, int value) {
		mem.setAlignedInt8(address, value);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "flash.Memory.setI16(p0 << 1, p1);"),
		@HaxeMethodBody("shortMem.set(p0, p1);"),
	})
	@JTranscSync
	static public void si16(int address2, int value) {
		mem.setAlignedInt16(address2, value);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "flash.Memory.setI32(p0 << 2, p1);"),
		@HaxeMethodBody("intMem.set(p0, p1);"),
	})
	@JTranscSync
	static public void si32(int address4, int value) {
		mem.setAlignedInt32(address4, value);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "flash.Memory.setFloat(p0 << 2, p1);"),
		@HaxeMethodBody("floatMem.set(p0, p1);"),
	})
	@JTranscSync
	static public void sf32(int address4, float value) {
		mem.setAlignedFloat32(address4, value);
	}

	@JTranscInline
	@HaxeMethodBodyList({
		@HaxeMethodBody(target = "flash", value = "flash.Memory.setDouble(p0 << 3, p1);"),
		@HaxeMethodBody("floatMem.set(p0, p1);"),
	})
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
