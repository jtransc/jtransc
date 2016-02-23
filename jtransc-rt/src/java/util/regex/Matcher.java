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

import jtransc.annotation.haxe.HaxeAddMembers;
import jtransc.annotation.haxe.HaxeMethodBody;

@HaxeAddMembers({
        "public var _ereg:EReg;",
        "public var _matches:Bool;",
        "public var _offset:Int = 0;",
        "public var _matchPos:Int = 0;",
        "public var _matchLen:Int = 0;",
        "public function _find() {\n" +
                "\tvar r = this._ereg;\n" +
                "\tthis._matches = r.matchSub(this.text._str, this._offset);\n" +
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
public final class Matcher implements MatchResult {
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

    @HaxeMethodBody(
            "var opts = '';\n" +
            "if ((this.flags & 0x02) != 0) opts += 'i';\n" +
            "if ((this.flags & 0x08) != 0) opts += 'm';\n" +
            "if ((this.flags & 0x20) != 0) opts += 's';\n" +
            "this._ereg = new EReg(this.pattern._str, opts);\n" +
            "this._matches = this._ereg.match(this.text._str);"
    )
	private void _init() {
	}

	public Pattern pattern() {
		return this.parent;
	}

	public MatchResult toMatchResult() {
		return this;
	}

	native public Matcher usePattern(Pattern newPattern);

	native public Matcher reset();

	public Matcher reset(CharSequence input) {
		this.text = input.toString();
		this.reset();
		return this;
	}

    @HaxeMethodBody("return this._matchPos;")
	native public int start();

	public int start(int group) {
		if (group == 0) return start();
		throw new Error("No implemented Matcher.start(int group) with group != 0");
	}

	native public int start(String name);

    @HaxeMethodBody("return this._matchPos + this._matchLen;")
	native public int end();

	public int end(int group) {
		if (group == 0) return end();
		throw new Error("No implemented Matcher.end(int group) with group != 0");
	}

	native public int end(String name);

    @HaxeMethodBody("return HaxeNatives.str(this._ereg.matched(0));")
	native public String group();

    @HaxeMethodBody("return HaxeNatives.str(this._ereg.matched(p0));")
	native public String group(int group);

	native public String group(String name);

	native public int groupCount();

    @HaxeMethodBody("return this._matches;")
	native public boolean matches();

    @HaxeMethodBody("return _find();")
	native public boolean find();

    @HaxeMethodBody("this._offset = p0; return _find();")
	native public boolean find(int start);

	native public boolean lookingAt();

	native public static String quoteReplacement(String s);

	native public Matcher appendReplacement(StringBuffer sb, String replacement);

	native public StringBuffer appendTail(StringBuffer sb);

	native public String replaceAll(String replacement);

	native public String replaceFirst(String replacement);

	native public Matcher region(int start, int end);

	native public int regionStart();

	native public int regionEnd();

	native public boolean hasTransparentBounds();

	native public Matcher useTransparentBounds(boolean b);

	native public boolean hasAnchoringBounds();

	native public Matcher useAnchoringBounds(boolean b);

	native public String toString();

	native public boolean hitEnd();

	native public boolean requireEnd();
}
