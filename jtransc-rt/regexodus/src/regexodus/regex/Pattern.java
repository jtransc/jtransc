package regexodus.regex;

import regexodus.REFlags;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 6/7/2016.
 */
public class Pattern implements Serializable {
    private static final long serialVersionUID = -3628346657932720807L;

    public regexodus.Pattern internal;
    private int flags;

    /**
     * Not used; present for compatibility.
     */
    public static final int UNIX_LINES = 0x01;

    /**
     * Enables case-insensitive matching.
     *
     * <p> Unicode-aware case-insensitive matching is always enabled by this
     * flag, regardless of unicode flag status.
     *
     * <p> Case-insensitive matching can also be enabled via the embedded flag
     * expression&nbsp;<tt>(?i)</tt>.
     */
    public static final int CASE_INSENSITIVE = 0x02;

    /**
     * Permits whitespace and comments in pattern.
     *
     * <p> In this mode, whitespace is ignored, and embedded comments starting
     * with <tt>#</tt> are ignored until the end of a line.
     *
     * <p> Comments mode can also be enabled via the embedded flag
     * expression&nbsp;<tt>(?x)</tt>.
     */
    public static final int COMMENTS = 0x04;

    /**
     * Enables multiline mode.
     * <br>
     * In multiline mode the expressions <tt>^</tt> and <tt>$</tt> match
     * just after or just before, respectively, a line terminator or the end of
     * the input sequence.  By default these expressions only match at the
     * beginning and the end of the entire input sequence.
     * <br>
     * Multiline mode can also be enabled via the embedded flag
     * expression&nbsp;<tt>(?m)</tt>.
     */
    public static final int MULTILINE = 0x08;

    /**
     * Enables literal mode.
     * <br>
     * In literal mode, metacharacters are not interpreted at all, and they
     * match the exact, literal string used for the Pattern. This is done
     * by running {@link Pattern#quote(String)} on the regexp string when
     * this flag is specified.
     */
    public static final int LITERAL = 0x10;

    /**
     * Enables dotall mode.
     * <br>
     * In dotall mode, the expression <tt>.</tt> matches any character,
     * including a line terminator.  By default this expression does not match
     * line terminators.
     * <br>
     * Dotall mode can also be enabled via the embedded flag
     * expression&nbsp;<tt>(?s)</tt>.  (The <tt>s</tt> is a mnemonic for
     * "single-line" mode, which is what this is called in Perl.)
     */
    public static final int DOTALL = 0x20;

    /**
     * Not used; present for compatibility.
     */
    public static final int UNICODE_CASE = 0x40;

    /**
     * Not used; present for compatibility.
     */
    public static final int CANON_EQ = 0x80;

    /**
     * Enables the Unicode version of <i>Predefined character classes</i> and
     * <i>POSIX character classes</i>.
     * <br>
     * When this flag is specified then the (US-ASCII only)
     * <i>Predefined character classes</i> and <i>POSIX character classes</i>
     * are in conformance with
     * <a href="http://www.unicode.org/reports/tr18/"><i>Unicode Technical
     * Standard #18: Unicode Regular Expression</i></a>
     * <i>Annex C: Compatibility Properties</i>.
     * <br>
     * The UNICODE_CHARACTER_CLASS mode can also be enabled via the embedded
     * flag expression&nbsp;<tt>(?u)</tt>.
     * <br>
     * Specifying this flag may impose a performance penalty.
     */
    public static final int UNICODE_CHARACTER_CLASS = 0x100;


