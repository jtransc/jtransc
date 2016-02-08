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

//public final class StringBuffer extends AbstractStringBuilder implements java.io.Serializable, CharSequence {
public final class StringBuffer implements java.io.Serializable, CharSequence {
    public StringBuffer() {
        this(16);
    }
    public StringBuffer(int capacity) {
        //super(capacity);
    }
    public StringBuffer(String str) {
        //super(str.length() + 16);
        this(str.length() + 16);
        append(str);
    }

    public StringBuffer(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    //@Override
    native public synchronized int length();

    //@Override
    native public synchronized int capacity();


    //@Override
    native public synchronized void ensureCapacity(int minimumCapacity);

    //@Override
    native public synchronized void trimToSize();

    //@Override
    native public synchronized void setLength(int newLength);

    //@Override
    native public synchronized char charAt(int index);

    //@Override
    native public synchronized int codePointAt(int index);

    //@Override
    native public synchronized int codePointBefore(int index);

    //@Override
    native public synchronized int codePointCount(int beginIndex, int endIndex);

    //@Override
    native public synchronized int offsetByCodePoints(int index, int codePointOffset);

    //@Override
    native public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);

    //@Override
    native public synchronized void setCharAt(int index, char ch);

    //@Override
    native public synchronized StringBuffer append(Object obj);

    //@Override
    native public synchronized StringBuffer append(String str);

    native public synchronized StringBuffer append(StringBuffer sb);

    //@Override
    native public synchronized StringBuffer append(CharSequence s);

    //@Override
    native public synchronized StringBuffer append(CharSequence s, int start, int end);

    //@Override
    native public synchronized StringBuffer append(char[] str);

    //@Override
    native public synchronized StringBuffer append(char[] str, int offset, int len);

    //@Override
    native public synchronized StringBuffer append(boolean b);

    //@Override
    native public synchronized StringBuffer append(char c);

    //@Override
    native public synchronized StringBuffer append(int i);

    //@Override
    native public synchronized StringBuffer appendCodePoint(int codePoint);

    //@Override
    native public synchronized StringBuffer append(long lng);

    //@Override
    native public synchronized StringBuffer append(float f);

    //@Override
    native public synchronized StringBuffer append(double d);

    //@Override
    native public synchronized StringBuffer delete(int start, int end);

    //@Override
    native public synchronized StringBuffer deleteCharAt(int index);

    //@Override
    native public synchronized StringBuffer replace(int start, int end, String str);

    //@Override
    native public synchronized String substring(int start);

    //@Override
    native public synchronized CharSequence subSequence(int start, int end);

    //@Override
    native public synchronized String substring(int start, int end);

    //@Override
    native public synchronized StringBuffer insert(int index, char[] str, int offset, int len);

    //@Override
    native public synchronized StringBuffer insert(int offset, Object obj);

    //@Override
    native public synchronized StringBuffer insert(int offset, String str);

    //@Override
    native public synchronized StringBuffer insert(int offset, char[] str);

    //@Override
    native public StringBuffer insert(int dstOffset, CharSequence s);

    //@Override
    native public synchronized StringBuffer insert(int dstOffset, CharSequence s, int start, int end);

    //@Override
    native public StringBuffer insert(int offset, boolean b);

    //@Override
    native public synchronized StringBuffer insert(int offset, char c);

    //@Override
    native public StringBuffer insert(int offset, int i);

    //@Override
    native public StringBuffer insert(int offset, long l);

    //@Override
    native public StringBuffer insert(int offset, float f);

    //@Override
    native public StringBuffer insert(int offset, double d);

    //@Override
    native public int indexOf(String str);

    //@Override
    native public synchronized int indexOf(String str, int fromIndex);

    //@Override
    native public int lastIndexOf(String str);

    //@Override
    native public synchronized int lastIndexOf(String str, int fromIndex);

    //@Override
    native public synchronized StringBuffer reverse();

    @Override
    native public synchronized String toString();
}