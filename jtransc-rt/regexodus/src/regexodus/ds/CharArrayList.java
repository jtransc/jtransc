/*
 * Copyright (C) 2002-2015 Sebastiano Vigna
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package regexodus.ds;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * A type-specific array-based list; provides some additional methods that use polymorphism to avoid (un)boxing.
 * <br>This class implements a lightweight, fast, open, optimized, reuse-oriented version of array-based lists. Instances of this class represent a list with an array that is enlarged as needed when
 * new entries are created (by doubling its current length), but is <em>never</em> made smaller (even on a {@link #clear()}). A family of {@linkplain #trim() trimming methods} lets you control the size
 * of the backing array; this is particularly useful if you reuse instances of this class. Range checks are equivalent to those of {@link java.util}'s classes, but they are delayed as much as
 * possible. The backing array is exposed by the {@link #elements()} method.
 * <br>This class implements the bulk methods <code>removeElements()</code>, <code>addElements()</code> and <code>getElements()</code> using high-performance system calls (e.g.,
 * {@link System#arraycopy(Object, int, Object, int, int) System.arraycopy()} instead of expensive loops.
 *
 * @see java.util.ArrayList
 */
public class CharArrayList implements RandomAccess, Cloneable, java.io.Serializable, List<Character>, Comparable<List<? extends Character>> {
    private static final long serialVersionUID = -7046029254386353130L;
    /**
     * The initial default capacity of an array list.
     */
    private final static int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * The backing array.
     */
    private transient char[] a;
    /**
     * The current actual size of the list (never greater than the backing-array length).
     */
    private int size;
    //private static final boolean ASSERTS = false;

    /**
     * Creates a new array list using a given array.
     * <br>This constructor is only meant to be used by the wrapping methods.
     *
     * @param a the array that will be used to back this array list.
     */
    protected CharArrayList(final char a[], boolean dummy) {
        this.a = a;
    }

    /**
     * Creates a new array list with given capacity.
     *
     * @param capacity the initial capacity of the array list (may be 0).
     */

    public CharArrayList(final int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
        a = new char[capacity];
    }

    /**
     * Creates a new array list with {@link #DEFAULT_INITIAL_CAPACITY} capacity.
     */
    private CharArrayList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Creates a new array list and fills it with a given type-specific list.
     *
     * @param l a type-specific list that will be used to fill the array list.
     */
    public CharArrayList(final CharArrayList l) {
        this(l.size());
        l.getElements(0, a, 0, size = l.size());
    }

    /**
     * Creates a new array list and fills it with the elements of a given array or vararg.
     *
     * @param a an array or vararg whose elements will be used to fill the array list.
     */
    private CharArrayList(final char... a) {
        this(a, 0, a.length);
    }

    /**
     * Creates a new array list and fills it with the elements of a given String.
     *
     * @param s a String whose char elements will be used to fill the array list.
     */
    public CharArrayList(final String s) { this(s.toCharArray());}
    /**
     * Creates a new array list and fills it with the elements of a given array.
     *
     * @param a      an array whose elements will be used to fill the array list.
     * @param offset the first element to use.
     * @param length the number of elements to use.
     */
    private CharArrayList(final char a[], final int offset, final int length) {
        this(length);
        System.arraycopy(a, offset, this.a, 0, length);
        size = length;
    }

    /**
     * Creates a new array list and fills it with the elements returned by an iterator..
     *
     * @param i an iterator whose returned elements will fill the array list.
     */
    public CharArrayList(final Iterator<? extends Character> i) {
        this();
        while (i.hasNext())
            this.add(i.next());
    }

    /**
     * Returns the backing array of this list.
     *
     * @return the backing array.
     */
    public char[] elements() {
        return a;
    }

    /**
     * Wraps a given array into an array list of given size.
     * <br>Note it is guaranteed that the type of the array returned by {@link #elements()} will be the same (see the comments in the class documentation).
     *
     * @param a      an array to wrap.
     * @param length the length of the resulting array list.
     * @return a new array list of the given size, wrapping the given array.
     */
    private static CharArrayList wrap(final char a[], final int length) {
        if (length > a.length)
            throw new IllegalArgumentException("The specified length (" + length + ") is greater than the array size (" + a.length + ")");
        final CharArrayList l = new CharArrayList(a, false);
        l.size = length;
        return l;
    }

