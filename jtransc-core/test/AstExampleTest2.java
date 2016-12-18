import java.nio.*;

public class AstExampleTest2 {
	/*
	boolean test = true;

	private boolean isLetter(char c) {
		return c >= 'a' && c <= 'z';
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	*/

	static private int demo(int a) {
		a = a + 10;
		return a;
	}

	/*
	static private int[] demo2() {
		return new int[]{100, 101, 102, 103, 104};
	}

	static private boolean isLetterOrDigit(char c) {
		return Character.isLetter(c) || Character.isDigit(c);
	}

	static private int max(int a, int b) {
		return (a > b) ? a : b;
	}

	static private int clamp(int v, int min, int max) {
		return (v < min) ? min : ((v > max) ? max : v);
	}

	static private String tryCatchTest(int a, int b) {
		try {
			if (a == b) throw new RuntimeException("equals");
			return "ok";
		} catch (RuntimeException t) {
			return "exception";
		} finally {
			System.out.println("Demo");
		}
	}
	*/
//
	//public boolean isDirectory() {
	//	//if (isInvalid()) return false;
	//	//return ((fs.getBooleanAttributes(this) & FileSystem.BA_DIRECTORY) != 0);
	//	return false;
	//}

	//public void demo() {
	//	int[] items = new int[4];
	//	int a = 10;
	//	System.out.println(a >= 3);
	//	//System.out.println(items.length >= 3);
	//	//System.out.println(items[1] != 0); // .contains("test")
	//	//System.out.println(items[2] != 0); // .contains("main")
	//}

	//public static void copy (Buffer src, Buffer dst, int numElements) {
	//	int srcPos = src.position();
	//	int dstPos = dst.position();
	//	src.limit(srcPos + numElements);
	//	final boolean srcIsByte = src instanceof ByteBuffer;
	//	final boolean dstIsByte = dst instanceof ByteBuffer;
	//	dst.limit(dst.capacity());
	//	if (srcIsByte && dstIsByte)
	//		((ByteBuffer)dst).put((ByteBuffer)src);
	//	else if ((srcIsByte || src instanceof CharBuffer) && (dstIsByte || dst instanceof CharBuffer))
	//		(dstIsByte ? ((ByteBuffer)dst).asCharBuffer() : (CharBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asCharBuffer() : (CharBuffer)src));
	//	else if ((srcIsByte || src instanceof ShortBuffer) && (dstIsByte || dst instanceof ShortBuffer))
	//		(dstIsByte ? ((ByteBuffer)dst).asShortBuffer() : (ShortBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asShortBuffer() : (ShortBuffer)src));
	//	else if ((srcIsByte || src instanceof IntBuffer) && (dstIsByte || dst instanceof IntBuffer))
	//		(dstIsByte ? ((ByteBuffer)dst).asIntBuffer() : (IntBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asIntBuffer() : (IntBuffer)src));
	//	else if ((srcIsByte || src instanceof LongBuffer) && (dstIsByte || dst instanceof LongBuffer))
	//		(dstIsByte ? ((ByteBuffer)dst).asLongBuffer() : (LongBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asLongBuffer() : (LongBuffer)src));
	//	else if ((srcIsByte || src instanceof FloatBuffer) && (dstIsByte || dst instanceof FloatBuffer))
	//		(dstIsByte ? ((ByteBuffer)dst).asFloatBuffer() : (FloatBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asFloatBuffer() : (FloatBuffer)src));
	//	else if ((srcIsByte || src instanceof DoubleBuffer) && (dstIsByte || dst instanceof DoubleBuffer))
	//		(dstIsByte ? ((ByteBuffer)dst).asDoubleBuffer() : (DoubleBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asDoubleBuffer() : (DoubleBuffer)src));
	//	else
	//		throw new RuntimeException("Buffers must be of same type or ByteBuffer");
	//	src.position(srcPos);
	//	dst.flip();
	//	dst.position(dstPos);
	//}

	//public static int mix2(int a, int b, int c) {
	//	return (a > b && a < b) ? (((b > a && b < c)) ? a * b : a * c) : b * c;
	//}

