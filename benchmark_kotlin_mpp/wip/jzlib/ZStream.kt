/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */ /*
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

import kotlin.jvm.JvmOverloads

/**
 * ZStream
 *
 * deprecated  Not for public use in the future.
 */
//@Deprecated
open class ZStream @JvmOverloads constructor(adler: Checksum = Adler32()) {
	var nextIn // next input byte
		: ByteArray?
	var nextInIndex = 0
	var availIn // number of bytes available at next_in
		= 0
	var totalIn // total nb of input bytes read so far
		: Long = 0
	var nextOut // next output byte should be put there
		: ByteArray?
	var nextOutIndex = 0
	var availOut // remaining free space at next_out
		= 0
	var totalOut // total nb of bytes output so far
		: Long = 0
	var message: String? = null
	var dstate: Deflate? = null
	var istate: Inflate? = null
	var data_type // best guess about the data type: ascii or binary
		= 0
	var adler: Checksum
	fun inflateInit(nowrap: Boolean): Int {
		return inflateInit(DEF_WBITS, nowrap)
	}

	fun inflateInit(wrapperType: JZlib.WrapperType?): Int {
		return inflateInit(DEF_WBITS, wrapperType)
	}

	fun inflateInit(w: Int, wrapperType: JZlib.WrapperType): Int {
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
		return inflateInit(w, nowrap)
	}

	@JvmOverloads
	fun inflateInit(w: Int = DEF_WBITS, nowrap: Boolean = false): Int {
		istate = Inflate(this)
		return istate.inflateInit(if (nowrap) -w else w)
	}

	fun inflate(f: Int): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflate(
			f
		)
	}

	fun inflateEnd(): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateEnd()
		//    istate = null;
	}

	fun inflateSync(): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateSync()
	}

	fun inflateSyncPoint(): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateSyncPoint()
	}

	fun inflateSetDictionary(dictionary: ByteArray?, dictLength: Int): Int {
		return if (istate == null) Z_STREAM_ERROR else istate.inflateSetDictionary(dictionary, 0, dictLength)
	}

	fun inflateFinished(): Boolean {
		return istate.mode === 12 /*DONE*/
	}

	fun deflateInit(level: Int, nowrap: Boolean): Int {
		return deflateInit(level, MAX_WBITS, nowrap)
	}

	fun deflateInit(level: Int, bits: Int, memlevel: Int, wrapperType: JZlib.WrapperType): Int {
		var bits = bits
		if (bits < 9 || bits > 15) {
			return Z_STREAM_ERROR
		}
		if (wrapperType === JZlib.W_NONE) {
			bits *= -1
		} else if (wrapperType === JZlib.W_GZIP) {
			bits += 16
		} else if (wrapperType === JZlib.W_ANY) {
			return Z_STREAM_ERROR
		} else if (wrapperType === JZlib.W_ZLIB) {
		}
		return this.deflateInit(level, bits, memlevel)
	}

	fun deflateInit(level: Int, bits: Int, memlevel: Int): Int {
		dstate = Deflate(this)
		return dstate.deflateInit(level, bits, memlevel)
	}

	@JvmOverloads
	fun deflateInit(level: Int, bits: Int = MAX_WBITS, nowrap: Boolean = false): Int {
		dstate = Deflate(this)
		return dstate.deflateInit(level, if (nowrap) -bits else bits)
	}

	fun deflate(flush: Int): Int {
		return if (dstate == null) {
			Z_STREAM_ERROR
		} else dstate.deflate(flush)
	}

	fun deflateEnd(): Int {
		if (dstate == null) return Z_STREAM_ERROR
		val ret: Int = dstate.deflateEnd()
		dstate = null
		return ret
	}

	fun deflateParams(level: Int, strategy: Int): Int {
		return if (dstate == null) Z_STREAM_ERROR else dstate.deflateParams(
			level,
			strategy
		)
	}

	fun deflateSetDictionary(dictionary: ByteArray?, dictIndex: Int, dictLength: Int): Int {
		return if (dstate == null) Z_STREAM_ERROR else dstate.deflateSetDictionary(
			dictionary,
			dictIndex,
			dictLength
		)
	}

	// Flush as much pending output as possible. All deflate() output goes
	// through this function so some applications may wish to modify it
	// to avoid allocating a large strm->next_out buffer and copying into it.
	// (See also read_buf()).
	fun flush_pending() {
		var len: Int = dstate.pending
		if (len > availOut) len = availOut
		if (len == 0) return
		if (dstate.pending_buf.length <= dstate.pending_out || nextOut!!.size <= nextOutIndex || dstate.pending_buf.length < dstate.pending_out + len || nextOut!!.size < nextOutIndex + len) {
			//System.out.println(dstate.pending_buf.length+", "+dstate.pending_out+
			//		 ", "+next_out.length+", "+next_out_index+", "+len);
			//System.out.println("avail_out="+avail_out);
		}
		java.lang.System.arraycopy(
			dstate.pending_buf, dstate.pending_out,
			nextOut, nextOutIndex, len
		)
		nextOutIndex += len
		dstate.pending_out += len
		totalOut += len.toLong()
		availOut -= len
		dstate.pending -= len
		if (dstate.pending === 0) {
			dstate.pending_out = 0
		}
	}

	// Read a new buffer from the current input stream, update the adler32
	// and total number of bytes read.  All deflate() input goes through
	// this function so some applications may wish to modify it to avoid
	// allocating a large strm->next_in buffer and copying from it.
	// (See also flush_pending()).
	fun read_buf(buf: ByteArray?, start: Int, size: Int): Int {
		var len = availIn
		if (len > size) len = size
		if (len == 0) return 0
		availIn -= len
		if (dstate.wrap !== 0) {
			adler.update(nextIn, nextInIndex, len)
		}
		java.lang.System.arraycopy(nextIn, nextInIndex, buf, start, len)
		nextInIndex += len
		totalIn += len.toLong()
		return len
	}

	fun getAdler(): Int {
		return adler.getValue()
	}

	fun free() {
		nextIn = null
		nextOut = null
		message = null
	}

	fun setOutput(buf: ByteArray) {
		setOutput(buf, 0, buf.size)
	}

	fun setOutput(buf: ByteArray?, off: Int, len: Int) {
		nextOut = buf
		nextOutIndex = off
		availOut = len
	}

	fun setInput(buf: ByteArray) {
		setInput(buf, 0, buf.size, false)
	}

	fun setInput(buf: ByteArray, append: Boolean) {
		setInput(buf, 0, buf.size, append)
	}

	fun setInput(buf: ByteArray?, off: Int, len: Int, append: Boolean) {
		if (len <= 0 && append && nextIn != null) return
		if (availIn > 0 && append) {
			val tmp = ByteArray(availIn + len)
			java.lang.System.arraycopy(nextIn, nextInIndex, tmp, 0, availIn)
			java.lang.System.arraycopy(buf, off, tmp, availIn, len)
			nextIn = tmp
			nextInIndex = 0
			availIn += len
		} else {
			nextIn = buf
			nextInIndex = off
			availIn = len
		}
	}

	fun end(): Int {
		return Z_OK
	}

	fun finished(): Boolean {
		return false
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

	init {
		this.adler = adler
	}
}