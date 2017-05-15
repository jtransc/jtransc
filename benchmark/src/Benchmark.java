import com.jtransc.compression.jzlib.*;
import com.jtransc.simd.MutableFloat32x4;
import com.jtransc.simd.Float32x4;
import com.jtransc.simd.MutableMatrixFloat32x4x4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.zip.*;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

@SuppressWarnings("Convert2Lambda")
public class Benchmark {
	interface Task {
		int run();
	}

	static private class Test1 {
	}

	static private class Test2 {
	}

	static public void main(String[] args) {
		System.out.println("JTransc " + com.jtransc.JTranscVersion.getVersion() + " - " + com.jtransc.JTranscSystem.getRuntimeKind());
		System.out.println("Java " + System.getProperty("java.version") + " - " + System.getProperty("java.vm.version") + " - " + System.getProperty("java.runtime.version"));
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

		benchmark("write byte[]", new Task() {
			@Override
			public int run() {
				byte[] array = new byte[1000000];
				for (int n = 0; n < 1000000; n++) {
					array[n] = (byte)(n * 1000);
				}
				return (int) array[7];
			}
		});

		benchmark("write short[]", new Task() {
			@Override
			public int run() {
				short[] array = new short[1000000];
				for (int n = 0; n < 1000000; n++) {
					array[n] = (short)(n * 1000);
				}
				return (int) array[7];
			}
		});

		benchmark("write char[]", new Task() {
			@Override
			public int run() {
				char[] array = new char[1000000];
				for (int n = 0; n < 1000000; n++) {
					array[n] = (char)(n * 1000);
				}
				return (int) array[7];
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

		benchmark("write double[]", new Task() {
			@Override
			public int run() {
				double[] array = new double[1000000];
				for (int n = 0; n < 1000000; n++) {
					array[n] = n * 1000;
				}
				return (int) array[7];
			}
		});

		benchmark("String Builder 1", new Task() {
			@Override
			public int run() {
				StringBuilder out = new StringBuilder();

				for (int n = 0; n < 100000; n++) {
					out.append(n);
				}

				return (int)out.toString().hashCode();
			}
		});

		benchmark("String Builder 2", new Task() {
			@Override
			public int run() {
				StringBuilder out = new StringBuilder();

				for (int n = 0; n < 100000; n++) {
					out.append("a");
				}

				return (int)out.toString().hashCode();
			}
		});

		benchmark("long arithmetic", new Task() {
			@Override
			public int run() {
				long a = 0;

				for (int n = 0; n < 10000; n++) {
					a = (17777L * (long)n) + a / 3;
				}

				return (int)a;
			}
		});

		benchmark("simd mutable", new Task() {
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

		benchmark("simd immutable", new Task() {
			@Override
			public int run() {
				Float32x4 a = Float32x4.create(0f, 0f, 0f, 0f);
				Float32x4 b = Float32x4.create(2f, 3f, 4f, 5f);

				for (int n = 0; n < 1000000; n++) {
					a = Float32x4.add(a, b);
				}

				return (int)Float32x4.getX(a) + (int)Float32x4.getY(a) + (int)Float32x4.getZ(a) + (int)Float32x4.getW(a);
			}
		});

		benchmark("simd mutable matrix mult", new Task() {
			@Override
			public int run() {
				MutableMatrixFloat32x4x4 a = new MutableMatrixFloat32x4x4().setTo(
					1f, 9f, 1f, 7f,
					3f, 2f, 4f, 5f,
					3f, 7f, 3f, 3f,
					3f, 8f, 4f, 4f
				);
				MutableMatrixFloat32x4x4 b = new MutableMatrixFloat32x4x4().setTo(
					2f, 3f, 4f, 5f,
					2f, 3f, 4f, 5f,
					2f, 3f, 4f, 5f,
					2f, 3f, 4f, 5f
				);

				for (int n = 0; n < 100000; n++) {
					a.setToMul44(a, b);
				}

				return (int) a.getSumAll();
			}
        });

		benchmark("StringBuilder1", new Task() {
			@Override
			public int run() {
				StringBuilder sb = new StringBuilder();
				for (int n = 0; n < 100000; n++) {
					sb.append("hello");
					sb.append('w');
					sb.append("orld");
				}
				return sb.toString().length();
			}
		});

		benchmark("StringBuilder2", new Task() {
			@Override
			public int run() {
				StringBuilder sb = new StringBuilder();
				sb.ensureCapacity(1000000);
				for (int n = 0; n < 100000; n++) {
					sb.append("hello");
					sb.append('w');
					sb.append("orld");
				}
				return sb.toString().length();
			}
		});

		benchmark("Create Instances1", new Task() {
			@Override
			public int run() {
				int out = 0;
				for (int n = 0; n < 100000; n++) {
					MyClass myClass = new MyClass("test");
					out += myClass.b;
				}
				return out;
			}
		});

		benchmark("Create Instances2", new Task() {
			@Override
			public int run() {
				int out = 0;
				String s = "test";
				for (int n = 0; n < 100000; n++) {
					MyClass myClass = new MyClass(s);
					out += myClass.b;
				}
				return out;
			}
		});

		benchmark("Create Instances with builder", new Task() {
			@Override
			public int run() {
				int out = 0;
				for (int n = 0; n < 100000; n++) {
					MyClass myClass = new MyClass("test" + n);
					out += myClass.b;
				}
				return out;
			}
		});

		char[] hexDataChar = new char[]{
			0x50, 0x4B, 0x03, 0x04, 0x0A, 0x03, 0x00, 0x00, 0x00, 0x00, 0x49, 0x9E, 0x74, 0x48, 0xA3, 0x1C,
			0x29, 0x1C, 0x0C, 0x00, 0x00, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x68, 0x65,
			0x6C, 0x6C, 0x6F, 0x2E, 0x74, 0x78, 0x74, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72,
			0x6C, 0x64, 0x21, 0x50, 0x4B, 0x03, 0x04, 0x14, 0x03, 0x00, 0x00, 0x08, 0x00, 0x35, 0xB5, 0x74,
			0x48, 0xAA, 0xC0, 0x69, 0x3A, 0x1D, 0x00, 0x00, 0x00, 0x38, 0x07, 0x00, 0x00, 0x0A, 0x00, 0x00,
			0x00, 0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x32, 0x2E, 0x74, 0x78, 0x74, 0xF3, 0x48, 0xCD, 0xC9, 0xC9,
			0x57, 0x08, 0xCF, 0x2F, 0xCA, 0x49, 0x51, 0x1C, 0x65, 0x8F, 0xB2, 0x47, 0xD9, 0xA3, 0xEC, 0x51,
			0xF6, 0x28, 0x7B, 0x94, 0x8D, 0x9F, 0x0D, 0x00, 0x50, 0x4B, 0x01, 0x02, 0x3F, 0x03, 0x0A, 0x03,
			0x00, 0x00, 0x00, 0x00, 0x49, 0x9E, 0x74, 0x48, 0xA3, 0x1C, 0x29, 0x1C, 0x0C, 0x00, 0x00, 0x00,
			0x0C, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x80,
			0xA4, 0x81, 0x00, 0x00, 0x00, 0x00, 0x68, 0x65, 0x6C, 0x6C, 0x6F, 0x2E, 0x74, 0x78, 0x74, 0x50,
			0x4B, 0x01, 0x02, 0x3F, 0x03, 0x14, 0x03, 0x00, 0x00, 0x08, 0x00, 0x35, 0xB5, 0x74, 0x48, 0xAA,
			0xC0, 0x69, 0x3A, 0x1D, 0x00, 0x00, 0x00, 0x38, 0x07, 0x00, 0x00, 0x0A, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x20, 0x80, 0xA4, 0x81, 0x33, 0x00, 0x00, 0x00, 0x68, 0x65, 0x6C,
			0x6C, 0x6F, 0x32, 0x2E, 0x74, 0x78, 0x74, 0x50, 0x4B, 0x05, 0x06, 0x00, 0x00, 0x00, 0x00, 0x02,
			0x00, 0x02, 0x00, 0x6F, 0x00, 0x00, 0x00, 0x78, 0x00, 0x00, 0x00, 0x00, 0x00
		};
		byte[] hexData = new byte[hexDataChar.length];
		for (int n = 0; n < hexDataChar.length; n++) hexData[n] = (byte) hexDataChar[n];

		benchmark("Java's CRC32", new Task() {
			@Override
			public int run() {
				int out = 0;
				CRC32 crc32 = new CRC32();
				for (int n = 0; n < 10000; n++) {
					crc32.reset();
					crc32.update(hexData, 0, hexData.length);
					out += crc32.getValue();
				}
				return out;
			}
		});

		benchmark("jzlib's CRC32", new Task() {
			@Override
			public int run() {
				int out = 0;
				com.jtransc.compression.jzlib.CRC32 crc32 = new com.jtransc.compression.jzlib.CRC32();
				for (int n = 0; n < 10000; n++) {
					crc32.reset();
					crc32.update(hexData, 0, hexData.length);
					out += crc32.getValue();
				}
				return out;
			}
		});

		//benchmark("decompress zlib", new Task() {
		//	@Override
		//	public int run() {
		//
		//		//new ZipFile("memory://hex")
		//		//com.jtransc.compression.JTranscZlib.inflate(hexData, 1848);
		//		return -1;
		//		//try {
		//		//	InflaterInputStream is = new InflaterInputStream(new ByteArrayInputStream(hexData));
		//		//	ByteArrayOutputStream out = new ByteArrayOutputStream();
		//		//	com.jtransc.io.JTranscIoTools.copy(is, out);
		//		//	return (int) out.size();
		//		//} catch (Throwable t) {
		//		//	t.printStackTrace();
		//		//	return 0;
		//		//}
		//	}
		//});

		benchmark("compress java's Deflate", new Task() {
			@Override
			public int run() {
				try {
					Random random = new Random(0L);
					byte[] bytes = new byte[64 * 1024];
					byte[] out = new byte[128 * 1024];
					for (int n = 0; n < bytes.length; n++) bytes[n] = (byte) random.nextInt();

					Deflater deflater = new Deflater(9, false);
					deflater.setInput(bytes, 0, bytes.length);
					int result = deflater.deflate(out, 0, out.length, Deflater.FULL_FLUSH);
					return result;
				} catch (Throwable t) {
					t.printStackTrace();
					return 0;
				}
			}
		});

		benchmark("compress jzlib", new Task() {
			@Override
			public int run() {
				try {
					Random random = new Random(0L);
					byte[] bytes = new byte[64 * 1024];
					byte[] out = new byte[128 * 1024];
					for (int n = 0; n < bytes.length; n++) bytes[n] = (byte) random.nextInt();

					com.jtransc.compression.jzlib.Deflater deflater = new com.jtransc.compression.jzlib.Deflater(9, false);
					deflater.setInput(bytes, 0, bytes.length, false);
					deflater.setOutput(out, 0, out.length);
					int result = deflater.deflate(3);
					return result;
				} catch (Throwable t) {
					t.printStackTrace();
					return 0;
				}
			}
		});
	}

	static private void benchmark(String name, Task run) {
		System.out.print(name + "...");

		try {
			long t1 = System.nanoTime();
			for (int n = 0; n < 10; n++) run.run(); // warming up
			long t2 = System.nanoTime();
			for (int n = 0; n < 10; n++) run.run();
			long t3 = System.nanoTime();
			//System.out.println("( " + (t2 - t1) + " ) :: ( " + (t3 - t2) + " )");

			//System.out.println((double)(t3 - t2) / 1000000.0);
			System.out.println((double)(t3 - t2) / 1000000.0);
		} catch (Throwable t) {
			System.out.println(t.getMessage());
		}

	}

	static public int calc(int a, int b) {
		return (a + b) * (a + b);
	}

	static class MyClass {
		public int a = 10;
		public int b = 20;
		public String c = "hello";
		public String d;

		public MyClass(String d) {
			this.d = d;
		}
	}
}
