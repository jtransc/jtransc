package simd

object Float32x4Utils {

	fun toStringInternal(v: Float32x4?): String {
		return "Simd.Float32x4(" + Float32x4.getX(v!!).toString() + ", " + Float32x4.getY(v)
			.toString() + ", " + Float32x4.getZ(
			v
		).toString() + ", " + Float32x4.getW(v).toString() + ")"
	}
}