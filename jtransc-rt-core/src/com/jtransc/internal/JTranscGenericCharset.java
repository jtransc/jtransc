package com.jtransc.internal;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

public class JTranscGenericCharset extends Charset {
	public JTranscGenericCharset(String canonicalName, String[] aliases) {
		super(canonicalName, aliases);
	}

	@Override
	public boolean contains(Charset cs) {
		return this == cs;
	}

	@Override
	public CharsetDecoder newDecoder() {
		throw new RuntimeException("No newDecoder for " + this.name());
	}

	@Override
	public CharsetEncoder newEncoder() {
		throw new RuntimeException("No newEncoder for " + this.name());
	}
}
