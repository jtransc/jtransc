package regexodus.regex;

/**
 * Created by Tommy Ettinger on 6/7/2016.
 */
public interface MatchResult {

    /**
     * Returns the start index of the match.
     * @return  The index of the first character matched
     */
    int start();

    /**
     * Returns the start index of the subsequence captured by the given group
     * during this match.
     * <br>
     * Capturing groups are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>start(0)</tt> is equivalent to
     * <i>m.</i><tt>start()</tt>.
     * @param  group
     *         The index of a capturing group in this matcher's pattern
     * @return  The index of the first character captured by the group,
     *          or <tt>-1</tt> if the match was successful but the group
     *          itself did not match anything
     */
    int start(int group);

    /**
     * Returns the start index of the subsequence captured by the given
     * named-capturing group during the previous match operation.
     *
     * @param  name
     *         The name of a named capturing group in this matcher's pattern
     * @return  The index of the first character captured by the group,
     *          or <tt>-1</tt> if the match was successful but the group
     *          itself did not match anything
     */
    int start(String name);

    /**
     * Returns the offset after the last character matched.
     * @return  The offset after the last character matched
     */
    int end();

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given group during this match.
     * <br>
     * Capturing groups are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <i>m.</i><tt>end(0)</tt> is equivalent to
     * <i>m.</i><tt>end()</tt>.
     *
     * @param  group
     *         The index of a capturing group in this matcher's pattern
     *
     * @return  The offset after the last character captured by the group,
     *          or <tt>-1</tt> if the match was successful
     *          but the group itself did not match anything
     */
    int end(int group);

    /**
     * Returns the offset after the last character of the subsequence captured
     * by the given named-capturing group during the previous match operation.
     *
     * @param  name
     *         The name of a named capturing group in this matcher's pattern
     *
     * @return  The offset after the last character captured by the group,
     *          or <tt>-1</tt> if the match was successful
     *          but the group itself did not match anything
     */
    int end(String name);

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
     *         in string form
     */
    String group();

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
     * @param  group
     *         The index of a capturing group in this matcher's pattern
     *
     * @return  The (possibly empty) subsequence captured by the group
     *          during the previous match, or <tt>null</tt> if the group
     *          failed to match part of the input
     */
    String group(int group);

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
    int groupCount();
}
