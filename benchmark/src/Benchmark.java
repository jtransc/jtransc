import com.jtransc.simd.MutableFloat32x4;

public class Benchmark {
	interface Task {
		int run();
	}

	static public void main(String[] args) {
		System.out.println("Benchmarking:");

		benchmark("plain loops", new Task() {
			@Override
			public int run() {
				int m = 0;
				for (int n = 0; n < 10000000; n++) {
					m += n;
				}
				return m;
			}
		});

		benchmark("call static mult", new Task() {
			@Override
			public int run() {
				int m = 0;
				for (int n = 0; n < 10000000; n++) {
					m += calc(m, n);
				}
				return m;
			}
		});

		benchmark("call instance mult", new Task() {
			@Override
			public int run() {
				int m = 0;
				for (int n = 0; n < 10000000; n++) {
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
				for (int n = 1; n < 10000000; n++) {
					m += calc(m, n);
				}
				return m;
			}

			private int calc(int a, int b) {
				return (a - b) / (a + b);
			}
		});

		benchmark("write int[]", new Task() {
			@Override
			public int run() {
				int[] array = new int[10000000];
				for (int n = 0; n < 10000000; n++) {
					array[n] = n * 1000;
				}
				return (int) array[7];
			}
		});

		benchmark("write float[]", new Task() {
			@Override
			public int run() {
				float[] array = new float[10000000];
				for (int n = 0; n < 10000000; n++) {
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

				for (int n = 0; n < 10000000; n++) {
					a.setToAdd(b, b);
				}

				return (int)a.getX();
			}
		});
	}

	static private void benchmark(String name, Task run) {
		System.out.print(name + "...");

		long t1 = System.currentTimeMillis();
		run.run(); // warming up
		long t2 = System.currentTimeMillis();
		run.run();
		long t3 = System.currentTimeMillis();
		//System.out.println("( " + (t2 - t1) + " ) :: ( " + (t3 - t2) + " )");
		System.out.println(t3 - t2);
	}

	static public int calc(int a, int b) {
		return (a + b) * (a + b);
	}
}
