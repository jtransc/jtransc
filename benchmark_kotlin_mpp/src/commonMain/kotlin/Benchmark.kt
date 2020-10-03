import simd.*
import kotlin.jvm.JvmStatic
import kotlin.native.concurrent.ThreadLocal
import kotlin.random.Random
import kotlin.time.TimeSource
import kotlin.time.measureTime

fun main() = Benchmark.main(arrayOf())

class Benchmark {
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			Benchmark().main()
		}
	}
	fun main() {
		println(
			"""Kotlin ${KotlinVersion.CURRENT} - $kotlinTarget"""
		)
		//println("Java " + System.getProperty("java.version") + " - " + System.getProperty("java.vm.version") + " - " + System.getProperty("java.runtime.version"))
		//println("freeMemory: " + runtime.freeMemory() + ", maxMemory: " + runtime.maxMemory() + ", totalMemory: " + runtime.totalMemory())
		println("Benchmarking:")
		benchmark("plain loops", object : Task {
			override fun run(): Int {
				var m = 0
				for (n in 0..999999) {
					m += n
				}
				return m
			}
		})
		benchmark("shift left constant", object : Task {
			override fun run(): Int {
				var m = 0x12345678
				for (n in 0..999999) {
					m += m shl 1
				}
				return m
			}
		})
		benchmark("shift right constant", object : Task {
			override fun run(): Int {
				var m = 0x12345678
				for (n in 0..999999) {
					m += m shr 1
				}
				return m
			}
		})
		benchmark("shift unsigned right constant", object : Task {
			override fun run(): Int {
				var m = 0x12345678
				for (n in 0..999999) {
					m += m ushr 1
				}
				return m
			}
		})
		benchmark("shift left constant long", object : Task {
			override fun run(): Int {
				var m: Long = 0x12345678
				for (n in 0..999999) {
					m += m shl 1
				}
				return m.toInt()
			}
		})
		benchmark("shift right constant long", object : Task {
			override fun run(): Int {
				var m: Long = 0x12345678
				for (n in 0..999999) {
					m += m shr 1
				}
				return m.toInt()
			}
		})
		benchmark("shift unsigned right constant long", object : Task {
			override fun run(): Int {
				var m: Long = 0x12345678
				for (n in 0..999999) {
					m += m ushr 1
				}
				return m.toInt()
			}
		})
		benchmark("left shift", object : Task {
			override fun run(): Int {
				var m = 0x12345678
				for (n in 0..999999) {
					m += (m shl n) + (m shl -n)
				}
				return m
			}
		})
		benchmark("right shift", object : Task {
			override fun run(): Int {
				var m = 0x12345678
				for (n in 0..999999) {
					m += (m shr n) + (m shr -n)
				}
				return m
			}
		})
		benchmark("right unsigned shift", object : Task {
			override fun run(): Int {
				var m = 0x12345678
				for (n in 0..999999) {
					m += (m ushr n) + (m ushr -n)
				}
				return m
			}
		})
		benchmark("call static mult", object : Task {
			override fun run(): Int {
				var m = 0
				for (n in 0..999999) {
					m += calc(m, n)
				}
				return m
			}
		})
		benchmark("call instance mult", object : Task {
			override fun run(): Int {
				var m = 0
				for (n in 0..999999) {
					m += calc(m, n)
				}
				return m
			}

			private fun calc(a: Int, b: Int): Int {
				return (a + b) * (a + b)
			}
		})
		benchmark("call instance div", object : Task {
			override fun run(): Int {
				var m = 1
				for (n in 1..999999) {
					m += calc(m, n)
				}
				return m
			}

			private fun calc(a: Int, b: Int): Int {
				return (a - b) / (a + b)
			}
		})
		benchmark("instanceof classes", object : Task {
			override fun run(): Int {
				var m = 1
				val rand = rand(2)
				val test1 = genObj((rand + 0) % 2)
				val test2 = genObj((rand + 1) % 2)
				for (n in 1..999999) {
					if (test1 is Test1) {
						m += n - 1
					} else if (test1 is Test2) {
						m += n + 2
					}
					if (test2 is Test1) {
						m += n - 3
					} else if (test2 is Test2) {
						m += n + 4
					}
				}
				return m
			}

			private fun rand(count: Int): Int {
				return (currentTimeMillis().toLong() % count.toLong()).toInt()
			}

			private fun genObj(index: Int): Any {
				return when (index) {
					0 -> Test1()
					else -> Test2()
				}
			}
		})
		val srcI = IntArray(16 * 1024)
		val dstI = IntArray(16 * 1024)
		benchmark("arraycopy int", object : Task {
			override fun run(): Int {
				for (n in 0..1023) {
					arraycopy(srcI, 0, dstI, n, 8 * 1024)
				}
				return 0
			}
		})
		val barray = ByteArray(1000000)
		val sarray = ShortArray(1000000)
		val carray = CharArray(1000000)
		val iarray = IntArray(1000000)
		val farray = FloatArray(1000000)
		val darray = DoubleArray(1000000)
		benchmark("write byte[]", object : Task {
			override fun run(): Int {
				for (n in 0..999999) {
					barray[n] = (n * 123456711).toByte()
				}
				return barray[7].toInt()
			}
		})
		benchmark("write short[]", object : Task {
			override fun run(): Int {
				for (n in 0..999999) {
					sarray[n] = (n * 1000).toShort()
				}
				return sarray[7].toInt()
			}
		})
		benchmark("write char[]", object : Task {
			override fun run(): Int {
				for (n in 0..999999) {
					carray[n] = (n * 1000).toChar()
				}
				return carray[7].toInt()
			}
		})
		benchmark("write int[]", object : Task {
			override fun run(): Int {
				for (n in 0..999999) {
					iarray[n] = n * 1000
				}
				return iarray[7]
			}
		})
		benchmark("write float[]", object : Task {
			override fun run(): Int {
				for (n in 0..999999) {
					farray[n] = (n * 1000).toFloat()
				}
				return farray[7].toInt()
			}
		})
		benchmark("write double[]", object : Task {
			override fun run(): Int {
				for (n in 0..999999) {
					darray[n] = (n * 1000).toDouble()
				}
				return darray[7].toInt()
			}
		})
		benchmark("String Builder 1", object : Task {
			override fun run(): Int {
				val out = StringBuilder()
				for (n in 0..99999) {
					out.append(n)
				}
				return out.toString().hashCode()
			}
		})
		benchmark("String Builder 2", object : Task {
			override fun run(): Int {
				val out = StringBuilder()
				for (n in 0..99999) {
					out.append("a")
				}
				return out.toString().hashCode()
			}
		})
		benchmark("long arithmetic", object : Task {
			override fun run(): Int {
				var a: Long = 0
				for (n in 0..99999) {
					a = 17777L * n.toLong() + a / 3
				}
				return a.toInt()
			}
		})
		benchmark("simd mutable", object : Task {
			override fun run(): Int {
				val a: MutableFloat32x4 = MutableFloat32x4.create()
				val b: MutableFloat32x4 = MutableFloat32x4.create(2f, 3f, 4f, 5f)
				for (n in 0..999999) {
					a.setToAdd(a, b)
				}
				return a.x.toInt() + a.y.toInt() + a.z.toInt() + a.w.toInt()
			}
		})
		benchmark("simd immutable", object : Task {
			override fun run(): Int {
				var a: Float32x4 = Float32x4.create(0f, 0f, 0f, 0f)
				val b: Float32x4 = Float32x4.create(2f, 3f, 4f, 5f)
				for (n in 0..999999) {
					a = Float32x4.add(a, b)
				}
				return Float32x4.getX(a).toInt() + Float32x4.getY(a).toInt() + Float32x4.getZ(a).toInt() + Float32x4.getW(a).toInt()
			}
		})
		benchmark("simd mutable matrix mult", object : Task {
			override fun run(): Int {
				val a: MutableMatrixFloat32x4x4 = MutableMatrixFloat32x4x4.create()
				a.setTo(
					1f, 9f, 1f, 7f,
					3f, 2f, 4f, 5f,
					3f, 7f, 3f, 3f,
					3f, 8f, 4f, 4f
				)
				val b: MutableMatrixFloat32x4x4 = MutableMatrixFloat32x4x4.create()
				b.setTo(
					2f, 3f, 4f, 5f,
					2f, 3f, 4f, 5f,
					2f, 3f, 4f, 5f,
					2f, 3f, 4f, 5f
				)
				for (n in 0..99999) {
					a.setToMul44(a, b)
				}
				return a.sumAll.toInt()
			}
		})
		benchmark("StringBuilder1", object : Task {
			override fun run(): Int {
				val sb = StringBuilder()
				for (n in 0..99999) {
					sb.append("hello")
					sb.append('w')
					sb.append("orld")
				}
				return sb.toString().length
			}
		})
		benchmark("StringBuilder2", object : Task {
			override fun run(): Int {
				val sb = StringBuilder()
				sb.ensureCapacity(1000000)
				for (n in 0..99999) {
					sb.append("hello")
					sb.append('w')
					sb.append("orld")
				}
				return sb.toString().length
			}
		})
		/*
		benchmark("Non Direct Buffer", object : Task {
			override fun run(): Int {
				val bb = ByteBuffer.allocate(1024).order(ByteOrder.nativeOrder())
				val ib = bb.asIntBuffer()
				val fb = bb.asFloatBuffer()
				var res = 0
				for (n in 0..99999) {
					fb.put(0, n.toFloat())
					res += ib[0]
				}
				return res
			}
		})
		benchmark("Direct Buffer Int/float", object : Task {
			override fun run(): Int {
				val bb = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder())
				val ib = bb.asIntBuffer()
				val fb = bb.asFloatBuffer()
				var res = 0
				for (n in 0..99999) {
					fb.put(0, n.toFloat())
					res += ib[0]
				}
				return res
			}
		})
		benchmark("Direct Buffer Short/Char", object : Task {
			override fun run(): Int {
				val bb = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder())
				val sb = bb.asShortBuffer()
				val cb = bb.asCharBuffer()
				var res = 0
				for (n in 0..99999) {
					cb.put(0, n.toChar())
					res += sb[0].toInt()
				}
				return res
			}
		})
		benchmark("Direct Buffer Double/Long", object : Task {
			override fun run(): Int {
				val bb = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder())
				val sb = bb.asLongBuffer()
				val cb = bb.asDoubleBuffer()
				var res = 0
				for (n in 0..99999) {
					cb.put(0, n.toDouble())
					res += sb[0].toInt()
				}
				return res
			}
		})
		 */
		/*
		benchmark("FastMemory", object : Task {
			override fun run(): Int {
				val mem: FastMemory = FastMemory.alloc(1024)
				var res = 0
				for (n in 0..99999) {
					mem.setAlignedFloat32(0, n.toFloat())
					res += mem.getAlignedInt32(0)
				}
				return res
			}
		})
		*/
		benchmark("Create Instances1 local", object : Task {
			override fun run(): Int {
				var out = 0
				for (n in 0..99999) {
					val myClass = MyClass("test")
					out += myClass.b
				}
				return out
			}
		})
		gc()
		benchmark("Create Instances2 local", object : Task {
			override fun run(): Int {
				var out = 0
				val s = "test"
				for (n in 0..99999) {
					val myClass = MyClass2(s, n * out)
					out += myClass.b
				}
				return out
			}
		})
		val objects = arrayOfNulls<MyClass2>(100000)
		benchmark("Create Instances2 global", object : Task {
			override fun run(): Int {
				var out = 0
				val s = "test"
				for (n in 0..99999) {
					val v = MyClass2(s, n * out)
					objects[n] = v
					out += v.b
				}
				return out
			}
		})
		benchmark("Create Instances with builder", object : Task {
			override fun run(): Int {
				var out = 0
				for (n in 0..99999) {
					val myClass = MyClass("test$n")
					out += myClass.b + myClass.d.hashCode()
				}
				return out
			}
		})
		val hexDataChar = charArrayOf(
			0x50.toChar(),
			0x4B.toChar(),
			0x03.toChar(),
			0x04.toChar(),
			0x0A.toChar(),
			0x03.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x49.toChar(),
			0x9E.toChar(),
			0x74.toChar(),
			0x48.toChar(),
			0xA3.toChar(),
			0x1C.toChar(),
			0x29.toChar(),
			0x1C.toChar(),
			0x0C.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x0C.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x09.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x68.toChar(),
			0x65.toChar(),
			0x6C.toChar(),
			0x6C.toChar(),
			0x6F.toChar(),
			0x2E.toChar(),
			0x74.toChar(),
			0x78.toChar(),
			0x74.toChar(),
			0x48.toChar(),
			0x65.toChar(),
			0x6C.toChar(),
			0x6C.toChar(),
			0x6F.toChar(),
			0x20.toChar(),
			0x57.toChar(),
			0x6F.toChar(),
			0x72.toChar(),
			0x6C.toChar(),
			0x64.toChar(),
			0x21.toChar(),
			0x50.toChar(),
			0x4B.toChar(),
			0x03.toChar(),
			0x04.toChar(),
			0x14.toChar(),
			0x03.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x08.toChar(),
			0x00.toChar(),
			0x35.toChar(),
			0xB5.toChar(),
			0x74.toChar(),
			0x48.toChar(),
			0xAA.toChar(),
			0xC0.toChar(),
			0x69.toChar(),
			0x3A.toChar(),
			0x1D.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x38.toChar(),
			0x07.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x0A.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x68.toChar(),
			0x65.toChar(),
			0x6C.toChar(),
			0x6C.toChar(),
			0x6F.toChar(),
			0x32.toChar(),
			0x2E.toChar(),
			0x74.toChar(),
			0x78.toChar(),
			0x74.toChar(),
			0xF3.toChar(),
			0x48.toChar(),
			0xCD.toChar(),
			0xC9.toChar(),
			0xC9.toChar(),
			0x57.toChar(),
			0x08.toChar(),
			0xCF.toChar(),
			0x2F.toChar(),
			0xCA.toChar(),
			0x49.toChar(),
			0x51.toChar(),
			0x1C.toChar(),
			0x65.toChar(),
			0x8F.toChar(),
			0xB2.toChar(),
			0x47.toChar(),
			0xD9.toChar(),
			0xA3.toChar(),
			0xEC.toChar(),
			0x51.toChar(),
			0xF6.toChar(),
			0x28.toChar(),
			0x7B.toChar(),
			0x94.toChar(),
			0x8D.toChar(),
			0x9F.toChar(),
			0x0D.toChar(),
			0x00.toChar(),
			0x50.toChar(),
			0x4B.toChar(),
			0x01.toChar(),
			0x02.toChar(),
			0x3F.toChar(),
			0x03.toChar(),
			0x0A.toChar(),
			0x03.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x49.toChar(),
			0x9E.toChar(),
			0x74.toChar(),
			0x48.toChar(),
			0xA3.toChar(),
			0x1C.toChar(),
			0x29.toChar(),
			0x1C.toChar(),
			0x0C.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x0C.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x09.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x20.toChar(),
			0x80.toChar(),
			0xA4.toChar(),
			0x81.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x68.toChar(),
			0x65.toChar(),
			0x6C.toChar(),
			0x6C.toChar(),
			0x6F.toChar(),
			0x2E.toChar(),
			0x74.toChar(),
			0x78.toChar(),
			0x74.toChar(),
			0x50.toChar(),
			0x4B.toChar(),
			0x01.toChar(),
			0x02.toChar(),
			0x3F.toChar(),
			0x03.toChar(),
			0x14.toChar(),
			0x03.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x08.toChar(),
			0x00.toChar(),
			0x35.toChar(),
			0xB5.toChar(),
			0x74.toChar(),
			0x48.toChar(),
			0xAA.toChar(),
			0xC0.toChar(),
			0x69.toChar(),
			0x3A.toChar(),
			0x1D.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x38.toChar(),
			0x07.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x0A.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x20.toChar(),
			0x80.toChar(),
			0xA4.toChar(),
			0x81.toChar(),
			0x33.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x68.toChar(),
			0x65.toChar(),
			0x6C.toChar(),
			0x6C.toChar(),
			0x6F.toChar(),
			0x32.toChar(),
			0x2E.toChar(),
			0x74.toChar(),
			0x78.toChar(),
			0x74.toChar(),
			0x50.toChar(),
			0x4B.toChar(),
			0x05.toChar(),
			0x06.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x02.toChar(),
			0x00.toChar(),
			0x02.toChar(),
			0x00.toChar(),
			0x6F.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x78.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00.toChar(),
			0x00
				.toChar()
		)
		val hexData = ByteArray(hexDataChar.size)
		for (n in hexDataChar.indices) hexData[n] = hexDataChar[n].toByte()
		benchmark("Java's CRC32", object : Task {
			override fun run(): Int {
				var out = 0
				val crc32 = CRC32()
				for (n in 0..9999) {
					crc32.reset()
					crc32.update(hexData, 0, hexData.size)
					out += crc32.value.toInt()
				}
				return out
			}
		})
		benchmark("jzlib's CRC32", object : Task {
			override fun run(): Int {
				var out = 0
				val crc32 = CRC32()
				for (n in 0..9999) {
					crc32.reset()
					crc32.update(hexData, 0, hexData.size)
					out += crc32.value.toInt()
				}
				return out
			}
		})

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
		val random = Random(0L)
		val bytes = ByteArray(64 * 1024)
		for (n in bytes.indices) bytes[n] = random.nextInt().toByte()
		/*
		benchmark("compress java's Deflate", object : Task {
			override fun run(): Int {
				return try {
					val out = ByteArray(128 * 1024)
					val deflater = Deflater(9, false)
					deflater.setInput(bytes, 0, bytes.size)
					deflater.deflate(out, 0, out.size, Deflater.FULL_FLUSH)
				} catch (t: Throwable) {
					t.printStackTrace()
					0
				}
			}
		})
		benchmark("compress jzlib", object : Task {
			override fun run(): Int {
				return try {
					val out = ByteArray(128 * 1024)
					val deflater = Deflater(9, false)
					deflater.setInput(bytes, 0, bytes.size, false)
					deflater.setOutput(out, 0, out.size)
					deflater.deflate(3)
				} catch (t: Throwable) {
					t.printStackTrace()
					0
				}
			}
		})
		*/
		benchmark("random", object : Task {
			override fun run(): Int {
				val random = Random(0L)
				val len = 64 * 1024
				val bytes = ByteArray(len)
				var sum = 0
				for (n in bytes.indices) {
					bytes[n] = random.nextInt().toByte()
					sum += bytes[n]
				}
				return sum
			}
		})
		benchmark("exception", object : Task {
			override fun run(): Int {
				var m = 0
				for (n in 0..999) {
					try {
						throw Throwable()
					} catch (e: Throwable) {
						m++
					}
				}
				return m
			}
		})
		println("TOTAL time: " + totalTime)

		//try {
		//    throw new Throwable();
		//} catch (Throwable e) {
		//    e.printStackTrace();
		//}
		//new Throwable().printStackTrace();
	}

	private var totalTime = 0.0

	private fun benchmark(name: String, run: Task) {
		print("$name...")
		try {
			val t1: Double = currentTimeMillis()
			for (n in 0..9) run.run() // warming up
			gc()
			val t2: Double = currentTimeMillis().toDouble()
			for (n in 0..9) run.run()
			val t3: Double = currentTimeMillis().toDouble()
			//System.out.println("( " + (t2 - t1) + " ) :: ( " + (t3 - t2) + " )");

			//System.out.println((double)(t3 - t2) / 1000000.0);
			val elapsedTime: Double = t3 - t2
			println(elapsedTime)
			totalTime += elapsedTime
		} catch (t: Throwable) {
			t.printStackTrace()
			//System.out.println(t.getMessage());
		}
	}

	fun calc(a: Int, b: Int): Int {
		return (a + b) * (a + b)
	}

	internal interface Task {
		fun run(): Int
	}

	private class Test1
	private class Test2
	internal class MyClass(var d: String) {
		var a = 10
		var b = 20
		var c = "hello"
	}

	internal class MyClass2(var d: String, b: Int) {
		var a = 10
		var b = 20
		var c = "hello"

		init {
			this.b = b
		}
	}
}
