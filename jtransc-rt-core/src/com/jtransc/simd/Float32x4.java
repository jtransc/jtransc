package com.jtransc.simd;

import com.jtransc.annotation.JTranscMethodBody;

/**
 * IMMUTABLE FLOAT32 x 4
 */
public class Float32x4 {
	static {
		Simd.ref();
	}

	public final float x;
	public final float y;
	public final float z;
	public final float w;

	private Float32x4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	static public Float32x4 create(float x, float y, float z, float w) {
		return new Float32x4(x, y, z, w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.add(p0, p1);")
	static public Float32x4 add(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.mul(p0, p1);")
	static public Float32x4 mul(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.mul(p0, SIMD.Float32x4(p1, p1, p1, p1));")
	static public Float32x4 mul(Float32x4 l, float r) {
		return new Float32x4(l.x * r, l.y * r, l.z * r, l.w * r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(p0, p1);")
	static public float getLane(Float32x4 l, int index) {
		switch (index) {
			default:
			case 0:	return l.x;
			case 1:	return l.y;
			case 2:	return l.z;
			case 3:	return l.w;
		}
	}

	static public String toString(Float32x4 v) {
		return "Simd.Float32x4(" + getLane(v, 0) + ", " + getLane(v, 1) + ", " + getLane(v, 2) + ", " + getLane(v, 3) + ")";
	}
}
