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
import jtransc.annotation.haxe.HaxeMethodBody;

public abstract class AbstractCollection<E> implements Collection<E> {
	protected AbstractCollection() {
	}

	@JTranscKeep
	public abstract Iterator<E> iterator();

	public abstract int size();

	public boolean isEmpty() {
		return size() == 0;
	}

	public int hashCode() {
		int h = 0;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			if (obj != null)
				h += obj.hashCode();
		}
		return h;
	}

	public boolean equals(Object o) {
		if (o == this) return true;
		Collection<?> c = (Collection<?>) o;
		return (c.size() == size()) && containsAll(c);
	}

	public boolean contains(Object o) {
		for (Object i : this) if (Objects.equals(i, o)) return true;
		return false;
	}

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

	public boolean remove(Object o) {
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			if (Objects.equals(o, it.next())) {
				it.remove();
				return true;
			}
		}
		return false;
	}

	public boolean containsAll(Collection<?> collection) {
		HashSet set = new HashSet(this);
		for (Object item : collection) {
			if (!set.contains(item)) return false;
		}
		return true;
	}

	public boolean addAll(Collection<? extends E> collection) {
		boolean changed = false;
		for (E item : collection) {
			if (this.add(item)) changed = true;
		}
		return changed;
	}

	public boolean removeAll(Collection<?> c) {
		int modifiedCount = 0;
		for (Object i : new ArrayList(c)) {
			if (this.remove(i)) modifiedCount++;
		}
		return modifiedCount > 0;
	}

	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c);
		HashSet cSet = new HashSet(c);
		int modifiedCount = 0;
		for (Object i : new ArrayList(this)) {
			if (!cSet.contains(i)) {
				this.remove(i);
				modifiedCount++;
			}
		}
		return modifiedCount > 0;
	}

	public void clear() {
		removeAll(this);
	}

	@HaxeMethodBody("return HaxeNatives.str('[' + this.toArray___Ljava_lang_Object_().join(', ') + ']');")
	native public String toString();
}
