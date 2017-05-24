package com.jtransc.simd;

import com.jtransc.annotation.*;

/**
 * IMMUTABLE FLOAT32 x 4
 */
@JTranscInvisible
@JTranscNativeNameList({
	//@JTranscNativeName(target = "js", value = "SIMD.Float32x4"),
	@JTranscNativeName(target = "dart", value = "Float32x4"),
	@JTranscNativeName(target = "cpp", value = "Float32x4", defaultValue = "Float32x4_i(0, 0, 0, 0)"),
})
final public class Float32x4 {
	static {
		Simd.ref();
	}

	private final float x;
	private final float y;
	private final float z;
	private final float w;

	private Float32x4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4(+0, +0, +0, +0);"),
	})
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Float32x4(0, 0, 0, 0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Float32x4_i(0, 0, 0, 0)"),
	})
	static public Float32x4 create() {
		return new Float32x4(0f, 0f, 0f, 0f);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4(+p0, +p1, +p2, +p3);"),
	})
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Float32x4(#0, #1, #2, #3)"),
		@JTranscCallSiteBody(target = "cpp", value = "Float32x4_i(#0, #1, #2, #3)"),
	})
	static public Float32x4 create(float x, float y, float z, float w) {
		return new Float32x4(x, y, z, w);
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.add(p0, p1);"),
	})
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0) + (#1))"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0) + (#1))"),
	})
	static public Float32x4 add(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w);
	}

	//@JTranscCallSiteBody(target = "dart", value = "((#0) + (#1) + (#2) + (#3))")
	static public Float32x4 add(Float32x4 a, Float32x4 b, Float32x4 c, Float32x4 d) {
		return add(add(a, b), add(c, d));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.mul(p0, p1);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0) * (#1))"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0) * (#1))"),
	})
	static public Float32x4 mul(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.mul(p0, SIMD.Float32x4(p1, p1, p1, p1));")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0) * (#1))"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0) * (#1))"),
	})
	static public Float32x4 mul(Float32x4 l, float r) {
		return new Float32x4(l.x * r, l.y * r, l.z * r, l.w * r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(p0, 0);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0).x)"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0).x)"),
	})
	static public float getX(Float32x4 l) {
		return l.x;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(p0, 1);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0).y)"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0).y)"),
	})
	static public float getY(Float32x4 l) {
		return l.y;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(p0, 2);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0).z)"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0).z)"),
	})
	static public float getZ(Float32x4 l) {
		return l.z;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(p0, 3);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "((#0).w)"),
		@JTranscCallSiteBody(target = "cpp", value = "((#0).w)"),
	})
	static public float getW(Float32x4 l) {
		return l.w;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(p0, p1);")
	@SuppressWarnings("all")
	static public float getLane(Float32x4 l, int index) {
		switch (index) {
			default:
			case 0:
				return getX(l);
			case 1:
				return getY(l);
			case 2:
				return getZ(l);
			case 3:
				return getW(l);
		}
	}

	@SuppressWarnings("all")
	static public Float32x4 xxxx(Float32x4 l) {
		return Float32x4.create(getX(l), getX(l), getX(l), getX(l));
	}

	@SuppressWarnings("all")
	static public Float32x4 yyyy(Float32x4 l) {
		return Float32x4.create(getY(l), getY(l), getY(l), getY(l));
	}

	@SuppressWarnings("all")
	static public Float32x4 zzzz(Float32x4 l) {
		return Float32x4.create(getZ(l), getZ(l), getZ(l), getZ(l));
	}

	@SuppressWarnings("all")
	static public Float32x4 wwww(Float32x4 l) {
		return Float32x4.create(getW(l), getW(l), getW(l), getW(l));
	}

	@SuppressWarnings("all")
	static public void mul44(Float32x4[] R, Float32x4[] A, Float32x4[] B) {
		Float32x4 a0 = A[0], a1 = A[1], a2 = A[2], a3 = A[3];
		Float32x4 b0 = B[0], b1 = B[1], b2 = B[2], b3 = B[3];
		R[0] = add(mul(xxxx(b0), a0), mul(yyyy(b0), a1), mul(zzzz(b0), a2), mul(wwww(b0), a3));
		R[1] = add(mul(xxxx(b1), a0), mul(yyyy(b1), a1), mul(zzzz(b1), a2), mul(wwww(b1), a3));
		R[2] = add(mul(xxxx(b2), a0), mul(yyyy(b2), a1), mul(zzzz(b2), a2), mul(wwww(b2), a3));
		R[3] = add(mul(xxxx(b3), a0), mul(yyyy(b3), a1), mul(zzzz(b3), a2), mul(wwww(b3), a3));
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "{% SMETHOD com.jtransc.simd.Float32x4Utils:toStringInternal %}(#0)"),
		@JTranscCallSiteBody(target = "cpp", value = "{% SMETHOD com.jtransc.simd.Float32x4Utils:toStringInternal %}(#0)"),
	})
	static public String toString(Float32x4 v) {
		return Float32x4Utils.toStringInternal(v);
	}
}