	//public static int mix2(int a, int b, int c) {
	//	return (a > b && a < b) ? (((b > a && b < c)) ? a * b : a * c) : b * c;
	//}

	//public static void mix(Buffer src, Buffer dst, boolean bb) {
	//	(bb ? ((ByteBuffer) dst).asCharBuffer() : (CharBuffer) dst).put((bb ? ((ByteBuffer) src).asCharBuffer() : (CharBuffer) src));
	//}

//	public static void copy (Buffer src, Buffer dst, int numElements) {
//		int srcPos = src.position();
//		int dstPos = dst.position();
//		src.limit(srcPos + numElements);
//		final boolean srcIsByte = src instanceof ByteBuffer;
//		final boolean dstIsByte = dst instanceof ByteBuffer;
//		dst.limit(dst.capacity());
//		if (srcIsByte && dstIsByte)
//			((ByteBuffer)dst).put((ByteBuffer)src);
//		else if ((srcIsByte || src instanceof CharBuffer) && (dstIsByte || dst instanceof CharBuffer))
//			(dstIsByte ? ((ByteBuffer)dst).asCharBuffer() : (CharBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asCharBuffer() : (CharBuffer)src));
//		//else if ((srcIsByte || src instanceof ShortBuffer) && (dstIsByte || dst instanceof ShortBuffer))
//		//	(dstIsByte ? ((ByteBuffer)dst).asShortBuffer() : (ShortBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asShortBuffer() : (ShortBuffer)src));
//		//else if ((srcIsByte || src instanceof IntBuffer) && (dstIsByte || dst instanceof IntBuffer))
//		//	(dstIsByte ? ((ByteBuffer)dst).asIntBuffer() : (IntBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asIntBuffer() : (IntBuffer)src));
//		//else if ((srcIsByte || src instanceof LongBuffer) && (dstIsByte || dst instanceof LongBuffer))
//		//	(dstIsByte ? ((ByteBuffer)dst).asLongBuffer() : (LongBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asLongBuffer() : (LongBuffer)src));
//		//else if ((srcIsByte || src instanceof FloatBuffer) && (dstIsByte || dst instanceof FloatBuffer))
//		//	(dstIsByte ? ((ByteBuffer)dst).asFloatBuffer() : (FloatBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asFloatBuffer() : (FloatBuffer)src));
//		//else if ((srcIsByte || src instanceof DoubleBuffer) && (dstIsByte || dst instanceof DoubleBuffer))
//		//	(dstIsByte ? ((ByteBuffer)dst).asDoubleBuffer() : (DoubleBuffer)dst).put((srcIsByte ? ((ByteBuffer)src).asDoubleBuffer() : (DoubleBuffer)src));
//		else
//			throw new RuntimeException("Buffers must be of same type or ByteBuffer");
//		src.position(srcPos);
//		dst.flip();
//		dst.position(dstPos);
//	}

	//public static File createTempFile(String prefix, String suffix, File directory) throws IOException {
	//	return (prefix != null) ? new File(prefix) : null;
	//	//while (true) {
	//	//	System.out.println("test");
	//	//}
	//	/*
	//	if (prefix.length() < 3) throw new IllegalArgumentException("Prefix string too short");
	//	if (suffix == null) suffix = ".tmp";
//
	//	File tmpdir = (directory != null) ? directory : null;
	//	File f;
	//	do {
	//		f = new File("demo");
	//	} while (f.isFile());
//
	//	if (!f.isFile()) throw new IOException("Unable to create temporary file");
//
	//	return f;
	//	*/
	//}

	//public static char forDigit(int digit, int radix) {
	//	if (digit >= 0 && digit <= 9) return (char) ('0' + (digit - 0));
	//	//if (digit >= 10 && digit <= 35) return (char) ('a' + (digit - 10));
	//	return '\0';
	//}

	/*
	//private void test() { int[] a = new int[] { 1, 2, 3, 4, c };}
	private void test2() {
		StringBuilder sb = new StringBuilder();
		sb.append("A");
		if (((sb.length() * 2 + 2) % 2) == 0) {
			sb.append("B");
		} else {
			sb.append("C");
		}
		sb.append("D");
	}
	*/
}
