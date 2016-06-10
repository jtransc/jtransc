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

import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({
	"public var buffer:StringBuf = new StringBuf();",
	"public var str2:String = null;",
	"public function add(str:String) { this.str2 = null; buffer.add(str); return this; }",
	"public function addChar(c:Int) { this.str2 = null; buffer.add(String.fromCharCode(c)); return this; }",
	"public function getStr() { if (this.str2 == null) this.str2 = buffer.toString(); return this.str2; }",
	"public function setStr(str:String) { this.str2 = str; buffer = new StringBuf(); buffer.add(str); return this; }",
})
abstract class AbstractStringBuilder implements Appendable, CharSequence {
	AbstractStringBuilder() {
		this(0);
	}

	@JTranscMethodBody(target = "js", value = "this._str = '';")
	AbstractStringBuilder(int capacity) {
	}

	//@Override
	@HaxeMethodBody("return this.buffer.length;")
	@JTranscMethodBody(target = "js", value = "return this._str.length;")
	native public int length();

	@HaxeMethodBody("setStr(getStr());")
	@JTranscMethodBody(target = "js", value = "")
	native public void trimToSize();

	//@Override
	@HaxeMethodBody("return this.getStr().charCodeAt(p0);")
	@JTranscMethodBody(target = "js", value = "return this._str.charCodeAt(p0);")
	native public char charAt(int index);

	@HaxeMethodBody("return this.getStr().indexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(p0._str);")
	native public int indexOf(String str);

	@HaxeMethodBody("return this.getStr().indexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(p0._str, p1);")
	native public int indexOf(String str, int fromIndex);

	@HaxeMethodBody("return this.getStr().lastIndexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(p0._str);")
	native public int lastIndexOf(String str);

	@HaxeMethodBody("return this.getStr().lastIndexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(p0._str, p1);")
	native public int lastIndexOf(String str, int fromIndex);

	@HaxeMethodBody("var reversed = ''; var str = getStr(); for (n in 0 ... str.length) reversed += str.charAt(str.length - n - 1); return this.setStr(reversed);")
	@JTranscMethodBody(target = "js", value = "this._str = this._str.reverse(); return this;")
	native public AbstractStringBuilder reverse();

	@HaxeMethodBody("return this.add(HaxeNatives.toNativeString(p0));")
	@JTranscMethodBody(target = "js", value = "this._str += p0._str; return this;")
	native public AbstractStringBuilder append(String str);

	@HaxeMethodBody("return this.setStr(this.getStr().substr(0, p0) + this.getStr().substr(p1));")
	@JTranscMethodBody(target = "js", value = "this._str = this._str.substr(0, p0) + this._str.substr(p1); return this;")
	native public AbstractStringBuilder delete(int start, int end);

	@HaxeMethodBody("return this.setStr(this.getStr().substr(0, p0) + p2._str + this.getStr().substr(p1));")
	native public AbstractStringBuilder replace(int start, int end, String str);

	public int capacity() {
		return length();
	}

	public void ensureCapacity(int minimumCapacity) {

	}

	public void setLength(int newLength) {
		this.delete(newLength, length());
	}

	public AbstractStringBuilder append(Object obj) {
		return this.append(String.valueOf(obj));
	}

	public AbstractStringBuilder append(StringBuffer sb) {
		return this.append(String.valueOf(sb));
	}

	//@Override
	public AbstractStringBuilder append(CharSequence s) {
		return this.append(String.valueOf(s));
	}

	//@Override
	public AbstractStringBuilder append(CharSequence s, int start, int end) {
		return this.append(s.toString().substring(start, end));
	}

	public AbstractStringBuilder append(char[] str) {
		return this.append(new String(str));
	}

	public AbstractStringBuilder append(char str[], int offset, int len) {
		return this.append(new String(str, offset, len));
	}

	public AbstractStringBuilder append(boolean v) {
		return this.append(String.valueOf(v));
	}

	//@Override
	@HaxeMethodBody("return this.addChar(p0);")
	@JTranscMethodBody(target = "js", value = "this._str += String.fromCharCode(p0); return this;")
	native public AbstractStringBuilder append(char v);

	public AbstractStringBuilder append(int v) {
		return this.append(String.valueOf(v));
	}

	public AbstractStringBuilder append(long v) {
		return this.append(String.valueOf(v));
	}

	public AbstractStringBuilder append(float v) {
		return this.append(String.valueOf(v));
	}

	public AbstractStringBuilder append(double v) {
		return this.append(String.valueOf(v));
	}

	public AbstractStringBuilder appendCodePoint(int codePoint) {
		return this.append(new String(new int[]{codePoint}, 0, 1));
	}

	public AbstractStringBuilder deleteCharAt(int index) {
		return this.delete(index, index + 1);
	}

	//@Override
	public CharSequence subSequence(int start, int end) {
		return this.substring(start, end);
	}

	public AbstractStringBuilder insert(int offset, String str) {
		return this.replace(offset, offset, str);
	}

	public AbstractStringBuilder insert(int offset, char[] str, int pos, int len) {
		return this.insert(offset, String.valueOf(str, pos, len));
	}

	public AbstractStringBuilder insert(int offset, Object obj) {
		return this.insert(offset, String.valueOf(obj));
	}

	public AbstractStringBuilder insert(int offset, char[] str) {
		return this.insert(offset, String.valueOf(str));
	}

	public AbstractStringBuilder insert(int offset, CharSequence s) {
		return this.insert(offset, s.toString());
	}

	public AbstractStringBuilder insert(int offset, CharSequence s, int start, int end) {
		return this.insert(offset, s.toString().substring(start, end));
	}

	public AbstractStringBuilder insert(int offset, boolean v) {
		return this.insert(offset, String.valueOf(v));
	}

	public AbstractStringBuilder insert(int offset, char v) {
		return this.insert(offset, String.valueOf(v));
	}

	public AbstractStringBuilder insert(int offset, int v) {
		return this.insert(offset, String.valueOf(v));
	}

	public AbstractStringBuilder insert(int offset, long v) {
		return this.insert(offset, String.valueOf(v));
	}

	public AbstractStringBuilder insert(int offset, float v) {
		return this.insert(offset, String.valueOf(v));
	}

	public AbstractStringBuilder insert(int offset, double v) {
		return this.insert(offset, String.valueOf(v));
	}

	public int codePointAt(int index) {
		return toString().codePointAt(index);
	}

	public int codePointBefore(int index) {
		return toString().codePointBefore(index);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return toString().codePointCount(beginIndex, endIndex);
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return toString().offsetByCodePoints(index, codePointOffset);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		String s = this.toString();
		int len = srcEnd - srcBegin;
		for (int n = 0; n < len; n++) dst[n] = s.charAt(srcBegin + n);
	}

	public void setCharAt(int index, char ch) {
		replace(index, index + 1, String.valueOf(ch));
	}

	public String substring(int start) {
		return this.toString().substring(start);
	}

	public String substring(int start, int end) {
		return this.toString().substring(start, end);
	}

	@Override
	public abstract String toString();
}
