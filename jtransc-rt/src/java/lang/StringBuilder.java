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

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({"public var _str:String = '';"})
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
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(Object obj);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(String str);

    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(StringBuffer sb);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(CharSequence s);

    @Override
    native public StringBuilder append(CharSequence s, int start, int end);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(char[] str);

    @Override
    native public StringBuilder append(char[] str, int offset, int len);

    @Override
    @HaxeMethodBody("this._str += p0 ? 'true' : 'false'; return this;")
    native public StringBuilder append(boolean b);

    @Override
    @HaxeMethodBody("this._str += String.fromCharCode(p0); return this;")
    native public StringBuilder append(char c);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(int i);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(long lng);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
    native public StringBuilder append(float f);

    @Override
    @HaxeMethodBody("this._str += p0; return this;")
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
    @HaxeMethodBody(
        "var reversed = '';\n" +
        "for (n in 0 ... this._str.length) reversed += this._str.charAt(this._str.length - n - 1);\n" +
        "this._str = reversed;\n" +
        "return this;\n"
    )
    native public StringBuilder reverse();

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    @HaxeMethodBody("return this._str.length;")
    native public int length();

    @HaxeMethodBody("return this._str.charCodeAt(p0);")
    native public char charAt(int index);

    native public CharSequence subSequence(int start, int end);

    @Override
    @HaxeMethodBody("return HaxeNatives.str(this._str);")
    native public String toString();

    @Override
    @HaxeMethodBody("this._str = this._str.substr(0, p0);")
    native public void setLength(int newLength);

    //native public IntStream chars();
    //native public IntStream codePoints();
}
