package com.jtransc.simd;

import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscSync;

@JTranscInvisible
final public class MutableFloat32x4Utils {
	static private final MutableFloat32x4 temp = MutableFloat32x4.create();

	@JTranscSync
	static public float getAddMul(MutableFloat32x4 l, MutableFloat32x4 r) {
		temp.setToMul(l, r);
		return temp.getSumAll();
	}

	@JTranscSync
	static public String toStringInternal(MutableFloat32x4 v) {
		return "Simd.MutableFloat32x4(" + v.getX() + ", " + v.getY() + ", " + v.getZ() + ", " + v.getW() + ")";
	}
}
