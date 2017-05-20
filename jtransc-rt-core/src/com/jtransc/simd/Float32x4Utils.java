package com.jtransc.simd;

import com.jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public class Float32x4Utils {
	static public String toStringInternal(Float32x4 v) {
		return "Simd.Float32x4(" + Float32x4.getX(v) + ", " + Float32x4.getY(v) + ", " + Float32x4.getZ(v) + ", " + Float32x4.getW(v) + ")";
	}
}
