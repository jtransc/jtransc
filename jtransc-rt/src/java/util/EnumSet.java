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

public abstract class EnumSet<E extends Enum<E>> extends AbstractSet<E> implements Cloneable, java.io.Serializable {
    final Class<E> elementType;
    final Enum<?>[] universe;

    private static Enum<?>[] ZERO_LENGTH_ENUM_ARRAY = new Enum<?>[0];

    EnumSet(Class<E> elementType, Enum<?>[] universe) {
        this.elementType = elementType;
        this.universe = universe;
    }

    public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
        Enum<?>[] universe = getUniverse(elementType);
        if (universe == null) throw new ClassCastException(elementType + " not an enum");

        return new RegularEnumSet<E>(elementType, universe);
    }

    public static <E extends Enum<E>> EnumSet<E> allOf(Class<E> elementType) {
        EnumSet<E> result = noneOf(elementType);
        result.addAll();
        return result;
    }

    abstract void addAll();

    public static <E extends Enum<E>> EnumSet<E> copyOf(EnumSet<E> s) {
        return s.clone();
    }

    public static <E extends Enum<E>> EnumSet<E> copyOf(Collection<E> c) {
        if (c instanceof EnumSet) return ((EnumSet<E>) c).clone();
        if (c.isEmpty()) throw new IllegalArgumentException("Collection is empty");
        Iterator<E> i = c.iterator();
        E first = i.next();
        EnumSet<E> result = EnumSet.of(first);
        while (i.hasNext()) result.add(i.next());
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> complementOf(EnumSet<E> s) {
        EnumSet<E> result = copyOf(s);
        result.complement();
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e) {
        EnumSet<E> result = noneOf(e.getDeclaringClass());
        result.add(e);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E e1, E e2, E e3, E e4, E e5) {
        EnumSet<E> result = noneOf(e1.getDeclaringClass());
        result.add(e1);
        result.add(e2);
        result.add(e3);
        result.add(e4);
        result.add(e5);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> of(E first, E... rest) {
        EnumSet<E> result = noneOf(first.getDeclaringClass());
        result.add(first);
        for (E e : rest)
            result.add(e);
        return result;
    }

    public static <E extends Enum<E>> EnumSet<E> range(E from, E to) {
        if (from.compareTo(to) > 0) throw new IllegalArgumentException(from + " > " + to);
        EnumSet<E> result = noneOf(from.getDeclaringClass());
        result.addRange(from, to);
        return result;
    }

    abstract void addRange(E from, E to);

    @SuppressWarnings("unchecked")
    public EnumSet<E> clone() {
        try {
            return (EnumSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    abstract void complement();

    final void typeCheck(E e) {
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType) {
            throw new ClassCastException(eClass + " != " + elementType);
        }
    }

    private static <E extends Enum<E>> E[] getUniverse(Class<E> elementType) {
        return elementType.getEnumConstants();
    }
}

class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private long elements = 0L;

    RegularEnumSet(Class<E> elementType, Enum<?>[] universe) {
        super(elementType, universe);
    }

    void addRange(E from, E to) {
        elements = (-1L >>> (from.ordinal() - to.ordinal() - 1)) << from.ordinal();
    }

    void addAll() {
        if (universe.length != 0) elements = -1L >>> -universe.length;
    }

    void complement() {
        if (universe.length == 0) return;
        elements = ~elements;
        elements &= -1L >>> -universe.length;
    }

    public Iterator<E> iterator() {
        return new EnumSetIterator<E>();
    }

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        long unseen;
        long lastReturned = 0;

        EnumSetIterator() {
            unseen = elements;
        }

        public boolean hasNext() {
            return unseen != 0;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            if (unseen == 0) throw new NoSuchElementException();
            lastReturned = unseen & -unseen;
            unseen -= lastReturned;
            return (E) universe[Long.numberOfTrailingZeros(lastReturned)];
        }

        public void remove() {
            if (lastReturned == 0) throw new IllegalStateException();
            elements &= ~lastReturned;
            lastReturned = 0;
        }
    }

    public int size() {
        return Long.bitCount(elements);
    }

    public boolean isEmpty() {
        return elements == 0;
    }

    public boolean contains(Object e) {
        if (e == null) return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType) return false;
        return (elements & (1L << ((Enum<?>) e).ordinal())) != 0;
    }

    public boolean add(E e) {
        typeCheck(e);

        long oldElements = elements;
        elements |= (1L << ((Enum<?>) e).ordinal());
        return elements != oldElements;
    }

    public boolean remove(Object e) {
        if (e == null) return false;
        Class<?> eClass = e.getClass();
        if (eClass != elementType && eClass.getSuperclass() != elementType) return false;
        long oldElements = elements;
        elements &= ~(1L << ((Enum<?>) e).ordinal());
        return elements != oldElements;
    }

    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet)) return super.containsAll(c);

        RegularEnumSet<?> es = (RegularEnumSet<?>) c;
        if (es.elementType != elementType) return es.isEmpty();

        return (es.elements & ~elements) == 0;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof RegularEnumSet)) return super.addAll(c);

        RegularEnumSet<?> es = (RegularEnumSet<?>) c;
        if (es.elementType != elementType) {
            if (es.isEmpty()) {
                return false;
            } else {
                throw new ClassCastException(es.elementType + " != " + elementType);
            }
        }

        long oldElements = elements;
        elements |= es.elements;
        return elements != oldElements;
    }

    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet)) return super.removeAll(c);
        RegularEnumSet<?> es = (RegularEnumSet<?>) c;
        if (es.elementType != elementType) return false;
        long oldElements = elements;
        elements &= ~es.elements;
        return elements != oldElements;
    }

    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet)) return super.retainAll(c);

        RegularEnumSet<?> es = (RegularEnumSet<?>) c;
        if (es.elementType != elementType) {
            boolean changed = (elements != 0);
            elements = 0;
            return changed;
        }

        long oldElements = elements;
        elements &= es.elements;
        return elements != oldElements;
    }

    public void clear() {
        elements = 0;
    }

    public boolean equals(Object o) {
        if (!(o instanceof RegularEnumSet)) return super.equals(o);
        RegularEnumSet<?> es = (RegularEnumSet<?>) o;
        if (es.elementType != elementType) return elements == 0 && es.elements == 0;
        return es.elements == elements;
    }
}
