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

import com.jtransc.annotation.JTranscInline;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeNativeConversion;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.jtransc.JTranscStrings;
import java.lang.jtransc.JTranscUTF8;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;

@SuppressWarnings("ALL")
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
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
	public char[] value;

	@HaxeMethodBody("this.setStr('');")
	@JTranscMethodBody(target = "js", value = "this._str = '';")
	public String() {
		//throw new RuntimeException("Native");
		this.value = new char[0];
	}

	@HaxeMethodBody("this.setStr(p0._str);")
	@JTranscMethodBody(target = "js", value = "this._str = p0._str;")
	public String(String original) {
		//throw new RuntimeException("Native");
		this.value = original.value;
	}

	@HaxeMethodBody("this.setStr(N.charArrayToString(p0, p1, p2));")
	@JTranscMethodBody(target = "js", value = "this._str = N.charArrayToString(p0, p1, p2);")
	public String(char[] value, int offset, int count) {
		//throw new RuntimeException("Native");
		this.value = Arrays.copyOfRange(value, offset, offset + count);
	}

	@HaxeMethodBody("this.setStr(N.intArrayToString(p0, p1, p2));")
	@JTranscMethodBody(target = "js", value = "this._str = N.intArrayToString(p0, p1, p2);")
	public String(int[] codePoints, int offset, int count) {
		this.value = new char[count];
		for (int n = 0; n < count; n++) {
			this.value[n] = (char) codePoints[offset + n];
		}
	}

	@Deprecated
	@HaxeMethodBody("this.setStr(N.byteArrayWithHiToString(p0, p1, p2, p3));")
	@JTranscMethodBody(target = "js", value = "this._str = N.byteArrayWithHiToString(p0, p1, p2, p3);")
	public String(byte[] ascii, int hibyte, int offset, int count) {
		this.value = new char[count];
		int up = (hibyte << 8);
		for (int n = 0; n < count; n++) {
			this.value[n] = (char) (ascii[n] | up);
		}
	}

	@HaxeMethodBody("this.setStr(N.byteArrayToString(p0, p1, p2, p3._str));")
	@JTranscMethodBody(target = "js", value = "this._str = N.byteArrayToString(p0, p1, p2, p3._str);")
	private String(byte[] bytes, int offset, int length, String charsetName, boolean dummy) {
		switch (charsetName) {
			case "ascii":
				this.value = new char[length];
				for (int n = 0; n < length; n++) this.value[n] = (char) bytes[offset + n];
				break;
			case "utf8":
			case "utf-8":
			case "UTF8":
			case "UTF-8":
				this.value = JTranscUTF8.decode(bytes, offset, length);
				break;
			default:
				throw new RuntimeException("Unsupported charset " + charsetName);
		}

	}

	@Deprecated
	public String(byte[] ascii, int hibyte) {
		this(ascii, hibyte, 0, ascii.length);
	}

	public String(char[] value) {
		this(value, 0, value.length);
	}

	// Constructor used by static targets (C++, D) to avoid copying or having extra dependencies
	@HaxeMethodBody("this.setStr(N.charArrayToString(p0, 0, p0.length));")
	@JTranscMethodBody(target = "js", value = "this._str = N.charArrayToString(p0, 0, p0.length);")
	String(char[] value, boolean dummy) {
		this.value = value;
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

	public String(byte[] bytes, int offset, int length) {
		this(bytes, offset, length, "UTF-8", false);
	}

	public String(byte[] bytes) {
		this(bytes, 0, bytes.length, "UTF-8", false);
	}

	public String(StringBuffer buffer) {
		this(buffer.toString());
	}

	public String(StringBuilder builder) {
		this(builder.toString());
	}

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

	@HaxeMethodBody("return N.stringToByteArray(this._str, p0._str);")
	@JTranscMethodBody(target = "js", value = "return N.stringToByteArray(this._str, p0._str);")
	private byte[] _getBytes(String charsetName) {
		int len = this.length();
		ByteArrayOutputStream out = new ByteArrayOutputStream(len * 2);
		if (charsetName == null) charsetName = "UTF-8";
		switch (charsetName) {
			default:
			case "UTF-8":
				for (int n = 0; n < len; n++) {
					// @TODO: Proper UTF-8 encoding!
					out.write(this.charAt(n));
				}
				break;
		}
		return out.toByteArray();
	}

	public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
		return _getBytes(charsetName);
	}

	public byte[] getBytes(Charset charset) {
		return _getBytes(charset.name());
	}

	public byte[] getBytes() {
		return _getBytes("UTF-8");
	}

	@HaxeMethodBody("return Std.is(p0, {% CLASS java.lang.String %}) && (cast(p0, {% CLASS java.lang.String %})._str == this._str);")
	@JTranscMethodBody(target = "js", value = "return N.is(p0, {% CLASS java.lang.String %}) && N.istr(this) == N.istr(p0);")
	public boolean equals(Object anObject) {
		if (this == anObject) return true;
		if (!(anObject instanceof String)) return false;
		String that = (String) anObject;
		if (this.length() != that.length()) return false;
		if (this.hashCode() != that.hashCode()) return false;
		int len = this.length();
		for (int n = 0; n < len; n++) if (this.charAt(n) != that.charAt(n)) return false;
		return true;
	}

	public boolean contentEquals(StringBuffer sb) {
		return this.equals(sb.toString());
	}

	public boolean contentEquals(CharSequence cs) {
		return this.equals(cs.toString());
	}

	public boolean equalsIgnoreCase(String anotherString) {
		return this.toLowerCase().equals(anotherString.toLowerCase());
	}

	@HaxeMethodBody("var a = this._str; var b = p0._str; return if ( a < b ) -1 else if ( a > b ) 1 else 0;")
	@JTranscMethodBody(target = "js", value = "var a = N.istr(this), b = N.istr(p0); return (a < b) ? -1 : ((a > b) ? 1 : 0);")
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

	//public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
	public static final Comparator<String> CASE_INSENSITIVE_ORDER = null;

	public int compareToIgnoreCase(String str) {
		return this.toLowerCase().compareTo(str.toLowerCase());
	}

	@HaxeMethodBody("return this._str.substr(p0, p3) == p1._str.substr(p2, p3);")
	public boolean regionMatches(int toffset, String other, int ooffset, int len) {
		return this.substring(toffset, toffset + len).equals(other.substring(ooffset, ooffset + len));
	}

	@HaxeMethodBody("return this._str.substr(p0, p3).toLowerCase() == p1._str.substr(p2, p3).toLowerCase();")
	private boolean regionMatchesIgnoreCase(int toffset, String other, int ooffset, int len) {
		return this.toLowerCase().substring(toffset, toffset + len).equals(other.toLowerCase().substring(ooffset, ooffset + len));
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

	@HaxeMethodBody("return StringTools.startsWith(this._str, p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.startsWith(p0._str);")
	public boolean startsWith(String prefix) {
		return this.length() >= prefix.length() && JTranscStrings.equals(this.value, 0, prefix.value, 0, prefix.length());
	}

	@HaxeMethodBody("return StringTools.endsWith(this._str, p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.endsWith(p0._str);")
	public boolean endsWith(String suffix) {
		return this.length() >= suffix.length() && JTranscStrings.equals(this.value, this.value.length - suffix.length(), suffix.value, 0, suffix.length());
	}

	/////////////////////
	// indexOf
	/////////////////////

	@HaxeMethodBody("return _str.indexOf(String.fromCharCode(p0));")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.ichar(p0));")
	@JTranscInline
	public int indexOf(int ch) {
		return indexOf(ch, 0);
	}

	@HaxeMethodBody("return _str.indexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.istr(p0));")
	@JTranscInline
	public int indexOf(String str) {
		return indexOf(str, 0);
	}

	@HaxeMethodBody("return _str.indexOf(String.fromCharCode(p0), p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.ichar(p0), p1);")
	public int indexOf(int ch, int fromIndex) {
		return JTranscStrings.indexOf(value, fromIndex, ch);
	}

	@HaxeMethodBody("return _str.indexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.istr(p0), p1);")
	public int indexOf(String str, int fromIndex) {
		return JTranscStrings.indexOf(value, fromIndex, str.value);
	}

	/////////////////////
	// lastIndexOf
	/////////////////////

	@HaxeMethodBody("return _str.lastIndexOf(String.fromCharCode(p0));")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(String.fromCharCode(p0));")
	@JTranscInline
	public int lastIndexOf(int ch) {
		return lastIndexOf(ch, length());
	}

	@HaxeMethodBody("return _str.lastIndexOf(String.fromCharCode(p0), p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(String.fromCharCode(p0), p1);")
	public int lastIndexOf(int ch, int fromIndex) {
		return JTranscStrings.lastIndexOf(value, fromIndex, ch);
	}

	@HaxeMethodBody("return _str.lastIndexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.istr(p0));")
	@JTranscInline
	public int lastIndexOf(String str) {
		return lastIndexOf(str, length());
	}

	@HaxeMethodBody("return _str.lastIndexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	public int lastIndexOf(String str, int fromIndex) {
		return JTranscStrings.lastIndexOf(value, fromIndex, str.value);
	}

	/////////////////////
	// substring
	/////////////////////

	@HaxeMethodBody("return make(_str.substring(p0));")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.slice(p0));")
	public String substring(int beginIndex) {
		return substring(beginIndex, this.value.length);
	}

	@HaxeMethodBody("return make(_str.substring(p0, p1));")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.slice(p0, p1));")
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

	@HaxeMethodBody("return N.str(this._str + p0._str);")
	@JTranscMethodBody(target = "js", value = "return N.str(N.istr(this) + N.istr(p0));")
	public String concat(String str) {
		char[] out = new char[this.length() + str.length()];
		System.arraycopy(this.value, 0, out, 0, this.length());
		System.arraycopy(str.value, 0, out, this.length(), str.length());
		return new String(out);
	}

	@HaxeMethodBody("return N.str(StringTools.replace(this._str, String.fromCharCode(p0), String.fromCharCode(p1)));")
	@JTranscMethodBody(target = "js", value = "return N.str(N.istr(this).replaceAll(String.fromCharCode(p0), String.fromCharCode(p1)));")
	public String replace(char oldChar, char newChar) {
		char[] out = Arrays.copyOf(value, value.length);
		for (int n = 0; n < out.length; n++) if (out[n] == oldChar) out[n] = newChar;
		return new String(out);
	}

	// @TODO: Implement locale?
	public String toLowerCase(Locale locale) {
		return toLowerCase();
	}

	@HaxeMethodBody("return N.str(_str.toLowerCase());")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.toLowerCase());")
	public String toLowerCase() {
		char[] out = Arrays.copyOf(value, value.length);
		for (int n = 0; n < out.length; n++) out[n] = Character.toLowerCase(out[n]);
		return new String(out);
	}

	// @TODO: Implement Locale?
	public String toUpperCase(Locale locale) {
		return toUpperCase();
	}

	@HaxeMethodBody("return N.str(_str.toUpperCase());")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.toUpperCase());")
	public String toUpperCase() {
		char[] out = Arrays.copyOf(value, value.length);
		for (int n = 0; n < out.length; n++) out[n] = Character.toUpperCase(out[n]);
		return new String(out);
	}

	@HaxeMethodBody("return N.str(StringTools.trim(this._str));")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str.trim());")
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
		return format(format, args);
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

	//@HaxeMethodBody("return N.str('' + p0);")
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

	@HaxeMethodBody("return new EReg('^' + p0._str + '$', '').match(this._str);")
	@JTranscMethodBody(target = "js", value = "return new RegExp('^' + N.istr(p0) + '$').test(N.istr(this));")
	public boolean matches(String regex) {
		return Pattern.matches(regex, this);
	}

	public String replace(CharSequence target, CharSequence replacement) {
		return _replace(target.toString(), replacement.toString());
	}

	@HaxeMethodBody("return N.str(StringTools.replace(this._str, '$p0', '$p1'));")
	@JTranscMethodBody(target = "js", value = "return N.str(N.istr(this).replaceAll(N.istr(p0), N.istr(p1)));")
	private String _replace(String target, String replacement) {
		int len = this.length();
		StringBuilder out = new StringBuilder(len);

		char[] _this = this.value;
		char[] _target = target.value;
		int n = 0;

		while (n < len) {
			if (n < this.length() - target.length()) {
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
	public char[] toCharArray() {
		return Arrays.copyOf(this.value, this.value.length);
	}

	@HaxeMethodBody(target = "js || flash || java || cs", value = "return _str.length;")
	@HaxeMethodBody("return _getArray().length;")
	@JTranscMethodBody(target = "js", value = "return this._str.length;")
	public int length() {
		return this.value.length;
	}

	@HaxeMethodBody(target = "js || flash || java || cs", value = "return _str.charCodeAt(p0);")
	@HaxeMethodBody("return _getArray().get(p0);")
	@JTranscMethodBody(target = "js", value = "return this._str.charCodeAt(p0) & 0xFFFF;")
	@JTranscInline
	public char charAt(int index) {
		return this.value[index];
	}

	private int hash = 0;

	@Override
	public int hashCode() {
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
