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

package java.nio.charset;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class CoderResult {

	private static final int CR_UNDERFLOW = 0;
	private static final int CR_OVERFLOW = 1;
	private static final int CR_ERROR_MIN = 2;
	private static final int CR_MALFORMED = 2;
	private static final int CR_UNMAPPABLE = 3;

	private static final String[] names = {"UNDERFLOW", "OVERFLOW", "MALFORMED", "UNMAPPABLE"};

	private final int type;
	private final int length;

	private CoderResult(int type, int length) {
		this.type = type;
		this.length = length;
	}

	public String toString() {
		String nm = names[type];
		return isError() ? nm + "[" + length + "]" : nm;
	}

	public boolean isUnderflow() {
		return (type == CR_UNDERFLOW);
	}

	public boolean isOverflow() {
		return (type == CR_OVERFLOW);
	}

	public boolean isError() {
		return (type >= CR_ERROR_MIN);
	}

	public boolean isMalformed() {
		return (type == CR_MALFORMED);
	}

	public boolean isUnmappable() {
		return (type == CR_UNMAPPABLE);
	}

	public int length() {
		if (!isError()) throw new UnsupportedOperationException();
		return length;
	}

	public static final CoderResult UNDERFLOW = new CoderResult(CR_UNDERFLOW, 0);

	public static final CoderResult OVERFLOW = new CoderResult(CR_OVERFLOW, 0);

	public static CoderResult malformedForLength(int length) {
		return new CoderResult(CR_MALFORMED, length);
	}

	public static CoderResult unmappableForLength(int length) {
		return new CoderResult(CR_UNMAPPABLE, length);
	}

	public void throwException() throws CharacterCodingException {
		switch (type) {
			case CR_UNDERFLOW:
				throw new BufferUnderflowException();
			case CR_OVERFLOW:
				throw new BufferOverflowException();
			case CR_MALFORMED:
				throw new MalformedInputException(length);
			case CR_UNMAPPABLE:
				throw new UnmappableCharacterException(length);
			default:
				assert false;
		}
	}

}