    /**
     * Wraps a given array into an array list.
     * <br>Note it is guaranteed that the type of the array returned by {@link #elements()} will be the same (see the comments in the class documentation).
     *
     * @param a an array to wrap.
     * @return a new array list wrapping the given array.
     */
    public static CharArrayList wrap(final char a[]) {
        return wrap(a, a.length);
    }

    /**
     * Ensures that this array list can contain the given number of entries without resizing.
     *
     * @param capacity the new minimum capacity for this array list.
     */

    private void ensureCapacity(final int capacity) {
        a = CharArrays.ensureCapacity(a, capacity, size);
        //if (ASSERTS) assert size <= a.length;
    }

    /**
     * Grows this array list, ensuring that it can contain the given number of entries without resizing, and in case enlarging it at least by a factor of two.
     *
     * @param capacity the new minimum capacity for this array list.
     */

    private void grow(final int capacity) {
        a = CharArrays.grow(a, capacity, size);
        //if (ASSERTS) assert size <= a.length;
    }

    private void add(final int index, final char k) {
        ensureIndex(index);
        grow(size + 1);
        if (index != size) System.arraycopy(a, index, a, index + 1, size - index);
        a[index] = k;
        size++;
        //if (ASSERTS) assert size <= a.length;
    }

    private boolean add(final char k) {
        grow(size + 1);
        a[size++] = k;
        //if (ASSERTS) assert size <= a.length;
        return true;
    }
    public boolean add(final int k) {
        grow(size + 1);
        a[size++] = (char)k;
        //if (ASSERTS) assert size <= a.length;
        return true;
    }

    public char getChar(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        return a[index];
    }

    public int getCodePoint(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        return a[index];
    }

    private int indexOf(final char k) {
        for (int i = 0; i < size; i++)
            if (((k) == (a[i]))) return i;
        return -1;
    }

    public int lastIndexOf(final char k) {
        for (int i = size; i-- != 0; )
            if (((k) == (a[i]))) return i;
        return -1;
    }

    private char removeChar(final int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        final char old = a[index];
        size--;
        if (index != size) System.arraycopy(a, index + 1, a, index, size - index);
        //if (ASSERTS) assert size <= a.length;
        return old;
    }

    private boolean rem(final char k) {
        int index = indexOf(k);
        if (index == -1) return false;
        removeChar(index);
        //if (ASSERTS) assert size <= a.length;
        return true;
    }

    /**
     * Delegates to the corresponding type-specific method.
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    public Character set(final int index, final Character ok) {
        return set(index, ok.charValue());
    }

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).  Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index   index at which the specified element is to be inserted
     * @param element element to be inserted
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this list
     * @throws NullPointerException          if the specified element is null and
     *                                       this list does not permit null elements
     * @throws IllegalArgumentException      if some property of the specified
     *                                       element prevents it from being added to this list
     * @throws IndexOutOfBoundsException     if the index is out of range
     *                                       (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    @Override
    public void add(int index, Character element) {

    }

    /**
     * Removes the element at the specified position in this list (optional
     * operation).  Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this list
     * @throws IndexOutOfBoundsException     if the index is out of range
     *                                       (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    @Override
    public Character remove(int index) {
        return null;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              list does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public int indexOf(Object o) {
        return 0;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the highest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in
     * this list, or -1 if this list does not contain the element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this list
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *                              list does not permit null elements
     *                              (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    @Override
    public int lastIndexOf(Object o) {
        return 0;
    }

    private char set(final int index, final char k) {
        if (index >= size)
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + size + ")");
        char old = a[index];
        a[index] = k;
        return old;
    }

    public boolean addAll(int index, final Collection<? extends Character> c) {
        ensureIndex(index);
        int n = c.size();
        if (n == 0) return false;
        Iterator<? extends Character> i = c.iterator();
        while (n-- != 0)
            add(index++, i.next());
        return true;
    }

    /**
     * Removes from this list all of its elements that are contained in the
     * specified collection (optional operation).
     *
     * @param c collection containing elements to be removed from this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>removeAll</tt> operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of this list
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this list contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this list all of its elements that are not contained in the
     * specified collection.
     *
     * @param c collection containing elements to be retained in this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>retainAll</tt> operation
     *                                       is not supported by this list
     * @throws ClassCastException            if the class of an element of this list
     *                                       is incompatible with the specified collection
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException          if this list contains a null element and the
     *                                       specified collection does not permit null elements
     *                                       (<a href="Collection.html#optional-restrictions">optional</a>),
     *                                       or if the specified collection is null
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public void clear() {
        size = 0;
        //if (ASSERTS) assert size <= a.length;
    }

    public int size() {
        return size;
    }

    public void size(final int size) {
        if (size > a.length) ensureCapacity(size);
        if (size > this.size) Arrays.fill(a, this.size, size, ((char) 0));
        this.size = size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Trims this array list so that the capacity is equal to the size.
     *
     * @see java.util.ArrayList#trimToSize()
     */
    public void trim() {
        trim(0);
    }

