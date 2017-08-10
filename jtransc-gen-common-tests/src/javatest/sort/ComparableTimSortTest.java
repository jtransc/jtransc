package javatest.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ComparableTimSortTest {
	static public void main(String[] args) {
		test(new Integer[]{1, 7, 3, 5, 2, 4, 0});
		test(new Integer[]{
			1, 7, 3, 5, 2, 4, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 3, 4, 5, 6, 7, 8, 9, 0, 100, -7, 3, 4, 5, 6, 7, 8, 9, 0, 3,
			4, 5, 6, 7, 8, 9, 0, 9
		});
		test2();
	}

	static private void test(Integer[] integers) {
		System.out.printf("ComparableTimSortTest[%d]:\n", integers.length);
		System.out.println(Arrays.toString(integers));
		ComparableTimSort.sort(integers);
		System.out.println(Arrays.toString(integers));
	}

	static private void test2() {
		System.out.println("checkInstanceOfDerivated:");
		Object list = new ArrayList<>();
		System.out.println(list instanceof ArrayList<?>);
		System.out.println(list instanceof List<?>);
		System.out.println(list instanceof Collection<?>);
		System.out.println(list instanceof Iterable<?>);
	}
}
