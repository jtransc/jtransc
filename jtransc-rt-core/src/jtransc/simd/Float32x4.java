package jtransc.simd;

/**
 * IMMUTABLE FLOAT32 x 4
 */
public class Float32x4 {
	public final float a;
	public final float b;
	public final float c;
	public final float d;

	public Float32x4(float a, float b, float c, float d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	static public Float32x4 add(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.a + r.a, l.b + r.b, l.c + r.c, l.d + r.d);
	}

	static public Float32x4 mul(Float32x4 l, Float32x4 r) {
		return new Float32x4(l.a * r.a, l.b * r.b, l.c * r.c, l.d * r.d);
	}

	static public Float32x4 mul(Float32x4 l, float r) {
		return new Float32x4(l.a * r, l.b * r, l.c * r, l.d * r);
	}
}
