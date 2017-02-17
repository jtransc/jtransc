/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.*;

// BEGIN android-note
// removed link to collections framework docs
// END android-note

/**
 * A scalable concurrent {@link NavigableSet} implementation based on
 * a {@link ConcurrentSkipListMap}.  The elements of the set are kept
 * sorted according to their {@linkplain Comparable natural ordering},
 * or by a {@link Comparator} provided at set creation time, depending
 * on which constructor is used.
 *
 * <p>This implementation provides expected average <i>log(n)</i> time
 * cost for the {@code contains}, {@code add}, and {@code remove}
 * operations and their variants.  Insertion, removal, and access
 * operations safely execute concurrently by multiple threads.
 * Iterators are <i>weakly consistent</i>, returning elements
 * reflecting the state of the set at some point at or since the
 * creation of the iterator.  They do <em>not</em> throw {@link
 * ConcurrentModificationException}, and may proceed concurrently with
 * other operations.  Ascending ordered views and their iterators are
 * faster than descending ones.
 *
 * <p>Beware that, unlike in most collections, the {@code size}
 * method is <em>not</em> a constant-time operation. Because of the
 * asynchronous nature of these sets, determining the current number
 * of elements requires a traversal of the elements, and so may report
 * inaccurate results if this collection is modified during traversal.
 * Additionally, the bulk operations {@code addAll},
 * {@code removeAll}, {@code retainAll}, {@code containsAll},
 * {@code equals}, and {@code toArray} are <em>not</em> guaranteed
 * to be performed atomically. For example, an iterator operating
 * concurrently with an {@code addAll} operation might view only some
 * of the added elements.
 *
 * <p>This class and its iterators implement all of the
 * <em>optional</em> methods of the {@link Set} and {@link Iterator}
 * interfaces. Like most other concurrent collection implementations,
 * this class does not permit the use of {@code null} elements,
 * because {@code null} arguments and return values cannot be reliably
 * distinguished from the absence of elements.
 *
 * @author Doug Lea
 * @param <E> the type of elements maintained by this set
 * @since 1.6
 */
public class ConcurrentSkipListSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, java.io.Serializable {

    ConcurrentNavigableMap<E,Object> m;

    public ConcurrentSkipListSet() {
        m = new ConcurrentSkipListMap<E,Object>();
    }

    public ConcurrentSkipListSet(Comparator<? super E> comparator) {
        m = new ConcurrentSkipListMap<E,Object>(comparator);
    }

    public ConcurrentSkipListSet(Collection<? extends E> c) {
        m = new ConcurrentSkipListMap<E,Object>();
        addAll(c);
    }

    public ConcurrentSkipListSet(SortedSet<E> s) {
        m = new ConcurrentSkipListMap<E,Object>(s.comparator());
        addAll(s);
    }

    ConcurrentSkipListSet(ConcurrentNavigableMap<E,Object> m) {
        this.m = m;
    }

    public ConcurrentSkipListSet<E> clone() {
        try {
            @SuppressWarnings("unchecked")
			ConcurrentSkipListSet<E> clone =
                (ConcurrentSkipListSet<E>) super.clone();
            clone.m = new ConcurrentSkipListMap<E,Object>(m);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public int size() {
        return m.size();
    }
    public boolean isEmpty() {
        return m.isEmpty();
    }
    public boolean contains(Object o) {
        return m.containsKey(o);
    }
    public boolean add(E e) {
        return m.putIfAbsent(e, Boolean.TRUE) == null;
    }
    public boolean remove(Object o) {
        return m.remove(o, Boolean.TRUE);
    }
    public void clear() {
        m.clear();
    }
    public Iterator<E> iterator() {
        return m.navigableKeySet().iterator();
    }
    public Iterator<E> descendingIterator() {
        return m.descendingKeySet().iterator();
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Set)) return false;
        Collection<?> c = (Collection<?>) o;
        try {
            return containsAll(c) && c.containsAll(this);
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) if (remove(e)) modified = true;
        return modified;
    }

    public E lower(E e) {
        return m.lowerKey(e);
    }

    public E floor(E e) {
        return m.floorKey(e);
    }

    public E ceiling(E e) {
        return m.ceilingKey(e);
    }

    public E higher(E e) {
        return m.higherKey(e);
    }

    public E pollFirst() {
        Map.Entry<E,Object> e = m.pollFirstEntry();
        return (e == null) ? null : e.getKey();
    }

    public E pollLast() {
        Map.Entry<E,Object> e = m.pollLastEntry();
        return (e == null) ? null : e.getKey();
    }

    public Comparator<? super E> comparator() {
        return m.comparator();
    }
    public E first() {
        return m.firstKey();
    }
    public E last() {
        return m.lastKey();
    }

    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new ConcurrentSkipListSet<E>
            (m.subMap(fromElement, fromInclusive,
                      toElement,   toInclusive));
    }

    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(m.headMap(toElement, inclusive));
    }

    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ConcurrentSkipListSet<E>(m.tailMap(fromElement, inclusive));
    }

    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    public NavigableSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    public NavigableSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    public NavigableSet<E> descendingSet() {
        return new ConcurrentSkipListSet<E>(m.descendingMap());
    }
}
