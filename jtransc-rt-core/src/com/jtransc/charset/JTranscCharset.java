package com.jtransc.charset;

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.ds.FastStringMap;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

abstract public class JTranscCharset {
	@JTranscSync
	final public float minBytesPerCharacter() {
		return min;
	}

	@JTranscSync
	final public float avgBytesPerCharacter() {
		return avg;
	}

	@JTranscSync
	final public float maxBytesPerCharacter() {
		return max;
	}

	@JTranscSync
	final public String getCannonicalName() {
		return names[0];
	}

	private final String[] names;
	private final int min;
	private final float avg;
	private final int max;

	@JTranscSync
	public JTranscCharset(String[] names, int min, float avg, int max) {
		this.names = names;
		this.min = min;
		this.avg= avg;
		this.max = max;
	}

	@JTranscSync
	final public String[] getAliases() {
		return names;
	}

	@JTranscSync
	abstract public void encode(char[] in, int offset, int len, ByteArrayOutputStream out);

	@JTranscSync
	abstract public void decode(byte[] in, int offset, int len, JTranscCharBuffer out);

	@JTranscAsync
	abstract public void decode(ByteBuffer in, CharBuffer out);

	@JTranscSync
	public final byte[] encode(String str) {
		ByteArrayOutputStream out = new ByteArrayOutputStream((int) (str.length() * avgBytesPerCharacter()));
		encode(str.toCharArray(), 0, str.length(), out);
		return out.toByteArray();

	}

	@JTranscSync
	public final String decode(byte[] data, int offset, int len) {
		JTranscCharBuffer out = new JTranscCharBuffer((int) (data.length / avgBytesPerCharacter()));
		decode(data, offset, len, out);
		return out.toString();
	}

	@JTranscSync
	public final String decode(byte[] data) {
		return decode(data, 0, data.length);
	}

	@JTranscSync
	public final char[] decodeChars(byte[] data) {
		return decode(data).toCharArray();
	}

	@JTranscSync
	public final char[] decodeChars(byte[] data, int offset, int len) {
		return decode(data, offset, len).toCharArray();
	}

	static private FastStringMap<JTranscCharset> charsets = new FastStringMap<>();

	static {
		for (JTranscCharset c : ServiceLoader.load(JTranscCharset.class)) {
			for (String alias : c.getAliases()) {
				charsets.set(alias.toUpperCase().trim(), c);
			}
		}
	}

	@JTranscSync
	static public JTranscCharset[] getSupportedCharsets() {
		return charsets.getValues();
	}

	@JTranscAsync
	static public Set<JTranscCharset> getSupportedCharsetsSet() {
		JTranscCharset[] supportedCharsets = getSupportedCharsets();
		HashSet<JTranscCharset> out = new HashSet<>();
		Collections.addAll(out, supportedCharsets);
		return out;
	}

	@JTranscSync
	static public JTranscCharset forName(String name) {
		JTranscCharset charset = charsets.get(name.toUpperCase().trim());
		if (charset == null) throw new UnsupportedCharsetException(name);
		return charset;
	}

	static private JTranscCharset _defaultCharset;

	@JTranscSync
	static public JTranscCharset defaultCharset() {
		if (_defaultCharset == null) _defaultCharset = forName("UTF-8");
		return _defaultCharset;
	}
}