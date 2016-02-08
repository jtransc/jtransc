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

public abstract class AbstractMap<K, V> implements Map<K, V> {
    transient volatile Set<K> keySet;
    transient volatile Collection<V> values;

    protected AbstractMap() {
    }

    public int size() {
        return entrySet().size();
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    native public boolean containsValue(Object value);

    native public boolean containsKey(Object key);

    native public V get(Object key);

    native public V put(K key, V value);

    native public V remove(Object key);

    public void putAll(Map<? extends K, ? extends V> m) {
        for (K key : m.keySet()) this.put(key, m.get(key));
    }

    native public void clear();

    native public Set<K> keySet();

    native public Collection<V> values();

    public abstract Set<Entry<K, V>> entrySet();

    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof Map)) return false;
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size()) return false;

        try {
            Iterator<Entry<K, V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key))) return false;
                } else {
                    if (!value.equals(m.get(key))) return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int h = 0;
        Iterator<Entry<K, V>> i = entrySet().iterator();
        while (i.hasNext()) h += i.next().hashCode();
        return h;
    }

    public String toString() {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext()) return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) break;
            sb.append(',').append(' ');
        }
        return sb.append('}').toString();
    }

    protected Object clone() throws CloneNotSupportedException {
        AbstractMap<?, ?> result = (AbstractMap<?, ?>) super.clone();
        //result.keySet = null;
        //result.values = null;
        return result;
    }

    public static class SimpleEntry<K, V> implements Entry<K, V>, java.io.Serializable {
        private final K key;
        private V value;

        public SimpleEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public SimpleEntry(Entry<? extends K, ? extends V> entry) {
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }

        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        public String toString() {
            return key + "=" + value;
        }
    }
}
