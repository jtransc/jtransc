package com.jtransc.text;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.ArrayList;
import java.util.regex.MatchResult;

// Small footprint regular expressions used by String, not requiring full Pattern/Matcher
public class JTranscRegex {
	static public final class Pattern implements java.io.Serializable {
		public static final int UNIX_LINES = 0x01;
		public static final int CASE_INSENSITIVE = 0x02;
		public static final int COMMENTS = 0x04;
		public static final int MULTILINE = 0x08;
		public static final int LITERAL = 0x10;
		public static final int DOTALL = 0x20;
		public static final int UNICODE_CASE = 0x40;
		public static final int CANON_EQ = 0x80;
		public static final int UNICODE_CHARACTER_CLASS = 0x100;

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

	@HaxeAddMembers({
		"public var _pattern:String;",
		"public var _opts:String;",
		"public var _text:String;",
		"public var _ereg:EReg;",
		"public var _matches:Bool;",
		"public var _offset:Int = 0;",
		"public var _matchPos:Int = 0;",
		"public var _matchLen:Int = 0;",
		"" +
			"public function _find() {\n" +
			"\tvar r = this._ereg;\n" +
			"\tthis._matches = r.matchSub(this.{% FIELD com.jtransc.text.JTranscRegex$Matcher:text:Ljava/lang/String; %}._str, this._offset);\n" +
			"\tif (this._matches) {\n" +
			"\t\tvar rpos = r.matchedPos();\n" +
			"\t\tthis._matchPos = rpos.pos;\n" +
			"\t\tthis._matchLen = rpos.len;\n" +
			"\t\tthis._offset = rpos.pos + rpos.len;\n" +
			"\t} else {\n" +
			"\t\tthis._matchPos = 0;\n" +
			"\t\tthis._matchLen = 0;\n" +
			"\t}\n" +
			"\treturn this._matches;\n" +
			"}"
		//public static final int UNIX_LINES = 0x01;
		//public static final int CASE_INSENSITIVE = 0x02;
		//public static final int COMMENTS = 0x04;
		//public static final int MULTILINE = 0x08;
		//public static final int LITERAL = 0x10;
		//public static final int DOTALL = 0x20;
		//public static final int UNICODE_CASE = 0x40;
		//public static final int CANON_EQ = 0x80;
		//public static final int UNICODE_CHARACTER_CLASS = 0x100;
		//i case insensitive matching
		//g global replace or split, see below
		//m multiline matching, ^ and $ represent the beginning and end of a line
		//s the dot . will also match newlines (Neko, C++, PHP, Flash and Java targets only)
		//u use UTF-8 matching (Neko and C++ targets only)
	})
	static public final class Matcher {
		private Pattern parent;
		private String pattern;
		private int flags;
		private String text;

		Matcher(Pattern parent, CharSequence text) {
			this.parent = parent;
			this.pattern = parent.pattern();
			this.flags = parent.flags();
			this.text = text.toString();
			_init();
		}

		@HaxeMethodBody("" +
			"var opts = '';\n" +
			"var flags = this.{% FIELD com.jtransc.text.JTranscRegex$Matcher:flags:I %};\n" +
			"var pattern = this.{% FIELD com.jtransc.text.JTranscRegex$Matcher:pattern:Ljava/lang/String; %};\n" +
			"var text = this.{% FIELD com.jtransc.text.JTranscRegex$Matcher:text:Ljava/lang/String; %};\n" +
			"if ((flags & 0x02) != 0) opts += 'i';\n" +
			"if ((flags & 0x08) != 0) opts += 'm';\n" +
			//"if ((this.flags & 0x20) != 0) opts += 's';\n" + // dotall default on javascript
			"this._pattern = pattern._str;\n" +
			"this._opts = opts;\n" +
			"this._text = text._str;\n" +
			"this._ereg = new EReg(pattern._str, opts);\n" +
			"this._matches = (new EReg('^' + pattern._str + '$', opts)).match(text._str);"
		)
		private void _init() {
		}

		public Pattern pattern() {
			return this.parent;
		}

		@HaxeMethodBody("return this._matchPos;")
		native public int start();

		public int start(int group) {
			if (group == 0) return start();
			JTranscSystem.debugger();
			throw new Error("No implemented Matcher.start(int group) with group != 0");
		}

		@HaxeMethodBody("return this._matchPos + this._matchLen;")
		native public int end();

		public int end(int group) {
			if (group == 0) return end();
			JTranscSystem.debugger();
			throw new Error("No implemented Matcher.end(int group) with group != 0");
		}

		@HaxeMethodBody("return HaxeNatives.str(this._ereg.matched(0));")
		native public String group();

		@HaxeMethodBody("return HaxeNatives.str(this._ereg.matched(p0));")
		native public String group(int group);

		@HaxeMethodBody("return this._matches;")
		native public boolean matches();

		@HaxeMethodBody("return _find();")
		native public boolean find();

		@HaxeMethodBody("this._offset = p0; return _find();")
		native public boolean find(int start);

		@HaxeMethodBody("return N.str(new EReg(this._pattern, this._opts + 'g').replace(this._text, p0._str));")
		native public String replaceAll(String replacement);

		@HaxeMethodBody("return N.str(new EReg(this._pattern, this._opts).replace(this._text, p0._str));")
		native public String replaceFirst(String replacement);

	}
}

