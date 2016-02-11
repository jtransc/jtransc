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

package jtransc.internal;

import jtransc.annotation.JTranscInvisible;

import java.util.Locale;

@JTranscInvisible
public class IntJTranscStrings {
	native public static String format(Locale l, String format, Object... args);

	static public char[] getChars(String s, int offset, int len) {
		char[] out = new char[len];
		for (int n = 0; n < len; n++) out[n] = s.charAt(offset + n);
		return out;
	}


	/*
	public static String format(Locale l, String format, Object... args) {
		return format + "@TODO:String.format:";
	}
	*/
}
