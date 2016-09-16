package jtransc.rt.test;

import java.util.*;

public class JTranscCollectionsTest {
	static public void main(String[] args) {
		testRandom();
		testReverseLoop();
		testReverseLoop2();
		testArrayList();
		testHashSet();
		testArrays();
		testShuffle();
	}

	static private void testRandom() {
		Random random = new Random(0L);
		for (int n = 0; n < 30; n++) System.out.print(random.nextInt(5) + ",");
		System.out.println();
	}

	static private void testReverseLoop() {
		for (int n = 10; n >= 0; n--) System.out.println(n);
	}

	static private void testReverseLoop2() {
		for (int n = 10; n >= 0; n -= 2) System.out.println(n);
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
		System.out.println(Arrays.toString(list.toArray(new Integer[0])));
		System.out.println(Arrays.toString(list.toArray(new Integer[6])));
		System.out.println(Arrays.toString(list.toArray(new Integer[10])));
		System.out.println(list.indexOf(2));
		System.out.println(list.indexOf(null));
		System.out.println(list.lastIndexOf(2));
		System.out.println(list.indexOf(99999));
		System.out.println(list.indexOf(-7));
		System.out.println(Arrays.toString(((ArrayList<Integer>)list.clone()).toArray()));

		ListIterator<Integer> iterator = list.listIterator();
		while (iterator.hasNext()) {
			Integer value = iterator.next();
			if (value != null && value == 2) iterator.remove();
		}

		System.out.println(Arrays.toString(list.toArray()));
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
		System.out.println(set.contains(s1));
		System.out.println(set.contains(s2));
		System.out.println(set.contains(a1));
		System.out.println(set.contains("b"));
		System.out.println(toSortedList(set));
		set.remove(s2);
		System.out.println(set.size());
		System.out.println(toSortedList(set));
	}

	static public <T extends Comparable<T>> List<T> toSortedList(Collection<T> c) {
		ArrayList<T> l = new ArrayList<T>(c);
		Collections.sort(l);
		return l;
	}

	static private void testArrays() {
		System.out.println("testArrays:");
		//System.out.println(new int[] {1,2,3,4,5}.hashCode());
		System.out.println(Arrays.hashCode(new int[] {1,2,3,4,5}));
		//System.out.println(new int[] {1,2,3,4,5}.hashCode());
		System.out.println(Arrays.hashCode(new long[] {1,2,3,4,5}));
		System.out.println(new int[0].getClass().getName());
	}

	static private void testShuffle() {
		System.out.println("testShuffle:");
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6);
		System.out.println(list);
		Collections.shuffle(list, new Random(0L));
		System.out.println(list);
	}
}
