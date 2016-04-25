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

import com.jtransc.internal.GenericMapEntry;

// SLOW IMPLEMENTATION!
public class IdentityHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, java.io.Serializable, Cloneable {
	private ArrayList<K> keys = new ArrayList<K>();
	private ArrayList<V> values = new ArrayList<V>();

	public IdentityHashMap() {

	}

	public IdentityHashMap(int expectedMaxSize) {
	}

	public IdentityHashMap(Map<? extends K, ? extends V> m) {
		putAll(m);
	}

	public int size() {
		return keys.size();
	}

	private static int hash(Object x, int length) {
		int h = System.identityHashCode(x);
		return ((h << 1) - (h << 8)) & (length - 1);
	}

	private int getKeyIndex(Object key) {
		for (int n = 0; n < size(); n++) if (keys.get(n) == key) return n;
		return -1;
	}

	private int getValueIndex(Object value) {
		for (int n = 0; n < size(); n++) if (values.get(n) == value) return n;
		return -1;
	}

	public V get(Object key) {
		int index = getKeyIndex(key);
		return (index >= 0) ? this.values.get(index) : null;
	}

	public boolean containsKey(Object key) {
		return getKeyIndex(key) >= 0;
	}

	public boolean containsValue(Object value) {
		return getValueIndex(value) >= 0;
	}

	public V put(K key, V value) {
		int index = getKeyIndex(key);
		if (index >= 0) {
			values.set(index, value);
			return values.get(index);
		} else {
			keys.add(key);
			values.add(value);
			return null;
		}
	}

	public V remove(Object key) {
		int index = getKeyIndex(key);
		V oldValue = null;
		if (index >= 0) {
			oldValue = this.values.get(index);
			keys.remove(index);
			values.remove(index);
		}
		return oldValue;
	}

	public void clear() {
		this.keys.clear();
		this.values.clear();
	}

	public boolean equals(Object t) {
		if (this == t) return true;
		if (!(t instanceof IdentityHashMap)) return false;
		IdentityHashMap that = (IdentityHashMap) t;
		return Objects.equals(this.keySet(), that.keySet()) && Objects.equals(this.values(), that.values());
	}

	public int hashCode() {
		int result = 0;
		for (int n = 0; n < size(); n++) {
			Object key = keys.get(n);
			Object value = values.get(n);
			result += System.identityHashCode(key) ^ System.identityHashCode(value);
		}
		return result;
	}

	public Object clone() {
		return new IdentityHashMap(this);
	}

	public Set<K> keySet() {
		return new HashSet<K>(keys);
	}

	public Collection<V> values() {
		return this.values;
	}

	public Set<Entry<K, V>> entrySet() {
		HashSet<Entry<K, V>> out = new HashSet<Entry<K, V>>();
		for (K key : keys) out.add(new GenericMapEntry<K, V>(this, key));
		return out;
	}
}
