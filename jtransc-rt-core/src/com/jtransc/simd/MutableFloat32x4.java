package com.jtransc.simd;

import com.jtransc.annotation.JTranscInline;

public class MutableFloat32x4 {
	private float a;
	private float b;
	private float c;
	private float d;

	public MutableFloat32x4() {
		setTo(0f, 0f, 0f, 0f);
	}

	public MutableFloat32x4(float a, float b, float c, float d) {
		setTo(a, b, c, d);
	}

	public MutableFloat32x4(float v) {
		setTo(v, v, v, v);
	}

	public void setTo(float a, float b, float c, float d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public void setToNeg(MutableFloat32x4 l) {
		setTo(-l.a, -l.b, -l.c, -l.d);
	}

	public void setToAbs(MutableFloat32x4 l) {
		setTo(Math.abs(l.a), Math.abs(l.b), Math.abs(l.c), Math.abs(l.d));
	}

	public void setToMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.a * r.a, l.b * r.b, l.c * r.c, l.d * r.d);
	}

	public void setToMul(MutableFloat32x4 l, float r) {
		setTo(l.a * r, l.b * r, l.c * r, l.d * r);
	}

	public void setToDiv(MutableFloat32x4 l, float r) {
		setTo(l.a / r, l.b / r, l.c / r, l.d / r);
	}

	public void setToAdd(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.a + r.a, l.b + r.b, l.c + r.c, l.d + r.d);
	}

	public void setToSub(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.a - r.a, l.b - r.b, l.c - r.c, l.d - r.d);
	}

	public void setToMax(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.max(l.a, r.a), Math.max(l.b, r.b), Math.max(l.c, r.c), Math.max(l.d, r.d));
	}

	public void setToMin(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.min(l.a, r.a), Math.min(l.b, r.b), Math.min(l.c, r.c), Math.min(l.d, r.d));
	}

	@JTranscInline
	public float getA() {
		return this.a;
	}

	@JTranscInline
	public float getB() {
		return this.b;
	}

	@JTranscInline
	public float getC() {
		return this.c;
	}

	@JTranscInline
	public float getD() {
		return this.d;
	}
}
