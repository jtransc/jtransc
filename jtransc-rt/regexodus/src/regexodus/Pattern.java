/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * <br>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <br>
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * - Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used
 * to endorse or promote products derived from this software without specific prior
 * written permission.
 * <br>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * @version 1.2_01
 */

package regexodus;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;

/**
 * A handle for a precompiled regular expression; core operations should be identical to java.util.regex.Pattern .
 * Pattern should be no different.
 * <br>
 * To match a regular expression <code>myExpr</code> against a text <code>myString</code> one should first
 * create a Pattern object:<pre>
 * Pattern p = new Pattern(myExpr);
 * </pre>
 * or <pre>
 * Pattern p = Pattern.compile(myExpr);
 * </pre>
 * then obtain a Matcher object:<pre>
 * Matcher matcher=p.matcher(myText);
 * </pre>
 * The latter is an automaton that actually performs a search. It provides the following methods:
 * <ul>
 * <li> search for matching substrings : matcher.find() or matcher.findAll();</li>
 * <li> test whether the text matches the whole pattern : matcher.matches();</li>
 * <li> test whether the text matches the beginning of the pattern : matcher.matchesPrefix();</li>
 * <li> search with custom options : matcher.find(int options);</li>
 * <li> replace simply : matcher.replaceFirst(String) or matcher.replaceAll(String) or matcher.replaceAmount(String, int);</li>
 * <li> for more complex replacement or to obtain a Replacer that should last, use the Pattern : p.replacer()</li>
 * </ul>
 * <br>
 * <b>Flags</b>
 * <br>
 * Flags (see REFlags interface) change the meaning of some regular expression elements at compile-time. Only Unicode
 * matching (for predefined char classes like <pre>\\w</pre>; doesn't affect user-defined Unicode character classes) is
 * enabled by default, but specifying the flags manually disregards the defaults.
 * These flags may be passed both as string(see Pattern(String,String)) and as bitwise OR of:
 * <ul>
 * <li><b>REFlags.IGNORE_CASE</b> - enables case insensitivity</li>
 * <li><b>REFlags.MULTILINE</b> - forces "^" and "$" to match both at the start and the end of line;</li>
 * <li><b>REFlags.DOTALL</b> - forces "." to match eols('\r' and '\n' in ASCII);</li>
 * <li><b>REFlags.IGNORE_SPACES</b> - literal spaces in expression are ignored for better readability;</li>
 * <li><b>REFlags.UNICODE</b> - the predefined classes('\w','\d',etc) are referenced to Unicode;</li>
 * <li><b>REFlags.XML_SCHEMA</b> - permits XML Schema regular expressions syntax extensions.</li>
 * </ul>
 * <br>
 * <b>Multithreading</b><br>
 * Pattern instances are not thread-safe, and neither are Matcher objects.
 * <br>
 * <b>Special Syntax</b>
 * <br>
 * RegExodus adds some features to Java's standard regexes and may not implement some rarely-used features, e.g.
 * character class intersections. Syntax is mostly similar to PCRE's regexes, which Java's are also based on.
 * <br>
 * Here's all of it, as best as I can supply at 3 in the morning:
 * <ul>
 *     <li>A single character, unless it is a metacharacter, represents itself. "a" will match "a".</li>
 *     <li>Metacharacters affect regex behavior, and include:
 *     <ul>
 *         <li>'.' matches any single character on the same line (not a newline or carriage return), unless the DOTALL
 *         flag is enabled, in which it matches any character, including newlines and carriage returns.</li>
 *         <li>'^' means the start of the searched text.</li>
 *         <li>'$' means the end of the searched text, and has different meaning in a replacement string.</li>
 *         <li>'*' means the previous term can be repeated 0 or more times.</li>
 *         <li>'+' means the previous term can be repeated 1 or more times. Its meaning as a "possessive" modifier for
 *         repeating terms is <b>not supported</b>.</li>
 *         <li>'?' means the previous term can be repeated 0 or 1 times. It can also be placed after a repeating term,
 *         like "*", "+", or "?", to make it "reluctant." It also has special meaning in parentheses.</li>
 *         <li>'&#123;' and '&#125;' should be in matching pairs, and usually have an integer between them or two integers separated by a comma.
 *         One integer with no comma means the previous term should be repeated that many times exactly.
 *         Two integers, "&#123;a,b&#125;" with a comma mean the previous term should be repeated anywhere between a and b times, inclusive.
 *         One integer with a comma on either side acts as the two-integer case with no bound where the integer of a and b has been omitted.</li>
 *         <li>'(' and ')' in a matching pair define a group, and groups have extensive syntax of their own.</li>
 *         <li>'[' and ']' in a matching pair define a character class, which can match a single occurrence of one of
 *         multiple possible characters, possibly represented as ranges of characters. "[abc]" will match "a", "b", and
 *         "c". "[A-Z]" will match all upper-case English letters (it will not behave as intended if another language
 *         finds its way into your text). You can use shorthands defined with a backslash, listed below, in a character
 *         class most of the time (not \\b or other boundary matchers, nor \\Q and \\E). This means you could match all
 *         upper-case letters in just about any language with "[\\p&#123;Lu&#125;]", which is the same as without the
 *         brackets, but you could add additional parts to that, such as "[\\p&#123;Lu&#125;_]+" to match strings like
 *         "ABACUS_ÆTHỲŔ" (the '+' allows matching the whole string, and the extra '_' in the character class allows the
 *         whole thing to match). If a character class starts with '^', it negates the character class, matching any
 *         char that <b>isn't</b> one of the characters in the character class. If you need to match a </li>
 *         <li>'\\' (the single backslash, which usually needs to be escaped in source) can be used as an escape for
 *         other regex-specific terms, or to match a backreference to an earlier group. Backreferences are augmented in
 *         RegExodus, and you can do some useful and uncommon things with them. They are documented later, with groups.
 *         Regex-specific terms include the common \\w to match a word character (alphanumeric or underscore, by default
 *         respecting Unicode), \\b to match a boundary between a word character and a non-word character or the
 *         start/end of the text, \\d to match a digit, \\s to match a space, and capitalized versions of the previous
 *         ones mean the negation of them (like \\D is any non-digit character). You can also use \\x?? where ? is a hex
 *         digit to produce a char between Unicode 0 and 255, \\o???? where you can have 1 or more ? that are octal
 *         digits to produce a char by its code point as an octal number, \\m????? where you can have between 1 and 5
 *         decimal digits to produce a char by its code point as a normal base-10 number, and also \\u???? with exactly
 *         4 hex digits (similar to the escaping that Java will do on its own if you don't escape the backslash) and
 *         \\x&#123;????&#125; to do basically the same thing with a variable amount of ? as hex digits, but no more
 *         than 4 digits. In addition to the predefined character classes like \\w, there are Unicode categories,
 *         accessible with \\p? or \\p&#123;??&#125; , where the single ? is a group of categories like L for letters,
 *         P for punctuation, or N for numbers, and an upper-case-then-lower-case pair of ? in curly braces is an
 *         individual category like Ll for lower-case letters, Nd for decimal numbers, or Sc for currency symbols.
 *         There's a good list here, http://www.regular-expressions.info/unicode.html ; only Cased_Letter is not
 *         supported of the list of categories. RegExodus also supports Zh and Zv for horizontal and vertical spacing
 *         characters, respectively. Some other \\p... features may be supported, but not necessarily for the same
 *         version of Unicode that the categories support (Unicode 8.0.0 for categories here, in standard Java it isn't
 *         expected until Java 9). \\&lt; and \\&gt; can be used to match the start or end of a word (similar to \\b
 *         but only for one side). \\Q starts a literal escape in which metacharacters lose their special treatment and
 *         are matched like normal characters; this escape ends when the sequence \\E is reached. \\h, \\H, \\v, and \\V
 *         match horizontal whitespace, non-(horizontal whitespace), vertical whitespace, and non-(vertical whitespace),
 *         respectively; they behave like Java 8's handling of these escapes and not earlier versions (which matched a
 *         specific vertical tab character with \\v instead of all vertical whitespace). Java 8's \\R escape, which
 *         matches all line separators known in use, is not supported yet, but you can copy its behavior with
 *         "<tt>(?&gt;\\r\\n|[\\n\\cK\\f\\r\\u0085\\u2028\\u2029])</tt>" .
 *         </li>
 *     </ul>
 *     </li>
 *     <li>Groups are, as in most regex flavors, rather complicated. You can create a group with "(something)", which
 *     would match "something" and also store the text that matched the parenthesized section as a numbered group, here,
 *     group 1. Referencing these groups is covered next; RegExodus adds some features to backreferences that aren't
 *     present elsewhere. You can also name a group with the syntax "(&#123;NAME&#125;something)", which would again
 *     match "something" but this time would have the name "NAME", which allows you to reassign the contents of the
 *     remembered group's match for the purposes of later regexes. Reassignment uses the syntax
 *     "(&#123;=NAME&#125;something)", and can be done even if the group called "NAME" hasn't been found yet. If an
 *     earlier match to a repeated group like "((&#123;=NAME&#125;a+) ?)+" matched the "aa" in "aa a aaa aaa", finding
 *     all matches (the outer group with a '+') would finally cause the group with the name "NAME" to have the value
 *     "aaa", the last match to the named group. Groups can have many other kinds of special syntax, usually starting
 *     with a question mark as a metacharacter just after the opening parenthesis:
 *     <ul>
 *         <li>"?:" means a "plain group"; one that only keeps its contents together as a single unit for the purposes
 *         of repetition and other things like it, but doesn't remember the matched text for backreferences.</li>
 *         <li>"?=" means "positive lookahead"; it doesn't consume the text it matches but does require that text to be
 *         ahead for the regex to succeed.</li>
 *         <li>"?<=" means "positive lookbehind"; it doesn't consume the text it matches but does require that text to be
 *         before the next part of the regex for the regex to succeed.</li>
 *         <li>"?!" means "negative lookahead"; it doesn't consume the text it matches but does require that text to NOT
 *         be ahead for the regex to succeed.</li>
 *         <li>"?<!" means "positive lookbehind"; it doesn't consume the text it matches but does require that text to
 *         NOT be before the next part of the regex for the regex to succeed.</li>
 *         <li>"?>" means an "atomic group"; it acts like a plain group in that it doesn't remember the matched text for
 *         backreferences, but it also tracks backreferences independently in itself, temporarily forgetting whatever is
 *         outside the independent, atomic group.</li>
 *         <li>"?#" means a comment in the regex; anything up until the closing parenthesis is ignored.</li>
 *         <li>"?imsuxX", where all of the letters are optional, changes the mode of the regex after that point. The "i"
 *         makes it case-insensitive, "m" makes "^" and "$" match the multi-line start and end of the text, instead of
 *         the start and end of a line. "s" makes "." match all characters, even line endings. "u" turns on Unicode
 *         handling for escapes like "\\w" and "\\b". "x" makes whitespace ignored in the regex, which can help
 *         legibility sometimes. "X" (capitalized) makes XML Schema terms allowed in "\\p" categories; "X" might not
 *         currently be working. You can add a ":" (and some text to match) after the letters to make a plain group that
 *         matches that text with the specified modes enabled, only for that group.</li>
 *         <li>"?(...)" means a "conditional group"; I actually don't know what this does since it was present in JRegex
 *         before I forked it to make RegExodus. It seems to be a non-standard extension.</li>
 *         <li>"?[...]" means a "class group"; I actually don't know what this does since it was present in JRegex
 *         before I forked it to make RegExodus. It seems to be a non-standard extension.</li>
 *     </ul>
 *     </li>
 *     <li>If a group matched some text, you may want to refer to that match later in the regex, or use it during a
 *     replacement. In replacement strings or in the matching regex itself, you can refer to what a group matched,
 *     though with slightly different syntax. In replacement strings, you can refer to a numbered group with "$?" where
 *     ? is a base-10 number with any number of digits, starting at 1. In a regex, you can make a backreference to an
 *     earlier group and what it matched with "\\?", where ? is again a base-10 number and refers to the text matched by
 *     the specified numbered group earlier in the regex. This could be used to match the text "HEY HEY" or "WHAT WHAT"
 *     with the example pattern "([A-Z]+) \\1", which would match the previous two examples but not "HEY WHAT". You can
 *     match a named group with the longer syntax "&#123;\\NAME&#125;" in a pattern or "$&#123;NAME&#125;" in a
 *     replacement string. This syntax also works with numbered groups (change "NAME" to "1", for example), and enables
 *     the special augmentations RegExodus adds:
 *     <ul>
 *         <li>Immediately before the name or number of the group ("NAME" in the examples), you can place one or more punctuation
 *         characters that (only in this context) change what the backreference is considered equivalent to.</li>
 *         <li>An at sign, {@literal @}, before the name or number of the group makes the match between the
 *         backreference and matched group case-insensitive. In replacement strings, this always makes the replacement
 *         lower-case.</li>
 *         <li>A slash, {@literal /}, before the name or number of the group makes the backreference match the group
 *         in reverse order, or reverses the group when used in a replacement string. This may have bad behavior with
 *         Unicode chars outside the first 65536 (the Basic Multilingual Plane), such as emoji.</li>
 *         <li>A colon, {@literal :}, before the name or number of the group makes any opening or closing parentheses
 *         or bracket-like characters match their closing or opening counterpart. Thus, "(" would be replaced with ")"
 *         and "〖〗" would be replaced with "〗〖" (it handles practically all of the Unicode Ps and Pe categories).
 *         </li>
 *         <li>Anywhere from none of these modifiers to all 3 can appear in a backreference; the order doesn't matter.
 *         </li>
 *     </ul>
 *     </li>
 * </ul>
 * <br>
 * I hope that answers at least some questions about the syntax extensions RegExodus makes.
 *
 * @see REFlags
 * @see Matcher
 * @see Matcher#setTarget(java.lang.CharSequence)
 * @see Matcher#setTarget(java.lang.CharSequence, int, int)
 * @see Matcher#setTarget(char[], int, int)
 * @see Matcher#setTarget(java.io.Reader, int)
 * @see MatchResult
 * @see MatchResult#group(int)
 * @see MatchResult#start(int)
 * @see MatchResult#end(int)
 * @see MatchResult#length(int)
 * @see MatchResult#charAt(int, int)
 * @see MatchResult#prefix()
 * @see MatchResult#suffix()
 */