    /**
     * Compiles the given regular expression into a pattern.
     *
     * @param  regex
     *         The expression to be compiled
     *
     * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
    public static Pattern compile(String regex) {
        return new Pattern(regex, 0);
    }

    /**
     * Compiles the given regular expression into a pattern with the given
     * flags.
     *
     * @param  regex
     *         The expression to be compiled
     *
     * @param  flags
     *         Match flags, a bit mask that may include
     *         {@link #CASE_INSENSITIVE}, {@link #MULTILINE}, {@link #DOTALL},
     *         {@link #UNICODE_CASE}, {@link #CANON_EQ}, {@link #UNIX_LINES},
     *         {@link #LITERAL}, {@link #UNICODE_CHARACTER_CLASS}
     *         and {@link #COMMENTS}
     *
     * @throws  IllegalArgumentException
     *          If bit values other than those corresponding to the defined
     *          match flags are set in <tt>flags</tt>
     *
     * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
    public static Pattern compile(String regex, int flags) {
        return new Pattern(regex, flags);
    }

    /**
     * Returns the regular expression from which this pattern was compiled.
     *
     * @return  The source of this pattern
     */
    public String pattern() {
        return internal.toString();
    }

    /**
     * Returns the string representation of this pattern. This
     * is the regular expression from which this pattern was
     * compiled.
     * @return  The string representation of this pattern
     * @since 1.5
     */
    public String toString() {
        return internal.toString();
    }

    /**
     * Creates a matcher that will match the given input against this pattern.
     * @param  input
     *         The character sequence to be matched
     *
     * @return  A new matcher for this pattern
     */
    public Matcher matcher(CharSequence input) {
        return new Matcher(this, input);
    }

    /**
     * Returns this pattern's match flags.
     *
     * @return  The match flags specified when this pattern was compiled
     */
    public int flags() {
        return flags;
    }

