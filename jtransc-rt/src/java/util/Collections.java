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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Collections {
    private Collections() {
    }

    native public static <T extends Comparable<? super T>> void sort(List<T> list);

    native public static <T> void sort(List<T> list, Comparator<? super T> c);

    native public static <T> int binarySearch(List<? extends Comparable<? super T>> list, T key);

    native public static <T> int binarySearch(List<? extends T> list, T key, Comparator<? super T> c);

    public static void reverse(List<?> list) {
        int size = list.size();
        for (int i = 0, mid = size >> 1, j = size - 1; i < mid; i++, j--) {
            swap(list, i, j);
        }
    }

    native public static void shuffle(List<?> list);

    native public static void shuffle(List<?> list, Random rnd);

    public static void swap(List<?> list, int i, int j) {
        final List l = list;
        l.set(i, l.set(j, l.get(i)));
    }

    public static <T> void fill(List<? super T> list, T obj) {
        int length = list.size();
        for (int n = 0; n < length; n++) list.set(n, obj);
    }

    native public static <T> void copy(List<? super T> dest, List<? extends T> src);

    native public static <T extends Object & Comparable<? super T>> T min(Collection<? extends T> coll);

    native public static <T> T min(Collection<? extends T> coll, Comparator<? super T> comp);

    native public static <T extends Object & Comparable<? super T>> T max(Collection<? extends T> coll);

    native public static <T> T max(Collection<? extends T> coll, Comparator<? super T> comp);

    public static void rotate(List<?> list, int distance) {
        int length = list.size();
        for (int n = 0; n < length; n++) {
            swap(list, n, (n + distance) % length);
        }
    }

    public static <T> boolean replaceAll(List<T> list, T oldVal, T newVal) {
        int length = list.size();
        int count = 0;
        for (int n = 0; n < length; n++) {
            if (Objects.equals(list.get(n), oldVal)) {
                list.set(n, newVal);
                count++;
            }
        }
        return count > 0;
    }

    native public static int indexOfSubList(List<?> source, List<?> target);

    native public static int lastIndexOfSubList(List<?> source, List<?> target);

    public static <T> Collection<T> unmodifiableCollection(Collection<? extends T> c) {
        return (Collection<T>) c;
    }

    public static <T> Set<T> unmodifiableSet(Set<? extends T> s) {
        return (Set<T>) s;
    }

    public static <T> SortedSet<T> unmodifiableSortedSet(SortedSet<T> s) {
        return s;
    }

    public static <T> NavigableSet<T> unmodifiableNavigableSet(NavigableSet<T> s) {
        return s;
    }

    public static <T> List<T> unmodifiableList(List<? extends T> list) {
        return (List<T>) list;
    }

    public static <K, V> Map<K, V> unmodifiableMap(Map<? extends K, ? extends V> m) {
        return (Map<K, V>) m;
    }

    public static <K, V> SortedMap<K, V> unmodifiableSortedMap(SortedMap<K, ? extends V> m) {
        return (SortedMap<K, V>) m;
    }

    public static <K, V> NavigableMap<K, V> unmodifiableNavigableMap(NavigableMap<K, ? extends V> m) {
        return (NavigableMap<K, V>) m;
    }

    public static <T> Collection<T> synchronizedCollection(Collection<T> c) {
        return c;
    }

    public static <T> Set<T> synchronizedSet(Set<T> s) {
        return s;
    }

    public static <T> SortedSet<T> synchronizedSortedSet(SortedSet<T> s) {
        return s;
    }

    public static <T> NavigableSet<T> synchronizedNavigableSet(NavigableSet<T> s) {
        return s;
    }

    static <T> List<T> synchronizedList(List<T> list, Object mutex) {
        return (list instanceof RandomAccess ? list : list);
    }
    public static <K, V> Map<K, V> synchronizedMap(Map<K, V> m) {
        return m;
    }

    public static <K, V> SortedMap<K, V> synchronizedSortedMap(SortedMap<K, V> m) {
        return m;
    }

    public static <K, V> NavigableMap<K, V> synchronizedNavigableMap(NavigableMap<K, V> m) {
        return m;
    }

    native public static <E> Collection<E> checkedCollection(Collection<E> c, Class<E> type);

    native public static <E> Queue<E> checkedQueue(Queue<E> queue, Class<E> type);

    native public static <E> Set<E> checkedSet(Set<E> s, Class<E> type);

    native public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s, Class<E> type);

    native public static <E> NavigableSet<E> checkedNavigableSet(NavigableSet<E> s, Class<E> type);

    native public static <E> List<E> checkedList(List<E> list, Class<E> type);

    native public static <K, V> Map<K, V> checkedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType);

    native public static <K, V> SortedMap<K, V> checkedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType);

    native public static <K, V> NavigableMap<K, V> checkedNavigableMap(NavigableMap<K, V> m, Class<K> keyType, Class<V> valueType);

    static private List<Object> empty = new ArrayList<Object>();
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) empty.iterator();
    }

    public static <T> ListIterator<T> emptyListIterator() {
        return (ListIterator<T>) empty.listIterator();
    }

    native public static <T> Enumeration<T> emptyEnumeration();

    // @TODO: Make those immutable
    public static final Set EMPTY_SET = new HashSet();
    public static final List EMPTY_LIST = new ArrayList();
    public static final Map EMPTY_MAP = new HashMap();

    native public static final <T> Set<T> emptySet();

    native public static <E> SortedSet<E> emptySortedSet();

    native public static <E> NavigableSet<E> emptyNavigableSet();

    native public static final <T> List<T> emptyList();

    native public static final <K, V> Map<K, V> emptyMap();

    native public static final <K, V> SortedMap<K, V> emptySortedMap();

    native public static final <K, V> NavigableMap<K, V> emptyNavigableMap();

    native public static <T> Set<T> singleton(T o);

    native static <E> Iterator<E> singletonIterator(final E e);

    //native static <T> Spliterator<T> singletonSpliterator(final T element);

    public static <T> List<T> singletonList(T o) {
        return nCopies(1, o);
    }

    public static <K, V> Map<K, V> singletonMap(K key, V value) {
        HashMap<K, V> ts = new HashMap<K, V>(1);
        ts.put(key, value);
        return ts;
    }

    public static <T> List<T> nCopies(int count, T o) {
        ArrayList<T> ts = new ArrayList<T>(count);
        for (int n = 0; n < count; n++) ts.add(o);
        return ts;
    }

    native public static <T> Comparator<T> reverseOrder();

    native public static <T> Comparator<T> reverseOrder(Comparator<T> cmp);

    native public static <T> Enumeration<T> enumeration(final Collection<T> c);

    native public static <T> ArrayList<T> list(Enumeration<T> e);

    native static boolean eq(Object o1, Object o2);

    native public static int frequency(Collection<?> c, Object o);

    native public static boolean disjoint(Collection<?> c1, Collection<?> c2);

    public static <T> boolean addAll(Collection<? super T> c, T... elements) {
        boolean out = false;
        for (T e : elements) out |= c.add(e);
        return out;
    }

    native public static <E> Set<E> newSetFromMap(Map<E, Boolean> map);

    native public static <T> Queue<T> asLifoQueue(Deque<T> deque);
}
