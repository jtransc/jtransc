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
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <b>The Replacer class</b> suggests some methods to replace occurrences of a pattern
 * either by a result of evaluation of a perl-like expression, or by a plain string,
 * or according to a custom substitution model, provided as a Substitution interface implementation.<br>
 * A Replacer instance may be obtained either using Pattern.replacer(...) method, or by constructor:<code>
 * Pattern p=new Pattern("\\w+");
 * Replacer perlExpressionReplacer=p.replacer("[$&amp;]");
 * //or another way to do the same
 * Substitution myOwnModel=new Substitution(){
 *    public void appendSubstitution(MatchResult match,TextBuffer tb){
 *       tb.append('[');
 *       match.getGroup(MatchResult.MATCH,tb);
 *       tb.append(']');
 *    }
 * }
 * Replacer myVeryOwnReplacer=new Replacer(p,myOwnModel);
 * </code>
 * The second method is much more verbose, but gives more freedom.
 * To perform a replacement call replace(someInput):<pre>
 * System.out.print(perlExpressionReplacer.replace("All your base "));
 * System.out.println(myVeryOwnReplacer.replace("are belong to us"));
 * //result: "[All] [your] [base] [are] [belong] [to] [us]"
 * </pre>
 * This code was mostly written in 2001, I hope the reference isn't too outdated...
 * @see Substitution
 * @see PerlSubstitution
 * @see Replacer#Replacer(regexodus.Pattern, regexodus.Substitution)
 */

public class Replacer implements Serializable {
    private static final long serialVersionUID = 2528136757932720807L;

    private Pattern pattern;
    private Substitution substitution;

    /**
     * Constructs a Replacer from a Pattern and implementation of Substitution.
     * Only meant to be used if you have complex substitution behavior. An example of how to make such an implementation
     * that surrounds each match with an increasing number of square brackets could be:
     * <br>
     * <code>
     * Substitution mySub=new Substitution(){
     *    public int counter = 1;
     *    public void appendSubstitution(MatchResult match,TextBuffer tb){
     *       for(int i = 0; i < counter; i++)
     *           tb.append('[');
     *       //appends the full match into tb; 0 can be used in place of MatchResult.MATCH
     *       match.getGroup(MatchResult.MATCH, tb);
     *       for(int i = 0; i < counter; i++)
     *           tb.append(']');
     *       counter++;
     *    }
     * }
     * </code>
     * @param pattern a regexodus.Pattern that determines what should be replaced
     * @param substitution an implementation of the Substitution interface, which allows custom replacement behavior
     */
    public Replacer(Pattern pattern, Substitution substitution) {
        this.pattern = pattern;
        this.substitution = substitution;
    }

    /**
     * Constructs a Replacer from a Pattern and a String to replace occurrences of the Pattern with.
     * @param pattern a regexodus.Pattern that determines what should be replaced
     * @param substitution a String that will be used to replace occurrences of the Pattern
     */
    public Replacer(Pattern pattern, String substitution) {
        this(pattern, substitution, true);
    }

    public Replacer(Pattern pattern, String substitution, boolean isPerlExpr) {
        this.pattern = pattern;
        this.substitution = isPerlExpr ? new PerlSubstitution(substitution) :
                new DummySubstitution(substitution);
    }

    public void setSubstitution(String s, boolean isPerlExpr) {
        substitution = isPerlExpr ? new PerlSubstitution(s) :
                new DummySubstitution(s);
    }

    /**
     * Takes all instances in text of the Pattern this was constructed with, and replaces them with substitution.
     * @param text a String, StringBuilder, or other CharSequence that may contain the text to replace
     * @return the post-replacement text
     */
    public String replace(CharSequence text) {
        TextBuffer tb = wrap(new StringBuilder(text.length()));
        replace(pattern.matcher(text), substitution, tb);
        return tb.toString();
    }

    /**
     * Takes instances in text of the Pattern this was constructed with, up to count times, and replaces them with
     * substitution. If you want to change the position in a Matcher so you start the next replacement at a later point
     * in text, you can use {@code replace(Matcher, Substitution, TextBuffer, int)}, which this uses internally. The
     * difference is that internally, this uses a temporary Matcher that doesn't store the change in position, and code
     * that should track replacement points should use a longer-lived Matcher.
     * @param text a String, StringBuilder, or other CharSequence that may contain the text to replace
     * @param count the maximum number of replacements to perform; will make no changes if less than 1
     * @return the post-replacement text
     */
    public String replace(CharSequence text, int count) {
        TextBuffer tb = wrap(new StringBuilder(text.length()));
        replace(pattern.matcher(text), substitution, tb, count);
        return tb.toString();
    }

