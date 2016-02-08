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

public class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, CharSequence {
//public class StringBuilder implements java.io.Serializable, CharSequence {
    public StringBuilder() {
        //super(16);
        this(16);
    }

    public StringBuilder(int capacity) {
        //super(capacity);
    }

    public StringBuilder(String str) {
        //super(str.length() + 16);
        this(str.length() + 16);
        append(str);
    }

    public StringBuilder(CharSequence seq) {
        this(seq.length() + 16);
        append(seq);
    }

    @Override
    native public StringBuilder append(Object obj);

    @Override
    native public StringBuilder append(String str);

    native public StringBuilder append(StringBuffer sb);

    @Override
    native public StringBuilder append(CharSequence s);

    @Override
    native public StringBuilder append(CharSequence s, int start, int end);

    @Override
    native public StringBuilder append(char[] str);

    @Override
    native public StringBuilder append(char[] str, int offset, int len);

    @Override
    native public StringBuilder append(boolean b);

    @Override
    native public StringBuilder append(char c);

    @Override
    native public StringBuilder append(int i);

    @Override
    native public StringBuilder append(long lng);

    @Override
    native public StringBuilder append(float f);

    @Override
    native public StringBuilder append(double d);

    @Override
    native public StringBuilder appendCodePoint(int codePoint);

    @Override
    native public StringBuilder delete(int start, int end);

    @Override
    native public StringBuilder deleteCharAt(int index);

    @Override
    native public StringBuilder replace(int start, int end, String str);

    @Override
    native public StringBuilder insert(int index, char[] str, int offset, int len);

    @Override
    native public StringBuilder insert(int offset, Object obj);

    @Override
    native public StringBuilder insert(int offset, String str);

    @Override
    native public StringBuilder insert(int offset, char[] str);

    @Override
    native public StringBuilder insert(int dstOffset, CharSequence s);

    @Override
    native public StringBuilder insert(int dstOffset, CharSequence s, int start, int end);

    @Override
    native public StringBuilder insert(int offset, boolean b);

    @Override
    native public StringBuilder insert(int offset, char c);

    @Override
    native public StringBuilder insert(int offset, int i);

    @Override
    native public StringBuilder insert(int offset, long l);

    @Override
    native public StringBuilder insert(int offset, float f);

    @Override
    native public StringBuilder insert(int offset, double d);

    @Override
    native public int indexOf(String str);

    @Override
    native public int indexOf(String str, int fromIndex);

    @Override
    native public int lastIndexOf(String str);

    @Override
    native public int lastIndexOf(String str, int fromIndex);

    @Override
    native public StringBuilder reverse();

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    native public int length();

    native public char charAt(int index);

    native public CharSequence subSequence(int start, int end);

    @Override
    native public String toString();

    //native public IntStream chars();
    //native public IntStream codePoints();
}
