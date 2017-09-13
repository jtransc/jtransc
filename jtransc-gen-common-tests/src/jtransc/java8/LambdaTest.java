package jtransc.java8;

import java.util.function.Function;

public class LambdaTest {
	public int multiplier;

	public LambdaTest(int multiplier) {
		this.multiplier = multiplier;
	}

	static public void main(String[] args) {
		System.out.println("LambdaTest:");
		new LambdaTest(77).test();
	}

	private int transformer(int value, int add) {
		return multiplier * value + add;
	}

	private void test() {
		Function<Integer, String> toStringMultiplied = it -> String.valueOf(transformer(it, 33));
		System.out.println(toStringMultiplied.apply(7));
		System.out.println(toStringMultiplied.apply(-1));
	}
}
