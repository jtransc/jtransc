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
package com.jtransc.compression.jzlib

import java.io.FilterInputStream
import java.io.IOException

/**
 * ZInputStream
 *
 * deprecated  use DeflaterOutputStream or InflaterInputStream
 */
//@Deprecated
class ZInputStream : FilterInputStream {
	var flushMode: Int = JZlib.Z_NO_FLUSH
	protected var compress: Boolean
	protected var `in`: java.io.InputStream? = null
	protected var deflater: Deflater? = null
	protected var iis: InflaterInputStream? = null

	@JvmOverloads
	constructor(`in`: java.io.InputStream?, nowrap: Boolean = false) : super(`in`) {
		iis = InflaterInputStream(`in`, nowrap)
		compress = false
	}

	constructor(`in`: java.io.InputStream?, level: Int) : super(`in`) {
		this.`in` = `in`
		deflater = Deflater()
		deflater.init(level)
		compress = true
	}

	private val buf1 = ByteArray(1)
	@Throws(IOException::class)
	override fun read(): Int {
		return if (read(buf1, 0, 1) == -1) -1 else buf1[0] and 0xFF
	}

	private val buf = ByteArray(512)
	@Throws(IOException::class)
	override fun read(b: ByteArray, off: Int, len: Int): Int {
		if (compress) {
			deflater.setOutput(b, off, len)
			while (true) {
				val datalen: Int = `in`.read(buf, 0, buf.size)
				if (datalen == -1) return -1
				deflater.setInput(buf, 0, datalen, true)
				val err: Int = deflater.deflate(flushMode)
				if (deflater.next_out_index > 0) return deflater.next_out_index
				if (err == JZlib.Z_STREAM_END) return 0
				if (err == JZlib.Z_STREAM_ERROR ||
					err == JZlib.Z_DATA_ERROR
				) {
					throw ZStreamException("deflating: " + deflater.msg)
				}
			}
		} else {
			return iis.read(b, off, len)
		}
	}

	@Throws(IOException::class)
	override fun skip(n: Long): Long {
		var len = 512
		if (n < len) len = n.toInt()
		val tmp = ByteArray(len)
		return read(tmp) as Long
	}

	val totalIn: Long
		get() = if (compress) deflater.total_in else iis.getTotalIn()
	val totalOut: Long
		get() = if (compress) deflater.total_out else iis.getTotalOut()

	@Throws(IOException::class)
	override fun close() {
		if (compress) deflater.end() else iis.close()
	}
}