public class Pattern implements Serializable, REFlags {
    private static final long serialVersionUID = -3628346657932720807L;

    String stringRepr;

    // tree entry
    Term root, root0;

    // required number of memory slots
    int memregs;

    // required number of iteration counters
    int counters;

    // number of lookahead groups
    int lookaheads;

    HashMap<String, Integer> namedGroupMap;

    boolean caseless = false;

    protected Pattern() throws PatternSyntaxException {
    }

    /**
     * Compiles an expression with default flags.
     *
     * @param regex the Perl5-compatible regular expression string.
     * @throws PatternSyntaxException if the argument doesn't correspond to perl5 regex syntax.
     * @see Pattern#Pattern(java.lang.String, java.lang.String)
     * @see Pattern#Pattern(java.lang.String, int)
     */
    public Pattern(String regex) throws PatternSyntaxException {
        this(regex, DEFAULT);
    }

    /**
     * Compiles a regular expression using Perl5-style flags.
     * The flag string should consist of letters 'i','m','s','x','u','X'(the case is significant) and a hyphen or plus.
     * The meaning of letters:
     * <ul>
     * <li><b>i</b> - case insensitivity, corresponds to REFlags.IGNORE_CASE;</li>
     * <li><b>m</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to REFlags.MULTILINE flag;</li>
     * <li><b>s</b> - single line treatment('.' matches \r's and \n's),corresponds to REFlags.DOTALL;</li>
     * <li><b>x</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to REFlags.IGNORE_SPACES.</li>
     * <li><b>u</b> - predefined classes are regarded as belonging to Unicode, corresponds to REFlags.UNICODE; this may yield some performance penalty.</li>
     * <li><b>X</b> - compatibility with XML Schema, corresponds to REFlags.XML_SCHEMA.</li>
     * <li><b>-</b> - turn off the specified flags; normally has no effect unless something adds the flags.</li>
     * <li><b>+</b> - turn on the specified flags; normally is no different from just using the letters.</li>
     * </ul>
     *
     * @param regex the Perl5-compatible regular expression string.
     * @param flags the Perl5-compatible flags.
     * @throws PatternSyntaxException if the argument doesn't correspond to perl5 regex syntax.
     *                                see REFlags
     */
    public Pattern(String regex, String flags) throws PatternSyntaxException {
        internalCompile(regex, parseFlags(flags));
    }

