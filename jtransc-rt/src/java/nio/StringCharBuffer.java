/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

class StringCharBuffer extends CharBuffer {
    CharSequence str;

    StringCharBuffer(CharSequence s, int start, int end) {
        super(-1, start, end, s.length());
        int n = s.length();
        if ((start < 0) || (start > n) || (end < start) || (end > n)) throw new IndexOutOfBoundsException();
        str = s;
    }

    public CharBuffer slice() {
        return new StringCharBuffer(str, -1, 0, this.remaining(), this.remaining(), offset + this.position());
    }

    private StringCharBuffer(CharSequence s, int mark, int pos, int limit, int cap, int offset) {
        super(mark, pos, limit, cap, null, offset);
        str = s;
    }

    public CharBuffer duplicate() {
        return new StringCharBuffer(str, markValue(), position(), limit(), capacity(), offset);
    }

    public CharBuffer asReadOnlyBuffer() {
        return duplicate();
    }

    public final char get() {
        return str.charAt(nextGetIndex() + offset);
    }

    public final char get(int index) {
        return str.charAt(checkIndex(index) + offset);
    }

    char getUnchecked(int index) {
        return str.charAt(index + offset);
    }

    public final CharBuffer put(char c) {
        throw new ReadOnlyBufferException();
    }

    public final CharBuffer put(int index, char c) {
        throw new ReadOnlyBufferException();
    }

    public final CharBuffer compact() {
        throw new ReadOnlyBufferException();
    }

    public final boolean isReadOnly() {
        return true;
    }

    final String toString(int start, int end) {
        return str.toString().substring(start + offset, end + offset);
    }

    public final CharBuffer subSequence(int start, int end) {
        try {
            int pos = position();
            return new StringCharBuffer(str, -1, pos + checkIndex(start, pos), pos + checkIndex(end, pos), capacity(), offset);
        } catch (IllegalArgumentException x) {
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean isDirect() {
        return false;
    }

    public ByteOrder order() {
        return ByteOrder.nativeOrder();
    }

}
