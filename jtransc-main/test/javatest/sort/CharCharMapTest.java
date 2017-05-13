package javatest.sort;


import com.jtransc.io.JTranscConsole;

import java.util.Arrays;

public class CharCharMapTest {
	static public void main(String[] args) {
		System.out.println("CharCharMapTest.main:");
		CharCharMap charMap = new CharCharMap("ABC".toCharArray(), "abc".toCharArray());
		System.out.println(charMap.get('A'));
		System.out.println(charMap.get('B'));
		System.out.println(charMap.get('C'));
		System.out.println((int)charMap.get('x'));
	}
}

class CharCharMap implements java.io.Serializable, Cloneable {
	private static final boolean ASSERTS = false;
	/**
	 * The array of keys.
	 */
	protected transient char[] key;
	/**
	 * The array of values.
	 */
	protected transient char[] value;
	/**
	 * The mask for wrapping a position counter.
	 */
	protected transient int mask;
	/**
	 * Whether this set contains the key zero.
	 */
	protected transient boolean containsNullKey;
	/**
	 * The current table size.
	 */
	protected transient int n;
	/**
	 * Threshold after which we rehash. It must be the table size times {@link #f}.
	 */
	protected transient int maxFill;
	/**
	 * Number of entries in the set (including the key zero, if present).
	 */
	protected int size;
	/**
	 * The acceptable load factor.
	 */
	protected final float f;
	/**
	 * Cached set of keys.
	 */
	protected transient volatile KeySet keys;

	/**
	 * The default return value for <code>get()</code>, <code>put()</code> and <code>remove()</code>.
	 */
	protected char defRetValue;

	/**
	 * The initial default size of a hash table.
	 */
	static final public int DEFAULT_INITIAL_SIZE = 16;
	/**
	 * The default load factor of a hash table.
	 */
	static final public float DEFAULT_LOAD_FACTOR = .75f;

	/**
	 * Creates a new hash map.
	 * <br>The actual table size will be the least power of two greater than <code>expected</code>/<code>f</code>.
	 *
	 * @param expected the expected number of elements in the hash set.
	 * @param f        the load factor.
	 */

	public CharCharMap(final int expected, final float f) {
		//System.out.println("[z1]");
		if (f <= 0 || f > 1)
			throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than or equal to 1");
		//System.out.println("[z2]");
		if (expected < 0) throw new IllegalArgumentException("The expected number of elements must be nonnegative");
		//System.out.println("[z3]");
		this.f = f;
		n = arraySize(expected, f);
		//System.out.println("[z4]");
		mask = n - 1;
		maxFill = maxFill(n, f);
		key = new char[n + 1];
		value = new char[n + 1];
		//System.out.println("[z5]");
	}

	/**
	 * Creates a new hash map with 0.75f as load factor.
	 *
	 * @param expected the expected number of elements in the hash map.
	 */
	public CharCharMap(final int expected) {
		this(expected, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map with initial expected 16 entries and 0.75f as load factor.
	 */
	public CharCharMap() {
		this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
	}

	/**
	 * Creates a new hash map using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @param f the load factor.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
	public CharCharMap(final char[] k, final char[] v, final float f) {
		this(k.length, f);
		//System.out.println("[za]");
		if (k.length != v.length)
			throw new IllegalArgumentException("The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
		//System.out.println("[zb]");
		JTranscConsole.log(k.length);
		for (int i = 0; i < k.length; i++) {
			JTranscConsole.log(i);
			//System.out.println(":" + (int) k[i]);
			//System.out.println(":" + (int) v[i]);
			this.put(k[i], v[i]);
		}
		//System.out.println("[zc]");
	}

	/**
	 * Creates a new hash map with 0.75f as load factor using the elements of two parallel arrays.
	 *
	 * @param k the array of keys of the new hash map.
	 * @param v the array of corresponding values in the new hash map.
	 * @throws IllegalArgumentException if <code>k</code> and <code>v</code> have different lengths.
	 */
	public CharCharMap(final char[] k, final char[] v) {
		this(k, v, DEFAULT_LOAD_FACTOR);
	}

	public void defaultReturnValue(final char rv) {
		defRetValue = rv;
	}

	public char defaultReturnValue() {
		return defRetValue;
	}

	private int realSize() {
		return containsNullKey ? size - 1 : size;
	}

	private void ensureCapacity(final int capacity) {
		final int needed = arraySize(capacity, f);
		if (needed > n) rehash(needed);
	}

	private void tryCapacity(final long capacity) {
		final int needed = (int) Math.min(1 << 30, Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(capacity / f))));
		if (needed > n) rehash(needed);
	}