    /**
     * Compiles a regular expression using REFlags.
     * The <code>flags</code> parameter is a bitwise OR of the following values:
     * <ul>
     * <li><b>REFlags.IGNORE_CASE</b> - case insensitivity, corresponds to '<b>i</b>' letter;</li>
     * <li><b>REFlags.MULTILINE</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to '<b>m</b>';</li>
     * <li><b>REFlags.DOTALL</b> - single line treatment('.' matches \r's and \n's),corresponds to '<b>s</b>';</li>
     * <li><b>REFlags.IGNORE_SPACES</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to '<b>x</b>'.</li>
     * <li><b>REFlags.UNICODE</b> - predefined classes are regarded as belonging to Unicode, corresponds to '<b>u</b>'; this may yield some performance penalty.</li>
     * <li><b>REFlags.XML_SCHEMA</b> - compatibility with XML Schema, corresponds to '<b>X</b>'.</li>
     * </ul>
     *
     * @param regex the Perl5-compatible regular expression string.
     * @param flags the Perl5-compatible flags.
     * @throws PatternSyntaxException if the argument doesn't correspond to perl5 regex syntax.
     *                                see REFlags
     */
    private Pattern(String regex, int flags) throws PatternSyntaxException {
        internalCompile(regex, flags);
    }

    /**
     * Sets this Pattern's flags with the char-per-flag representation of regex flags. Removes flags set earlier.
     * The flag string should consist of letters 'i','m','s','x','u','X'(the case is significant) and a hyphen or plus.
     * The meaning of letters:
     * <ul>
     * <li><b>i</b> - case insensitivity, corresponds to REFlags.IGNORE_CASE;</li>
     * <li><b>m</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to REFlags.MULTILINE flag;</li>
     * <li><b>s</b> - single line treatment('.' matches \r's and \n's),corresponds to REFlags.DOTALL;</li>
     * <li><b>x</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to REFlags.IGNORE_SPACES.</li>
     * <li><b>u</b> - predefined classes are regarded as belonging to Unicode, corresponds to REFlags.UNICODE; this may yield some performance penalty.</li>
     * <li><b>X</b> - compatibility with XML Schema, corresponds to REFlags.XML_SCHEMA.</li>
     * <li><b>-</b> - turn off the specified flags; normally has no effect unless something adds the flags.</li>
     * <li><b>+</b> - turn on the specified flags; normally is no different from just using the letters.</li>
     * </ul>
     * @param flags a String that stores various flags as chars
     */
    public void setFlags(String flags)
    {
        internalCompile(stringRepr, parseFlags(flags));
    }

