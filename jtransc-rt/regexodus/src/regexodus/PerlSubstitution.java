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

import java.io.Serializable;

/**
 * An implementation of the Substitution interface. Performs substitutions in accordance with Perl-like substitution scripts.<br>
 * The latter is a string, containing a mix of memory register references and plain text blocks.<br>
 * It may look like "some_chars $1 some_chars$2some_chars" or "123${1}45${2}67".<br>
 * A tag consisting of '$',not preceded by the escape character'\' and followed by some digits (possibly enclosed in the
 * curly brackets) is interpreted as a memory register reference, the digits forming a register ID. If you follow '$'
 * with curly brackets, you can also use the Java-identifier-like named groups that may have been defined in the search
 * pattern (as well as pure numbers for non-named register IDs), and can specify different modes for the replacement
 * with punctuation between the opening bracket and the group name. Modes can be specified by any combination or order
 * of '@' to get a case-folded (lower-case-only) replacement of the matched group, '/' to get the reverse of the string
 * matched by the group, and ':' to get any opening or closing bracket characters (Unicode categories Ps and Pe,
 * including parentheses, square brackets, and curly brackets among many others) in the matched group replaced by their
 * closing or opening counterparts. All the rest is considered plain text.<br>
 * Upon the Replacer has found a text block that matches the pattern, a references in a replacement string are replaced by the contents of 
 * corresponding memory registers, and the resulting text replaces the matched block.<br>
 * For example, the following code:
 * <pre>
 * System.out.println("\""+
 *    new Replacer(new Pattern("\\b(\\d+)\\b"),new PerlSubstitution("'$1'")).replace("abc 123 def")
 *    +"\"");
 * </pre>
 * will print <code>"abc '123' def"</code>.<br>
 * @see        Substitution
 * @see        Replacer
 * @see        Pattern
 */


public class PerlSubstitution implements Substitution, Serializable {
    //private static Pattern refPtn,argsPtn;
    private static final long serialVersionUID = -1537346657932720807L;

    private static Pattern refPtn;
    private static int MODE_ID;
    private static int NAME_ID;
    private static int ESC_ID;

    static final int MODE_INSENSITIVE = 1,
            MODE_REVERSE = 2,
            MODE_BRACKET = 4;

    //private static int FN_NAME_ID;
    //private static int FN_ARGS_ID;
    //private static int ARG_NAME_ID;

    private static final String groupRef = "\\$(?:\\{({=mode}\\p{Po}+)?({=name}\\w+)\\})|({=name}\\d+|\\&)|\\\\({esc}.)";
    //private static final String fnRef="\\&({fn_name}\\w+)\\(({fn_args}"+groupRef+"(?:,"+groupRef+")*)*\\)";

