package com.jtransc.charset.charsets;

import com.jtransc.annotation.JTranscSync;

public class JTranscCharsetUTF16LE extends JTranscCharsetUTF16Base {
	@JTranscSync
	public JTranscCharsetUTF16LE() {
		super(new String[]{"UTF-16LE", "UTF-16", "UnicodeLittleUnmarked", "X-UTF-16LE"}, false);
	}
}
