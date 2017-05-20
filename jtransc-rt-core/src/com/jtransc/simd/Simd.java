package com.jtransc.simd;

import com.jtransc.annotation.JTranscAddFile;
import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscMethodBodyList;

@JTranscInvisible
@JTranscAddFile(target = "js", priority = -2000, prepend = "js/SimdPolyfill.js")
final public class Simd {
	static public void ref() {
	}

	@JTranscMethodBodyList({
		@JTranscMethodBody(target = "js", cond = "hasSIMD", value = "return true;"),
		@JTranscMethodBody(target = "dart", value = "return true;"),
	})
	static public boolean supported() {
		return false;
	}
}
