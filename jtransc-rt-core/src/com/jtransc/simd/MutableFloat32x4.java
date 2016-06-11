package com.jtransc.simd;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;

public class MutableFloat32x4 {
	static {
		Simd.ref();
	}

	private float[] v = new float[4];

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4(+0, +0, +0, +0);")
	public MutableFloat32x4() {
		setTo(0f, 0f, 0f, 0f);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	public MutableFloat32x4(float x, float y, float z, float w) {
		setTo(x, y, z, w);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4(+p0, +p0, +p0, +p0);")
	public MutableFloat32x4(float v) {
		setTo(v, v, v, v);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	public void setTo(float x, float y, float z, float w) {
		this.v[0] = x;
		this.v[1] = y;
		this.v[2] = z;
		this.v[3] = w;
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.neg(p0.simd);")
	public void setToNeg(MutableFloat32x4 l) {
		setTo(-l.v[0], -l.v[1], -l.v[2], -l.v[3]);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.abs(p0.simd);")
	public void setToAbs(MutableFloat32x4 l) {
		setTo(Math.abs(l.v[0]), Math.abs(l.v[1]), Math.abs(l.v[2]), Math.abs(l.v[3]));
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.mul(p0.simd, p1.simd);")
	public void setToMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.v[0] * r.v[0], l.v[1] * r.v[1], l.v[2] * r.v[2], l.v[3] * r.v[3]);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.mul(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	public void setToMul(MutableFloat32x4 l, float r) {
		setTo(l.v[0] * r, l.v[1] * r, l.v[2] * r, l.v[3] * r);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.div(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	public void setToDiv(MutableFloat32x4 l, float r) {
		setTo(l.v[0] / r, l.v[1] / r, l.v[2] / r, l.v[3] / r);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.add(p0.simd, p1.simd);")
	public void setToAdd(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.v[0] + r.v[0], l.v[1] + r.v[1], l.v[2] + r.v[2], l.v[3] + r.v[3]);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.sub(p0.simd, p1.simd);")
	public void setToSub(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.v[0] - r.v[0], l.v[1] - r.v[1], l.v[2] - r.v[2], l.v[3] - r.v[3]);
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.max(p0.simd, p1.simd);")
	public void setToMax(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.max(l.v[0], r.v[0]), Math.max(l.v[1], r.v[1]), Math.max(l.v[2], r.v[2]), Math.max(l.v[3], r.v[3]));
	}

	@JTranscMethodBody(target = "js", value = "this.simd = SIMD.Float32x4.min(p0.simd, p1.simd);")
	public void setToMin(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.min(l.v[0], r.v[0]), Math.min(l.v[1], r.v[1]), Math.min(l.v[2], r.v[2]), Math.min(l.v[3], r.v[3]));
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.extractLane(this.simd, 0);")
	public float getX() {
		return this.v[0];
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.extractLane(this.simd, 1);")
	public float getY() {
		return this.v[1];
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.extractLane(this.simd, 2);")
	public float getZ() {
		return this.v[2];
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.extractLane(this.simd, 3);")
	public float getW() {
		return this.v[3];
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.extractLane(this.simd, p0);")
	public float getLane(int index) {
		return this.v[index];
	}

	@Override
	public String toString() {
		return "Simd.MutableFloat32x4(" + getLane(0) + ", " + getLane(1) + ", " + getLane(2) + ", " + getLane(3) + ")";
	}
}
