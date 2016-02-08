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

package java.util;

public class StringTokenizer implements Enumeration<Object> {
	private int currentPosition;
	private int newPosition;
	private int maxPosition;
	private String str;
	private String delimiters;
	private boolean retDelims;
	private boolean delimsChanged;
	private int maxDelimCodePoint;
	private boolean hasSurrogates = false;
	private int[] delimiterCodePoints;

	private void setMaxDelimCodePoint() {
		if (delimiters == null) {
			maxDelimCodePoint = 0;
			return;
		}

		int m = 0;
		int c;
		int count = 0;
		for (int i = 0; i < delimiters.length(); i += Character.charCount(c)) {
			c = delimiters.charAt(i);
			if (c >= Character.MIN_HIGH_SURROGATE && c <= Character.MAX_LOW_SURROGATE) {
				c = delimiters.codePointAt(i);
				hasSurrogates = true;
			}
			if (m < c) m = c;
			count++;
		}
		maxDelimCodePoint = m;

		if (hasSurrogates) {
			delimiterCodePoints = new int[count];
			for (int i = 0, j = 0; i < count; i++, j += Character.charCount(c)) {
				c = delimiters.codePointAt(j);
				delimiterCodePoints[i] = c;
			}
		}
	}

	public StringTokenizer(String str, String delim, boolean returnDelims) {
		currentPosition = 0;
		newPosition = -1;
		delimsChanged = false;
		this.str = str;
		maxPosition = str.length();
		delimiters = delim;
		retDelims = returnDelims;
		setMaxDelimCodePoint();
	}

	public StringTokenizer(String str, String delim) {
		this(str, delim, false);
	}

	public StringTokenizer(String str) {
		this(str, " \t\n\r\f", false);
	}

	private int skipDelimiters(int startPos) {
		if (delimiters == null) throw new NullPointerException();

		int position = startPos;
		while (!retDelims && position < maxPosition) {
			if (!hasSurrogates) {
				char c = str.charAt(position);
				if ((c > maxDelimCodePoint) || (delimiters.indexOf(c) < 0)) break;
				position++;
			} else {
				int c = str.codePointAt(position);
				if ((c > maxDelimCodePoint) || !isDelimiter(c)) break;
				position += Character.charCount(c);
			}
		}
		return position;
	}

	private int scanToken(int startPos) {
		int position = startPos;
		while (position < maxPosition) {
			if (!hasSurrogates) {
				char c = str.charAt(position);
				if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0)) break;
				position++;
			} else {
				int c = str.codePointAt(position);
				if ((c <= maxDelimCodePoint) && isDelimiter(c)) break;
				position += Character.charCount(c);
			}
		}
		if (retDelims && (startPos == position)) {
			if (!hasSurrogates) {
				char c = str.charAt(position);
				if ((c <= maxDelimCodePoint) && (delimiters.indexOf(c) >= 0)) position++;
			} else {
				int c = str.codePointAt(position);
				if ((c <= maxDelimCodePoint) && isDelimiter(c)) position += Character.charCount(c);
			}
		}
		return position;
	}

	private boolean isDelimiter(int codePoint) {
		for (int i = 0; i < delimiterCodePoints.length; i++) {
			if (delimiterCodePoints[i] == codePoint) return true;
		}
		return false;
	}

	public boolean hasMoreTokens() {
		newPosition = skipDelimiters(currentPosition);
		return (newPosition < maxPosition);
	}

	public String nextToken() {
		currentPosition = (newPosition >= 0 && !delimsChanged) ? newPosition : skipDelimiters(currentPosition);
		delimsChanged = false;
		newPosition = -1;
		if (currentPosition >= maxPosition) throw new NoSuchElementException();
		int start = currentPosition;
		currentPosition = scanToken(currentPosition);
		return str.substring(start, currentPosition);
	}

	public String nextToken(String delim) {
		delimiters = delim;
		delimsChanged = true;
		setMaxDelimCodePoint();
		return nextToken();
	}

	public boolean hasMoreElements() {
		return hasMoreTokens();
	}

	public Object nextElement() {
		return nextToken();
	}

	public int countTokens() {
		int count = 0;
		int currpos = currentPosition;
		while (currpos < maxPosition) {
			currpos = skipDelimiters(currpos);
			if (currpos >= maxPosition) break;
			currpos = scanToken(currpos);
			count++;
		}
		return count;
	}
}