    public String replace(char[] chars, int off, int len) {
        TextBuffer tb = wrap(new StringBuilder(len));
        replace(pattern.matcher(chars, off, len), substitution, tb);
        return tb.toString();
    }

    public String replace(MatchResult res, int group) {
        TextBuffer tb = wrap(new StringBuilder());
        replace(pattern.matcher(res, group), substitution, tb);
        return tb.toString();
    }

    @GwtIncompatible
    public String replace(Reader text, int length) throws IOException {
        TextBuffer tb = wrap(new StringBuilder(length >= 0 ? length : 0));
        replace(pattern.matcher(text, length), substitution, tb);
        return tb.toString();
    }

    /**
     * Takes all occurrences of the pattern this was constructed with in text and replaces them with the substitution.
     * Appends the replaced text into sb.
     * @param text a String, StringBuilder, or other CharSequence that may contain the text to replace
     * @param sb the StringBuilder to append the result into
     * @return the number of individual replacements performed; the results are applied to sb
     */
    public int replace(CharSequence text, StringBuilder sb) {
        return replace(pattern.matcher(text), substitution, wrap(sb));
    }

    /**
     * Takes instances in text of the Pattern this was constructed with, up to count times, and replaces them with the
     * substitution. Appends the replaced text into sb.
     * @param text a String, StringBuilder, or other CharSequence that may contain the text to replace
     * @param sb the StringBuilder to append the result into
     * @param count the maximum number of replacements to perform; will make no changes if less than 1
     * @return the number of individual replacements performed; the results are applied to sb
     */
    public int replace(CharSequence text, StringBuilder sb, int count) {
        return replace(pattern.matcher(text), substitution, wrap(sb), count);
    }

    /**
     */
    public int replace(char[] chars, int off, int len, StringBuilder sb) {
        return replace(chars, off, len, wrap(sb));
    }

    /**
     */
    public int replace(MatchResult res, int group, StringBuilder sb) {
        return replace(res, group, wrap(sb));
    }

    /**
     */
    public int replace(MatchResult res, String groupName, StringBuilder sb) {
        return replace(res, groupName, wrap(sb));
    }

    @GwtIncompatible
    public int replace(Reader text, int length, StringBuilder sb) throws IOException {
        return replace(text, length, wrap(sb));
    }

    /**
     */
    public int replace(CharSequence text, TextBuffer dest) {
        return replace(pattern.matcher(text), substitution, dest);
    }

    /**
     */
    private int replace(char[] chars, int off, int len, TextBuffer dest) {
        return replace(pattern.matcher(chars, off, len), substitution, dest);
    }

    /**
     */
    private int replace(MatchResult res, int group, TextBuffer dest) {
        return replace(pattern.matcher(res, group), substitution, dest);
    }

    /**
     */
    private int replace(MatchResult res, String groupName, TextBuffer dest) {
        return replace(pattern.matcher(res, groupName), substitution, dest);
    }

    @GwtIncompatible
    private int replace(Reader text, int length, TextBuffer dest) throws IOException {
        return replace(pattern.matcher(text, length), substitution, dest);
    }

    /**
     * Replaces all occurrences of a matcher's pattern in a matcher's target
     * by a given substitution appending the result to a buffer.<br>
     * The substitution starts from current matcher's position, current match
     * not included.
     */
    public static int replace(Matcher m, Substitution substitution, TextBuffer dest) {
        boolean firstPass = true;
        int c = 0;
        while (m.find()) {
            if (m.end() == 0 && !firstPass) continue;  //allow to replace at "^"
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            c++;
            m.setTarget(m, MatchResult.SUFFIX);
            firstPass = false;
        }
        m.getGroup(MatchResult.TARGET, dest);
        return c;
    }

    /**
     * Replaces the first n occurrences of a matcher's pattern, where n is equal to count,
     * in a matcher's target by a given substitution, appending the result to a buffer.
     * <br>
     * The substitution starts from current matcher's position, current match not included.
     * @param m a Matcher
     * @param substitution a Substitution, typically a PerlSubstitution
     * @param dest the TextBuffer this will write to; see Replacer.wrap()
     * @param count the number of replacements to attempt
     * @return the number of replacements performed
     */
    public static int replace(Matcher m, Substitution substitution, TextBuffer dest, int count) {
        boolean firstPass = true;
        int c = 0;
        while (c < count && m.find()) {
            if (m.end() == 0 && !firstPass) continue;  //allow to replace at "^"
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            c++;
            m.setTarget(m, MatchResult.SUFFIX);
            firstPass = false;
        }
        m.getGroup(MatchResult.TARGET, dest);
        return c;
    }

