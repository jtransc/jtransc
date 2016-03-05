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

import jtransc.internal.GenericMapEntry;

import java.io.Serializable;

// @TODO: Very slow HashMap
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
	private ArrayList<K> keys = new ArrayList<K>();
	private ArrayList<V> values = new ArrayList<V>();

	public HashMap(int initialCapacity, float loadFactor) {
	}

	public HashMap(int initialCapacity) {

	}

	public HashMap() {

	}

	public HashMap(Map<? extends K, ? extends V> m) {
		putAll(m);
	}

	public int size() {
		return keys.size();
	}

	public V get(Object key) {
		int index = keys.indexOf(key);
		return (index >= 0) ? values.get(index) : null;
	}

	public boolean containsKey(Object key) {
		return keys.contains(key);
	}

	public V put(K key, V value) {
		int index = keys.indexOf(key);
		V old = null;
		if (index < 0) {
			keys.add(key);
			values.add(value);
		} else {
			old = values.get(index);
			keys.set(index, key);
			values.set(index, value);
		}
		return old;
	}

	public V remove(Object key) {
		int index = keys.indexOf(key);
		if (index >= 0) {
			V old = values.get(index);
			keys.remove(index);
			values.remove(index);
			return old;
		}
		return null;
	}

	public void clear() {
		this.keys.clear();
		this.values.clear();
	}

	public boolean containsValue(Object value) {
		return values.indexOf(value) >= 0;
	}

	public Set<K> keySet() {
		return new HashSet(keys);
	}

	public Collection<V> values() {
		return this.values;
	}

	public Set<Entry<K, V>> entrySet() {
		HashSet<Entry<K, V>> out = new HashSet<Entry<K, V>>();
		for (K key : keys) out.add(new GenericMapEntry<K, V>(this, key));
		return out;
	}

	@Override
	native public Object clone();
}

