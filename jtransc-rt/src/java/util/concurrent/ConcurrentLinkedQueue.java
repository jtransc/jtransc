/*
 * Written by Doug Lea and Martin Buchholz with assistance from members of
 * JCP JSR-166 Expert Group and released to the public domain, as explained
 * at http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;

import java.util.*;

public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E>, java.io.Serializable {
	private static class Node<E> {
		volatile E item;
		volatile Node<E> next;

		Node(E item) {
			set(item);
		}

		void set(E item) {
			this.item = item;
		}

		synchronized boolean casItem(E cmp, E val) {
			if (this.item == cmp) {
				this.item = val;
				return true;
			} else {
				return false;
			}
		}

		synchronized void lazySetNext(Node<E> val) {
			this.next = val;
		}

		boolean casNext(Node<E> cmp, Node<E> val) {
			if (this.next == cmp) {
				this.next = val;
				return true;
			} else {
				return false;
			}
		}
	}

	private transient volatile Node<E> head;
	private transient volatile Node<E> tail;

	public ConcurrentLinkedQueue() {
		head = tail = new Node<E>(null);
	}

	public ConcurrentLinkedQueue(Collection<? extends E> c) {
		Node<E> h = null, t = null;
		for (E e : c) {
			checkNotNull(e);
			Node<E> newNode = new Node<E>(e);
			if (h == null)
				h = t = newNode;
			else {
				t.lazySetNext(newNode);
				t = newNode;
			}
		}
		if (h == null)
			h = t = new Node<E>(null);
		head = h;
		tail = t;
	}

	public boolean add(E e) {
		return offer(e);
	}

	final void updateHead(Node<E> h, Node<E> p) {
		if (h != p && casHead(h, p)) h.lazySetNext(h);
	}

	final Node<E> succ(Node<E> p) {
		Node<E> next = p.next;
		return (p == next) ? head : next;
	}

	public boolean offer(E e) {
		checkNotNull(e);
		final Node<E> newNode = new Node<E>(e);

		for (Node<E> t = tail, p = t; ; ) {
			Node<E> q = p.next;
			if (q == null) {
				if (p.casNext(null, newNode)) {
					if (p != t) casTail(t, newNode);
					return true;
				}
			} else if (p == q) {
				p = (t != (t = tail)) ? t : head;
			} else {
				p = (p != t && t != (t = tail)) ? t : q;
			}
		}
	}

	public E poll() {
		restartFromHead:
		for (; ; ) {
			for (Node<E> h = head, p = h, q; ; ) {
				E item = p.item;

				if (item != null && p.casItem(item, null)) {
					if (p != h) updateHead(h, ((q = p.next) != null) ? q : p);
					return item;
				} else if ((q = p.next) == null) {
					updateHead(h, p);
					return null;
				} else if (p == q) {
					continue restartFromHead;
				} else {
					p = q;
				}
			}
		}
	}

	public E peek() {
		restartFromHead:
		for (; ; ) {
			for (Node<E> h = head, p = h, q; ; ) {
				E item = p.item;
				if (item != null || (q = p.next) == null) {
					updateHead(h, p);
					return item;
				} else if (p == q)
					continue restartFromHead;
				else
					p = q;
			}
		}
	}

	Node<E> first() {
		restartFromHead:
		for (; ; ) {
			for (Node<E> h = head, p = h, q; ; ) {
				boolean hasItem = (p.item != null);
				if (hasItem || (q = p.next) == null) {
					updateHead(h, p);
					return hasItem ? p : null;
				} else if (p == q)
					continue restartFromHead;
				else
					p = q;
			}
		}
	}

	public boolean isEmpty() {
		return first() == null;
	}

	public int size() {
		int count = 0;
		for (Node<E> p = first(); p != null; p = succ(p))
			if (p.item != null)
				// Collection.size() spec says to max out
				if (++count == Integer.MAX_VALUE)
					break;
		return count;
	}

	public boolean contains(Object o) {
		if (o == null) return false;
		for (Node<E> p = first(); p != null; p = succ(p)) {
			E item = p.item;
			if (item != null && o.equals(item))
				return true;
		}
		return false;
	}

	public boolean remove(Object o) {
		if (o == null) return false;
		Node<E> pred = null;
		for (Node<E> p = first(); p != null; p = succ(p)) {
			E item = p.item;
			if (item != null &&
				o.equals(item) &&
				p.casItem(item, null)) {
				Node<E> next = succ(p);
				if (pred != null && next != null)
					pred.casNext(p, next);
				return true;
			}
			pred = p;
		}
		return false;
	}

	public boolean addAll(Collection<? extends E> c) {
		if (c == this) throw new IllegalArgumentException();

		Node<E> beginningOfTheEnd = null, last = null;
		for (E e : c) {
			checkNotNull(e);
			Node<E> newNode = new Node<E>(e);
			if (beginningOfTheEnd == null) {
				beginningOfTheEnd = last = newNode;
			} else {
				last.lazySetNext(newNode);
				last = newNode;
			}
		}
		if (beginningOfTheEnd == null) return false;

		for (Node<E> t = tail, p = t; ; ) {
			Node<E> q = p.next;
			if (q == null) {
				if (p.casNext(null, beginningOfTheEnd)) {
					if (!casTail(t, last)) {
						t = tail;
						if (last.next == null) casTail(t, last);
					}
					return true;
				}
			} else if (p == q) {
				p = (t != (t = tail)) ? t : head;
			} else {
				p = (p != t && t != (t = tail)) ? t : q;
			}
		}
	}

	public Object[] toArray() {
		// Use ArrayList to deal with resizing.
		ArrayList<E> al = new ArrayList<E>();
		for (Node<E> p = first(); p != null; p = succ(p)) {
			E item = p.item;
			if (item != null) al.add(item);
		}
		return al.toArray();
	}

	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		// try to use sent-in array
		int k = 0;
		Node<E> p;
		for (p = first(); p != null && k < a.length; p = succ(p)) {
			E item = p.item;
			if (item != null)
				a[k++] = (T) item;
		}
		if (p == null) {
			if (k < a.length)
				a[k] = null;
			return a;
		}

		// If won't fit, use ArrayList version
		ArrayList<E> al = new ArrayList<E>();
		for (Node<E> q = first(); q != null; q = succ(q)) {
			E item = q.item;
			if (item != null)
				al.add(item);
		}
		return al.toArray(a);
	}

	public Iterator<E> iterator() {
		return new Itr();
	}

	private class Itr implements Iterator<E> {
		private Node<E> nextNode;
		private E nextItem;
		private Node<E> lastRet;

		Itr() {
			advance();
		}

		private E advance() {
			lastRet = nextNode;
			E x = nextItem;

			Node<E> pred, p;
			if (nextNode == null) {
				p = first();
				pred = null;
			} else {
				pred = nextNode;
				p = succ(nextNode);
			}

			for (; ; ) {
				if (p == null) {
					nextNode = null;
					nextItem = null;
					return x;
				}
				E item = p.item;
				if (item != null) {
					nextNode = p;
					nextItem = item;
					return x;
				} else {
					// skip over nulls
					Node<E> next = succ(p);
					if (pred != null && next != null)
						pred.casNext(p, next);
					p = next;
				}
			}
		}

		public boolean hasNext() {
			return nextNode != null;
		}

		public E next() {
			if (nextNode == null) throw new NoSuchElementException();
			return advance();
		}

		public void remove() {
			Node<E> l = lastRet;
			if (l == null) throw new IllegalStateException();
			// rely on a future traversal to relink.
			l.item = null;
			lastRet = null;
		}
	}

	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		for (Node<E> p = first(); p != null; p = succ(p)) {
			Object item = p.item;
			if (item != null) s.writeObject(item);
		}
		s.writeObject(null);
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();

		Node<E> h = null, t = null;
		Object item;
		while ((item = s.readObject()) != null) {
			@SuppressWarnings("unchecked")
			Node<E> newNode = new Node<E>((E) item);
			if (h == null) {
				h = t = newNode;
			} else {
				t.lazySetNext(newNode);
				t = newNode;
			}
		}
		if (h == null) h = t = new Node<E>(null);
		head = h;
		tail = t;
	}

	private static void checkNotNull(Object v) {
		if (v == null) throw new NullPointerException();
	}

	synchronized private boolean casTail(Node<E> cmp, Node<E> val) {
		if (this.tail == cmp) {
			this.tail = val;
			return true;
		} else {
			return false;
		}
	}

	synchronized private boolean casHead(Node<E> cmp, Node<E> val) {
		if (this.head == cmp) {
			this.head = val;
			return true;
		} else {
			return false;
		}
	}
}
