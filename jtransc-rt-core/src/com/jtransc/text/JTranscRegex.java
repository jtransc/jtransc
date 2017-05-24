package com.jtransc.text;

import com.jtransc.JTranscSystem;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

import java.util.ArrayList;

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

			//int count = 1000;
			while (m.find()) {
				//System.out.println(m.start() + ":" + m.end());
				//if (count-- <= 0) {
				//	throw new RuntimeException("error!");
				//}
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
			"\tthis._matches = r.matchSub(this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:text:Ljava/lang/String; %}._str, this._offset);\n" +
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
	})
	static public final class Matcher {
		private Pattern parent;
		private String pattern;
		private int flags;
		private String flagsString;
		private String text;
		private String subtext;
		private int matchStart = 0;
		private int matchEnd = 0;
		//private String[] groups = null;

		Matcher(Pattern parent, CharSequence text) {
			this.parent = parent;
			this.pattern = parent.pattern();
			this.flags = parent.flags();
			this.text = text.toString();
			this.subtext = this.text;
			flagsString = "";
			if ((flags & 0x02) != 0) flagsString += "i";
			if ((flags & 0x08) != 0) flagsString += "m";
			_init();
		}

		@HaxeMethodBody("" +
			"var opts = '';\n" +
			"var flags = this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:flags %};\n" +
			"var pattern = this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:pattern %};\n" +
			"var text = this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:text %};\n" +
			"if ((flags & 0x02) != 0) opts += 'i';\n" +
			"if ((flags & 0x08) != 0) opts += 'm';\n" +
			//"if ((this.flags & 0x20) != 0) opts += 's';\n" + // dotall default on javascript
			"this._pattern = pattern._str;\n" +
			"this._opts = opts;\n" +
			"this._text = text._str;\n" +
			"this._ereg = new EReg(pattern._str, opts);\n" +
			"this._matches = (new EReg('^' + pattern._str + '$', opts)).match(text._str);"
		)
		@JTranscMethodBody(target = "js", value = {
			"this._ereg = new RegExp(N.istr(this._pattern), N.istr(this._flagsString + 'g'));",
			"var flags = this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:flags %};",
			"var opts = '';",
			"if ((flags & 0x02) != 0) opts += 'i';",
			"if ((flags & 0x08) != 0) opts += 'm';",
			"this._opts = opts;",
		})
		private void _init() {
		}

		public Pattern pattern() {
			return this.parent;
		}

		@HaxeMethodBody("return this._matchPos;")
		public int start() {
			return this.matchStart;
		}

		public int start(int group) {
			if (group == 0) return start();
			JTranscSystem.debugger();
			throw new Error("No implemented Matcher.start(int group) with group != 0");
		}

		@HaxeMethodBody("return this._matchPos + this._matchLen;")
		public int end() {
			return this.matchEnd;
		}

		public int end(int group) {
			if (group == 0) return end();
			JTranscSystem.debugger();
			throw new Error("No implemented Matcher.end(int group) with group != 0");
		}

		@HaxeMethodBody("return N.str(this._ereg.matched(0));")
		public String group() {
			return group(0);
		}

		@HaxeMethodBody("return N.str(this._ereg.matched(p0));")
		@JTranscMethodBody(target = "js", value = "return N.str(this._groups[p0]);")
		native public String group(int group);

		@HaxeMethodBody("return this._matches;")
		@JTranscMethodBody(target = "js", value = "return (new RegExp('^' + N.istr(this._pattern) + '$', opts)).test(N.istr(this._text));")
		native public boolean matches();

		@HaxeMethodBody("return _find();")
		public boolean find() {
			return _find();
		}

		@HaxeMethodBody("this._offset = p0; return _find();")
		public boolean find(int start) {
			this.subtext = text.substring(start);
			return _find();
		}

		@JTranscMethodBody(target = "js", value = {
			"this._groups = this._ereg.exec(N.istr(this._subtext));",
			"this._matchStart = (this._groups) ? this._groups.index : -1;",
			"this._matchEnd   = this._ereg.lastIndex;",
			"return this._groups != null;",
		})
		native private boolean _find();

		public String replaceAll(String replacement) {
			return replaceFirstAll(replacement, true);
		}

		public String replaceFirst(String replacement) {
			return replaceFirstAll(replacement, false);
		}

		@HaxeMethodBody("return N.str(new EReg(this._pattern, p1 ? (this._opts + 'g') : (this._opts)).replace(this._text, p0._str));")
		@JTranscMethodBody(target = "js", value = {
			"var opts = p1 ? (this._opts + 'g') : (this._opts);",
			"var text = N.istr(this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:text %});",
			"var pattern = N.istr(this{% IFIELD com.jtransc.text.JTranscRegex$Matcher:pattern %});",
			"return N.str(text.replace(new RegExp(pattern, opts), N.istr(p0)));"
		})
		native private String replaceFirstAll(String replacement, boolean all);

	}
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
}

