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
    public Hashtable(int initialCapacity, float loadFactor) {
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

    native public synchronized int size();

    native public synchronized boolean isEmpty();

    native public synchronized Enumeration<K> keys();

    native public synchronized Enumeration<V> elements();

    native public synchronized boolean contains(Object value);

    native public boolean containsValue(Object value);

    native public synchronized boolean containsKey(Object key);

    native public synchronized V get(Object key);

    native protected void rehash();

    native public synchronized V put(K key, V value);

    native public synchronized V remove(Object key);

    native public synchronized void putAll(Map<? extends K, ? extends V> t);

    native public synchronized void clear();

    native public synchronized Object clone();

    native public synchronized String toString();

    native public Set<K> keySet();

    native public Set<Entry<K, V>> entrySet();

    native public Collection<V> values();

    native public synchronized boolean equals(Object o);

    native public synchronized int hashCode();
}
