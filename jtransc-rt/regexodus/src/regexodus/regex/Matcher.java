package regexodus.regex;

import regexodus.PerlSubstitution;
import regexodus.Replacer;

/**
 * Created by Tommy Ettinger on 6/7/2016.
 */
public class Matcher implements MatchResult {
    private regexodus.Matcher matcher;
    private Pattern pattern;

    /**
     * No default constructor.
     */
    Matcher() {
    }

    /**
     * All matchers have the state used by Pattern during a match.
     */
    Matcher(Pattern parent, CharSequence text) {
        pattern = parent;
        matcher = new regexodus.Matcher(parent.internal, text);
    }

    /**
     * Returns the pattern that is interpreted by this matcher.
     *
     * @return  The pattern for which this matcher was created
     */
    public Pattern pattern() {
        return pattern;
    }

    /**
     * Returns the match state of this matcher as a {@link MatchResult}.
     * The result is unaffected by subsequent operations performed upon this
     * matcher.
     *
     * @return  a <code>MatchResult</code> with the state of this matcher
     * @since 1.5
     */
    public MatchResult toMatchResult() {
        return matcher.copy();
    }

    /**
     * Changes the <tt>Pattern</tt> that this <tt>Matcher</tt> uses to
     * find matches with.
     *
     * <p> This method causes this matcher to lose information
     * about the groups of the last match that occurred. The
     * matcher's position in the input is maintained and its
     * last append position is unaffected.</p>
     *
     * @param  newPattern
     *         The new pattern used by this matcher
     * @return  This matcher
     * @throws  IllegalArgumentException
     *          If newPattern is <tt>null</tt>
     * @since 1.5
     */
    public Matcher usePattern(Pattern newPattern) {
        if (newPattern == null)
            throw new IllegalArgumentException("Pattern cannot be null");
        matcher.setPattern(newPattern.internal);
        return this;
    }

    /**
     * Resets this matcher.
     *
     * <p> Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero. The matcher's region is set to the
     * default region, which is its entire character sequence. The anchoring
     * and transparency of this matcher's region boundaries are unaffected.
     *
     * @return  This matcher
     */
    public Matcher reset() {
        matcher.flush();
        return this;
    }

    /**
     * Resets this matcher with a new input sequence.
     *
     * <p> Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero.  The matcher's region is set to
     * the default region, which is its entire character sequence.  The
     * anchoring and transparency of this matcher's region boundaries are
     * unaffected.
     *
     * @param  input
     *         The new input character sequence
     *
     * @return  This matcher
     */
    public Matcher reset(CharSequence input) {
        matcher.setTarget(input);
        return reset();
    }

    /**
     * Returns the start index of the match.
     *
     * @return The index of the first character matched
     */
    @Override
    public int start() {
        return matcher.start();
    }

    /**
     * Returns the start index of the subsequence captured by the given group
     * during this match.
     * <br>
     * Capturing groups are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>start(0)</tt> is equivalent to
     * <i>m.</i><tt>start()</tt>.
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The index of the first character captured by the group,
     * or <tt>-1</tt> if the match was successful but the group
     * itself did not match anything
     */
    @Override
    public int start(int group) {
        return matcher.start(group);
    }

    /**
     * Returns the start index of the subsequence captured by the given
     * named-capturing group during the previous match operation.
     *
     * @param name The name of a named capturing group in this matcher's pattern
     * @return The index of the first character captured by the group,
     * or <tt>-1</tt> if the match was successful but the group
     * itself did not match anything
     */
    @Override
    public int start(String name) {
        return matcher.start(name);
    }

    /**
     * Returns the offset after the last character of the subsequence captured
     * by the given named-capturing group during the previous match operation.
     *
     * @param name The name of a named capturing group in this matcher's pattern
     * @return The offset after the last character captured by the group,
     * or <tt>-1</tt> if the match was successful
     * but the group itself did not match anything
     */
    @Override
    public int end(String name) {
        return matcher.end(1);
    }

    /**
     * Returns the offset after the last character matched.
     *
     * @return The offset after the last character matched
     */
    @Override
    public int end() {
        return matcher.end();
    }

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given group during this match.
     * <br>
     * Capturing groups are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>end(0)</tt> is equivalent to
     * <i>m.</i><tt>end()</tt>.
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The offset after the last character captured by the group,
     * or <tt>-1</tt> if the match was successful
     * but the group itself did not match anything
     */
    @Override
    public int end(int group) {
        return matcher.end(group);
    }

