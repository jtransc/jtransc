package simd

object MutableMatrixFloat32x4x4Utils {

	fun _getCell(src: MutableMatrixFloat32x4x4, row: Int, column: Int): Float {
		return src.getRow(row).getLane(column)
	}


	fun _getSumAll(a: MutableMatrixFloat32x4x4): Float {
		return a.getX().sumAll + a.getY().sumAll + a.getZ().sumAll + a.getW().sumAll
	}

	private val vtemp1: MutableFloat32x4 = MutableFloat32x4.create()

	private val vtemp2: MutableFloat32x4 = MutableFloat32x4.create()

	fun _setToMul44(
		dst: MutableMatrixFloat32x4x4,
		a: MutableMatrixFloat32x4x4,
		b: MutableMatrixFloat32x4x4
	): MutableMatrixFloat32x4x4 {
		val a0: MutableFloat32x4 = a.getX()
		val b0: MutableFloat32x4 = b.getX()
		val a1: MutableFloat32x4 = a.getY()
		val b1: MutableFloat32x4 = b.getY()
		val a2: MutableFloat32x4 = a.getZ()
		val b2: MutableFloat32x4 = b.getZ()
		val a3: MutableFloat32x4 = a.getW()
		val b3: MutableFloat32x4 = b.getW()


		//R[0] = add(mul(xxxx(b0), a0), mul(yyyy(b0), a1), mul(zzzz(b0), a2), mul(wwww(b0), a3));
		//R[1] = add(mul(xxxx(b1), a0), mul(yyyy(b1), a1), mul(zzzz(b1), a2), mul(wwww(b1), a3));
		//R[2] = add(mul(xxxx(b2), a0), mul(yyyy(b2), a1), mul(zzzz(b2), a2), mul(wwww(b2), a3));
		//R[3] = add(mul(xxxx(b3), a0), mul(yyyy(b3), a1), mul(zzzz(b3), a2), mul(wwww(b3), a3));
		val vt1: MutableFloat32x4 = vtemp1
		val vt2: MutableFloat32x4 = vtemp2

		// X
		run {
			vt2.setToZero()
			vt1.setToXXXX(b0)
			vt2.setToAddMul(vt2, vt1, a0)
			vt1.setToYYYY(b0)
			vt2.setToAddMul(vt2, vt1, a1)
			vt1.setToZZZZ(b0)
			vt2.setToAddMul(vt2, vt1, a2)
			vt1.setToWWWW(b0)
			vt2.setToAddMul(vt2, vt1, a3)
			dst.setX(vt2)
		}

		// Y
		run {
			vt2.setToZero()
			vt1.setToXXXX(b1)
			vt2.setToAddMul(vt2, vt1, a0)
			vt1.setToYYYY(b1)
			vt2.setToAddMul(vt2, vt1, a1)
			vt1.setToZZZZ(b1)
			vt2.setToAddMul(vt2, vt1, a2)
			vt1.setToWWWW(b1)
			vt2.setToAddMul(vt2, vt1, a3)
			dst.setY(vt2)
		}

		// Z
		run {
			vt2.setToZero()
			vt1.setToXXXX(b2)
			vt2.setToAddMul(vt2, vt1, a0)
			vt1.setToYYYY(b2)
			vt2.setToAddMul(vt2, vt1, a1)
			vt1.setToZZZZ(b2)
			vt2.setToAddMul(vt2, vt1, a2)
			vt1.setToWWWW(b2)
			vt2.setToAddMul(vt2, vt1, a3)
			dst.setZ(vt2)
		}

		// W
		run {
			vt2.setToZero()
			vt1.setToXXXX(b3)
			vt2.setToAddMul(vt2, vt1, a0)
			vt1.setToYYYY(b3)
			vt2.setToAddMul(vt2, vt1, a1)
			vt1.setToZZZZ(b3)
			vt2.setToAddMul(vt2, vt1, a2)
			vt1.setToWWWW(b3)
			vt2.setToAddMul(vt2, vt1, a3)
			dst.setW(vt2)
		}

		// Unrolled!
		//for (int n = 0; n < 4; n++) {
		//	vt2.setToZero();
		//	MutableFloat32x4 b0 = b.getRow(n);
		//	vt1.setToXXXX(b0); vt2.setToAddMul(vt2, vt1, a0);
		//	vt1.setToYYYY(b0); vt2.setToAddMul(vt2, vt1, a1);
		//	vt1.setToZZZZ(b0); vt2.setToAddMul(vt2, vt1, a2);
		//	vt1.setToWWWW(b0); vt2.setToAddMul(vt2, vt1, a3);
		//	dst.setRow(vt2, n);
		//}
		return dst
	}
}