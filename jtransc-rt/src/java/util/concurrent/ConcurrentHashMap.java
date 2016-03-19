package java.util.concurrent;

import java.io.Serializable;
import java.util.*;

public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {
	private HashMap<K, V> map = new HashMap<>();

	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public V get(Object key) {
		return map.get(key);
	}

	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		map.putAll(m);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		boolean removed = remove(key, oldValue);
		put(key, newValue);
		return removed;
	}

	@Override
	public V replace(K key, V value) {
		V old = remove(key);
		put(key, value);
		return old;
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public boolean equals(Object o) {
		return map.equals(o);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		V oldValue = get(key);
		if (oldValue == null) {
			put(key, value);
		}
		return oldValue;
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public boolean remove(Object key, Object value) {
		V old = get(key);
		if (Objects.equals(old, value)) {
			remove(key);
			return true;
		} else {
			return false;
		}
	}
}