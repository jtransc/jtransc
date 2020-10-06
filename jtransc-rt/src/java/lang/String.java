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

package java.lang;

import com.jtransc.annotation.*;




import java.io.UnsupportedEncodingException;
import com.jtransc.charset.JTranscCharset;
import java.lang.jtransc.JTranscStrings;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

@JTranscAddMembers(target = "dart", value = "String _str = null; JA_C _arr = null;")
@JTranscNativeWrapper(target = "js", value = "String")
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
	public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

	public char[] value;
	private int hash = 0;


	@JTranscMethodBody(target = "js", value = "this._str = N.charArrayToString(p0, 0, p0.length);")
	@JTranscMethodBody(target = "dart", value = "this._str = N.charArrayToString(p0);")
	private void setChars(char[] chars) {
		this.value = chars;
	}

	/////////////////////
	// Constructors:
	/////////////////////

	@JTranscMethodBody(target = "js", value = "return '';")
	public String() {
		setChars(new char[0]);
	}


	@JTranscMethodBody(target = "js", value = "return p0;")
	@JTranscMethodBody(target = "dart", value = "this._str = p0._str; return this;")
	public String(String original) {
		setChars(original.toCharArray());
	}

	@JTranscMethodBody(target = "js", value = "return N.charArrayToString(p0, 0, p0.length);")
	public String(char[] value, int offset, int count) {
		setChars(Arrays.copyOfRange(value, offset, offset + count));
	}

	@JTranscMethodBody(target = "js", value = "return N.intArrayToString(p0, 0, p0.length);")
	public String(int[] codePoints, int offset, int count) {
		char[] chars = new char[count];
		for (int n = 0; n < count; n++) chars[n] = (char) codePoints[offset + n];
		setChars(chars);
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "throw new Error('unsupported constructor');")
	public String(byte[] ascii, int hibyte, int offset, int count) {
		char[] chars = new char[count];
		int up = (hibyte << 8);
		for (int n = 0; n < count; n++) chars[n] = (char) (ascii[offset + n] | up);
		setChars(chars);
	}

	@Deprecated
	@JTranscMethodBody(target = "js", value = "throw new Error('unsupported constructor');")
	public String(byte[] ascii, int hibyte) {
		this(ascii, hibyte, 0, ascii.length);
	}

	@JTranscMethodBody(target = "js", value = "return N.charArrayToString(p0);")
	public String(char[] chars) {
		setChars(Arrays.copyOf(chars, chars.length));
	}

	// Constructor used by static targets (C++, D) to avoid copying or having extra dependencies
	@JTranscMethodBody(target = "js", value = "return N.charArrayToString(p0);")
	String(char[] value, boolean dummy) {
		setChars(value);
	}

	public String(byte[] bytes, int offset, int length) {
		this(bytes, offset, length, "UTF-8", false);
	}

	public String(byte[] bytes) {
		this(bytes, 0, bytes.length, "UTF-8", false);
	}

	public String(StringBuffer buffer) {
		this(buffer.toStringSync());
	}

	public String(StringBuilder builder) {
		this(builder.toStringSync());
	}

	// --------- CHARSETS START

	private String(byte[] bytes, int offset, int length, String charsetName, boolean dummy) {
		setChars(JTranscCharset.forName(charsetName).decodeChars(bytes, offset, length));
	}

	public String(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
		this(bytes, offset, length, charsetName, false);
	}

	public String(byte[] bytes, int offset, int length, Charset charset) {
		this(bytes, offset, length, charset.name(), false);
	}

	public String(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
		this(bytes, 0, bytes.length, charsetName, false);
	}

	public String(byte[] bytes, Charset charset) {
		this(bytes, 0, bytes.length, charset.name(), false);
	}

	// --------- CHARSETS END

	/////////////////////
	// End of constructors
	/////////////////////

	public boolean isEmpty() {
		return length() == 0;
	}

	public int codePointAt(int index) {
		return (int) charAt(index);
	}

	public int codePointBefore(int index) {
		return codePointAt(index - 1);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return endIndex - beginIndex;
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return index;
	}

	//native void getChars(char dst[], int dstBegin);

	public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
		int length = srcEnd - srcBegin;
		for (int n = 0; n < length; n++) {
			dst[dstBegin + n] = this.charAt(srcBegin + n);
		}
	}

	@Deprecated
	public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
		byte[] out = this.substring(srcBegin, srcEnd).getBytes();
		System.arraycopy(out, 0, dst, dstBegin, out.length);
	}

	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return JTranscCharset.forName(charsetName).encode(this);
	}

	public byte[] getBytes(Charset charset) {
		return JTranscCharset.forName(charset.name()).encode(this);
	}

	public byte[] getBytes() {
		return JTranscCharset.defaultCharset().encode(this);
	}


	@JTranscMethodBody(target = "js", value = "return this == p0;")
	@JTranscMethodBody(target = "dart", value = "return (p0 is {% CLASS java.lang.String %}) && N.istr(this) == N.istr(p0);")
	public boolean equals(Object that) {
		if (this == that) return true;
		if (!(that instanceof String)) return false;
		return sequals(this, (String) that);
	}

	static private boolean sequals(String l, String r) {
		//noinspection StringEquality
		if (l == r) return true;
		if (l == null) return false;
		if (r == null) return false;
		if (l.length() != r.length()) return false;
		if (l.hashCodeSync() != r.hashCodeSync()) return false;
		final int len = l.length();
		for (int n = 0; n < len; n++) if (l.charAt(n) != r.charAt(n)) return false;
		return true;
	}

	public boolean contentEquals(StringBuffer sb) {
		return sequals(this, sb.toStringSync());
	}

	public boolean contentEquals(CharSequence cs) {
		return this.equals(cs.toString());
	}

	public boolean equalsIgnoreCase(String anotherString) {
		return sequals(this.toLowerCase(), anotherString.toLowerCase());
	}


	@JTranscMethodBody(target = "js", value = "return (this < p0) ? -1 : ((p0 > this) ? 1 : 0);")
	@JTranscMethodBody(target = "dart", value = "return this._str.compareTo(N.istr(p0));")
	private int _compareTo(String that) {
		char v1[] = this.value;
		char v2[] = that.value;
		int len = Math.min(v1.length, v2.length);

		for (int n = 0; n < len; n++) {
			int v = v1[n] - v2[n];
			if (v != 0) return v;
		}

		return v1.length - v2.length;
	}

	public int compareTo(String anotherString) {
		return _compareTo(anotherString);
	}

	private static class CaseInsensitiveComparator implements Comparator<String>, java.io.Serializable {
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
		}
	}

	public int compareToIgnoreCase(String str) {
		return this.toLowerCase().compareTo(str.toLowerCase());
	}


	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		return sequals(this.substring(toffset, toffset + len), (other.substring(ooffset, ooffset + len)));
	}


	private boolean regionMatchesIgnoreCase(int toffset, String other, int ooffset, int len) {
		return sequals(this.toLowerCase().substring(toffset, toffset + len), (other.toLowerCase().substring(ooffset, ooffset + len)));
	}

	public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
		if (ignoreCase) {
			return regionMatchesIgnoreCase(toffset, other, ooffset, len);
		} else {
			return regionMatches(toffset, other, ooffset, len);
		}
	}

	/////////////////////
	// startsWith/startsWith
	/////////////////////

	public boolean startsWith(String prefix, int toffset) {
		return this.substring(toffset).startsWith(prefix);
	}


	@JTranscMethodBody(target = "js", value = "return this.startsWith(p0);")
	@JTranscMethodBody(target = "dart", value = "return this._str.substring(0, Math.min(this._str.length, p0._str.length)) == p0._str;")
	public boolean startsWith(String prefix) {
		return this.length() >= prefix.length() && JTranscStrings.equals(this.value, 0, prefix.value, 0, prefix.length());
	}


	@JTranscMethodBody(target = "js", value = "return this.endsWith(p0);")
	@JTranscMethodBody(target = "dart", value = "return this._str.substring(this._str.length-p0._str.length) == p0._str;")
	public boolean endsWith(String suffix) {
		return this.length() >= suffix.length() && JTranscStrings.equals(this.value, this.length() - suffix.length(), suffix.value, 0, suffix.length());
	}

	/////////////////////
	// indexOf
	/////////////////////

	public int indexOf(int ch) {
		return indexOf(ch, 0);
	}
	public int indexOf(String str) {
		return indexOf(str, 0);
	}


	@JTranscMethodBody(target = "js", value = "return this.indexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.indexOf(N.ichar(p0), p1);")
	public int indexOf(int ch, int fromIndex) {
		return JTranscStrings.indexOf(value, fromIndex, ch);
	}


	@JTranscMethodBody(target = "js", value = "return this.indexOf(p0, p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.indexOf(N.istr(p0), p1);")
	public int indexOf(String str, int fromIndex) {
		return JTranscStrings.indexOf(value, fromIndex, str.value);
	}

	/////////////////////
	// lastIndexOf
	/////////////////////

	public int lastIndexOf(int ch) {
		return lastIndexOf(ch, length());
	}
	public int lastIndexOf(String str) {
		return lastIndexOf(str, length());
	}


	@JTranscMethodBody(target = "js", value = "return this.lastIndexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.lastIndexOf(N.ichar(p0), p1);")
	public int lastIndexOf(int ch, int fromIndex) {
		return JTranscStrings.lastIndexOf(value, fromIndex, ch);
	}


	@JTranscMethodBody(target = "js", value = "return this.lastIndexOf(N.istr(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	public int lastIndexOf(String str, int fromIndex) {
		return JTranscStrings.lastIndexOf(value, fromIndex, str.value);
	}

	/////////////////////
	// substring
	/////////////////////

	public String substring(int beginIndex) {
		return substring(beginIndex, this.length());
	}


	@JTranscMethodBody(target = "js", value = "return this.slice(p0, p1);")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.substring(p0, p1));")
	public String substring(int beginIndex, int endIndex) {
		return new String(this.value, beginIndex, endIndex - beginIndex);
	}

	// @TODO: Optimize in some targets to avoid copying?!
	public CharSequence subSequence(int beginIndex, int endIndex) {
		return this.substring(beginIndex, endIndex);
	}

	/////////////////////
	// concat
	/////////////////////


	@JTranscMethodBody(target = "js", value = "return '' + this + p0;")
	@JTranscMethodBody(target = "dart", value = "return N.str(N.istr(this) + N.istr(p0));")
	public String concat(String str) {
		char[] out = new char[this.length() + str.length()];
		System.arraycopy(this.value, 0, out, 0, this.length());
		System.arraycopy(str.value, 0, out, this.length(), str.length());
		return new String(out);
	}


	@JTranscMethodBody(target = "js", value = "return this.replaceAll(N.ichar(p0), N.ichar(p1));")
	@JTranscMethodBody(target = "dart", value = "return N.str(N.istr(this).split(N.ichar(p0)).join(N.ichar(p1)));")
	public String replace(char oldChar, char newChar) {
		char[] out = Arrays.copyOf(value, length());
		for (int n = 0; n < out.length; n++) if (out[n] == oldChar) out[n] = newChar;
		return new String(out);
	}

	// @TODO: Implement locale?
	public String toLowerCase(Locale locale) {
		return toLowerCase();
	}


	@JTranscMethodBody(target = "js", value = "return this.toLowerCase();")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.toLowerCase());")
	public String toLowerCase() {
		char[] out = Arrays.copyOf(value, length());
		for (int n = 0; n < out.length; n++) out[n] = Character.toLowerCase(out[n]);
		return new String(out);
	}

	// @TODO: Implement Locale?
	public String toUpperCase(Locale locale) {
		return toUpperCase();
	}


	@JTranscMethodBody(target = "js", value = "return this.toUpperCase();")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.toUpperCase());")
	public String toUpperCase() {
		char[] out = Arrays.copyOf(value, length());
		for (int n = 0; n < out.length; n++) out[n] = Character.toUpperCase(out[n]);
		return new String(out);
	}

	@JTranscMethodBody(target = "js", value = "return this.trim();")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.trim());")
	public String trim() {
		int len = length();
		int l = 0, r = len - 1;
		while (l < length() && Character.isSpaceChar(this.value[l])) l++;
		while (r >= 0 && Character.isSpaceChar(this.value[r])) r--;
		return new String(this.value, l, r - l + 1);
	}

	public boolean contains(CharSequence s) {
		return indexOf(s.toString()) >= 0;
	}

	//static private Formatter formatter;
	//static private StringBuilder formatterSB;

	public static String format(String format, Object... args) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format(format, args);
		return sb.toString();
	}

	public static String format(Locale l, String format, Object... args) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, l);
		formatter.format(format, args);
		return sb.toString();
	}

	public static String valueOf(Object obj) {
		return (obj != null) ? obj.toString() : "null";
	}

	public static String valueOf(char data[]) {
		return new String(data);
	}

	public static String valueOf(char data[], int offset, int count) {
		return new String(data, offset, count);
	}

	public static String copyValueOf(char data[], int offset, int count) {
		return valueOf(data, offset, count);
	}

	public static String copyValueOf(char data[]) {
		return valueOf(data);
	}

	public static String valueOf(boolean b) {
		return Boolean.toString(b);
	}

	public static String valueOf(char c) {
		return Character.toString(c);
	}

	public static String valueOf(int i) {
		return Integer.toString(i);
	}

	public static String valueOf(long l) {
		return Long.toString(l);
	}

	public static String valueOf(float f) {
		return Float.toString(f);
	}

	public static String valueOf(double d) {
		return Double.toString(d);
	}

	public String intern() {
		return this;
	}

	// REGULAR EXPRESIONS

	public String replaceFirst(String regex, String replacement) {
		return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
		//return JTranscRegex.Pattern.compile(regex).matcher(this).replaceFirst(replacement);
	}

	public String replaceAll(String regex, String replacement) {
		return Pattern.compile(regex).matcher(this).replaceAll(replacement);
		//return JTranscRegex.Pattern.compile(regex).matcher(this).replaceAll(replacement);
	}

	private String[] split(char ch, int limit) {
		ArrayList<String> out = new ArrayList<String>();
		int n = 0;
		int start = 0;
		for (; n < length(); n++) {
			if (charAt(n) == ch) {
				out.add(this.substring(start, n));
				start = n + 1;
				if (out.size() >= limit - 1) break;
			}
		}
		if (start < this.length()) out.add(this.substring(start));
		return out.toArray(new String[out.size()]);
	}

	public String[] split(String regex, int limit) {
		if (regex.length() == 1) {
			return split(regex.charAt(0), limit);
		} else {
			return Pattern.compile(regex).split(this, limit);
		}
		//return JTranscRegex.Pattern.compile(regex).split(this, limit);
	}

	//native public String replaceFirst(String regex, String replacement);
	//native public String replaceAll(String regex, String replacement);
	//
	//@JTranscMethodBody(target = "js", value="return N.strArray(N.istr(this).split(N.istr(p0), p1));")
	//native public String[] split(String regex, int limit);

	public String[] split(String regex) {
		return split(regex, Integer.MAX_VALUE);
	}

	public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
		ArrayList<CharSequence> out = new ArrayList<CharSequence>();
		for (CharSequence element : elements) out.add(element);
		return join(delimiter, out.toArray(new CharSequence[out.size()]));
	}

	@Override
	public String toString() {
		return this;
	}


	@JTranscMethodBody(target = "js", value = "return new RegExp('^' + (p0) + '$').test((this));")
	public boolean matches(String regex) {
		return Pattern.matches(regex, this);
	}

	public String replace(CharSequence target, CharSequence replacement) {
		return _replace(this, target.toString(), replacement.toString());
	}

	@JTranscMethodBody(target = "js", value = "return p0.replaceAll(p1, p2);")
	@JTranscMethodBody(target = "dart", value = "return N.str(N.istr(p0).split(N.istr(p1)).join(N.istr(p2)));")
	static public String _replace(String base, String target, String replacement) {
		int len = base.length();
		StringBuilder out = new StringBuilder(len);

		char[] _this = base.value;
		char[] _target = target.value;
		int n = 0;

		while (n < len) {
			if (n < base.length() - target.length()) {
				if (JTranscStrings.equals(_this, n, _target, 0, target.length())) {
					out.append(replacement);
					n += target.length();
					continue;
				}
			}
			out.append(_this[n]);
			n++;
		}
		//JTranscStrings.equals(this.value, )
		return out.toString();
	}


	public static String join(CharSequence delimiter, CharSequence... elements) {
		StringBuilder out = new StringBuilder();
		for (CharSequence e : elements) {
			if (out.length() != 0) out.append(delimiter);
			out.append(e);
		}
		return out.toString();
	}

	@JTranscMethodBody(target = "js", value = "return N.stringToCharArray(this);")
	@JTranscMethodBody(target = "dart", value = "if (this._arr == null) this._arr = N.stringToCharArray(this._str); return this._arr;")
	public char[] toCharArray() {
		return Arrays.copyOf(this.value, this.length());
	}

	@JTranscMethodBody(target = "js", value = "return N.stringToCharArray(this);")
	@JTranscMethodBody(target = "dart", value = "if (this._arr == null) this._arr = N.stringToCharArray(this._str); return this._arr;")
	public char[] getNativeCharArray() {
		return this.value;
	}

	@JTranscMethodBody(target = "js", value = "return this.length;")
	@JTranscMethodBody(target = "dart", value = "return this._str.length;")
	public int length() {
		return this.value.length;
	}

	@JTranscMethodBody(target = "js", value = "return this.charCodeAt(p0) & 0xFFFF;")
	@JTranscMethodBody(target = "dart", value = "return this._str.codeUnitAt(p0) & 0xFFFF;")
	@JTranscInline
	public char charAt(int index) {
		return this.value[index];
	}

	@Override
	public int hashCode() {
		return hashCodeSync();
	}

	@JTranscMethodBody(target = "js", value = "return this.length;") // @TODO: Fix hash!
	private int hashCodeSync() {
		int h = hash;
		int length = this.length();
		if (h == 0 && length > 0) {
			for (int i = 0; i < length; i++) {
				h  = ((h << 5) - h) + this.charAt(i);
			}
			//if (h == 0) h = 1;
			hash = h;
		}
		return h;
	}
}
