package com.jtransc.simd;

import com.jtransc.annotation.JTranscCallSiteBody;

final public class MutableMatrixFloat32x4x4 {
	static {
		Simd.ref();
	}

	final private MutableFloat32x4 x = MutableFloat32x4.create();
	final private MutableFloat32x4 y = MutableFloat32x4.create();
	final private MutableFloat32x4 z = MutableFloat32x4.create();
	final private MutableFloat32x4 w = MutableFloat32x4.create();

	private MutableMatrixFloat32x4x4() {
	}

	@JTranscCallSiteBody(target = "dart", value = "new Float32x4List(4)")
	static public MutableMatrixFloat32x4x4 create() {
		return new MutableMatrixFloat32x4x4();
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[0] = new Float32x4(#0, #1, #2, #3); #@[1] = new Float32x4(#4, #5, #6, #7); #@[2] = new Float32x4(#8, #9, #10, #11); #@[3] = new Float32x4(#12, #13, #14, #15);")
	public void setTo(
		float m00, float m01, float m02, float m03,
		float m10, float m11, float m12, float m13,
		float m20, float m21, float m22, float m23,
		float m30, float m31, float m32, float m33
	) {
		this.getX().setTo(m00, m01, m02, m03);
		this.getY().setTo(m10, m11, m12, m13);
		this.getZ().setTo(m20, m21, m22, m23);
		this.getW().setTo(m30, m31, m32, m33);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[0] = p0; #@[1] = p1; #@[2] = p2; #@[3] = p3;")
	public void setTo(MutableFloat32x4 x, MutableFloat32x4 y, MutableFloat32x4 z, MutableFloat32x4 w) {
		this.getX().setTo(x);
		this.getY().setTo(y);
		this.getZ().setTo(z);
		this.getW().setTo(w);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[0] = #0;")
	public void setX(MutableFloat32x4 x) {
		this.getX().setTo(x);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[1] = #0;")
	public void setY(MutableFloat32x4 y) {
		this.getY().setTo(y);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[2] = #0;")
	public void setZ(MutableFloat32x4 z) {
		this.getZ().setTo(z);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[3] = #0;")
	public void setW(MutableFloat32x4 w) {
		this.getW().setTo(w);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[#0] = #1;")
	public void setRow(int index, MutableFloat32x4 v) {
		switch (index) {
			case 0:
				this.setX(v);
				break;
			case 1:
				this.setY(v);
				break;
			case 2:
				this.setZ(v);
				break;
			case 3:
				this.setW(v);
				break;
			default:
				this.setX(v);
				break;
		}
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[0]")
	public MutableFloat32x4 getX() {
		return this.x;
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[1]")
	public MutableFloat32x4 getY() {
		return this.y;
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[2]")
	public MutableFloat32x4 getZ() {
		return this.z;
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[3]")
	public MutableFloat32x4 getW() {
		return this.w;
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[#0]")
	public MutableFloat32x4 getRow(int index) {
		switch (index) {
			case 0:
				return getX();
			case 1:
				return getY();
			case 2:
				return getZ();
			case 3:
				return getW();
		}
		return getX();
	}

	@JTranscCallSiteBody(target = "dart", value = "{% SMETHOD com.jtransc.simd.MutableMatrixFloat32x4x4:_getCell %}(#@, #0, #1)")
	public float getCell(int row, int column) {
		return _getCell(this, row, column);
	}

	static public float _getCell(MutableMatrixFloat32x4x4 src, int row, int column) {
		return src.getRow(row).getLane(column);
	}

	@JTranscCallSiteBody(target = "dart", value = "{% SMETHOD com.jtransc.simd.MutableMatrixFloat32x4x4:_getSumAll %}(#@)")
	final public float getSumAll() {
		return _getSumAll(this);
	}

	final static public float _getSumAll(MutableMatrixFloat32x4x4 a) {
		return a.getX().getSumAll() + a.getY().getSumAll() + a.getZ().getSumAll() + a.getW().getSumAll();
	}

	static private MutableFloat32x4 vtemp1 = MutableFloat32x4.create();
	static private MutableFloat32x4 vtemp2 = MutableFloat32x4.create();

	@JTranscCallSiteBody(target = "dart", value = "#@ = {% SMETHOD com.jtransc.simd.MutableMatrixFloat32x4x4:_setToMul44 %}(#@, #0, #1);")
	public void setToMul44(MutableMatrixFloat32x4x4 a, MutableMatrixFloat32x4x4 b) {
		_setToMul44(this, a, b);
	}

	static public MutableMatrixFloat32x4x4 _setToMul44(MutableMatrixFloat32x4x4 dst, MutableMatrixFloat32x4x4 a, MutableMatrixFloat32x4x4 b) {
		MutableFloat32x4 a0 = a.getX();
		MutableFloat32x4 a1 = a.getY();
		MutableFloat32x4 a2 = a.getZ();
		MutableFloat32x4 a3 = a.getW();

		//R[0] = add(mul(xxxx(b0), a0), mul(yyyy(b0), a1), mul(zzzz(b0), a2), mul(wwww(b0), a3));
		//R[1] = add(mul(xxxx(b1), a0), mul(yyyy(b1), a1), mul(zzzz(b1), a2), mul(wwww(b1), a3));
		//R[2] = add(mul(xxxx(b2), a0), mul(yyyy(b2), a1), mul(zzzz(b2), a2), mul(wwww(b2), a3));
		//R[3] = add(mul(xxxx(b3), a0), mul(yyyy(b3), a1), mul(zzzz(b3), a2), mul(wwww(b3), a3));

		MutableFloat32x4 vt1 = vtemp1;
		MutableFloat32x4 vt2 = vtemp2;

		// X
		{
			vt2.setToZero();

			MutableFloat32x4 b0 = b.getX();

			vt1.setToXXXX(b0);
			vt1.setToMul(vt1, a0);
			vt2.setToAdd(vt2, vt1);

			vt1.setToYYYY(b0);
			vt1.setToMul(vt1, a1);
			vt2.setToAdd(vt2, vt1);

			vt1.setToZZZZ(b0);
			vt1.setToMul(vt1, a2);
			vt2.setToAdd(vt2, vt1);

			vt1.setToWWWW(b0);
			vt1.setToMul(vt1, a3);
			vt2.setToAdd(vt2, vt1);

			dst.setX(vt2);
		}

		// Y
		{
			vt2.setToZero();

			MutableFloat32x4 b0 = b.getY();

			vt1.setToXXXX(b0);
			vt1.setToMul(vt1, a0);
			vt2.setToAdd(vt2, vt1);

			vt1.setToYYYY(b0);
			vt1.setToMul(vt1, a1);
			vt2.setToAdd(vt2, vt1);

			vt1.setToZZZZ(b0);
			vt1.setToMul(vt1, a2);
			vt2.setToAdd(vt2, vt1);

			vt1.setToWWWW(b0);
			vt1.setToMul(vt1, a3);
			vt2.setToAdd(vt2, vt1);

			dst.setY(vt2);
		}

		// Z
		{
			vt2.setToZero();

			MutableFloat32x4 b0 = b.getZ();

			vt1.setToXXXX(b0);
			vt1.setToMul(vt1, a0);
			vt2.setToAdd(vt2, vt1);

			vt1.setToYYYY(b0);
			vt1.setToMul(vt1, a1);
			vt2.setToAdd(vt2, vt1);

			vt1.setToZZZZ(b0);
			vt1.setToMul(vt1, a2);
			vt2.setToAdd(vt2, vt1);

			vt1.setToWWWW(b0);
			vt1.setToMul(vt1, a3);
			vt2.setToAdd(vt2, vt1);

			dst.setZ(vt2);
		}

		// W
		{
			vt2.setToZero();

			MutableFloat32x4 b0 = b.getW();

			vt1.setToXXXX(b0);
			vt1.setToMul(vt1, a0);
			vt2.setToAdd(vt2, vt1);

			vt1.setToYYYY(b0);
			vt1.setToMul(vt1, a1);
			vt2.setToAdd(vt2, vt1);

			vt1.setToZZZZ(b0);
			vt1.setToMul(vt1, a2);
			vt2.setToAdd(vt2, vt1);

			vt1.setToWWWW(b0);
			vt1.setToMul(vt1, a3);
			vt2.setToAdd(vt2, vt1);

			dst.setW(vt2);
		}

		// Unrolled!
		//for (int n = 0; n < 4; n++) {
		//	vt2.setToZero();
		//
		//	MutableFloat32x4 b0 = b.getRow(n);
		//
		//	vt1.setToXXXX(b0);
		//	vt1.setToMul(vt1, a0);
		//	vt2.setToAdd(vt2, vt1);
		//
		//	vt1.setToYYYY(b0);
		//	vt1.setToMul(vt1, a1);
		//	vt2.setToAdd(vt2, vt1);
		//
		//	vt1.setToZZZZ(b0);
		//	vt1.setToMul(vt1, a2);
		//	vt2.setToAdd(vt2, vt1);
		//
		//	vt1.setToWWWW(b0);
		//	vt1.setToMul(vt1, a3);
		//	vt2.setToAdd(vt2, vt1);
		//
		//	dst.setRow(vt2, n);
		//}

		return dst;
	}
}
