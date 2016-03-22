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

public class Hashtable<K, V> extends Dictionary<K, V> implements Map<K, V>, Cloneable, java.io.Serializable {
	private HashMap<K, V> data;

	public Hashtable(int initialCapacity, float loadFactor) {
		this.data = new HashMap<>(initialCapacity, loadFactor);
	}

	public Hashtable(int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public Hashtable() {
		this(11, 0.75f);
	}

	public Hashtable(Map<? extends K, ? extends V> t) {
		this(Math.max(2 * t.size(), 11), 0.75f);
		putAll(t);
	}

	public synchronized int size() {
		return data.size();
	}

	public synchronized boolean isEmpty() {
		return data.isEmpty();
	}

	public synchronized Enumeration<K> keys() {
		return Collections.enumeration(data.keySet());
	}

	public synchronized Enumeration<V> elements() {
		return Collections.enumeration(data.values());
	}

	public synchronized boolean contains(Object value) {
		return data.containsValue(value);
	}

	public boolean containsValue(Object value) {
		return data.containsValue(value);
	}

	public synchronized boolean containsKey(Object key) {
		return data.containsKey(key);
	}

	public synchronized V get(Object key) {
		return data.get(key);
	}

	protected void rehash() {
	}

	public synchronized V put(K key, V value) {
		return data.put(key, value);
	}

	public synchronized V remove(Object key) {
		return data.remove(key);
	}

	public synchronized void putAll(Map<? extends K, ? extends V> t) {
		data.putAll(t);
	}

	public synchronized void clear() {
		data.clear();
	}

	public synchronized Object clone() {
		return new Hashtable<>(data);
	}

	public synchronized String toString() {
		return data.toString();
	}

	public Set<K> keySet() {
		return data.keySet();
	}

	public Set<Entry<K, V>> entrySet() {
		return data.entrySet();
	}

	public Collection<V> values() {
		return data.values();
	}

	public synchronized boolean equals(Object o) {
		if (!(o instanceof Hashtable)) return false;
		return Objects.equals(this.data, ((Hashtable) o).data);
	}

	public synchronized int hashCode() {
		return data.hashCode();
	}
}
