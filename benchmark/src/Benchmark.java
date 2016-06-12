import com.jtransc.simd.MutableFloat32x4;

public class Benchmark {
	interface Task {
		int run();
	}

	static private class Test1 {
	}

	static private class Test2 {
	}

	static public void main(String[] args) {
		System.out.println("Benchmarking:");

		benchmark("plain loops", new Task() {
			@Override
			public int run() {
				int m = 0;
				for (int n = 0; n < 1000000; n++) {
					m += n;
				}
				return m;
			}
		});

		benchmark("call static mult", new Task() {
			@Override
			public int run() {
				int m = 0;
				for (int n = 0; n < 1000000; n++) {
					m += calc(m, n);
				}
				return m;
			}
		});

		benchmark("call instance mult", new Task() {
			@Override
			public int run() {
				int m = 0;
				for (int n = 0; n < 1000000; n++) {
					m += calc(m, n);
				}
				return m;
			}

			private int calc(int a, int b) {
				return (a + b) * (a + b);
			}
		});

		benchmark("call instance div", new Task() {
			@Override
			public int run() {
				int m = 1;
				for (int n = 1; n < 1000000; n++) {
					m += calc(m, n);
				}
				return m;
			}

			private int calc(int a, int b) {
				return (a - b) / (a + b);
			}
		});

		benchmark("instanceof classes", new Task() {
			@Override
			@SuppressWarnings("all")
			public int run() {
				int m = 1;
				int rand = rand(2);
				Object test1 = genObj((rand + 0) % 2);
				Object test2 = genObj((rand + 1) % 2);
				for (int n = 1; n < 1000000; n++) {
					if (test1 instanceof Test1) {
						m += n - 1;
					} else if (test1 instanceof Test2) {
						m += n + 2;
					}

					if (test2 instanceof Test1) {
						m += n - 3;
					} else if (test2 instanceof Test2) {
						m += n + 4;
					}
				}
				return m;
			}

			private int rand(int count) {
				return (int)(System.currentTimeMillis() % (long)count);
			}

			private Object genObj(int index) {
				switch (index) {
					case 0: return new Test1();
					default: return new Test2();
				}
			}
		});


		benchmark("write int[]", new Task() {
			@Override
			public int run() {
				int[] array = new int[1000000];
				for (int n = 0; n < 1000000; n++) {
					array[n] = n * 1000;
				}
				return (int) array[7];
			}
		});

		benchmark("write float[]", new Task() {
			@Override
			public int run() {
				float[] array = new float[1000000];
				for (int n = 0; n < 1000000; n++) {
					array[n] = n * 1000;
				}
				return (int) array[7];
			}
		});

		benchmark("simd", new Task() {
			@Override
			public int run() {
				MutableFloat32x4 a = new MutableFloat32x4();
				MutableFloat32x4 b = new MutableFloat32x4(2f, 3f, 4f, 5f);

				for (int n = 0; n < 1000000; n++) {
					a.setToAdd(a, b);
				}

				return (int)a.getX() + (int)a.getY() + (int)a.getZ() + (int)a.getW();
			}
		});
	}

	static private void benchmark(String name, Task run) {
		System.out.print(name + "...");

		long t1 = System.currentTimeMillis();
		for (int n = 0; n < 10; n++) run.run(); // warming up
		long t2 = System.currentTimeMillis();
		for (int n = 0; n < 10; n++) run.run();
		long t3 = System.currentTimeMillis();
		//System.out.println("( " + (t2 - t1) + " ) :: ( " + (t3 - t2) + " )");
		System.out.println(t3 - t2);
	}

	static public int calc(int a, int b) {
		return (a + b) * (a + b);
	}
}
