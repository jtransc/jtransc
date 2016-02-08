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

package java.util;

import jtransc.rt.GenericListIterator;

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    protected AbstractList() {
    }

    public boolean add(E e) {
        add(size(), e);
        return true;
    }

    abstract public E get(int index);

    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    public int indexOf(Object o) {
        ListIterator<E> it = listIterator();
        if (o == null) {
            while (it.hasNext()) if (it.next() == null) return it.previousIndex();
        } else {
            while (it.hasNext()) if (o.equals(it.next())) return it.previousIndex();
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());
        if (o == null) {
            while (it.hasPrevious()) if (it.previous() == null) return it.nextIndex();
        } else {
            while (it.hasPrevious()) if (o.equals(it.previous())) return it.nextIndex();
        }
        return -1;
    }

    public void clear() {
        this.removeRange(0, this.size());
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        boolean modified = false;
        for (E e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }

    public Iterator<E> iterator() {
        return new GenericListIterator<E>(this, 0);
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) {
        return new GenericListIterator<E>(this, index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList<E>(this, fromIndex, toIndex);
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof List)) return false;

        ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List<?>) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2))) return false;
        }
        return !e1.hasNext() && !e2.hasNext();

    }

    public int hashCode() {
        int hashCode = 1;
        for (E e : this) hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
            it.next();
            it.remove();
        }
    }

    protected transient int modCount = 0;

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size())
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size();
    }
}

class SubList<E> extends AbstractList<E> {
    private AbstractList<E> parent;
    private int offset;
    private int size;

    SubList(AbstractList<E> parent, int fromIndex, int toIndex) {
        if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > parent.size()) throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        this.parent = parent;
        this.offset = fromIndex;
        this.size = toIndex - fromIndex;
    }

    public E set(int index, E element) {
        rangeCheck(index);
        return this.parent.set(offset + index, element);
    }

    public E get(int index) {
        rangeCheck(index);
        return this.parent.get(offset + index);
    }

    public int size() {
        return this.size;
    }

    public void add(int index, E element) {
        rangeCheck(index);
        this.parent.add(offset + index, element);
    }

    public E remove(int index) {
        rangeCheck(index);
        return this.parent.remove(offset + index);
    }

    public boolean addAll(Collection<? extends E> c) {
        return addAll(size, c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheck(index);
        return this.parent.addAll(offset + index, c);
    }

    public Iterator<E> iterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(final int index) {
        return new GenericListIterator<E>(this, 0);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return super.subList(fromIndex, toIndex);
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }
}
