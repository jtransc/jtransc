package com.jtransc.target;

import com.jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public class Cpp {
	@JTranscInvisible
	native static public void v_raw(String raw);

	@JTranscInvisible
	native static public boolean z_raw(String raw);

	@JTranscInvisible
	native static public int i_raw(String raw);

	@JTranscInvisible
	native static public double d_raw(String raw);

	@JTranscInvisible
	native static public String s_raw(String raw);

	@JTranscInvisible
	native static public Object o_raw(String raw);
}
