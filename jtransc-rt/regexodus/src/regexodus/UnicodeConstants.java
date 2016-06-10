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

public interface UnicodeConstants {
    int CATEGORY_COUNT = 32;
    int Cc = 15;//Character.CONTROL;
    int Cf = 16;//Character.FORMAT;
    int Co = 18;//Character.PRIVATE_USE;
    int Cn = 0;//Character.UNASSIGNED;
    int Lu = 1;//Character.UPPERCASE_LETTER;
    int Ll = 2;//Character.LOWERCASE_LETTER;
    int Lt = 3;//Character.TITLECASE_LETTER;
    int Lm = 4;//Character.MODIFIER_LETTER;
    int Lo = 5;//Character.OTHER_LETTER;
    int Mn = 6;//Character.NON_SPACING_MARK;
    int Me = 7;//Character.ENCLOSING_MARK;
    int Mc = 8;//Character.COMBINING_SPACING_MARK;
    int Nd = 9;//Character.DECIMAL_DIGIT_NUMBER;
    int Nl = 10;//Character.LETTER_NUMBER;
    int No = 11;//Character.OTHER_NUMBER;
    int Zs = 12;//Character.SPACE_SEPARATOR;
    int Zl = 13;//Character.LINE_SEPARATOR;
    int Zp = 14;//Character.PARAGRAPH_SEPARATOR;
    int Cs = 19;//Character.SURROGATE;
    int Pd = 20;//Character.DASH_PUNCTUATION;
    int Ps = 21;// Character.START_PUNCTUATION;
    int Pi = 29;//Character.INITIAL_QUOTE_PUNCTUATION;
    int Pe = 22;//Character.END_PUNCTUATION;
    int Pf = 30;//Character.FINAL_QUOTE_PUNCTUATION;
    int Pc = 23;//Character.CONNECTOR_PUNCTUATION;
    int Po = 24;//Character.OTHER_PUNCTUATION;
    int Sm = 25;//Character.MATH_SYMBOL;
    int Sc = 26;//Character.CURRENCY_SYMBOL;
    int Sk = 27;//Character.MODIFIER_SYMBOL;
    int So = 28;//Character.OTHER_SYMBOL;

    int BLOCK_COUNT = 256;
    int BLOCK_SIZE = 256;

    int MAX_WEIGHT = Character.MAX_VALUE + 1;
}