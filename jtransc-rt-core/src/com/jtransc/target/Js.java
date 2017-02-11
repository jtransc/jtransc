package com.jtransc.target;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscInvisible;
import com.jtransc.annotation.JTranscLiteralParam;

@JTranscInvisible
public class Js {
	@JTranscInvisible
	@JTranscCallSiteBody(target = "js", value = "#0")
	native static public void v_raw(@JTranscLiteralParam String raw);

	@JTranscInvisible
	@JTranscCallSiteBody(target = "js", value = "#0")
	native static public boolean z_raw(@JTranscLiteralParam String raw);

	@JTranscInvisible
	@JTranscCallSiteBody(target = "js", value = "#0")
	native static public int i_raw(@JTranscLiteralParam String raw);

	@JTranscInvisible
	@JTranscCallSiteBody(target = "js", value = "#0")
	native static public double d_raw(@JTranscLiteralParam String raw);

	@JTranscInvisible
	@JTranscCallSiteBody(target = "js", value = "#0")
	native static public String s_raw(@JTranscLiteralParam String raw);

	@JTranscInvisible
	@JTranscCallSiteBody(target = "js", value = "#0")
	native static public Object o_raw(@JTranscLiteralParam String raw);
}
