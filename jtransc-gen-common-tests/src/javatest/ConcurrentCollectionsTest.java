package javatest;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class ConcurrentCollectionsTest {
	static public void main(String[] args) {
		testConcurrentQueue("ConcurrentLinkedQueue", new ConcurrentLinkedQueue<>());
		testConcurrentQueue("ConcurrentLinkedDeque", new ConcurrentLinkedDeque<>());
		//testConcurrentSet("ConcurrentSkipListSet", new ConcurrentSkipListSet<>());
		testConcurrentMap("ConcurrentSkipListMap", new ConcurrentSkipListMap<>());
		testConcurrentMap("ConcurrentHashMap", new ConcurrentHashMap<>());
	}

	private static void testConcurrentSet(String name, Set<String> strings) {
		System.out.println(name);
		System.out.println(strings.size());
		System.out.println(strings.add("a"));
		System.out.println(strings.add("b"));
		System.out.println(strings.add("c"));
		System.out.println(strings.add("a"));
		System.out.println(strings.size());
	}

	private static void testConcurrentQueue(String name, Queue<String> strings) {
		System.out.println(name);
		System.out.println(strings.size());
		strings.add("Test");
		System.out.println(strings.size());
		System.out.println(strings.remove());
		System.out.println(strings.size());
	}

	private static void testConcurrentMap(String name, Map<Integer, String> strings) {
		System.out.println(name);
		System.out.println(strings.size());
		strings.put(10, "Test");
		System.out.println(strings.size());
		System.out.println(strings.get(10));
		System.out.println(strings.get(20));
		System.out.println(strings.size());
		System.out.println(strings.remove(10));
		System.out.println(strings.remove(20));
		System.out.println(strings.size());
	}
}
