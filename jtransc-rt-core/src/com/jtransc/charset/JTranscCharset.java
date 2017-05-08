package com.jtransc.charset;

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
	final public float minBytesPerCharacter() {
		return min;
	}

	final public float avgBytesPerCharacter() {
		return avg;
	}

	final public float maxBytesPerCharacter() {
		return max;
	}

	final public String getCannonicalName() {
		return names[0];
	}

	private final String[] names;
	private final int min;
	private final float avg;
	private final int max;

	public JTranscCharset(String[] names, int min, float avg, int max) {
		this.names = names;
		this.min = min;
		this.avg= avg;
		this.max = max;
	}

	final public String[] getAliases() {
		return names;
	}

	abstract public void encode(char[] in, int offset, int len, ByteArrayOutputStream out);

	abstract public void decode(byte[] in, int offset, int len, StringBuilder out);

	abstract public void decode(ByteBuffer in, CharBuffer out);

	public final byte[] encode(String str) {
		ByteArrayOutputStream out = new ByteArrayOutputStream((int) (str.length() * avgBytesPerCharacter()));
		encode(str.toCharArray(), 0, str.length(), out);
		return out.toByteArray();

	}

	public final String decode(byte[] data, int offset, int len) {
		StringBuilder out = new StringBuilder((int) (data.length / avgBytesPerCharacter()));
		decode(data, offset, len, out);
		return out.toString();
	}

	public final String decode(byte[] data) {
		return decode(data, 0, data.length);
	}

	public final char[] decodeChars(byte[] data) {
		return decode(data).toCharArray();
	}

	public final char[] decodeChars(byte[] data, int offset, int len) {
		return decode(data, offset, len).toCharArray();
	}

	static private boolean loadedCharsets = false;
	static private FastStringMap<JTranscCharset> charsets = new FastStringMap<>();

	static private void registerCharset(JTranscCharset c) {
		if (charsets == null) charsets = new FastStringMap<>();
		for (String alias : c.getAliases()) {
			charsets.set(alias.toUpperCase().trim(), c);
		}
	}

	static private void ensureRegister() {
		if (loadedCharsets) return;
		loadedCharsets = true;
		for (JTranscCharset c : ServiceLoader.load(JTranscCharset.class)) registerCharset(c);
	}

	static public JTranscCharset[] getSupportedCharsets() {
		ensureRegister();
		return charsets.getValues();
	}

	static public Set<JTranscCharset> getSupportedCharsetsSet() {
		JTranscCharset[] supportedCharsets = getSupportedCharsets();
		HashSet<JTranscCharset> out = new HashSet<>();
		Collections.addAll(out, supportedCharsets);
		return out;
	}

	static public JTranscCharset forName(String name) {
		ensureRegister();
		JTranscCharset charset = charsets.get(name.toUpperCase().trim());
		if (charset == null) throw new UnsupportedCharsetException(name);
		return charset;
	}

	static private JTranscCharset _defaultCharset;

	static public JTranscCharset defaultCharset() {
		if (_defaultCharset == null) _defaultCharset = forName("UTF-8");
		return _defaultCharset;
	}
}
