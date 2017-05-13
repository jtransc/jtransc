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

import com.jtransc.annotation.JTranscAddMembers;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.lang.jtransc.JTranscStrings;
import java.util.Arrays;

// @TODO: Optimize using arrays
@HaxeAddMembers({
	"public var buffer2:StringBuf = new StringBuf();",
	"public var str2:String = null;",
	"public function add(str:String) { this.str2 = null; buffer2.add(str); return this; }",
	"public function addChar(c:Int) { this.str2 = null; buffer2.add(String.fromCharCode(c)); return this; }",
	"public function getStr() { if (this.str2 == null) this.str2 = buffer2.toString(); return this.str2; }",
	"public function setStr(str:String) { this.str2 = str; buffer2 = new StringBuf(); buffer2.add(str); return this; }",
})
@JTranscAddMembers(target = "as3", value = {
	"public var _str: String = '';"
})
abstract class AbstractStringBuilder implements Appendable, CharSequence {
	protected char[] buffer;
	protected int length;

	AbstractStringBuilder() {
		this(0);
	}

	@JTranscMethodBody(target = "js", value = "this._str = ''; return this;")
	@JTranscMethodBody(target = "as3", value = "this._str = ''; return this;")
	AbstractStringBuilder(int capacity) {
		buffer = new char[capacity];
	}

	//@Override
	@HaxeMethodBody("return this.buffer2.length;")
	@JTranscMethodBody(target = "js", value = "return this._str.length;")
	@JTranscMethodBody(target = "as3", value = "return this._str.length;")
	public int length() {
		return length;
	}

	@HaxeMethodBody("setStr(getStr());")
	@JTranscMethodBody(target = "js", value = "")
	@JTranscMethodBody(target = "as3", value = "")
	public void trimToSize() {
		this.buffer = Arrays.copyOf(buffer, length);
	}

	//@Override
	@HaxeMethodBody("return this.getStr().charCodeAt(p0);")
	@JTranscMethodBody(target = "js", value = "return this._str.charCodeAt(p0);")
	@JTranscMethodBody(target = "as3", value = "return this._str.charCodeAt(p0);")
	public char charAt(int index) {
		return buffer[index];
	}

	@HaxeMethodBody("return this.getStr().indexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(N.istr(p0));")
	@JTranscMethodBody(target = "as3", value = "return this._str.indexOf(N.istr(p0));")
	public int indexOf(String str) {
		return indexOf(str, 0);
	}

	@HaxeMethodBody("return this.getStr().indexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.indexOf(p0._str, p1);")
	@JTranscMethodBody(target = "as3", value = "return this._str.indexOf(N.istr(p0), p1);")
	public int indexOf(String str, int fromIndex) {
		return JTranscStrings.indexOf(buffer, fromIndex, JTranscStrings.getData(str));
	}

	@HaxeMethodBody("return this.getStr().lastIndexOf(p0._str);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.istr(p0));")
	@JTranscMethodBody(target = "as3", value = "return this._str.lastIndexOf(N.istr(p0));")
	public int lastIndexOf(String str) {
		return lastIndexOf(str, length);
	}

	@HaxeMethodBody("return this.getStr().lastIndexOf(p0._str, p1);")
	@JTranscMethodBody(target = "js", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	@JTranscMethodBody(target = "as3", value = "return this._str.lastIndexOf(N.istr(p0), p1);")
	public int lastIndexOf(String str, int fromIndex) {
		return JTranscStrings.lastIndexOf(buffer, fromIndex, JTranscStrings.getData(str));
	}

	@HaxeMethodBody("var reversed = ''; var str = getStr(); for (n in 0 ... str.length) reversed += str.charAt(str.length - n - 1); return this.setStr(reversed);")
	@JTranscMethodBody(target = "js", value = "this._str = this._str.reverse(); return this;")
	@JTranscMethodBody(target = "as3", value = "var len: int = this._str.length; var reversed: String = ''; for (var n:int = 0; n < len; n++) reversed += this._str.substr(len - n - 1, 1); this._str = reversed; return this;")
	public AbstractStringBuilder reverse() {
		int len = length / 2;
		for (int n = 0; n < len; n++) {
			int m = length - n - 1;
			char temp = this.buffer[n];
			this.buffer[n] = this.buffer[m];
			this.buffer[m] = temp;
		}
		return this;
	}

	@HaxeMethodBody("return this.add(N.toNativeString(p0));")
	@JTranscMethodBody(target = "js", value = "this._str += N.istr(p0); return this;")
	@JTranscMethodBody(target = "as3", value = "this._str += N.istr(p0); return this;")
	public AbstractStringBuilder append(String _str) {
		String str = String.valueOf(_str);
		//JTranscConsole.log("append.String:");
		//JTranscConsole.log(str);
		int strlen = str.length();
		ensureCapacity(length + strlen);
		System.arraycopy(JTranscStrings.getData(str), 0, buffer, length, strlen);
		length += strlen;
		return this;
	}

	//@Override
	@HaxeMethodBody("return this.addChar(p0);")
	@JTranscMethodBody(target = "js", value = "this._str += String.fromCharCode(p0); return this;")
	@JTranscMethodBody(target = "as3", value = "this._str += String.fromCharCode(p0); return this;")
	public AbstractStringBuilder append(char v) {
		//JTranscConsole.log("append.char:");
		//JTranscConsole.log(v);
		ensureCapacity(length + 1);
		buffer[length++] = v;
		return this;
	}

	@HaxeMethodBody("return this.setStr(this.getStr().substr(0, p0) + this.getStr().substr(p1));")
	@JTranscMethodBody(target = "js", value = "this._str = this._str.substr(0, p0) + this._str.substr(p1); return this;")
	@JTranscMethodBody(target = "as3", value = "this._str = this._str.substr(0, p0) + this._str.substr(p1); return this;")
	public AbstractStringBuilder delete(int start, int end) {
		return replace(start, end, "");
	}

	@HaxeMethodBody("return this.setStr(this.getStr().substr(0, p0) + p2._str + this.getStr().substr(p1));")
	@JTranscMethodBody(target = "js", value = "this._str = this._str.substr(0, p0) + N.istr(p2) + this._str.substr(p1); return this;")
	@JTranscMethodBody(target = "as3", value = "this._str = this._str.substr(0, p0) + N.istr(p2) + this._str.substr(p1); return this;")
	public AbstractStringBuilder replace(int start, int end, String str) {
		//ensure(end);
		int addLength = str.length();
		int removeLength = end - start;

		ensureCapacity(this.length - removeLength + addLength);

		System.arraycopy(buffer, end, buffer, start + addLength, this.length - end);
		System.arraycopy(JTranscStrings.getData(str), 0, buffer, start, addLength);
		this.length = this.length - removeLength + addLength;
		return this;
	}

	public int capacity() {
		return buffer.length;
	}

	private char[] ensure(int minimumCapacity) {
		if (minimumCapacity > buffer.length) {
			buffer = Arrays.copyOf(buffer, Math.max(minimumCapacity, (buffer.length * 2) + 2));
		}
		return buffer;
	}

	public void ensureCapacity(int minimumCapacity) {
		ensure(minimumCapacity);
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
	@JTranscMethodBody(target = "js", value = "return N.str(this._str);")
	@JTranscMethodBody(target = "as3", value = "return N.str(this._str);")
	public String toString() {
		return new String(buffer, 0, length);
	}
}
