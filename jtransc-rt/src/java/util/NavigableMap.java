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

public interface NavigableMap<K, V> extends SortedMap<K, V> {
    Entry<K, V> lowerEntry(K key);

    K lowerKey(K key);

    Entry<K, V> floorEntry(K key);

    K floorKey(K key);

    Entry<K, V> ceilingEntry(K key);

    K ceilingKey(K key);

    Entry<K, V> higherEntry(K key);

    K higherKey(K key);

    Entry<K, V> firstEntry();

    Entry<K, V> lastEntry();

    Entry<K, V> pollFirstEntry();

    Entry<K, V> pollLastEntry();

    NavigableMap<K, V> descendingMap();

    NavigableSet<K> navigableKeySet();

    NavigableSet<K> descendingKeySet();

    NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    NavigableMap<K, V> headMap(K toKey, boolean inclusive);

    NavigableMap<K, V> tailMap(K fromKey, boolean inclusive);

    SortedMap<K, V> subMap(K fromKey, K toKey);

    SortedMap<K, V> headMap(K toKey);

    SortedMap<K, V> tailMap(K fromKey);
}
