@file:Suppress("unused")

class CRC32 {
	/*
	 *  The following logic has come from RFC1952.
     */
	private var v = 0

	companion object {
		private val crc_table: IntArray = IntArray(256).also { crc_table ->
			for (n in 0..255) {
				var c = n
				var k = 8
				while (--k >= 0) {
					c = if (c and 1 != 0) {
						-0x12477ce0 xor (c ushr 1)
					} else {
						c ushr 1
					}
				}
				crc_table[n] = c
			}
		}
	}

	fun update(buf: ByteArray, index: Int, len: Int) {
		//int[] crc_table = CRC32.crc_table;
		var idx = index
		var l = len
		var c = v.inv()
		while (--l >= 0) {
			c = crc_table[c xor buf[idx++].toInt() and 0xff] xor (c ushr 8)
		}
		v = c.inv()
	}

	fun reset() {
		v = 0
	}

	fun reset(vv: Int) {
		v = vv
	}

	val value: Int get() = v

	fun copy(): CRC32 {
		val foo = CRC32()
		foo.v = v
		return foo
	}
}
