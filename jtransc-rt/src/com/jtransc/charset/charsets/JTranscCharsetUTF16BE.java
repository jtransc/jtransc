package com.jtransc.charset.charsets;

import com.jtransc.annotation.JTranscSync;

public class JTranscCharsetUTF16BE extends JTranscCharsetUTF16Base {
	@JTranscSync
	public JTranscCharsetUTF16BE() {
		super(new String[] { "UTF-16BE", "UnicodeBigUnmarked", "X-UTF-16BE", "ISO-10646-UCS-2" }, false);
	}
}