	private char removeEntry(final int pos) {
		final char oldValue = value[pos];
		size--;
		shiftKeys(pos);
		if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
		return oldValue;
	}

	private char removeNullEntry() {
		containsNullKey = false;
		final char oldValue = value[n];
		size--;
		if (size < maxFill / 4 && n > DEFAULT_INITIAL_SIZE) rehash(n / 2);
		return oldValue;
	}

	private int insert(final char k, final char v) {
		int pos;
		//System.out.println("[a]");
		if (((k) == ((char) 0))) {
			//System.out.println("[aa]");
			if (containsNullKey) return n;
			containsNullKey = true;
			pos = n;
		} else {
			//System.out.println("[ba]");
			char curr;
			final char[] key = this.key;
			//System.out.println("[bb]");
			// The starting point.
			if (!((curr = key[pos = (HashCommon.mix((k))) & mask]) == ((char) 0))) {
				//System.out.println("[bc]");
				if (((curr) == (k))) return pos;
				//System.out.println("[bd]");
				//System.out.println("curr:" + curr);
				//System.out.println("pos:" + pos);
				//System.out.println("mask:" + mask);
				while (!((curr = key[pos = (pos + 1) & mask]) == ((char) 0))) {
					//System.out.println("[b0]");
					//System.out.println((int)key[pos]);
					//System.out.println("key:" + new String(key));
					//System.out.println("key:" + Arrays.asList((char[])key));
					//System.out.println("curr:" + curr);
					//System.out.println("pos:" + pos);
					//System.out.println("mask:" + mask);
					if (((curr) == (k))) return pos;
				}
				//System.out.println("[be]");
			}
		}
		//System.out.println("[ca]");
		key[pos] = k;
		value[pos] = v;
		//System.out.println("[cb]");
		if (size++ >= maxFill) rehash(arraySize(size + 1, f));
		//System.out.println("[cc]");
		return -1;
	}

	public char put(final char k, final char v) {
		//System.out.println("-1");
		final int pos = insert(k, v);
		//System.out.println("-2");
		if (pos < 0) return defRetValue;
		//System.out.println("-3");
		final char oldValue = value[pos];
		//System.out.println("-4");
		value[pos] = v;
		//System.out.println("-5");
		return oldValue;
	}

	/**
	 * Shifts left entries with the specified hash code, starting at the specified position, and empties the resulting free entry.
	 *
	 * @param pos a starting position.
	 */
	protected final void shiftKeys(int pos) {
		// Shift entries with the same hash.
		int last, slot;
		char curr;
		final char[] key = this.key;
		for (; ; ) {
			pos = ((last = pos) + 1) & mask;
			for (; ; ) {
				if (((curr = key[pos]) == ((char) 0))) {
					key[last] = ((char) 0);
					return;
				}
				slot = (HashCommon.mix((curr))) & mask;
				if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
				pos = (pos + 1) & mask;
			}
			key[last] = curr;
			value[last] = value[pos];
		}
	}

