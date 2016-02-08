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

    native public int start();

    public int start(int group) {
        if (group == 0) return start();
        throw new Error("No implemented Matcher.start(int group) with group != 0");
    }

    native public int start(String name);

    native public int end();

    public int end(int group) {
        if (group == 0) return end();
        throw new Error("No implemented Matcher.end(int group) with group != 0");
    }

    native public int end(String name);

    native public String group();

    native public String group(int group);

    native public String group(String name);

    native public int groupCount();

    native public boolean matches();

    native public boolean find();

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