    /**
     * Replaces the next occurrence of a matcher's pattern in a matcher's target by a given substitution, appending the
     * result to a buffer but not writing the remainder of m's match to the end of dest.
     * <br>
     * The substitution starts from current matcher's position, current match not included.
     * <br>
     * You typically want to call {@code m.getGroup(MatchResult.TARGET, dest);} after you have called replaceStep()
     * until it returns false, which will fill in the remainder of the matching text into dest.
     * @param m a Matcher
     * @param substitution a Substitution, typically a PerlSubstitution
     * @param dest the TextBuffer this will write to; see Replacer.wrap()
     * @return the number of replacements performed
     */
    public static boolean replaceStep(Matcher m, Substitution substitution, TextBuffer dest) {
        boolean firstPass = true;
        int c = 0, count = 1;
        while (c < count && m.find()) {
            if (m.end() == 0 && !firstPass) continue;  //allow to replace at "^"
            if (m.start() > 0) m.getGroup(MatchResult.PREFIX, dest);
            substitution.appendSubstitution(m, dest);
            c++;
            m.setTarget(m, MatchResult.SUFFIX);
            firstPass = false;
        }
        return c > 0;
    }

    @GwtIncompatible
    private static int replace(Matcher m, Substitution substitution, Writer out) throws IOException {
        try {
            return replace(m, substitution, wrap(out));
        } catch (WriteException e) {
            throw e.reason;
        }
    }

    @GwtIncompatible
    public void replace(CharSequence text, Writer out) throws IOException {
        replace(pattern.matcher(text), substitution, out);
    }

    @GwtIncompatible
    public void replace(char[] chars, int off, int len, Writer out) throws IOException {
        replace(pattern.matcher(chars, off, len), substitution, out);
    }

    @GwtIncompatible
    public void replace(MatchResult res, int group, Writer out) throws IOException {
        replace(pattern.matcher(res, group), substitution, out);
    }

    @GwtIncompatible
    public void replace(MatchResult res, String groupName, Writer out) throws IOException {
        replace(pattern.matcher(res, groupName), substitution, out);
    }

    @GwtIncompatible
    public void replace(Reader in, int length, Writer out) throws IOException {
        replace(pattern.matcher(in, length), substitution, out);
    }

    private static class DummySubstitution implements Substitution {
        String str;

        DummySubstitution(String s) {
            str = s;
        }

        public void appendSubstitution(MatchResult match, TextBuffer res) {
            if (str != null) res.append(str);
        }
    }
    private static class TableSubstitution implements Substitution
    {
        final LinkedHashMap<String, String> dictionary;

        TableSubstitution(LinkedHashMap<String, String> dict)
        {
            dictionary = dict;
        }

        TableSubstitution(String... dict)
        {
            dictionary = new LinkedHashMap<String, String>(dict.length / 2);
            for (int i = 0; i < dict.length - 1; i+=2) {
                dictionary.put(dict[i], dict[i+1]);
            }
        }
        @Override
        public void appendSubstitution(MatchResult match, TextBuffer dest) {
            String m = match.group(0);
            if(m == null)
                return;
            for (Map.Entry<String, String> kv : dictionary.entrySet()) {
                if (kv.getKey().equals(m)) {
                    dest.append(kv.getValue());
                    return;
                }
            }
            dest.append(m);
        }
    }

