/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.charset;

import com.jtransc.annotation.JTranscSync;
import com.jtransc.ds.FastStringMap;

import com.jtransc.charset.JTranscCharset;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.*;

public abstract class Charset implements Comparable<Charset> {
	public static boolean isSupported(String charsetName) {
		try {
			forName(charsetName);
			return true;
		} catch (UnsupportedCharsetException e) {
			return false;
		}
	}

	native public static SortedMap<String, Charset> availableCharsets();

	static private FastStringMap<Charset> charsets = null;
	static private Charset _default;

	static private Charset toCharset(JTranscCharset jCharset) {
		return new Charset(jCharset.getCannonicalName(), jCharset.getAliases()) {
			@Override
			public boolean contains(Charset cs) {
				return false;
			}

			@Override
			public CharsetDecoder newDecoder() {
				return new CharsetDecoder(this, jCharset.avgBytesPerCharacter(), jCharset.maxBytesPerCharacter()) {
					@Override
					protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
						jCharset.decode(in, out);
						return CoderResult.UNDERFLOW;
					}
				};
			}

			@Override
			public CharsetEncoder newEncoder() {
				return new CharsetEncoder(this, jCharset.avgBytesPerCharacter(), jCharset.maxBytesPerCharacter()) {
					@Override
					protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
						char[] chars = new char[in.remaining()];
						in.get(chars);
						out.put(jCharset.encode(new String(chars)));
						return CoderResult.UNDERFLOW;
					}
				};
			}
		};
	}

	public static Charset forName(String charsetName) {
		charsetName = charsetName.toUpperCase().trim();

		if (charsets == null) {
			charsets = new FastStringMap<>();

			final Set<JTranscCharset> jCharsets = JTranscCharset.getSupportedCharsetsSet();
			for (JTranscCharset jCharset : jCharsets) {
				Charset charset = toCharset(jCharset);
				for (String alias : jCharset.getAliases()) {
					charsets.set(alias.toUpperCase().trim(), charset);
				}
			}
		}

		if (charsets.has(charsetName)) {
			return charsets.get(charsetName);
		} else {
			throw new UnsupportedCharsetException(charsetName);
		}
	}

	public static Charset defaultCharset() {
		Class<CoderResult> dummy1 = CoderResult.class;
		if (_default == null) _default = forName(JTranscCharset.defaultCharset().getCannonicalName());
		return _default;
	}

	private String canonicalName;
	private Set<String> aliases;

	protected Charset(String canonicalName, String[] aliases) {
		this.canonicalName = canonicalName;
		this.aliases = new HashSet<String>();
		for (int n = 0; n < aliases.length; n++) this.aliases.add(aliases[n]);
	}

	@JTranscSync
	public final String name() {
		return canonicalName;
	}

	@JTranscSync
	public String displayName() {
		return canonicalName;
	}

	@JTranscSync
	public final Set<String> aliases() {
		return this.aliases;
	}

	@JTranscSync
	public final boolean isRegistered() {
		return true;
	}

	@JTranscSync
	public String displayName(Locale locale) {
		return canonicalName;
	}

	public abstract boolean contains(Charset cs);

	public abstract CharsetDecoder newDecoder();

	public abstract CharsetEncoder newEncoder();

	@JTranscSync
	public boolean canEncode() {
		return true;
	}

	public final CharBuffer decode(ByteBuffer bb) {
		try {
			return this.newDecoder().decode(bb);
		} catch (CharacterCodingException e) {
			throw new Error(e);
		}
	}

	private CharsetEncoder ce;

	public final ByteBuffer encode(CharBuffer cb) {
		try {
			return this.newEncoder().encode(cb);
		} catch (CharacterCodingException e) {
			throw new Error(e);
		}
	}

	public final ByteBuffer encode(String str) {
		return encode(CharBuffer.wrap(str));
	}

	public final int hashCode() {
		return displayName().hashCode();
	}

	native public final int compareTo(Charset that);

	native public final boolean equals(Object ob);

	public final String toString() {
		return displayName();
	}
}

