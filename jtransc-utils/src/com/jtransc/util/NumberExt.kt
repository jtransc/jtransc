@file:Suppress("NOTHING_TO_INLINE")

package com.jtransc.util

inline fun Int.mask(): Int = (1 shl this) - 1
inline fun Long.mask(): Long = (1L shl this.toInt()) - 1L
fun Int.toUInt(): Long = this.toLong() and 0xFFFFFFFFL
fun Int.getBits(offset: Int, count: Int): Int = (this ushr offset) and count.mask()
fun Int.extract(offset: Int, count: Int): Int = (this ushr offset) and count.mask()
fun Int.extract8(offset: Int): Int = (this ushr offset) and 0xFF
fun Int.extract(offset: Int): Boolean = ((this ushr offset) and 1) != 0

fun Int.extractScaled(offset: Int, count: Int, scale: Int): Int {
	val mask = count.mask()
	return (extract(offset, count) * scale) / mask
}

fun Int.extractScaledf01(offset: Int, count: Int): Double {
	val mask = count.mask().toDouble()
	return extract(offset, count).toDouble() / mask
}

fun Int.extractScaledFF(offset: Int, count: Int): Int = extractScaled(offset, count, 0xFF)
fun Int.extractScaledFFDefault(offset: Int, count: Int, default: Int): Int = if (count == 0) default else extractScaled(offset, count, 0xFF)

fun Int.insert(value: Int, offset: Int, count: Int): Int {
	val mask = count.mask()
	val clearValue = this and (mask shl offset).inv()
	return clearValue or ((value and mask) shl offset)
}

fun Int.insert8(value: Int, offset: Int): Int = insert(value, offset, 8)

fun Int.insert(value: Boolean, offset: Int): Int = this.insert(if (value) 1 else 0, offset, 1)

fun Int.insertScaled(value: Int, offset: Int, count: Int, scale: Int): Int {
	val mask = count.mask()
	return insert((value * mask) / scale, offset, count)
}

fun Int.insertScaledFF(value: Int, offset: Int, count: Int): Int = if (count == 0) this else this.insertScaled(value, offset, count, 0xFF)

fun Long.nextAlignedTo(align: Long) = if (this % align == 0L) {
	this
} else {
	(((this / align) + 1) * align)
}

fun Int.clamp(min: Int, max: Int): Int = if (this < min) min else if (this > max) max else this
fun Double.clamp(min: Double, max: Double): Double = if (this < min) min else if (this > max) max else this
fun Long.clamp(min: Long, max: Long): Long = if (this < min) min else if (this > max) max else this

fun Long.toIntSafe(): Int {
	if (this.toInt().toLong() != this) throw IllegalArgumentException("Long doesn't fit Integer")
	return this.toInt()
}

fun Long.toIntClamp(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
	if (this < min) return min
	if (this > max) return max
	return this.toInt()
}

fun Long.toUintClamp(min: Int = 0, max: Int = Int.MAX_VALUE) = this.toIntClamp(0, Int.MAX_VALUE)

fun String.toDoubleOrNull2(): Double? = try {
	this.toDouble()
} catch (e: NumberFormatException) {
	null
}

fun String.toLongOrNull2(): Long? = try {
	this.toLong()
} catch (e: NumberFormatException) {
	null
}

fun String.toIntOrNull2(): Int? = try {
	this.toInt()
} catch (e: NumberFormatException) {
	null
}

fun String.toNumber(): Number = this.toIntOrNull2() as Number? ?: this.toLongOrNull2() as Number? ?: this.toDoubleOrNull2() as Number? ?: 0

fun Byte.toUnsigned() = this.toInt() and 0xFF
fun Int.toUnsigned() = this.toLong() and 0xFFFFFFFFL

fun Int.signExtend(bits: Int) = (this shl (32 - bits)) shr (32 - bits)
fun Long.signExtend(bits: Int) = (this shl (64 - bits)) shr (64 - bits)

infix fun Int.umod(other: Int): Int {
	val remainder = this % other
	return when {
		remainder < 0 -> remainder + other
		else -> remainder
	}
}

fun Double.convertRange(srcMin: Double, srcMax: Double, dstMin: Double, dstMax: Double): Double {
	val ratio = (this - srcMin) / (srcMax - srcMin)
	return (dstMin + (dstMax - dstMin) * ratio)
}

fun Long.convertRange(srcMin: Long, srcMax: Long, dstMin: Long, dstMax: Long): Long {
	val ratio = (this - srcMin).toDouble() / (srcMax - srcMin).toDouble()
	return (dstMin + (dstMax - dstMin) * ratio).toLong()
}


object Bits {
	@JvmStatic
	fun mask(count: Int): Int = (1 shl count) - 1

	@JvmStatic
	fun extract(data: Int, offset: Int, length: Int): Int {
		return (data ushr offset) and mask(length)
	}

	@JvmStatic
	fun extractBool(data: Int, offset: Int): Boolean = extract(data, offset, 1) != 0

}

fun Int.extractBool(offset: Int): Boolean = Bits.extractBool(this, offset)
fun Int.hasBitmask(mask: Int): Boolean = (this and mask) == mask

fun Int.withBool(offset: Int): Int {
	val v = (1 shl offset)
	return (this and v.inv()) or (v)
}

fun Int.withoutBool(offset: Int): Int {
	val v = (1 shl offset)
	return (this and v.inv())
}
