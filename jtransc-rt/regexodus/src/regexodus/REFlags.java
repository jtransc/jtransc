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

public interface REFlags {
    /**
     * All the following options turned off, EXCEPT UNICODE. Unicode handling can be turned off with "-u" at the end
     * of a flag string, or by simply specifying only the flags you want in a bitmask, like
     * {@code (REFlags.IGNORE_CASE | REFlags.MULTILINE | REFlags.DOTALL)}.
     * <br>
     * This behavior changed between the 0.1.1 and 0.1.2 release.
     */
    int DEFAULT = 16;

    /**
     * Pattern "a" matches both "a" and "A".
     * Corresponds to "i" in Perl notation.
     */
    int IGNORE_CASE = 1 << 0;

    /**
     * Affects the behaviour of "^" and "$" tags. When switched off:
     * <ul>
     * <li> the "^" matches the beginning of the whole text;</li>
     * <li> the "$" matches the end of the whole text, or just before the '\n' or "\r\n" at the end of text.</li>
     * </ul>
     * When switched on:
     * <ul>
     * <li> the "^" additionally matches the line beginnings (that is just after the '\n');</li>
     * <li> the "$" additionally matches the line ends (that is just before "\r\n" or '\n');</li>
     * </ul>
     * Corresponds to "m" in Perl notation.
     */
    int MULTILINE = 1 << 1;

    /**
     * Affects the behaviour of dot(".") tag. When switched off:
     * <ul>
     * <li> the dot matches any character but EOLs('\r','\n');</li>
     * </ul>
     * When switched on:
     * <ul>
     * <li> the dot matches any character, including EOLs.</li>
     * </ul>
     * This flag is sometimes referenced in regex tutorials as SINGLELINE, which confusingly seems opposite to MULTILINE, but in fact is orthogonal.
     * Corresponds to "s" in Perl notation.
     */
    int DOTALL = 1 << 2;

    /**
     * Affects how the space characters are interpreted in the expression. When switched off:
     * <ul>
     * <li> the spaces are interpreted literally;</li>
     * </ul>
     * When switched on:
     * <ul>
     * <li> the spaces are ignored, allowing an expression to be slightly more readable.</li>
     * </ul>
     * Corresponds to "x" in Perl notation.
     */
    int IGNORE_SPACES = 1 << 3;

    /**
     * Affects whether the predefined classes("\d","\s","\w",etc) in the expression are interpreted as belonging to Unicode. When switched off:
     * <ul>
     * <li> the predefined classes are interpreted as ASCII;</li>
     * </ul>
     * When switched on:
     * <ul>
     * <li> the predefined classes are interpreted as Unicode categories;</li>
     * </ul>
     * Defaults to switched on, unlike the others. When specifying a flags with an int, however, UNICODE doesn't get
     * added automatically, so if you add a flag and want UNICODE on as well, you should specify it, too.
     * <br>
     * Corresponds to "u" in Perl notation.
     */
    int UNICODE = 1 << 4;

    /**
     * Turns on the compatibility with XML Schema regular expressions.
     * <br>
     * Corresponds to "X" in Perl notation.
     */
    int XML_SCHEMA = 1 << 5;


}