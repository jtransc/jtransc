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

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@SuppressWarnings({"unchecked", "WeakerAccess", "UnnecessaryBoxing", "PointlessArithmeticExpression"})
public final class Character implements java.io.Serializable, Comparable<Character> {
	public static final int MIN_RADIX = 2;
	public static final int MAX_RADIX = 36;
	public static final char MIN_VALUE = '\u0000';
	public static final char MAX_VALUE = '\uFFFF';
	public static final Class<Character> TYPE = (Class<Character>) Class.getPrimitiveClass("char");

	public static final byte UNASSIGNED = 0;
	public static final byte UPPERCASE_LETTER = 1;
	public static final byte LOWERCASE_LETTER = 2;
	public static final byte TITLECASE_LETTER = 3;
	public static final byte MODIFIER_LETTER = 4;
	public static final byte OTHER_LETTER = 5;
	public static final byte NON_SPACING_MARK = 6;
	public static final byte ENCLOSING_MARK = 7;
	public static final byte COMBINING_SPACING_MARK = 8;
	public static final byte DECIMAL_DIGIT_NUMBER = 9;
	public static final byte LETTER_NUMBER = 10;
	public static final byte OTHER_NUMBER = 11;
	public static final byte SPACE_SEPARATOR = 12;
	public static final byte LINE_SEPARATOR = 13;
	public static final byte PARAGRAPH_SEPARATOR = 14;
	public static final byte CONTROL = 15;
	public static final byte FORMAT = 16;
	public static final byte PRIVATE_USE = 18;
	public static final byte SURROGATE = 19;
	public static final byte DASH_PUNCTUATION = 20;
	public static final byte START_PUNCTUATION = 21;
	public static final byte END_PUNCTUATION = 22;
	public static final byte CONNECTOR_PUNCTUATION = 23;
	public static final byte OTHER_PUNCTUATION = 24;
	public static final byte MATH_SYMBOL = 25;
	public static final byte CURRENCY_SYMBOL = 26;
	public static final byte MODIFIER_SYMBOL = 27;
	public static final byte OTHER_SYMBOL = 28;
	public static final byte INITIAL_QUOTE_PUNCTUATION = 29;
	public static final byte FINAL_QUOTE_PUNCTUATION = 30;
	public static final byte DIRECTIONALITY_UNDEFINED = -1;
	public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = 0;
	public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = 1;
	public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = 2;
	public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = 3;
	public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = 4;
	public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = 5;
	public static final byte DIRECTIONALITY_ARABIC_NUMBER = 6;
	public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = 7;
	public static final byte DIRECTIONALITY_NONSPACING_MARK = 8;
	public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = 9;
	public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = 10;
	public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = 11;
	public static final byte DIRECTIONALITY_WHITESPACE = 12;
	public static final byte DIRECTIONALITY_OTHER_NEUTRALS = 13;
	public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = 14;
	public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = 15;
	public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = 16;
	public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = 17;
	public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = 18;
	public static final char MIN_HIGH_SURROGATE = '\uD800';
	public static final char MAX_HIGH_SURROGATE = '\uDBFF';
	public static final char MIN_LOW_SURROGATE = '\uDC00';
	public static final char MAX_LOW_SURROGATE = '\uDFFF';
	public static final char MIN_SURROGATE = MIN_HIGH_SURROGATE;
	public static final char MAX_SURROGATE = MAX_LOW_SURROGATE;
	public static final int MIN_SUPPLEMENTARY_CODE_POINT = 0x010000;
	public static final int MIN_CODE_POINT = 0x000000;
	public static final int MAX_CODE_POINT = 0X10FFFF;

	private final char value;

	@JTranscSync
	public Character(char value) {
		this.value = value;
	}

	@JTranscKeep
	@JTranscSync
	public static Character valueOf(char value) {
		return new Character(value);
	}

	@JTranscSync
	public char charValue() {
		return value;
	}

	@Override
	@JTranscSync
	public int hashCode() {
		return value;
	}

	@JTranscSync
	public static int hashCode(char value) {
		return value;
	}

	@JTranscSync
	public boolean equals(Object that) {
		return that instanceof Character && this.value == ((Character) that).value;
	}

