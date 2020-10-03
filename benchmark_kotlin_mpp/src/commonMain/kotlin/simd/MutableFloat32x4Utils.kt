package simd

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
private val temp: MutableFloat32x4 = MutableFloat32x4.create()

object MutableFloat32x4Utils {

	fun getAddMul(l: MutableFloat32x4, r: MutableFloat32x4): Float {
		temp.setToMul(l, r)
		return temp.sumAll
	}


	fun toStringInternal(v: MutableFloat32x4): String {
		return "Simd.MutableFloat32x4(" + v.x.toString() + ", " + v.y.toString() + ", " + v.z
			.toString() + ", " + v.w.toString() + ")"
	}
}