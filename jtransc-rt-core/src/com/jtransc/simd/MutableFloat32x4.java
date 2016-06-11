package com.jtransc.simd;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;

public class MutableFloat32x4 {
	static {
		Simd.ref();
	}

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
	public void setTo(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.neg(p0.simd);")
	public void setToNeg(MutableFloat32x4 l) {
		setTo(-l.x, -l.y, -l.z, -l.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.abs(p0.simd);")
	public void setToAbs(MutableFloat32x4 l) {
		setTo(Math.abs(l.x), Math.abs(l.y), Math.abs(l.z), Math.abs(l.w));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.mul(p0.simd, p1.simd);")
	public void setToMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.mul(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	public void setToMul(MutableFloat32x4 l, float r) {
		setTo(l.x * r, l.y * r, l.z * r, l.w * r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.div(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	public void setToDiv(MutableFloat32x4 l, float r) {
		setTo(l.x / r, l.y / r, l.z / r, l.w / r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.add(p0.simd, p1.simd);")
	public void setToAdd(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.sub(p0.simd, p1.simd);")
	public void setToSub(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x - r.x, l.y - r.y, l.z - r.z, l.w - r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.max(p0.simd, p1.simd);")
	public void setToMax(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.max(l.x, r.x), Math.max(l.y, r.y), Math.max(l.z, r.z), Math.max(l.w, r.w));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.min(p0.simd, p1.simd);")
	public void setToMin(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.min(l.x, r.x), Math.min(l.y, r.y), Math.min(l.z, r.z), Math.min(l.w, r.w));
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 0);")
	public float getX() {
		return this.x;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 1);")
	public float getY() {
		return this.y;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 2);")
	public float getZ() {
		return this.z;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 3);")
	public float getW() {
		return this.w;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, p0);")
	public float getLane(int index) {
		switch (index) {
			default:
			case 0: return x;
			case 1: return y;
			case 2: return z;
			case 3: return w;
		}
	}

	@Override
	public String toString() {
		return "Simd.MutableFloat32x4(" + getLane(0) + ", " + getLane(1) + ", " + getLane(2) + ", " + getLane(3) + ")";
	}
}
