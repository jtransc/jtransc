package java.util;

public class TreeSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, java.io.Serializable
{
	
	private transient NavigableMap<E,Object> m;


	private static final Object PRESENT = new Object();

	
	TreeSet(NavigableMap<E,Object> m) {
		this.m = m;
	}

	
	public TreeSet() {
		this(new TreeMap<E,Object>());
	}

	
	public TreeSet(Comparator<? super E> comparator) {
		this(new TreeMap<>(comparator));
	}

	
	public TreeSet(Collection<? extends E> c) {
		this();
		addAll(c);
	}

	
	public TreeSet(SortedSet<E> s) {
		this(s.comparator());
		addAll(s);
	}

	
	public Iterator<E> iterator() {
		return m.navigableKeySet().iterator();
	}

	
	public Iterator<E> descendingIterator() {
		return m.descendingKeySet().iterator();
	}

	
	public NavigableSet<E> descendingSet() {
		return new TreeSet<>(m.descendingMap());
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

	
	public boolean add(E e) {
		return m.put(e, PRESENT)==null;
	}

	
	public boolean remove(Object o) {
		return m.remove(o)==PRESENT;
	}

	
	public void clear() {
		m.clear();
	}

	
	public  boolean addAll(Collection<? extends E> c) {

		if (m.size()==0 && c.size() > 0 &&
			c instanceof SortedSet &&
			m instanceof TreeMap) {
			SortedSet<? extends E> set = (SortedSet<? extends E>) c;
			TreeMap<E,Object> map = (TreeMap<E, Object>) m;
			Comparator<? super E> cc = (Comparator<? super E>) set.comparator();
			Comparator<? super E> mc = map.comparator();
			if (cc==mc || (cc != null && cc.equals(mc))) {
				map.addAllForTreeSet(set, PRESENT);
				return true;
			}
		}
		return super.addAll(c);
	}

	
	public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
	                              E toElement,   boolean toInclusive) {
		return new TreeSet<>(m.subMap(fromElement, fromInclusive,
			toElement,   toInclusive));
	}

	
	public NavigableSet<E> headSet(E toElement, boolean inclusive) {
		return new TreeSet<>(m.headMap(toElement, inclusive));
	}

	
	public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
		return new TreeSet<>(m.tailMap(fromElement, inclusive));
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

	public Comparator<? super E> comparator() {
		return m.comparator();
	}

	
	public E first() {
		return m.firstKey();
	}

	
	public E last() {
		return m.lastKey();
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

	
	public E pollFirst() {
		Map.Entry<E,?> e = m.pollFirstEntry();
		return (e == null) ? null : e.getKey();
	}

	
	public E pollLast() {
		Map.Entry<E,?> e = m.pollLastEntry();
		return (e == null) ? null : e.getKey();
	}

	
	public Object clone() {
		TreeSet<E> clone = null;
		try {
			clone = (TreeSet<E>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}

		clone.m = new TreeMap<>(m);
		return clone;
	}
}