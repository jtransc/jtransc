package java.lang.jtransc;

import com.jtransc.ds.FastStringMap;

import java.io.ByteArrayOutputStream;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;

public class JTranscCharset {
	public float minBytesPerCharacter() {
		return 1;
	}

	public float avgBytesPerCharacter() {
		return minBytesPerCharacter();
	}

	public float maxBytesPerCharacter() {
		return minBytesPerCharacter();
	}

	final public String getCannonicalName() {
		return names[0];
	}

	private final String[] names;

	public JTranscCharset(String[] names) {
		this.names = names;
	}

	final public String[] getAliases() {
		return names;
	}

	public void encode(char[] in, int offset, int len, ByteArrayOutputStream out) {
		throw new RuntimeException("Not implemented encode");
	}

	public void decode(byte[] in, int offset, int len, StringBuilder out) {
		throw new RuntimeException("Not implemented decode");
	}

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
