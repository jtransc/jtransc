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

abstract class AbstractStringBuilder implements Appendable, CharSequence {
	AbstractStringBuilder() {
	}

	AbstractStringBuilder(int capacity) {

	}

	//@Override
	native public int length();

	native public int capacity();

	native public void ensureCapacity(int minimumCapacity);

	native public void trimToSize();

	native public void setLength(int newLength);

	//@Override
	native public char charAt(int index);

	native public int codePointAt(int index);

	native public int codePointBefore(int index);

	native public int codePointCount(int beginIndex, int endIndex);

	native public int offsetByCodePoints(int index, int codePointOffset);

	native public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);

	native public void setCharAt(int index, char ch);

	native public AbstractStringBuilder append(Object obj);

	native public AbstractStringBuilder append(String str);

	native public AbstractStringBuilder append(StringBuffer sb);

	//@Override
	native public AbstractStringBuilder append(CharSequence s);

	//@Override
	native public AbstractStringBuilder append(CharSequence s, int start, int end);

	native public AbstractStringBuilder append(char[] str);

	native public AbstractStringBuilder append(char str[], int offset, int len);

	native public AbstractStringBuilder append(boolean b);

	//@Override
	native public AbstractStringBuilder append(char c);

	native public AbstractStringBuilder append(int i);

	native public AbstractStringBuilder append(long l);

	native public AbstractStringBuilder append(float f);

	native public AbstractStringBuilder append(double d);

	native public AbstractStringBuilder delete(int start, int end);

	native public AbstractStringBuilder appendCodePoint(int codePoint);

	native public AbstractStringBuilder deleteCharAt(int index);

	native public AbstractStringBuilder replace(int start, int end, String str);

	native public String substring(int start);

	//@Override
	native public CharSequence subSequence(int start, int end);

	native public String substring(int start, int end);

	native public AbstractStringBuilder insert(int index, char[] str, int offset, int len);

	native public AbstractStringBuilder insert(int offset, Object obj);

	native public AbstractStringBuilder insert(int offset, String str);

	native public AbstractStringBuilder insert(int offset, char[] str);

	native public AbstractStringBuilder insert(int dstOffset, CharSequence s);

	native public AbstractStringBuilder insert(int dstOffset, CharSequence s, int start, int end);

	native public AbstractStringBuilder insert(int offset, boolean b);

	native public AbstractStringBuilder insert(int offset, char c);

	native public AbstractStringBuilder insert(int offset, int i);

	native public AbstractStringBuilder insert(int offset, long l);

	native public AbstractStringBuilder insert(int offset, float f);

	native public AbstractStringBuilder insert(int offset, double d);

	native public int indexOf(String str);

	native public int indexOf(String str, int fromIndex);

	native public int lastIndexOf(String str);

	native public int lastIndexOf(String str, int fromIndex);

	native public AbstractStringBuilder reverse();

	@Override
	public abstract String toString();
}
