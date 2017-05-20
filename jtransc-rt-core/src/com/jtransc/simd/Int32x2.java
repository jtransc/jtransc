package com.jtransc.simd;

import com.jtransc.annotation.JTranscCallSiteBody;
import com.jtransc.annotation.JTranscCallSiteBodyList;
import com.jtransc.annotation.JTranscNativeName;
import com.jtransc.annotation.JTranscNativeNameList;

@JTranscNativeNameList({
	@JTranscNativeName(target = "dart", value = "Int32x4"),
	@JTranscNativeName(target = "cpp", value = "Int32x2", defaultValue = "Int32x2_i(0, 0)"),
})
public class Int32x2 {
	private int x;
	private int y;

	private Int32x2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Int32x4(0, 0, 0, 0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Int32x2_i(0, 0)"),
	})
	static public Int32x2 create() {
		return create(0, 0);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Int32x4(#0, #1, 0, 0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Int32x2_i(#0, #1)"),
	})
	static public Int32x2 create(int x, int y) {
		return new Int32x2(x, y);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Int32x4(#0, #1, 0, 0);"),
		@JTranscCallSiteBody(target = "cpp", value = "Int32x2_i(#0, #1)"),
	})
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #@ + #0;"),
		@JTranscCallSiteBody(target = "cpp", value = "{ #@.x = #0.x + #1.x; #@.y = #0.y + #1.y; }"),
	})
	public void setToAdd(Int32x2 l, Int32x2 r) {
		this.x = l.x + r.x;
		this.y = l.y + r.y;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.x"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.x"),
	})
	public int getX() {
		return x;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.y"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.y"),
	})
	public int getY() {
		return y;
	}
}