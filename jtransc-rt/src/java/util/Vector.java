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

public class Vector<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
    protected Object[] elementData;
    protected int elementCount;
    protected int capacityIncrement;

    public Vector(int initialCapacity, int capacityIncrement) {
        super();
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }

    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    public Vector() {
        this(10);
    }

    public Vector(Collection<? extends E> c) {
        elementData = c.toArray();
        elementCount = elementData.length;
        if (elementData.getClass() != Object[].class) {
            elementData = Arrays.copyOf(elementData, elementCount, Object[].class);
        }
    }

    public synchronized void copyInto(Object[] anArray) {
        System.arraycopy(elementData, 0, anArray, 0, elementCount);
    }

    public synchronized void trimToSize() {
        modCount++;
        int oldCapacity = elementData.length;
        if (elementCount < oldCapacity) {
            elementData = Arrays.copyOf(elementData, elementCount);
        }
    }

    public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity <= 0) return;
        modCount++;
        ensureCapacityHelper(minCapacity);
    }

    private void ensureCapacityHelper(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - elementData.length > 0) grow(minCapacity);
    }

    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + ((capacityIncrement > 0) ? capacityIncrement : oldCapacity);
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }

    public synchronized void setSize(int newSize) {
        modCount++;
        if (newSize > elementCount) {
            ensureCapacityHelper(newSize);
        } else {
            for (int i = newSize; i < elementCount; i++) elementData[i] = null;
        }
        elementCount = newSize;
    }

    public synchronized int capacity() {
        return elementData.length;
    }

    public synchronized int size() {
        return elementCount;
    }

    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }

    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int count = 0;

            public boolean hasMoreElements() {
                return count < elementCount;
            }

            public E nextElement() {
                synchronized (Vector.this) {
                    if (count < elementCount) {
                        return elementData(count++);
                    }
                }
                throw new NoSuchElementException("Vector Enumeration");
            }
        };
    }

    public boolean contains(Object o) {
        return indexOf(o, 0) >= 0;
    }

    public int indexOf(Object o) {
        return indexOf(o, 0);
    }

    public synchronized int indexOf(Object o, int index) {
        if (o == null) {
            for (int i = index; i < elementCount; i++) if (elementData[i] == null) return i;
        } else {
            for (int i = index; i < elementCount; i++) if (o.equals(elementData[i])) return i;
        }
        return -1;
    }

    public synchronized int lastIndexOf(Object o) {
        return lastIndexOf(o, elementCount - 1);
    }

    public synchronized int lastIndexOf(Object o, int index) {
        if (index >= elementCount) throw new IndexOutOfBoundsException(index + " >= " + elementCount);

        if (o == null) {
            for (int i = index; i >= 0; i--) if (elementData[i] == null) return i;
        } else {
            for (int i = index; i >= 0; i--) if (o.equals(elementData[i])) return i;
        }
        return -1;
    }

    public synchronized E elementAt(int index) {
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        return elementData(index);
    }

    public synchronized E firstElement() {
        if (elementCount == 0) throw new NoSuchElementException();
        return elementData(0);
    }

    public synchronized E lastElement() {
        if (elementCount == 0) throw new NoSuchElementException();
        return elementData(elementCount - 1);
    }

    public synchronized void setElementAt(E obj, int index) {
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        elementData[index] = obj;
    }

    public synchronized void removeElementAt(int index) {
        modCount++;
        if (index >= elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + elementCount);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int j = elementCount - index - 1;
        if (j > 0) System.arraycopy(elementData, index + 1, elementData, index, j);
        elementCount--;
        elementData[elementCount] = null; /* to let gc do its work */
    }

    public synchronized void insertElementAt(E obj, int index) {
        modCount++;
        if (index > elementCount) throw new ArrayIndexOutOfBoundsException(index + " > " + elementCount);
        ensureCapacityHelper(elementCount + 1);
        System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
        elementData[index] = obj;
        elementCount++;
    }

    public synchronized void addElement(E obj) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = obj;
    }

    public synchronized boolean removeElement(Object obj) {
        modCount++;
        int i = indexOf(obj);
        if (i >= 0) {
            removeElementAt(i);
            return true;
        }
        return false;
    }

    public synchronized void removeAllElements() {
        modCount++;
        for (int i = 0; i < elementCount; i++) elementData[i] = null;
        elementCount = 0;
    }

    public synchronized Object clone() {
        try {
            @SuppressWarnings("unchecked")
            Vector<E> v = (Vector<E>) super.clone();
            v.elementData = Arrays.copyOf(elementData, elementCount);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }

    public synchronized Object[] toArray() {
        return Arrays.copyOf(elementData, elementCount);
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] a) {
        if (a.length < elementCount) return (T[]) Arrays.copyOf(elementData, elementCount, a.getClass());
        System.arraycopy(elementData, 0, a, 0, elementCount);
        if (a.length > elementCount) a[elementCount] = null;
        return a;
    }

    @SuppressWarnings("unchecked")
    E elementData(int index) {
        return (E) elementData[index];
    }

    public synchronized E get(int index) {
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index);
        return elementData(index);
    }

    public synchronized E set(int index, E element) {
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index);
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }

    public synchronized boolean add(E e) {
        modCount++;
        ensureCapacityHelper(elementCount + 1);
        elementData[elementCount++] = e;
        return true;
    }

    public boolean remove(Object o) {
        return removeElement(o);
    }

    public void add(int index, E element) {
        insertElementAt(element, index);
    }

    public synchronized E remove(int index) {
        modCount++;
        if (index >= elementCount) throw new ArrayIndexOutOfBoundsException(index);
        E oldValue = elementData(index);
        int numMoved = elementCount - index - 1;
        if (numMoved > 0) System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        elementData[--elementCount] = null; // Let gc do its work
        return oldValue;
    }

    public void clear() {
        removeAllElements();
    }

    public synchronized boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    public synchronized boolean addAll(Collection<? extends E> c) {
        modCount++;
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);
        System.arraycopy(a, 0, elementData, elementCount, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    public synchronized boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    public synchronized boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        modCount++;
        if (index < 0 || index > elementCount) throw new ArrayIndexOutOfBoundsException(index);
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityHelper(elementCount + numNew);
        int numMoved = elementCount - index;
        if (numMoved > 0) System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        System.arraycopy(a, 0, elementData, index, numNew);
        elementCount += numNew;
        return numNew != 0;
    }

    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    public synchronized int hashCode() {
        return super.hashCode();
    }

    public synchronized String toString() {
        return super.toString();
    }

    public synchronized List<E> subList(int fromIndex, int toIndex) {
        return Collections.synchronizedList(super.subList(fromIndex, toIndex), this);
    }

    protected synchronized void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = elementCount - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);
        // Let gc do its work
        int newElementCount = elementCount - (toIndex - fromIndex);
        while (elementCount != newElementCount) elementData[--elementCount] = null;
    }

    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        final java.io.ObjectOutputStream.PutField fields = s.putFields();
        final Object[] data;
        synchronized (this) {
            fields.put("capacityIncrement", capacityIncrement);
            fields.put("elementCount", elementCount);
            data = elementData.clone();
        }
        fields.put("elementData", data);
        s.writeFields();
    }

    public synchronized ListIterator<E> listIterator(int index) {
        if (index < 0 || index > elementCount) throw new IndexOutOfBoundsException("Index: " + index);
        return new ListItr(index);
    }

    public synchronized ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    public synchronized Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        public boolean hasNext() {
            // Racy but within spec, since modifications are checked
            // within or after synchronization in next/previous
            return cursor != elementCount;
        }

        public E next() {
            synchronized (Vector.this) {
                checkForComodification();
                int i = cursor;
                if (i >= elementCount)
                    throw new NoSuchElementException();
                cursor = i + 1;
                return elementData(lastRet = i);
            }
        }

        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException();
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.remove(lastRet);
                expectedModCount = modCount;
            }
            cursor = lastRet;
            lastRet = -1;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
        }
    }

    final class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            cursor = index;
        }

        public boolean hasPrevious() {
            return cursor != 0;
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public E previous() {
            synchronized (Vector.this) {
                checkForComodification();
                int i = cursor - 1;
                if (i < 0) throw new NoSuchElementException();
                cursor = i;
                return elementData(lastRet = i);
            }
        }

        public void set(E e) {
            if (lastRet == -1) throw new IllegalStateException();
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.set(lastRet, e);
            }
        }

        public void add(E e) {
            int i = cursor;
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.add(i, e);
                expectedModCount = modCount;
            }
            cursor = i + 1;
            lastRet = -1;
        }
    }
}
