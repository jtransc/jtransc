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

import jtransc.annotation.haxe.HaxeMethodBody;

public class StringBuilder extends AbstractStringBuilder implements java.io.Serializable, CharSequence {
	public StringBuilder() {
		super(16);
	}

	public StringBuilder(int capacity) {
		super(capacity);
	}

	public StringBuilder(String str) {
		super(str.length() + 16);
		append(str);
	}

	public StringBuilder(CharSequence seq) {
		super(seq.length() + 16);
		append(seq);
	}

	@Override
	public StringBuilder append(String str) {
		super.append(str);
		return this;
	}

	@Override
	public StringBuilder delete(int start, int end) {
		super.delete(start, end);
		return this;
	}

	@Override
	public StringBuilder replace(int start, int end, String str) {
		super.replace(start, end, str);
		return this;
	}

	@Override
	public StringBuilder append(Object obj) {
		super.append(obj);
		return this;
	}

	@Override
	public StringBuilder append(StringBuffer sb) {
		super.append(sb);
		return this;
	}

	@Override
	public StringBuilder append(CharSequence s) {
		super.append(s);
		return this;
	}

	@Override
	public StringBuilder append(CharSequence s, int start, int end) {
		super.append(s, start, end);
		return this;
	}

	@Override
	public StringBuilder append(char[] str) {
		super.append(str);
		return this;
	}

	@Override
	public StringBuilder append(char[] str, int offset, int len) {
		super.append(str, offset, len);
		return this;
	}

	@Override
	public StringBuilder append(boolean v) {
		super.append(v);
		return this;
	}

	@Override
	public StringBuilder append(char v) {
		super.append(v);
		return this;
	}

	@Override
	public StringBuilder append(int v) {
		super.append(v);
		return this;
	}

	@Override
	public StringBuilder append(long v) {
		super.append(v);
		return this;
	}

	@Override
	public StringBuilder append(float v) {
		super.append(v);
		return this;
	}

	@Override
	public StringBuilder append(double v) {
		super.append(v);
		return this;
	}

	@Override
	public StringBuilder appendCodePoint(int codePoint) {
		super.appendCodePoint(codePoint);
		return this;
	}

	@Override
	public StringBuilder deleteCharAt(int index) {
		super.deleteCharAt(index);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, String str) {
		super.insert(offset, str);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, char[] str, int pos, int len) {
		super.insert(offset, str, pos, len);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, Object obj) {
		super.insert(offset, obj);
		return this;

	}

	@Override
	public StringBuilder insert(int offset, char[] str) {
		super.insert(offset, str);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, CharSequence s) {
		super.insert(offset, s);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, CharSequence s, int start, int end) {
		super.insert(offset, s, start, end);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, boolean v) {
		super.insert(offset, v);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, char v) {
		super.insert(offset, v);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, int v) {
		super.insert(offset, v);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, long v) {
		super.insert(offset, v);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, float v) {
		super.insert(offset, v);
		return this;
	}

	@Override
	public StringBuilder insert(int offset, double v) {
		super.insert(offset, v);
		return this;
	}

	@Override
	public StringBuilder reverse() {
		super.reverse();
		return this;
	}

	@Override
	@HaxeMethodBody("return HaxeNatives.str(this._str);")
	native public String toString();
}
