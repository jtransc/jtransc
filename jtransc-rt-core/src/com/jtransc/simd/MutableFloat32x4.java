package com.jtransc.simd;

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;

final public class MutableFloat32x4 {
	static {
		Simd.ref();
	}

	static private final MutableFloat32x4 temp = new MutableFloat32x4();

	private float x;
	private float y;
	private float z;
	private float w;

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+0, +0, +0, +0);")
	public MutableFloat32x4() {
		setTo(0f, 0f, 0f, 0f);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	public MutableFloat32x4(float x, float y, float z, float w) {
		setTo(x, y, z, w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+p0, +p0, +p0, +p0);")
	public MutableFloat32x4(float v) {
		setTo(v, v, v, v);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	final public void setTo(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	final public void setToZero() {
		this.setTo(0f, 0f, 0f, 0f);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = p0.simd;")
	final public void setTo(MutableFloat32x4 l) {
		this.x = l.x;
		this.y = l.y;
		this.z = l.z;
		this.w = l.w;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.neg(p0.simd);")
	final public void setToNeg(MutableFloat32x4 l) {
		setTo(-l.x, -l.y, -l.z, -l.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.abs(p0.simd);")
	final public void setToAbs(MutableFloat32x4 l) {
		setTo(Math.abs(l.x), Math.abs(l.y), Math.abs(l.z), Math.abs(l.w));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.mul(p0.simd, p1.simd);")
	final public void setToMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.mul(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	final public void setToMul(MutableFloat32x4 l, float r) {
		setTo(l.x * r, l.y * r, l.z * r, l.w * r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.div(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	final public void setToDiv(MutableFloat32x4 l, float r) {
		setTo(l.x / r, l.y / r, l.z / r, l.w / r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.add(p0.simd, p1.simd);")
	final public void setToAdd(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.sub(p0.simd, p1.simd);")
	final public void setToSub(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x - r.x, l.y - r.y, l.z - r.z, l.w - r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.max(p0.simd, p1.simd);")
	final public void setToMax(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.max(l.x, r.x), Math.max(l.y, r.y), Math.max(l.z, r.z), Math.max(l.w, r.w));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.min(p0.simd, p1.simd);")
	final public void setToMin(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.min(l.x, r.x), Math.min(l.y, r.y), Math.min(l.z, r.z), Math.min(l.w, r.w));
	}

	final public float getSumAll() {
		return this.getX() + this.getY() + this.getZ() + this.getW();
	}

	static public float getAddMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		temp.setToMul(l, r);
		return temp.getSumAll();
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 0, 0, 0, 0);")
	static public void setToXXXX(MutableFloat32x4 l) {
		l.setTo(l.x, l.x, l.x, l.x);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 1, 1, 1, 1);")
	static public void setToYYYY(MutableFloat32x4 l) {
		l.setTo(l.y, l.y, l.y, l.y);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 2, 2, 2, 2);")
	static public void setToZZZZ(MutableFloat32x4 l) {
		l.setTo(l.z, l.z, l.z, l.z);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 3, 3, 3, 3);")
	static public void setToWWWW(MutableFloat32x4 l) {
		l.setTo(l.w, l.w, l.w, l.w);
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 0);")
	final public float getX() {
		return this.x;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 1);")
	final public float getY() {
		return this.y;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 2);")
	final public float getZ() {
		return this.z;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 3);")
	final public float getW() {
		return this.w;
	}

	public void setToMultiply(MutableFloat32x4 vector, MutableMatrixFloat32x4x4 matrix) {
		this.setTo(
			MutableFloat32x4.getAddMul(vector, matrix.getX()),
			MutableFloat32x4.getAddMul(vector, matrix.getY()),
			MutableFloat32x4.getAddMul(vector, matrix.getZ()),
			MutableFloat32x4.getAddMul(vector, matrix.getW())
		);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, p0);")
	final public float getLane(int index) {
		switch (index) {
			default:
			case 0:
				return x;
			case 1:
				return y;
			case 2:
				return z;
			case 3:
				return w;
		}
	}

	@Override
	final public String toString() {
		return "Simd.MutableFloat32x4(" + getLane(0) + ", " + getLane(1) + ", " + getLane(2) + ", " + getLane(3) + ")";
	}
}
