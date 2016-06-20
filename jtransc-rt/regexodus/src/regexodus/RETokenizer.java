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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The Tokenizer class suggests a methods to break a text into tokens using
 * occurrences of a pattern as delimiters.
 * There are two ways to obtain a text tokenizer for some pattern:<pre>
 * Pattern p=new Pattern("\\s+"); //any number of space characters
 * String text="blah blah blah";
 * //by factory method
 * RETokenizer tok1=p.tokenizer(text);
 * //or by constructor
 * RETokenizer tok2=new RETokenizer(p,text);
 * </pre>
 * Now the one way is to use the tokenizer as a token enumeration/iterator:<pre>
 * while(tok1.hasMore()) System.out.println(tok1.nextToken());
 * </pre>
 * and another way is to split it into a String array:
 * {@code
 * String[] arr=tok2.split();
 * for(int i=0;i<tok2.length;i++) System.out.println(arr[i]);}
 *
 * @see Pattern#tokenizer(java.lang.String)
 */

public class RETokenizer implements Iterator<String> {
    private Matcher matcher;
    private boolean checked;
    private boolean hasToken;
    private String token;
    private int pos = 0;
    private boolean endReached = false;
    private boolean emptyTokensEnabled = false;

    public RETokenizer(Pattern pattern, String text) {
        this(pattern.matcher(text), false);
    }

    public RETokenizer(Pattern pattern, char[] chars, int off, int len) {
        this(pattern.matcher(chars, off, len), false);
    }

    @GwtIncompatible
    public RETokenizer(Pattern pattern, Reader r, int len) throws IOException {
        this(pattern.matcher(r, len), false);
    }

    private RETokenizer(Matcher m, boolean emptyEnabled) {
        matcher = m;
        emptyTokensEnabled = emptyEnabled;
    }

    public void setEmptyEnabled(boolean b) {
        emptyTokensEnabled = b;
    }

    public boolean isEmptyEnabled() {
        return emptyTokensEnabled;
    }

    private boolean hasMore() {
        if (!checked) check();
        return hasToken;
    }

    private String nextToken() {
        if (!checked) check();
        if (!hasToken) throw new NoSuchElementException();
        checked = false;
        return token;
    }

    public String[] split() {
        return collect(this, null, 0);
    }

    public void reset() {
        matcher.setPosition(0);
    }

    private static String[] collect(RETokenizer tok, String[] arr, int count) {
        if (tok.hasMore()) {
            String s = tok.nextToken();
//System.out.println("collect(,,"+count+"): token="+s);
            arr = collect(tok, arr, count + 1);
            arr[count] = s;
        } else {
            arr = new String[count];
        }
        return arr;
    }

    private void check() {
        final boolean emptyOk = this.emptyTokensEnabled;
        checked = true;
        if (endReached) {
            hasToken = false;
            return;
        }
        Matcher m = matcher;
        boolean hasMatch = false;
        while (m.find()) {
            if (m.start() > 0) {
                hasMatch = true;
                break;
            } else if (m.end() > 0) {
                if (emptyOk) {
                    hasMatch = true;
                    break;
                } else m.setTarget(m, MatchResult.SUFFIX);
            }
        }
        if (!hasMatch) {
            endReached = true;
            if (m.length(MatchResult.TARGET) == 0 && !emptyOk) {
                hasToken = false;
            } else {
                hasToken = true;
                token = m.target();
            }
            return;
        }

        hasToken = true;
        token = m.prefix();
        m.setTarget(m, MatchResult.SUFFIX);
        //m.setTarget(m.suffix());
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator (optional operation).  This method can be called
     * only once per call to {@link #next}.  The behavior of an iterator
     * is unspecified if the underlying collection is modified while the
     * iteration is in progress in any way other than by calling this
     * method.
     *
     * @throws UnsupportedOperationException if the {@code remove}
     *                                       operation is not supported by this iterator
     * @throws IllegalStateException         if the {@code next} method has not
     *                                       yet been called, or the {@code remove} method has already
     *                                       been called after the last call to the {@code next}
     *                                       method
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() not supported on RETokenizer");
    }

    @Override
    public boolean hasNext() {
        return hasMore();
    }

    /**
     * @return a next token as a String
     */
    @Override
    public String next() {
        return nextToken();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RETokenizer that = (RETokenizer) o;

        if (checked != that.checked) return false;
        if (hasToken != that.hasToken) return false;
        if (pos != that.pos) return false;
        if (endReached != that.endReached) return false;
        if (emptyTokensEnabled != that.emptyTokensEnabled) return false;
        if (matcher != null ? !matcher.equals(that.matcher) : that.matcher != null) return false;
        return token != null ? token.equals(that.token) : that.token == null;

    }

    @Override
    public int hashCode() {
        int result = matcher != null ? matcher.hashCode() : 0;
        result = 31 * result + (checked ? 1 : 0);
        result = 31 * result + (hasToken ? 1 : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + pos;
        result = 31 * result + (endReached ? 1 : 0);
        result = 31 * result + (emptyTokensEnabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RETokenizer{" +
                "matcher=" + matcher +
                ", checked=" + checked +
                ", hasToken=" + hasToken +
                ", token='" + token + '\'' +
                ", pos=" + pos +
                ", endReached=" + endReached +
                ", emptyTokensEnabled=" + emptyTokensEnabled +
                '}';
    }
}