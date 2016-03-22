package java.util;

public class LinkedHashMap<K, V> extends HashMap<K, V> implements Map<K, V> {
	private transient Entry<K, V> header;

	private final boolean accessOrder;

	public LinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		accessOrder = false;
	}

	public LinkedHashMap(int initialCapacity) {
		super(initialCapacity);
		accessOrder = false;
	}

	public LinkedHashMap() {
		super();
		accessOrder = false;
	}

	public LinkedHashMap(Map<? extends K, ? extends V> m) {
		super(m);
		accessOrder = false;
	}

	public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor);
		this.accessOrder = accessOrder;
	}

	void init() {
		header = new Entry<>(-1, null, null, null);
		header.before = header.after = header;
	}

	void transfer(HashMap.Entry[] newTable) {
		int newCapacity = newTable.length;
		for (Entry<K, V> e = header.after; e != header; e = e.after) {
			int index = indexFor(e.hash, newCapacity);
			e.next = newTable[index];
			newTable[index] = e;
		}
	}


	public boolean containsValue(Object value) {
		for (Entry e = header.after; e != header; e = e.after) {
			if (Objects.equals(value, e.value)) return true;
		}
		return false;
	}

	public V get(Object key) {
		Entry<K, V> e = (Entry<K, V>) getEntry(key);
		if (e == null) return null;
		e.recordAccess(this);
		return e.value;
	}

	public void clear() {
		super.clear();
		header.before = header.after = header;
	}

	private static class Entry<K, V> extends HashMap.Entry<K, V> {
		Entry<K, V> before, after;

		Entry(int hash, K key, V value, HashMap.Entry<K, V> next) {
			super(hash, key, value, next);
		}

		private void remove() {
			before.after = after;
			after.before = before;
		}

		private void addBefore(Entry<K, V> existingEntry) {
			after = existingEntry;
			before = existingEntry.before;
			before.after = this;
			after.before = this;
		}

		void recordAccess(HashMap<K, V> m) {
			LinkedHashMap<K, V> lm = (LinkedHashMap<K, V>) m;
			if (lm.accessOrder) {
				lm.modCount++;
				remove();
				addBefore(lm.header);
			}
		}

		void recordRemoval(HashMap<K, V> m) {
			remove();
		}
	}

	private abstract class LinkedHashIterator<T> implements Iterator<T> {
		Entry<K, V> nextEntry = header.after;
		Entry<K, V> lastReturned = null;

		int expectedModCount = modCount;

		public boolean hasNext() {
			return nextEntry != header;
		}

		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();

			LinkedHashMap.this.remove(lastReturned.key);
			lastReturned = null;
			expectedModCount = modCount;
		}

		Entry<K, V> nextEntry() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			if (nextEntry == header)
				throw new NoSuchElementException();

			Entry<K, V> e = lastReturned = nextEntry;
			nextEntry = e.after;
			return e;
		}
	}

	private class KeyIterator extends LinkedHashIterator<K> {
		public K next() {
			return nextEntry().getKey();
		}
	}

	private class ValueIterator extends LinkedHashIterator<V> {
		public V next() {
			return nextEntry().value;
		}
	}

	private class EntryIterator extends LinkedHashIterator<Map.Entry<K, V>> {
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	void addEntry(int hash, K key, V value, int bucketIndex) {
		createEntry(hash, key, value, bucketIndex);

		Entry<K, V> eldest = header.after;
		if (removeEldestEntry(eldest)) {
			removeEntryForKey(eldest.key);
		} else {
			if (size >= threshold) resize(2 * table.length);
		}
	}

	void createEntry(int hash, K key, V value, int bucketIndex) {
		HashMap.Entry<K, V> old = table[bucketIndex];
		Entry<K, V> e = new Entry<>(hash, key, value, old);
		table[bucketIndex] = e;
		e.addBefore(header);
		size++;
	}

	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return false;
	}
}