    /**
     * Trims the backing array if it is too large.
     * <br>
     * If the current array length is smaller than or equal to <code>n</code>, this method does nothing. Otherwise, it trims the array length to the maximum between <code>n</code> and {@link #size()}.
     * <br>This method is useful when reusing lists. {@linkplain #clear() Clearing a list} leaves the array length untouched. If you are reusing a list many times, you can call this method with a
     * typical size to avoid keeping around a very large array just because of a few large transient lists.
     *
     * @param n the threshold for the trimming.
     */

    private void trim(final int n) {
        if (n >= a.length || size == a.length) return;
        final char t[] = new char[Math.max(n, size)];
        System.arraycopy(a, 0, t, 0, size);
        a = t;
        //if (ASSERTS) assert size <= a.length;
    }

    /**
     * Copies element of this type-specific list into the given array using optimized system calls.
     *
     * @param from   the start index (inclusive).
     * @param a      the destination array.
     * @param offset the offset into the destination array where to store the first element copied.
     * @param length the number of elements to be copied.
     */
    private void getElements(final int from, final char[] a, final int offset, final int length) {
        CharArrays.ensureOffsetLength(a, offset, length);
        System.arraycopy(this.a, from, a, offset, length);
    }


    /**
     * Removes elements of this type-specific list using optimized system calls.
     *
     * @param from the start index (inclusive).
     * @param to   the end index (exclusive).
     */
    public void removeElements(final int from, final int to) {
        CharArrays.ensureFromTo(size, from, to);
        System.arraycopy(a, to, a, from, size - to);
        size -= (to - from);
    }

    /**
     * Adds elements to this type-specific list using optimized system calls.
     *
     * @param index  the index at which to add elements.
     * @param a      the array containing the elements.
     * @param offset the offset of the first element to add.
     * @param length the number of elements to add.
     */
    public void addElements(final int index, final char a[], final int offset, final int length) {
        ensureIndex(index);
        CharArrays.ensureOffsetLength(a, offset, length);
        grow(size + length);
        System.arraycopy(this.a, index, this.a, index + length, size - index);
        System.arraycopy(a, offset, this.a, index, length);
        size += length;
    }

    private char[] toCharArray(char a[]) {
        if (a == null || a.length < size) a = new char[size];
        System.arraycopy(this.a, 0, a, 0, size);
        return a;
    }

    public boolean addAll(final int index, final CharArrayList l) {
        ensureIndex(index);
        final int n = l.size();
        if (n == 0) return false;
        grow(size + n);
        if (index != size) System.arraycopy(a, index, a, index + n, size - index);
        l.getElements(0, a, index, n);
        size += n;
        //if (ASSERTS) assert size <= a.length;
        return true;
    }

    public class CharListIterator implements Iterator<Character>, ListIterator<Character> {

        public CharListIterator(int idx) {
            index = idx;
        }

        /**
         * Delegates to the corresponding type-specific method.
         */
        public Character previous() {
            return previousChar();
        }

        int index, pos = index, last = -1;

        public boolean hasNext() {
            return pos < size;
        }

        public boolean hasPrevious() {
            return pos > 0;
        }

        public char nextChar() {
            if (!hasNext()) throw new NoSuchElementException();
            return a[last = pos++];
        }

