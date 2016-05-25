/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

import com.jtransc.annotation.JTranscKeep;
import com.jtransc.annotation.haxe.HaxeMethodBody;
import com.jtransc.internal.JTranscSorter;

import java.lang.reflect.Array;

public class Arrays {
	native public static int binarySearch(long[] a, int fromIndex, int toIndex, long key);

	public static int binarySearch(int[] a, int fromIndex, int toIndex, int key) {
		return JTranscSorter.binarySearch(new JTranscSorter.IntArrayWrapped(a, key), 0, a.length);
	}

	native public static int binarySearch(short[] a, int fromIndex, int toIndex, short key);

	native public static int binarySearch(char[] a, int fromIndex, int toIndex, char key);

	native public static int binarySearch(byte[] a, int fromIndex, int toIndex, byte key);

	native public static int binarySearch(double[] a, int fromIndex, int toIndex, double key);

	native public static int binarySearch(float[] a, int fromIndex, int toIndex, float key);

	native public static int binarySearch(Object[] a, int fromIndex, int toIndex, Object key);

	native public static <T> int binarySearch(T[] a, int fromIndex, int toIndex, T key, Comparator<? super T> c);

	native public static void sort(int[] a, int fromIndex, int toIndex);

	native public static void sort(long[] a, int fromIndex, int toIndex);

	native public static void sort(short[] a, int fromIndex, int toIndex);

	native public static void sort(char[] a, int fromIndex, int toIndex);

	native public static void sort(byte[] a, int fromIndex, int toIndex);

	native public static void sort(float[] a, int fromIndex, int toIndex);

	native public static void sort(double[] a, int fromIndex, int toIndex);

	public static void sort(Object[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex, (Comparator<? super Object>) ComparableComparator.INSTANCE);
	}

	@HaxeMethodBody(
		"var array = cast(p0, HaxeArrayAny);\n" +
			"var start = p1;\n" +
			"var end = p2;\n" +
			"var comparator = p3;\n" +
			"var slice = array.toArray().slice(start, end);\n" +
			"haxe.ds.ArraySort.sort(slice, function(a, b) {\n" +
			"\treturn comparator.{% METHOD java.util.Comparator:compare:(Ljava/lang/Object;Ljava/lang/Object;)I %}(cast a, cast b);\n" +
			"});\n" +
			"for (n in 0 ... slice.length) {\n" +
			"\tarray.set(start + n, slice[n]);\n" +
			"}"
	)
	native public static <T> void sort(T[] a, int fromIndex, int toIndex, Comparator<? super T> c);

	native public static int deepHashCode(Object a[]);

	native public static boolean deepEquals(Object[] a1, Object[] a2);

	native static boolean deepEquals0(Object a1, Object a2);

	native public static String deepToString(Object[] a);

	public static <T> T[] copyOfRange(T[] original, int from, int to) {
		return (T[]) copyOfRange(original, from, to, (Class<Object[]>) original.getClass());
	}

	public static <T, U> T[] copyOfRange(U[] original, int from, int to, Class<Object[]> newType) {
		int length = to - from;
		if (length < 0) {
			throw new IllegalArgumentException(from + " > " + to);
		} else {
			Object[] out = (newType == Object[].class) ? new Object[length] : (Object[]) Array.newInstance(newType.getComponentType(), length);
			System.arraycopy(original, from, out, 0, Math.min(original.length - from, length));
			return (T[]) out;
		}
	}

