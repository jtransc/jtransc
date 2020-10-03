package simd

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MutableFloat32x4 private constructor(x: Float, y: Float, z: Float, w: Float) {
	companion object {
		fun create(x: Float, y: Float, z: Float, v: Float): MutableFloat32x4 {
			return MutableFloat32x4(x, y, z, v)
		}

		fun create(): MutableFloat32x4 {
			return create(0f, 0f, 0f, 0f)
		}

		fun create(v: Float): MutableFloat32x4 {
			return create(v, v, v, v)
		}

		init {
			Simd.ref()
		}
	}

	var x = 0f
		private set

	var y = 0f
		private set

	var z = 0f
		private set

	var w = 0f
		private set

	fun setTo(x: Float, y: Float, z: Float, w: Float) {
		this.x = x
		this.y = y
		this.z = z
		this.w = w
	}

	fun setToZero() {
		this.setTo(0f, 0f, 0f, 0f)
	}

	fun setTo(l: MutableFloat32x4) {
		x = l.x
		y = l.y
		z = l.z
		w = l.w
	}

	fun setToNeg(l: MutableFloat32x4) {
		setTo(-l.x, -l.y, -l.z, -l.w)
	}

	fun setToAbs(l: MutableFloat32x4) {
		setTo(abs(l.x), abs(l.y), abs(l.z), abs(l.w))
	}

	fun setToMul(l: MutableFloat32x4, r: MutableFloat32x4) {
		setTo(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w)
	}

	fun setToMul(l: MutableFloat32x4, r: Float) {
		setTo(l.x * r, l.y * r, l.z * r, l.w * r)
	}

	fun setToDiv(l: MutableFloat32x4, r: Float) {
		setTo(l.x / r, l.y / r, l.z / r, l.w / r)
	}

	fun setToAdd(l: MutableFloat32x4, r: MutableFloat32x4) {
		setTo(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w)
	}

	fun setToAddMul(add: MutableFloat32x4, l: MutableFloat32x4, r: MutableFloat32x4) {
		setTo(
			add.x + l.x * r.x,
			add.y + l.y * r.y,
			add.z + l.z * r.z,
			add.w + l.w * r.w
		)
	}

	fun setToSub(l: MutableFloat32x4, r: MutableFloat32x4) {
		setTo(l.x - r.x, l.y - r.y, l.z - r.z, l.w - r.w)
	}

	fun setToMax(l: MutableFloat32x4, r: MutableFloat32x4) {
		setTo(
			max(l.x, r.x),
			max(l.y, r.y),
			max(l.z, r.z),
			max(l.w, r.w)
		)
	}

	fun setToMin(l: MutableFloat32x4, r: MutableFloat32x4) {
		setTo(
			min(l.x, r.x),
			min(l.y, r.y),
			min(l.z, r.z),
			min(l.w, r.w)
		)
	}

	val sumAll: Float
		get() = x + y + z + w

	fun setToXXXX(l: MutableFloat32x4) {
		l.setTo(l.x, l.x, l.x, l.x)
	}

	fun setToYYYY(l: MutableFloat32x4) {
		l.setTo(l.y, l.y, l.y, l.y)
	}

	fun setToZZZZ(l: MutableFloat32x4) {
		l.setTo(l.z, l.z, l.z, l.z)
	}

	fun setToWWWW(l: MutableFloat32x4) {
		l.setTo(l.w, l.w, l.w, l.w)
	}


	fun setToMultiply(vector: MutableFloat32x4, matrix: MutableMatrixFloat32x4x4) {
		this.setTo(
			MutableFloat32x4Utils.getAddMul(vector, matrix.getX()),
			MutableFloat32x4Utils.getAddMul(vector, matrix.getY()),
			MutableFloat32x4Utils.getAddMul(vector, matrix.getZ()),
			MutableFloat32x4Utils.getAddMul(vector, matrix.getW())
		)
	}

	fun getLane(index: Int): Float {
		return when (index) {
			0 -> x
			1 -> y
			2 -> z
			3 -> w
			else -> x
		}
	}

	override fun toString(): String {
		return MutableFloat32x4Utils.toStringInternal(this)
	}

	init {
		setTo(x, y, z, w)
	}
}