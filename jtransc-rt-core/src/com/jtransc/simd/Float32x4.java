package com.jtransc.simd;

import com.jtransc.annotation.JTranscMethodBody;

/**
 * IMMUTABLE FLOAT32 x 4
 */
public class Float32x4 {
	static {
		Simd.ref();
	}

	public final float v[] = new float[4];

	private Float32x4(float x, float y, float z, float w) {
		this.v[0] = x;
		this.v[1] = y;
		this.v[2] = z;
		this.v[3] = w;
	}

	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	static public Float32x4 create(float x, float y, float z, float w) {
		return new Float32x4(x, y, z, w);
	}

	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.add(p0, p1);")
	static public Float32x4 add(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.v[0] + r.v[0], l.v[1] + r.v[1], l.v[2] + r.v[2], l.v[3] + r.v[3]);
	}

	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.mul(p0, p1);")
	static public Float32x4 mul(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.v[0] * r.v[0], l.v[1] * r.v[1], l.v[2] * r.v[2], l.v[3] * r.v[3]);
	}

	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.mul(p0, SIMD.Float32x4(p1, p1, p1, p1));")
	static public Float32x4 mul(Float32x4 l, float r) {
		return new Float32x4(l.v[0] * r, l.v[1] * r, l.v[2] * r, l.v[3] * r);
	}

	@JTranscMethodBody(target = "js", value = "return SIMD.Float32x4.extractLane(p0, p1);")
	static public float getLane(Float32x4 l, int index) {
		return l.v[index];
	}

	static public String toString(Float32x4 v) {
		return "Simd.Float32x4(" + getLane(v, 0) + ", " + getLane(v, 1) + ", " + getLane(v, 2) + ", " + getLane(v, 3) + ")";
	}
}
