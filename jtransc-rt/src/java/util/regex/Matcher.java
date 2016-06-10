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

import regexodus.PerlSubstitution;
import regexodus.Replacer;

/**
 * Created by Tommy Ettinger on 6/7/2016.
 */
public final class Matcher implements MatchResult {
	public regexodus.Matcher matcher;
	public regexodus.Pattern internalPattern;
	public Pattern pattern;
	public CharSequence text;

	Matcher(Pattern parent, CharSequence text) {
		this.pattern = parent;
		this.internalPattern = parent.internal;
		this.text = text;
		this.matcher = new regexodus.Matcher(internalPattern, text);
	}

	private Matcher() {
	}

	private Matcher copy() {
		// @TODO: Copy!
		return this;
	}

	public Pattern pattern() {
		return this.pattern;
	}

	public MatchResult toMatchResult() {
		return copy();
	}

	public Matcher usePattern(Pattern newPattern) {
		this.matcher.setPattern(newPattern.internal);
		this.pattern = newPattern;
		this.internalPattern = newPattern.internal;
		return this;
	}

	public Matcher reset() {
		this.matcher.flush();
		return this;
	}

	public Matcher reset(CharSequence input) {
		matcher.setTarget(input);
		return reset();
	}

	public int start() {
		return matcher.start();
	}

	public int start(int group) {
		return matcher.start(group);
	}

	public int end() {
		return matcher.end();
	}

	public int end(int group) {
		return matcher.end(group);
	}

	public String group() {
		return matcher.group();
	}

	public String group(int group) {
		return matcher.group(group);
	}

	public String group(String name) {
		return matcher.group(name);
	}

	public int groupCount() {
		return matcher.groupCount();
	}

	public boolean matches() {
		return matcher.matches();
	}

	public boolean find() {
		return matcher.find();
	}

	public boolean find(int start) {
		int limit = matcher.targetEnd();
		if ((start < 0) || (start > limit))
			throw new IndexOutOfBoundsException("Illegal start index");
		reset();
		matcher.setPosition(start);
		return matcher.find();
	}

	public boolean lookingAt() {
		return matcher.search(regexodus.Matcher.ACCEPT_INCOMPLETE);
	}

	public static String quoteReplacement(String s) {
		if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1))
			return s;
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\\' || c == '$') {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public Matcher appendReplacement(StringBuffer sb, String replacement) {
		Replacer rep = internalPattern.replacer(replacement);
		Replacer.replaceStep(matcher, new PerlSubstitution(replacement), Replacer.wrap(sb));
		return this;
	}

	public StringBuffer appendTail(StringBuffer sb) {
		matcher.getGroup(regexodus.MatchResult.TARGET, Replacer.wrap(sb));
		return sb;
	}

	public String replaceAll(String replacement) {
		reset();
		return matcher.replaceAll(replacement);
	}

	public String replaceFirst(String replacement) {
		if (replacement == null)
			throw new NullPointerException("replacement");
		reset();
		return matcher.replaceFirst(replacement);
	}

	public Matcher region(int start, int end) {
		if ((start < 0) || (start > matcher.targetEnd()))
			throw new IndexOutOfBoundsException("start");
		if ((end < 0) || (end > matcher.targetEnd()))
			throw new IndexOutOfBoundsException("end");
		if (start > end)
			throw new IndexOutOfBoundsException("start > end");
		matcher.setTarget(matcher.target(), start, end - start);
		return this;
	}

	public int regionStart() {
		return matcher.targetStart();
	}

	public int regionEnd() {
		return matcher.targetEnd();
	}

	public boolean hasTransparentBounds() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Matcher useTransparentBounds(boolean b) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean hasAnchoringBounds() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public Matcher useAnchoringBounds(boolean b){
		throw new UnsupportedOperationException("Not implemented");
	}

	public String toString() {
		return matcher.toString();
	}

	public boolean hitEnd() {
		throw new UnsupportedOperationException("Not implemented");
	}

	public boolean requireEnd() {
		throw new UnsupportedOperationException("Not implemented");
	}

	///////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////

	// @TODO: Implement this!
	public int start(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	// @TODO: Implement this!
	public int end(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

}
