import com.jtransc.simd.MutableFloat32x4;

public class Benchmark {
	static public void main(String[] args) {
		System.out.println("Benchmarking:");

		benchmark("plain loops", new Runnable() {
			@Override
			public void run() {
				int m = 0;
				for (int n = 0; n < 10000000; n++) {
					m += n;
				}
			}
		});

		benchmark("call static", new Runnable() {
			@Override
			public void run() {
				int m = 0;
				for (int n = 0; n < 10000000; n++) {
					m += calc(m, n);
				}
			}
		});

		benchmark("call instance", new Runnable() {
			@Override
			public void run() {
				int m = 0;
				for (int n = 0; n < 10000000; n++) {
					m += calc(m, n);
				}
			}

			private int calc(int a, int b) {
				return (a + b) * (a + b);
			}
		});

		benchmark("write int[]", new Runnable() {
			@Override
			public void run() {
				int[] array = new int[10000000];
				for (int n = 0; n < 10000000; n++) {
					array[n] = n * 1000;
				}
			}
		});

		benchmark("write float[]", new Runnable() {
			@Override
			public void run() {
				float[] array = new float[10000000];
				for (int n = 0; n < 10000000; n++) {
					array[n] = n * 1000;
				}
			}
		});

		benchmark("simd", new Runnable() {
			@Override
			public void run() {
				MutableFloat32x4 a = new MutableFloat32x4();
				MutableFloat32x4 b = new MutableFloat32x4(2f, 3f, 4f, 5f);

				for (int n = 0; n < 10000000; n++) {
					a.setToAdd(b, b);
				}
			}
		});
	}

	static private void benchmark(String name, Runnable run) {
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
