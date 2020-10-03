package simd

class MutableMatrixFloat32x4x4  private constructor() {
	companion object {
		fun create(): MutableMatrixFloat32x4x4 {
			return MutableMatrixFloat32x4x4()
		}

		init {
			Simd.ref()
		}
	}

	//final private MutableFloat32x4[] v = new MutableFloat32x4[]{MutableFloat32x4.create(), MutableFloat32x4.create(), MutableFloat32x4.create(), MutableFloat32x4.create()};
	private val x: MutableFloat32x4 = MutableFloat32x4.create()
	private val y: MutableFloat32x4 = MutableFloat32x4.create()
	private val z: MutableFloat32x4 = MutableFloat32x4.create()
	private val w: MutableFloat32x4 = MutableFloat32x4.create()

	fun setTo(
		m00: Float, m01: Float, m02: Float, m03: Float,
		m10: Float, m11: Float, m12: Float, m13: Float,
		m20: Float, m21: Float, m22: Float, m23: Float,
		m30: Float, m31: Float, m32: Float, m33: Float
	) {
		getX().setTo(m00, m01, m02, m03)
		getY().setTo(m10, m11, m12, m13)
		getZ().setTo(m20, m21, m22, m23)
		getW().setTo(m30, m31, m32, m33)
	}

	fun setTo(x: MutableFloat32x4, y: MutableFloat32x4, z: MutableFloat32x4, w: MutableFloat32x4) {
		getX().setTo(x)
		getY().setTo(y)
		getZ().setTo(z)
		getW().setTo(w)
	}

	fun setX(v: MutableFloat32x4) {
		getX().setTo(v)
	}

	fun setY(v: MutableFloat32x4) {
		getY().setTo(v)
	}

	fun setZ(v: MutableFloat32x4) {
		getZ().setTo(v)
	}

	fun setW(v: MutableFloat32x4) {
		getW().setTo(v)
	}

	fun setRow(index: Int, v: MutableFloat32x4) {
		//this.v[index].setTo(v);
		when (index) {
			0 -> x.setTo(v)
			1 -> y.setTo(v)
			2 -> z.setTo(v)
			3 -> w.setTo(v)
		}
	}

	fun getX(): MutableFloat32x4 {
		return x
	}

	fun getY(): MutableFloat32x4 {
		return y
	}

	fun getZ(): MutableFloat32x4 {
		return z
	}

	fun getW(): MutableFloat32x4 {
		return w
	}

	fun getRow(index: Int): MutableFloat32x4 {
		when (index) {
			0 -> return x
			1 -> return y
			2 -> return z
			3 -> return w
		}
		return x
	}

	fun getCell(row: Int, column: Int): Float {
		return MutableMatrixFloat32x4x4Utils._getCell(this, row, column)
	}

	val sumAll: Float
		get() = MutableMatrixFloat32x4x4Utils._getSumAll(this)

	fun setToMul44(a: MutableMatrixFloat32x4x4, b: MutableMatrixFloat32x4x4) {
		MutableMatrixFloat32x4x4Utils._setToMul44(this, a, b)
	}
}