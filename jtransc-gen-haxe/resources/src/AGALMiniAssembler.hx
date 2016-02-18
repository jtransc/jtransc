/*
 * Copyright (c) 2011, Adobe Systems Incorporated
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * * Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution.
 * 
 * * Neither the name of Adobe Systems Incorporated nor the names of its 
 *   contributors may be used to endorse or promote products derived from 
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import flash.utils.ByteArray;
import flash.display3D.Context3DProgramType;
import flash.Lib;

import flash.utils.Endian;

// Original haxe port : https://github.com/KTXSoftware/haxe-graphicscorelib
// Cleaned & updated by Thomas Hourdel
class AGALMiniAssembler
{
	static var trimReg:EReg = ~/^\s+|\s+$/g;
	static var initialized:Bool = false;
	
	public var agalcode(default, null):ByteArray;
	public var error(default, null):String;

	var debugEnabled:Bool;
	
	public function new(debugging:Bool = false)
	{
		agalcode = null;
		error = "";
		debugEnabled = debugging;
		
		if (!initialized)
			init();
	}

   static public function matchAll(r:EReg, str:String) {
        var matches = [];
        var offset = 0;
        while (r.match(str.substr(offset))) {
            //trace(str);
            var roffset = r.matchedPos();

            var pos = r.matchedPos().pos + offset;
            var len = r.matchedPos().len;

            matches.push(str.substr(pos, len));

            offset += roffset.pos + roffset.len;
        }

        return matches;
    }

	public function assemble(mode:Context3DProgramType, source:String, verbose:Bool = false, version:Int = 1, ignorelimits:Bool = false):ByteArray
	{
		var start:Int = Lib.getTimer();
		
		agalcode = new ByteArray();
		error = "";
		
		var isFrag:Bool = false;
		
		if (mode == Context3DProgramType.FRAGMENT)
			isFrag = true;
		
		agalcode.endian = Endian.LITTLE_ENDIAN;
		agalcode.writeByte(0xa0);            // tag version
		agalcode.writeUnsignedInt(0x1);      // AGAL version, big endian, bit pattern will be 0x01000000
		agalcode.writeByte(0xa1);            // tag program id
		agalcode.writeByte(isFrag ? 1 : 0);  // vertex or fragment
		
		initregmap(version, ignorelimits);
		
		var reg = ~/[\n\r]+/g;
		var lines = reg.replace(source, "\n").split("\n");
		var nest = 0;
		var nops = 0;
		var lng = lines.length;
		
		var i = 0;
		while (i < lng && error == "")
		{
			var line = new String(lines[i]);
			line = trimReg.replace(line, "");
			
			// remove comments
			var startcomment = line.indexOf("//");
			
			if (startcomment != -1)
				line = line.substr(0, startcomment);
			
			// grab options
			reg = ~/<.*>/g;
			var optsi:Int = -1, options:String = new String(line);
			
			if (reg.match(options))
				optsi = reg.matchedPos().pos;
				
			var opts:Array<String> = [];
			
			if (optsi != -1)
			{
				options = line.substr(optsi);
				line = line.substr(0, optsi);
				for (part in matchAll(~/\w+/, options)) {
					opts.push(part);
				}
			}
			
			// find opcode
			reg = ~/^\w{3}/ig;
			reg.match(line);
			var opCode = reg.matched(0);
			var opFound = OPMAP.get(opCode);
			
			// if debug is enabled, output the opcodes
			if (debugEnabled)
				Lib.trace(opFound);
			
			if (opFound == null)
			{
				if (line.length >= 3)
					Lib.trace("warning: bad line " + i + ": " + lines[i]);
					
				i++;
				continue;
			}
			
			line = line.substr(line.indexOf(opFound.name) + opFound.name.length);
			
			// nesting check
			if ((opFound.flags & OP_DEC_NEST) != 0)
			{
				nest--;
				
				if (nest < 0)
				{
					error = "error: conditional closes without open.";
					break;
				}
			}
			
			if ((opFound.flags & OP_INC_NEST) != 0)
			{
				nest++;
				
				if (nest > MAX_NESTING)
				{
					error = "error: nesting to deep, maximum allowed is " + MAX_NESTING + ".";
					break;
				}
			}
					
			if ((opFound.flags & OP_VERT_ONLY) != 0 && isFrag)
			{
				error = "error: opcode is only allowed in vertex programs.";
				break;
			}
			
			if ((opFound.flags & OP_FRAG_ONLY) != 0 && !isFrag)
			{
				error = "error: opcode is only allowed in fragment programs.";
				break;
			}
			
			if (verbose)
				Lib.trace("emit opcode=" + opFound);
			
			agalcode.writeUnsignedInt(opFound.emitCode);
			nops++;
			
			if (nops > MAX_OPCODES)
			{
				error = "error: too many opcodes. maximum is " + MAX_OPCODES + ".";
				break;
			}
			
			// get operands, use regexp
			reg = ~/vc\[([vof][actps]?)(\d*)?(\.[xyzw](\+\d{1,3})?)?\](\.[xyzw]{1,4})?|([vof][actps]?)(\d*)?(\.[xyzw]{1,4})?/gi;
			var subline = line;
			var regs = new Array<String>();
			
			while (reg.match(subline))
			{
				regs.push(reg.matched(0));
				subline = subline.substr(reg.matchedPos().pos + reg.matchedPos().len);
				
				if (subline.charAt(0) == ",")
					subline = subline.substr(1);
					
				reg = ~/vc\[([vof][actps]?)(\d*)?(\.[xyzw](\+\d{1,3})?)?\](\.[xyzw]{1,4})?|([vof][actps]?)(\d*)?(\.[xyzw]{1,4})?/gi;
			}
			
			if (regs.length != Std.int(opFound.numRegister))
			{
				error = "error: wrong number of operands. found " + regs.length + " but expected " + opFound.numRegister + ".";
				break;
			}
			
			var badreg = false;
			var pad = 64 + 64 + 32;
			var regLength = regs.length;
			
			var j = 0;
			while (j < Std.int(regLength))
			{
				var isRelative = false;
				reg = ~/\[.*\]/ig;
				var relreg = "";
				
				if (reg.match(regs[j]))
				{
					relreg = reg.matched(0);
					var relpos = source.indexOf(relreg);
					regs[j] = regs[j].substr(0, relpos) + "0" + regs[j].substr(relpos + relreg.length);
					
					if (verbose)
						Lib.trace("IS REL");
						
					isRelative = true;
				}
				
				reg = ~/^\b[A-Za-z]{1,2}/ig;
				reg.match(regs[j]);
				var res = reg.matched(0);
				var regFound = REGMAP.get(res);
				
				// if debug is enabled, output the registers
				if (debugEnabled)
					Lib.trace(regFound);
				
				if (regFound == null)
				{
					error = "error: could not parse operand " + j + " (" + regs[j] + ").";
					badreg = true;
					break;
				}
				
				if (isFrag)
				{
					if ((regFound.flags & REG_FRAG) == 0)
					{
						error = "error: register operand "+j+" ("+regs[j]+") only allowed in vertex programs.";
						badreg = true;
						break;
					}
					
					if (isRelative)
					{
						error = "error: register operand " + j + " (" + regs[j] + ") relative adressing not allowed in fragment programs.";
						badreg = true;
						break;
					}
				}
				else
				{
					if ((regFound.flags & REG_VERT) == 0)
					{
						error = "error: register operand " + j + " (" + regs[j] + ") only allowed in fragment programs.";
						badreg = true;
						break;
					}
				}
				
				regs[j] = regs[j].substr(regs[j].indexOf(regFound.name) + regFound.name.length);
				reg = ~/\d+/;
				var idxmatched = false;
				
				if (isRelative)
					idxmatched = reg.match(relreg);
					
				else idxmatched = reg.match(regs[j]);
				var regidx = 0;
				
				if (idxmatched)
					regidx = Std.parseInt(reg.matched(0));
				
				if (regFound.range < regidx)
				{
					error = "error: register operand " + j + " (" + regs[j] + ") index exceeds limit of " + (regFound.range + 1) + ".";
					badreg = true;
					break;
				}
				
				var regmask = 0;
				var isDest = (j == 0 && (opFound.flags & OP_NO_DEST) == 0);
				var isSampler = (j == 2 && (opFound.flags & OP_SPECIAL_TEX) != 0);
				var reltype = 0;
				var relsel = 0;
				var reloffset = 0;
				
				if (isDest && isRelative)
				{
					error = "error: relative can not be destination";  
					badreg = true; 
					break;                
				}
				
				reg = ~/(\.[xyzw]{1,4})/;
				
				if (reg.match(regs[j]))
				{
					var maskmatch = reg.matched(0);
					regmask = 0;
					var cv = 0;
					var maskLength = maskmatch.length;
					
					var k = 1;
					while (k < Std.int(maskLength))
					{
						cv = maskmatch.charCodeAt(k) - "x".charCodeAt(0);
						
						if (cv > 2)
							cv = 3;
							
						if (isDest)
							regmask |= 1 << cv;
							
						else regmask |= cv << ((k - 1) << 1);
						k++;
					}
					
					if (!isDest)
					{
						while (k <= 4)
						{
							regmask |= cv << ((k - 1) << 1); // repeat last
							k++;
						}
					}
				}
				else regmask = isDest ? 0xf : 0xe4; // id swizzle or mask
				
				if (isRelative)
				{
					reg = ~/[A-Za-z]{1,2}/ig;
					reg.match(relreg);
					var relname = reg.matched(0);
					var regFoundRel = REGMAP.get(relname);
					
					if (regFoundRel == null)
					{
						error = "error: bad index register";
						badreg = true;
						break;
					}
					
					reltype = regFoundRel.emitCode;
					reg = ~/(\.[xyzw]{1,1})/;
					
					if (!reg.match(relreg))
					{
						error = "error: bad index register select";
						badreg = true;
						break;
					}
					
					var selmatch = reg.matched(0);
					relsel = selmatch.charCodeAt(1) - "x".charCodeAt(0);
					
					if (relsel > 2)
						relsel = 3;
						
					reg = ~/\+\d{1,3}/ig;
					
					if (reg.match(relreg))
						reloffset = Std.parseInt(reg.matched(0));
						
					if (reloffset < 0 || reloffset > 255)
					{
						error = "error: index offset " + reloffset + " out of bounds. [0..255]";
						badreg = true;
						break;
					}
					
					if (verbose)
						Lib.trace("RELATIVE: type=" + reltype + "==" + relname + " sel=" + relsel + "==" + selmatch + " idx=" + regidx + " offset=" + reloffset);
				}
				
				if (verbose)
					Lib.trace("  emit argcode=" + regFound + "[" + regidx + "][" + regmask + "]");
					
				if (isDest)
				{
					agalcode.writeShort(regidx);
					agalcode.writeByte(regmask);
					agalcode.writeByte(regFound.emitCode);
					pad -= 32;
				}
				else
				{
					if (isSampler)
					{
						if (verbose)
							Lib.trace("  emit sampler");
							
						var samplerbits = 5; // type 5
						var optsLength = opts.length;
						var bias = 0.;
						
						var k = 0;
						while (k < Std.int(optsLength))
						{
							if (verbose)
								Lib.trace("    opt: " + opts[k]);
								
							var optfound:Sampler = SAMPLEMAP.get(opts[k]);
							
							if (optfound == null)
							{
								bias = Std.parseFloat(opts[k]);
								
								if (verbose)
									Lib.trace("    bias: " + bias);
							}
							else
							{
								if (optfound.flag != SAMPLER_SPECIAL_SHIFT)
									samplerbits &= ~(0xf << optfound.flag);
								
								samplerbits |= optfound.mask << optfound.flag;
							}
							
							k++;
						}
						
						agalcode.writeShort(regidx);
						agalcode.writeByte(Std.int(bias * 8.0));
						agalcode.writeByte(0);
						agalcode.writeUnsignedInt(samplerbits);
						
						if (verbose)
							Lib.trace("    bits: " + ( samplerbits - 5 ));
							
						pad -= 64;
					}
					else
					{
						if (j == 0)
						{
							agalcode.writeUnsignedInt(0);
							pad -= 32;
						}
						
						agalcode.writeShort(regidx);
						agalcode.writeByte(reloffset);
						agalcode.writeByte(regmask);
						agalcode.writeByte(regFound.emitCode);
						agalcode.writeByte(reltype);
						agalcode.writeShort(isRelative ? ( relsel | ( 1 << 15 ) ) : 0);
						
						pad -= 64;
					}
				}
				
				j++;
			}
			
			// pad unused regs
			j = 0;
			while (j < Std.int(pad))
			{
				agalcode.writeByte(0);
				j += 8;
			}
			
			if (badreg)
				break;
				
			i++;
		}
		
		if (error != "")
		{
			error += "\n  at line " + i + " " + lines[i];
			agalcode.length = 0;
			Lib.trace(error);
		}
		
		// trace the bytecode bytes if debugging is enabled
		if (debugEnabled)
		{
			var dbgLine = "generated bytecode:";
			var agalLength = Std.int(agalcode.length);
			var index = 0;
			
			while (index < agalLength)
			{
				if (( index % 16) == 0)
					dbgLine += "\n";
					
				if ((index % 4) == 0)
					dbgLine += " ";
				
				var byteStr = Std.string(agalcode[index]);// .toString( 16 );
				
				if (byteStr.length < 2)
					byteStr = "0" + byteStr;
				
				dbgLine += byteStr;
				index++;
			}
			
			Lib.trace(dbgLine);
		}
		
		if (verbose)
			Lib.trace("AGALMiniAssembler.assemble time: " + ((Lib.getTimer() - start) / 1000) + "s");
		
		return agalcode;
	}
	
	function initregmap(version:Int, ignorelimits:Bool)
	{
		// version changes limits				
		REGMAP.set(VA, new Register(VA,	"vertex attribute",		0x0, ignorelimits?1024:7,						REG_VERT | REG_READ));
		REGMAP.set(VC, new Register(VC,	"vertex constant",		0x1, ignorelimits?1024:(version==1?127:250),	REG_VERT | REG_READ));
		REGMAP.set(VT, new Register(VT,	"vertex temporary",		0x2, ignorelimits?1024:(version==1?7:27),		REG_VERT | REG_WRITE | REG_READ));
		REGMAP.set(VO, new Register(VO,	"vertex output",		0x3, ignorelimits?1024:0,						REG_VERT | REG_WRITE));
		REGMAP.set(VI, new Register(VI,	"varying",				0x4, ignorelimits?1024:(version==1?7:11),		REG_VERT | REG_FRAG | REG_READ | REG_WRITE));
		REGMAP.set(FC, new Register(FC,	"fragment constant",	0x1, ignorelimits?1024:(version==1?27:63),		REG_FRAG | REG_READ));
		REGMAP.set(FT, new Register(FT,	"fragment temporary",	0x2, ignorelimits?1024:(version==1?7:27),		REG_FRAG | REG_WRITE | REG_READ));
		REGMAP.set(FS, new Register(FS,	"texture sampler",		0x5, ignorelimits?1024:7,						REG_FRAG | REG_READ));
		REGMAP.set(FO, new Register(FO,	"fragment output",		0x3, ignorelimits?1024:(version==1?0:3),		REG_FRAG | REG_WRITE));
		REGMAP.set(FD, new Register(FD,	"fragment depth output",0x6, ignorelimits?1024:(version==1?-1:0),		REG_FRAG | REG_WRITE));
		
		// aliases
		REGMAP.set("op", REGMAP.get(VO));
		REGMAP.set("i",  REGMAP.get(VI));
		REGMAP.set("v",  REGMAP.get(VI));
		REGMAP.set("oc", REGMAP.get(FO));
		REGMAP.set("od", REGMAP.get(FD));
		REGMAP.set("fi", REGMAP.get(VI)); 
	}

	static function init()
	{
		initialized = true;

		// Fill the dictionaries with opcodes and registers
		OPMAP.set(MOV, new OpCode(MOV, 2, 0x00, 0));
		OPMAP.set(ADD, new OpCode(ADD, 3, 0x01, 0));
		OPMAP.set(SUB, new OpCode(SUB, 3, 0x02, 0));
		OPMAP.set(MUL, new OpCode(MUL, 3, 0x03, 0));
		OPMAP.set(DIV, new OpCode(DIV, 3, 0x04, 0));
		OPMAP.set(RCP, new OpCode(RCP, 2, 0x05, 0));
		OPMAP.set(MIN, new OpCode(MIN, 3, 0x06, 0));
		OPMAP.set(MAX, new OpCode(MAX, 3, 0x07, 0));
		OPMAP.set(FRC, new OpCode(FRC, 2, 0x08, 0));
		OPMAP.set(SQT, new OpCode(SQT, 2, 0x09, 0));
		OPMAP.set(RSQ, new OpCode(RSQ, 2, 0x0a, 0));
		OPMAP.set(POW, new OpCode(POW, 3, 0x0b, 0));
		OPMAP.set(LOG, new OpCode(LOG, 2, 0x0c, 0));
		OPMAP.set(EXP, new OpCode(EXP, 2, 0x0d, 0));
		OPMAP.set(NRM, new OpCode(NRM, 2, 0x0e, 0));
		OPMAP.set(SIN, new OpCode(SIN, 2, 0x0f, 0));
		OPMAP.set(COS, new OpCode(COS, 2, 0x10, 0));
		OPMAP.set(CRS, new OpCode(CRS, 3, 0x11, 0));
		OPMAP.set(DP3, new OpCode(DP3, 3, 0x12, 0));
		OPMAP.set(DP4, new OpCode(DP4, 3, 0x13, 0));
		OPMAP.set(ABS, new OpCode(ABS, 2, 0x14, 0));
		OPMAP.set(NEG, new OpCode(NEG, 2, 0x15, 0));
		OPMAP.set(SAT, new OpCode(SAT, 2, 0x16, 0));
		OPMAP.set(M33, new OpCode(M33, 3, 0x17, OP_SPECIAL_MATRIX));
		OPMAP.set(M44, new OpCode(M44, 3, 0x18, OP_SPECIAL_MATRIX));
		OPMAP.set(M34, new OpCode(M34, 3, 0x19, OP_SPECIAL_MATRIX));
		OPMAP.set(IFZ, new OpCode(IFZ, 1, 0x1a, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(INZ, new OpCode(INZ, 1, 0x1b, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(IFE, new OpCode(IFE, 2, 0x1c, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(INE, new OpCode(INE, 2, 0x1d, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(IFG, new OpCode(IFG, 2, 0x1e, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(IFL, new OpCode(IFL, 2, 0x1f, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(IEG, new OpCode(IEG, 2, 0x20, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(IEL, new OpCode(IEL, 2, 0x21, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(ELS, new OpCode(ELS, 0, 0x22, OP_NO_DEST | OP_INC_NEST | OP_DEC_NEST));
		OPMAP.set(EIF, new OpCode(EIF, 0, 0x23, OP_NO_DEST | OP_DEC_NEST));
		OPMAP.set(REP, new OpCode(REP, 1, 0x24, OP_NO_DEST | OP_INC_NEST | OP_SCALAR));
		OPMAP.set(ERP, new OpCode(ERP, 0, 0x25, OP_NO_DEST | OP_DEC_NEST));
		OPMAP.set(BRK, new OpCode(BRK, 0, 0x26, OP_NO_DEST));
		OPMAP.set(KIL, new OpCode(KIL, 1, 0x27, OP_NO_DEST | OP_FRAG_ONLY));
		OPMAP.set(TEX, new OpCode(TEX, 3, 0x28, OP_FRAG_ONLY | OP_SPECIAL_TEX));
		OPMAP.set(SGE, new OpCode(SGE, 3, 0x29, 0));
		OPMAP.set(SLT, new OpCode(SLT, 3, 0x2a, 0));
		OPMAP.set(SGN, new OpCode(SGN, 2, 0x2b, 0));

		SAMPLEMAP.set(D2,         new Sampler(D2,         SAMPLER_DIM_SHIFT,     0));
		SAMPLEMAP.set(D3,         new Sampler(D3,         SAMPLER_DIM_SHIFT,     2));
		SAMPLEMAP.set(CUBE,       new Sampler(CUBE,       SAMPLER_DIM_SHIFT,     1));
		SAMPLEMAP.set(MIPNEAREST, new Sampler(MIPNEAREST, SAMPLER_MIPMAP_SHIFT,  1));
		SAMPLEMAP.set(MIPLINEAR,  new Sampler(MIPLINEAR,  SAMPLER_MIPMAP_SHIFT,  2));
		SAMPLEMAP.set(MIPNONE,    new Sampler(MIPNONE,    SAMPLER_MIPMAP_SHIFT,  0));
		SAMPLEMAP.set(NOMIP,      new Sampler(NOMIP,      SAMPLER_MIPMAP_SHIFT,  0));
		SAMPLEMAP.set(NEAREST,    new Sampler(NEAREST,    SAMPLER_FILTER_SHIFT,  0));
		SAMPLEMAP.set(LINEAR,     new Sampler(LINEAR,     SAMPLER_FILTER_SHIFT,  1));
		SAMPLEMAP.set(CENTROID,   new Sampler(CENTROID,   SAMPLER_SPECIAL_SHIFT, 1 << 0));
		SAMPLEMAP.set(SINGLE,     new Sampler(SINGLE,     SAMPLER_SPECIAL_SHIFT, 1 << 1));
		SAMPLEMAP.set(DEPTH,      new Sampler(DEPTH,      SAMPLER_SPECIAL_SHIFT, 1 << 2));
		SAMPLEMAP.set(REPEAT,     new Sampler(REPEAT,     SAMPLER_REPEAT_SHIFT,  1));
		SAMPLEMAP.set(WRAP,       new Sampler(WRAP,       SAMPLER_REPEAT_SHIFT,  1));
		SAMPLEMAP.set(CLAMP,      new Sampler(CLAMP,      SAMPLER_REPEAT_SHIFT,  0));
	}

	static var OPMAP                     = new Map<String, OpCode>();
	static var REGMAP                    = new Map<String, Register>();
	static var SAMPLEMAP                 = new Map<String, Sampler>();

	static var MAX_NESTING:Int           = 4;
	static var MAX_OPCODES:Int           = 256;

	// masks and shifts
	static var SAMPLER_DIM_SHIFT:Int     = 12;
	static var SAMPLER_SPECIAL_SHIFT:Int = 16;
	static var SAMPLER_REPEAT_SHIFT:Int  = 20;
	static var SAMPLER_MIPMAP_SHIFT:Int  = 24;
	static var SAMPLER_FILTER_SHIFT:Int  = 28;

	// regmap flags
	static var REG_WRITE:Int             = 0x1;
	static var REG_READ:Int              = 0x2;
	static var REG_FRAG:Int              = 0x20;
	static var REG_VERT:Int              = 0x40;

	// opmap flags
	static var OP_SCALAR:Int             = 0x1;
	static var OP_INC_NEST:Int           = 0x2;
	static var OP_DEC_NEST:Int           = 0x4;
	static var OP_SPECIAL_TEX:Int        = 0x8;
	static var OP_SPECIAL_MATRIX:Int     = 0x10;
	static var OP_FRAG_ONLY:Int          = 0x20;
	static var OP_VERT_ONLY:Int          = 0x40;
	static var OP_NO_DEST:Int            = 0x80;

	// opcodes
	static var MOV:String                = "mov";
	static var ADD:String                = "add";
	static var SUB:String                = "sub";
	static var MUL:String                = "mul";
	static var DIV:String                = "div";
	static var RCP:String                = "rcp";
	static var MIN:String                = "min";
	static var MAX:String                = "max";
	static var FRC:String                = "frc";
	static var SQT:String                = "sqt";
	static var RSQ:String                = "rsq";
	static var POW:String                = "pow";
	static var LOG:String                = "log";
	static var EXP:String                = "exp";
	static var NRM:String                = "nrm";
	static var SIN:String                = "sin";
	static var COS:String                = "cos";
	static var CRS:String                = "crs";
	static var DP3:String                = "dp3";
	static var DP4:String                = "dp4";
	static var ABS:String                = "abs";
	static var NEG:String                = "neg";
	static var SAT:String                = "sat";
	static var M33:String                = "m33";
	static var M44:String                = "m44";
	static var M34:String                = "m34";
	static var IFZ:String                = "ifz";
	static var INZ:String                = "inz";
	static var IFE:String                = "ife";
	static var INE:String                = "ine";
	static var IFG:String                = "ifg";
	static var IFL:String                = "ifl";
	static var IEG:String                = "ieg";
	static var IEL:String                = "iel";
	static var ELS:String                = "els";
	static var EIF:String                = "eif";
	static var REP:String                = "rep";
	static var ERP:String                = "erp";
	static var BRK:String                = "brk";
	static var KIL:String                = "kil";
	static var TEX:String                = "tex";
	static var SGE:String                = "sge";
	static var SLT:String                = "slt";
	static var SGN:String                = "sgn";

	// registers
	static var VA:String                 = "va";
	static var VC:String                 = "vc";
	static var VT:String                 = "vt";
	static var VO:String                 = "vo";
	static var VI:String                 = "vi";
	static var FC:String                 = "fc";
	static var FT:String                 = "ft";
	static var FS:String                 = "fs";
	static var FO:String                 = "fo";
	static var FD:String                 = "fd";
	// samplers
	static var D2:String                 = "2d";
	static var D3:String                 = "3d";
	static var CUBE:String               = "cube";
	static var MIPNEAREST:String         = "mipnearest";
	static var MIPLINEAR:String          = "miplinear";
	static var MIPNONE:String            = "mipnone";
	static var NOMIP:String              = "nomip";
	static var NEAREST:String            = "nearest";
	static var LINEAR:String             = "linear";
	static var CENTROID:String           = "centroid";
	static var SINGLE:String             = "single";
	static var DEPTH:String              = "depth";
	static var REPEAT:String             = "repeat";
	static var WRAP:String               = "wrap";
	static var CLAMP:String              = "clamp";
}

class OpCode
{
	public var emitCode(default, null):Int;
	public var flags(default, null):Int;
	public var name(default, null):String;
	public var numRegister(default, null):Int;

	public function new(name:String, numRegister:Int, emitCode:Int, flags:Int)
	{
		this.name = name;
		this.numRegister = numRegister;
		this.emitCode = emitCode;
		this.flags = flags;
	}

	public function toString():String
	{
		return "[OpCode name=\"" + name + "\", numRegister=" + numRegister + ", emitCode=" + emitCode + ", flags=" + flags + "]";
	}
}

class Register
{
	public var emitCode(default, null):Int;
	public var name(default, null):String;
	public var longName(default, null):String;
	public var flags(default, null):Int;
	public var range(default, null):Int;

	public function new(name:String, longName:String, emitCode:Int, range:Int, flags:Int)
	{
		this.name = name;
		this.longName = longName;
		this.emitCode = emitCode;
		this.range = range;
		this.flags = flags;
	}

	public function toString():String
	{
		return "[Register name=\"" + name + "\", longName=\"" + longName + "\", emitCode=" + emitCode + ", range=" + range + ", flags=" + flags + "]";
	}
}

class Sampler
{
	public var flag(default, null):Int;
	public var mask(default, null):Int;
	public var name(default, null):String;

	public function new(name:String, flag:Int, mask:Int)
	{
		this.name = name;
		this.flag = flag;
		this.mask = mask;
	}

	public function toString():String
	{
		return "[Sampler name=\"" + name + "\", flag=\"" + flag + "\", mask=" + mask + "]";
	}
}