        public char previousChar() {
            if (!hasPrevious()) throw new NoSuchElementException();
            return a[last = --pos];
        }

        public int nextIndex() {
            return pos;
        }

        public int previousIndex() {
            return pos - 1;
        }

        /**
         * Delegates to the corresponding type-specific method.
         */
        public void set(Character ok) {
            set(ok.charValue());
        }

        /**
         * Delegates to the corresponding type-specific method.
         */
        public void add(Character ok) {
            add(ok.charValue());
        }

        public void add(char k) {
            if (last == -1) throw new IllegalStateException();
            CharArrayList.this.add(pos++, k);
            last = -1;
        }


        public void set(char k) {
            if (last == -1) throw new IllegalStateException();
            CharArrayList.this.set(last, k);
        }

        public void remove() {
            if (last == -1) throw new IllegalStateException();
            CharArrayList.this.removeChar(last);
                /* If the last operation was a next(), we are removing an element *before* us, and we must decrease pos correspondingly. */
            if (last < pos) pos--;
            last = -1;
        }

        /**
         * Delegates to the corresponding type-specific method.
         *
         * @deprecated Please use the corresponding type-specific method instead.
         */
        @Deprecated
        public Character next() {
            return nextChar();
        }

        /**
         * This method just iterates the type-specific version of {@link #next()} for at most <code>n</code> times, stopping if {@link #hasNext()} becomes false.
         */
        public int skip(final int n) {
            int i = n;
            while (i-- != 0 && hasNext())
                nextChar();
            return n - i - 1;
        }

    }

    public CharListIterator iterator() {
        return listIterator();
    }

    public CharListIterator listIterator() {
        return listIterator(0);
    }

