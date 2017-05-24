package com.jtransc.target.js;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscLiteralParam;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscUnboxParam;

final public class JsDynamic {
	@JTranscCallSiteBody(target = "js", value = "#@#.0")
	native public JsDynamic get(@JTranscLiteralParam String name);

	@JTranscCallSiteBody(target = "js", value = "#@[#0]")
	native public JsDynamic get(int index);

	@JTranscCallSiteBody(target = "js", value = "(#@)#.0 = #1;")
	native public void set(@JTranscLiteralParam String key, @JTranscUnboxParam Object value);

	@JTranscCallSiteBody(target = "js", value = "(#@)[#0] = #1;")
	native public void set(int index, @JTranscUnboxParam Object value);

	@JTranscCallSiteBody(target = "js", value = "#@#.0()")
	native public JsDynamic call(@JTranscUnboxParam String methodName);

	@JTranscCallSiteBody(target = "js", value = "#@#.0(#1)")
	native public JsDynamic call(@JTranscUnboxParam String methodName, @JTranscUnboxParam Object p1);

	@JTranscCallSiteBody(target = "js", value = "#@#.0(#1, #2)")
	native public JsDynamic call(@JTranscUnboxParam String methodName, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2);

	@JTranscCallSiteBody(target = "js", value = "#@#.0(#1, #2, #3)")
	native public JsDynamic call(@JTranscUnboxParam String methodName, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2, @JTranscUnboxParam Object p3);

	@JTranscCallSiteBody(target = "js", value = "#@#.0(#1, #2, #3, #4)")
	native public JsDynamic call(@JTranscUnboxParam String methodName, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2, @JTranscUnboxParam Object p3, @JTranscUnboxParam Object p4);

	@JTranscCallSiteBody(target = "js", value = "#@#.0(#1, #2, #3, #4, #5)")
	native public JsDynamic call(@JTranscUnboxParam String methodName, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2, @JTranscUnboxParam Object p3, @JTranscUnboxParam Object p4, @JTranscUnboxParam Object p5);

	@JTranscCallSiteBody(target = "js", value = "new (#@)()")
	native public JsDynamic newInstance();

	@JTranscCallSiteBody(target = "js", value = "new (#@)(#0)")
	native public JsDynamic newInstance(@JTranscUnboxParam Object p0);

	@JTranscCallSiteBody(target = "js", value = "new (#@)(#0, #1)")
	native public JsDynamic newInstance(@JTranscUnboxParam Object p0, @JTranscUnboxParam Object p1);

	@JTranscCallSiteBody(target = "js", value = "new (#@)(#0, #1, #2)")
	native public JsDynamic newInstance(@JTranscUnboxParam Object p0, @JTranscUnboxParam Object p1, @JTranscUnboxParam Object p2);

	@JTranscCallSiteBody(target = "js", value = "((#@)|0)")
	native public int toInt();

	@JTranscCallSiteBody(target = "js", value = "(+(#@))")
	native public double toDouble();

	@JTranscCallSiteBody(target = "js", value = "N.box(#@)")
	native public Object box();

	@JTranscMethodBody(target = "js", value = "return (typeof(window) != 'undefined') ? window : global;")
	native public static JsDynamic global();

	@JTranscCallSiteBody(target = "js", value = "_global#.0")
	native public static JsDynamic global(@JTranscLiteralParam String name);

	@JTranscCallSiteBody(target = "js", value = "(#0)")
	native public static JsDynamic as(Object obj);

	@JTranscCallSiteBody(target = "js", value = "{}")
	native public static JsDynamic newEmptyObject();

	@JTranscCallSiteBody(target = "js", value = "[]")
	native public static JsDynamic newEmptyArray();

	@JTranscCallSiteBody(target = "js", value = "#0")
	native public static JsDynamic raw(@JTranscLiteralParam String js);

	@JTranscMethodBody(target = "js", value = {
		"var handler = p0;",
		"return function() {",
		"	return N.unbox(handler{% IMETHOD com.jtransc.target.js.JsDynamic$Function0:invoke %}());",
		"};",
	})
	native public static Object func(Function0 func);

	@JTranscMethodBody(target = "js", value = {
		"var handler = p0;",
		"return function(p1) {",
		"	return N.unbox(handler{% IMETHOD com.jtransc.target.js.JsDynamic$Function1:invoke %}(p1));",
		"};",
	})
	native public static Object func(Function1 func);

	@JTranscMethodBody(target = "js", value = {
		"var handler = p0;",
		"return function(p1, p2) {",
		"	return N.unbox(handler{% IMETHOD com.jtransc.target.js.JsDynamic$Function2:invoke %}(p1, p2));",
		"};",
	})
	native public static Object func(Function2 func);

	public interface Function0 {
		Object run();
	}

	public interface Function1 {
		Object run(JsDynamic p0);
	}

	public interface Function2 {
		Object run(JsDynamic p0, JsDynamic p1);
	}
}
