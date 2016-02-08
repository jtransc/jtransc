package jtransc;

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
	native public CharsetDecoder newDecoder();

	@Override
	native public CharsetEncoder newEncoder();
}
