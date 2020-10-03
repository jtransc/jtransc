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

import com.jtransc.annotation.JTranscInvisible
import java.lang.CloneNotSupportedException

@JTranscInvisible
class Deflate internal constructor(  // pointer back to this zlib stream
	var strm: ZStream
) : Cloneable, Cloneable {
	internal class Config(// reduce lazy search above this match length
		var good_length: Int, // do not perform lazy search above this match length
		var max_lazy: Int,
		// quit search above this match length
		var nice_length: Int, var max_chain: Int, var func: Int
	)

	companion object {
		private const val MAX_MEM_LEVEL = 9
		private const val Z_DEFAULT_COMPRESSION = -1
		private const val MAX_WBITS = 15 // 32K LZ77 window
		private const val DEF_MEM_LEVEL = 8
		private const val STORED = 0
		private const val FAST = 1
		private const val SLOW = 2
		private val config_table: Array<Config?>
		private val z_errmsg = arrayOf(
			"need dictionary",  // Z_NEED_DICT       2
			"stream end",  // Z_STREAM_END      1
			"",  // Z_OK              0
			"file error",  // Z_ERRNO         (-1)
			"stream error",  // Z_STREAM_ERROR  (-2)
			"data error",  // Z_DATA_ERROR    (-3)
			"insufficient memory",  // Z_MEM_ERROR     (-4)
			"buffer error",  // Z_BUF_ERROR     (-5)
			"incompatible version",  // Z_VERSION_ERROR (-6)
			""
		)

		// block not completed, need more input or more output
		private const val NeedMore = 0

		// block flush performed
		private const val BlockDone = 1

		// finish started, need only more output at next deflate
		private const val FinishStarted = 2

		// finish done, accept no more input or output
		private const val FinishDone = 3

		// preset dictionary flag in zlib header
		private const val PRESET_DICT = 0x20
		private const val Z_FILTERED = 1
		private const val Z_HUFFMAN_ONLY = 2
		private const val Z_DEFAULT_STRATEGY = 0
		private const val Z_NO_FLUSH = 0
		private const val Z_PARTIAL_FLUSH = 1
		private const val Z_SYNC_FLUSH = 2
		private const val Z_FULL_FLUSH = 3
		private const val Z_FINISH = 4
		private const val Z_OK = 0
		private const val Z_STREAM_END = 1
		private const val Z_NEED_DICT = 2
		private const val Z_ERRNO = -1
		private const val Z_STREAM_ERROR = -2
		private const val Z_DATA_ERROR = -3
		private const val Z_MEM_ERROR = -4
		private const val Z_BUF_ERROR = -5
		private const val Z_VERSION_ERROR = -6
		private const val INIT_STATE = 42
		private const val BUSY_STATE = 113
		private const val FINISH_STATE = 666

		// The deflate compression method
		private const val Z_DEFLATED = 8
		private const val STORED_BLOCK = 0
		private const val STATIC_TREES = 1
		private const val DYN_TREES = 2

		// The three kinds of block type
		private const val Z_BINARY = 0
		private const val Z_ASCII = 1
		private const val Z_UNKNOWN = 2
		private const val Buf_size = 8 * 2

		// repeat previous bit length 3-6 times (2 bits of repeat count)
		private const val REP_3_6 = 16

		// repeat a zero length 3-10 times  (3 bits of repeat count)
		private const val REPZ_3_10 = 17

		// repeat a zero length 11-138 times  (7 bits of repeat count)
		private const val REPZ_11_138 = 18
		private const val MIN_MATCH = 3
		private const val MAX_MATCH = 258
		private const val MIN_LOOKAHEAD = MAX_MATCH + MIN_MATCH + 1
		private const val MAX_BITS = 15
		private const val D_CODES = 30
		private const val BL_CODES = 19
		private const val LENGTH_CODES = 29
		private const val LITERALS = 256
		private const val L_CODES = LITERALS + 1 + LENGTH_CODES
		private const val HEAP_SIZE = 2 * L_CODES + 1
		private const val END_BLOCK = 256
		fun smaller(tree: ShortArray, n: Int, m: Int, depth: ByteArray): Boolean {
			val tn2 = tree[n * 2]
			val tm2 = tree[m * 2]
			return tn2 < tm2 ||
				tn2 == tm2 && depth[n] <= depth[m]
		}

		fun deflateCopy(dest: ZStream, src: ZStream): Int {
			if (src.dstate == null) {
				return Z_STREAM_ERROR
			}
			if (src.next_in != null) {
				dest.next_in = ByteArray(src.next_in.length)
				java.lang.System.arraycopy(src.next_in, 0, dest.next_in, 0, src.next_in.length)
			}
			dest.next_in_index = src.next_in_index
			dest.avail_in = src.avail_in
			dest.total_in = src.total_in
			if (src.next_out != null) {
				dest.next_out = ByteArray(src.next_out.length)
				java.lang.System.arraycopy(src.next_out, 0, dest.next_out, 0, src.next_out.length)
			}
			dest.next_out_index = src.next_out_index
			dest.avail_out = src.avail_out
			dest.total_out = src.total_out
			dest.msg = src.msg
			dest.data_type = src.data_type
			dest.adler = src.adler.copy()!!
			try {
				dest.dstate = src.dstate!!.clone() as Deflate
				dest.dstate!!.strm = dest
			} catch (e: CloneNotSupportedException) {
				//
			}
			return Z_OK
		}

		init {
			config_table = arrayOfNulls(10)
			//                         good  lazy  nice  chain
			config_table[0] = Config(0, 0, 0, 0, STORED)
			config_table[1] = Config(4, 4, 8, 4, FAST)
			config_table[2] = Config(4, 5, 16, 8, FAST)
			config_table[3] = Config(4, 6, 32, 32, FAST)
			config_table[4] = Config(4, 4, 16, 16, SLOW)
			config_table[5] = Config(8, 16, 32, 32, SLOW)
			config_table[6] = Config(8, 16, 128, 128, SLOW)
			config_table[7] = Config(8, 32, 128, 256, SLOW)
			config_table[8] = Config(32, 128, 258, 1024, SLOW)
			config_table[9] = Config(32, 258, 258, 4096, SLOW)
		}
	}

	var status // as the name implies
		= 0
	var pending_buf // output still pending
		: ByteArray?
	var pending_buf_size // size of pending_buf
		= 0
	var pending_out // next pending byte to output to the stream
		= 0
	var pending // nb of bytes in the pending buffer
		= 0
	var wrap = 1
	var data_type // UNKNOWN, BINARY or ASCII
		: Byte = 0
	var method // STORED (for zip only) or DEFLATED
		: Byte = 0
	var last_flush // value of flush param for previous deflate call
		= 0
	var w_size // LZ77 window size (32K by default)
		= 0
	var w_bits // log2(w_size)  (8..16)
		= 0
	var w_mask // w_size - 1
		= 0
	var window: ByteArray?

	// Sliding window. Input bytes are read into the second half of the window,
	// and move to the first half later to keep a dictionary of at least wSize
	// bytes. With this organization, matches are limited to a distance of
	// wSize-MAX_MATCH bytes, but this ensures that IO is always
	// performed with a length multiple of the block size. Also, it limits
	// the window size to 64K, which is quite useful on MSDOS.
	// To do: use the user input buffer as sliding window.
	var window_size = 0

	// Actual size of window: 2*wSize, except when the user input buffer
	// is directly used as sliding window.
	var prev: ShortArray?

	// Link to older string with same hash index. To limit the size of this
	// array to 64K, this link is maintained only for the last 32K strings.
	// An index in this array is thus a window index modulo 32K.
	var head // Heads of the hash chains or NIL.
		: ShortArray?
	var ins_h // hash index of string to be inserted
		= 0
	var hash_size // number of elements in hash table
		= 0
	var hash_bits // log2(hash_size)
		= 0
	var hash_mask // hash_size-1
		= 0

	// Number of bits by which ins_h must be shifted at each input
	// step. It must be such that after MIN_MATCH steps, the oldest
	// byte no longer takes part in the hash key, that is:
	// hash_shift * MIN_MATCH >= hash_bits
	var hash_shift = 0

	// Window position at the beginning of the current output block. Gets
	// negative when the window is moved backwards.
	var block_start = 0
	var match_length // length of best match
		= 0
	var prev_match // previous match
		= 0
	var match_available // set if previous match exists
		= 0
	var strstart // start of string to insert
		= 0
	var match_start // start of matching string
		= 0
	var lookahead // number of valid bytes ahead in window
		= 0

	// Length of the best match at previous step. Matches not greater than this
	// are discarded. This is used in the lazy match evaluation.
	var prev_length = 0

	// To speed up deflation, hash chains are never searched beyond this
	// length.  A higher limit improves compression ratio but degrades the speed.
	var max_chain_length = 0

	// Attempt to find a better match only when the current match is strictly
	// smaller than this value. This mechanism is used only for compression
	// levels >= 4.
	var max_lazy_match = 0

	// Insert new strings in the hash table only if the match length is not
	// greater than this length. This saves time but degrades compression.
	// max_insert_length is used only for compression levels <= 3.
	var level // compression level (1..9)
		= 0
	var strategy // favor or force Huffman coding
		= 0

	// Use a faster search when the previous match is longer than this
	var good_match = 0

	// Stop searching when current match exceeds this
	var nice_match = 0
	var dyn_ltree // literal and length tree
		: ShortArray
	var dyn_dtree // distance tree
		: ShortArray
	var bl_tree // Huffman tree for bit lengths
		: ShortArray
	var l_desc = Tree() // desc for literal tree
	var d_desc = Tree() // desc for distance tree
	var bl_desc = Tree() // desc for bit length tree

	// number of codes at each bit length for an optimal tree
	var bl_count = ShortArray(MAX_BITS + 1)

	// working area to be used in Tree#gen_codes()
	var next_code = ShortArray(MAX_BITS + 1)

	// heap used to build the Huffman trees
	var heap = IntArray(2 * L_CODES + 1)
	var heap_len // number of elements in the heap
		= 0
	var heap_max // element of largest frequency
		= 0

	// The sons of heap[n] are heap[2*n] and heap[2*n+1]. heap[0] is not used.
	// The same heap array is used to build all trees.
	// Depth of each subtree used as tie breaker for trees of equal frequency
	var depth = ByteArray(2 * L_CODES + 1)
	var l_buf // index for literals or lengths */
		: ByteArray?

	// Size of match buffer for literals/lengths.  There are 4 reasons for
	// limiting lit_bufsize to 64K:
	//   - frequencies can be kept in 16 bit counters
	//   - if compression is not successful for the first block, all input
	//     data is still in the window so we can still emit a stored block even
	//     when input comes from standard input.  (This can also be done for
	//     all blocks if lit_bufsize is not greater than 32K.)
	//   - if compression is not successful for a file smaller than 64K, we can
	//     even emit a stored file instead of a stored block (saving 5 bytes).
	//     This is applicable only for zip (not gzip or zlib).
	//   - creating new Huffman trees less frequently may not provide fast
	//     adaptation to changes in the input data statistics. (Take for
	//     example a binary file with poorly compressible code followed by
	//     a highly compressible string table.) Smaller buffer sizes give
	//     fast adaptation but have of course the overhead of transmitting
	//     trees more frequently.
	//   - I can't count above 4
	var lit_bufsize = 0
	var last_lit // running index in l_buf
		= 0

	// Buffer for distances. To simplify the code, d_buf and l_buf have
	// the same number of elements. To use different lengths, an extra flag
	// array would be necessary.
	var d_buf // index of pendig_buf
		= 0
	var opt_len // bit length of current block with optimal trees
		= 0
	var static_len // bit length of current block with static trees
		= 0
	var matches // number of string matches in current block
		= 0
	var last_eob_len // bit length of EOB code for last block
		= 0

	// Output buffer. bits are inserted starting at the bottom (least
	// significant bits).
	var bi_buf: Short = 0

	// Number of valid bits in bi_buf.  All bits above the last valid bit
	// are always zero.
	var bi_valid = 0
	var gheader: GZIPHeader? = null
	fun lm_init() {
		window_size = 2 * w_size
		head!![hash_size - 1] = 0
		for (i in 0 until hash_size - 1) {
			head!![i] = 0
		}

		// Set the default configuration parameters:
		max_lazy_match = config_table[level]!!.max_lazy
		good_match = config_table[level]!!.good_length
		nice_match = config_table[level]!!.nice_length
		max_chain_length = config_table[level]!!.max_chain
		strstart = 0
		block_start = 0
		lookahead = 0
		prev_length = MIN_MATCH - 1
		match_length = prev_length
		match_available = 0
		ins_h = 0
	}

	// Initialize the tree data structures for a new zlib stream.
	fun tr_init() {
		l_desc.dyn_tree = dyn_ltree
		l_desc.stat_desc = StaticTree.static_l_desc
		d_desc.dyn_tree = dyn_dtree
		d_desc.stat_desc = StaticTree.static_d_desc
		bl_desc.dyn_tree = bl_tree
		bl_desc.stat_desc = StaticTree.static_bl_desc
		bi_buf = 0
		bi_valid = 0
		last_eob_len = 8 // enough lookahead for inflate

		// Initialize the first block of the first file:
		init_block()
	}

	fun init_block() {
		// Initialize the trees.
		for (i in 0 until L_CODES) dyn_ltree[i * 2] = 0
		for (i in 0 until D_CODES) dyn_dtree[i * 2] = 0
		for (i in 0 until BL_CODES) bl_tree[i * 2] = 0
		dyn_ltree[END_BLOCK * 2] = 1
		static_len = 0
		opt_len = static_len
		matches = 0
		last_lit = matches
	}

	// Restore the heap property by moving down the tree starting at node k,
	// exchanging a node with the smallest of its two sons if necessary, stopping
	// when the heap property is re-established (each father smaller than its
	// two sons).
	fun pqdownheap(
		tree: ShortArray,  // the tree to restore
		k: Int // node to move down
	) {
		var k = k
		val v = heap[k]
		var j = k shl 1 // left son of k
		while (j <= heap_len) {
			// Set j to the smallest of the two sons:
			if (j < heap_len &&
				smaller(tree, heap[j + 1], heap[j], depth)
			) {
				j++
			}
			// Exit if v is smaller than both sons
			if (smaller(tree, v, heap[j], depth)) break

			// Exchange v with the smallest son
			heap[k] = heap[j]
			k = j
			// And continue down the tree, setting j to the left son of k
			j = j shl 1
		}
		heap[k] = v
	}

	// Scan a literal or distance tree to determine the frequencies of the codes
	// in the bit length tree.
	fun scan_tree(
		tree: ShortArray,  // the tree to be scanned
		max_code: Int // and its largest code of non zero frequency
	) {
		var n: Int // iterates over all tree elements
		var prevlen = -1 // last emitted length
		var curlen: Int // length of current code
		var nextlen = tree[0 * 2 + 1].toInt() // length of next code
		var count = 0 // repeat count of the current code
		var max_count = 7 // max repeat count
		var min_count = 4 // min repeat count
		if (nextlen == 0) {
			max_count = 138
			min_count = 3
		}
		tree[(max_code + 1) * 2 + 1] = 0xffff.toShort() // guard
		n = 0
		while (n <= max_code) {
			curlen = nextlen
			nextlen = tree[(n + 1) * 2 + 1].toInt()
			if (++count < max_count && curlen == nextlen) {
				n++
				continue
			} else if (count < min_count) {
				(bl_tree[curlen * 2] += count).toShort()
			} else if (curlen != 0) {
				if (curlen != prevlen) bl_tree[curlen * 2]++
				bl_tree[REP_3_6 * 2]++
			} else if (count <= 10) {
				bl_tree[REPZ_3_10 * 2]++
			} else {
				bl_tree[REPZ_11_138 * 2]++
			}
			count = 0
			prevlen = curlen
			if (nextlen == 0) {
				max_count = 138
				min_count = 3
			} else if (curlen == nextlen) {
				max_count = 6
				min_count = 3
			} else {
				max_count = 7
				min_count = 4
			}
			n++
		}
	}

	// Construct the Huffman tree for the bit lengths and return the index in
	// bl_order of the last bit length code to send.
	fun build_bl_tree(): Int {
		var max_blindex: Int // index of last bit length code of non zero freq

		// Determine the bit length frequencies for literal and distance trees
		scan_tree(dyn_ltree, l_desc.max_code)
		scan_tree(dyn_dtree, d_desc.max_code)

		// Build the bit length tree:
		bl_desc.build_tree(this)
		// opt_len now includes the length of the tree representations, except
		// the lengths of the bit lengths codes and the 5+5+4 bits for the counts.

		// Determine the number of bit length codes to send. The pkzip format
		// requires that at least 4 bit length codes be sent. (appnote.txt says
		// 3 but the actual value used is 4.)
		max_blindex = BL_CODES - 1
		while (max_blindex >= 3) {
			if (bl_tree[Tree.bl_order[max_blindex] * 2 + 1] != 0) break
			max_blindex--
		}
		// Update opt_len to include the bit length tree and counts
		opt_len += 3 * (max_blindex + 1) + 5 + 5 + 4
		return max_blindex
	}

	// Send the header for a block using dynamic Huffman trees: the counts, the
	// lengths of the bit length codes, the literal tree and the distance tree.
	// IN assertion: lcodes >= 257, dcodes >= 1, blcodes >= 4.
	fun send_all_trees(lcodes: Int, dcodes: Int, blcodes: Int) {
		var rank: Int // index in bl_order
		send_bits(lcodes - 257, 5) // not +255 as stated in appnote.txt
		send_bits(dcodes - 1, 5)
		send_bits(blcodes - 4, 4) // not -3 as stated in appnote.txt
		rank = 0
		while (rank < blcodes) {
			send_bits(bl_tree[Tree.bl_order[rank] * 2 + 1].toInt(), 3)
			rank++
		}
		send_tree(dyn_ltree, lcodes - 1) // literal tree
		send_tree(dyn_dtree, dcodes - 1) // distance tree
	}

	// Send a literal or distance tree in compressed form, using the codes in
	// bl_tree.
	fun send_tree(
		tree: ShortArray,  // the tree to be sent
		max_code: Int // and its largest code of non zero frequency
	) {
		var n: Int // iterates over all tree elements
		var prevlen = -1 // last emitted length
		var curlen: Int // length of current code
		var nextlen = tree[0 * 2 + 1].toInt() // length of next code
		var count = 0 // repeat count of the current code
		var max_count = 7 // max repeat count
		var min_count = 4 // min repeat count
		if (nextlen == 0) {
			max_count = 138
			min_count = 3
		}
		n = 0
		while (n <= max_code) {
			curlen = nextlen
			nextlen = tree[(n + 1) * 2 + 1].toInt()
			if (++count < max_count && curlen == nextlen) {
				n++
				continue
			} else if (count < min_count) {
				do {
					send_code(curlen, bl_tree)
				} while (--count != 0)
			} else if (curlen != 0) {
				if (curlen != prevlen) {
					send_code(curlen, bl_tree)
					count--
				}
				send_code(REP_3_6, bl_tree)
				send_bits(count - 3, 2)
			} else if (count <= 10) {
				send_code(REPZ_3_10, bl_tree)
				send_bits(count - 3, 3)
			} else {
				send_code(REPZ_11_138, bl_tree)
				send_bits(count - 11, 7)
			}
			count = 0
			prevlen = curlen
			if (nextlen == 0) {
				max_count = 138
				min_count = 3
			} else if (curlen == nextlen) {
				max_count = 6
				min_count = 3
			} else {
				max_count = 7
				min_count = 4
			}
			n++
		}
	}

	// Output a byte on the stream.
	// IN assertion: there is enough room in pending_buf.
	fun put_byte(p: ByteArray?, start: Int, len: Int) {
		java.lang.System.arraycopy(p, start, pending_buf, pending, len)
		pending += len
	}

	fun put_byte(c: Byte) {
		pending_buf!![pending++] = c
	}

	fun put_short(w: Int) {
		put_byte(w /*&0xff*/.toByte())
		put_byte((w ushr 8).toByte())
	}

	fun putShortMSB(b: Int) {
		put_byte((b shr 8).toByte())
		put_byte(b /*&0xff*/.toByte())
	}

	fun send_code(c: Int, tree: ShortArray) {
		val c2 = c * 2
		send_bits(tree[c2] and 0xffff, tree[c2 + 1] and 0xffff)
	}

	fun send_bits(value: Int, length: Int) {
		if (bi_valid > Buf_size - length) {
			//      bi_buf |= (val << bi_valid);
			bi_buf = bi_buf or (value shl bi_valid and 0xffff)
			put_short(bi_buf.toInt())
			bi_buf = (value ushr Buf_size - bi_valid).toShort()
			bi_valid += length - Buf_size
		} else {
//      bi_buf |= (value) << bi_valid;
			bi_buf = bi_buf or (value shl bi_valid and 0xffff)
			bi_valid += length
		}
	}

	// Send one empty static block to give enough lookahead for inflate.
	// This takes 10 bits, of which 7 may remain in the bit buffer.
	// The current inflate code requires 9 bits of lookahead. If the
	// last two codes for the previous block (real code plus EOB) were coded
	// on 5 bits or less, inflate may have only 5+3 bits of lookahead to decode
	// the last real code. In this case we send two empty static blocks instead
	// of one. (There are no problems if the previous block is stored or fixed.)
	// To simplify the code, we assume the worst case of last real code encoded
	// on one bit only.
	fun _tr_align() {
		send_bits(STATIC_TREES shl 1, 3)
		send_code(END_BLOCK, StaticTree.static_ltree)
		bi_flush()

		// Of the 10 bits for the empty block, we have already sent
		// (10 - bi_valid) bits. The lookahead for the last real code (before
		// the EOB of the previous block) was thus at least one plus the length
		// of the EOB plus what we have just sent of the empty static block.
		if (1 + last_eob_len + 10 - bi_valid < 9) {
			send_bits(STATIC_TREES shl 1, 3)
			send_code(END_BLOCK, StaticTree.static_ltree)
			bi_flush()
		}
		last_eob_len = 7
	}

	// Save the match info and tally the frequency counts. Return true if
	// the current block must be flushed.
	fun _tr_tally(
		dist: Int,  // distance of matched string
		lc: Int // match length-MIN_MATCH or unmatched char (if dist==0)
	): Boolean {
		var dist = dist
		pending_buf!![d_buf + last_lit * 2] = (dist ushr 8).toByte()
		pending_buf!![d_buf + last_lit * 2 + 1] = dist.toByte()
		l_buf!![last_lit] = lc.toByte()
		last_lit++
		if (dist == 0) {
			// lc is the unmatched char
			dyn_ltree[lc * 2]++
		} else {
			matches++
			// Here, lc is the match length - MIN_MATCH
			dist-- // dist = match distance - 1
			dyn_ltree[(Tree._length_code[lc] + LITERALS + 1) * 2]++
			dyn_dtree[Tree.d_code(dist) * 2]++
		}
		if (last_lit and 0x1fff == 0 && level > 2) {
			// Compute an upper bound for the compressed length
			var out_length = last_lit * 8
			val in_length = strstart - block_start
			var dcode: Int
			dcode = 0
			while (dcode < D_CODES) {
				out_length += dyn_dtree[dcode * 2].toInt() *
					(5L + Tree.extra_dbits[dcode]).toInt()
				dcode++
			}
			out_length = out_length ushr 3
			if (matches < last_lit / 2 && out_length < in_length / 2) return true
		}
		return last_lit == lit_bufsize - 1
		// We avoid equality with lit_bufsize because of wraparound at 64K
		// on 16 bit machines and because stored blocks are restricted to
		// 64K-1 bytes.
	}

	// Send the block data compressed using the given Huffman trees
	fun compress_block(ltree: ShortArray, dtree: ShortArray) {
		var dist: Int // distance of matched string
		var lc: Int // match length or unmatched char (if dist == 0)
		var lx = 0 // running index in l_buf
		var code: Int // the code to send
		var extra: Int // number of extra bits to send
		if (last_lit != 0) {
			do {
				dist = pending_buf!![d_buf + lx * 2] shl 8 and 0xff00 or
					(pending_buf!![d_buf + lx * 2 + 1] and 0xff)
				lc = l_buf!![lx] and 0xff
				lx++
				if (dist == 0) {
					send_code(lc, ltree) // send a literal byte
				} else {
					// Here, lc is the match length - MIN_MATCH
					code = Tree._length_code[lc].toInt()
					send_code(code + LITERALS + 1, ltree) // send the length code
					extra = Tree.extra_lbits[code]
					if (extra != 0) {
						lc -= Tree.base_length[code]
						send_bits(lc, extra) // send the extra length bits
					}
					dist-- // dist is now the match distance - 1
					code = Tree.d_code(dist)
					send_code(code, dtree) // send the distance code
					extra = Tree.extra_dbits[code]
					if (extra != 0) {
						dist -= Tree.base_dist[code]
						send_bits(dist, extra) // send the extra distance bits
					}
				} // literal or match pair ?

				// Check that the overlay between pending_buf and d_buf+l_buf is ok:
			} while (lx < last_lit)
		}
		send_code(END_BLOCK, ltree)
		last_eob_len = ltree[END_BLOCK * 2 + 1].toInt()
	}

	// Set the data type to ASCII or BINARY, using a crude approximation:
	// binary if more than 20% of the bytes are <= 6 or >= 128, ascii otherwise.
	// IN assertion: the fields freq of dyn_ltree are set and the total of all
	// frequencies does not exceed 64K (to fit in an int on 16 bit machines).
	fun set_data_type() {
		var n = 0
		var ascii_freq = 0
		var bin_freq = 0
		while (n < 7) {
			bin_freq += dyn_ltree[n * 2]
			n++
		}
		while (n < 128) {
			ascii_freq += dyn_ltree[n * 2]
			n++
		}
		while (n < LITERALS) {
			bin_freq += dyn_ltree[n * 2]
			n++
		}
		data_type = (if (bin_freq > ascii_freq ushr 2) Z_BINARY else Z_ASCII).toByte()
	}

	// Flush the bit buffer, keeping at most 7 bits in it.
	fun bi_flush() {
		if (bi_valid == 16) {
			put_short(bi_buf.toInt())
			bi_buf = 0
			bi_valid = 0
		} else if (bi_valid >= 8) {
			put_byte(bi_buf.toByte())
			bi_buf = bi_buf ushr 8
			bi_valid -= 8
		}
	}

	// Flush the bit buffer and align the output on a byte boundary
	fun bi_windup() {
		if (bi_valid > 8) {
			put_short(bi_buf.toInt())
		} else if (bi_valid > 0) {
			put_byte(bi_buf.toByte())
		}
		bi_buf = 0
		bi_valid = 0
	}

	// Copy a stored block, storing first the length and its
	// one's complement if requested.
	fun copy_block(
		buf: Int,  // the input data
		len: Int,  // its length
		header: Boolean // true if block header must be written
	) {
		val index = 0
		bi_windup() // align on byte boundary
		last_eob_len = 8 // enough lookahead for inflate
		if (header) {
			put_short(len as Short.toInt())
			put_short(len.inv() as Short.toInt())
		}

		//  while(len--!=0) {
		//    put_byte(window[buf+index]);
		//    index++;
		//  }
		put_byte(window, buf, len)
	}

	fun flush_block_only(eof: Boolean) {
		_tr_flush_block(
			if (block_start >= 0) block_start else -1,
			strstart - block_start,
			eof
		)
		block_start = strstart
		strm.flush_pending()
	}

	// Copy without compression as much as possible from the input stream, return
	// the current block state.
	// This function does not insert new strings in the dictionary since
	// uncompressible data is probably not useful. This function is used
	// only for the level=0 compression option.
	// NOTE: this function should be optimized to avoid extra copying from
	// window to pending_buf.
	fun deflate_stored(flush: Int): Int {
		// Stored blocks are limited to 0xffff bytes, pending_buf is limited
		// to pending_buf_size, and each stored block has a 5 byte header:
		var max_block_size = 0xffff
		var max_start: Int
		if (max_block_size > pending_buf_size - 5) {
			max_block_size = pending_buf_size - 5
		}

		// Copy as much as possible from input to output:
		while (true) {
			// Fill the window as much as possible:
			if (lookahead <= 1) {
				fill_window()
				if (lookahead == 0 && flush == Z_NO_FLUSH) return NeedMore
				if (lookahead == 0) break // flush the current block
			}
			strstart += lookahead
			lookahead = 0

			// Emit a stored block if pending_buf will be full:
			max_start = block_start + max_block_size
			if (strstart == 0 || strstart >= max_start) {
				// strstart == 0 is possible when wraparound on 16-bit machine
				lookahead = (strstart - max_start)
				strstart = max_start
				flush_block_only(false)
				if (strm.avail_out === 0) return NeedMore
			}

			// Flush if we may have to slide, otherwise block_start may become
			// negative and the data will be gone:
			if (strstart - block_start >= w_size - MIN_LOOKAHEAD) {
				flush_block_only(false)
				if (strm.avail_out === 0) return NeedMore
			}
		}
		flush_block_only(flush == Z_FINISH)
		if (strm.avail_out === 0) return if (flush == Z_FINISH) FinishStarted else NeedMore
		return if (flush == Z_FINISH) FinishDone else BlockDone
	}

	// Send a stored block
	fun _tr_stored_block(
		buf: Int,  // input block
		stored_len: Int,  // length of input block
		eof: Boolean // true if this is the last block for a file
	) {
		send_bits((STORED_BLOCK shl 1) + if (eof) 1 else 0, 3) // send block type
		copy_block(buf, stored_len, true) // with header
	}

	// Determine the best encoding for the current block: dynamic trees, static
	// trees or store, and output the encoded block to the zip file.
	fun _tr_flush_block(
		buf: Int,  // input block, or NULL if too old
		stored_len: Int,  // length of input block
		eof: Boolean // true if this is the last block for a file
	) {
		var opt_lenb: Int
		val static_lenb: Int // opt_len and static_len in bytes
		var max_blindex = 0 // index of last bit length code of non zero freq

		// Build the Huffman trees unless a stored block is forced
		if (level > 0) {
			// Check if the file is ascii or binary
			if (data_type.toInt() == Z_UNKNOWN) set_data_type()

			// Construct the literal and distance trees
			l_desc.build_tree(this)
			d_desc.build_tree(this)

			// At this point, opt_len and static_len are the total bit lengths of
			// the compressed block data, excluding the tree representations.

			// Build the bit length tree for the above two trees, and get the index
			// in bl_order of the last bit length code to send.
			max_blindex = build_bl_tree()

			// Determine the best encoding. Compute first the block length in bytes
			opt_lenb = opt_len + 3 + 7 ushr 3
			static_lenb = static_len + 3 + 7 ushr 3
			if (static_lenb <= opt_lenb) opt_lenb = static_lenb
		} else {
			static_lenb = stored_len + 5
			opt_lenb = static_lenb // force a stored block
		}
		if (stored_len + 4 <= opt_lenb && buf != -1) {
			// 4: two words for the lengths
			// The test buf != NULL is only necessary if LIT_BUFSIZE > WSIZE.
			// Otherwise we can't have processed more than WSIZE input bytes since
			// the last block flush, because compression would have been
			// successful. If LIT_BUFSIZE <= WSIZE, it is never too late to
			// transform a block into a stored block.
			_tr_stored_block(buf, stored_len, eof)
		} else if (static_lenb == opt_lenb) {
			send_bits((STATIC_TREES shl 1) + if (eof) 1 else 0, 3)
			compress_block(StaticTree.static_ltree, StaticTree.static_dtree)
		} else {
			send_bits((DYN_TREES shl 1) + if (eof) 1 else 0, 3)
			send_all_trees(l_desc.max_code + 1, d_desc.max_code + 1, max_blindex + 1)
			compress_block(dyn_ltree, dyn_dtree)
		}

		// The above check is made mod 2^32, for files larger than 512 MB
		// and uLong implemented on 32 bits.
		init_block()
		if (eof) {
			bi_windup()
		}
	}

	// Fill the window when the lookahead becomes insufficient.
	// Updates strstart and lookahead.
	//
	// IN assertion: lookahead < MIN_LOOKAHEAD
	// OUT assertions: strstart <= window_size-MIN_LOOKAHEAD
	//    At least one byte has been read, or avail_in == 0; reads are
	//    performed for at least two bytes (required for the zip translate_eol
	//    option -- not supported here).
	fun fill_window() {
		var n: Int
		var m: Int
		var p: Int
		var more: Int // Amount of free space at the end of the window.
		do {
			more = window_size - lookahead - strstart

			// Deal with !@#$% 64K limit:
			if (more == 0 && strstart == 0 && lookahead == 0) {
				more = w_size
			} else if (more == -1) {
				// Very unlikely, but possible on 16 bit machine if strstart == 0
				// and lookahead == 1 (input done one byte at time)
				more--

				// If the window is almost full and there is insufficient lookahead,
				// move the upper half to the lower one to make room in the upper half.
			} else if (strstart >= w_size + w_size - MIN_LOOKAHEAD) {
				java.lang.System.arraycopy(window, w_size, window, 0, w_size)
				match_start -= w_size
				strstart -= w_size // we now have strstart >= MAX_DIST
				block_start -= w_size

				// Slide the hash table (could be avoided with 32 bit values
				// at the expense of memory usage). We slide even when level == 0
				// to keep the hash table consistent if we switch back to level > 0
				// later. (Using level 0 permanently is not an optimal usage of
				// zlib, so we don't care about this pathological case.)
				n = hash_size
				p = n
				do {
					m = head!![--p] and 0xffff
					head!![p] = if (m >= w_size) (m - w_size).toShort() else 0
				} while (--n != 0)
				n = w_size
				p = n
				do {
					m = prev!![--p] and 0xffff
					prev!![p] = if (m >= w_size) (m - w_size).toShort() else 0
					// If n is not on any hash chain, prev[n] is garbage but
					// its value will never be used.
				} while (--n != 0)
				more += w_size
			}
			if (strm.avail_in === 0) return

			// If there was no sliding:
			//    strstart <= WSIZE+MAX_DIST-1 && lookahead <= MIN_LOOKAHEAD - 1 &&
			//    more == window_size - lookahead - strstart
			// => more >= window_size - (MIN_LOOKAHEAD-1 + WSIZE + MAX_DIST-1)
			// => more >= window_size - 2*WSIZE + 2
			// In the BIG_MEM or MMAP case (not yet supported),
			//   window_size == input_size + MIN_LOOKAHEAD  &&
			//   strstart + s->lookahead <= input_size => more >= MIN_LOOKAHEAD.
			// Otherwise, window_size == 2*WSIZE so more >= 2.
			// If there was sliding, more >= WSIZE. So in all cases, more >= 2.
			n = strm.read_buf(window, strstart + lookahead, more)
			lookahead += n

			// Initialize the hash value now that we have some input:
			if (lookahead >= MIN_MATCH) {
				ins_h = window!![strstart] and 0xff
				ins_h = ins_h shl hash_shift xor (window!![strstart + 1] and 0xff) and hash_mask
			}
			// If the whole input has less than MIN_MATCH bytes, ins_h is garbage,
			// but this is not important since only literal bytes will be emitted.
		} while (lookahead < MIN_LOOKAHEAD && strm.avail_in !== 0)
	}

	// Compress as much as possible from the input stream, return the current
	// block state.
	// This function does not perform lazy evaluation of matches and inserts
	// new strings in the dictionary only for unmatched strings or for short
	// matches. It is used only for the fast compression options.
	fun deflate_fast(flush: Int): Int {
//    short hash_head = 0; // head of the hash chain
		var hash_head = 0 // head of the hash chain
		var bflush: Boolean // set if current block must be flushed
		while (true) {
			// Make sure that we always have enough lookahead, except
			// at the end of the input file. We need MAX_MATCH bytes
			// for the next match, plus MIN_MATCH bytes to insert the
			// string following the next match.
			if (lookahead < MIN_LOOKAHEAD) {
				fill_window()
				if (lookahead < MIN_LOOKAHEAD && flush == Z_NO_FLUSH) {
					return NeedMore
				}
				if (lookahead == 0) break // flush the current block
			}

			// Insert the string window[strstart .. strstart+2] in the
			// dictionary, and set hash_head to the head of the hash chain:
			if (lookahead >= MIN_MATCH) {
				ins_h = ins_h shl hash_shift xor (window!![strstart + (MIN_MATCH - 1)] and 0xff) and hash_mask

//	prev[strstart&w_mask]=hash_head=head[ins_h];
				hash_head = head!![ins_h] and 0xffff
				prev!![strstart and w_mask] = head!![ins_h]
				head!![ins_h] = strstart.toShort()
			}

			// Find the longest match, discarding those <= prev_length.
			// At this point we have always match_length < MIN_MATCH
			if (hash_head.toLong() != 0L &&
				strstart - hash_head and 0xffff <= w_size - MIN_LOOKAHEAD
			) {
				// To simplify the code, we prevent matches with the string
				// of window index 0 (in particular we have to avoid a match
				// of the string with itself at the start of the input file).
				if (strategy != Z_HUFFMAN_ONLY) {
					match_length = longest_match(hash_head)
				}
				// longest_match() sets match_start
			}
			if (match_length >= MIN_MATCH) {
				//        check_match(strstart, match_start, match_length);
				bflush = _tr_tally(strstart - match_start, match_length - MIN_MATCH)
				lookahead -= match_length

				// Insert new strings in the hash table only if the match length
				// is not too large. This saves time but degrades compression.
				if (match_length <= max_lazy_match &&
					lookahead >= MIN_MATCH
				) {
					match_length-- // string at strstart already in hash table
					do {
						strstart++
						ins_h = ins_h shl hash_shift xor (window!![strstart + (MIN_MATCH - 1)] and 0xff) and hash_mask
						//	    prev[strstart&w_mask]=hash_head=head[ins_h];
						hash_head = head!![ins_h] and 0xffff
						prev!![strstart and w_mask] = head!![ins_h]
						head!![ins_h] = strstart.toShort()

						// strstart never exceeds WSIZE-MAX_MATCH, so there are
						// always MIN_MATCH bytes ahead.
					} while (--match_length != 0)
					strstart++
				} else {
					strstart += match_length
					match_length = 0
					ins_h = window!![strstart] and 0xff
					ins_h = ins_h shl hash_shift xor (window!![strstart + 1] and 0xff) and hash_mask
					// If lookahead < MIN_MATCH, ins_h is garbage, but it does not
					// matter since it will be recomputed at next deflate call.
				}
			} else {
				// No match, output a literal byte
				bflush = _tr_tally(0, window!![strstart] and 0xff)
				lookahead--
				strstart++
			}
			if (bflush) {
				flush_block_only(false)
				if (strm.avail_out === 0) return NeedMore
			}
		}
		flush_block_only(flush == Z_FINISH)
		if (strm.avail_out === 0) {
			return if (flush == Z_FINISH) FinishStarted else NeedMore
		}
		return if (flush == Z_FINISH) FinishDone else BlockDone
	}

	// Same as above, but achieves better compression. We use a lazy
	// evaluation for matches: a match is finally adopted only if there is
	// no better match at the next window position.
	fun deflate_slow(flush: Int): Int {
//    short hash_head = 0;    // head of hash chain
		var hash_head = 0 // head of hash chain
		var bflush: Boolean // set if current block must be flushed

		// Process the input block.
		while (true) {
			// Make sure that we always have enough lookahead, except
			// at the end of the input file. We need MAX_MATCH bytes
			// for the next match, plus MIN_MATCH bytes to insert the
			// string following the next match.
			if (lookahead < MIN_LOOKAHEAD) {
				fill_window()
				if (lookahead < MIN_LOOKAHEAD && flush == Z_NO_FLUSH) {
					return NeedMore
				}
				if (lookahead == 0) break // flush the current block
			}

			// Insert the string window[strstart .. strstart+2] in the
			// dictionary, and set hash_head to the head of the hash chain:
			if (lookahead >= MIN_MATCH) {
				ins_h = ins_h shl hash_shift xor (window!![strstart + (MIN_MATCH - 1)] and 0xff) and hash_mask
				//	prev[strstart&w_mask]=hash_head=head[ins_h];
				hash_head = head!![ins_h] and 0xffff
				prev!![strstart and w_mask] = head!![ins_h]
				head!![ins_h] = strstart.toShort()
			}

			// Find the longest match, discarding those <= prev_length.
			prev_length = match_length
			prev_match = match_start
			match_length = MIN_MATCH - 1
			if (hash_head != 0 && prev_length < max_lazy_match && strstart - hash_head and 0xffff <= w_size - MIN_LOOKAHEAD) {
				// To simplify the code, we prevent matches with the string
				// of window index 0 (in particular we have to avoid a match
				// of the string with itself at the start of the input file).
				if (strategy != Z_HUFFMAN_ONLY) {
					match_length = longest_match(hash_head)
				}
				// longest_match() sets match_start
				if (match_length <= 5 && (strategy == Z_FILTERED ||
						match_length == MIN_MATCH &&
						strstart - match_start > 4096)
				) {

					// If prev_match is also MIN_MATCH, match_start is garbage
					// but we will ignore the current match anyway.
					match_length = MIN_MATCH - 1
				}
			}

			// If there was a match at the previous step and the current
			// match is not better, output the previous match:
			if (prev_length >= MIN_MATCH && match_length <= prev_length) {
				val max_insert = strstart + lookahead - MIN_MATCH
				// Do not insert strings in hash table beyond this.

				//          check_match(strstart-1, prev_match, prev_length);
				bflush = _tr_tally(strstart - 1 - prev_match, prev_length - MIN_MATCH)

				// Insert in hash table all strings up to the end of the match.
				// strstart-1 and strstart are already inserted. If there is not
				// enough lookahead, the last two strings are not inserted in
				// the hash table.
				lookahead -= prev_length - 1
				prev_length -= 2
				do {
					if (++strstart <= max_insert) {
						ins_h = ins_h shl hash_shift xor (window!![strstart + (MIN_MATCH - 1)] and 0xff) and hash_mask
						//prev[strstart&w_mask]=hash_head=head[ins_h];
						hash_head = head!![ins_h] and 0xffff
						prev!![strstart and w_mask] = head!![ins_h]
						head!![ins_h] = strstart.toShort()
					}
				} while (--prev_length != 0)
				match_available = 0
				match_length = MIN_MATCH - 1
				strstart++
				if (bflush) {
					flush_block_only(false)
					if (strm.avail_out === 0) return NeedMore
				}
			} else if (match_available != 0) {

				// If there was no match at the previous position, output a
				// single literal. If there was a match but the current match
				// is longer, truncate the previous match to a single literal.
				bflush = _tr_tally(0, window!![strstart - 1] and 0xff)
				if (bflush) {
					flush_block_only(false)
				}
				strstart++
				lookahead--
				if (strm.avail_out === 0) return NeedMore
			} else {
				// There is no previous match to compare with, wait for
				// the next step to decide.
				match_available = 1
				strstart++
				lookahead--
			}
		}
		if (match_available != 0) {
			bflush = _tr_tally(0, window!![strstart - 1] and 0xff)
			match_available = 0
		}
		flush_block_only(flush == Z_FINISH)
		if (strm.avail_out === 0) {
			return if (flush == Z_FINISH) FinishStarted else NeedMore
		}
		return if (flush == Z_FINISH) FinishDone else BlockDone
	}

	fun longest_match(cur_match: Int): Int {
		var cur_match = cur_match
		var chain_length = max_chain_length // max hash chain length
		var scan = strstart // current string
		var match: Int // matched string
		var len: Int // length of current match
		var best_len = prev_length // best match length so far
		val limit = if (strstart > w_size - MIN_LOOKAHEAD) strstart - (w_size - MIN_LOOKAHEAD) else 0
		var nice_match = nice_match

		// Stop when cur_match becomes <= limit. To simplify the code,
		// we prevent matches with the string of window index 0.
		val wmask = w_mask
		val strend = strstart + MAX_MATCH
		var scan_end1 = window!![scan + best_len - 1]
		var scan_end = window!![scan + best_len]

		// The code is optimized for HASH_BITS >= 8 and MAX_MATCH-2 multiple of 16.
		// It is easy to get rid of this optimization if necessary.

		// Do not waste too much time if we already have a good match:
		if (prev_length >= good_match) {
			chain_length = chain_length shr 2
		}

		// Do not look for matches beyond the end of the input. This is necessary
		// to make deflate deterministic.
		if (nice_match > lookahead) nice_match = lookahead
		do {
			match = cur_match

			// Skip to next match if the match length cannot increase
			// or if the match length is less than 2:
			if (window!![match + best_len] != scan_end || window!![match + best_len - 1] != scan_end1 || window!![match] != window!![scan] || window!![++match] != window!![scan + 1]) continue

			// The check at best_len-1 can be removed because it will be made
			// again later. (This heuristic is not always a win.)
			// It is not necessary to compare scan[2] and match[2] since they
			// are always equal when the other bytes match, given that
			// the hash keys are equal and that HASH_BITS >= 8.
			scan += 2
			match++

			// We check for insufficient lookahead only every 8th comparison;
			// the 256th check will be made at strstart+258.
			do {
			} while (window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && window!![++scan] == window!![++match] && scan < strend)
			len = MAX_MATCH - (strend - scan)
			scan = strend - MAX_MATCH
			if (len > best_len) {
				match_start = cur_match
				best_len = len
				if (len >= nice_match) break
				scan_end1 = window!![scan + best_len - 1]
				scan_end = window!![scan + best_len]
			}
		} while ((prev!![cur_match and wmask] and 0xffff).also { cur_match = it } > limit
			&& --chain_length != 0)
		return if (best_len <= lookahead) best_len else lookahead
	}

	fun deflateInit(level: Int, bits: Int, memlevel: Int): Int {
		return deflateInit(
			level, Z_DEFLATED, bits, memlevel,
			Z_DEFAULT_STRATEGY
		)
	}

	@JvmOverloads
	fun deflateInit(level: Int, bits: Int = MAX_WBITS): Int {
		return deflateInit(
			level, Z_DEFLATED, bits, DEF_MEM_LEVEL,
			Z_DEFAULT_STRATEGY
		)
	}

	private fun deflateInit(
		level: Int, method: Int, windowBits: Int,
		memLevel: Int, strategy: Int
	): Int {
		var level = level
		var windowBits = windowBits
		var wrap = 1
		//    byte[] my_version=ZLIB_VERSION;

		//
		//  if (version == null || version[0] != my_version[0]
		//  || stream_size != sizeof(z_stream)) {
		//  return Z_VERSION_ERROR;
		//  }
		strm.msg = null
		if (level == Z_DEFAULT_COMPRESSION) level = 6
		if (windowBits < 0) { // undocumented feature: suppress zlib header
			wrap = 0
			windowBits = -windowBits
		} else if (windowBits > 15) {
			wrap = 2
			windowBits -= 16
			strm.adler = CRC32()
		}
		if (memLevel < 1 || memLevel > MAX_MEM_LEVEL || method != Z_DEFLATED || windowBits < 9 || windowBits > 15 || level < 0 || level > 9 || strategy < 0 || strategy > Z_HUFFMAN_ONLY) {
			return Z_STREAM_ERROR
		}
		strm.dstate = this
		this.wrap = wrap
		w_bits = windowBits
		w_size = 1 shl w_bits
		w_mask = w_size - 1
		hash_bits = memLevel + 7
		hash_size = 1 shl hash_bits
		hash_mask = hash_size - 1
		hash_shift = (hash_bits + MIN_MATCH - 1) / MIN_MATCH
		window = ByteArray(w_size * 2)
		prev = ShortArray(w_size)
		head = ShortArray(hash_size)
		lit_bufsize = 1 shl memLevel + 6 // 16K elements by default

		// We overlay pending_buf and d_buf+l_buf. This works since the average
		// output size for (length,distance) codes is <= 24 bits.
		pending_buf = ByteArray(lit_bufsize * 3)
		pending_buf_size = lit_bufsize * 3
		d_buf = lit_bufsize
		l_buf = ByteArray(lit_bufsize)
		this.level = level
		this.strategy = strategy
		this.method = method.toByte()
		return deflateReset()
	}

	fun deflateReset(): Int {
		strm.total_out = 0
		strm.total_in = strm.total_out
		strm.msg = null //
		strm.data_type = Z_UNKNOWN
		pending = 0
		pending_out = 0
		if (wrap < 0) {
			wrap = -wrap
		}
		status = if (wrap == 0) BUSY_STATE else INIT_STATE
		strm.adler.reset()
		last_flush = Z_NO_FLUSH
		tr_init()
		lm_init()
		return Z_OK
	}

	fun deflateEnd(): Int {
		if (status != INIT_STATE && status != BUSY_STATE && status != FINISH_STATE) {
			return Z_STREAM_ERROR
		}
		// Deallocate in reverse order of allocations:
		pending_buf = null
		l_buf = null
		head = null
		prev = null
		window = null
		// free
		// dstate=null;
		return if (status == BUSY_STATE) Z_DATA_ERROR else Z_OK
	}

	fun deflateParams(_level: Int, _strategy: Int): Int {
		var _level = _level
		var err = Z_OK
		if (_level == Z_DEFAULT_COMPRESSION) {
			_level = 6
		}
		if (_level < 0 || _level > 9 || _strategy < 0 || _strategy > Z_HUFFMAN_ONLY) {
			return Z_STREAM_ERROR
		}
		if (config_table[level]!!.func != config_table[_level]!!.func &&
			strm.total_in !== 0
		) {
			// Flush the last buffer:
			err = strm.deflate(Z_PARTIAL_FLUSH)
		}
		if (level != _level) {
			level = _level
			max_lazy_match = config_table[level]!!.max_lazy
			good_match = config_table[level]!!.good_length
			nice_match = config_table[level]!!.nice_length
			max_chain_length = config_table[level]!!.max_chain
		}
		strategy = _strategy
		return err
	}

	fun deflateSetDictionary(dictionary: ByteArray?, dictIndex: Int, dictLength: Int): Int {
		var dictIndex = dictIndex
		var length = dictLength
		if (dictionary == null || status != INIT_STATE) return Z_STREAM_ERROR
		strm.adler.update(dictionary, dictIndex, dictLength)
		if (length < MIN_MATCH) return Z_OK
		if (length > w_size - MIN_LOOKAHEAD) {
			length = w_size - MIN_LOOKAHEAD
			dictIndex = dictLength - length // use the tail of the dictionary
		}
		java.lang.System.arraycopy(dictionary, dictIndex, window, 0, length)
		strstart = length
		block_start = length

		// Insert all strings in the hash table (except for the last two bytes).
		// s->lookahead stays null, so s->ins_h will be recomputed at the next
		// call of fill_window.
		ins_h = window!![0] and 0xff
		ins_h = ins_h shl hash_shift xor (window!![1] and 0xff) and hash_mask
		for (n in 0..length - MIN_MATCH) {
			ins_h = ins_h shl hash_shift xor (window!![n + (MIN_MATCH - 1)] and 0xff) and hash_mask
			prev!![n and w_mask] = head!![ins_h]
			head!![ins_h] = n.toShort()
		}
		return Z_OK
	}

	fun deflate(flush: Int): Int {
		val old_flush: Int
		if (flush > Z_FINISH || flush < 0) {
			return Z_STREAM_ERROR
		}
		if (strm.next_out == null ||
			strm.next_in == null && strm.avail_in !== 0 ||
			status == FINISH_STATE && flush != Z_FINISH
		) {
			strm.msg = z_errmsg[Z_NEED_DICT - Z_STREAM_ERROR]
			return Z_STREAM_ERROR
		}
		if (strm.avail_out === 0) {
			strm.msg = z_errmsg[Z_NEED_DICT - Z_BUF_ERROR]
			return Z_BUF_ERROR
		}
		old_flush = last_flush
		last_flush = flush

		// Write the zlib header
		if (status == INIT_STATE) {
			if (wrap == 2) {
				gZIPHeader.put(this)
				status = BUSY_STATE
				strm.adler.reset()
			} else {
				var header = Z_DEFLATED + (w_bits - 8 shl 4) shl 8
				var level_flags = level - 1 and 0xff shr 1
				if (level_flags > 3) level_flags = 3
				header = header or (level_flags shl 6)
				if (strstart != 0) header = header or PRESET_DICT
				header += 31 - header % 31
				status = BUSY_STATE
				putShortMSB(header)


				// Save the adler32 of the preset dictionary:
				if (strstart != 0) {
					val adler = strm.adler.value
					putShortMSB((adler ushr 16))
					putShortMSB((adler and 0xffff))
				}
				strm.adler.reset()
			}
		}

		// Flush as much pending output as possible
		if (pending != 0) {
			strm.flush_pending()
			if (strm.avail_out === 0) {
				// Since avail_out is 0, deflate will be called again with
				// more output space, but possibly with both pending and
				// avail_in equal to zero. There won't be anything to do,
				// but this is not an error situation so make sure we
				// return OK instead of BUF_ERROR at next call of deflate:
				last_flush = -1
				return Z_OK
			}

			// Make sure there is something to do and avoid duplicate consecutive
			// flushes. For repeated and useless calls with Z_FINISH, we keep
			// returning Z_STREAM_END instead of Z_BUFF_ERROR.
		} else if (strm.avail_in === 0 && flush <= old_flush && flush != Z_FINISH) {
			strm.msg = z_errmsg[Z_NEED_DICT - Z_BUF_ERROR]
			return Z_BUF_ERROR
		}

		// User must not provide more input after the first FINISH:
		if (status == FINISH_STATE && strm.avail_in !== 0) {
			strm.msg = z_errmsg[Z_NEED_DICT - Z_BUF_ERROR]
			return Z_BUF_ERROR
		}

		// Start a new block or continue the current one.
		if (strm.avail_in !== 0 || lookahead != 0 ||
			flush != Z_NO_FLUSH && status != FINISH_STATE
		) {
			var bstate = -1
			when (config_table[level]!!.func) {
				STORED -> bstate = deflate_stored(flush)
				FAST -> bstate = deflate_fast(flush)
				SLOW -> bstate = deflate_slow(flush)
				else -> {
				}
			}
			if (bstate == FinishStarted || bstate == FinishDone) {
				status = FINISH_STATE
			}
			if (bstate == NeedMore || bstate == FinishStarted) {
				if (strm.avail_out === 0) {
					last_flush = -1 // avoid BUF_ERROR next call, see above
				}
				return Z_OK
				// If flush != Z_NO_FLUSH && avail_out == 0, the next call
				// of deflate should use the same flush parameter to make sure
				// that the flush is complete. So we don't have to output an
				// empty block here, this will be done at next call. This also
				// ensures that for a very small output buffer, we emit at most
				// one empty block.
			}
			if (bstate == BlockDone) {
				if (flush == Z_PARTIAL_FLUSH) {
					_tr_align()
				} else { // FULL_FLUSH or SYNC_FLUSH
					_tr_stored_block(0, 0, false)
					// For a full flush, this empty block will be recognized
					// as a special marker by inflate_sync().
					if (flush == Z_FULL_FLUSH) {
						//state.head[s.hash_size-1]=0;
						for (i in 0 until hash_size)  // forget history
							head!![i] = 0
					}
				}
				strm.flush_pending()
				if (strm.avail_out === 0) {
					last_flush = -1 // avoid BUF_ERROR at next call, see above
					return Z_OK
				}
			}
		}
		if (flush != Z_FINISH) return Z_OK
		if (wrap <= 0) return Z_STREAM_END
		if (wrap == 2) {
			val adler = strm.adler.value
			put_byte((adler and 0xff).toByte())
			put_byte((adler shr 8 and 0xff).toByte())
			put_byte((adler shr 16 and 0xff).toByte())
			put_byte((adler shr 24 and 0xff).toByte())
			put_byte((strm.total_in and 0xff) as Byte)
			put_byte((strm.total_in shr 8 and 0xff) as Byte)
			put_byte((strm.total_in shr 16 and 0xff) as Byte)
			put_byte((strm.total_in shr 24 and 0xff) as Byte)
			gZIPHeader.cRC = adler.toLong()
		} else {
			// Write the zlib trailer (adler32)
			val adler = strm.adler.value
			putShortMSB((adler ushr 16))
			putShortMSB((adler and 0xffff))
		}
		strm.flush_pending()

		// If avail_out is zero, the application will call deflate again
		// to flush the rest.
		if (wrap > 0) wrap = -wrap // write the trailer only once!
		return if (pending != 0) Z_OK else Z_STREAM_END
	}

	@Throws(CloneNotSupportedException::class)
	override fun clone(): Any {
		val dest = super.clone() as Deflate
		dest.pending_buf = dup(dest.pending_buf)
		dest.d_buf = dest.d_buf
		dest.l_buf = dup(dest.l_buf)
		dest.window = dup(dest.window)
		dest.prev = dup(dest.prev)
		dest.head = dup(dest.head)
		dest.dyn_ltree = dup(dest.dyn_ltree)
		dest.dyn_dtree = dup(dest.dyn_dtree)
		dest.bl_tree = dup(dest.bl_tree)
		dest.bl_count = dup(dest.bl_count)
		dest.next_code = dup(dest.next_code)
		dest.heap = dup(dest.heap)
		dest.depth = dup(dest.depth)
		dest.l_desc.dyn_tree = dest.dyn_ltree
		dest.d_desc.dyn_tree = dest.dyn_dtree
		dest.bl_desc.dyn_tree = dest.bl_tree

		/*
    dest.l_desc.stat_desc = StaticTree.static_l_desc;
    dest.d_desc.stat_desc = StaticTree.static_d_desc;
    dest.bl_desc.stat_desc = StaticTree.static_bl_desc;
    */if (dest.gheader != null) {
			dest.gheader = dest.gheader!!.clone() as GZIPHeader
		}
		return dest
	}

	private fun dup(buf: ByteArray?): ByteArray {
		val foo = ByteArray(buf!!.size)
		java.lang.System.arraycopy(buf, 0, foo, 0, foo.size)
		return foo
	}

	private fun dup(buf: ShortArray?): ShortArray {
		val foo = ShortArray(buf!!.size)
		java.lang.System.arraycopy(buf, 0, foo, 0, foo.size)
		return foo
	}

	private fun dup(buf: IntArray): IntArray {
		val foo = IntArray(buf.size)
		java.lang.System.arraycopy(buf, 0, foo, 0, foo.size)
		return foo
	}

	@get:Synchronized
	val gZIPHeader: GZIPHeader
		get() {
			if (gheader == null) {
				gheader = GZIPHeader()
			}
			return gheader!!
		}

	init {
		dyn_ltree = ShortArray(HEAP_SIZE * 2)
		dyn_dtree = ShortArray((2 * D_CODES + 1) * 2) // distance tree
		bl_tree = ShortArray((2 * BL_CODES + 1) * 2) // Huffman tree for bit lengths
	}
}