	public static void sort(int[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(long[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(short[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(char[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(byte[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(float[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(double[] a) {
		sort(a, 0, a.length);
	}

	public static void sort(Object[] a) {
		sort(a, 0, a.length);
	}

	public static <T> void sort(T[] a, Comparator<? super T> c) {
		sort(a, 0, a.length, c);
	}

	public static void parallelSort(byte[] a) {
		sort(a);
	}

	public static void parallelSort(byte[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static void parallelSort(char[] a) {
		sort(a);
	}

	public static void parallelSort(char[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static void parallelSort(short[] a) {
		sort(a);
	}

	public static void parallelSort(short[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static void parallelSort(int[] a) {
		sort(a);
	}

	public static void parallelSort(int[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static void parallelSort(long[] a) {
		sort(a);
	}

	public static void parallelSort(long[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static void parallelSort(float[] a) {
		sort(a);
	}

	public static void parallelSort(float[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static void parallelSort(double[] a) {
		sort(a);
	}

	public static void parallelSort(double[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static <T extends Comparable<? super T>> void parallelSort(T[] a) {
		sort(a);
	}

	public static <T extends Comparable<? super T>> void parallelSort(T[] a, int fromIndex, int toIndex) {
		sort(a, fromIndex, toIndex);
	}

	public static <T> void parallelSort(T[] a, Comparator<? super T> cmp) {
		sort(a, cmp);
	}

	public static <T> void parallelSort(T[] a, int fromIndex, int toIndex, Comparator<? super T> cmp) {
		sort(a, fromIndex, toIndex, cmp);
	}

	public static int binarySearch(long[] a, long key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(int[] a, int key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(short[] a, short key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(char[] a, char key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(byte[] a, byte key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(double[] a, double key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(float[] a, float key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static int binarySearch(Object[] a, Object key) {
		return binarySearch(a, 0, a.length, key);
	}

	public static <T> int binarySearch(T[] a, T key, Comparator<? super T> c) {
		return binarySearch(a, 0, a.length, c);
	}

	public static boolean equals(long[] a, long[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(int[] a, int[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(short[] a, short a2[]) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(char[] a, char[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(byte[] a, byte[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(boolean[] a, boolean[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(double[] a, double[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(float[] a, float[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (a[n] != a2[n]) return false;
		return true;
	}

	public static boolean equals(Object[] a, Object[] a2) {
		if (a == a2) return true;
		if (a == null || a2 == null) return false;
		if (a.length != a2.length) return false;
		for (int n = 0; n < a.length; n++) if (!Objects.equals(a[n], a2[n])) return false;
		return true;
	}

	public static void fill(long[] a, long val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(long[] a, int fromIndex, int toIndex, long val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(int[] a, int val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(int[] a, int fromIndex, int toIndex, int val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(short[] a, short val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(short[] a, int fromIndex, int toIndex, short val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(char[] a, char val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(char[] a, int fromIndex, int toIndex, char val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(byte[] a, byte val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(byte[] a, int fromIndex, int toIndex, byte val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(boolean[] a, boolean val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(boolean[] a, int fromIndex, int toIndex, boolean val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(double[] a, double val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(double[] a, int fromIndex, int toIndex, double val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(float[] a, float val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(float[] a, int fromIndex, int toIndex, float val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static void fill(Object[] a, Object val) {
		fill(a, 0, a.length, val);
	}

	public static void fill(Object[] a, int fromIndex, int toIndex, Object val) {
		for (int n = fromIndex; n < toIndex; n++) a[n] = val;
	}

	public static <T> T[] copyOf(T[] original, int newLength) {
		Object[] copy = new Object[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return (T[]) copy;
	}

	public static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
		Object[] copy = new Object[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return (T[]) copy;
	}

	public static byte[] copyOf(byte[] original, int newLength) {
		byte[] copy = new byte[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static short[] copyOf(short[] original, int newLength) {
		short[] copy = new short[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static int[] copyOf(int[] original, int newLength) {
		int[] copy = new int[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static long[] copyOf(long[] original, int newLength) {
		long[] copy = new long[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static char[] copyOf(char[] original, int newLength) {
		char[] copy = new char[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static float[] copyOf(float[] original, int newLength) {
		float[] copy = new float[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static double[] copyOf(double[] original, int newLength) {
		double[] copy = new double[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	public static boolean[] copyOf(boolean[] original, int newLength) {
		boolean[] copy = new boolean[newLength];
		System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
		return copy;
	}

	static private int rangeLength(int from, int to) {
		int newLength = to - from;
		if (newLength < 0) throw new IllegalArgumentException(from + " > " + to);
		return newLength;
	}

	public static byte[] copyOfRange(byte[] original, int from, int to) {
		byte[] out = new byte[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static short[] copyOfRange(short[] original, int from, int to) {
		short[] out = new short[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static int[] copyOfRange(int[] original, int from, int to) {
		int[] out = new int[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static long[] copyOfRange(long[] original, int from, int to) {
		long[] out = new long[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static char[] copyOfRange(char[] original, int from, int to) {
		char[] out = new char[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static float[] copyOfRange(float[] original, int from, int to) {
		float[] out = new float[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static double[] copyOfRange(double[] original, int from, int to) {
		double[] out = new double[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static boolean[] copyOfRange(boolean[] original, int from, int to) {
		boolean[] out = new boolean[rangeLength(from, to)];
		for (int n = 0; n < out.length; n++) out[n] = original[from + n];
		return out;
	}

	public static <T> List<T> asList(T... array) {
		ArrayList<T> out = new ArrayList<T>(array.length);
		int length = array.length;
		for (T it : array) out.add(it);
		return out;
	}

	public static int hashCode(long a[]) {
		if (a == null) return 0;
		int result = 1;
		for (long e : a) result = 31 * result + (int) (e ^ (e >>> 32));
		return result;
	}

	public static int hashCode(int a[]) {
		if (a == null) return 0;
		int result = 1;
		for (int e : a) result = 31 * result + e;
		return result;
	}

	public static int hashCode(short a[]) {
		if (a == null) return 0;
		int result = 1;
		for (short e : a) result = 31 * result + e;
		return result;
	}

	public static int hashCode(char a[]) {
		if (a == null) return 0;
		int result = 1;
		for (char e : a) result = 31 * result + e;
		return result;
	}

	public static int hashCode(byte a[]) {
		if (a == null) return 0;
		int result = 1;
		for (byte e : a) result = 31 * result + e;
		return result;
	}

	public static int hashCode(boolean a[]) {
		if (a == null) return 0;
		int result = 1;
		for (boolean e : a) result = 31 * result + (e ? 1231 : 1237);
		return result;
	}

	public static int hashCode(float a[]) {
		if (a == null) return 0;
		int result = 1;
		for (float e : a) result = 31 * result + Float.floatToIntBits(e);
		return result;

	}

	public static int hashCode(double a[]) {
		if (a == null) return 0;
		int result = 1;
		for (double e : a) {
			long bits = Double.doubleToLongBits(e);
			result = 31 * result + (int) (bits ^ (bits >>> 32));
		}
		return result;
	}

	public static int hashCode(Object a[]) {
		if (a == null) return 0;
		int result = 1;
		for (Object element : a) result = 31 * result + (element == null ? 0 : element.hashCode());
		return result;
	}

	public static String toString(long[] a) {
		return _toString(a);
	}

	public static String toString(int[] a) {
		return _toString(a);
	}

	public static String toString(short[] a) {
		return _toString(a);
	}

	public static String toString(char[] a) {
		return _toString(a);
	}

	public static String toString(byte[] a) {
		return _toString(a);
	}

	public static String toString(boolean[] a) {
		return _toString(a);
	}

	public static String toString(float[] a) {
		return _toString(a);
	}

	public static String toString(double[] a) {
		return _toString(a);
	}

	public static String toString(Object[] a) {
		return _toString(a);
	}

	private static String _toString(Object a) {
		if (a == null) return "null";
		StringBuilder b = new StringBuilder();
		b.append('[');
		int length = Array.getLength(a);
		for (int i = 0; i < length; i++) {
			if (i >= 1) b.append(", ");
			b.append(Array.get(a, i));
		}
		return b.append(']').toString();
	}
}

class ComparableComparator<T extends Comparable<T>> implements Comparator<T> {
	static public ComparableComparator<?> INSTANCE = new ComparableComparator();

	@Override
	@JTranscKeep
	public int compare(T o1, T o2) {
		return o1.compareTo(o2);
	}
}
