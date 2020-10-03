/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */ /*
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

import com.jtransc.annotation.JTranscInvisible
import com.jtransc.compression.jzlib.JZlib.WrapperType

@JTranscInvisible
class Inflater : ZStream {
	constructor() : super() {
		init()
	}

	constructor(wrapperType: WrapperType) : this(DEF_WBITS, wrapperType) {}
	constructor(w: Int, wrapperType: WrapperType) : super() {
		val ret = init(w, wrapperType)
		if (ret != Z_OK) throw GZIPException("$ret: $msg")
	}

	constructor(nowrap: Boolean) : this(DEF_WBITS, nowrap) {}

	@JvmOverloads
	constructor(w: Int, nowrap: Boolean = false) : super() {
		val ret = init(w, nowrap)
		if (ret != Z_OK) throw GZIPException("$ret: $msg")
	}

	private var finished = false
	fun init(wrapperType: WrapperType): Int {
		return init(DEF_WBITS, wrapperType)
	}

	fun init(w: Int, wrapperType: WrapperType): Int {
		var w = w
		var nowrap = false
		if (wrapperType === JZlib.W_NONE) {
			nowrap = true
		} else if (wrapperType === JZlib.W_GZIP) {
			w += 16
		} else if (wrapperType === JZlib.W_ANY) {
			w = w or Inflate.INFLATE_ANY
		} else if (wrapperType === JZlib.W_ZLIB) {
		}
		return init(w, nowrap)
	}

	fun init(nowrap: Boolean): Int {
		return init(DEF_WBITS, nowrap)
	}

	@JvmOverloads
	fun init(w: Int = DEF_WBITS, nowrap: Boolean = false): Int {
		finished = false
		istate = Inflate(this)
		return istate.inflateInit(if (nowrap) -w else w)
	}

	override fun inflate(f: Int): Int {
		if (istate == null) return Z_STREAM_ERROR
		val ret: Int = istate.inflate(f)
		if (ret == Z_STREAM_END) finished = true
		return ret
	}

	override fun end(): Int {
		finished = true
		return if (istate == null) Z_STREAM_ERROR else istate.inflateEnd()
		//    istate = null;
	}

	fun sync(): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateSync()
	}

	fun syncPoint(): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateSyncPoint()
	}

	fun setDictionary(dictionary: ByteArray?, index: Int, dictLength: Int): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateSetDictionary(dictionary, index, dictLength)
	}

	override fun finished(): Boolean {
		return istate.mode === 12 /*DONE*/
	}

	companion object {
		private const val MAX_WBITS = 15 // 32K LZ77 window
		private const val DEF_WBITS = MAX_WBITS
		private const val Z_NO_FLUSH = 0
		private const val Z_PARTIAL_FLUSH = 1
		private const val Z_SYNC_FLUSH = 2
		private const val Z_FULL_FLUSH = 3
		private const val Z_FINISH = 4
		private const val MAX_MEM_LEVEL = 9
		private const val Z_OK = 0
		private const val Z_STREAM_END = 1
		private const val Z_NEED_DICT = 2
		private const val Z_ERRNO = -1
		private const val Z_STREAM_ERROR = -2
		private const val Z_DATA_ERROR = -3
		private const val Z_MEM_ERROR = -4
		private const val Z_BUF_ERROR = -5
		private const val Z_VERSION_ERROR = -6
	}
}