
expect fun currentTimeMillis(): Double
fun gc() {

}
fun arraycopy(src: ByteArray, srcPos: Int, dst: ByteArray, dstPos: Int, size: Int) = src.copyInto(dst, dstPos, srcPos, srcPos + size)
fun arraycopy(src: IntArray, srcPos: Int, dst: IntArray, dstPos: Int, size: Int) = src.copyInto(dst, dstPos, srcPos, srcPos + size)

