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

public interface NavigableSet<E> extends SortedSet<E> {
	E lower(E e);

	E floor(E e);

	E ceiling(E e);

	E higher(E e);

	E pollFirst();

	E pollLast();

	Iterator<E> iterator();

	NavigableSet<E> descendingSet();

	Iterator<E> descendingIterator();

	NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

	NavigableSet<E> headSet(E toElement, boolean inclusive);

	NavigableSet<E> tailSet(E fromElement, boolean inclusive);

	SortedSet<E> subSet(E fromElement, E toElement);

	SortedSet<E> headSet(E toElement);

	SortedSet<E> tailSet(E fromElement);
}