    /**
     * Returns the input subsequence matched by the previous match.
     * <br>
     * For a matcher <i>m</i> with input sequence <i>s</i>,
     * the expressions <i>m.</i><tt>group()</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(),</tt>&nbsp;<i>m.</i><tt>end())</tt>
     * are equivalent.
     * <br>
     * Note that some patterns, for example <tt>a*</tt>, match the empty
     * string.  This method will return the empty string when the pattern
     * successfully matches the empty string in the input.
     *
     * @return The (possibly empty) subsequence matched by the previous match,
     * in string form
     */
    @Override
    public String group() {
        return matcher.group();
    }

    /**
     * Returns the input subsequence captured by the given group during the
     * previous match operation.
     * <br>
     * For a matcher <i>m</i>, input sequence <i>s</i>, and group index
     * <i>g</i>, the expressions <i>m.</i><tt>group(</tt><i>g</i><tt>)</tt> and
     * <i>s.</i><tt>substring(</tt><i>m.</i><tt>start(</tt><i>g</i><tt>),</tt>&nbsp;<i>m.</i><tt>end(</tt><i>g</i><tt>))</tt>
     * are equivalent.
     * <br>
     * Capturing groups are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <tt>m.group(0)</tt> is equivalent to <tt>m.group()</tt>.
     * <br>
     * If the match was successful but the group specified failed to match
     * any part of the input sequence, then <tt>null</tt> is returned. Note
     * that some groups, for example <tt>(a*)</tt>, match the empty string.
     * This method will return the empty string when such a group successfully
     * matches the empty string in the input.
     *
     * @param group The index of a capturing group in this matcher's pattern
     * @return The (possibly empty) subsequence captured by the group
     * during the previous match, or <tt>null</tt> if the group
     * failed to match part of the input
     */
    @Override
    public String group(int group) {
        return matcher.group(group);
    }

    /**
     * Returns the input subsequence captured by the given named group during the
     * previous match operation.
     * <br>
     * Like {@link #group(int) group} but for named groups instead of numbered.
     * @param  name
     *         The name of a capturing group in this matcher's pattern
     *
     * @return  The (possibly empty) subsequence captured by the group
     *          during the previous match, or <tt>null</tt> if the group
     *          failed to match part of the input
     */
    public String group(String name)
    {
        return matcher.group(name);
    }

    /**
     * Returns the number of capturing groups in this match result's pattern.
     * <br>
     * Group zero denotes the entire pattern by convention. It is not
     * included in this count.
     * <br>
     * Any non-negative integer smaller than or equal to the value
     * returned by this method is guaranteed to be a valid group index for
     * this matcher.
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    @Override
    public int groupCount() {
        return matcher.groupCount();
    }


    /**
     * Attempts to match the entire region against the pattern.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.  </p>
     *
     * @return  <tt>true</tt> if, and only if, the entire region sequence
     *          matches this matcher's pattern
     */
    public boolean matches() {
        return matcher.matches();
    }

    /**
     * Attempts to find the next subsequence of the input sequence that matches
     * the pattern.
     *
     * <p> This method starts at the beginning of this matcher's region, or, if
     * a previous invocation of the method was successful and the matcher has
     * not since been reset, at the first character not matched by the previous
     * match.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.  </p>
     *
     * @return  <tt>true</tt> if, and only if, a subsequence of the input
     *          sequence matches this matcher's pattern
     */
    public boolean find() {
        return matcher.find();
    }

    /**
     * Resets this matcher and then attempts to find the next subsequence of
     * the input sequence that matches the pattern, starting at the specified
     * index.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods, and subsequent
     * invocations of the {@link #find()} method will start at the first
     * character not matched by this match.  </p>
     *
     * @throws  IndexOutOfBoundsException
     *          If start is less than zero or if start is greater than the
     *          length of the input sequence.
     *
     * @return  <tt>true</tt> if, and only if, a subsequence of the input
     *          sequence starting at the given index matches this matcher's
     *          pattern
     */
    public boolean find(int start) {
        int limit = matcher.targetEnd();
        if ((start < 0) || (start > limit))
            throw new IndexOutOfBoundsException("Illegal start index");
        reset();
        matcher.setPosition(start);
        return matcher.find();
    }

    /**
     * Attempts to match the input sequence, starting at the beginning of the
     * region, against the pattern.
     *
     * <p> Like the {@link #matches matches} method, this method always starts
     * at the beginning of the region; unlike that method, it does not
     * require that the entire region be matched.
     *
     * <p> If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.  </p>
     *
     * @return  <tt>true</tt> if, and only if, a prefix of the input
     *          sequence matches this matcher's pattern
     */
    public boolean lookingAt() {
        return matcher.search(regexodus.Matcher.ACCEPT_INCOMPLETE);
    }

