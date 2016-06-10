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

import regexodus.REFlags;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 6/7/2016.
 */
public final class Pattern implements java.io.Serializable {
	public static final int UNIX_LINES = 0x01;
	public static final int CASE_INSENSITIVE = 0x02;
	public static final int COMMENTS = 0x04;
	public static final int MULTILINE = 0x08;
	public static final int LITERAL = 0x10;
	public static final int DOTALL = 0x20;
	public static final int UNICODE_CASE = 0x40;
	public static final int CANON_EQ = 0x80;
	public static final int UNICODE_CHARACTER_CLASS = 0x100;

	public regexodus.Pattern internal;

	public static Pattern compile(String regex) {
		return new Pattern(regex, 0);
	}

	public static Pattern compile(String regex, int flags) {
		return new Pattern(regex, flags);
	}

	private String pattern;
	private int flags;

	private Pattern(String pattern, int flags) {
		this.pattern = pattern;
		this.flags = flags;

		internal = regexodus.Pattern.compile(pattern, convertFlags(flags));
	}

	// @TODO: Maybe we could match flags to avoid conversion at all!
	static private int convertFlags(int flags) {
		int fm = 0;
		fm |= (flags & CASE_INSENSITIVE) != 0 ? REFlags.IGNORE_CASE : 0;
		fm |= (flags & DOTALL) != 0 ? REFlags.DOTALL : 0;
		fm |= (flags & COMMENTS) != 0 ? REFlags.IGNORE_SPACES : 0;
		fm |= (flags & MULTILINE) != 0 ? REFlags.MULTILINE : 0;
		fm |= (flags & UNICODE_CHARACTER_CLASS) != 0 ? REFlags.UNICODE : 0;
		return fm;
	}

	public String pattern() {
		return pattern;
	}

	public int flags() {
		return flags;
	}

	public String toString() {
		return pattern;
	}

	public Matcher matcher(CharSequence input) {
		return new Matcher(this, input);
	}

	public static boolean matches(String regex, CharSequence input) {
		return compile(regex).matcher(input).matches();
	}

	public String[] split(CharSequence input, int limit) {
		int index = 0;
		boolean matchLimited = limit > 0;
		ArrayList<String> matchList = new ArrayList<String>();
		Matcher m = matcher(input);

		while (m.find()) {
			if (!matchLimited || matchList.size() < limit - 1) {
				if (index == 0 && index == m.start() && m.start() == m.end()) continue;
				String match = input.subSequence(index, m.start()).toString();
				matchList.add(match);
				index = m.end();
			} else if (matchList.size() == limit - 1) { // last one
				String match = input.subSequence(index, input.length()).toString();
				matchList.add(match);
				index = m.end();
			}
		}

		if (index == 0) return new String[]{input.toString()};

		if (!matchLimited || matchList.size() < limit) {
			matchList.add(input.subSequence(index, input.length()).toString());
		}

		int resultSize = matchList.size();
		if (limit == 0) {
			while (resultSize > 0 && matchList.get(resultSize - 1).equals("")) resultSize--;
		}
		String[] result = new String[resultSize];
		return matchList.subList(0, resultSize).toArray(result);
	}

	public String[] split(CharSequence input) {
		return split(input, 0);
	}

	public static String quote(String s) {
		int slashEIndex = s.indexOf("\\E");
		if (slashEIndex == -1) return "\\Q" + s + "\\E";

		StringBuilder sb = new StringBuilder(s.length() * 2);
		sb.append("\\Q");
		slashEIndex = 0;
		int current = 0;
		while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
			sb.append(s.substring(current, slashEIndex));
			current = slashEIndex + 2;
			sb.append("\\E\\\\E\\Q");
		}
		sb.append(s.substring(current, s.length()));
		sb.append("\\E");
		return sb.toString();
	}
}
