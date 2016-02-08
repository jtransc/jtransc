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

public class FormatFlagsConversionMismatchException extends IllegalFormatException {
	private String f;

	private char c;

	public FormatFlagsConversionMismatchException(String f, char c) {
		if (f == null) throw new NullPointerException();
		this.f = f;
		this.c = c;
	}

	public String getFlags() {
		return f;
	}

	public char getConversion() {
		return c;
	}

	public String getMessage() {
		return "Conversion = " + c + ", Flags = " + f;
	}
}
