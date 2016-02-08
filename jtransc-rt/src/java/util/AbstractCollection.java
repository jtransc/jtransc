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

import jtransc.annotation.JTranscKeep;

public abstract class AbstractCollection<E> implements Collection<E> {
	protected AbstractCollection() {
	}

	@JTranscKeep
	public abstract Iterator<E> iterator();

	public abstract int size();

	public boolean isEmpty() {
		return size() == 0;
	}

	native public boolean contains(Object o);

	@JTranscKeep
	public Object[] toArray() {
		Object[] r = new Object[size()];
		int i = 0;
		for (E e : this) r[i++] = e;
		return r;
	}

	public <T> T[] toArray(T[] a) {
		return (T[]) toArray();
	}

	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	native public boolean remove(Object o);

	native public boolean containsAll(Collection<?> c);

	public boolean addAll(Collection<? extends E> collection) {
		boolean changed = false;
		for (E item : collection) {
			if (this.add(item)) changed = true;
		}
		return changed;
	}

	native public boolean removeAll(Collection<?> c);

	native public boolean retainAll(Collection<?> c);

	native public void clear();

	native public String toString();
}
