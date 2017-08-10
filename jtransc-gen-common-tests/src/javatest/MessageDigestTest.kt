package javatest

import java.security.MessageDigest
import java.util.*

object MessageDigestTest {
	@JvmStatic fun main(args: Array<String>) {
		println("MessageDigestTest:")
		val random = Random(0L)
		val data = (0 until 0x100).map { random.nextInt().toByte() }.toByteArray()
		for (method in arrayOf("MD5", "SHA1")) {
			println("$method:" + MessageDigest.getInstance(method).digest().toHexString())
			println("$method:" + MessageDigest.getInstance(method).digest(byteArrayOf(1)).toHexString())
			println("$method:" + MessageDigest.getInstance(method).digest(byteArrayOf(1, 2)).toHexString())
			println("$method:" + MessageDigest.getInstance(method).digest(byteArrayOf(1, 2, 3)).toHexString())
			println("$method:" + MessageDigest.getInstance(method).digest(data).toHexString())
		}
	}
}

val HexDigitsLC = "0123456789abcdef"

fun ByteArray.toHexString():String {
	val out = StringBuilder(this.size * 2)
	for (b in this) {
		val bi = b.toInt()
		val l = ((bi ushr 0) and 0xF)
		val h = ((bi ushr 4) and 0xF)
		out.append(HexDigitsLC[h])
		out.append(HexDigitsLC[l])
	}
	return out.toString()
}
