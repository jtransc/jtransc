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

package java.text;

import jtransc.JTranscExceptions;

import java.util.Objects;

public final class StringCharacterIterator implements CharacterIterator {
	private String text;
	private int begin;
	private int end;
	private int pos;

	public StringCharacterIterator(String text) {
		this(text, 0);
	}

	public StringCharacterIterator(String text, int pos) {
		this(text, 0, text.length(), pos);
	}

	public StringCharacterIterator(String text, int begin, int end, int pos) {
		Objects.requireNonNull(text);
		this.text = text;
		this.begin = begin;
		this.end = end;
		this.pos = pos;
	}

	public void setText(String text) {
		Objects.requireNonNull(text);
		this.text = text;
		this.begin = 0;
		this.end = text.length();
		this.pos = 0;
	}

	public char first() {
		pos = begin;
		return current();
	}

	public char last() {
		pos = (end != begin) ? (end - 1) : end;
		return current();
	}

	public char setIndex(int p) {
		if (p < begin || p > end) JTranscExceptions.invalidIndex();
		pos = p;
		return current();
	}

	public char current() {
		return (pos >= begin && pos < end) ? text.charAt(pos) : DONE;
	}

	public char next() {
		if (pos >= end - 1) {
			pos = end;
			return DONE;
		}
		pos++;
		return text.charAt(pos);
	}

	public char previous() {
		if (pos <= begin) return DONE;
		pos--;
		return text.charAt(pos);
	}

	public int getBeginIndex() {
		return begin;
	}

	public int getEndIndex() {
		return end;
	}

	public int getIndex() {
		return pos;
	}

	public boolean equals(Object that) {
		if (this == that) return true;
		if (!(that instanceof StringCharacterIterator)) return false;
		StringCharacterIterator l = this;
		StringCharacterIterator r = (StringCharacterIterator) that;
		return l.hashCode() == r.hashCode() && Objects.equals(l.text, r.text) && !(l.pos != r.pos || l.begin != r.begin || l.end != r.end);
	}

	public int hashCode() {
		return text.hashCode() ^ pos ^ begin ^ end;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
