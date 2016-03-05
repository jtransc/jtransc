package jtransc.rt.test;

import java.util.*;

public class JTranscCollectionsTest {
	static public void main(String[] args) {
		testArrayList();
		testHashSet();
	}

	static public void testArrayList() {
		System.out.println("ArrayList:");
		ArrayList<Integer> list = new ArrayList<>();
		list.add(1);
		System.out.println(list.size());
		list.add(2);
		list.add(3);
		list.add(99999);
		list.add(null);
		list.add(2);
		System.out.println(list.size());
		System.out.println(Arrays.toString(list.toArray()));
		System.out.println(list.indexOf(2));
		System.out.println(list.indexOf(null));
		System.out.println(list.lastIndexOf(2));
		System.out.println(list.indexOf(99999));
		System.out.println(list.indexOf(-7));
	}

	static public void testHashSet() {
		System.out.println("HashSet:");
		HashSet<String> set = new HashSet<>();
		String s1 = new String("s");
		String s2 = new String("s");
		String a1 = new String("a");
		set.add(s1);
		set.add(s2);
		set.add(a1);
		System.out.println(set.size());
		System.out.println(toSortedList(set));
		set.remove(s2);
		System.out.println(set.size());
		System.out.println(toSortedList(set));
	}

	static public <T extends Comparable<T>> List<T> toSortedList(Collection<T> c) {
		ArrayList<T> l = new ArrayList<T>(c);
		l.sort(null);
		Collections.sort(l);
		return l;
	}
}