    public CharListIterator listIterator(final int index) {
        ensureIndex(index);
        return new CharListIterator(index);
    }

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.)  The returned list is backed by this list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.
     * The returned list supports all of the optional list operations supported
     * by this list.
     * <br>
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).  Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom
     * removes a range of elements from a list:
     * <pre>{@code
     *      list.subList(from, to).clear();
     * }</pre>
     * Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the
     * <tt>Collections</tt> class can be applied to a subList.
     * <br>
     * The semantics of the list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex   high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</tt>)
     */
    @Override
    public List<Character> subList(int fromIndex, int toIndex) {
        return new CharArrayList(a, fromIndex, toIndex - fromIndex);
    }

    public CharArrayList clone() {
        CharArrayList c = new CharArrayList(size);
        System.arraycopy(a, 0, c.a, 0, size);
        c.size = size;
        return c;
    }

    /**
     * Compares this type-specific array list to another one.
     * <br>This method exists only for sake of efficiency. The implementation inherited from the abstract implementation would already work.
     *
     * @param l a type-specific array list.
     * @return true if the argument contains the same elements of this type-specific array list.
     */
    public boolean equals(final CharArrayList l) {
        if (l == this) return true;
        int s = size();
        if (s != l.size()) return false;
        final char[] a1 = a;
        final char[] a2 = l.a;
        while (s-- != 0)
            if (a1[s] != a2[s]) return false;
        return true;
    }

    /**
     * Compares this array list to another array list.
     * <br>This method exists only for sake of efficiency. The implementation inherited from the abstract implementation would already work.
     *
     * @param l an array list.
     * @return a negative integer, zero, or a positive integer as this list is lexicographically less than, equal to, or greater than the argument.
     */

    public int compareTo(final CharArrayList l) {
        final int s1 = size(), s2 = l.size();
        final char a1[] = a, a2[] = l.a;
        char e1, e2;
        int r, i;
        for (i = 0; i < s1 && i < s2; i++) {
            e1 = a1[i];
            e2 = a2[i];
            if ((r = (e1 - e2)) != 0) return r;
        }
        return i < s2 ? -1 : (i < s1 ? 1 : 0);
    }

    /**
     * Compares this list to another object. If the argument is a {@link java.util.List}, this method performs a lexicographical comparison; otherwise, it throws a <code>ClassCastException</code>.
     *
     * @param l a list.
     * @return if the argument is a {@link java.util.List}, a negative integer, zero, or a positive integer as this list is lexicographically less than, equal to, or greater than the argument.
     * @throws ClassCastException if the argument is not a list.
     */

    public int compareTo(final List<? extends Character> l) {
        if (l == this) return 0;
        if (l instanceof CharArrayList) {
            final CharListIterator i1 = listIterator(), i2 = ((CharArrayList) l).listIterator();
            int r;
            char e1, e2;
            while (i1.hasNext() && i2.hasNext()) {
                e1 = i1.nextChar();
                e2 = i2.nextChar();
                if ((r = (e1 - e2)) != 0) return r;
            }
            return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
        }
        ListIterator<? extends Character> i1 = listIterator(), i2 = l.listIterator();
        int r;
        while (i1.hasNext() && i2.hasNext()) {
            if ((r = ((Comparable<? super Character>) i1.next()).compareTo(i2.next())) != 0) return r;
        }
        return i2.hasNext() ? -1 : (i1.hasNext() ? 1 : 0);
    }

    /**
     * Returns the hash code for this list, which is identical to {@link java.util.List#hashCode()}.
     *
     * @return the hash code for this list.
     */
    public int hashCode() {
        CharListIterator i = iterator();
        int h = 1, s = size();
        while (s-- != 0) {
            char k = i.nextChar();
            h = 31 * h + (k);
        }
        return h;
    }


    /**
     * Delegates to the corresponding type-specific method.
     *
     * @deprecated Please use the corresponding type-specific method instead.
     */
    @Deprecated
    public Character get(final int index) {
        return getChar(index);
    }


    /**
     * Ensures that the given index is non-negative and not greater than the list size.
     *
     * @param index an index.
     * @throws IndexOutOfBoundsException if the given index is negative or greater than the list size.
     */
    private void ensureIndex(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
        if (index > size())
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than list size (" + (size()) + ")");
    }

    /**
     * Ensures that the given index is non-negative and smaller than the list size.
     *
     * @param index an index.
     * @throws IndexOutOfBoundsException if the given index is negative or not smaller than the list size.
     */
    protected void ensureRestrictedIndex(final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("Index (" + index + ") is negative");
        if (index >= size())
            throw new IndexOutOfBoundsException("Index (" + index + ") is greater than or equal to list size (" + (size()) + ")");
    }


    private boolean contains(final char k) {
        return indexOf(k) >= 0;
    }

    /**
     * Delegates to the corresponding type-specific method.
     */
    public boolean contains(final Object o) {
        return contains(((((Character) (o)).charValue())));
    }


    public Object[] toArray() {
        final Object[] a = new Object[size()];
        objectUnwrap(iterator(), a);
        return a;
    }

    /**
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element); the runtime type of
     * the returned array is that of the specified array.  If the list fits
     * in the specified array, it is returned therein.  Otherwise, a new
     * array is allocated with the runtime type of the specified array and
     * the size of this list.
     * <br>If the list fits in the specified array with room to spare (i.e.,
     * the array has more elements than the list), the element in the array
     * immediately following the end of the list is set to <tt>null</tt>.
     * (This is useful in determining the length of the list <i>only</i> if
     * the caller knows that the list does not contain any null elements.)
     * <br>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     * <br>Suppose <tt>x</tt> is a list known to contain only strings.
     * The following code can be used to dump the list into a newly
     * allocated array of <tt>String</tt>:
     * <br>
     * <pre>{@code
     *     String[] y = x.toArray(new String[0]);
     * }</pre>
     * <br>
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of this list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return an array containing the elements of this list
     * @throws ArrayStoreException  if the runtime type of the specified array
     *                              is not a supertype of the runtime type of every element in
     *                              this list
     * @throws NullPointerException if the specified array is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        Character[] b;
        if (a.length < size()) b = new Character[size()];
        else b = (Character[]) a;
        objectUnwrap(iterator(), b);
        return (T[]) b;

    }

    public Character[] toArray(Character[] a) {
        if (a.length < size()) a = new Character[size()];
        objectUnwrap(iterator(), a);
        return a;
    }


    /**
     * Delegates to the corresponding type-specific method.
     */
    public boolean add(final Character o) {
        return add(o.charValue());
    }


    /**
     * Delegates to the type-specific <code>rem()</code> method.
     */
    public boolean remove(Object ok) {
        return rem((((Character) (ok))));
    }


    /**
     * Checks whether this collection contains all elements from the given collection.
     *
     * @param c a collection.
     * @return <code>true</code> if this collection contains all elements of the argument.
     */
    public boolean containsAll(Collection<?> c) {
        int n = c.size();
        final Iterator<?> i = c.iterator();
        while (n-- != 0)
            if (!contains(i.next())) return false;
        return true;
    }

    /**
     * Adds all elements of the given collection to this collection.
     *
     * @param c a collection.
     * @return <code>true</code> if this collection changed as a result of the call.
     */
    public boolean addAll(Collection<? extends Character> c) {
        boolean retVal = false;
        final Iterator<? extends Character> i = c.iterator();
        int n = c.size();
        while (n-- != 0)
            if (add(i.next())) retVal = true;
        return retVal;
    }


    /**
     * Unwraps an iterator into an array starting at a given offset for a given number of elements.
     * <br>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i     a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped.
     */
    private static <K> int objectUnwrap(final Iterator<? extends K> i, final K array[]) {
        int j = array.length, offset = 0;
        while (j-- != 0 && i.hasNext())
            array[offset++] = i.next();
        return array.length - j - 1;
    }


    public char[] toArray(char a[]) {
        return toCharArray(a);
    }

    public char[] toCharArray() {
        return toCharArray(null);
    }

    @Override
    public String toString() {
        return new String(a, 0, size);
    }

    /**
     * Unwraps an iterator into an array starting at a given offset for a given number of elements.
     * <br>This method iterates over the given type-specific iterator and stores the elements returned, up to a maximum of <code>length</code>, in the given array starting at <code>offset</code>. The
     * number of actually unwrapped elements is returned (it may be less than <code>max</code> if the iterator emits less than <code>max</code> elements).
     *
     * @param i     a type-specific iterator.
     * @param array an array to contain the output of the iterator.
     * @return the number of elements unwrapped.
     */
    public static int unwrap(final CharListIterator i, final char array[]) {
        int j = array.length, offset = 0;
        while (j-- != 0 && i.hasNext())
            array[offset++] = i.nextChar();
        return array.length - j - 1;
    }


    private static class CharArrays    /** A static, final, empty array. */
    {
        public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
        public final static char[] EMPTY_ARRAY = {};


        /**
         * Ensures that an array can contain the given number of entries.
         * <br>If you cannot foresee whether this array will need again to be enlarged, you should probably use <code>grow()</code> instead.
         *
         * @param array  an array.
         * @param length the new minimum length for this array.
         * @return <code>array</code>, if it contains <code>length</code> entries or more; otherwise, an array with <code>length</code> entries whose first <code>array.length</code> entries are the same
         * as those of <code>array</code>.
         */
        public static char[] ensureCapacity(final char[] array, final int length) {
            if (length > array.length) {
                final char t[] =
                        new char[length];
                System.arraycopy(array, 0, t, 0, array.length);
                return t;
            }
            return array;
        }

        /**
         * Ensures that an array can contain the given number of entries, preserving just a part of the array.
         *
         * @param array    an array.
         * @param length   the new minimum length for this array.
         * @param preserve the number of elements of the array that must be preserved in case a new allocation is necessary.
         * @return <code>array</code>, if it can contain <code>length</code> entries or more; otherwise, an array with <code>length</code> entries whose first <code>preserve</code> entries are the same as
         * those of <code>array</code>.
         */
        public static char[] ensureCapacity(final char[] array, final int length, final int preserve) {
            if (length > array.length) {
                final char t[] =
                        new char[length];
                System.arraycopy(array, 0, t, 0, preserve);
                return t;
            }
            return array;
        }

        /**
         * Grows the given array to the maximum between the given length and the current length multiplied by two, provided that the given length is larger than the current length.
         * <br>If you want complete control on the array growth, you should probably use <code>ensureCapacity()</code> instead.
         *
         * @param array  an array.
         * @param length the new minimum length for this array.
         * @return <code>array</code>, if it can contain <code>length</code> entries; otherwise, an array with max(<code>length</code>,<code>array.length</code>/&phi;) entries whose first
         * <code>array.length</code> entries are the same as those of <code>array</code>.
         */
        public static char[] grow(final char[] array, final int length) {
            if (length > array.length) {
                final int newLength = (int) Math.max(Math.min(2L * array.length, MAX_ARRAY_SIZE), length);
                final char t[] =
                        new char[newLength];
                System.arraycopy(array, 0, t, 0, array.length);
                return t;
            }
            return array;
        }

        /**
         * Grows the given array to the maximum between the given length and the current length multiplied by two, provided that the given length is larger than the current length, preserving just a part
         * of the array.
         * <br>If you want complete control on the array growth, you should probably use <code>ensureCapacity()</code> instead.
         *
         * @param array    an array.
         * @param length   the new minimum length for this array.
         * @param preserve the number of elements of the array that must be preserved in case a new allocation is necessary.
         * @return <code>array</code>, if it can contain <code>length</code> entries; otherwise, an array with max(<code>length</code>,<code>array.length</code>/&phi;) entries whose first
         * <code>preserve</code> entries are the same as those of <code>array</code>.
         */
        public static char[] grow(final char[] array, final int length, final int preserve) {
            if (length > array.length) {
                final int newLength = (int) Math.max(Math.min(2L * array.length, MAX_ARRAY_SIZE), length);
                final char t[] =
                        new char[newLength];
                System.arraycopy(array, 0, t, 0, preserve);
                return t;
            }
            return array;
        }

        /**
         * Trims the given array to the given length.
         *
         * @param array  an array.
         * @param length the new maximum length for the array.
         * @return <code>array</code>, if it contains <code>length</code> entries or less; otherwise, an array with <code>length</code> entries whose entries are the same as the first <code>length</code>
         * entries of <code>array</code>.
         */
        public static char[] trim(final char[] array, final int length) {
            if (length >= array.length) return array;
            final char t[] =
                    length == 0 ? EMPTY_ARRAY : new char[length];
            System.arraycopy(array, 0, t, 0, length);
            return t;
        }

        /**
         * Sets the length of the given array.
         *
         * @param array  an array.
         * @param length the new length for the array.
         * @return <code>array</code>, if it contains exactly <code>length</code> entries; otherwise, if it contains <em>more</em> than <code>length</code> entries, an array with <code>length</code>
         * entries whose entries are the same as the first <code>length</code> entries of <code>array</code>; otherwise, an array with <code>length</code> entries whose first <code>array.length</code>
         * entries are the same as those of <code>array</code>.
         */
        public static char[] setLength(final char[] array, final int length) {
            if (length == array.length) return array;
            if (length < array.length) return trim(array, length);
            return ensureCapacity(array, length);
        }

        /**
         * Returns a copy of a portion of an array.
         *
         * @param array  an array.
         * @param offset the first element to copy.
         * @param length the number of elements to copy.
         * @return a new array containing <code>length</code> elements of <code>array</code> starting at <code>offset</code>.
         */
        public static char[] copy(final char[] array, final int offset, final int length) {
            ensureOffsetLength(array, offset, length);
            final char[] a =
                    length == 0 ? EMPTY_ARRAY : new char[length];
            System.arraycopy(array, offset, a, 0, length);
            return a;
        }

        /**
         * Returns a copy of an array.
         *
         * @param array an array.
         * @return a copy of <code>array</code>.
         */
        public static char[] copy(final char[] array) {
            char[] next = new char[array.length];
            System.arraycopy(array, 0, next, 0, array.length);
            return next;
        }

        /**
         * Fills the given array with the given value.
         *
         * @param array an array.
         * @param value the new value for all elements of the array.
         * @deprecated Please use the corresponding {@link java.util.Arrays} method.
         */
        @Deprecated
        public static void fill(final char[] array, final char value) {
            int i = array.length;
            while (i-- != 0)
                array[i] = value;
        }

        /**
         * Fills a portion of the given array with the given value.
         *
         * @param array an array.
         * @param from  the starting index of the portion to fill (inclusive).
         * @param to    the end index of the portion to fill (exclusive).
         * @param value the new value for all elements of the specified portion of the array.
         * @deprecated Please use the corresponding {@link java.util.Arrays} method.
         */
        @Deprecated
        public static void fill(final char[] array, final int from, int to, final char value) {
            ensureFromTo(array, from, to);
            if (from == 0) while (to-- != 0)
                array[to] = value;
            else for (int i = from; i < to; i++)
                array[i] = value;
        }

        /**
         * Returns true if the two arrays are element-wise equal.
         *
         * @param a1 an array.
         * @param a2 another array.
         * @return true if the two arrays are of the same length, and their elements are equal.
         * @deprecated Please use the corresponding {@link java.util.Arrays} method, which is intrinsic in recent JVMs.
         */
        @Deprecated
        public static boolean equals(final char[] a1, final char a2[]) {
            int i = a1.length;
            if (i != a2.length) return false;
            while (i-- != 0)
                if (!((a1[i]) == (a2[i]))) return false;
            return true;
        }

        /**
         * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array.
         * <br>This method may be used whenever an array range check is needed.
         *
         * @param a    an array.
         * @param from a start index (inclusive).
         * @param to   an end index (exclusive).
         * @throws IllegalArgumentException       if <code>from</code> is greater than <code>to</code>.
         * @throws ArrayIndexOutOfBoundsException if <code>from</code> or <code>to</code> are greater than the array length or negative.
         */
        public static void ensureFromTo(final char[] a, final int from, final int to) {
            ensureFromTo(a.length, from, to);
        }

        /**
         * Ensures that a range given by an offset and a length fits an array.
         * <br>This method may be used whenever an array range check is needed.
         *
         * @param a      an array.
         * @param offset a start index.
         * @param length a length (the number of elements in the range).
         * @throws IllegalArgumentException       if <code>length</code> is negative.
         * @throws ArrayIndexOutOfBoundsException if <code>offset</code> is negative or <code>offset</code>+<code>length</code> is greater than the array length.
         */
        public static void ensureOffsetLength(final char[] a, final int offset, final int length) {
            ensureOffsetLength(a.length, offset, length);
        }

        /**
         * Ensures that two arrays are of the same length.
         *
         * @param a an array.
         * @param b another array.
         * @throws IllegalArgumentException if the two argument arrays are not of the same length.
         */
        public static void ensureSameLength(final char[] a, final char[] b) {
            if (a.length != b.length)
                throw new IllegalArgumentException("Array size mismatch: " + a.length + " != " + b.length);
        }

        /**
         * Ensures that a range given by its first (inclusive) and last (exclusive) elements fits an array of given length.
         * <br>This method may be used whenever an array range check is needed.
         *
         * @param arrayLength an array length.
         * @param from        a start index (inclusive).
         * @param to          an end index (inclusive).
         * @throws IllegalArgumentException       if <code>from</code> is greater than <code>to</code>.
         * @throws ArrayIndexOutOfBoundsException if <code>from</code> or <code>to</code> are greater than <code>arrayLength</code> or negative.
         */
        public static void ensureFromTo(final int arrayLength, final int from, final int to) {
            if (from < 0) throw new ArrayIndexOutOfBoundsException("Start index (" + from + ") is negative");
            if (from > to)
                throw new IllegalArgumentException("Start index (" + from + ") is greater than end index (" + to + ")");
            if (to > arrayLength)
                throw new ArrayIndexOutOfBoundsException("End index (" + to + ") is greater than array length (" + arrayLength + ")");
        }


        /**
         * Ensures that a range given by an offset and a length fits an array of given length.
         * <br>This method may be used whenever an array range check is needed.
         *
         * @param arrayLength an array length.
         * @param offset      a start index for the fragment
         * @param length      a length (the number of elements in the fragment).
         * @throws IllegalArgumentException       if <code>length</code> is negative.
         * @throws ArrayIndexOutOfBoundsException if <code>offset</code> is negative or <code>offset</code>+<code>length</code> is greater than <code>arrayLength</code>.
         */
        public static void ensureOffsetLength(final int arrayLength, final int offset, final int length) {
            if (offset < 0) throw new ArrayIndexOutOfBoundsException("Offset (" + offset + ") is negative");
            if (length < 0) throw new IllegalArgumentException("Length (" + length + ") is negative");
            if (offset + length > arrayLength)
                throw new ArrayIndexOutOfBoundsException("Last index (" + (offset + length) + ") is greater than array length (" + arrayLength + ")");
        }
    }
}
