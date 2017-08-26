package jtransc.java8;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

public class Java8Test2 {
	static public void main(String[] args) {
		java8Test2Main(args);
		//java8AltMetfactory(args);
	}

	static public void java8Test2Main(String[] args) {
		System.out.println("Java8Test2.main:");
		int z = 10;
		String s = "test";
		myrunnerInt(i -> (i < z));
		myrunnerInt(i -> (i < z) && Objects.equals(s, "test"));
	}

	static private void myrunnerInt(Predicate<Integer> pred) {
		System.out.println("0: " + pred.test(0));
		System.out.println("10: " + pred.test(10));
	}

	@SuppressWarnings("ComparatorCombinators")
	static private void java8AltMetfactory(String[] args) {
		System.out.println("java8AltMetfactory:");
		Comparator<String> compareByLength = (l, r) -> Integer.compare(l.length(), r.length());
		Comparator<String> compareByContentIC = (l, r) -> l.compareToIgnoreCase(r);
		Comparator<String> comparator = compareByLength.thenComparing(compareByContentIC);
		String[] strings = {"abc", "ABC", "aa", "zz"};
		for (String l : strings) {
			for (String r : strings) {
				System.out.print(comparator.compare(l, r));
				System.out.print(",");
			}
			System.out.println();
		}
	}
}
