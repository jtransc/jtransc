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
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeNativeConversion;

import java.io.UnsupportedEncodingException;
import com.jtransc.charset.JTranscCharset;
import java.lang.jtransc.JTranscStrings;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

//@SuppressWarnings("ALL")
@HaxeAddMembers({
	"public var _str:String = '';",
	"public var _array:JA_C = null;",
	"public function setStr(str:String) { this._str = str; return this; }",
	"public function _getArray():JA_C { if (this._array == null) { this._array = N.stringToCharArray(_str); } return this._array; }",
	"static public function make(str:String) { return new {% CLASS java.lang.String %}().setStr(str); }",
})
@HaxeNativeConversion(haxeType = "String", toHaxe = "N.i_str(@self)", toJava = "N.str(@self)")
//@JTranscAddMembers(target = "cpp", value = {
//	""
//})
//@JTranscAddMembers(target = "php", value = { "public $_str = null;", })
@JTranscAddMembers(target = "as3", value = {
	"public var _str: String = null;",
	"public var _arr: JA_C = null;",
	"public function __initFromAs3(str: String): void { this._str = str; } ",
})
@JTranscAddMembers(target = "dart", value = "String _str = null; JA_C _arr = null;")
@JTranscAddMembers(target = "php", value = "public $_str = null;")
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
	public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();

	public char[] value;
	private int hash = 0;

	@HaxeMethodBody("this.setStr(N.charArrayToString(p0, 0, p0.length));")
	@JTranscMethodBody(target = "js", value = "this._str = N.charArrayToString(p0, 0, p0.length);")
	@JTranscMethodBody(target = "as3", value = "this._str = N.charArrayToString(p0, 0, p0.length);")
	@JTranscMethodBody(target = "dart", value = "this._str = N.charArrayToString(p0);")
	@JTranscSync
	private void setChars(char[] chars) {
		this.value = chars;
	}

	/////////////////////
	// Constructors:
	/////////////////////

	@JTranscSync
	public String() {
		setChars(new char[0]);
	}

	@HaxeMethodBody("this.setStr(p0._str);")
	@JTranscMethodBody(target = "js", value = "this._str = p0._str; return this;")
	@JTranscMethodBody(target = "as3", value = "this._str = p0._str; return this;")
	@JTranscMethodBody(target = "dart", value = "this._str = p0._str; return this;")
	@JTranscSync
	public String(String original) {
		setChars(original.toCharArray());
	}

	@JTranscSync
	public String(char[] value, int offset, int count) {
		setChars(Arrays.copyOfRange(value, offset, offset + count));
	}

	@JTranscSync
	public String(int[] codePoints, int offset, int count) {
		char[] chars = new char[count];
		for (int n = 0; n < count; n++) chars[n] = (char) codePoints[offset + n];
		setChars(chars);
	}

	@Deprecated
	@JTranscSync
	public String(byte[] ascii, int hibyte, int offset, int count) {
		char[] chars = new char[count];
		int up = (hibyte << 8);
		for (int n = 0; n < count; n++) chars[n] = (char) (ascii[offset + n] | up);
		setChars(chars);
	}

	@Deprecated
	@JTranscSync
	public String(byte[] ascii, int hibyte) {
		this(ascii, hibyte, 0, ascii.length);
	}

	@JTranscSync
	public String(char[] value) {
		setChars(Arrays.copyOf(value, value.length));
	}

	// Constructor used by static targets (C++, D) to avoid copying or having extra dependencies
	@JTranscSync
	String(char[] value, boolean dummy) {
		setChars(value);
	}

	@JTranscSync
	public String(byte[] bytes, int offset, int length) {
		this(bytes, offset, length, "UTF-8", false);
	}

	@JTranscSync
	public String(byte[] bytes) {
		this(bytes, 0, bytes.length, "UTF-8", false);
	}

	@JTranscSync
	public String(StringBuffer buffer) {
		this(buffer.toStringSync());
	}

	@JTranscSync
	public String(StringBuilder builder) {
		this(builder.toStringSync());
	}

	// --------- CHARSETS START

	@JTranscSync
	private String(byte[] bytes, int offset, int length, String charsetName, boolean dummy) {
		setChars(JTranscCharset.forName(charsetName).decodeChars(bytes, offset, length));
	}

	@JTranscSync
	public String(byte[] bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
		this(bytes, offset, length, charsetName, false);
	}

	@JTranscSync
	public String(byte[] bytes, int offset, int length, Charset charset) {
		this(bytes, offset, length, charset.name(), false);
	}

	@JTranscSync
	public String(byte[] bytes, String charsetName) throws UnsupportedEncodingException {
		this(bytes, 0, bytes.length, charsetName, false);
	}

	@JTranscSync
	public String(byte[] bytes, Charset charset) {
		this(bytes, 0, bytes.length, charset.name(), false);
	}

	// --------- CHARSETS END

	/////////////////////
	// End of constructors
	/////////////////////

	@JTranscSync
	public boolean isEmpty() {
		return length() == 0;
	}

	@JTranscSync
	public int codePointAt(int index) {
		return (int) charAt(index);
	}

	@JTranscSync
	public int codePointBefore(int index) {
		return codePointAt(index - 1);
	}

	@JTranscSync
	public int codePointCount(int beginIndex, int endIndex) {
		return endIndex - beginIndex;
	}

	@JTranscSync
	public int offsetByCodePoints(int index, int codePointOffset) {
		return index;
	}

	//native void getChars(char dst[], int dstBegin);

	@JTranscSync
	public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
		int length = srcEnd - srcBegin;
		for (int n = 0; n < length; n++) {
			dst[dstBegin + n] = this.charAt(srcBegin + n);
		}
	}

	@Deprecated
	@JTranscSync
	public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
		byte[] out = this.substring(srcBegin, srcEnd).getBytes();
		System.arraycopy(out, 0, dst, dstBegin, out.length);
	}

	@JTranscSync
	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return JTranscCharset.forName(charsetName).encode(this);
	}

	@JTranscSync
	public byte[] getBytes(Charset charset) {
		return JTranscCharset.forName(charset.name()).encode(this);
	}

	@JTranscSync
	public byte[] getBytes() {
		return JTranscCharset.defaultCharset().encode(this);
	}

	@HaxeMethodBody("return Std.is(p0, {% CLASS java.lang.String %}) && (cast(p0, {% CLASS java.lang.String %})._str == this._str);")
	@JTranscMethodBody(target = "js", value = "return N.is(p0, {% CLASS java.lang.String %}) && N.istr(this) == N.istr(p0);")
	@JTranscMethodBody(target = "as3", value = "return (p0 is {% CLASS java.lang.String %}) && N.istr(this) == N.istr(p0 as {% CLASS java.lang.String %});")
	@JTranscMethodBody(target = "dart", value = "return (p0 is {% CLASS java.lang.String %}) && N.istr(this) == N.istr(p0);")
	@JTranscSync
	public boolean equals(Object that) {
		if (this == that) return true;
		if (!(that instanceof String)) return false;
		return sequals(this, (String) that);
	}

	@JTranscSync
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

	@JTranscSync
	public boolean contentEquals(StringBuffer sb) {
		return sequals(this, sb.toStringSync());
	}

	@JTranscAsync
	public boolean contentEquals(CharSequence cs) {
		return this.equals(cs.toString());
	}

	@JTranscSync
	public boolean equalsIgnoreCase(String anotherString) {
		return sequals(this.toLowerCase(), anotherString.toLowerCase());
	}

	@HaxeMethodBody("var a = this._str; var b = p0._str; return if ( a < b ) -1 else if ( a > b ) 1 else 0;")
	@JTranscMethodBody(target = "js", value = "var a = N.istr(this), b = N.istr(p0); return (a < b) ? -1 : ((a > b) ? 1 : 0);")
	@JTranscMethodBody(target = "as3", value = "var a: String = N.istr(this), b: String = N.istr(p0); return (a < b) ? -1 : ((a > b) ? 1 : 0);")
	@JTranscMethodBody(target = "dart", value = "return this._str.compareTo(N.istr(p0));")
	@JTranscSync
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

	@JTranscSync
	public int compareTo(String anotherString) {
		return _compareTo(anotherString);
	}

	private static class CaseInsensitiveComparator implements Comparator<String>, java.io.Serializable {
		@JTranscSync
		public int compare(String s1, String s2) {
			return s1.compareToIgnoreCase(s2);
		}
	}

	@JTranscSync
	public int compareToIgnoreCase(String str) {
		return this.toLowerCase().compareTo(str.toLowerCase());
	}

	@HaxeMethodBody("return this._str.substr(p0, p3) == p1._str.substr(p2, p3);")
	@JTranscSync
	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		return sequals(this.substring(toffset, toffset + len), (other.substring(ooffset, ooffset + len)));
	}

	@HaxeMethodBody("return this._str.substr(p0, p3).toLowerCase() == p1._str.substr(p2, p3).toLowerCase();")
	@JTranscSync
	private boolean regionMatchesIgnoreCase(int toffset, String other, int ooffset, int len) {
		return sequals(this.toLowerCase().substring(toffset, toffset + len), (other.toLowerCase().substring(ooffset, ooffset + len)));
	}

	@JTranscSync
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

	@JTranscSync
	public boolean startsWith(String prefix, int toffset) {
		return this.substring(toffset).startsWith(prefix);
	}

	@HaxeMethodBody("return StringTools.startsWith(this._str, p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.startsWith(p0._str);")
	@JTranscMethodBody(target = "as3", value = "return this._str.substr(0, p0._str.length) == p0._str;")
	@JTranscMethodBody(target = "dart", value = "return this._str.substring(0, Math.min(this._str.length, p0._str.length)) == p0._str;")
	@JTranscSync
	public boolean startsWith(String prefix) {
		return this.length() >= prefix.length() && JTranscStrings.equals(this.value, 0, prefix.value, 0, prefix.length());
	}

	@HaxeMethodBody("return StringTools.endsWith(this._str, p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.endsWith(p0._str);")
	@JTranscMethodBody(target = "as3", value = "return this._str.substr(-p0._str.length) == p0._str;")
	@JTranscMethodBody(target = "dart", value = "return this._str.substring(this._str.length-p0._str.length) == p0._str;")
	@JTranscSync
	public boolean endsWith(String suffix) {
		return this.length() >= suffix.length() && JTranscStrings.equals(this.value, this.value.length - suffix.length(), suffix.value, 0, suffix.length());
	}

	/////////////////////
	// indexOf
	/////////////////////

	@HaxeMethodBody("return _str.indexOf(N.ichar(p0));")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.ichar(p0));")
	@JTranscMethodBody(target = "as3", value = "return this._str.indexOf(N.ichar(p0));")
	@JTranscMethodBody(target = "dart", value = "return this._str.indexOf(N.ichar(p0));")
	@JTranscInline
	@JTranscSync
	public int indexOf(int ch) {
		return indexOf(ch, 0);
	}

	@HaxeMethodBody("return _str.indexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.istr(p0));")
	@JTranscMethodBody(target = "as3", value = "return this._str.indexOf(N.istr(p0));")
	@JTranscMethodBody(target = "dart", value = "return this._str.indexOf(N.istr(p0));")
	@JTranscInline
	@JTranscSync
	public int indexOf(String str) {
		return indexOf(str, 0);
	}

	@HaxeMethodBody("return _str.indexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "as3", value = "return this._str.indexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.indexOf(N.ichar(p0), p1);")
	@JTranscSync
	public int indexOf(int ch, int fromIndex) {
		return JTranscStrings.indexOf(value, fromIndex, ch);
	}

	@HaxeMethodBody("return _str.indexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.istr(p0), p1);")
	@JTranscMethodBody(target = "as3", value = "return this._str.indexOf(N.istr(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.indexOf(N.istr(p0), p1);")
	@JTranscSync
	public int indexOf(String str, int fromIndex) {
		return JTranscStrings.indexOf(value, fromIndex, str.value);
	}

	/////////////////////
	// lastIndexOf
	/////////////////////

	@HaxeMethodBody("return _str.lastIndexOf(N.ichar(p0));")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.ichar(p0));")
	@JTranscMethodBody(target = "as3", value = "return this._str.lastIndexOf(N.ichar(p0));")
	@JTranscMethodBody(target = "dart", value = "return this._str.lastIndexOf(N.ichar(p0));")
	@JTranscInline
	@JTranscSync
	public int lastIndexOf(int ch) {
		return lastIndexOf(ch, length());
	}

	@HaxeMethodBody("return _str.lastIndexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "as3", value = "return this._str.lastIndexOf(N.ichar(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.lastIndexOf(N.ichar(p0), p1);")
	@JTranscSync
	public int lastIndexOf(int ch, int fromIndex) {
		return JTranscStrings.lastIndexOf(value, fromIndex, ch);
	}

	@HaxeMethodBody("return _str.lastIndexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.istr(p0));")
	@JTranscMethodBody(target = "as3", value = "return this._str.lastIndexOf(N.istr(p0));")
	@JTranscMethodBody(target = "dart", value = "return this._str.lastIndexOf(N.istr(p0));")
	@JTranscInline
	@JTranscSync
	public int lastIndexOf(String str) {
		return lastIndexOf(str, length());
	}

	@HaxeMethodBody("return _str.lastIndexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	@JTranscMethodBody(target = "as3", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	@JTranscMethodBody(target = "dart", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	@JTranscSync
	public int lastIndexOf(String str, int fromIndex) {
		return JTranscStrings.lastIndexOf(value, fromIndex, str.value);
	}

	/////////////////////
	// substring
	/////////////////////

	@HaxeMethodBody("return make(_str.substring(p0));")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.slice(p0));")
	@JTranscMethodBody(target = "as3", value = "return N.str(this._str.slice(p0));")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.substring(p0));")
	@JTranscSync
	public String substring(int beginIndex) {
		return substring(beginIndex, this.value.length);
	}

	@HaxeMethodBody("return make(_str.substring(p0, p1));")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.slice(p0, p1));")
	@JTranscMethodBody(target = "as3", value = "return N.str(this._str.slice(p0, p1));")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.substring(p0, p1));")
	@JTranscSync
	public String substring(int beginIndex, int endIndex) {
		return new String(this.value, beginIndex, endIndex - beginIndex);
	}

	// @TODO: Optimize in some targets to avoid copying?!
	@JTranscSync
	public CharSequence subSequence(int beginIndex, int endIndex) {
		return this.substring(beginIndex, endIndex);
	}

	/////////////////////
	// concat
	/////////////////////

	@HaxeMethodBody("return N.str(this._str + p0._str);")
	@JTranscMethodBody(target = "js", value = "return N.str(N.istr(this) + N.istr(p0));")
	@JTranscMethodBody(target = "as3", value = "return N.str(N.istr(this) + N.istr(p0));")
	@JTranscMethodBody(target = "dart", value = "return N.str(N.istr(this) + N.istr(p0));")
	@JTranscSync
	public String concat(String str) {
		char[] out = new char[this.length() + str.length()];
		System.arraycopy(this.value, 0, out, 0, this.length());
		System.arraycopy(str.value, 0, out, this.length(), str.length());
		return new String(out);
	}

	@HaxeMethodBody("return N.str(StringTools.replace(this._str, N.ichar(p0), N.ichar(p1)));")
	@JTranscMethodBody(target = "js", value = "return N.str(N.istr(this).replaceAll(N.ichar(p0), N.ichar(p1)));")
	@JTranscMethodBody(target = "as3", value = "return N.str(N.istr(this).split(N.ichar(p0)).join(N.ichar(p1)));")
	@JTranscMethodBody(target = "dart", value = "return N.str(N.istr(this).split(N.ichar(p0)).join(N.ichar(p1)));")
	@JTranscSync
	public String replace(char oldChar, char newChar) {
		char[] out = Arrays.copyOf(value, value.length);
		for (int n = 0; n < out.length; n++) if (out[n] == oldChar) out[n] = newChar;
		return new String(out);
	}

	// @TODO: Implement locale?
	@JTranscSync
	public String toLowerCase(Locale locale) {
		return toLowerCase();
	}

	@HaxeMethodBody("return N.str(_str.toLowerCase());")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.toLowerCase());")
	@JTranscMethodBody(target = "as3", value = "return N.str(this._str.toLowerCase());")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.toLowerCase());")
	@JTranscSync
	public String toLowerCase() {
		char[] out = Arrays.copyOf(value, value.length);
		for (int n = 0; n < out.length; n++) out[n] = Character.toLowerCase(out[n]);
		return new String(out);
	}

	// @TODO: Implement Locale?
	@JTranscSync
	public String toUpperCase(Locale locale) {
		return toUpperCase();
	}

	@HaxeMethodBody("return N.str(_str.toUpperCase());")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.toUpperCase());")
	@JTranscMethodBody(target = "as3", value = "return N.str(this._str.toUpperCase());")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.toUpperCase());")
	@JTranscSync
	public String toUpperCase() {
		char[] out = Arrays.copyOf(value, value.length);
		for (int n = 0; n < out.length; n++) out[n] = Character.toUpperCase(out[n]);
		return new String(out);
	}

	@HaxeMethodBody("return N.str(StringTools.trim(this._str));")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.trim());")
	@JTranscMethodBody(target = "as3", value = "return N.str(this._str.replace(/^\\s+/, '').replace(/\\s+$/, ''));")
	@JTranscMethodBody(target = "dart", value = "return N.str(this._str.trim());")
	@JTranscSync
	public String trim() {
		int len = length();
		int l = 0, r = len - 1;
		while (l < length() && Character.isSpaceChar(this.value[l])) l++;
		while (r >= 0 && Character.isSpaceChar(this.value[r])) r--;
		return new String(this.value, l, r - l + 1);
	}

	@JTranscAsync
	public boolean contains(CharSequence s) {
		return indexOf(s.toString()) >= 0;
	}

	//static private Formatter formatter;
	//static private StringBuilder formatterSB;

	@JTranscAsync
	public static String format(String format, Object... args) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format(format, args);
		return sb.toString();
	}

	@JTranscAsync
	public static String format(Locale l, String format, Object... args) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, l);
		formatter.format(format, args);
		return sb.toString();
	}

	@JTranscAsync
	public static String valueOf(Object obj) {
		return (obj != null) ? obj.toString() : "null";
	}

	@JTranscSync
	public static String valueOf(char data[]) {
		return new String(data);
	}

	@JTranscSync
	public static String valueOf(char data[], int offset, int count) {
		return new String(data, offset, count);
	}

	@JTranscSync
	public static String copyValueOf(char data[], int offset, int count) {
		return valueOf(data, offset, count);
	}

	@JTranscSync
	public static String copyValueOf(char data[]) {
		return valueOf(data);
	}

	@JTranscSync
	public static String valueOf(boolean b) {
		return Boolean.toString(b);
	}

	@JTranscSync
	public static String valueOf(char c) {
		return Character.toString(c);
	}

	@JTranscSync
	public static String valueOf(int i) {
		return Integer.toString(i);
	}

	@JTranscSync
	public static String valueOf(long l) {
		return Long.toString(l);
	}

	@JTranscSync
	public static String valueOf(float f) {
		return Float.toString(f);
	}

	@JTranscSync
	public static String valueOf(double d) {
		return Double.toString(d);
	}

	@JTranscSync
	public String intern() {
		return this;
	}

	// REGULAR EXPRESIONS

	@JTranscAsync
	public String replaceFirst(String regex, String replacement) {
		return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
		//return JTranscRegex.Pattern.compile(regex).matcher(this).replaceFirst(replacement);
	}

	@JTranscAsync
	public String replaceAll(String regex, String replacement) {
		return Pattern.compile(regex).matcher(this).replaceAll(replacement);
		//return JTranscRegex.Pattern.compile(regex).matcher(this).replaceAll(replacement);
	}

	@JTranscSync
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

	@JTranscAsync
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

	@JTranscAsync
	public String[] split(String regex) {
		return split(regex, Integer.MAX_VALUE);
	}

	@JTranscAsync
	public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
		ArrayList<CharSequence> out = new ArrayList<CharSequence>();
		for (CharSequence element : elements) out.add(element);
		return join(delimiter, out.toArray(new CharSequence[out.size()]));
	}

	@Override
	@JTranscSync
	public String toString() {
		return this;
	}

	@HaxeMethodBody("return new EReg('^' + p0._str + '$', '').match(this._str);")
	@JTranscMethodBody(target = "js", value = "return new RegExp('^' + N.istr(p0) + '$').test(N.istr(this));")
	@JTranscAsync
	public boolean matches(String regex) {
		return Pattern.matches(regex, this);
	}

	@JTranscAsync
	public String replace(CharSequence target, CharSequence replacement) {
		return _replace(this, target.toString(), replacement.toString());
	}

	@HaxeMethodBody("return N.str(StringTools.replace(N.istr(p0), '$p1', '$p2'));")
	@JTranscMethodBody(target = "js", value = "return N.str(N.istr(p0).replaceAll(N.istr(p1), N.istr(p2)));")
	@JTranscMethodBody(target = "as3", value = "return N.str(N.istr(p0).split(N.istr(p1)).join(N.istr(p2)));")
	@JTranscMethodBody(target = "dart", value = "return N.str(N.istr(p0).split(N.istr(p1)).join(N.istr(p2)));")
	@JTranscSync
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

	@HaxeMethodBody("return N.str(p1.toArray().join('$p0'));")
	@JTranscAsync
	public static String join(CharSequence delimiter, CharSequence... elements) {
		StringBuilder out = new StringBuilder();
		for (CharSequence e : elements) {
			if (out.length() != 0) out.append(delimiter);
			out.append(e);
		}
		return out.toString();
	}

	@HaxeMethodBody("return _getArray();")
	@JTranscMethodBody(target = "js", value = "if (!this._arr) this._arr = N.stringToCharArray(this._str); return this._arr;")
	@JTranscMethodBody(target = "as3", value = "if (!this._arr) this._arr = N.stringToCharArray(this._str); return this._arr;")
	@JTranscMethodBody(target = "dart", value = "if (this._arr == null) this._arr = N.stringToCharArray(this._str); return this._arr;")
	@JTranscSync
	public char[] toCharArray() {
		return Arrays.copyOf(this.value, this.value.length);
	}

	@HaxeMethodBody("return _getArray();")
	@JTranscMethodBody(target = "js", value = "if (!this._arr) this._arr = N.stringToCharArray(this._str); return this._arr;")
	@JTranscMethodBody(target = "as3", value = "if (!this._arr) this._arr = N.stringToCharArray(this._str); return this._arr;")
	@JTranscMethodBody(target = "dart", value = "if (this._arr == null) this._arr = N.stringToCharArray(this._str); return this._arr;")
	@JTranscSync
	public char[] getNativeCharArray() {
		return this.value;
	}

	@HaxeMethodBody(target = "js || flash || java || cs", value = "return _str.length;")
	@HaxeMethodBody("return _getArray().length;")
	@JTranscMethodBody(target = "js", value = "return this._str.length;")
	@JTranscMethodBody(target = "as3", value = "return this._str.length;")
	@JTranscMethodBody(target = "dart", value = "return this._str.length;")
	@JTranscSync
	public int length() {
		return this.value.length;
	}

	@HaxeMethodBody(target = "js || flash || java || cs", value = "return _str.charCodeAt(p0);")
	@HaxeMethodBody("return _getArray().get(p0);")
	@JTranscMethodBody(target = "js", value = "return this._str.charCodeAt(p0) & 0xFFFF;")
	@JTranscMethodBody(target = "as3", value = "return this._str.charCodeAt(p0) & 0xFFFF;")
	@JTranscMethodBody(target = "dart", value = "return this._str.codeUnitAt(p0) & 0xFFFF;")
	@JTranscInline
	@JTranscSync
	public char charAt(int index) {
		return this.value[index];
	}

	@Override
	@JTranscSync
	public int hashCode() {
		return hashCodeSync();
	}

	@JTranscSync
	private int hashCodeSync() {
		int h = hash;
		int length = this.length();
		if (h == 0 && length > 0) {
			for (int i = 0; i < length; i++) h = 31 * h + this.charAt(i);
			//if (h == 0) h = 1;
			hash = h;
		}
		return h;
	}
}