	public char remove(final char k) {
		if (((k) == ((char) 0))) {
			if (containsNullKey) return removeNullEntry();
			return defRetValue;
		}
		char curr;
		final char[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (HashCommon.mix((k))) & mask]) == ((char) 0))) return defRetValue;
		if (((k) == (curr))) return removeEntry(pos);
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == ((char) 0))) return defRetValue;
			if (((k) == (curr))) return removeEntry(pos);
		}
	}

	public char get(final char k) {
		if (((k) == ((char) 0))) return containsNullKey ? value[n] : defRetValue;
		char curr;
		final char[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (HashCommon.mix((k))) & mask]) == ((char) 0))) return defRetValue;
		if (((k) == (curr))) return value[pos];
		// There's always an unused entry.
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == ((char) 0))) return defRetValue;
			if (((k) == (curr))) return value[pos];
		}
	}

	public boolean containsKey(final char k) {
		if (((k) == ((char) 0))) return containsNullKey;
		char curr;
		final char[] key = this.key;
		int pos;
		// The starting point.
		if (((curr = key[pos = (HashCommon.mix((k))) & mask]) == ((char) 0))) return false;
		if (((k) == (curr))) return true;
		// There's always an unused entry.
		while (true) {
			if (((curr = key[pos = (pos + 1) & mask]) == ((char) 0))) return false;
			if (((k) == (curr))) return true;
		}
	}

	public boolean containsValue(final char v) {
		final char value[] = this.value;
		final char key[] = this.key;
		if (containsNullKey && ((value[n]) == (v))) return true;
		for (int i = n; i-- != 0; )
			if (!((key[i]) == ((char) 0)) && ((value[i]) == (v))) return true;
		return false;
	}

	/* Removes all elements from this map.
	 *
     * <br>To increase object reuse, this method does not change the table size. If you want to reduce the table size, you must use {@link #trim()}. */
	public void clear() {
		if (size == 0) return;
		size = 0;
		containsNullKey = false;
		Arrays.fill(key, ((char) 0));
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * A no-op for backward compatibility.
	 *
	 * @param growthFactor unused.
	 * @deprecated Since <code>fastutil</code> 6.1.0, hash tables are doubled when they are too full.
	 */
	@Deprecated
	public void growthFactor(int growthFactor) {
	}

	/**
	 * Gets the growth factor (2).
	 *
	 * @return the growth factor of this set, which is fixed (2).
	 * @see #growthFactor(int)
	 * @deprecated Since <code>fastutil</code> 6.1.0, hash tables are doubled when they are too full.
	 */
	@Deprecated
	public int growthFactor() {
		return 16;
	}

	private final class KeySet {

		public int size() {
			return size;
		}

		public boolean contains(char k) {
			return containsKey(k);
		}

		public boolean remove(char k) {
			final int oldSize = size;
			CharCharMap.this.remove(k);
			return size != oldSize;
		}

		public void clear() {
			CharCharMap.this.clear();
		}

		/**
		 * Delegates to the corresponding type-specific method.
		 */
		public boolean remove(final Object o) {
			return remove(((((Character) (o)).charValue())));
		}
	}

	public KeySet keySet() {
		if (keys == null) keys = new KeySet();
		return keys;
	}

	/**
	 * Rehashes the map, making the table as small as possible.
	 * <br>This method rehashes the table to the smallest size satisfying the load factor. It can be used when the set will not be changed anymore, so to optimize access speed and size.
	 * <br>If the table size is already the minimum possible, this method does nothing.
	 *
	 * @return true if there was enough memory to trim the map.
	 * @see #trim(int)
	 */
	public boolean trim() {
		final int l = arraySize(size, f);
		if (l >= n || size > maxFill(l, f)) return true;
		try {
			rehash(l);
		} catch (Error cantDoIt) {
			return false;
		}
		return true;
	}

	/**
	 * Rehashes this map if the table is too large.
	 * <br>Let <var>N</var> be the smallest table size that can hold <code>max(n,{@link #size()})</code> entries, still satisfying the load factor. If the current table size is smaller than or equal to
	 * <var>N</var>, this method does nothing. Otherwise, it rehashes this map in a table of size <var>N</var>.
	 * <br>This method is useful when reusing maps. {@linkplain #clear() Clearing a map} leaves the table size untouched. If you are reusing a map many times, you can call this method with a typical
	 * size to avoid keeping around a very large table just because of a few large transient maps.
	 *
	 * @param n the threshold for the trimming.
	 * @return true if there was enough memory to trim the map.
	 * @see #trim()
	 */
	public boolean trim(final int n) {
		final int l = HashCommon.nextPowerOfTwo((int) Math.ceil(n / f));
		if (l >= n || size > maxFill(l, f)) return true;
		try {
			rehash(l);
		} catch (Error cantDoIt) {
			return false;
		}
		return true;
	}

	/**
	 * Rehashes the map.
	 * <br>This method implements the basic rehashing strategy, and may be overriden by subclasses implementing different rehashing strategies (e.g., disk-based rehashing). However, you should not
	 * override this method unless you understand the internal workings of this class.
	 *
	 * @param newN the new size
	 */

	protected void rehash(final int newN) {
		final char key[] = this.key;
		final char value[] = this.value;
		final int mask = newN - 1; // Note that this is used by the hashing macro
		final char newKey[] = new char[newN + 1];
		final char newValue[] = new char[newN + 1];
		int i = n, pos;
		for (int j = realSize(); j-- != 0; ) {
			while (((key[--i]) == ((char) 0))) ;
			if (!((newKey[pos = (HashCommon.mix((key[i]))) & mask]) == ((char) 0)))
				while (!((newKey[pos = (pos + 1) & mask]) == ((char) 0))) ;
			newKey[pos] = key[i];
			newValue[pos] = value[i];
		}
		newValue[newN] = value[n];
		n = newN;
		this.mask = mask;
		maxFill = maxFill(n, f);
		this.key = newKey;
		this.value = newValue;
	}

	/**
	 * Returns a deep copy of this map.
	 * <br>
	 * This method performs a deep copy of this hash map, but with primitive keys and values it doesn't matter much.
	 *
	 * @return a deep copy of this map.
	 */

	public CharCharMap clone() {
		char[] k = new char[key.length], v = new char[value.length];
		System.arraycopy(key, 0, k, 0, key.length);
		System.arraycopy(value, 0, v, 0, value.length);
		return new CharCharMap(k, v, f);
	}

	/**
	 * Returns a hash code for this map.
	 * <br>
	 * This method overrides the generic method provided by the superclass. Since <code>equals()</code> is not overriden, it is important that the value returned by this method is the same value as
	 * the one returned by the overriden method.
	 *
	 * @return a hash code for this map.
	 */
	public int hashCode() {
		int h = 0;
		for (int j = realSize(), i = 0, t = 0; j-- != 0; ) {
			while (((key[i]) == ((char) 0)))
				i++;
			t = (key[i]);
			t ^= (value[i]);
			h += t;
			i++;
		}
		// Zero / null keys have hash zero.
		if (containsNullKey) h += (value[n]);
		return h;
	}

	/**
	 * Returns the maximum number of entries that can be filled before rehashing.
	 *
	 * @param n the size of the backing array.
	 * @param f the load factor.
	 * @return the maximum number of entries before rehashing.
	 */
	public static int maxFill(final int n, final float f) {
        /* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
		return Math.min((int) Math.ceil(n * f), n - 1);
	}

	/**
	 * Returns the maximum number of entries that can be filled before rehashing.
	 *
	 * @param n the size of the backing array.
	 * @param f the load factor.
	 * @return the maximum number of entries before rehashing.
	 */
	public static long maxFill(final long n, final float f) {
		/* We must guarantee that there is always at least
		 * one free entry (even with pathological load factors). */
		return Math.min((long) Math.ceil(n * f), n - 1);
	}

	/**
	 * Returns the least power of two smaller than or equal to 2<sup>30</sup> and larger than or equal to <code>Math.ceil( expected / f )</code>.
	 *
	 * @param expected the expected number of elements in a hash table.
	 * @param f        the load factor.
	 * @return the minimum possible size for a backing array.
	 * @throws IllegalArgumentException if the necessary size is larger than 2<sup>30</sup>.
	 */
	public static int arraySize(final int expected, final float f) {
		final long s = Math.max(2, HashCommon.nextPowerOfTwo((long) Math.ceil(expected / f)));
		if (s > (1 << 30))
			throw new IllegalArgumentException("Too large (" + expected + " expected elements with load factor " + f + ")");
		return (int) s;
	}

	private static class HashCommon {

		private HashCommon() {
		}

		;

		/**
		 * This reference is used to fill keys and values of removed entries (if
		 * they are objects). <code>null</code> cannot be used as it would confuse the
		 * search algorithm in the presence of an actual <code>null</code> key.
		 */
		public static final Object REMOVED = new Object();

		/**
		 * 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
		 */
		private static final int INT_PHI = 0x9E3779B9;
		/**
		 * The reciprocal of {@link #INT_PHI} modulo 2<sup>32</sup>.
		 */
		private static final int INV_INT_PHI = 0x144cbc89;
		/**
		 * 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
		 */
		private static final long LONG_PHI = 0x9E3779B97F4A7C15L;
		/**
		 * The reciprocal of {@link #LONG_PHI} modulo 2<sup>64</sup>.
		 */
		private static final long INV_LONG_PHI = 0xf1de83e19937733dL;

		/**
		 * Avalanches the bits of an integer by applying the finalisation step of MurmurHash3.
		 * <br>
		 * <br>This method implements the finalisation step of Austin Appleby's <a href="http://code.google.com/p/smhasher/">MurmurHash3</a>.
		 * Its purpose is to avalanche the bits of the argument to within 0.25% bias.
		 *
		 * @param x an integer.
		 * @return a hash value with good avalanching properties.
		 */
		public final static int murmurHash3(int x) {
			x ^= x >>> 16;
			x *= 0x85ebca6b;
			x ^= x >>> 13;
			x *= 0xc2b2ae35;
			x ^= x >>> 16;
			return x;
		}


		/**
		 * Avalanches the bits of a long integer by applying the finalisation step of MurmurHash3.
		 * <br>
		 * <br>This method implements the finalisation step of Austin Appleby's <a href="http://code.google.com/p/smhasher/">MurmurHash3</a>.
		 * Its purpose is to avalanche the bits of the argument to within 0.25% bias.
		 *
		 * @param x a long integer.
		 * @return a hash value with good avalanching properties.
		 */
		public final static long murmurHash3(long x) {
			x ^= x >>> 33;
			x *= 0xff51afd7ed558ccdL;
			x ^= x >>> 33;
			x *= 0xc4ceb9fe1a85ec53L;
			x ^= x >>> 33;
			return x;
		}

		/**
		 * Quickly mixes the bits of an integer.
		 * <br>This method mixes the bits of the argument by multiplying by the golden ratio and
		 * xorshifting the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
		 * it has slightly worse behaviour than {@link #murmurHash3(int)} (in open-addressing hash tables the average number of probes
		 * is slightly larger), but it's much faster.
		 *
		 * @param x an integer.
		 * @return a hash value obtained by mixing the bits of {@code x}.
		 * @see #invMix(int)
		 */
		public final static int mix(final int x) {
			final int h = x * INT_PHI;
			return h ^ (h >>> 16);
		}

		/**
		 * The inverse of {@link #mix(int)}. This method is mainly useful to create unit tests.
		 *
		 * @param x an integer.
		 * @return a value that passed through {@link #mix(int)} would give {@code x}.
		 */
		public final static int invMix(final int x) {
			return (x ^ x >>> 16) * INV_INT_PHI;
		}

		/**
		 * Quickly mixes the bits of a long integer.
		 * <br>This method mixes the bits of the argument by multiplying by the golden ratio and
		 * xorshifting twice the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
		 * it has slightly worse behaviour than {@link #murmurHash3(long)} (in open-addressing hash tables the average number of probes
		 * is slightly larger), but it's much faster.
		 *
		 * @param x a long integer.
		 * @return a hash value obtained by mixing the bits of {@code x}.
		 */
		public final static long mix(final long x) {
			long h = x * LONG_PHI;
			h ^= h >>> 32;
			return h ^ (h >>> 16);
		}

		/**
		 * The inverse of {@link #mix(long)}. This method is mainly useful to create unit tests.
		 *
		 * @param x a long integer.
		 * @return a value that passed through {@link #mix(long)} would give {@code x}.
		 */
		public final static long invMix(long x) {
			x ^= x >>> 32;
			x ^= x >>> 16;
			return (x ^ x >>> 32) * INV_LONG_PHI;
		}


		/**
		 * Returns the hash code that would be returned by {@link Float#hashCode()}.
		 *
		 * @param f a float.
		 * @return the same code as {@link Float#hashCode() new Float(f).hashCode()}.
		 */

		final public static int float2int(final float f) {
			return Float.floatToIntBits(f);
		}

		/**
		 * Returns the hash code that would be returned by {@link Double#hashCode()}.
		 *
		 * @param d a double.
		 * @return the same code as {@link Double#hashCode() new Double(f).hashCode()}.
		 */

		final public static int double2int(final double d) {
			final long l = Double.doubleToLongBits(d);
			return (int) (l ^ (l >>> 32));
		}

		/**
		 * Returns the hash code that would be returned by {@link Long#hashCode()}.
		 *
		 * @param l a long.
		 * @return the same code as {@link Long#hashCode() new Long(f).hashCode()}.
		 */
		final public static int long2int(final long l) {
			return (int) (l ^ (l >>> 32));
		}

		/**
		 * Return the least power of two greater than or equal to the specified value.
		 * <br>Note that this function will return 1 when the argument is 0.
		 *
		 * @param x an integer smaller than or equal to 2<sup>30</sup>.
		 * @return the least power of two greater than or equal to the specified value.
		 */
		public static int nextPowerOfTwo(int x) {
			if (x == 0) return 1;
			x--;
			x |= x >> 1;
			x |= x >> 2;
			x |= x >> 4;
			x |= x >> 8;
			return (x | x >> 16) + 1;
		}

		/**
		 * Return the least power of two greater than or equal to the specified value.
		 * <br>Note that this function will return 1 when the argument is 0.
		 *
		 * @param x a long integer smaller than or equal to 2<sup>62</sup>.
		 * @return the least power of two greater than or equal to the specified value.
		 */
		public static long nextPowerOfTwo(long x) {
			if (x == 0) return 1;
			x--;
			x |= x >> 1;
			x |= x >> 2;
			x |= x >> 4;
			x |= x >> 8;
			x |= x >> 16;
			return (x | x >> 32) + 1;
		}

		/**
		 * Returns the least power of two larger than or equal to <code>Math.ceil( expected / f )</code>.
		 *
		 * @param expected the expected number of elements in a hash table.
		 * @param f        the load factor.
		 * @return the minimum possible size for a backing big array.
		 */
		public static long bigArraySize(final long expected, final float f) {
			return nextPowerOfTwo((long) Math.ceil(expected / f));
		}
	}
}

