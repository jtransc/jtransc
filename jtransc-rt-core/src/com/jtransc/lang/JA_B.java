package com.jtransc.lang;

import com.jtransc.annotation.JTranscMethodBody;

public class JA_B extends JA_0 {
	@JTranscMethodBody(target = "js", value = "this.data = new Int8Array(p0);")
	public JA_B(int size) {
	}

	@JTranscMethodBody(target = "js", value = "return this.data.length;")
	@Override
	native public int length();

	@JTranscMethodBody(target = "js", value = "this.data[p0] = p1;")
	native public void set(int offset, byte value);

	@JTranscMethodBody(target = "js", value = "return this.data[p0];")
	native public byte get(int offset);
}
