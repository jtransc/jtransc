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

import jtransc.internal.IntJTranscStrings;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Locale;

public final class String implements java.io.Serializable, Comparable<String>, CharSequence {
	public String() {

	}

	public String(String original) {
	}

	public String(char value[]) {

	}

	public String(char value[], int offset, int count) {
	}

	public String(int[] codePoints, int offset, int count) {
	}

	@Deprecated
	public String(byte ascii[], int hibyte, int offset, int count) {
	}

	@Deprecated
	public String(byte ascii[], int hibyte) {

	}

	public String(byte bytes[], int offset, int length, String charsetName) throws UnsupportedEncodingException {
	}

	public String(byte bytes[], int offset, int length, Charset charset) {
	}

	public String(byte bytes[], String charsetName) throws UnsupportedEncodingException {
	}

	public String(byte bytes[], Charset charset) {

	}

	public String(byte bytes[], int offset, int length) {
	}

	public String(byte bytes[]) {
		this(bytes, 0, bytes.length);
	}

	public String(StringBuffer buffer) {
	}

	public String(StringBuilder builder) {

	}

	native public int length();

	native public boolean isEmpty();

	native public char charAt(int index);

	public int codePointAt(int index) {
		return charAt(index);
	}

	native public int codePointBefore(int index);

	native public int codePointCount(int beginIndex, int endIndex);

	native public int offsetByCodePoints(int index, int codePointOffset);

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

	native public byte[] getBytes(String charsetName) throws UnsupportedEncodingException;

	native public byte[] getBytes(Charset charset);

	native public byte[] getBytes();

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

	native public int compareTo(String anotherString);

	//public static final Comparator<String> CASE_INSENSITIVE_ORDER = new CaseInsensitiveComparator();
	public static final Comparator<String> CASE_INSENSITIVE_ORDER = null;

	native public int compareToIgnoreCase(String str);

	native public boolean regionMatches(int toffset, String other, int ooffset, int len);

	native public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len);

	native public boolean startsWith(String prefix, int toffset);

	native public boolean startsWith(String prefix);

	native public boolean endsWith(String suffix);

	private int hash = 0;

	public int hashCode() {
		int h = hash;
		int length = this.length();
		if (h == 0 && length > 0) {
			for (int i = 0; i < length; i++) h = 31 * h + this.charAt(i);
			hash = h;
		}
		return h;
	}

	native public int indexOf(int ch);

	native public int indexOf(int ch, int fromIndex);

	native public int lastIndexOf(int ch);

	native public int lastIndexOf(int ch, int fromIndex);

	native public int indexOf(String str);

	native public int indexOf(String str, int fromIndex);

	native public int lastIndexOf(String str);

	native public int lastIndexOf(String str, int fromIndex);

	native public String substring(int beginIndex);

	native public String substring(int beginIndex, int endIndex);

	native public CharSequence subSequence(int beginIndex, int endIndex);

	native public String concat(String str);

	native public String replace(char oldChar, char newChar);

	native public boolean matches(String regex);

	native public boolean contains(CharSequence s);

	native public String replaceFirst(String regex, String replacement);

	native public String replaceAll(String regex, String replacement);

	native public String replace(CharSequence target, CharSequence replacement);

	native public String[] split(String regex, int limit);

	native public String[] split(String regex);

	native public static String join(CharSequence delimiter, CharSequence... elements);

	native public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements);

	native public String toLowerCase(Locale locale);

	native public String toLowerCase();

	native public String toUpperCase(Locale locale);

	native public String toUpperCase();

	native public String trim();

	public String toString() {
		return this;
	}

	public char[] toCharArray() {
		char[] out = new char[length()];
		for (int n = 0; n < out.length; n++) out[n] = this.charAt(n);
		return out;
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

	native public static String copyValueOf(char data[], int offset, int count);

	native public static String copyValueOf(char data[]);

	public static String valueOf(boolean b) {
		return Boolean.toString(b);
	}

	native public static String valueOf(char c);

	native public static String valueOf(int i);

	native public static String valueOf(long l);

	native public static String valueOf(float f);

	native public static String valueOf(double d);

	public String intern() {
		return this;
	}
}
