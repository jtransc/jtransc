package java.util;

public class TreeMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Cloneable, java.io.Serializable {
	private final Comparator<? super K> comparator;

	private transient Entry<K, V> root = null;

	private transient int size = 0;

	private transient int modCount = 0;

	public TreeMap() {
		comparator = null;
	}

	public TreeMap(Comparator<? super K> comparator) {
		this.comparator = comparator;
	}

	public TreeMap(Map<? extends K, ? extends V> m) {
		comparator = null;
		putAll(m);
	}

	public TreeMap(SortedMap<K, ? extends V> m) {
		comparator = m.comparator();
		try {
			buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
		} catch (java.io.IOException cannotHappen) {
		} catch (ClassNotFoundException cannotHappen) {
		}
	}

	public int size() {
		return size;
	}


	public boolean containsKey(Object key) {
		return getEntry(key) != null;
	}


	public boolean containsValue(Object value) {
		for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e))
			if (valEquals(value, e.value))
				return true;
		return false;
	}


	public V get(Object key) {
		Entry<K, V> p = getEntry(key);
		return (p == null ? null : p.value);
	}

	public Comparator<? super K> comparator() {
		return comparator;
	}


	public K firstKey() {
		return key(getFirstEntry());
	}


	public K lastKey() {
		return key(getLastEntry());
	}


	public void putAll(Map<? extends K, ? extends V> map) {
		int mapSize = map.size();
		if (size == 0 && mapSize != 0 && map instanceof SortedMap) {
			Comparator c = ((SortedMap) map).comparator();
			if (c == comparator || (c != null && c.equals(comparator))) {
				++modCount;
				try {
					buildFromSorted(mapSize, map.entrySet().iterator(),
						null, null);
				} catch (java.io.IOException cannotHappen) {
				} catch (ClassNotFoundException cannotHappen) {
				}
				return;
			}
		}
		super.putAll(map);
	}


	final Entry<K, V> getEntry(Object key) {

		if (comparator != null)
			return getEntryUsingComparator(key);
		if (key == null)
			throw new NullPointerException();
		Comparable<? super K> k = (Comparable<? super K>) key;
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = k.compareTo(p.key);
			if (cmp < 0)
				p = p.left;
			else if (cmp > 0)
				p = p.right;
			else
				return p;
		}
		return null;
	}


	final Entry<K, V> getEntryUsingComparator(Object key) {
		K k = (K) key;
		Comparator<? super K> cpr = comparator;
		if (cpr != null) {
			Entry<K, V> p = root;
			while (p != null) {
				int cmp = cpr.compare(k, p.key);
				if (cmp < 0)
					p = p.left;
				else if (cmp > 0)
					p = p.right;
				else
					return p;
			}
		}
		return null;
	}


	final Entry<K, V> getCeilingEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp < 0) {
				if (p.left != null)
					p = p.left;
				else
					return p;
			} else if (cmp > 0) {
				if (p.right != null) {
					p = p.right;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.right) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			} else
				return p;
		}
		return null;
	}


	final Entry<K, V> getFloorEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp > 0) {
				if (p.right != null)
					p = p.right;
				else
					return p;
			} else if (cmp < 0) {
				if (p.left != null) {
					p = p.left;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.left) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			} else
				return p;

		}
		return null;
	}


	final Entry<K, V> getHigherEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp < 0) {
				if (p.left != null)
					p = p.left;
				else
					return p;
			} else {
				if (p.right != null) {
					p = p.right;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.right) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			}
		}
		return null;
	}


	final Entry<K, V> getLowerEntry(K key) {
		Entry<K, V> p = root;
		while (p != null) {
			int cmp = compare(key, p.key);
			if (cmp > 0) {
				if (p.right != null)
					p = p.right;
				else
					return p;
			} else {
				if (p.left != null) {
					p = p.left;
				} else {
					Entry<K, V> parent = p.parent;
					Entry<K, V> ch = p;
					while (parent != null && ch == parent.left) {
						ch = parent;
						parent = parent.parent;
					}
					return parent;
				}
			}
		}
		return null;
	}


	public V put(K key, V value) {
		Entry<K, V> t = root;
		if (t == null) {
			compare(key, key);

			root = new Entry<>(key, value, null);
			size = 1;
			modCount++;
			return null;
		}
		int cmp;
		Entry<K, V> parent;

		Comparator<? super K> cpr = comparator;
		if (cpr != null) {
			do {
				parent = t;
				cmp = cpr.compare(key, t.key);
				if (cmp < 0)
					t = t.left;
				else if (cmp > 0)
					t = t.right;
				else
					return t.setValue(value);
			} while (t != null);
		} else {
			if (key == null)
				throw new NullPointerException();
			Comparable<? super K> k = (Comparable<? super K>) key;
			do {
				parent = t;
				cmp = k.compareTo(t.key);
				if (cmp < 0)
					t = t.left;
				else if (cmp > 0)
					t = t.right;
				else
					return t.setValue(value);
			} while (t != null);
		}
		Entry<K, V> e = new Entry<>(key, value, parent);
		if (cmp < 0)
			parent.left = e;
		else
			parent.right = e;
		fixAfterInsertion(e);
		size++;
		modCount++;
		return null;
	}


	public V remove(Object key) {
		Entry<K, V> p = getEntry(key);
		if (p == null)
			return null;

		V oldValue = p.value;
		deleteEntry(p);
		return oldValue;
	}


	public void clear() {
		modCount++;
		size = 0;
		root = null;
	}


	public Object clone() {
		TreeMap<K, V> clone = null;
		try {
			clone = (TreeMap<K, V>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}


		clone.root = null;
		clone.size = 0;
		clone.modCount = 0;
		clone.entrySet = null;
		clone.navigableKeySet = null;
		clone.descendingMap = null;


		try {
			clone.buildFromSorted(size, entrySet().iterator(), null, null);
		} catch (java.io.IOException cannotHappen) {
		} catch (ClassNotFoundException cannotHappen) {
		}

		return clone;
	}


	public Map.Entry<K, V> firstEntry() {
		return exportEntry(getFirstEntry());
	}


	public Map.Entry<K, V> lastEntry() {
		return exportEntry(getLastEntry());
	}


	public Map.Entry<K, V> pollFirstEntry() {
		Entry<K, V> p = getFirstEntry();
		Map.Entry<K, V> result = exportEntry(p);
		if (p != null)
			deleteEntry(p);
		return result;
	}


	public Map.Entry<K, V> pollLastEntry() {
		Entry<K, V> p = getLastEntry();
		Map.Entry<K, V> result = exportEntry(p);
		if (p != null)
			deleteEntry(p);
		return result;
	}


	public Map.Entry<K, V> lowerEntry(K key) {
		return exportEntry(getLowerEntry(key));
	}


	public K lowerKey(K key) {
		return keyOrNull(getLowerEntry(key));
	}


	public Map.Entry<K, V> floorEntry(K key) {
		return exportEntry(getFloorEntry(key));
	}


	public K floorKey(K key) {
		return keyOrNull(getFloorEntry(key));
	}


	public Map.Entry<K, V> ceilingEntry(K key) {
		return exportEntry(getCeilingEntry(key));
	}


	public K ceilingKey(K key) {
		return keyOrNull(getCeilingEntry(key));
	}


	public Map.Entry<K, V> higherEntry(K key) {
		return exportEntry(getHigherEntry(key));
	}


	public K higherKey(K key) {
		return keyOrNull(getHigherEntry(key));
	}


	private transient EntrySet entrySet = null;
	private transient KeySet<K> navigableKeySet = null;
	private transient NavigableMap<K, V> descendingMap = null;


	public Set<K> keySet() {
		return navigableKeySet();
	}


	public NavigableSet<K> navigableKeySet() {
		KeySet<K> nks = navigableKeySet;
		return (nks != null) ? nks : (navigableKeySet = new KeySet(this));
	}


	public NavigableSet<K> descendingKeySet() {
		return descendingMap().navigableKeySet();
	}


	public Collection<V> values() {
		Collection<V> vs = values;
		return (vs != null) ? vs : (values = new Values());
	}


	public Set<Map.Entry<K, V>> entrySet() {
		EntrySet es = entrySet;
		return (es != null) ? es : (entrySet = new EntrySet());
	}


	public NavigableMap<K, V> descendingMap() {
		NavigableMap<K, V> km = descendingMap;
		return (km != null) ? km :
			(descendingMap = new DescendingSubMap(this,
				true, null, true,
				true, null, true));
	}


	public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive,
	                                 K toKey, boolean toInclusive) {
		return new AscendingSubMap(this,
			false, fromKey, fromInclusive,
			false, toKey, toInclusive);
	}


	public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
		return new AscendingSubMap(this,
			true, null, true,
			false, toKey, inclusive);
	}


	public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
		return new AscendingSubMap(this,
			false, fromKey, inclusive,
			true, null, true);
	}


	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, true, toKey, false);
	}


	public SortedMap<K, V> headMap(K toKey) {
		return headMap(toKey, false);
	}


	public SortedMap<K, V> tailMap(K fromKey) {
		return tailMap(fromKey, true);
	}


	class Values extends AbstractCollection<V> {
		public Iterator<V> iterator() {
			return new ValueIterator(getFirstEntry());
		}

		public int size() {
			return TreeMap.this.size();
		}

		public boolean contains(Object o) {
			return TreeMap.this.containsValue(o);
		}

		public boolean remove(Object o) {
			for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
				if (valEquals(e.getValue(), o)) {
					deleteEntry(e);
					return true;
				}
			}
			return false;
		}

		public void clear() {
			TreeMap.this.clear();
		}
	}

	class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		public Iterator<Map.Entry<K, V>> iterator() {
			return new EntryIterator(getFirstEntry());
		}

		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			V value = entry.getValue();
			Entry<K, V> p = getEntry(entry.getKey());
			return p != null && valEquals(p.getValue(), value);
		}

		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			V value = entry.getValue();
			Entry<K, V> p = getEntry(entry.getKey());
			if (p != null && valEquals(p.getValue(), value)) {
				deleteEntry(p);
				return true;
			}
			return false;
		}

		public int size() {
			return TreeMap.this.size();
		}

		public void clear() {
			TreeMap.this.clear();
		}
	}


	Iterator<K> keyIterator() {
		return new KeyIterator(getFirstEntry());
	}

	Iterator<K> descendingKeyIterator() {
		return new DescendingKeyIterator(getLastEntry());
	}

	static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
		private final NavigableMap<E, Object> m;

		KeySet(NavigableMap<E, Object> map) {
			m = map;
		}

		public Iterator<E> iterator() {
			if (m instanceof TreeMap)
				return ((TreeMap<E, Object>) m).keyIterator();
			else
				return (Iterator<E>) (((TreeMap.NavigableSubMap) m).keyIterator());
		}

		public Iterator<E> descendingIterator() {
			if (m instanceof TreeMap)
				return ((TreeMap<E, Object>) m).descendingKeyIterator();
			else
				return (Iterator<E>) (((TreeMap.NavigableSubMap) m).descendingKeyIterator());
		}

		public int size() {
			return m.size();
		}

		public boolean isEmpty() {
			return m.isEmpty();
		}

		public boolean contains(Object o) {
			return m.containsKey(o);
		}

		public void clear() {
			m.clear();
		}

		public E lower(E e) {
			return m.lowerKey(e);
		}

		public E floor(E e) {
			return m.floorKey(e);
		}

		public E ceiling(E e) {
			return m.ceilingKey(e);
		}

		public E higher(E e) {
			return m.higherKey(e);
		}

		public E first() {
			return m.firstKey();
		}

		public E last() {
			return m.lastKey();
		}

		public Comparator<? super E> comparator() {
			return m.comparator();
		}

		public E pollFirst() {
			Map.Entry<E, Object> e = m.pollFirstEntry();
			return (e == null) ? null : e.getKey();
		}

		public E pollLast() {
			Map.Entry<E, Object> e = m.pollLastEntry();
			return (e == null) ? null : e.getKey();
		}

		public boolean remove(Object o) {
			int oldSize = size();
			m.remove(o);
			return size() != oldSize;
		}

		public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
		                              E toElement, boolean toInclusive) {
			return new KeySet<>(m.subMap(fromElement, fromInclusive,
				toElement, toInclusive));
		}

		public NavigableSet<E> headSet(E toElement, boolean inclusive) {
			return new KeySet<>(m.headMap(toElement, inclusive));
		}

		public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
			return new KeySet<>(m.tailMap(fromElement, inclusive));
		}

		public SortedSet<E> subSet(E fromElement, E toElement) {
			return subSet(fromElement, true, toElement, false);
		}

		public SortedSet<E> headSet(E toElement) {
			return headSet(toElement, false);
		}

		public SortedSet<E> tailSet(E fromElement) {
			return tailSet(fromElement, true);
		}

		public NavigableSet<E> descendingSet() {
			return new KeySet(m.descendingMap());
		}
	}


	abstract class PrivateEntryIterator<T> implements Iterator<T> {
		Entry<K, V> next;
		Entry<K, V> lastReturned;
		int expectedModCount;

		PrivateEntryIterator(Entry<K, V> first) {
			expectedModCount = modCount;
			lastReturned = null;
			next = first;
		}

		public final boolean hasNext() {
			return next != null;
		}

		final Entry<K, V> nextEntry() {
			Entry<K, V> e = next;
			if (e == null)
				throw new NoSuchElementException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = successor(e);
			lastReturned = e;
			return e;
		}

		final Entry<K, V> prevEntry() {
			Entry<K, V> e = next;
			if (e == null)
				throw new NoSuchElementException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = predecessor(e);
			lastReturned = e;
			return e;
		}

		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();

			if (lastReturned.left != null && lastReturned.right != null)
				next = lastReturned;
			deleteEntry(lastReturned);
			expectedModCount = modCount;
			lastReturned = null;
		}
	}

	final class EntryIterator extends PrivateEntryIterator<Map.Entry<K, V>> {
		EntryIterator(Entry<K, V> first) {
			super(first);
		}

		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	final class ValueIterator extends PrivateEntryIterator<V> {
		ValueIterator(Entry<K, V> first) {
			super(first);
		}

		public V next() {
			return nextEntry().value;
		}
	}

	final class KeyIterator extends PrivateEntryIterator<K> {
		KeyIterator(Entry<K, V> first) {
			super(first);
		}

		public K next() {
			return nextEntry().key;
		}
	}

	final class DescendingKeyIterator extends PrivateEntryIterator<K> {
		DescendingKeyIterator(Entry<K, V> first) {
			super(first);
		}

		public K next() {
			return prevEntry().key;
		}
	}


	final int compare(Object k1, Object k2) {
		return comparator == null ? ((Comparable<? super K>) k1).compareTo((K) k2)
			: comparator.compare((K) k1, (K) k2);
	}


	static final boolean valEquals(Object o1, Object o2) {
		return (o1 == null ? o2 == null : o1.equals(o2));
	}


	static <K, V> Map.Entry<K, V> exportEntry(TreeMap.Entry<K, V> e) {
		return (e == null) ? null :
			new AbstractMap.SimpleImmutableEntry<>(e);
	}


	static <K, V> K keyOrNull(TreeMap.Entry<K, V> e) {
		return (e == null) ? null : e.key;
	}


	static <K> K key(Entry<K, ?> e) {
		if (e == null)
			throw new NoSuchElementException();
		return e.key;
	}


	private static final Object UNBOUNDED = new Object();


	abstract static class NavigableSubMap<K, V> extends AbstractMap<K, V>
		implements NavigableMap<K, V>, java.io.Serializable {

		final TreeMap<K, V> m;


		final K lo, hi;
		final boolean fromStart, toEnd;
		final boolean loInclusive, hiInclusive;

		NavigableSubMap(TreeMap<K, V> m,
		                boolean fromStart, K lo, boolean loInclusive,
		                boolean toEnd, K hi, boolean hiInclusive) {
			if (!fromStart && !toEnd) {
				if (m.compare(lo, hi) > 0)
					throw new IllegalArgumentException("fromKey > toKey");
			} else {
				if (!fromStart)
					m.compare(lo, lo);
				if (!toEnd)
					m.compare(hi, hi);
			}

			this.m = m;
			this.fromStart = fromStart;
			this.lo = lo;
			this.loInclusive = loInclusive;
			this.toEnd = toEnd;
			this.hi = hi;
			this.hiInclusive = hiInclusive;
		}


		final boolean tooLow(Object key) {
			if (!fromStart) {
				int c = m.compare(key, lo);
				if (c < 0 || (c == 0 && !loInclusive))
					return true;
			}
			return false;
		}

		final boolean tooHigh(Object key) {
			if (!toEnd) {
				int c = m.compare(key, hi);
				if (c > 0 || (c == 0 && !hiInclusive))
					return true;
			}
			return false;
		}

		final boolean inRange(Object key) {
			return !tooLow(key) && !tooHigh(key);
		}

		final boolean inClosedRange(Object key) {
			return (fromStart || m.compare(key, lo) >= 0)
				&& (toEnd || m.compare(hi, key) >= 0);
		}

		final boolean inRange(Object key, boolean inclusive) {
			return inclusive ? inRange(key) : inClosedRange(key);
		}


		final TreeMap.Entry<K, V> absLowest() {
			TreeMap.Entry<K, V> e =
				(fromStart ? m.getFirstEntry() :
					(loInclusive ? m.getCeilingEntry(lo) :
						m.getHigherEntry(lo)));
			return (e == null || tooHigh(e.key)) ? null : e;
		}

		final TreeMap.Entry<K, V> absHighest() {
			TreeMap.Entry<K, V> e =
				(toEnd ? m.getLastEntry() :
					(hiInclusive ? m.getFloorEntry(hi) :
						m.getLowerEntry(hi)));
			return (e == null || tooLow(e.key)) ? null : e;
		}

		final TreeMap.Entry<K, V> absCeiling(K key) {
			if (tooLow(key))
				return absLowest();
			TreeMap.Entry<K, V> e = m.getCeilingEntry(key);
			return (e == null || tooHigh(e.key)) ? null : e;
		}

		final TreeMap.Entry<K, V> absHigher(K key) {
			if (tooLow(key))
				return absLowest();
			TreeMap.Entry<K, V> e = m.getHigherEntry(key);
			return (e == null || tooHigh(e.key)) ? null : e;
		}

		final TreeMap.Entry<K, V> absFloor(K key) {
			if (tooHigh(key))
				return absHighest();
			TreeMap.Entry<K, V> e = m.getFloorEntry(key);
			return (e == null || tooLow(e.key)) ? null : e;
		}

		final TreeMap.Entry<K, V> absLower(K key) {
			if (tooHigh(key))
				return absHighest();
			TreeMap.Entry<K, V> e = m.getLowerEntry(key);
			return (e == null || tooLow(e.key)) ? null : e;
		}


		final TreeMap.Entry<K, V> absHighFence() {
			return (toEnd ? null : (hiInclusive ?
				m.getHigherEntry(hi) :
				m.getCeilingEntry(hi)));
		}


		final TreeMap.Entry<K, V> absLowFence() {
			return (fromStart ? null : (loInclusive ?
				m.getLowerEntry(lo) :
				m.getFloorEntry(lo)));
		}


		abstract TreeMap.Entry<K, V> subLowest();

		abstract TreeMap.Entry<K, V> subHighest();

		abstract TreeMap.Entry<K, V> subCeiling(K key);

		abstract TreeMap.Entry<K, V> subHigher(K key);

		abstract TreeMap.Entry<K, V> subFloor(K key);

		abstract TreeMap.Entry<K, V> subLower(K key);


		abstract Iterator<K> keyIterator();


		abstract Iterator<K> descendingKeyIterator();


		public boolean isEmpty() {
			return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
		}

		public int size() {
			return (fromStart && toEnd) ? m.size() : entrySet().size();
		}

		public final boolean containsKey(Object key) {
			return inRange(key) && m.containsKey(key);
		}

		public final V put(K key, V value) {
			if (!inRange(key))
				throw new IllegalArgumentException("key out of range");
			return m.put(key, value);
		}

		public final V get(Object key) {
			return !inRange(key) ? null : m.get(key);
		}

		public final V remove(Object key) {
			return !inRange(key) ? null : m.remove(key);
		}

		public final Map.Entry<K, V> ceilingEntry(K key) {
			return exportEntry(subCeiling(key));
		}

		public final K ceilingKey(K key) {
			return keyOrNull(subCeiling(key));
		}

		public final Map.Entry<K, V> higherEntry(K key) {
			return exportEntry(subHigher(key));
		}

		public final K higherKey(K key) {
			return keyOrNull(subHigher(key));
		}

		public final Map.Entry<K, V> floorEntry(K key) {
			return exportEntry(subFloor(key));
		}

		public final K floorKey(K key) {
			return keyOrNull(subFloor(key));
		}

		public final Map.Entry<K, V> lowerEntry(K key) {
			return exportEntry(subLower(key));
		}

		public final K lowerKey(K key) {
			return keyOrNull(subLower(key));
		}

		public final K firstKey() {
			return key(subLowest());
		}

		public final K lastKey() {
			return key(subHighest());
		}

		public final Map.Entry<K, V> firstEntry() {
			return exportEntry(subLowest());
		}

		public final Map.Entry<K, V> lastEntry() {
			return exportEntry(subHighest());
		}

		public final Map.Entry<K, V> pollFirstEntry() {
			TreeMap.Entry<K, V> e = subLowest();
			Map.Entry<K, V> result = exportEntry(e);
			if (e != null)
				m.deleteEntry(e);
			return result;
		}

		public final Map.Entry<K, V> pollLastEntry() {
			TreeMap.Entry<K, V> e = subHighest();
			Map.Entry<K, V> result = exportEntry(e);
			if (e != null)
				m.deleteEntry(e);
			return result;
		}


		transient NavigableMap<K, V> descendingMapView = null;
		transient EntrySetView entrySetView = null;
		transient KeySet<K> navigableKeySetView = null;

		public final NavigableSet<K> navigableKeySet() {
			KeySet<K> nksv = navigableKeySetView;
			return (nksv != null) ? nksv :
				(navigableKeySetView = new TreeMap.KeySet(this));
		}

		public final Set<K> keySet() {
			return navigableKeySet();
		}

		public NavigableSet<K> descendingKeySet() {
			return descendingMap().navigableKeySet();
		}

		public final SortedMap<K, V> subMap(K fromKey, K toKey) {
			return subMap(fromKey, true, toKey, false);
		}

		public final SortedMap<K, V> headMap(K toKey) {
			return headMap(toKey, false);
		}

		public final SortedMap<K, V> tailMap(K fromKey) {
			return tailMap(fromKey, true);
		}


		abstract class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
			private transient int size = -1, sizeModCount;

			public int size() {
				if (fromStart && toEnd)
					return m.size();
				if (size == -1 || sizeModCount != m.modCount) {
					sizeModCount = m.modCount;
					size = 0;
					Iterator i = iterator();
					while (i.hasNext()) {
						size++;
						i.next();
					}
				}
				return size;
			}

			public boolean isEmpty() {
				TreeMap.Entry<K, V> n = absLowest();
				return n == null || tooHigh(n.key);
			}

			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
				K key = entry.getKey();
				if (!inRange(key))
					return false;
				TreeMap.Entry node = m.getEntry(key);
				return node != null &&
					valEquals(node.getValue(), entry.getValue());
			}

			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
				K key = entry.getKey();
				if (!inRange(key))
					return false;
				TreeMap.Entry<K, V> node = m.getEntry(key);
				if (node != null && valEquals(node.getValue(),
					entry.getValue())) {
					m.deleteEntry(node);
					return true;
				}
				return false;
			}
		}


		abstract class SubMapIterator<T> implements Iterator<T> {
			TreeMap.Entry<K, V> lastReturned;
			TreeMap.Entry<K, V> next;
			final Object fenceKey;
			int expectedModCount;

			SubMapIterator(TreeMap.Entry<K, V> first,
			               TreeMap.Entry<K, V> fence) {
				expectedModCount = m.modCount;
				lastReturned = null;
				next = first;
				fenceKey = fence == null ? UNBOUNDED : fence.key;
			}

			public final boolean hasNext() {
				return next != null && next.key != fenceKey;
			}

			final TreeMap.Entry<K, V> nextEntry() {
				TreeMap.Entry<K, V> e = next;
				if (e == null || e.key == fenceKey)
					throw new NoSuchElementException();
				if (m.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				next = successor(e);
				lastReturned = e;
				return e;
			}

			final TreeMap.Entry<K, V> prevEntry() {
				TreeMap.Entry<K, V> e = next;
				if (e == null || e.key == fenceKey)
					throw new NoSuchElementException();
				if (m.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				next = predecessor(e);
				lastReturned = e;
				return e;
			}

			final void removeAscending() {
				if (lastReturned == null)
					throw new IllegalStateException();
				if (m.modCount != expectedModCount)
					throw new ConcurrentModificationException();

				if (lastReturned.left != null && lastReturned.right != null)
					next = lastReturned;
				m.deleteEntry(lastReturned);
				lastReturned = null;
				expectedModCount = m.modCount;
			}

			final void removeDescending() {
				if (lastReturned == null)
					throw new IllegalStateException();
				if (m.modCount != expectedModCount)
					throw new ConcurrentModificationException();
				m.deleteEntry(lastReturned);
				lastReturned = null;
				expectedModCount = m.modCount;
			}

		}

		final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K, V>> {
			SubMapEntryIterator(TreeMap.Entry<K, V> first,
			                    TreeMap.Entry<K, V> fence) {
				super(first, fence);
			}

			public Map.Entry<K, V> next() {
				return nextEntry();
			}

			public void remove() {
				removeAscending();
			}
		}

		final class SubMapKeyIterator extends SubMapIterator<K> {
			SubMapKeyIterator(TreeMap.Entry<K, V> first,
			                  TreeMap.Entry<K, V> fence) {
				super(first, fence);
			}

			public K next() {
				return nextEntry().key;
			}

			public void remove() {
				removeAscending();
			}
		}

		final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K, V>> {
			DescendingSubMapEntryIterator(TreeMap.Entry<K, V> last,
			                              TreeMap.Entry<K, V> fence) {
				super(last, fence);
			}

			public Map.Entry<K, V> next() {
				return prevEntry();
			}

			public void remove() {
				removeDescending();
			}
		}

		final class DescendingSubMapKeyIterator extends SubMapIterator<K> {
			DescendingSubMapKeyIterator(TreeMap.Entry<K, V> last,
			                            TreeMap.Entry<K, V> fence) {
				super(last, fence);
			}

			public K next() {
				return prevEntry().key;
			}

			public void remove() {
				removeDescending();
			}
		}
	}

	static final class AscendingSubMap<K, V> extends NavigableSubMap<K, V> {
		AscendingSubMap(TreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
			super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}

		public Comparator<? super K> comparator() {
			return m.comparator();
		}

		public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive)) throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive)) throw new IllegalArgumentException("toKey out of range");
			return new AscendingSubMap(m, false, fromKey, fromInclusive, false, toKey, toInclusive);
		}

		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive)) throw new IllegalArgumentException("toKey out of range");
			return new AscendingSubMap(m, fromStart, lo, loInclusive, false, toKey, inclusive);
		}

		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive)) throw new IllegalArgumentException("fromKey out of range");
			return new AscendingSubMap(m, false, fromKey, inclusive, toEnd, hi, hiInclusive);
		}

		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new DescendingSubMap(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive));
		}

		Iterator<K> keyIterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}

		Iterator<K> descendingKeyIterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}

		final class AscendingEntrySetView extends EntrySetView {
			public Iterator<Map.Entry<K, V>> iterator() {
				return new SubMapEntryIterator(absLowest(), absHighFence());
			}
		}

		public Set<Map.Entry<K, V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : new AscendingEntrySetView();
		}

		TreeMap.Entry<K, V> subLowest() {
			return absLowest();
		}

		TreeMap.Entry<K, V> subHighest() {
			return absHighest();
		}

		TreeMap.Entry<K, V> subCeiling(K key) {
			return absCeiling(key);
		}

		TreeMap.Entry<K, V> subHigher(K key) {
			return absHigher(key);
		}

		TreeMap.Entry<K, V> subFloor(K key) {
			return absFloor(key);
		}

		TreeMap.Entry<K, V> subLower(K key) {
			return absLower(key);
		}
	}


	static final class DescendingSubMap<K, V> extends NavigableSubMap<K, V> {
		DescendingSubMap(TreeMap<K, V> m,
		                 boolean fromStart, K lo, boolean loInclusive,
		                 boolean toEnd, K hi, boolean hiInclusive) {
			super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}

		private final Comparator<? super K> reverseComparator =
			Collections.reverseOrder(m.comparator);

		public Comparator<? super K> comparator() {
			return reverseComparator;
		}

		public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive,
		                                 K toKey, boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescendingSubMap(m,
				false, toKey, toInclusive,
				false, fromKey, fromInclusive);
		}

		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescendingSubMap(m,
				false, toKey, inclusive,
				toEnd, hi, hiInclusive);
		}

		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new DescendingSubMap(m,
				fromStart, lo, loInclusive,
				false, fromKey, inclusive);
		}

		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv :
				(descendingMapView =
					new AscendingSubMap(m,
						fromStart, lo, loInclusive,
						toEnd, hi, hiInclusive));
		}

		Iterator<K> keyIterator() {
			return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
		}

		Iterator<K> descendingKeyIterator() {
			return new SubMapKeyIterator(absLowest(), absHighFence());
		}

		final class DescendingEntrySetView extends EntrySetView {
			public Iterator<Map.Entry<K, V>> iterator() {
				return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
			}
		}

		public Set<Map.Entry<K, V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : new DescendingEntrySetView();
		}

		TreeMap.Entry<K, V> subLowest() {
			return absHighest();
		}

		TreeMap.Entry<K, V> subHighest() {
			return absLowest();
		}

		TreeMap.Entry<K, V> subCeiling(K key) {
			return absFloor(key);
		}

		TreeMap.Entry<K, V> subHigher(K key) {
			return absLower(key);
		}

		TreeMap.Entry<K, V> subFloor(K key) {
			return absCeiling(key);
		}

		TreeMap.Entry<K, V> subLower(K key) {
			return absHigher(key);
		}
	}


	private class SubMap extends AbstractMap<K, V>
		implements SortedMap<K, V>, java.io.Serializable {
		private boolean fromStart = false, toEnd = false;
		private K fromKey, toKey;

		private Object readResolve() {
			return new AscendingSubMap(TreeMap.this,
				fromStart, fromKey, true,
				toEnd, toKey, false);
		}

		public Set<Map.Entry<K, V>> entrySet() {
			throw new InternalError();
		}

		public K lastKey() {
			throw new InternalError();
		}

		public K firstKey() {
			throw new InternalError();
		}

		public SortedMap<K, V> subMap(K fromKey, K toKey) {
			throw new InternalError();
		}

		public SortedMap<K, V> headMap(K toKey) {
			throw new InternalError();
		}

		public SortedMap<K, V> tailMap(K fromKey) {
			throw new InternalError();
		}

		public Comparator<? super K> comparator() {
			throw new InternalError();
		}
	}


	private static final boolean RED = false;
	private static final boolean BLACK = true;


	static final class Entry<K, V> implements Map.Entry<K, V> {
		K key;
		V value;
		Entry<K, V> left = null;
		Entry<K, V> right = null;
		Entry<K, V> parent;
		boolean color = BLACK;


		Entry(K key, V value, Entry<K, V> parent) {
			this.key = key;
			this.value = value;
			this.parent = parent;
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
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;

			return valEquals(key, e.getKey()) && valEquals(value, e.getValue());
		}

		public int hashCode() {
			int keyHash = (key == null ? 0 : key.hashCode());
			int valueHash = (value == null ? 0 : value.hashCode());
			return keyHash ^ valueHash;
		}

		public String toString() {
			return key + "=" + value;
		}
	}

	final Entry<K, V> getFirstEntry() {
		Entry<K, V> p = root;
		if (p != null) while (p.left != null) p = p.left;
		return p;
	}

	final Entry<K, V> getLastEntry() {
		Entry<K, V> p = root;
		if (p != null) while (p.right != null) p = p.right;
		return p;
	}

	static <K, V> TreeMap.Entry<K, V> successor(Entry<K, V> t) {
		if (t == null) return null;

		if (t.right != null) {
			Entry<K, V> p = t.right;
			while (p.left != null) p = p.left;
			return p;
		} else {
			Entry<K, V> p = t.parent;
			Entry<K, V> ch = t;
			while (p != null && ch == p.right) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}

	static <K, V> Entry<K, V> predecessor(Entry<K, V> t) {
		if (t == null) return null;

		if (t.left != null) {
			Entry<K, V> p = t.left;
			while (p.right != null) p = p.right;
			return p;
		} else {
			Entry<K, V> p = t.parent;
			Entry<K, V> ch = t;
			while (p != null && ch == p.left) {
				ch = p;
				p = p.parent;
			}
			return p;
		}
	}

	private static <K, V> boolean colorOf(Entry<K, V> p) {
		return (p == null ? BLACK : p.color);
	}

	private static <K, V> Entry<K, V> parentOf(Entry<K, V> p) {
		return (p == null ? null : p.parent);
	}

	private static <K, V> void setColor(Entry<K, V> p, boolean c) {
		if (p != null)
			p.color = c;
	}

	private static <K, V> Entry<K, V> leftOf(Entry<K, V> p) {
		return (p == null) ? null : p.left;
	}

	private static <K, V> Entry<K, V> rightOf(Entry<K, V> p) {
		return (p == null) ? null : p.right;
	}

	private void rotateLeft(Entry<K, V> p) {
		if (p == null) return;
		Entry<K, V> r = p.right;
		p.right = r.left;
		if (r.left != null) r.left.parent = p;
		r.parent = p.parent;
		if (p.parent == null) root = r;
		else if (p.parent.left == p) p.parent.left = r;
		else p.parent.right = r;
		r.left = p;
		p.parent = r;
	}

	private void rotateRight(Entry<K, V> p) {
		if (p == null) return;
		Entry<K, V> l = p.left;
		p.left = l.right;
		if (l.right != null) l.right.parent = p;
		l.parent = p.parent;
		if (p.parent == null) root = l;
		else if (p.parent.right == p) p.parent.right = l;
		else p.parent.left = l;
		l.right = p;
		p.parent = l;
	}


	private void fixAfterInsertion(Entry<K, V> x) {
		x.color = RED;

		while (x != null && x != root && x.parent.color == RED) {
			if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
				Entry<K, V> y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == rightOf(parentOf(x))) {
						x = parentOf(x);
						rotateLeft(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					rotateRight(parentOf(parentOf(x)));
				}
			} else {
				Entry<K, V> y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == RED) {
					setColor(parentOf(x), BLACK);
					setColor(y, BLACK);
					setColor(parentOf(parentOf(x)), RED);
					x = parentOf(parentOf(x));
				} else {
					if (x == leftOf(parentOf(x))) {
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x), BLACK);
					setColor(parentOf(parentOf(x)), RED);
					rotateLeft(parentOf(parentOf(x)));
				}
			}
		}
		root.color = BLACK;
	}


	private void deleteEntry(Entry<K, V> p) {
		modCount++;
		size--;


		if (p.left != null && p.right != null) {
			Entry<K, V> s = successor(p);
			p.key = s.key;
			p.value = s.value;
			p = s;
		}


		Entry<K, V> replacement = (p.left != null ? p.left : p.right);

		if (replacement != null) {

			replacement.parent = p.parent;
			if (p.parent == null)
				root = replacement;
			else if (p == p.parent.left)
				p.parent.left = replacement;
			else
				p.parent.right = replacement;


			p.left = p.right = p.parent = null;


			if (p.color == BLACK)
				fixAfterDeletion(replacement);
		} else if (p.parent == null) {
			root = null;
		} else {
			if (p.color == BLACK)
				fixAfterDeletion(p);

			if (p.parent != null) {
				if (p == p.parent.left)
					p.parent.left = null;
				else if (p == p.parent.right)
					p.parent.right = null;
				p.parent = null;
			}
		}
	}


	private void fixAfterDeletion(Entry<K, V> x) {
		while (x != root && colorOf(x) == BLACK) {
			if (x == leftOf(parentOf(x))) {
				Entry<K, V> sib = rightOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}

				if (colorOf(leftOf(sib)) == BLACK &&
					colorOf(rightOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(rightOf(sib)) == BLACK) {
						setColor(leftOf(sib), BLACK);
						setColor(sib, RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(rightOf(sib), BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			} else {
				Entry<K, V> sib = leftOf(parentOf(x));

				if (colorOf(sib) == RED) {
					setColor(sib, BLACK);
					setColor(parentOf(x), RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}

				if (colorOf(rightOf(sib)) == BLACK &&
					colorOf(leftOf(sib)) == BLACK) {
					setColor(sib, RED);
					x = parentOf(x);
				} else {
					if (colorOf(leftOf(sib)) == BLACK) {
						setColor(rightOf(sib), BLACK);
						setColor(sib, RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), BLACK);
					setColor(leftOf(sib), BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}

		setColor(x, BLACK);
	}

	void readTreeSet(int size, java.io.ObjectInputStream s, V defaultVal)
		throws java.io.IOException, ClassNotFoundException {
		buildFromSorted(size, null, s, defaultVal);
	}

	void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
		try {
			buildFromSorted(set.size(), set.iterator(), null, defaultVal);
		} catch (java.io.IOException cannotHappen) {
		} catch (ClassNotFoundException cannotHappen) {
		}
	}

	private void buildFromSorted(int size, Iterator it, java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException {
		this.size = size;
		root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
	}

	private final Entry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, Iterator it, java.io.ObjectInputStream str, V defaultVal) throws java.io.IOException, ClassNotFoundException {

		if (hi < lo) return null;

		int mid = (lo + hi) >>> 1;

		Entry<K, V> left = null;
		if (lo < mid) left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);

		K key;
		V value;
		if (it != null) {
			if (defaultVal == null) {
				Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
				key = entry.getKey();
				value = entry.getValue();
			} else {
				key = (K) it.next();
				value = defaultVal;
			}
		} else {
			key = (K) str.readObject();
			value = (defaultVal != null ? defaultVal : (V) str.readObject());
		}

		Entry<K, V> middle = new Entry<>(key, value, null);

		if (level == redLevel) middle.color = RED;

		if (left != null) {
			middle.left = left;
			left.parent = middle;
		}

		if (mid < hi) {
			Entry<K, V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
			middle.right = right;
			right.parent = middle;
		}

		return middle;
	}

	private static int computeRedLevel(int sz) {
		int level = 0;
		for (int m = sz - 1; m >= 0; m = m / 2 - 1) level++;
		return level;
	}
}