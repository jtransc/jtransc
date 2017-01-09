package com.jtransc.target.js;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscLiteralParam;
import com.jtransc.annotation.JTranscUnboxParam;

final public class JsDynamic {
	@JTranscCallSiteBody(target = "js", value = "#@[#'0]")
	native public JsDynamic get(@JTranscLiteralParam String name);

	@JTranscCallSiteBody(target = "js", value = "#@(#0)")
	native public JsDynamic call(@JTranscUnboxParam Object p0);

	@JTranscCallSiteBody(target = "js", value = "#@(#0, #1)")
	native public JsDynamic call(@JTranscUnboxParam Object p0, @JTranscUnboxParam Object p1);

	@JTranscCallSiteBody(target = "js", value = "#@(#0, #1, #2)")
	native public JsDynamic call(@JTranscUnboxParam Object p0, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2);

	@JTranscCallSiteBody(target = "js", value = "#@(#0, #1, #2, #3)")
	native public JsDynamic call(@JTranscUnboxParam Object p0, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2, @JTranscUnboxParam Object p3);

	@JTranscCallSiteBody(target = "js", value = "((#@)|0)")
	native public int toInt();

	@JTranscCallSiteBody(target = "js", value = "_global[#'0]")
	static native public JsDynamic global(@JTranscLiteralParam String name);
}
