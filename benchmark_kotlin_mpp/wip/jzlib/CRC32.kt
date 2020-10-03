/* -*-mode:java; c-basic-offset:2; -*- */ /*
Copyright (c) 2011 ymnk, JCraft,Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright 
     notice, this list of conditions and the following disclaimer in 
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * This program is based on zlib-1.1.3, so all credit should go authors
 * Jean-loup Gailly(jloup@gzip.org) and Mark Adler(madler@alumni.caltech.edu)
 * and contributors of zlib.
 */
package com.jtransc.compression.jzlib

import arraycopy

class CRC32 : Checksum {
	/*
	 *  The following logic has come from RFC1952.
     */
	private var v = 0

	companion object {
		private var crc_table: IntArray? = null

		// The following logic has come from zlib.1.2.
		private const val GF2_DIM = 32
		fun combine(crc1: Long, crc2: Long, len2: Long): Long {
			var crc1 = crc1
			var len2 = len2
			var row: Long
			val even = LongArray(GF2_DIM)
			val odd = LongArray(GF2_DIM)

			// degenerate case (also disallow negative lengths)
			if (len2 <= 0) return crc1

			// put operator for one zero bit in odd
			odd[0] = 0xedb88320L // CRC-32 polynomial
			row = 1
			for (n in 1 until GF2_DIM) {
				odd[n] = row
				row = row shl 1
			}

			// put operator for two zero bits in even
			gf2_matrix_square(even, odd)

			// put operator for four zero bits in odd
			gf2_matrix_square(odd, even)

			// apply len2 zeros to crc1 (first square will put the operator for one
			// zero byte, eight zero bits, in even)
			do {
				// apply zeros operator for this bit of len2
				gf2_matrix_square(even, odd)
				if (len2 and 1 != 0L) crc1 = gf2_matrix_times(even, crc1)
				len2 = len2 shr 1

				// if no more bits set, then done
				if (len2 == 0L) break

				// another iteration of the loop with odd and even swapped
				gf2_matrix_square(odd, even)
				if (len2 and 1 != 0L) crc1 = gf2_matrix_times(odd, crc1)
				len2 = len2 shr 1

				// if no more bits set, then done
			} while (len2 != 0L)

			/* return combined crc */crc1 = crc1 xor crc2
			return crc1
		}

		private fun gf2_matrix_times(mat: LongArray, vec: Long): Long {
			var vec = vec
			var sum: Long = 0
			var index = 0
			while (vec != 0L) {
				if (vec and 1 != 0L) sum = sum xor mat[index]
				vec = vec shr 1
				index++
			}
			return sum
		}

		fun gf2_matrix_square(square: LongArray, mat: LongArray) {
			for (n in 0 until GF2_DIM) square[n] = gf2_matrix_times(mat, mat[n])
		}

		val cRC32Table: IntArray
			get() {
				val tmp = IntArray(crc_table!!.size)
				arraycopy(crc_table!!, 0, tmp, 0, tmp.size)
				return tmp
			}

		init {
			crc_table = IntArray(256)
			for (n in 0..255) {
				var c = n
				var k = 8
				while (--k >= 0) {
					if (c and 1 != 0) {
						c = -0x12477ce0 xor (c ushr 1)
					} else {
						c = c ushr 1
					}
				}
				crc_table!![n] = c
			}
		}
	}

	override fun update(buf: ByteArray?, index: Int, len: Int) {
		//int[] crc_table = CRC32.crc_table;
		var index = index
		var len = len
		var c = v.inv()
		while (--len >= 0) {
			c = crc_table!![c xor buf!![index++].toInt() and 0xff] xor (c ushr 8)
		}
		v = c.inv()
	}

	override fun reset() {
		v = 0
	}

	override fun reset(vv: Int) {
		v = vv
	}

	override val value: Int
		get() = v

	/*
	private java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();

    public void update(byte[] buf, int index, int len){
      if(buf==null) {crc32.reset();}
      else{crc32.update(buf, index, len);}
    }
    public void reset(){
      crc32.reset();
    }
    public void reset(long init){
      if(init==0L){
        crc32.reset();
      }
      else{
        System.err.println("unsupported operation");
      }
    }
    public long getValue(){
      return crc32.getValue();
    }
  */
	override fun copy(): CRC32? {
		val foo = CRC32()
		foo.v = v
		return foo
	}
}