    static {
        try {
            //refPtn=new Pattern("(?<!\\\\)"+fnRef+"|"+groupRef);
            //argsPtn=new Pattern(groupRef);
            //refPtn=new Pattern("(?<!\\\\)"+groupRef);
            refPtn = new Pattern(groupRef);
            MODE_ID = refPtn.groupId("mode");
            NAME_ID = refPtn.groupId("name");
            ESC_ID = refPtn.groupId("esc");
            //ARG_NAME_ID=argsPtn.groupId("name").intValue();
            //FN_NAME_ID=refPtn.groupId("fn_name").intValue();
            //FN_ARGS_ID=refPtn.groupId("fn_args").intValue();
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
    }

    private Element queueEntry;

    //It seems we should somehow throw an IllegalArgumentException if an expression
    //holds a reference to a non-existing group. Such checking will require a Pattern instance.
    public PerlSubstitution() {
        this("");
    }
    public PerlSubstitution(String s) {
        Matcher refMatcher = new Matcher(refPtn);
        refMatcher.setTarget(s);
        queueEntry = makeQueue(refMatcher);
    }

    public String value(MatchResult mr) {
        TextBuffer dest = Replacer.wrap(new StringBuilder(mr.length()));
        appendSubstitution(mr, dest);
        return dest.toString();
    }

    private static Element makeQueue(Matcher refMatcher) {
        if (refMatcher.find()) {
            Element element;
            int modes = 0;
            if (refMatcher.isCaptured(NAME_ID)) {
                if(refMatcher.isCaptured(MODE_ID))
                {
                    String md = refMatcher.group(MODE_ID);
                    for (int i = 0; i < md.length(); i++) {
                        switch (md.charAt(i))
                        {
                            case '@': modes ^= MODE_INSENSITIVE;
                                break;
                            case '/': modes ^= MODE_REVERSE;
                                break;
                            case ':': modes ^= MODE_BRACKET;
                                break;
                        }
                    }
                }
                char c = refMatcher.charAt(0, NAME_ID);
                if (c == '&') {
                    element = new IntRefHandler(refMatcher.prefix(), 0, modes);
                } else if (Character.isDigit(c)) {
                    element = new IntRefHandler(refMatcher.prefix(), new Integer(refMatcher.group(NAME_ID)), modes);
                } else
                    element = new StringRefHandler(refMatcher.prefix(), refMatcher.group(NAME_ID), modes);
            } else {
                //escaped char
                element = new PlainElement(refMatcher.prefix(), refMatcher.group(ESC_ID));
            }
            refMatcher.setTarget(refMatcher, MatchResult.SUFFIX);
            element.next = makeQueue(refMatcher);
            return element;
        } else return new PlainElement(refMatcher.target());
    }

    public void appendSubstitution(MatchResult match, TextBuffer dest) {
        for (Element element = this.queueEntry; element != null; element = element.next) {
            element.append(match, dest);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Element element = this.queueEntry; element != null; element = element.next) {
            sb.append(element.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PerlSubstitution that = (PerlSubstitution) o;

        return queueEntry != null ? queueEntry.equals(that.queueEntry) : that.queueEntry == null;

    }

    @Override
    public int hashCode() {
        return queueEntry != null ? queueEntry.hashCode() : 0;
    }

    private static abstract class Element {
        String prefix;
        Element next;

        abstract void append(MatchResult match, TextBuffer dest);
    }

    private static class PlainElement extends Element {
        private String str;

        PlainElement(String s) {
            str = s;
        }

        PlainElement(String pref, String s) {
            prefix = pref;
            str = s;
        }

        void append(MatchResult match, TextBuffer dest) {
            if (prefix != null) dest.append(prefix);
            if (str != null) dest.append(str);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlainElement that = (PlainElement) o;

            return str != null ? str.equals(that.str) : that.str == null;

        }

        @Override
        public int hashCode() {
            return str != null ? str.hashCode() : 0;
        }
    }

    private static class IntRefHandler extends Element {
        private Integer index;
        private int modes;

        IntRefHandler(String s, Integer ind, int modes) {
            prefix = s;
            index = ind;
            this.modes = modes;
        }

        void append(MatchResult match, TextBuffer dest) {
            if (prefix != null) dest.append(prefix);
            if (index == null) return;
            int i = index;
            if (i >= match.pattern().groupCount()) return;
            if (match.isCaptured(i)) match.getGroup(i, dest, modes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IntRefHandler that = (IntRefHandler) o;

            if (modes != that.modes) return false;
            return index != null ? index.equals(that.index) : that.index == null;

        }

        @Override
        public int hashCode() {
            int result = index != null ? index.hashCode() : 0;
            result = 31 * result + modes;
            return result;
        }
    }

    private static class StringRefHandler extends Element {
        private String index;
        private int modes;
        StringRefHandler(String s, String ind, int modes) {
            prefix = s;
            index = ind;
            this.modes = modes;
        }

        void append(MatchResult match, TextBuffer dest) {
            if (prefix != null) dest.append(prefix);
            if (index == null) return;
            //if(id==null) return; //???
            int i = match.pattern().groupId(index);
            if (match.isCaptured(i)) match.getGroup(i, dest, modes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StringRefHandler that = (StringRefHandler) o;

            if (modes != that.modes) return false;
            return index != null ? index.equals(that.index) : that.index == null;

        }

        @Override
        public int hashCode() {
            int result = index != null ? index.hashCode() : 0;
            result = 31 * result + modes;
            return result;
        }
    }

}
