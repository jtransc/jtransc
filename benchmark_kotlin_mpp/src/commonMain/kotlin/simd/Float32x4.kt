package simd

/**
 * IMMUTABLE FLOAT32 x 4
 */
class Float32x4  private constructor(
	private val x: Float,
	private val y: Float,
	private val z: Float,
	private val w: Float
) {
	companion object {
		fun create(): Float32x4 {
			return Float32x4(0f, 0f, 0f, 0f)
		}

		fun create(x: Float, y: Float, z: Float, w: Float): Float32x4 {
			return Float32x4(x, y, z, w)
		}

		fun add(l: Float32x4, r: Float32x4): Float32x4 {
			return Float32x4(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w)
		}

		//@JTranscCallSiteBody(target = "dart", value = "((#0) + (#1) + (#2) + (#3))")

		fun add(a: Float32x4, b: Float32x4, c: Float32x4, d: Float32x4): Float32x4 {
			return add(add(a, b), add(c, d))
		}

		fun mul(l: Float32x4, r: Float32x4): Float32x4 {
			return Float32x4(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w)
		}

		fun mul(l: Float32x4, r: Float): Float32x4 {
			return Float32x4(l.x * r, l.y * r, l.z * r, l.w * r)
		}

		fun getX(l: Float32x4): Float {
			return l.x
		}

		fun getY(l: Float32x4): Float {
			return l.y
		}

		fun getZ(l: Float32x4): Float {
			return l.z
		}

		fun getW(l: Float32x4): Float {
			return l.w
		}

		fun getLane(l: Float32x4, index: Int): Float {
			return when (index) {
				0 -> getX(l)
				1 -> getY(l)
				2 -> getZ(l)
				3 -> getW(l)
				else -> getX(l)
			}
		}


		fun xxxx(l: Float32x4): Float32x4 {
			return create(getX(l), getX(l), getX(l), getX(l))
		}


		fun yyyy(l: Float32x4): Float32x4 {
			return create(getY(l), getY(l), getY(l), getY(l))
		}


		fun zzzz(l: Float32x4): Float32x4 {
			return create(getZ(l), getZ(l), getZ(l), getZ(l))
		}


		fun wwww(l: Float32x4): Float32x4 {
			return create(getW(l), getW(l), getW(l), getW(l))
		}


		fun mul44(R: Array<Float32x4?>, A: Array<Float32x4>, B: Array<Float32x4>) {
			val a0 = A[0]
			val a1 = A[1]
			val a2 = A[2]
			val a3 = A[3]
			val b0 = B[0]
			val b1 = B[1]
			val b2 = B[2]
			val b3 = B[3]
			R[0] = add(mul(xxxx(b0), a0), mul(yyyy(b0), a1), mul(zzzz(b0), a2), mul(wwww(b0), a3))
			R[1] = add(mul(xxxx(b1), a0), mul(yyyy(b1), a1), mul(zzzz(b1), a2), mul(wwww(b1), a3))
			R[2] = add(mul(xxxx(b2), a0), mul(yyyy(b2), a1), mul(zzzz(b2), a2), mul(wwww(b2), a3))
			R[3] = add(mul(xxxx(b3), a0), mul(yyyy(b3), a1), mul(zzzz(b3), a2), mul(wwww(b3), a3))
		}

		fun toString(v: Float32x4?): String {
			return Float32x4Utils.toStringInternal(v)
		}

		init {
			Simd.ref()
		}
	}
}