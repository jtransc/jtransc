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

package jtransc.internal;

import java.util.List;
import java.util.ListIterator;

public class GenericListIterator<T> implements ListIterator<T> {
	private List<T> list;
	private int index = 0;

	public GenericListIterator(List<T> list, int index) {
		this.list = list;
		this.index = index;
	}

	public boolean hasNext() {
		return index < list.size();
	}

	public T next() {
		return list.get(index++);
	}

	@Override
	public boolean hasPrevious() {
		return index > 0;
	}

	@Override
	public T previous() {
		return list.get(index - 1);
	}

	@Override
	public int nextIndex() {
		return index + 1;
	}

	@Override
	public int previousIndex() {
		return index - 1;
	}

	public void remove() {
		this.list.remove(index);
	}

	@Override
	public void set(T t) {
		this.list.set(index, t);
	}

	@Override
	public void add(T t) {
		this.list.add(index, t);
	}
}
