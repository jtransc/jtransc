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

import com.jtransc.annotation.JTranscInvisible
import java.io.FilterOutputStream
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException

@JTranscInvisible
open class DeflaterOutputStream(
	out: java.io.OutputStream?,
	deflater: Deflater?,
	size: Int,
	close_out: Boolean
) : FilterOutputStream(out) {
	protected val deflater: Deflater
	protected var buffer: ByteArray
	private var closed = false
	var syncFlush = false
	private val buf1 = ByteArray(1)
	protected var mydeflater = false
	private val close_out = true

	constructor(out: java.io.OutputStream?) : this(
		out,
		Deflater(JZlib.Z_DEFAULT_COMPRESSION),
		DEFAULT_BUFSIZE, true
	) {
		mydeflater = true
	}

	constructor(out: java.io.OutputStream?, def: Deflater?) : this(out, def, DEFAULT_BUFSIZE, true) {}
	constructor(
		out: java.io.OutputStream?,
		deflater: Deflater?,
		size: Int
	) : this(out, deflater, size, true) {
	}

	@Throws(IOException::class)
	override fun write(b: Int) {
		buf1[0] = (b and 0xff).toByte()
		write(buf1, 0, 1)
	}

	@Throws(IOException::class)
	override fun write(b: ByteArray, off: Int, len: Int) {
		if (deflater.finished()) {
			throw IOException("finished")
		} else if ((off < 0) or (len < 0) or (off + len > b.size)) {
			throw IndexOutOfBoundsException()
		} else if (len == 0) {
			return
		} else {
			val flush = if (syncFlush) JZlib.Z_SYNC_FLUSH else JZlib.Z_NO_FLUSH
			deflater.setInput(b, off, len, true)
			while (deflater.avail_in > 0) {
				val err = deflate(flush)
				if (err == JZlib.Z_STREAM_END) break
			}
		}
	}

	@Throws(IOException::class)
	fun finish() {
		while (!deflater.finished()) {
			deflate(JZlib.Z_FINISH)
		}
	}

	@Throws(IOException::class)
	override fun close() {
		if (!closed) {
			finish()
			if (mydeflater) {
				deflater.end()
			}
			if (close_out) out.close()
			closed = true
		}
	}

	@Throws(IOException::class)
	protected fun deflate(flush: Int): Int {
		deflater.setOutput(buffer, 0, buffer.size)
		val err: Int = deflater.deflate(flush)
		when (err) {
			JZlib.Z_OK, JZlib.Z_STREAM_END -> {
			}
			JZlib.Z_BUF_ERROR -> {
				if (deflater.avail_in <= 0 && flush != JZlib.Z_FINISH) {
					// flush() without any data
					break
				}
				throw IOException("failed to deflate")
			}
			else -> throw IOException("failed to deflate")
		}
		val len: Int = deflater.next_out_index
		if (len > 0) {
			out.write(buffer, 0, len)
		}
		return err
	}

	@Throws(IOException::class)
	override fun flush() {
		if (syncFlush && !deflater.finished()) {
			while (true) {
				val err = deflate(JZlib.Z_SYNC_FLUSH)
				if (deflater.next_out_index < buffer.size) break
				if (err == JZlib.Z_STREAM_END) break
			}
		}
		out.flush()
	}

	val totalIn: Long
		get() = deflater.getTotalIn()
	val totalOut: Long
		get() = deflater.getTotalOut()

	fun getDeflater(): Deflater {
		return deflater
	}

	companion object {
		protected const val DEFAULT_BUFSIZE = 512
	}

	init {
		if (out == null || deflater == null) {
			throw NullPointerException()
		} else if (size <= 0) {
			throw java.lang.IllegalArgumentException("buffer size must be greater than 0")
		}
		this.deflater = deflater
		buffer = ByteArray(size)
		this.close_out = close_out
	}
}