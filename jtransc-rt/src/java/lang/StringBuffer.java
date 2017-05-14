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
import com.jtransc.annotation.haxe.HaxeMethodBody;

public final class StringBuffer extends StringBuilder implements java.io.Serializable, CharSequence {
	public StringBuffer() {
		this(16);
	}

	public StringBuffer(int capacity) {
		super(capacity);
	}

	public StringBuffer(String str) {
		super(str.length() + 16);
		append(str);
	}

	public StringBuffer(CharSequence seq) {
		super(seq.length() + 16);
		append(seq);
	}

	@Override
	public synchronized int length() {
		return super.length();
	}

	@Override
	public synchronized int capacity() {
		return super.capacity();
	}

	@Override
	public synchronized void ensureCapacity(int minimumCapacity) {
		super.ensureCapacity(minimumCapacity);
	}

	@Override
	public synchronized void trimToSize() {
		super.trimToSize();
	}

	@Override
	public synchronized void setLength(int newLength) {
		super.setLength(newLength);
	}

	@Override
	public synchronized char charAt(int index) {
		return super.charAt(index);
	}

	@Override
	public synchronized int codePointAt(int index) {
		return super.codePointAt(index);
	}

	@Override
	public synchronized int codePointBefore(int index) {
		return super.codePointBefore(index);
	}

	@Override
	public synchronized int codePointCount(int beginIndex, int endIndex) {
		return super.codePointCount(beginIndex, endIndex);
	}

	@Override
	public synchronized int offsetByCodePoints(int index, int codePointOffset) {
		return super.offsetByCodePoints(index, codePointOffset);
	}

	@Override
	public synchronized void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		super.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	@Override
	public synchronized void setCharAt(int index, char ch) {
		super.setCharAt(index, ch);
	}

	@Override
	public synchronized StringBuffer append(Object obj) {
		super.append(obj);
		return this;
	}

	@Override
	public synchronized StringBuffer append(String str) {
		super.append(str);
		return this;
	}

	public synchronized StringBuffer append(StringBuffer sb) {
		super.append(sb);
		return this;
	}

	@Override
	public synchronized StringBuffer append(CharSequence s) {
		super.append(s);
		return this;
	}

	@Override
	public synchronized StringBuffer append(CharSequence s, int start, int end) {
		super.append(s, start, end);
		return this;
	}

	@Override
	public synchronized StringBuffer append(char[] str) {
		super.append(str);
		return this;
	}

	@Override
	public synchronized StringBuffer append(char[] str, int offset, int len) {
		super.append(str, offset, len);
		return this;
	}

	@Override
	public synchronized StringBuffer append(boolean v) {
		super.append(v);
		return this;
	}

	@Override
	public synchronized StringBuffer append(char v) {
		super.append(v);
		return this;
	}

	@Override
	public synchronized StringBuffer append(int v) {
		super.append(v);
		return this;
	}

	@Override
	public synchronized StringBuffer appendCodePoint(int codePoint) {
		super.append(codePoint);
		return this;
	}

	@Override
	public synchronized StringBuffer append(long v) {
		super.append(v);
		return this;
	}

	@Override
	public synchronized StringBuffer append(float v) {
		super.append(v);
		return this;
	}

	@Override
	public synchronized StringBuffer append(double v) {
		super.append(v);
		return this;
	}

	@Override
	public synchronized StringBuffer delete(int start, int end) {
		super.delete(start, end);
		return this;
	}

	@Override
	public synchronized StringBuffer deleteCharAt(int index) {
		super.deleteCharAt(index);
		return this;
	}

	@Override
	public synchronized StringBuffer replace(int start, int end, String str) {
		super.replace(start, end, str);
		return this;
	}

	@Override
	public synchronized String substring(int start) {
		return super.substring(start);
	}

	@Override
	public synchronized CharSequence subSequence(int start, int end) {
		return super.subSequence(start, end);
	}

	@Override
	public synchronized String substring(int start, int end) {
		return super.substring(start, end);
	}

	@Override
	public synchronized StringBuffer insert(int index, char[] str, int offset, int len) {
		super.insert(index, str, offset, len);
		return this;
	}

	@Override
	public synchronized StringBuffer insert(int offset, Object obj) {
		super.insert(offset, obj);
		return this;
	}

	@Override
	public synchronized StringBuffer insert(int offset, String str) {
		super.insert(offset, str);
		return this;
	}

	@Override
	public synchronized StringBuffer insert(int offset, char[] str) {
		super.insert(offset, str);
		return this;
	}

	@Override
	public StringBuffer insert(int dstOffset, CharSequence s) {
		super.insert(dstOffset, s);
		return this;
	}

	@Override
	public synchronized StringBuffer insert(int dstOffset, CharSequence s, int start, int end) {
		super.insert(dstOffset, s, start, end);
		return this;
	}

	@Override
	public StringBuffer insert(int offset, boolean b) {
		super.insert(offset, b);
		return this;
	}

	@Override
	public synchronized StringBuffer insert(int offset, char c) {
		super.insert(offset, c);
		return this;
	}

	@Override
	public StringBuffer insert(int offset, int i) {
		super.insert(offset, i);
		return this;
	}

	@Override
	public StringBuffer insert(int offset, long l) {
		super.insert(offset, l);
		return this;
	}

	@Override
	public StringBuffer insert(int offset, float f) {
		super.insert(offset, f);
		return this;
	}

	@Override
	public StringBuffer insert(int offset, double d) {
		super.insert(offset, d);
		return this;
	}

	@Override
	public int indexOf(String str) {
		return super.indexOf(str);
	}

	@Override
	public synchronized int indexOf(String str, int fromIndex) {
		return super.indexOf(str, fromIndex);
	}

	@Override
	public int lastIndexOf(String str) {
		return super.lastIndexOf(str);
	}

	@Override
	public synchronized int lastIndexOf(String str, int fromIndex) {
		return super.lastIndexOf(str, fromIndex);
	}

	//@Override
	public synchronized StringBuffer reverse() {
		super.reverse();
		return this;
	}

	@Override
	@HaxeMethodBody("return N.str(this.getStr());")
	@JTranscMethodBody(target = "js", value = "return N.str(this._str);")
	public String toString() {
		return super.toString();
	}
}