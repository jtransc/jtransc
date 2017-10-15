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

import com.jtransc.annotation.JTranscAsync;
import com.jtransc.annotation.JTranscMethodBody;
import com.jtransc.annotation.JTranscSync;
import com.jtransc.annotation.haxe.HaxeAddMembers;
import com.jtransc.annotation.haxe.HaxeMethodBody;

@SuppressWarnings("unchecked")
@HaxeAddMembers("var _data:Array<Dynamic> = [];")
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
	private Object[] buffer;
	private int length;

	@HaxeMethodBody("this._data = [];")
	@JTranscMethodBody(target = "js", value = "this._data = [];")
	@JTranscSync
	public ArrayList(int initialCapacity) {
		buffer = new Object[initialCapacity];
		length = 0;
	}

	@JTranscSync
	public ArrayList() {
		this(0);
	}

	@JTranscAsync
	public ArrayList(Collection<? extends E> c) {
		this(c.size());
		addAll(c);
	}

	@HaxeMethodBody("")
	@JTranscMethodBody(target = "js", value = "")
	@JTranscSync
	public void trimToSize() {
		buffer = Arrays.copyOf(buffer, length);
	}

	@HaxeMethodBody("")
	@JTranscMethodBody(target = "js", value = "")
	@JTranscSync
	private void ensure(int minimumCapacity) {
		if (minimumCapacity > buffer.length) {
			buffer = Arrays.copyOf(buffer, Math.max(minimumCapacity, (buffer.length * 2) + 2));
		}
	}

	@JTranscSync
	public void ensureCapacity(int minCapacity) {
		ensure(minCapacity);
	}

	@HaxeMethodBody("return _data.length;")
	@JTranscMethodBody(target = "js", value = "return this._data.length;")
	@JTranscSync
	public int size() {
		return length;
	}

	@HaxeMethodBody("return _data[p0];")
	@JTranscMethodBody(target = "js", value = "return this._data[p0];")
	@JTranscSync
	private E _get(int index) {
		return (E)buffer[index];
	}

	@HaxeMethodBody("_data[p0] = p1;")
	@JTranscMethodBody(target = "js", value = "this._data[p0] = p1;")
	@JTranscSync
	private void _set(int index, E element) {
		buffer[index] = element;
	}

	@HaxeMethodBody("_data = _data.slice(0, p0);")
	@JTranscMethodBody(target = "js", value = "this._data.length = p0;")
	@JTranscSync
	private void _setLength(int length) {
		ensure(length);
		this.length = length;
	}

	@HaxeMethodBody("_data.push(p0);")
	@JTranscMethodBody(target = "js", value = "this._data.push(p0);")
	@JTranscSync
	private void _add(E element) {
		ensure(length + 1);
		buffer[length++] = element;
	}

	@JTranscSync
	private void makeHole(int index, int count) {
		ensure(length + count);
		System.arraycopy(buffer, index, buffer, index + count, length - index - count);
		length += count;
	}

	@HaxeMethodBody("N.arrayInsert(_data, p0, p1);")
	@JTranscMethodBody(target = "js", value = "this._data.splice(p0, 0, p1);")
	@JTranscSync
	private void _insert(int index, E element) {
		makeHole(index, 1);
		buffer[index] = element;
	}

	@HaxeMethodBody("_data = _data.slice(0, p0).concat(p1.toArray()).concat(_data.slice(p0));")
	@JTranscMethodBody(target = "js", value = "this._data.splice.apply(this._data, [p0, 0].concat(p1.toArray()));")
	@JTranscSync
	private void _insert(int index, Object[] elements) {
		makeHole(index, elements.length);
		System.arraycopy(elements, 0, buffer, index, elements.length);
	}

	@HaxeMethodBody("_data = _data.slice(0, p0).concat(_data.slice(p0));")
	@JTranscMethodBody(target = "js", value = "this._data.splice(p0, p1 - p0);")
	@JTranscSync
	private void _remove(int from, int to) {
		int count = to - from;
		System.arraycopy(buffer, to, buffer, from, length - to);
		length -= count;
	}

	@HaxeMethodBody("_data.splice(p0, 1);")
	@JTranscMethodBody(target = "js", value = "this._data.splice(p0, 1);")
	@JTranscSync
	private void _remove(int index) {
		_remove(index, index + 1);
	}

	@JTranscSync
	private void _clear() {
		_setLength(0);
	}

	@HaxeMethodBody("p0._data = p1._data.slice(0);")
	@JTranscMethodBody(target = "js", value = "p0._data = p1._data.slice(0);")
	@JTranscSync
	static private <T> void _copy(ArrayList<T> dst, ArrayList<T> src) {
		dst.buffer = Arrays.copyOf(src.buffer, src.length);
		dst.length = src.length;
	}

	@JTranscSync
	public boolean isEmpty() {
		return size() == 0;
	}

	@JTranscAsync
	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	@JTranscAsync
	public int indexOf(Object o) {
		int len = size();
		for (int i = 0; i < len; i++) if (Objects.equals(o, _get(i))) return i;
		return -1;
	}

	@JTranscAsync
	public int lastIndexOf(Object o) {
		int len = size();
		for (int i = len - 1; i >= 0; i--) if (Objects.equals(o, _get(i))) return i;
		return -1;
	}

	@JTranscAsync
	public Object clone() {
		try {
			ArrayList<E> v = (ArrayList<E>) super.clone();
			_copy(v, this);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	@JTranscSync
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	@SuppressWarnings("unchecked")
	@JTranscSync
	public <T> T[] toArray(T[] a) {
		int len = size();
		if (a.length < len) a = (T[]) Arrays.copyOf(new Object[0], len, a.getClass());
		for (int n = 0; n < len; n++) a[n] = (T) _get(n);
		return a;
	}

	@JTranscSync
	public E get(int index) {
		rangeCheck(index);
		return _get(index);
	}

	@JTranscSync
	public E set(int index, E element) {
		rangeCheck(index);
		E oldValue = _get(index);
		_set(index, element);
		return oldValue;
	}

	@JTranscSync
	public boolean add(E e) {
		_add(e);
		return true;
	}

	@JTranscSync
	public void add(int index, E element) {
		rangeCheckForAdd(index);
		_insert(index, element);
	}

	@JTranscSync
	public E remove(int index) {
		rangeCheck(index);

		modCount++;
		E oldValue = _get(index);
		_remove(index);

		return oldValue;
	}

	@JTranscAsync
	public boolean remove(Object o) {
		int len = size();
		for (int index = 0; index < len; index++) {
			if (Objects.equals(o, _get(index))) {
				_remove(index);
				return true;
			}
		}
		return false;
	}

	@JTranscSync
	public void clear() {
		modCount++;
		_clear();
	}

	//@JTranscSync
	@JTranscAsync
	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) add(e);
		return c.size() != 0;
	}

	//@JTranscSync
	@JTranscAsync
	public boolean addAll(int index, Collection<? extends E> c) {
		rangeCheckForAdd(index);

		_insert(index, c.toArray());

		if (c.size() != 0) modCount++;
		return c.size() != 0;
	}

	@JTranscSync
	protected void removeRange(int fromIndex, int toIndex) {
		modCount++;
		_remove(fromIndex, toIndex);
	}

	@JTranscSync
	private void rangeCheck(int index) {
		if (index >= size()) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	@JTranscSync
	private void rangeCheckForAdd(int index) {
		if (index > size() || index < 0) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	@JTranscSync
	private String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size();
	}

	//@JTranscSync
	@JTranscAsync
	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c);
		return batchRemove(c, false);
	}

	//@JTranscSync
	@JTranscAsync
	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c);
		return batchRemove(c, true);
	}

	//@JTranscSync
	@JTranscAsync
	private boolean batchRemove(Collection<?> c, boolean complement) {
		int r = 0, w = 0;
		for (; r < size(); r++) if (c.contains(_get(r)) == complement) _set(w++, _get(r));
		_setLength(w);
		return r != w;
	}

	//public ListIterator<E> listIterator(int index) {
	//	if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index);
	//	return new GenericListIterator(this, index);
	//}
