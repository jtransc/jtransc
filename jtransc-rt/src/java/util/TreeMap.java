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

public class TreeMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Cloneable, java.io.Serializable {
	public TreeMap() {

	}

	public TreeMap(Comparator<? super K> comparator) {

	}

	public TreeMap(Map<? extends K, ? extends V> m) {
		putAll(m);
	}

	public TreeMap(SortedMap<K, ? extends V> m) {
	}

	native public int size();

	native public boolean containsKey(Object key);

	native public boolean containsValue(Object value);

	native public V get(Object key);

	native public Comparator<? super K> comparator();

	native public K firstKey();

	native public K lastKey();

	native public void putAll(Map<? extends K, ? extends V> map);

	native public V put(K key, V value);

	native public V remove(Object key);

	native public void clear();

	native public Object clone();

	native public Entry<K, V> firstEntry();

	native public Entry<K, V> lastEntry();

	native public Entry<K, V> pollFirstEntry();

	native public Entry<K, V> pollLastEntry();

	native public Entry<K, V> lowerEntry(K key);

	native public K lowerKey(K key);

	native public Entry<K, V> floorEntry(K key);

	native public K floorKey(K key);

	native public Entry<K, V> ceilingEntry(K key);

	native public K ceilingKey(K key);

	native public Entry<K, V> higherEntry(K key);

	native public K higherKey(K key);

	native public Set<K> keySet();

	native public NavigableSet<K> navigableKeySet();

	native public NavigableSet<K> descendingKeySet();

	native public Collection<V> values();

	native public Set<Entry<K, V>> entrySet();

	native public NavigableMap<K, V> descendingMap();

	native public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

	native public NavigableMap<K, V> headMap(K toKey, boolean inclusive);

	native public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive);

	native public SortedMap<K, V> subMap(K fromKey, K toKey);

	native public SortedMap<K, V> headMap(K toKey);

	native public SortedMap<K, V> tailMap(K fromKey);

	native public boolean replace(K key, V oldValue, V newValue);

	native public V replace(K key, V value);
}
