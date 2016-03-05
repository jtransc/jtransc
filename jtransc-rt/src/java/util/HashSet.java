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

public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable {
	private transient HashMap<E, Object> map;
	static private Object HAS = new Object();

	public HashSet() {
		map = new HashMap<>();
	}

	public HashSet(Collection<? extends E> c) {
		map = new HashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	public HashSet(int initialCapacity, float loadFactor) {
		map = new HashMap<>(initialCapacity, loadFactor);
	}

	public HashSet(int initialCapacity) {
		map = new HashMap<>(initialCapacity);
	}

	HashSet(int initialCapacity, float loadFactor, boolean dummy) {
		map = new LinkedHashMap<>(initialCapacity, loadFactor);
	}

	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	public boolean add(E e) {
		return map.put(e, HAS) == null;
	}

	public boolean remove(Object o) {
		return map.remove(o) == HAS;
	}

	public void clear() {
		map.clear();
	}

	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			HashSet<E> newSet = (HashSet<E>) super.clone();
			newSet.map = (HashMap<E, Object>) map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}
}