    /**
     * Returns a literal replacement <code>String</code> for the specified
     * <code>String</code>.
     *
     * This method produces a <code>String</code> that will work
     * as a literal replacement <code>s</code> in the
     * <code>appendReplacement</code> method of the {@link Matcher} class.
     * The <code>String</code> produced will match the sequence of characters
     * in <code>s</code> treated as a literal sequence. Slashes ('\') and
     * dollar signs ('$') will be given no special meaning.
     *
     * @param  s The string to be literalized
     * @return  A literal string replacement
     */
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

    /**
     * Implements a non-terminal append-and-replace step.
     *
     * <p> This method performs the following actions: </p>
     *
     * <ol>
     *
     *   <li><p> It reads characters from the input sequence, starting at the
     *   append position, and appends them to the given string buffer.  It
     *   stops after reading the last character preceding the previous match,
     *   that is, the character at index {@link
     *   #start()}&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>.  </p></li>
     *
     *   <li><p> It appends the given replacement string to the string buffer.
     *   </p></li>
     *
     *   <li><p> It sets the append position of this matcher to the index of
     *   the last character matched, plus one, that is, to {@link #end()}.
     *   </p></li>
     *
     * </ol>
     *
     * <p> The replacement string may contain references to subsequences
     * captured during the previous match: Each occurrence of
     * <tt>${</tt><i>name</i><tt>}</tt> or <tt>$</tt><i>g</i>
     * will be replaced by the result of evaluating the corresponding
     * {@link #group(String) group(name)} or {@link #group(int) group(g)</tt>}
     * respectively. For  <tt>$</tt><i>g</i><tt></tt>,
     * the first number after the <tt>$</tt> is always treated as part of
     * the group reference. Subsequent numbers are incorporated into g if
     * they would form a legal group reference. Only the numerals '0'
     * through '9' are considered as potential components of the group
     * reference. If the second group matched the string <tt>"foo"</tt>, for
     * example, then passing the replacement string <tt>"$2bar"</tt> would
     * cause <tt>"foobar"</tt> to be appended to the string buffer. A dollar
     * sign (<tt>$</tt>) may be included as a literal in the replacement
     * string by preceding it with a backslash (<tt>\$</tt>).
     *
     * <p> Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> This method is intended to be used in a loop together with the
     * {@link #appendTail appendTail} and {@link #find find} methods.  The
     * following code, for example, writes <tt>one dog two dogs in the
     * yard</tt> to the standard-output stream: </p>
     *
     * <blockquote><pre>
     * Pattern p = Pattern.compile("cat");
     * Matcher m = p.matcher("one cat two cats in the yard");
     * StringBuffer sb = new StringBuffer();
     * while (m.find()) {
     *     m.appendReplacement(sb, "dog");
     * }
     * m.appendTail(sb);
     * System.out.println(sb.toString());</pre></blockquote>
     *
     * @param  sb
     *         The target string buffer
     *
     * @param  replacement
     *         The replacement string
     *
     * @return  This matcher
     *
     * @throws  IllegalStateException
     *          If no match has yet been attempted,
     *          or if the previous match operation failed
     *
     * @throws  IllegalArgumentException
     *          If the replacement string refers to a named-capturing
     *          group that does not exist in the pattern
     *
     * @throws  IndexOutOfBoundsException
     *          If the replacement string refers to a capturing group
     *          that does not exist in the pattern
     */
    public Matcher appendReplacement(StringBuffer sb, String replacement) {
        Replacer rep = pattern.internal.replacer(replacement);
        Replacer.replaceStep(matcher, new PerlSubstitution(replacement), Replacer.wrap(sb));
        return this;
    }