    /**
     * Sets this Pattern's flags with the bitmask-style int representation of regex flags. Removes flags set earlier.
     * Flag constants can be found in REFlags; UNICODE is enabled normally but is not automatically turned on here.
     * The <code>flags</code> parameter is a bitwise OR of the following values:
     * <ul>
     * <li><b>REFlags.IGNORE_CASE</b> - case insensitivity, corresponds to '<b>i</b>' letter;</li>
     * <li><b>REFlags.MULTILINE</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to '<b>m</b>';</li>
     * <li><b>REFlags.DOTALL</b> - single line treatment('.' matches \r's and \n's),corresponds to '<b>s</b>';</li>
     * <li><b>REFlags.IGNORE_SPACES</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to '<b>x</b>'.</li>
     * <li><b>REFlags.UNICODE</b> - predefined classes are regarded as belonging to Unicode, corresponds to '<b>u</b>'; this may yield some performance penalty.</li>
     * <li><b>REFlags.XML_SCHEMA</b> - compatibility with XML Schema, corresponds to '<b>X</b>'.</li>
     * </ul>
     * @param flags an int that stores various flags from REFlags bitwise-OR-ed with each other
     */
    public void setFlags(int flags)
    {
        internalCompile(stringRepr, flags);
    }

    //java.util.regex.* compatibility

