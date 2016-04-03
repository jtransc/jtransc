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

package java.util.regex;

import jtransc.JTranscSystem;

public class PatternSyntaxException extends IllegalArgumentException {
	private final String desc;
	private final String pattern;
	private final int index;

	public PatternSyntaxException(String desc, String regex, int index) {
		this.desc = desc;
		this.pattern = regex;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public String getDescription() {
		return desc;
	}

	public String getPattern() {
		return pattern;
	}

	public String getMessage() {
		String nl = JTranscSystem.lineSeparator();
		String sb = "";
		sb += desc;
		if (index >= 0) sb += " near index " + index;
		sb += nl + pattern;
		if (index >= 0) {
			sb += nl;
			for (int i = 0; i < index; i++) sb += " ";
			sb += "^";
		}
		return sb;
	}

}
