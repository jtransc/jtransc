package com.jtransc.simd;

import com.jtransc.annotation.*;

@JTranscInvisible
@JTranscAddFile(target = "js", priority = -2000, prepend = "js/SimdPolyfill.js")
final public class Simd {
	@JTranscSync
	static public void ref() {
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return true;"),
		@JTranscMethodBody(target = "dart", value = "return true;"),
	})
	@JTranscSync
	static public boolean supported() {
		return false;
	}
}