    /**
     * Compiles the given String into a Pattern that can be used to match text.
     * The syntax is normal for Java, including backslashes as part of regex syntax, like the digit shorthand "\d",
     * escaped twice to "\\d" (so the double-quoted String itself doesn't try to interpret the backslash).
     * @param regex a String in normal Java regular expression format
     * @return a newly constructed Pattern object that can be used to match text that fits the given regular expression
     * @throws PatternSyntaxException
     */
    public static Pattern compile(String regex) throws PatternSyntaxException{
        return new Pattern(regex, DEFAULT);
    }
    //java.util.regex.* compatibility

    /**
     * Compiles the given String into a Pattern that can be used to match text.
     * The syntax is normal for Java, including backslashes as part of regex syntax, like the digit shorthand "\d",
     * escaped twice to "\\d" (so the double-quoted String itself doesn't try to interpret the backslash).
     * <br>
     * This variant allows flags to be passed as an int constructed via bitwise OR from REFlags constants. You may prefer
     * the variant that takes a String for clarity.
     * @param regex a String in normal Java regular expression format
     * @param flags integer flags that are constructed via bitwise OR from the flag constants in REFlags.
     * @return a newly constructed Pattern object that can be used to match text that fits the given regular expression
     * @throws PatternSyntaxException
     */
    public static Pattern compile(String regex,int flags) throws PatternSyntaxException{
        return new Pattern(regex, flags);
    }
    //java.util.regex.* compatibility
    /**
     * Compiles the given String into a Pattern that can be used to match text.
     * The syntax is normal for Java, including backslashes as part of regex syntax, like the digit shorthand "\d",
     * escaped twice to "\\d" (so the double-quoted String itself doesn't try to interpret the backslash).
     * <br>
     * This variant allows flags to be passed as an String.
     * The flag string should consist of letters 'i','m','s','x','u','X'(the case is significant) and a hyphen or plus.
     * The meaning of letters:
     * <ul>
     * <li><b>i</b> - case insensitivity, corresponds to REFlags.IGNORE_CASE;</li>
     * <li><b>m</b> - multiline treatment(BOLs and EOLs affect the '^' and '$'), corresponds to REFlags.MULTILINE flag;</li>
     * <li><b>s</b> - single line treatment('.' matches \r's and \n's),corresponds to REFlags.DOTALL;</li>
     * <li><b>x</b> - extended whitespace comments (spaces and eols in the expression are ignored), corresponds to REFlags.IGNORE_SPACES.</li>
     * <li><b>u</b> - predefined classes are regarded as belonging to Unicode, corresponds to REFlags.UNICODE; this may yield some performance penalty.</li>
     * <li><b>X</b> - compatibility with XML Schema, corresponds to REFlags.XML_SCHEMA.</li>
     * <li><b>-</b> - turn off the specified flags; normally has no effect unless something adds the flags.</li>
     * <li><b>+</b> - turn on the specified flags; normally is no different from just using the letters.</li>
     * </ul>
     *
     * @param regex a String in normal Java regular expression format
     * @param flags integer flags that are constructed via bitwise OR from the flag constants in REFlags.
     * @return a newly constructed Pattern object that can be used to match text that fits the given regular expression
     * @throws PatternSyntaxException
     */
    public static Pattern compile(String regex,String flags) throws PatternSyntaxException{
        return new Pattern(regex, flags);
    }