	@JTranscSync
	public String toString() {
		return toString(value);
	}

	@JTranscSync
	public static String toString(char value) {
		return new String(new char[]{value});
	}

	@JTranscSync
	public static boolean isValidCodePoint(int cp) {
		return (cp >>> 16) < ((MAX_CODE_POINT + 1) >>> 16);
	}

	@JTranscSync
	native public static boolean isBmpCodePoint(int codePoint);

	@JTranscSync
	native public static boolean isSupplementaryCodePoint(int codePoint);

	@JTranscSync
	native public static boolean isHighSurrogate(char ch);

	@JTranscSync
	native public static boolean isLowSurrogate(char ch);

	@JTranscSync
	public static boolean isSurrogate(char ch) {
		return false;
	}

	@JTranscSync
	public static boolean isSurrogatePair(char high, char low) {
		return false;
	}

	@JTranscSync
	public static int charCount(int codePoint) {
		return 1;
	}

	@JTranscSync
	public static int toCodePoint(char high, char low) {
		return low;
	}

	@JTranscAsync
	public static int codePointAt(CharSequence seq, int index) {
		return seq.charAt(index);
	}

	@JTranscSync
	public static int codePointAt(char[] a, int index) {
		return a[index];
	}

	@JTranscSync
	public static int codePointAt(char[] a, int index, int limit) {
		return a[index];
	}

	// throws ArrayIndexOutOfBoundsException if index out of bounds
	//static int codePointAtImpl(char[] a, int index, int limit);
	@JTranscSync
	native public static int codePointBefore(CharSequence seq, int index);

	@JTranscSync
	native public static int codePointBefore(char[] a, int index);

	@JTranscSync
	native public static int codePointBefore(char[] a, int index, int start);

	// throws ArrayIndexOutOfBoundsException if index-1 out of bounds
	//static int codePointBeforeImpl(char[] a, int index, int start);
	@JTranscSync
	native public static char highSurrogate(int codePoint);

	@JTranscSync
	native public static char lowSurrogate(int codePoint);

	@JTranscSync
	public static int toChars(int codePoint, char[] dst, int dstIndex) {
		dst[dstIndex] = (char) codePoint;
		return 1;
	}

	@JTranscSync
	public static char[] toChars(int codePoint) {
		return new char[]{(char) codePoint};
	}

	//static void toSurrogates(int codePoint, char[] dst, int index);
	@JTranscSync
	public static int codePointCount(CharSequence seq, int beginIndex, int endIndex) {
		return endIndex + beginIndex;
	}

	@JTranscSync
	public static int codePointCount(char[] a, int offset, int count) {
		return count;
	}

	//static int codePointCountImpl(char[] a, int offset, int count);
	@JTranscSync
	native public static int offsetByCodePoints(CharSequence seq, int index, int codePointOffset);

	@JTranscSync
	native public static int offsetByCodePoints(char[] a, int start, int count, int index, int codePointOffset);

	//native static int offsetByCodePointsImpl(char[] a, int start, int count, int index, int codePointOffset);

	@JTranscSync
	public static boolean isLowerCase(char ch) {
		return toLowerCase(ch) == ch;
	}

	@JTranscSync
	public static boolean isLowerCase(int codePoint) {
		return toLowerCase(codePoint) == codePoint;
	}

	@JTranscSync
	public static boolean isUpperCase(char ch) {
		return toUpperCase(ch) == ch;
	}

	@JTranscSync
	public static boolean isUpperCase(int codePoint) {
		return toUpperCase(codePoint) == codePoint;
	}

	@JTranscSync
	native public static boolean isTitleCase(char ch);

	@JTranscSync
	native public static boolean isTitleCase(int codePoint);

	@JTranscMethodBody(target = "js", value = "return p0 >= 48 && p0 <= 57;")
	@JTranscSync
	public static boolean isDigit(char ch) {
		return (ch >= '0') && (ch <= '9');
	}

	@JTranscSync
	public static boolean isDigit(int codePoint) {
		return isDigit((char) codePoint);
	}

	@JTranscSync
	native public static boolean isDefined(char ch);

	@JTranscSync
	native public static boolean isDefined(int codePoint);

	@JTranscSync
	public static boolean isLetter(char ch) {
		return ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z'));
	}