    /**
     * Makes a Replacer that replaces a literal String at index i in pairs with the String at index i+1. Doesn't need
     * escapes in the Strings it searches for (at index 0, 2, 4, etc.), but cannot search for the exact two characters
     * in immediate succession, backslash then capital E, because it finds literal Strings using {@code \\Q...\\E}.
     * Uses only default modes (not case-insensitive, and most other flags don't have any effect since this doesn't care
     * about "\\w" or other backslash-escaped special categories), but you can get the Pattern from this afterwards and
     * set its flags with its setFlags() method. The Strings this replaces with are at index 1, 3, 5, etc. and
     * correspond to the search String immediately before it; they are also literal.
     * @param pairs alternating search String, then replacement String, then search, replacement, etc.
     * @return a Replacer that will act as a replacement table for the given Strings
     */
    public static Replacer makeTable(String... pairs)
    {
        if(pairs == null || pairs.length < 2)
            return new Replacer(Pattern.compile("(.+)"), new DummySubstitution("\\1"));
        TableSubstitution tab = new TableSubstitution(pairs);
        StringBuilder sb = new StringBuilder(128);
        sb.append("(?>");
        for(String s : tab.dictionary.keySet())
        {
            sb.append("\\Q");
            sb.append(s);
            sb.append("\\E|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(')');
        return new Replacer(Pattern.compile(sb.toString()), tab);
    }

    /**
     * Makes a Replacer that replaces a literal String key in dict with the corresponding String value in dict. Doesn't
     * need escapes in the Strings it searches for (at index 0, 2, 4, etc.), but cannot search for the exact two
     * characters in immediate succession, backslash then capital E, because it finds literal Strings using
     * {@code \\Q...\\E}. Uses only default modes (not case-insensitive, and most other flags don't have any effect
     * since this doesn't care about "\\w" or other backslash-escaped special categories), but you can get the Pattern
     * from this afterwards and set its flags with its setFlags() method. The Strings this replaces with are the values,
     * and are also literal. If the Map this is given is a sorted Map of some kind or a (preferably) LinkedHashMap, then
     * the order search strings will be tried will be stable; the same is not necessarily true for HashMap.
     * @param dict a Map (hopefully with stable order) with search String keys and replacement String values
     * @return a Replacer that will act as a replacement table for the given Strings
     */
    public static Replacer makeTable(Map<String, String> dict)
    {
        if(dict == null || dict.isEmpty())
            return new Replacer(Pattern.compile("(.+)"), new DummySubstitution("\\1"));
        TableSubstitution tab = new TableSubstitution(new LinkedHashMap<String, String>(dict));
        StringBuilder sb = new StringBuilder(128);
        sb.append("(?>");
        for(String s : tab.dictionary.keySet())
        {
            sb.append("\\Q");
            sb.append(s);
            sb.append("\\E|");
        }
        sb.setCharAt(sb.length() - 1, ')');
        return new Replacer(Pattern.compile(sb.toString()), tab);
    }

    public static StringBuilderBuffer wrap(final StringBuilder sb) {
        return new StringBuilderBuffer(sb);
    }

    public static class StringBuilderBuffer implements TextBuffer, Serializable
    {
        private static final long serialVersionUID = 2589054766833218313L;

        public StringBuilder sb;

        public StringBuilderBuffer() {
            sb = new StringBuilder();
        }
        public StringBuilderBuffer(final StringBuilder builder) {
            sb = builder;
        }
        public void append(char c) {
            sb.append(c);
        }

        public void append(char[] chars, int start, int len) {
            sb.append(chars, start, len);
        }

        public void append(String s) {
            sb.append(s);
        }

        public String toString() {
            return sb.toString();
        }

        public StringBuilder toStringBuilder()
        {
            return sb;
        }
    }

    public static StringBufferBuffer wrap(final StringBuffer sb) {
        return new StringBufferBuffer(sb);
    }

    public static class StringBufferBuffer implements TextBuffer, Serializable
    {
        private static final long serialVersionUID = 2589054766833218313L;

        public StringBuffer sb;

        public StringBufferBuffer() {
            sb = new StringBuffer();
        }
        public StringBufferBuffer(final StringBuffer builder) {
            sb = builder;
        }
        public void append(char c) {
            sb.append(c);
        }

        public void append(char[] chars, int start, int len) {
            sb.append(chars, start, len);
        }

        public void append(String s) {
            sb.append(s);
        }

        public String toString() {
            return sb.toString();
        }

        public StringBuffer toStringBuffer()
        {
            return sb;
        }
    }

    @GwtIncompatible
    private static TextBuffer wrap(final Writer writer) {
        return new TextBuffer() {
            public void append(char c) {
                try {
                    writer.write(c);
                } catch (IOException e) {
                    throw new WriteException(e);
                }
            }

            public void append(char[] chars, int off, int len) {
                try {
                    writer.write(chars, off, len);
                } catch (IOException e) {
                    throw new WriteException(e);
                }
            }

            public void append(String s) {
                try {
                    writer.write(s);
                } catch (IOException e) {
                    throw new WriteException(e);
                }
            }
        };
    }

    private static class WriteException extends RuntimeException {
        IOException reason;

        WriteException(IOException io) {
            reason = io;
        }
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Substitution getSubstitution() {
        return substitution;
    }

    public void setSubstitution(Substitution substitution) {
        this.substitution = substitution;
    }

    public void setSubstitution(String substitution) {
        this.substitution = new PerlSubstitution(substitution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Replacer replacer = (Replacer) o;

        return pattern != null ? pattern.equals(replacer.pattern) : replacer.pattern == null && (substitution != null ? substitution.equals(replacer.substitution) : replacer.substitution == null);

    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (substitution != null ? substitution.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Replacer{" +
                "pattern=" + pattern +
                ", substitution=" + substitution +
                '}';
    }
}