package java.util.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class LinkedBlockingDeque<E> extends AbstractQueue<E> implements BlockingDeque<E>, java.io.Serializable {
	private LinkedList<E> queue = new LinkedList<>();

	@Override
	public Iterator<E> iterator() {
		return queue.iterator();
	}

	@NotNull
	@Override
	public Iterator<E> descendingIterator() {
		return queue.descendingIterator();
	}

	@Override
	public void push(E e) {
		queue.push(e);
	}

	@Override
	public E pop() {
		return queue.pop();
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public void put(E e) throws InterruptedException {
		queue.add(e);
	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return queue.offer(e);
	}

	@Override
	public E take() throws InterruptedException {
		return queue.remove();
	}

	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		return queue.poll();
	}

	@Override
	public int remainingCapacity() {
		return Integer.MAX_VALUE;
	}

	@Override
	public int drainTo(Collection<? super E> c) {
		return drainTo(c, Integer.MAX_VALUE);
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		int transferred = 0;
		while (queue.size() > 0 && transferred < maxElements) {
			c.add(queue.poll());
		}
		return transferred;
	}

	@Override
	public void addFirst(E e) {
		queue.addFirst(e);
	}

	@Override
	public void addLast(E e) {
		queue.addLast(e);
	}

	@Override
	public boolean offerFirst(E e) {
		return queue.offerFirst(e);
	}

	@Override
	public boolean offerLast(E e) {
		return queue.offerLast(e);
	}

	@Override
	public E removeFirst() {
		return queue.removeFirst();
	}

	@Override
	public E removeLast() {
		return queue.removeLast();
	}

	@Override
	public E pollFirst() {
		return queue.pollFirst();
	}

	@Override
	public E pollLast() {
		return queue.pollLast();
	}

	@Override
	public E getFirst() {
		return queue.getFirst();
	}

	@Override
	public E getLast() {
		return queue.getLast();
	}

	@Override
	public E peekFirst() {
		return queue.peekFirst();
	}

	@Override
	public E peekLast() {
		return queue.peekLast();
	}

	@Override
	public void putFirst(E e) throws InterruptedException {
		queue.addFirst(e);
	}

	@Override
	public void putLast(E e) throws InterruptedException {
		queue.addLast(e);
	}

	@Override
	public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return queue.offerFirst(e);
	}

	@Override
	public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
		return queue.offerLast(e);
	}

	@Override
	public E takeFirst() throws InterruptedException {
		return queue.pollFirst();
	}

	@Override
	public E takeLast() throws InterruptedException {
		return queue.pollLast();
	}

	@Override
	public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
		return queue.pollFirst();
	}

	@Override
	public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
		return queue.pollLast();
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		return queue.removeFirstOccurrence(o);
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		return queue.removeLastOccurrence(o);
	}

	@Override
	public boolean offer(E e) {
		return queue.offer(e);
	}

	@Override
	public E poll() {
		return queue.poll();
	}

	@Override
	public E peek() {
		return queue.peek();
	}

}