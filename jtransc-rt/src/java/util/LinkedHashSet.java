package java.util;

public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable, java.io.Serializable {

	public LinkedHashSet(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
	}

	public LinkedHashSet(int initialCapacity) {
		super(initialCapacity, .75f, true);
	}

	public LinkedHashSet() {
		super(16, .75f, true);
	}

	public LinkedHashSet(Collection<? extends E> c) {
		super(Math.max(2 * c.size(), 11), .75f, true);
		addAll(c);
	}
}