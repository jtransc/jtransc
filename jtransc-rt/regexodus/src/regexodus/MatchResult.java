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

import com.jtransc.annotation.JTranscInvisible;

@JTranscInvisible
public interface MatchResult extends regexodus.regex.MatchResult{
    int MATCH = 0;
    int PREFIX = -1;
    int SUFFIX = -2;
    int TARGET = -3;

    Pattern pattern();

    int groupCount();

    boolean isCaptured();

    boolean isCaptured(int groupId);

    boolean isCaptured(String groupName);

    String group();

    String group(int group);

    boolean getGroup(int group, StringBuilder sb, int modes);

    boolean getGroup(int group, TextBuffer tb, int modes);

    boolean getGroup(int group, StringBuilder sb);

    boolean getGroup(int group, TextBuffer tb);

    String group(String name);

    boolean getGroup(String name, StringBuilder sb, int modes);

    boolean getGroup(String name, TextBuffer tb, int modes);

    boolean getGroup(String name, StringBuilder sb);

    boolean getGroup(String name, TextBuffer tb);

    String prefix();

    String suffix();

    String target();

    int targetStart();

    int targetEnd();

    char[] targetChars();

    int start();

    int end();

    int length();

    int start(int group);

    int end(int group);

    int length(int group);

    char charAt(int i);

    char charAt(int i, int groupNo);
}