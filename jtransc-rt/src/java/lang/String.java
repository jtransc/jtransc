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

import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.annotation.haxe.HaxeNativeConversion;
import com.jtransc.internal.IntJTranscStrings;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

@HaxeAddMembers({
	"public var _str:String = '';",
	"public var _array:HaxeArrayChar = null;",
	"public function setStr(str:String) { this._str = str; return this; }",
	"public function _getArray():HaxeArrayChar { if (this._array == null) { this._array = HaxeNatives.stringToCharArray(_str); } return this._array; }",
	"static public function make(str:String) { return new {% CLASS java.lang.String %}().setStr(str); }",
})
@HaxeNativeConversion(haxeType = "String", toHaxe = "N.i_str(@self)", toJava = "N.str(@self)")
public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
	@HaxeMethodBody("this.setStr('');")
	public String() {
	}

	@HaxeMethodBody("this.setStr(p0._str);")
	public String(String original) {
	}

	@HaxeMethodBody("this.setStr(HaxeNatives.charArrayToString(p0, p1, p2));")
	public String(char value[], int offset, int count) {
	}

	@HaxeMethodBody("this.setStr(HaxeNatives.intArrayToString(p0, p1, p2));")
	public String(int[] codePoints, int offset, int count) {
	}

	@Deprecated
	@HaxeMethodBody("this.setStr(HaxeNatives.byteArrayWithHiToString(p0, p1, p2, p3));")
	public String(byte[] ascii, int hibyte, int offset, int count) {
	}

	@HaxeMethodBody("this.setStr(HaxeNatives.byteArrayToString(p0, p1, p2, p3._str));")
	private String(byte[] bytes, int offset, int length, String charsetName, boolean dummy) {
	}

	@Deprecated
	public String(byte[] ascii, int hibyte) {
		this(ascii, hibyte, 0, ascii.length);
	}

	public String(char[] value) {
		this(value, 0, value.length);
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
		return (int)charAt(index);
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

	@HaxeMethodBody("return HaxeNatives.stringToByteArray(this._str, p0._str);")
	native private byte[] _getBytes(String charsetName);

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
	native public boolean equals(Object anObject);

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
	native private int _compareTo(String anotherString);

	public int compareTo(String anotherString) {
		return _compareTo(anotherString);
	}

	//public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
	public static final Comparator<String> CASE_INSENSITIVE_ORDER = null;

	@HaxeMethodBody("var a = this._str.toLowerCase(); var b = p0._str.toLowerCase(); return if ( a < b ) -1 else if ( a > b ) 1 else 0;")
	native public int compareToIgnoreCase(String str);

	@HaxeMethodBody("return this._str.substr(p0, p3) == p1._str.substr(p2, p3);")
	native public boolean regionMatches(int toffset, String other, int ooffset, int len);

	@HaxeMethodBody("return this._str.substr(p0, p3).toLowerCase() == p1._str.substr(p2, p3).toLowerCase();")
	native private boolean regionMatchesIgnoreCase(int toffset, String other, int ooffset, int len);

	public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
		if (ignoreCase) {
			return regionMatchesIgnoreCase(toffset, other, ooffset, len);
		} else {
			return regionMatches(toffset, other, ooffset, len);
		}
	}

	public boolean startsWith(String prefix, int toffset) {
		return this.substring(toffset).startsWith(prefix);
	}

	@HaxeMethodBody("return StringTools.startsWith(this._str, p0._str);")
	native public boolean startsWith(String prefix);

	@HaxeMethodBody("return StringTools.endsWith(this._str, p0._str);")
	native public boolean endsWith(String suffix);

	private int hash = 0;

	@Override
	public int hashCode() {
		int h = hash;
		int length = this.length();
		if (h == 0 && length > 0) {
			for (int i = 0; i < length; i++) {
				h = 31 * h + this.charAt(i);
			}
			hash = h;
		}
		return h;
	}

	@HaxeMethodBody("return _str.indexOf(String.fromCharCode(p0));")
	native public int indexOf(int ch);

	@HaxeMethodBody("return _str.lastIndexOf(String.fromCharCode(p0));")
	native public int lastIndexOf(int ch);

	@HaxeMethodBody("return _str.indexOf(p0._str);")
	native public int indexOf(String str);

	@HaxeMethodBody("return _str.lastIndexOf(p0._str);")
	native public int lastIndexOf(String str);

	@HaxeMethodBody("return _str.indexOf(String.fromCharCode(p0), p1);")
	native public int indexOf(int ch, int fromIndex);

	@HaxeMethodBody("return _str.lastIndexOf(String.fromCharCode(p0), p1);")
	native public int lastIndexOf(int ch, int fromIndex);

	@HaxeMethodBody("return _str.indexOf(p0._str, p1);")
	native public int indexOf(String str, int fromIndex);

	@HaxeMethodBody("return _str.lastIndexOf(p0._str, p1);")
	native public int lastIndexOf(String str, int fromIndex);

	@HaxeMethodBody("return make(_str.substring(p0));")
	native public String substring(int beginIndex);

	@HaxeMethodBody("return make(_str.substring(p0, p1));")
	native public String substring(int beginIndex, int endIndex);

	@HaxeMethodBody("return make(_str.substring(p0, p1));")
	native public CharSequence subSequence(int beginIndex, int endIndex);

	@HaxeMethodBody("return HaxeNatives.str(this._str + p0._str);")
	native public String concat(String str);

	@HaxeMethodBody("return HaxeNatives.str(StringTools.replace(this._str, String.fromCharCode(p0), String.fromCharCode(p1)));")
	native public String replace(char oldChar, char newChar);

	@HaxeMethodBody("return N.str(_str.toLowerCase());")
	native public String toLowerCase(Locale locale);

	@HaxeMethodBody("return N.str(_str.toLowerCase());")
	native public String toLowerCase();

	@HaxeMethodBody("return N.str(_str.toUpperCase());")
	native public String toUpperCase(Locale locale);

	@HaxeMethodBody("return N.str(_str.toUpperCase());")
	native public String toUpperCase();

	@HaxeMethodBody("return N.str(StringTools.trim(this._str));")
	native public String trim();

	public boolean contains(CharSequence s) {
		return indexOf(s.toString()) >= 0;
	}

	//static private Formatter formatter;
	//static private StringBuilder formatterSB;

	public static String format(String format, Object... args) {
		return IntJTranscStrings.format(Locale.getDefault(), format, args);
	}

	public static String format(Locale l, String format, Object... args) {
		return IntJTranscStrings.format(l, format, args);
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

	//@HaxeMethodBody("return HaxeNatives.str('' + p0);")
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
	}

	public String replaceAll(String regex, String replacement) {
		return Pattern.compile(regex).matcher(this).replaceAll(replacement);
	}

	public String[] split(String regex, int limit) {
		return Pattern.compile(regex).split(this, limit);
	}

	public String[] split(String regex) {
		return Pattern.compile(regex).split(this);
	}

	public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
		ArrayList<CharSequence> out = new ArrayList<>();
		for (CharSequence element : elements) out.add(element);
		return join(delimiter, out.toArray(new CharSequence[out.size()]));
	}

	public String toString() {
		return this;
	}

	@HaxeMethodBody("return new EReg('^' + p0._str + '$', '').match(this._str);")
	native public boolean matches(String regex);

	@HaxeMethodBody("return HaxeNatives.str(StringTools.replace(this._str, '$p0', '$p1'));")
	native public String replace(CharSequence target, CharSequence replacement);

	@HaxeMethodBody("return HaxeNatives.str(p1.toArray().join('$p0'));")
	native public static String join(CharSequence delimiter, CharSequence... elements);

	@HaxeMethodBody("return _getArray();")
	native public char[] toCharArray();

	@HaxeMethodBody(target = "js || flash || java || cs", value = "return _str.length;")
	@HaxeMethodBody("return _getArray().length;")
	native public int length();

	@HaxeMethodBody(target = "js || flash || java || cs", value = "return _str.charCodeAt(p0);")
	@HaxeMethodBody("return _getArray().get(p0);")
	native public char charAt(int index);
}