    /**
     * Implements a terminal append-and-replace step.
     *
     * <p> This method reads characters from the input sequence, starting at
     * the append position, and appends them to the given string buffer.  It is
     * intended to be invoked after one or more invocations of the {@link
     * #appendReplacement appendReplacement} method in order to copy the
     * remainder of the input sequence.  </p>
     *
     * @param  sb
     *         The target string buffer
     *
     * @return  The target string buffer
     */
    public StringBuffer appendTail(StringBuffer sb) {
        matcher.getGroup(regexodus.MatchResult.TARGET, Replacer.wrap(sb));
        return sb;
    }

    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * <p> This method first resets this matcher.  It then scans the input
     * sequence looking for matches of the pattern.  Characters that are not
     * part of any match are appended directly to the result string; each match
     * is replaced in the result by the replacement string.  The replacement
     * string may contain references to captured subsequences as in the {@link
     * #appendReplacement appendReplacement} method.
     *
     * <p> Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     * <p> Given the regular expression <tt>a*b</tt>, the input
     * <tt>"aabfooaabfooabfoob"</tt>, and the replacement string
     * <tt>"-"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"-foo-foo-foo-"</tt>.
     *
     * <p> Invoking this method changes this matcher's state.  If the matcher
     * is to be used in further matching operations then it should first be
     * reset.  </p>
     *
     * @param  replacement
     *         The replacement string
     *
     * @return  The string constructed by replacing each matching subsequence
     *          by the replacement string, substituting captured subsequences
     *          as needed
     */
    public String replaceAll(String replacement) {
        reset();
        return matcher.replaceAll(replacement);
    }

    /**
     * Replaces the first subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     * <br>
     * This method first resets this matcher.  It then scans the input
     * sequence looking for a match of the pattern.  Characters that are not
     * part of the match are appended directly to the result string; the match
     * is replaced in the result by the replacement string.  The replacement
     * string may contain references to captured subsequences.
     * <br>
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     * <br>
     * Given the regular expression <tt>dog</tt>, the input
     * <tt>"zzzdogzzzdogzzz"</tt>, and the replacement string
     * <tt>"cat"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"zzzcatzzzdogzzz"</tt>.
     * <br>
     * Invoking this method changes this matcher's state.  If the matcher
     * is to be used in further matching operations then it should first be
     * reset.
     *
     * @param  replacement
     *         The replacement string
     * @return  The string constructed by replacing the first matching
     *          subsequence by the replacement string, substituting captured
     *          subsequences as needed
     */
    public String replaceFirst(String replacement) {
        if (replacement == null)
            throw new NullPointerException("replacement");
        reset();
        return matcher.replaceFirst(replacement);
    }

    /**
     * Sets the limits of this matcher's region. The region is the part of the
     * input sequence that will be searched to find a match. Invoking this
     * method resets the matcher, and then sets the region to start at the
     * index specified by the <code>start</code> parameter and end at the
     * index specified by the <code>end</code> parameter.
     *
     * @param  start
     *         The index to start searching at (inclusive)
     * @param  end
     *         The index to end searching at (exclusive)
     * @throws  IndexOutOfBoundsException
     *          If start or end is less than zero, if
     *          start is greater than the length of the input sequence, if
     *          end is greater than the length of the input sequence, or if
     *          start is greater than end.
     * @return  this matcher
     * @since 1.5
     */
    public Matcher region(int start, int end) {
        if ((start < 0) || (start < 0))
            throw new IndexOutOfBoundsException("start");
        if ((end < 0) || (end > matcher.dataEnd()))
            throw new IndexOutOfBoundsException("end");
        if (start > end)
            throw new IndexOutOfBoundsException("start > end");
        matcher.setTarget(matcher.targetChars(), start, end - start);
        return this;
    }

    /**
     * Reports the start index of this matcher's region. The
     * searches this matcher conducts are limited to finding matches
     * within {@link #regionStart regionStart} (inclusive) and
     * {@link #regionEnd regionEnd} (exclusive).
     *
     * @return  The starting point of this matcher's region
     * @since 1.5
     */
    public int regionStart() {
        return matcher.targetStart();
    }

    /**
     * Reports the end index (exclusive) of this matcher's region.
     * The searches this matcher conducts are limited to finding matches
     * within {@link #regionStart regionStart} (inclusive) and
     * {@link #regionEnd regionEnd} (exclusive).
     *
     * @return  the ending point of this matcher's region
     * @since 1.5
     */
    public int regionEnd() {
        return matcher.targetEnd();
    }

    /**
     */
    public boolean hasTransparentBounds() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     */
    public Matcher useTransparentBounds(boolean b) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     * @return always throws an Exception.
     * @throws UnsupportedOperationException every time.
     */
    public boolean hasAnchoringBounds() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     * @param  b a boolean indicating whether or not to use anchoring bounds.
     * @return this matcher
     */
    public Matcher useAnchoringBounds(boolean b) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * <p>Returns the string representation of this matcher. The
     * string representation of a <code>Matcher</code> contains information
     * that may be useful for debugging. The exact format is unspecified.
     *
     * @return  The string representation of this matcher
     */
    public String toString() {
        return matcher.toString();
    }

    /**
     * Not implemented.
     */
    public boolean hitEnd() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Not implemented.
     */
    public boolean requireEnd() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
