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

public class IllegalFormatConversionException extends IllegalFormatException {
	private char c;
	private Class<?> arg;

	public IllegalFormatConversionException(char c, Class<?> arg) {
		if (arg == null) throw new NullPointerException();
		this.c = c;
		this.arg = arg;
	}

	public char getConversion() {
		return c;
	}

	public Class<?> getArgumentClass() {
		return arg;
	}

	public String getMessage() {
		return String.format("%c != %s", c, arg.getName());
	}
}
