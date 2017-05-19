package com.jtransc.simd;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscNativeName;

@JTranscNativeName(target = "dart", value = "Float32x4List")
final public class MutableMatrixFloat32x4x4 {
	static {
		Simd.ref();
	}

	final private MutableFloat32x4[] v = new MutableFloat32x4[]{MutableFloat32x4.create(), MutableFloat32x4.create(), MutableFloat32x4.create(), MutableFloat32x4.create()};

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
		this.v[index].setTo(v);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[0]")
	public MutableFloat32x4 getX() {
		return this.v[0];
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[1]")
	public MutableFloat32x4 getY() {
		return this.v[1];
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[2]")
	public MutableFloat32x4 getZ() {
		return this.v[2];
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[3]")
	public MutableFloat32x4 getW() {
		return this.v[3];
	}

	@JTranscCallSiteBody(target = "dart", value = "#@[#0]")
	public MutableFloat32x4 getRow(int index) {
		return v[index];
	}

	@JTranscCallSiteBody(target = "dart", value = "{% SMETHOD com.jtransc.simd.MutableMatrixFloat32x4x4Utils:_getCell %}(#@, #0, #1)")
	public float getCell(int row, int column) {
		return MutableMatrixFloat32x4x4Utils._getCell(this, row, column);
	}

	@JTranscCallSiteBody(target = "dart", value = "{% SMETHOD com.jtransc.simd.MutableMatrixFloat32x4x4Utils:_getSumAll %}(#@)")
	final public float getSumAll() {
		return MutableMatrixFloat32x4x4Utils._getSumAll(this);
	}

	@JTranscCallSiteBody(target = "dart", value = "#@ = {% SMETHOD com.jtransc.simd.MutableMatrixFloat32x4x4Utils:_setToMul44 %}(#@, #0, #1);")
	public void setToMul44(MutableMatrixFloat32x4x4 a, MutableMatrixFloat32x4x4 b) {
		MutableMatrixFloat32x4x4Utils._setToMul44(this, a, b);
	}
}
