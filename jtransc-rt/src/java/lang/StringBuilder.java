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

import java.util.Objects;

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
    native public StringBuilder append(String str);

	@Override
	@HaxeMethodBody("this._str = this._str.substr(0, p0) + this._str.substr(p1); return this;")
	native public StringBuilder delete(int start, int end);

	@Override
	@HaxeMethodBody("this._str = this._str.substr(0, p0) + p2._str + this._str.substr(p1); return this;")
	native public StringBuilder replace(int start, int end, String str);

	@Override
	public StringBuilder append(Object obj) {
		return this.append(Objects.toString(obj));
	}

	@Override
    public StringBuilder append(StringBuffer sb) {
		return this.append(sb.toString());
	}

    @Override
    public StringBuilder append(CharSequence s) {
	    return this.append(s.toString());
    }

    @Override
    public StringBuilder append(CharSequence s, int start, int end) {
	    return append(s.toString().substring(start, end));
    }

    @Override
    public StringBuilder append(char[] str) {
	    return this.append(new String(str));
    }

    @Override
    public StringBuilder append(char[] str, int offset, int len) {
	    return this.append(new String(str, offset, len));
    }

    @Override
    public StringBuilder append(boolean v) {
	    return this.append(Boolean.toString(v));
    }

    @Override
    public StringBuilder append(char v) {
	    return this.append(Character.toString(v));
    }

    @Override
    public StringBuilder append(int v) {
	    return this.append(Integer.toString(v));
    }

    @Override
    public StringBuilder append(long v) {
	    return this.append(Long.toString(v));
    }

    @Override
    public StringBuilder append(float v) {
	    return this.append(Float.toString(v));
    }

    @Override
    public StringBuilder append(double d) {
	    return this.append(Double.toString(d));
    }

    @Override
    public StringBuilder appendCodePoint(int codePoint) {
	    return append(new String(new int[] { codePoint }, 0, 1));
    }

    @Override
    public StringBuilder deleteCharAt(int index) {
	    return delete(index, index + 1);
    }

	@Override
	public StringBuilder insert(int offset, String str) {
		return this.replace(offset, offset, str);
	}

	@Override
    public StringBuilder insert(int offset, char[] str, int pos, int len) {
		return this.insert(offset, new String(str, pos, len));
	}

    @Override
    public StringBuilder insert(int offset, Object obj) {
	    return this.insert(offset, Objects.toString(obj));
    }

    @Override
    public StringBuilder insert(int offset, char[] str) {
	    return this.insert(offset, new String(str));
    }

    @Override
    public StringBuilder insert(int offset, CharSequence s) {
	    return this.insert(offset, s.toString());
    }

    @Override
    public StringBuilder insert(int offset, CharSequence s, int start, int end) {
	    return this.insert(offset, s.toString().substring(start, end));
    }

    @Override
    public StringBuilder insert(int offset, boolean b) {
	    return this.insert(offset, Boolean.toString(b));
    }

    @Override
    public StringBuilder insert(int offset, char c) {
	    return this.insert(offset, Character.toString(c));
    }

    @Override
    public StringBuilder insert(int offset, int i) {
	    return this.insert(offset, Integer.toString(i));
    }

    @Override
    public StringBuilder insert(int offset, long l) {
	    return this.insert(offset, Long.toString(l));
    }

    @Override
    public StringBuilder insert(int offset, float f) {
	    return this.insert(offset, Float.toString(f));
    }

    @Override
    public StringBuilder insert(int offset, double d) {
	    return this.insert(offset, Double.toString(d));
    }

    @Override
    @HaxeMethodBody("return this._str.indexOf(p0._str);")
    native public int indexOf(String str);

    @Override
    @HaxeMethodBody("return this._str.indexOf(p0._str, p1);")
    native public int indexOf(String str, int fromIndex);

    @Override
    @HaxeMethodBody("return this._str.lastIndexOf(p0._str);")
    native public int lastIndexOf(String str);

    @Override
    @HaxeMethodBody("return this._str.lastIndexOf(p0._str, p1);")
    native public int lastIndexOf(String str, int fromIndex);

    @Override
    @HaxeMethodBody("this._str = HaxeNatives.reverseString(this._str); return this;")
    native public StringBuilder reverse();

    /////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////
    @HaxeMethodBody("return this._str.length;")
    native public int length();

    @HaxeMethodBody("return this._str.charCodeAt(p0);")
    native public char charAt(int index);

    public CharSequence subSequence(int start, int end) {
	    return toString().substring(start, end);
    }

    @Override
    @HaxeMethodBody("return HaxeNatives.str(this._str);")
    native public String toString();

    @Override
    public void setLength(int newLength) {
	    this.delete(newLength, length());
    }
}