    private void internalCompile(String regex, int flags) throws PatternSyntaxException {
        stringRepr = regex;
        caseless = (flags & IGNORE_CASE) == IGNORE_CASE;
        Term.makeTree(regex, new int[]{flags}, this);
    }

    /**
     * How many capturing groups does this expression include?
     */
    public int groupCount() {
        return memregs;
    }

    /**
     * Get numeric id for a group name.
     *
     * @return <code>null</code> if no such name found.
     * @see MatchResult#group(java.lang.String)
     * @see MatchResult#isCaptured(java.lang.String)
     */
    public Integer groupId(String name) {
        return (namedGroupMap.get(name));
    }

    /**
     * A shorthand for Pattern.matcher(String).matches().<br>
     *
     * @param s the target
     * @return true if the entire target matches the pattern
     * @see Matcher#matches()
     * @see Matcher#matches(String)
     */
    public boolean matches(String s) {
        return matcher(s).matches();
    }

    /**
     * A shorthand for Pattern.matcher(String).matchesPrefix().<br>
     *
     * @param s the target
     * @return true if the entire target matches the beginning of the pattern
     * @see Matcher#matchesPrefix()
     */
    public boolean startsWith(String s) {
        return matcher(s).matchesPrefix();
    }

    /**
     * Returns a target-less matcher.
     * Don't forget to supply a target.
     */
    public Matcher matcher() {
        return new Matcher(this);
    }