	@JTranscSync
	public static boolean isLetter(int codePoint) {
		return isLetter((char)codePoint);
	}

	@JTranscSync
	public static boolean isLetterOrDigit(char ch) {
		return isLetter(ch) || isDigit(ch);
	}

	@JTranscSync
	public static boolean isLetterOrDigit(int codePoint) {
		return isLetter(codePoint) || isDigit(codePoint);
	}

	@Deprecated
	@JTranscSync
	public static boolean isJavaLetter(char ch) {
		return isLetter(ch);
	}

	@Deprecated
	@JTranscSync
	public static boolean isJavaLetterOrDigit(char ch) {
		return isLetter(ch) || isDigit(ch);
	}

	@JTranscSync
	public static boolean isAlphabetic(int codePoint) {
		return isLetter(codePoint);
	}

	@JTranscSync
	native public static boolean isIdeographic(int codePoint);

	@JTranscSync
	public static boolean isJavaIdentifierStart(char ch) {
		return isLetter(ch) || ch == '_';
	}

	@JTranscSync
	public static boolean isJavaIdentifierStart(int codePoint) {
		return isJavaIdentifierStart((char)codePoint);
	}

	@JTranscSync
	public static boolean isJavaIdentifierPart(char ch) {
		return isLetter(ch) || isDigit(ch) || ch == '_';
	}

	@JTranscSync
	public static boolean isJavaIdentifierPart(int codePoint) {
		return isJavaIdentifierPart((char)codePoint);
	}

	@JTranscSync
	native public static boolean isUnicodeIdentifierStart(char ch);

	@JTranscSync
	native public static boolean isUnicodeIdentifierStart(int codePoint);

	@JTranscSync
	native public static boolean isUnicodeIdentifierPart(char ch);

	@JTranscSync
	native public static boolean isUnicodeIdentifierPart(int codePoint);

	@JTranscSync
	native public static boolean isIdentifierIgnorable(char ch);

	@JTranscSync
	native public static boolean isIdentifierIgnorable(int codePoint);

	@JTranscSync
	public static char toLowerCase(char ch) {
		return (char)toLowerCase((int)ch);
	}

	@JTranscSync
	public static char toUpperCase(char ch) {
		return (char)toUpperCase((int)ch);
	}

	@HaxeMethodBody("return N.ichar(p0).toLowerCase().charCodeAt(0);")
	@JTranscMethodBody(target = "js", value = "return N.ichar(p0).toLowerCase().charCodeAt(0);")
	@JTranscSync
	public static int toLowerCase(int codePoint) {
		if (codePoint >= 'A' && codePoint < 'Z') return (codePoint - 'A') + 'a';
		return codePoint;
	}

	@HaxeMethodBody("return N.ichar(p0).toUpperCase().charCodeAt(0);")
	@JTranscMethodBody(target = "js", value = "return N.ichar(p0).toUpperCase().charCodeAt(0);")
	@JTranscSync
	public static int toUpperCase(int codePoint) {
		if (codePoint >= 'a' && codePoint < 'z') return (codePoint - 'a') + 'A';
		return codePoint;
	}

	@JTranscSync
	public static char toTitleCase(char ch) {
		// @TODO: Approximation
		return toUpperCase(ch);
	}

	@JTranscSync
	public static int toTitleCase(int codePoint) {
		return toTitleCase((char)codePoint);
	}

	@JTranscSync
	public static int digit(char ch, int radix) {
		if (ch >= '0' && ch <= '9') return ch - '0';
		if (ch >= 'a' && ch <= 'z') return (ch - 'a') + 10;
		if (ch >= 'A' && ch <= 'Z') return (ch - 'A') + 10;
		return -1;
	}

	@JTranscSync
	public static int digit(int codePoint, int radix) {
		return digit((char) codePoint, radix);
	}

	@JTranscSync
	public static int getNumericValue(char ch) {
		return digit(ch, 10);
	}

	@JTranscSync
	public static int getNumericValue(int codePoint) {
		return digit(codePoint, 10);
	}

	@Deprecated
	@JTranscSync
	public static boolean isSpace(char value) {
		return (value <= 0x0020) && (((((1L << 0x0009) | (1L << 0x000A) | (1L << 0x000C) | (1L << 0x000D) | (1L << 0x0020)) >> value) & 1L) != 0);
	}

