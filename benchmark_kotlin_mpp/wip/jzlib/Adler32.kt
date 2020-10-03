/* -*-mode:java; c-basic-offset:2; -*- */ /*
Copyright (c) 2000-2011 ymnk, JCraft,Inc. All rights reserved.

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

class Adler32 : Checksum {
	private var s1 = 1
	private var s2 = 0
	override fun reset(init: Int) {
		s1 = init shr 0 and 0xffff
		s2 = init shr 16 and 0xffff
	}

	override fun reset() {
		s1 = 1
		s2 = 0
	}

	override val value: Int
		get() = s2 shl 16 or s1

	override fun update(buf: ByteArray?, index: Int, len: Int) {
		var index = index
		var len = len
		if (len == 1) {
			s1 += buf!![index++].toInt() and 0xff
			s2 += s1
			s1 %= BASE
			s2 %= BASE
			return
		}
		var len1 = len / NMAX
		val len2 = len % NMAX
		while (len1-- > 0) {
			var k = NMAX
			len -= k
			while (k-- > 0) {
				s1 += buf!![index++].toInt() and 0xff
				s2 += s1
			}
			s1 %= BASE
			s2 %= BASE
		}
		var k = len2
		len -= k
		while (k-- > 0) {
			s1 += buf!![index++].toInt() and 0xff
			s2 += s1
		}
		s1 %= BASE
		s2 %= BASE
	}

	override fun copy(): Adler32? {
		val foo = Adler32()
		foo.s1 = s1
		foo.s2 = s2
		return foo
	}

	companion object {
		// largest prime smaller than 65536
		private const val BASE = 65521

		// NMAX is the largest n such that 255n(n+1)/2 + (n+1)(BASE-1) <= 2^32-1
		private const val NMAX = 5552

		// The following logic has come from zlib.1.2.
		fun combine(adler1: Long, adler2: Long, len2: Long): Long {
			val BASEL = BASE.toLong()
			var sum1: Long
			var sum2: Long
			val rem: Long // unsigned int
			rem = len2 % BASEL
			sum1 = adler1 and 0xffffL
			sum2 = rem * sum1
			sum2 %= BASEL // MOD(sum2);
			sum1 += (adler2 and 0xffffL) + BASEL - 1
			sum2 += (adler1 shr 16 and 0xffffL) + (adler2 shr 16 and 0xffffL) + BASEL - rem
			if (sum1 >= BASEL) sum1 -= BASEL
			if (sum1 >= BASEL) sum1 -= BASEL
			if (sum2 >= BASEL shl 1) sum2 -= BASEL shl 1
			if (sum2 >= BASEL) sum2 -= BASEL
			return sum1 or (sum2 shl 16)
		} /*
  private java.util.zip.Adler32 adler=new java.util.zip.Adler32();
  public void update(byte[] buf, int index, int len){
    if(buf==null) {adler.reset();}
    else{adler.update(buf, index, len);}
  }
  public void reset(){
    adler.reset();
  }
  public void reset(long init){
    if(init==1L){
      adler.reset();
    }
    else{
      System.err.println("unsupported operation");
    }
  }
  public long getValue(){
    return adler.getValue();
  }
*/
	}
}