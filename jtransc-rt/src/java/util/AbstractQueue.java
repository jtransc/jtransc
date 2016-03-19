package java.util;

public abstract class AbstractQueue<E> extends AbstractCollection<E> implements Queue<E> {
	protected AbstractQueue() {
	}

	public boolean add(E e) {
		if (!offer(e)) throw new IllegalStateException("Queue full");
		return true;
	}

	public E remove() {
		E x = poll();
		if (x == null) throw new NoSuchElementException();
		return x;
	}

	public E element() {
		E x = peek();
		if (x == null) throw new NoSuchElementException();
		return x;
	}

	public void clear() {
		while (poll() != null) {
		}
	}

	public boolean addAll(Collection<? extends E> c) {
		Objects.requireNonNull(c);
		if (c == this) throw new IllegalArgumentException();
		boolean modified = false;
		for (E e : c) if (add(e)) modified = true;
		return modified;
	}

}
