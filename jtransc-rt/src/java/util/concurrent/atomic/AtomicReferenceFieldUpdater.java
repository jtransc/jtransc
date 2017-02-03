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

package java.util.concurrent.atomic;

import java.lang.reflect.Field;

@SuppressWarnings({"WeakerAccess", "NullableProblems", "unchecked", "unused"})
public abstract class AtomicReferenceFieldUpdater<T, V> {
	public static <U, W> AtomicReferenceFieldUpdater<U, W> newUpdater(Class<U> tclass, Class<W> vclass, String fieldName) {
		Field f;
		try {
			f = tclass.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			try {
				f = tclass.getField(fieldName);
			} catch (NoSuchFieldException e2) {
				throw new RuntimeException(e2);
			}
		}

		if (f == null) {
			throw new RuntimeException("field==null");
		}

		final Field field = f;

		return new AtomicReferenceFieldUpdater<U, W>() {
			@Override
			public boolean compareAndSet(U obj, W expect, W update) {
				try {
					if (field.get(obj) == expect) {
						field.set(obj, update);
						return true;
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				return false;
			}

			@Override
			public boolean weakCompareAndSet(U obj, W expect, W update) {
				return this.compareAndSet(obj, expect, update);
			}

			@Override
			public void set(U obj, W newValue) {
				try {
					field.set(obj, newValue);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void lazySet(U obj, W newValue) {
				try {
					field.set(obj, newValue);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			@Override
			public W get(U obj) {
				try {
					return (W) field.get(obj);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	protected AtomicReferenceFieldUpdater() {
	}

	public abstract boolean compareAndSet(T obj, V expect, V update);

	public abstract boolean weakCompareAndSet(T obj, V expect, V update);

	public abstract void set(T obj, V newValue);

	public abstract void lazySet(T obj, V newValue);

	public abstract V get(T obj);

	public V getAndSet(T obj, V newValue) {
		V prev;
		do {
			prev = get(obj);
		} while (!compareAndSet(obj, prev, newValue));
		return prev;
	}
}
