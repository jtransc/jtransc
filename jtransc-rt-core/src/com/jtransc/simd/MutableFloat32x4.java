package com.jtransc.simd;

import com.jtransc.annotation.*;

@JTranscInvisible
@JTranscNativeNameList({
	@JTranscNativeName(target = "dart", value = "Float32x4"),
	@JTranscNativeName(target = "cpp", value = "Float32x4", defaultValue = "Float32x4_i(0, 0, 0, 0)"),
})
final public class MutableFloat32x4 {
	static {
		Simd.ref();
	}

	private float x;
	private float y;
	private float z;
	private float w;

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	@JTranscSync
	private MutableFloat32x4(float x, float y, float z, float w) {
		setTo(x, y, z, w);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Float32x4(#0, #1, #2, #3)"),
		@JTranscCallSiteBody(target = "cpp", value = "Float32x4_i(#0, #1, #2, #3)"),
	})
	@JTranscSync
	static public MutableFloat32x4 create(float x, float y, float z, float v) {
		return new MutableFloat32x4(x, y, z, v);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Float32x4(0.0, 0.0, 0.0, 0.0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Float32x4_i(0, 0, 0, 0)"),
	})
	@JTranscSync
	static public MutableFloat32x4 create() {
		return create(0f, 0f, 0f, 0f);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Float32x4(#0, #0, #0, #0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Float32x4_i(#0, #0, #0, #0)"),
	})
	@JTranscSync
	static public MutableFloat32x4 create(float v) {
		return create(v, v, v, v);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4(+p0, +p1, +p2, +p3);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Float32x4(#0, #1, #2, #3);"),
		@JTranscCallSiteBody(target = "cpp", value = "Float32x4_i(#0, #0, #0, #0)"),
	})
	@JTranscSync
	final public void setTo(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Float32x4(0.0, 0.0, 0.0, 0.0);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = Float32x4_i(0, 0, 0, 0);"),
	})
	@JTranscSync
	final public void setToZero() {
		this.setTo(0f, 0f, 0f, 0f);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = p0.simd;")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0;"),
	})
	@JTranscSync
	final public void setTo(MutableFloat32x4 l) {
		this.x = l.x;
		this.y = l.y;
		this.z = l.z;
		this.w = l.w;
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.neg(p0.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = -#0;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = -#0;"),
	})
	@JTranscSync
	final public void setToNeg(MutableFloat32x4 l) {
		setTo(-l.x, -l.y, -l.z, -l.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.abs(p0.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0.abs();"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = abs(#0);"),
	})
	@JTranscSync
	final public void setToAbs(MutableFloat32x4 l) {
		setTo(Math.abs(l.x), Math.abs(l.y), Math.abs(l.z), Math.abs(l.w));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.mul(p0.simd, p1.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0 * #1;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0 * #1;"),
	})
	@JTranscSync
	final public void setToMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x * r.x, l.y * r.y, l.z * r.z, l.w * r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.mul(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0 * #1;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0 * #1;"),
	})
	@JTranscSync
	final public void setToMul(MutableFloat32x4 l, float r) {
		setTo(l.x * r, l.y * r, l.z * r, l.w * r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.div(p0.simd, SIMD.Float32x4(p1, p1, p1, p1));")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0 / #1;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0 / #1;"),
	})
	@JTranscSync
	final public void setToDiv(MutableFloat32x4 l, float r) {
		setTo(l.x / r, l.y / r, l.z / r, l.w / r);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.add(p0.simd, p1.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0 + #1;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0 + #1;"),
	})
	@JTranscSync
	final public void setToAdd(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x + r.x, l.y + r.y, l.z + r.z, l.w + r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.add(p0.simd, SIMD.Float32x4.mul(p1.simd, p2.simd));")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0 + (#1 * #2);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0 + (#1 * #2);"),
	})
	@JTranscSync
	final public void setToAddMul(MutableFloat32x4 add, MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(
			add.x + l.x * r.x,
			add.y + l.y * r.y,
			add.z + l.z * r.z,
			add.w + l.w * r.w
		);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.sub(p0.simd, p1.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0 - #1;"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = #0 - #1;"),
	})
	@JTranscSync
	final public void setToSub(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(l.x - r.x, l.y - r.y, l.z - r.z, l.w - r.w);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.max(p0.simd, p1.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0.max(#1);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = max(#0, #1);"),
	})
	@JTranscSync
	final public void setToMax(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.max(l.x, r.x), Math.max(l.y, r.y), Math.max(l.z, r.z), Math.max(l.w, r.w));
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.min(p0.simd, p1.simd);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #0.min(#1);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = min(#0, #1);"),
	})
	@JTranscSync
	final public void setToMin(MutableFloat32x4 l, MutableFloat32x4 r) {
		setTo(Math.min(l.x, r.x), Math.min(l.y, r.y), Math.min(l.z, r.z), Math.min(l.w, r.w));
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.x+#@.y+#@.z+#@.w"),
		@JTranscCallSiteBody(target = "cpp", value = "sum(#@)"),
	})
	@JTranscSync
	final public float getSumAll() {
		return this.getX() + this.getY() + this.getZ() + this.getW();
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 0, 0, 0, 0);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Float32x4(#0.x, #0.x, #0.x, #0.x);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = Float32x4_i(#0.x, #0.x, #0.x, #0.x);"),
	})
	@JTranscSync
	public void setToXXXX(MutableFloat32x4 l) {
		l.setTo(l.x, l.x, l.x, l.x);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 1, 1, 1, 1);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Float32x4(#0.y, #0.y, #0.y, #0.y);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = Float32x4_i(#0.y, #0.y, #0.y, #0.y);"),
	})
	@JTranscSync
	public void setToYYYY(MutableFloat32x4 l) {
		l.setTo(l.y, l.y, l.y, l.y);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 2, 2, 2, 2);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Float32x4(#0.z, #0.z, #0.z, #0.z);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = Float32x4_i(#0.z, #0.z, #0.z, #0.z);"),
	})
	@JTranscSync
	public void setToZZZZ(MutableFloat32x4 l) {
		l.setTo(l.z, l.z, l.z, l.z);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "this.simd = SIMD.Float32x4.swizzle(p0.simd, 3, 3, 3, 3);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Float32x4(#0.w, #0.w, #0.w, #0.w);"),
		@JTranscCallSiteBody(target = "cpp", value = "#@ = Float32x4_i(#0.w, #0.w, #0.w, #0.w);"),
	})
	@JTranscSync
	public void setToWWWW(MutableFloat32x4 l) {
		l.setTo(l.w, l.w, l.w, l.w);
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 0);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.x"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.x"),
	})
	@JTranscSync
	final public float getX() {
		return this.x;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 1);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.y"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.y"),
	})
	@JTranscSync
	final public float getY() {
		return this.y;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 2);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.z"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.z"),
	})
	@JTranscSync
	final public float getZ() {
		return this.z;
	}

	@JTranscInline
	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, 3);")
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.w"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.w"),
	})
	@JTranscSync
	final public float getW() {
		return this.w;
	}

	@JTranscSync
	public void setToMultiply(MutableFloat32x4 vector, MutableMatrixFloat32x4x4 matrix) {
		this.setTo(
			MutableFloat32x4Utils.getAddMul(vector, matrix.getX()),
			MutableFloat32x4Utils.getAddMul(vector, matrix.getY()),
			MutableFloat32x4Utils.getAddMul(vector, matrix.getZ()),
			MutableFloat32x4Utils.getAddMul(vector, matrix.getW())
		);
	}

	@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return SIMD.Float32x4.extractLane(this.simd, p0);")
	@JTranscSync
	final public float getLane(int index) {
		switch (index) {
			default:
			case 0:
				return getX();
			case 1:
				return getY();
			case 2:
				return getZ();
			case 3:
				return getW();
		}
	}

	@Override
	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "{% SMETHOD com.jtransc.simd.MutableFloat32x4Utils:toStringInternal %}(#@)"),
		@JTranscCallSiteBody(target = "cpp", value = "{% SMETHOD com.jtransc.simd.MutableFloat32x4Utils:toStringInternal %}(#@)"),
	})
	@JTranscSync
	final public String toString() {
		return MutableFloat32x4Utils.toStringInternal(this);
	}
}