    /**
     * Returns a matcher for a specified string.
     */
    public Matcher matcher(CharSequence s) {
        Matcher m = new Matcher(this);
        m.setTarget(s);
        return m;
    }

    /**
     * Returns a matcher for a specified region.
     */
    public Matcher matcher(char[] data, int start, int end) {
        Matcher m = new Matcher(this);
        m.setTarget(data, start, end);
        return m;
    }

    /**
     * Returns a matcher for a match result (in a performance-friendly way).
     * <code>groupId</code> parameter specifies which group is a target.
     *
     * @param groupId which group is a target; either positive integer(group id), or one of MatchResult.MATCH,MatchResult.PREFIX,MatchResult.SUFFIX,MatchResult.TARGET.
     */
    public Matcher matcher(MatchResult res, int groupId) {
        Matcher m = new Matcher(this);
        if (res instanceof Matcher) {
            m.setTarget((Matcher) res, groupId);
        } else {
            m.setTarget(res.targetChars(), res.start(groupId) + res.targetStart(), res.length(groupId));
        }
        return m;
    }

    /**
     * Just as above, yet with symbolic group name.
     *
     * @throws NullPointerException if there is no group with such name
     */
    public Matcher matcher(MatchResult res, String groupName) {
        Integer id = res.pattern().groupId(groupName);
        if (id == null) throw new IllegalArgumentException("group not found:" + groupName);
        int group = id;
        return matcher(res, group);
    }

    /**
     * Returns a matcher taking a text stream as target.
     * <b>Note that this is not a true POSIX-style stream matching</b>, i.e. the whole length of the text is preliminary read and stored in a char array.
     *
     * @param text   a text stream
     * @param length the length to read from a stream; if <code>len</code> is <code>-1</code>, the whole stream is read in.
     * @throws IOException indicates an IO problem
     */
    @GwtIncompatible
    public Matcher matcher(Reader text, int length) throws IOException {
        Matcher m = new Matcher(this);
        m.setTarget(text, length);
        return m;
    }

    /**
     * Returns a replacer of a pattern by specified perl-like expression.
     * Such replacer will substitute all occurrences of a pattern by an evaluated expression
     * ("$&amp;" and "$0" will substitute by the whole match, "$1" will substitute by group#1, etc).
     * Example:<pre>
     * String text="The quick brown fox jumped over the lazy dog";
     * Pattern word=new Pattern("\\w+");
     * System.out.println(word.replacer("[$&amp;]").replace(text));
     * //prints "[The] [quick] [brown] [fox] [jumped] [over] [the] [lazy] [dog]"
     * Pattern swap=new Pattern("(fox|dog)(.*?)(fox|dog)");
     * System.out.println(swap.replacer("$3$2$1").replace(text));
     * //prints "The quick brown dog jumped over the lazy fox"
     * Pattern scramble=new Pattern("(\\w+)(.*?)(\\w+)");
     * System.out.println(scramble.replacer("$3$2$1").replace(text));
     * //prints "quick The fox brown over jumped lazy the dog"
     * </pre>
     *
     * @param expr a perl-like expression, the "$&amp;" and "${&amp;}" standing for whole match, the "$N" and "${N}" standing for group#N, and "${Foo}" standing for named group Foo.
     * @see Replacer
     */
    public Replacer replacer(String expr) {
        return new Replacer(this, expr);
    }

