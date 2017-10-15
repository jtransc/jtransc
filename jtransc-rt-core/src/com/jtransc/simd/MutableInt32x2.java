package com.jtransc.simd;

import com.jtransc.annotation.*;

@JTranscInvisible
@JTranscNativeNameList({
	@JTranscNativeName(target = "dart", value = "Int32x4"),
	@JTranscNativeName(target = "cpp", value = "Int32x2", defaultValue = "Int32x2_i(0, 0)"),
})
public class MutableInt32x2 {
	private int x;
	private int y;

	@JTranscSync
	private MutableInt32x2(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Int32x4(0, 0, 0, 0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Int32x2_i(0, 0)"),
	})
	@JTranscSync
	static public MutableInt32x2 create() {
		return create(0, 0);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "new Int32x4(#0, #1, 0, 0)"),
		@JTranscCallSiteBody(target = "cpp", value = "Int32x2_i(#0, #1)"),
	})
	@JTranscSync
	static public MutableInt32x2 create(int x, int y) {
		return new MutableInt32x2(x, y);
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = new Int32x4(#0, #1, 0, 0);"),
		@JTranscCallSiteBody(target = "cpp", value = "Int32x2_i(#0, #1)"),
	})
	@JTranscSync
	public void set(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@ = #@ + #0;"),
		@JTranscCallSiteBody(target = "cpp", value = "{ #@.x = #0.x + #1.x; #@.y = #0.y + #1.y; }"),
	})
	@JTranscSync
	public void setToAdd(MutableInt32x2 l, MutableInt32x2 r) {
		this.x = l.x + r.x;
		this.y = l.y + r.y;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.x"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.x"),
	})
	@JTranscSync
	public int getX() {
		return x;
	}

	@JTranscCallSiteBodyList({
		@JTranscCallSiteBody(target = "dart", value = "#@.y"),
		@JTranscCallSiteBody(target = "cpp", value = "#@.y"),
	})
	@JTranscSync
	public int getY() {
		return y;
	}
}