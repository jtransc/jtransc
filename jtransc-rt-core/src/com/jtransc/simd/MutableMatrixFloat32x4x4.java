package com.jtransc.simd;

final public class MutableMatrixFloat32x4x4 {
	static {
		Simd.ref();
	}

	final private MutableFloat32x4 x = new MutableFloat32x4();
	final private MutableFloat32x4 y = new MutableFloat32x4();
	final private MutableFloat32x4 z = new MutableFloat32x4();
	final private MutableFloat32x4 w = new MutableFloat32x4();

	public MutableMatrixFloat32x4x4 setTo(
		float m00, float m01, float m02, float m03,
		float m10, float m11, float m12, float m13,
		float m20, float m21, float m22, float m23,
		float m30, float m31, float m32, float m33
	) {
		this.x.setTo(m00, m01, m02, m03);
		this.y.setTo(m10, m11, m12, m13);
		this.z.setTo(m20, m21, m22, m23);
		this.w.setTo(m30, m31, m32, m33);

		return this;
	}

	public void setTo(MutableFloat32x4 x, MutableFloat32x4 y, MutableFloat32x4 z, MutableFloat32x4 w) {
		this.x.setTo(x);
		this.y.setTo(y);
		this.z.setTo(z);
		this.w.setTo(w);
	}

	public void setX(MutableFloat32x4 x) {
		this.x.setTo(x);
	}

	public void setY(MutableFloat32x4 y) {
		this.y.setTo(y);
	}

	public void setZ(MutableFloat32x4 z) {
		this.z.setTo(z);
	}

	public void setW(MutableFloat32x4 w) {
		this.w.setTo(w);
	}

	public void setRow(MutableFloat32x4 v, int index) {
		switch (index) {
			case 0:
				this.x.setTo(v);
				break;
			case 1:
				this.y.setTo(v);
				break;
			case 2:
				this.z.setTo(v);
				break;
			case 3:
				this.w.setTo(v);
				break;
		}
	}


	public MutableFloat32x4 getX() {
		return this.x;
	}

	public MutableFloat32x4 getY() {
		return this.y;
	}

	public MutableFloat32x4 getZ() {
		return this.z;
	}

	public MutableFloat32x4 getW() {
		return this.w;
	}

	public MutableFloat32x4 getRow(int index) {
		switch (index) {
			default:
			case 0: return this.x;
			case 1: return this.x;
			case 2: return this.x;
			case 3: return this.x;
		}
	}

	public float getCell(int row, int column) {
		return getRow(row).getLane(column);
	}

	final public float getSumAll() {
		return this.getX().getSumAll() + this.getY().getSumAll() + this.getZ().getSumAll() + this.getW().getSumAll();
	}

	static private MutableFloat32x4 vtemp1 = new MutableFloat32x4();
	static private MutableFloat32x4 vtemp2 = new MutableFloat32x4();

	public void setToMul44(MutableMatrixFloat32x4x4 a, MutableMatrixFloat32x4x4 b) {
		MutableFloat32x4 a0 = a.x, a1 = a.y, a2 = a.z, a3 = a.w;

		//R[0] = add(mul(xxxx(b0), a0), mul(yyyy(b0), a1), mul(zzzz(b0), a2), mul(wwww(b0), a3));
		//R[1] = add(mul(xxxx(b1), a0), mul(yyyy(b1), a1), mul(zzzz(b1), a2), mul(wwww(b1), a3));
		//R[2] = add(mul(xxxx(b2), a0), mul(yyyy(b2), a1), mul(zzzz(b2), a2), mul(wwww(b2), a3));
		//R[3] = add(mul(xxxx(b3), a0), mul(yyyy(b3), a1), mul(zzzz(b3), a2), mul(wwww(b3), a3));
		for (int n = 0; n < 4; n++) {
			vtemp2.setToZero();

			MutableFloat32x4 b0 = b.getRow(n);

			vtemp1.setToXXXX(b0);
			vtemp1.setToMul(vtemp1, a0);
			vtemp2.setToAdd(vtemp2, vtemp1);

			vtemp1.setToYYYY(b0);
			vtemp1.setToMul(vtemp1, a1);
			vtemp2.setToAdd(vtemp2, vtemp1);

			vtemp1.setToZZZZ(b0);
			vtemp1.setToMul(vtemp1, a2);
			vtemp2.setToAdd(vtemp2, vtemp1);

			vtemp1.setToWWWW(b0);
			vtemp1.setToMul(vtemp1, a3);
			vtemp2.setToAdd(vtemp2, vtemp1);

			this.setRow(vtemp2, n);
		}
	}
}
