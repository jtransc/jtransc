package javatest.utils;

import java.util.NoSuchElementException;
import java.util.Optional;

public class OptionalTest {
	static public void main(String[] args) {
		System.out.println("OptionalTest.main:");
		test(Optional.empty(), null);
		test(Optional.empty(), 1);
		test(Optional.of(1), 2);
		test(Optional.ofNullable(1), 2);
		test(Optional.ofNullable(null), 2);
		testInt(Optional.empty());
		testInt(Optional.of(7));
	}

	static private <T> void test(Optional<T> opt, T optDefault) {
		System.out.println(opt.isPresent());
		System.out.println(opt.orElse(optDefault));
		try {
			System.out.println(opt.get());
		} catch (NoSuchElementException e) {
			System.out.println(e.getMessage());
		}
	}

	static private void testInt(Optional<Integer> opt) {
		System.out.println(opt.map(it -> it * 2));
	}
}