    /**
     * Returns a replacer will substitute all occurrences of a pattern
     * through applying a user-defined substitution model.
     *
     * @param model a Substitution object which is in charge for match substitution
     * @see Replacer
     */
    public Replacer replacer(Substitution model) {
        return new Replacer(this, model);
    }

    /**
     * Tokenizes a text by an occurrences of the pattern.
     * Note that a series of adjacent matches are regarded as a single separator.
     * The same as new RETokenizer(Pattern,String);
     *
     * @see RETokenizer
     * @see RETokenizer#RETokenizer(regexodus.Pattern, java.lang.String)
     */
    public RETokenizer tokenizer(String text) {
        return new RETokenizer(this, text);
    }

    /**
     * Tokenizes a specified region by an occurrences of the pattern.
     * Note that a series of adjacent matches are regarded as a single separator.
     * The same as new RETokenizer(Pattern,char[],int,int);
     *
     * @see RETokenizer
     * @see RETokenizer#RETokenizer(regexodus.Pattern, char[], int, int)
     */
    public RETokenizer tokenizer(char[] data, int off, int len) {
        return new RETokenizer(this, data, off, len);
    }

    /**
     * Tokenizes a specified region by an occurrences of the pattern.
     * Note that a series of adjacent matches are regarded as a single separator.
     * The same as new RETokenizer(Pattern,Reader,int);
     *
     * @see RETokenizer
     * @see RETokenizer#RETokenizer(regexodus.Pattern, java.io.Reader, int)
     */
    @GwtIncompatible
    public RETokenizer tokenizer(Reader in, int length) throws IOException {
        return new RETokenizer(this, in, length);
    }

    public String toString() {
        return stringRepr;
    }

    /**
     * Returns a less or more readable representation of a bytecode for the pattern.
     */
    public String toString_d() {
        return root.toStringAll();
    }

    private static int parseFlags(String flags) throws PatternSyntaxException {
        boolean enable = true;
        int len = flags.length();
        int result = DEFAULT;
        for (int i = 0; i < len; i++) {
            char c = flags.charAt(i);
            switch (c) {
                case '+':
                    enable = true;
                    break;
                case '-':
                    enable = false;
                    break;
                default:
                    int flag = getFlag(c);
                    if (enable) result |= flag;
                    else result &= (~flag);
            }
        }
        return result;
    }

    static int parseFlags(char[] data, int start, int len) throws PatternSyntaxException {
        boolean enable = true;
        int result = DEFAULT;
        for (int i = 0; i < len; i++) {
            char c = data[start + i];
            switch (c) {
                case '+':
                    enable = true;
                    break;
                case '-':
                    enable = false;
                    break;
                default:
                    int flag = getFlag(c);
                    if (enable) result |= flag;
                    else result &= (~flag);
            }
        }
        return result;
    }

    private static int getFlag(char c) throws PatternSyntaxException {
        switch (c) {
            case 'i':
                return IGNORE_CASE;
            case 'm':
                return MULTILINE;
            case 's':
                return DOTALL;
            case 'x':
                return IGNORE_SPACES;
            case 'u':
                return UNICODE;
            case 'X':
                return XML_SCHEMA;
        }
        throw new PatternSyntaxException("unknown flag: " + c);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pattern pattern = (Pattern) o;

        if (memregs != pattern.memregs) return false;
        if (counters != pattern.counters) return false;
        if (lookaheads != pattern.lookaheads) return false;
        if (stringRepr != null ? !stringRepr.equals(pattern.stringRepr) : pattern.stringRepr != null) return false;
        return root != null ? root.equals(pattern.root) : pattern.root == null && (root0 != null ? root0.equals(pattern.root0) : pattern.root0 == null && (namedGroupMap != null ? namedGroupMap.equals(pattern.namedGroupMap) : pattern.namedGroupMap == null));

    }

    @Override
    public int hashCode() {
        int result = stringRepr != null ? stringRepr.hashCode() : 0;
        result = 31 * result + (root != null ? root.hashCode() : 0);
        result = 31 * result + (root0 != null ? root0.hashCode() : 0);
        result = 31 * result + memregs;
        result = 31 * result + counters;
        result = 31 * result + lookaheads;
        result = 31 * result + (namedGroupMap != null ? namedGroupMap.hashCode() : 0);
        return result;
    }
}