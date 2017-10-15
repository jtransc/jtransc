package com.jtransc.simd;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscSync;

@JTranscInvisible
final public class MutableMatrixFloat32x4x4Utils {
	@JTranscSync
	static public float _getCell(MutableMatrixFloat32x4x4 src, int row, int column) {
		return src.getRow(row).getLane(column);
	}

	@JTranscSync
	final static public float _getSumAll(MutableMatrixFloat32x4x4 a) {
		return a.getX().getSumAll() + a.getY().getSumAll() + a.getZ().getSumAll() + a.getW().getSumAll();
	}

	@JTranscInvisible
	static private MutableFloat32x4 vtemp1 = MutableFloat32x4.create();
	@JTranscInvisible
	static private MutableFloat32x4 vtemp2 = MutableFloat32x4.create();

	@JTranscSync
	static public MutableMatrixFloat32x4x4 _setToMul44(MutableMatrixFloat32x4x4 dst, MutableMatrixFloat32x4x4 a, MutableMatrixFloat32x4x4 b) {
		final MutableFloat32x4 a0 = a.getX(), b0 = b.getX();
		final MutableFloat32x4 a1 = a.getY(), b1 = b.getY();
		final MutableFloat32x4 a2 = a.getZ(), b2 = b.getZ();
		final MutableFloat32x4 a3 = a.getW(), b3 = b.getW();


		//R[0] = add(mul(xxxx(b0), a0), mul(yyyy(b0), a1), mul(zzzz(b0), a2), mul(wwww(b0), a3));
		//R[1] = add(mul(xxxx(b1), a0), mul(yyyy(b1), a1), mul(zzzz(b1), a2), mul(wwww(b1), a3));
		//R[2] = add(mul(xxxx(b2), a0), mul(yyyy(b2), a1), mul(zzzz(b2), a2), mul(wwww(b2), a3));
		//R[3] = add(mul(xxxx(b3), a0), mul(yyyy(b3), a1), mul(zzzz(b3), a2), mul(wwww(b3), a3));

		final MutableFloat32x4 vt1 = vtemp1;
		final MutableFloat32x4 vt2 = vtemp2;

		// X
		{
			vt2.setToZero();

			vt1.setToXXXX(b0);
			vt2.setToAddMul(vt2, vt1, a0);

			vt1.setToYYYY(b0);
			vt2.setToAddMul(vt2, vt1, a1);

			vt1.setToZZZZ(b0);
			vt2.setToAddMul(vt2, vt1, a2);

			vt1.setToWWWW(b0);
			vt2.setToAddMul(vt2, vt1, a3);

			dst.setX(vt2);
		}

		// Y
		{
			vt2.setToZero();

			vt1.setToXXXX(b1);
			vt2.setToAddMul(vt2, vt1, a0);

			vt1.setToYYYY(b1);
			vt2.setToAddMul(vt2, vt1, a1);

			vt1.setToZZZZ(b1);
			vt2.setToAddMul(vt2, vt1, a2);

			vt1.setToWWWW(b1);
			vt2.setToAddMul(vt2, vt1, a3);

			dst.setY(vt2);
		}

		// Z
		{
			vt2.setToZero();

			vt1.setToXXXX(b2);
			vt2.setToAddMul(vt2, vt1, a0);

			vt1.setToYYYY(b2);
			vt2.setToAddMul(vt2, vt1, a1);

			vt1.setToZZZZ(b2);
			vt2.setToAddMul(vt2, vt1, a2);

			vt1.setToWWWW(b2);
			vt2.setToAddMul(vt2, vt1, a3);

			dst.setZ(vt2);
		}

		// W
		{
			vt2.setToZero();

			vt1.setToXXXX(b3);
			vt2.setToAddMul(vt2, vt1, a0);

			vt1.setToYYYY(b3);
			vt2.setToAddMul(vt2, vt1, a1);

			vt1.setToZZZZ(b3);
			vt2.setToAddMul(vt2, vt1, a2);

			vt1.setToWWWW(b3);
			vt2.setToAddMul(vt2, vt1, a3);

			dst.setW(vt2);
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

		return dst;
	}
}
