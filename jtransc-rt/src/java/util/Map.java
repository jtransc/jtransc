/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface Map<K, V> {
	interface Entry<K, V> {
		boolean equals(Object object);

		K getKey();

		V getValue();

		int hashCode();

		V setValue(V object);
	}

	void clear();

	boolean containsKey(Object key);

	boolean containsValue(Object value);

	Set<Entry<K, V>> entrySet();

	boolean equals(Object object);

	V get(Object key);

	int hashCode();

	boolean isEmpty();

	Set<K> keySet();

	V put(K key, V value);

	void putAll(Map<? extends K, ? extends V> map);

	V remove(Object key);

	int size();

	Collection<V> values();

	default V putIfAbsent(K key, V value) {
		if (containsKey(key)) {
			return get(key);
		} else {
			return put(key, value);
		}
	}

	default V getOrDefault(Object key, V defaultValue) {
		return containsKey(key) ? get(key) : defaultValue;
	}

	default void forEach(BiConsumer<? super K, ? super V> action) {
		for (final Map.Entry<K, V> e : entrySet()) {
			action.accept(e.getKey(), e.getValue());
		}
	}

	default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		for (final Map.Entry<K, V> e : entrySet()) {
			final K k = e.getKey();
			final V v = e.getValue();
			e.setValue(function.apply(k, v));
		}
	}

	default boolean remove(Object key, Object value) {
		Object cvalue = get(key);
		if (!Objects.equals(cvalue, value) || (cvalue == null && !containsKey(key))) return false;
		remove(key);
		return true;
	}

	default boolean replace(K key, V oldValue, V newValue) {
		final Object next = get(key);
		if (Objects.equals(next, oldValue) && (next != null || containsKey(key))) {
			put(key, newValue);
			return true;
		} else {
			return false;
		}
	}

	default V replace(K key, V value) {
		if (containsKey(key)) {
			return put(key, value);
		} else {
			return null;
		}
	}

	default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
		if (!containsKey(key)) return null;
		final V prev = get(key);
		final V next = remap.apply(key, prev);
		if (next != null) {
			put(key, next);
			return next;
		} else {
			remove(key);
			return null;
		}
	}

	default V computeIfAbsent(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
		if (containsKey(key)) return get(key);
		final V prev = get(key);
		final V next = remap.apply(key, prev);
		if (next != null) put(key, next);
		return next;
	}

	default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remap) {
		final V prev = get(key);
		final V next = remap.apply(key, prev);
		if (next == null) {
			if (prev != null || containsKey(key)) remove(key);
		} else {
			put(key, next);
		}
		return next;
	}

	default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remap) {
		final V prev = get(key);
		final V next = (prev == null) ? value : remap.apply(prev, value);
		if (next == null) {
			remove(key);
		} else {
			put(key, next);
		}
		return next;
	}
}