    /**
     * Compiles the given regular expression and attempts to match the given
     * input against it.
     * <br>
     * An invocation of this convenience method of the form
     *
     * <blockquote><pre>
     * Pattern.matches(regex, input);</pre></blockquote>
     *
     * behaves in exactly the same way as the expression
     *
     * <blockquote><pre>
     * Pattern.compile(regex).matcher(input).matches()</pre></blockquote>
     * <br>
     * If a pattern is to be used multiple times, compiling it once and reusing
     * it will be more efficient than invoking this method each time.
     *
     * @param  regex
     *         The expression to be compiled
     *
     * @param  input
     *         The character sequence to be matched
     *
     * @throws  PatternSyntaxException
     *          If the expression's syntax is invalid
     */
    public static boolean matches(String regex, CharSequence input) {
        Pattern p = Pattern.compile(regex);
        return p.matcher(input).matches();
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     * <br>
     * The array returned by this method contains each substring of the
     * input sequence that is terminated by another subsequence that matches
     * this pattern or is terminated by the end of the input sequence.  The
     * substrings in the array are in the order in which they occur in the
     * input.  If this pattern does not match any subsequence of the input then
     * the resulting array has just one element, namely the input sequence in
     * string form.
     * <br>
     * The <tt>limit</tt> parameter controls the number of times the
     * pattern is applied and therefore affects the length of the resulting
     * array.  If the limit <i>n</i> is greater than zero then the pattern
     * will be applied at most <i>n</i>&nbsp;-&nbsp;1 times, the array's
     * length will be no greater than <i>n</i>, and the array's last entry
     * will contain all input beyond the last matched delimiter.  If <i>n</i>
     * is non-positive then the pattern will be applied as many times as
     * possible and the array can have any length.  If <i>n</i> is zero then
     * the pattern will be applied as many times as possible, the array can
     * have any length, and trailing empty strings will be discarded.
     * <br>
     * The input <tt>"boo:and:foo"</tt>, for example, yields the following
     * results with these parameters:
     *
     * <blockquote><table cellpadding=1 cellspacing=0
     *              summary="Split examples showing regex, limit, and result">
     * <tr><th><P align="left"><i>Regex&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
     *     <th><P align="left"><i>Limit&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
     *     <th><P align="left"><i>Result&nbsp;&nbsp;&nbsp;&nbsp;</i></th></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>2</td>
     *     <td><tt>{ "boo", "and:foo" }</tt></td></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>5</td>
     *     <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>:</td>
     *     <td align=center>-2</td>
     *     <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>5</td>
     *     <td><tt>{ "b", "", ":and:f", "", "" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>-2</td>
     *     <td><tt>{ "b", "", ":and:f", "", "" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td align=center>0</td>
     *     <td><tt>{ "b", "", ":and:f" }</tt></td></tr>
     * </table></blockquote>
     *
     *
     * @param  input
     *         The character sequence to be split
     *
     * @param  limit
     *         The result threshold, as described above
     *
     * @return  The array of strings computed by splitting the input
     *          around matches of this pattern
     */
    public String[] split(CharSequence input, int limit) {
        int index = 0;
        boolean matchLimited = limit > 0;
        ArrayList<String> matchList = new ArrayList<String>();
        regexodus.Matcher m = new regexodus.Matcher(internal, input);

        // Add segments before each match found
        while(m.find()) {
            if (!matchLimited || matchList.size() < limit - 1) {
                String match = input.subSequence(index, m.start()).toString();
                matchList.add(match);
                index = m.end();
            } else if (matchList.size() == limit - 1) { // last one
                String match = input.subSequence(index,
                        input.length()).toString();
                matchList.add(match);
                index = m.end();
            }
        }

        // If no match was found, return this
        if (index == 0)
            return new String[] {input.toString()};

        // Add remaining segment
        if (!matchLimited || matchList.size() < limit)
            matchList.add(input.subSequence(index, input.length()).toString());

        // Construct result
        int resultSize = matchList.size();
        if (limit == 0)
            while (resultSize > 0 && matchList.get(resultSize-1).equals(""))
                resultSize--;
        String[] result = new String[resultSize];
        return matchList.subList(0, resultSize).toArray(result);
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     *
     * This method works as if by invoking the two-argument {@link
     * #split(java.lang.CharSequence, int) split} method with the given input
     * sequence and a limit argument of zero.  Trailing empty strings are
     * therefore not included in the resulting array.
     * <br>
     * The input <tt>"boo:and:foo"</tt>, for example, yields the following
     * results with these expressions:
     *
     * <blockquote><table cellpadding=1 cellspacing=0
     *              summary="Split examples showing regex and result">
     * <tr><th><P align="left"><i>Regex&nbsp;&nbsp;&nbsp;&nbsp;</i></th>
     *     <th><P align="left"><i>Result</i></th></tr>
     * <tr><td align=center>:</td>
     *     <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>o</td>
     *     <td><tt>{ "b", "", ":and:f" }</tt></td></tr>
     * </table></blockquote>
     *
     * @param  input
     *         The character sequence to be split
     *
     * @return  The array of strings computed by splitting the input
     *          around matches of this pattern
     */
    public String[] split(CharSequence input) {
        return split(input, 0);
    }

    /**
     * Returns a literal pattern <code>String</code> for the specified
     * <code>String</code>.
     * <br>
     * This method produces a <code>String</code> that can be used to
     * create a <code>Pattern</code> that would match the string
     * <code>s</code> as if it were a literal pattern. Metacharacters
     * or escape sequences in the input sequence will be given no special
     * meaning.
     *
     * @param  s The string to be literalized
     * @return  A literal string replacement
     */
    public static String quote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1)
            return "\\Q" + s + "\\E";

        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
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

    private Pattern(String p, int flags)
    {
        int fm = (flags & CASE_INSENSITIVE) != 0 ? REFlags.IGNORE_CASE : 0;
        this.flags = flags;
        fm |= (flags & DOTALL) != 0 ? REFlags.DOTALL : 0;
        fm |= (flags & COMMENTS) != 0 ? REFlags.IGNORE_SPACES : 0;
        fm |= (flags & MULTILINE) != 0 ? REFlags.MULTILINE : 0;
        fm |= (flags & UNICODE_CHARACTER_CLASS) != 0 ? REFlags.UNICODE : 0;
        if((flags & LITERAL) != 0)
            internal = regexodus.Pattern.compile(quote(p), fm);
        else
            internal = regexodus.Pattern.compile(p, fm);
    }

}
