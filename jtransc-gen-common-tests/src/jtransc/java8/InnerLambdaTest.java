package jtransc.java8;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class InnerLambdaTest {
	public static void main(String[] args) {
		setTimeout(() -> {
			System.out.println("Test 1");
		}, 10);

		Promise promise = new Promise((resolve, reject) -> {
			setTimeout(() -> {
				System.out.println("Test 2");
			}, 100);

			setTimeout(() -> {
				resolve.apply("!!");
			}, 100);
		});
	}

	static void setTimeout(Runnable run, double millis) {
		try {
			Thread.sleep((long) millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		run.run();
	}

	static class Promise {
		public Promise(BiConsumer<Function<String, String>, Function<String, String>> exec) {
			exec.accept((str) -> {
				System.out.println("Resolved " + str);
				return "";
			}, (str) -> {
				System.out.println("Rejected " + str);
				return "";
			});
		}
	}

}