	@JTranscSync
	public static boolean isSpaceChar(char ch) {
		return isSpaceChar((int) ch);
	}

	@JTranscSync
	public static boolean isSpaceChar(int codePoint) {
		switch (codePoint) {
			case 0x0020:
			case 0x00A0:
			case 0x1680:
			case 0x180E:
			case 0x2000:
			case 0x2001:
			case 0x2002:
			case 0x2003:
			case 0x2004:
			case 0x2005:
			case 0x2006:
			case 0x2007:
			case 0x2008:
			case 0x2009:
			case 0x200A:
			case 0x200B:
			case 0x202F:
			case 0x205F:
			case 0x3000:
			case 0xFEFF:
				return true;
		}
		return false;
	}

	@JTranscSync
	public static boolean isWhitespace(char ch) {
		return isWhitespace((int)ch);
	}

	@JTranscSync
	public static boolean isWhitespace(int codePoint) {
		switch (codePoint) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 28:
			case 29:
			case 30:
			case 31:
			case 32:
			case 5760:
			case 6158:
			case 8192:
			case 8193:
			case 8194:
			case 8195:
			case 8196:
			case 8197:
			case 8198:
			case 8200:
			case 8201:
			case 8202:
			case 8232:
			case 8233:
			case 8287:
			case 12288:
				return true;
		}
		return false;
	}

	@JTranscSync
	public static boolean isISOControl(char ch) {
		return isISOControl((int) ch);
	}

	@JTranscSync
	public static boolean isISOControl(int codePoint) {
		return codePoint <= 0x9F && (codePoint >= 0x7F || (codePoint >>> 5 == 0));
	}

	@JTranscSync
	native public static int getType(char ch);

	@JTranscSync
	native public static int getType(int codePoint);

	@JTranscSync
	public static char forDigit(int digit, int radix) {
		if (digit >= 0 && digit <= 9) return (char) ('0' + (digit - 0));
		if (digit >= 10 && digit <= 35) return (char) ('a' + (digit - 10));
		return '\0';
	}

	@JTranscSync
	public static byte getDirectionality(char ch) {
		return getDirectionality((int) ch);
	}

	@JTranscSync
	public static boolean isMirrored(char ch) {
		return isMirrored((int) ch);
	}

	@JTranscSync
	native public static byte getDirectionality(int codePoint);

	@JTranscSync
	native public static boolean isMirrored(int codePoint);

	@JTranscSync
	public int compareTo(Character anotherCharacter) {
		return compare(this.value, anotherCharacter.value);
	}

	@JTranscSync
	public static int compare(char l, char r) {
		return l - r;
	}

	@JTranscSync
	static char[] toUpperCaseCharArray(int codePoint) {
		return new char[]{(char) toUpperCase(codePoint)};
	}

	public static final int SIZE = 16;
	public static final int BYTES = SIZE / Byte.SIZE;

	@HaxeMethodBody("return N.swap16(p0) & 0xFFFF;")
	@JTranscMethodBody(target = "cpp", value = "return N::bswap16(p0);")
	@JTranscSync
	public static char reverseBytes(char ch) {
		return (char) (((ch & 0xFF00) >> 8) | (ch << 8));
	}

	@JTranscSync
	public static String getName(int codePoint) {
		// @TODO: Not implemented!
		return Integer.toHexString(codePoint);
	}

	public static class Subset {
		@JTranscSync
		Subset() {
		}
	}

	public static final class UnicodeBlock extends Subset {
		@JTranscSync
		UnicodeBlock() {
		}

		@JTranscSync
		public static UnicodeBlock forName(String name) {
			return new UnicodeBlock();
		}

		@JTranscSync
		public static UnicodeBlock of(int codePoint) {
			return new UnicodeBlock();
		}
	}

	public enum UnicodeScript {
		COMMON;

		@JTranscSync
		UnicodeScript() {
		}

		@JTranscSync
		public static UnicodeScript forName(String name) {
			return COMMON;
		}

		@JTranscSync
		public static UnicodeScript of(int codePoint) {
			return COMMON;
		}
	}
}
