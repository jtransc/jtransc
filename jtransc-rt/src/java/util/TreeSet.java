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

public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, java.io.Serializable {
	ArrayList<E> items = new ArrayList<E>();

	TreeSet(NavigableMap<E, Object> m) {
	}

	public TreeSet() {
	}

	public TreeSet(Comparator<? super E> comparator) {
	}

	public TreeSet(Collection<? extends E> c) {
	}

	public TreeSet(SortedSet<E> s) {
	}

	native public Iterator<E> iterator();

	native public Iterator<E> descendingIterator();

	native public NavigableSet<E> descendingSet();

	public int size() {
		return items.size();
	}

	public boolean contains(Object o) {
		return items.contains(o);
	}

	public boolean add(E e) {
		if (!contains(e)) {
			items.add(e);
			return true;
		} else{
			return false;
		}
	}

	public boolean remove(Object o) {
		return items.remove(o);
	}

	public void clear() {
		items.clear();
	}

	native public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

	native public NavigableSet<E> headSet(E toElement, boolean inclusive);

	native public NavigableSet<E> tailSet(E fromElement, boolean inclusive);

	native public SortedSet<E> subSet(E fromElement, E toElement);

	native public SortedSet<E> headSet(E toElement);

	native public SortedSet<E> tailSet(E fromElement);

	native public Comparator<? super E> comparator();

	public E first() {
		return isEmpty() ? null : items.get(0);
	}

	public E last() {
		return isEmpty() ? null : items.get(size() - 1);
	}

	native public E lower(E e);

	native public E floor(E e);

	native public E ceiling(E e);

	native public E higher(E e);

	native public E pollFirst();

	native public E pollLast();

	native public Object clone();
}
