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

public abstract class Buffer {
    private int mark = -1;
    private int position = 0;
    private int limit;
    private int capacity;

    Buffer(int mark, int pos, int lim, int cap) {
        if (cap < 0) throw new IllegalArgumentException("Negative capacity");
        if (mark > pos) throw new IllegalArgumentException("mark > position");
        this.capacity = cap;
        limit(lim);
        position(pos);
        if (mark >= 0) this.mark = mark;
    }

    public final int capacity() {
        return capacity;
    }

    public final int position() {
        return position;
    }

    public final Buffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0)) throw new IllegalArgumentException();
        position = newPosition;
        if (mark > position) mark = -1;
        return this;
    }

    public final int limit() {
        return limit;
    }

    public final Buffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0)) throw new IllegalArgumentException();
        limit = newLimit;
        if (position > limit) position = limit;
        if (mark > limit) mark = -1;
        return this;
    }

    public final Buffer mark() {
        mark = position;
        return this;
    }

    public final Buffer reset() {
        int m = mark;
        if (m < 0) throw new InvalidMarkException();
        position = m;
        return this;
    }

    public final Buffer clear() {
        position = 0;
        limit = capacity;
        mark = -1;
        return this;
    }

    public final Buffer flip() {
        limit = position;
        position = 0;
        mark = -1;
        return this;
    }

    public final Buffer rewind() {
        position = 0;
        mark = -1;
        return this;
    }

    public final int remaining() {
        return limit - position;
    }

    public final boolean hasRemaining() {
        return position < limit;
    }

    public abstract boolean isReadOnly();

    public abstract boolean hasArray();

    public abstract Object array();

    public abstract int arrayOffset();

    public abstract boolean isDirect();

    final int nextGetIndex() {
        if (position >= limit) throw new BufferUnderflowException();
        return position++;
    }

    final int nextGetIndex(int nb) {
        if (limit - position < nb) throw new BufferUnderflowException();
        int p = position;
        position += nb;
        return p;
    }

    final int nextPutIndex() {
        if (position >= limit) throw new BufferOverflowException();
        return position++;
    }

    final int nextPutIndex(int nb) {
        if (limit - position < nb) throw new BufferOverflowException();
        int p = position;
        position += nb;
        return p;
    }

    final int checkIndex(int i) {
        if ((i < 0) || (i >= limit)) throw new IndexOutOfBoundsException();
        return i;
    }

    final int checkIndex(int i, int nb) {
        if ((i < 0) || (nb > limit - i)) throw new IndexOutOfBoundsException();
        return i;
    }

    final int markValue() {                             // package-private
        return mark;
    }

    final void truncate() {
        mark = -1;
        position = 0;
        limit = 0;
        capacity = 0;
    }

    final void discardMark() {                          // package-private
        mark = -1;
    }
}