//
	//public ListIterator<E> listIterator() {
	//	return listIterator(0);
	//}

	@JTranscAsync
	public Iterator<E> iterator() {
		return listIterator();
	}

	@JTranscAsync
	public List<E> subList(int fromIndex, int toIndex) {
		return super.subList(fromIndex, toIndex);
	}
}

/*
@SuppressWarnings("unchecked")
@HaxeAddMembers("var _data:Array<Dynamic> = [];")
public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, java.io.Serializable {
	private E[] buffer;
	private int length;

	@JTranscMethodBody(target = "js", value = "this._data = [];")
	public ArrayList(int initialCapacity) {
		buffer = (E[])new Object[initialCapacity];
		length = 0;
	}

	public ArrayList() {
		this(0);
	}

	public ArrayList(Collection<? extends E> c) {
		this(c.size());
		addAll(c);
	}

	public void trimToSize() {
		buffer = Arrays.copyOf(buffer, length);
	}

	private void ensure(int minimumCapacity) {
		if (minimumCapacity > buffer.length) {
			buffer = Arrays.copyOf(buffer, Math.max(minimumCapacity, (buffer.length * 2) + 2));
		}
	}

	public void ensureCapacity(int minCapacity) {
		ensure(minCapacity);
	}

	@HaxeMethodBody("return _data.length;")
	@JTranscMethodBody(target = "js", value = "return this._data.length;")
	public int size() {
		return length;
	}

	@HaxeMethodBody("return _data[p0];")
	@JTranscMethodBody(target = "js", value = "return this._data[p0];")
	private E _get(int index) {
		return buffer[index];
	}

	@HaxeMethodBody("_data[p0] = p1;")
	@JTranscMethodBody(target = "js", value = "this._data[p0] = p1;")
	private void _set(int index, E element) {
		buffer[index] = element;
	}

	@HaxeMethodBody("_data = _data.slice(0, p0);")
	@JTranscMethodBody(target = "js", value = "this._data.length = p0;")
	private void _setLength(int length) {
		ensure(length);
		this.length = length;
	}

	@HaxeMethodBody("_data.push(p0);")
	@JTranscMethodBody(target = "js", value = "this._data.push(p0);")
	private void _add(E element) {
		ensure(length + 1);
		buffer[length++] = element;
	}

	private void makeHole(int index, int count) {
		ensure(length + count);
		System.arraycopy(buffer, index, buffer, index + count, length - index - count);
		length += count;
	}

	@HaxeMethodBody("N.arrayInsert(_data, p0, p1);")
	@JTranscMethodBody(target = "js", value = "this._data.splice(p0, 0, p1);")
	private void _insert(int index, E element) {
		makeHole(index, 1);
		buffer[index] = element;
	}

	@HaxeMethodBody("_data = _data.slice(0, p0).concat(p1.toArray()).concat(_data.slice(p0));")
	@JTranscMethodBody(target = "js", value = "this._data.splice.apply(this._data, [p0, 0].concat(p1.toArray()));")
	private void _insert(int index, Object[] elements) {
		makeHole(index, elements.length);
		System.arraycopy(elements, 0, buffer, index, elements.length);
	}

	@HaxeMethodBody("_data = _data.slice(0, p0).concat(_data.slice(p0));")
	@JTranscMethodBody(target = "js", value = "this._data.splice(p0, p1 - p0);")
	private void _remove(int from, int to) {
		int count = to - from;
		System.arraycopy(buffer, to, buffer, from, length - to);
		length -= count;
	}

	@HaxeMethodBody("_data.splice(p0, 1);")
	@JTranscMethodBody(target = "js", value = "this._data.splice(p0, 1);")
	private void _remove(int index) {
		_remove(index, index + 1);
	}

	private void _clear() {
		_setLength(0);
	}

	@HaxeMethodBody("p0._data = p1._data.slice(0);")
	@JTranscMethodBody(target = "js", value = "p0._data = p1._data.slice(0);")
	static private <T> void _copy(ArrayList<T> dst, ArrayList<T> src) {
		dst.buffer = Arrays.copyOf(src.buffer, src.length);
		dst.length = src.length;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean contains(Object o) {
		return indexOf(o) >= 0;
	}

	public int indexOf(Object o) {
		int len = size();
		for (int i = 0; i < len; i++) if (Objects.equals(o, _get(i))) return i;
		return -1;
	}

	public int lastIndexOf(Object o) {
		int len = size();
		for (int i = len - 1; i >= 0; i--) if (Objects.equals(o, _get(i))) return i;
		return -1;
	}

	public Object clone() {
		try {
			ArrayList<E> v = (ArrayList<E>) super.clone();
			_copy(v, this);
			v.modCount = 0;
			return v;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int len = size();
		if (a.length < len) a = (T[]) Arrays.copyOf(new Object[0], len, a.getClass());
		for (int n = 0; n < len; n++) a[n] = (T) _get(n);
		return a;
	}

	public E get(int index) {
		rangeCheck(index);
		return _get(index);
	}

	public E set(int index, E element) {
		rangeCheck(index);
		E oldValue = _get(index);
		_set(index, element);
		return oldValue;
	}

	public boolean add(E e) {
		_add(e);
		return true;
	}

	public void add(int index, E element) {
		rangeCheckForAdd(index);
		_insert(index, element);
	}

	public E remove(int index) {
		rangeCheck(index);

		modCount++;
		E oldValue = _get(index);
		_remove(index);

		return oldValue;
	}

	public boolean remove(Object o) {
		int len = size();
		for (int index = 0; index < len; index++) {
			if (Objects.equals(o, _get(index))) {
				_remove(index);
				return true;
			}
		}
		return false;
	}

	public void clear() {
		modCount++;
		_clear();
	}

	public boolean addAll(Collection<? extends E> c) {
		for (E e : c) add(e);
		return c.size() != 0;
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		rangeCheckForAdd(index);

		_insert(index, c.toArray());

		if (c.size() != 0) modCount++;
		return c.size() != 0;
	}

	protected void removeRange(int fromIndex, int toIndex) {
		modCount++;
		_remove(fromIndex, toIndex);
	}

	private void rangeCheck(int index) {
		if (index >= size()) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	private void rangeCheckForAdd(int index) {
		if (index > size() || index < 0) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
	}

	private String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size();
	}

	public boolean removeAll(Collection<?> c) {
		Objects.requireNonNull(c);
		return batchRemove(c, false);
	}

	public boolean retainAll(Collection<?> c) {
		Objects.requireNonNull(c);
		return batchRemove(c, true);
	}

	private boolean batchRemove(Collection<?> c, boolean complement) {
		int r = 0, w = 0;
		for (; r < size(); r++) if (c.contains(_get(r)) == complement) _set(w++, _get(r));
		_setLength(w);
		return r != w;
	}

	//public ListIterator<E> listIterator(int index) {
	//	if (index < 0 || index > size()) throw new IndexOutOfBoundsException("Index: " + index);
	//	return new GenericListIterator(this, index);
	//}
//
	//public ListIterator<E> listIterator() {
	//	return listIterator(0);
	//}

	public Iterator<E> iterator() {
		return listIterator();
	}

	public List<E> subList(int fromIndex, int toIndex) {
		return super.subList(fromIndex, toIndex);
	}
}
*/