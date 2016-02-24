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

import java.io.ObjectStreamField;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

public class BitSet implements Cloneable, java.io.Serializable {
	private final static int ADDRESS_BITS_PER_WORD = 6;
	private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
	private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;
	private static final long WORD_MASK = 0xffffffffffffffffL;

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("bits", long[].class),
	};
	private long[] words;
	private transient int wordsInUse = 0;
	private transient boolean sizeIsSticky = false;

	private static int wordIndex(int bitIndex) {
		return bitIndex >> ADDRESS_BITS_PER_WORD;
	}

	private void checkInvariants() {
		assert (wordsInUse == 0 || words[wordsInUse - 1] != 0);
		assert (wordsInUse >= 0 && wordsInUse <= words.length);
		assert (wordsInUse == words.length || words[wordsInUse] == 0);
	}

	private void recalculateWordsInUse() {
		// Traverse the bitset until a used word is found
		int i;
		for (i = wordsInUse - 1; i >= 0; i--) if (words[i] != 0) break;
		wordsInUse = i + 1; // The new logical size
	}

	public BitSet() {
		initWords(BITS_PER_WORD);
		sizeIsSticky = false;
	}

	public BitSet(int nbits) {
		if (nbits < 0) throw new NegativeArraySizeException("nbits < 0: " + nbits);
		initWords(nbits);
		sizeIsSticky = true;
	}

	private void initWords(int nbits) {
		words = new long[wordIndex(nbits - 1) + 1];
	}

	private BitSet(long[] words) {
		this.words = words;
		this.wordsInUse = words.length;
		checkInvariants();
	}

	public static BitSet valueOf(long[] longs) {
		int n;
		for (n = longs.length; n > 0 && longs[n - 1] == 0; n--)
			;
		return new BitSet(Arrays.copyOf(longs, n));
	}

	public static BitSet valueOf(LongBuffer lb) {
		lb = lb.slice();
		int n;
		for (n = lb.remaining(); n > 0 && lb.get(n - 1) == 0; n--) ;
		long[] words = new long[n];
		lb.get(words);
		return new BitSet(words);
	}

	public static BitSet valueOf(byte[] bytes) {
		return BitSet.valueOf(ByteBuffer.wrap(bytes));
	}

	public static BitSet valueOf(ByteBuffer bb) {
		bb = bb.slice().order(ByteOrder.LITTLE_ENDIAN);
		int n;
		for (n = bb.remaining(); n > 0 && bb.get(n - 1) == 0; n--) ;
		long[] words = new long[(n + 7) / 8];
		bb.limit(n);
		int i = 0;
		while (bb.remaining() >= 8) words[i++] = bb.getLong();
		for (int remaining = bb.remaining(), j = 0; j < remaining; j++) words[i] |= (bb.get() & 0xffL) << (8 * j);
		return new BitSet(words);
	}

	public byte[] toByteArray() {
		int n = wordsInUse;
		if (n == 0) return new byte[0];
		int len = 8 * (n - 1);
		for (long x = words[n - 1]; x != 0; x >>>= 8) len++;
		byte[] bytes = new byte[len];
		ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 0; i < n - 1; i++) bb.putLong(words[i]);
		for (long x = words[n - 1]; x != 0; x >>>= 8) bb.put((byte) (x & 0xff));
		return bytes;
	}

	public long[] toLongArray() {
		return Arrays.copyOf(words, wordsInUse);
	}

	private void ensureCapacity(int wordsRequired) {
		if (words.length < wordsRequired) {
			// Allocate larger of doubled size or required size
			int request = Math.max(2 * words.length, wordsRequired);
			words = Arrays.copyOf(words, request);
			sizeIsSticky = false;
		}
	}

	private void expandTo(int wordIndex) {
		int wordsRequired = wordIndex + 1;
		if (wordsInUse < wordsRequired) {
			ensureCapacity(wordsRequired);
			wordsInUse = wordsRequired;
		}
	}

	private static void checkRange(int fromIndex, int toIndex) {
		if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
		if (toIndex < 0) throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
		if (fromIndex > toIndex)
			throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
	}

	public void flip(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);
		expandTo(wordIndex);

		words[wordIndex] ^= (1L << bitIndex);

		recalculateWordsInUse();
		checkInvariants();
	}

	public void flip(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		int startWordIndex = wordIndex(fromIndex);
		int endWordIndex = wordIndex(toIndex - 1);
		expandTo(endWordIndex);

		long firstWordMask = WORD_MASK << fromIndex;
		long lastWordMask = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex) {
			// Case 1: One word
			words[startWordIndex] ^= (firstWordMask & lastWordMask);
		} else {
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] ^= firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] ^= WORD_MASK;

			// Handle last word
			words[endWordIndex] ^= lastWordMask;
		}

		recalculateWordsInUse();
		checkInvariants();
	}

	public void set(int bitIndex) {
		if (bitIndex < 0) throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
		int wordIndex = wordIndex(bitIndex);
		expandTo(wordIndex);
		words[wordIndex] |= (1L << bitIndex); // Restores invariants
		checkInvariants();
	}

	public void set(int bitIndex, boolean value) {
		if (value) set(bitIndex);
		else clear(bitIndex);
	}

	public void set(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		// Increase capacity if necessary
		int startWordIndex = wordIndex(fromIndex);
		int endWordIndex = wordIndex(toIndex - 1);
		expandTo(endWordIndex);

		long firstWordMask = WORD_MASK << fromIndex;
		long lastWordMask = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex) {
			// Case 1: One word
			words[startWordIndex] |= (firstWordMask & lastWordMask);
		} else {
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] |= firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] = WORD_MASK;

			// Handle last word (restores invariants)
			words[endWordIndex] |= lastWordMask;
		}

		checkInvariants();
	}

	public void set(int fromIndex, int toIndex, boolean value) {
		if (value)
			set(fromIndex, toIndex);
		else
			clear(fromIndex, toIndex);
	}

	public void clear(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);
		if (wordIndex >= wordsInUse)
			return;

		words[wordIndex] &= ~(1L << bitIndex);

		recalculateWordsInUse();
		checkInvariants();
	}

	public void clear(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex) return;

		int startWordIndex = wordIndex(fromIndex);
		if (startWordIndex >= wordsInUse) return;

		int endWordIndex = wordIndex(toIndex - 1);
		if (endWordIndex >= wordsInUse) {
			toIndex = length();
			endWordIndex = wordsInUse - 1;
		}

		long firstWordMask = WORD_MASK << fromIndex;
		long lastWordMask = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex) {
			words[startWordIndex] &= ~(firstWordMask & lastWordMask);
		} else {
			words[startWordIndex] &= ~firstWordMask;
			for (int i = startWordIndex + 1; i < endWordIndex; i++) words[i] = 0;
			words[endWordIndex] &= ~lastWordMask;
		}

		recalculateWordsInUse();
		checkInvariants();
	}

	public void clear() {
		while (wordsInUse > 0) words[--wordsInUse] = 0;
	}

	public boolean get(int bitIndex) {
		if (bitIndex < 0) throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);
		checkInvariants();
		int wordIndex = wordIndex(bitIndex);
		return (wordIndex < wordsInUse) && ((words[wordIndex] & (1L << bitIndex)) != 0);
	}

	public BitSet get(int fromIndex, int toIndex) {
		checkRange(fromIndex, toIndex);
		checkInvariants();
		int len = length();
		if (len <= fromIndex || fromIndex == toIndex) return new BitSet(0);
		if (toIndex > len) toIndex = len;

		BitSet result = new BitSet(toIndex - fromIndex);
		int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
		int sourceIndex = wordIndex(fromIndex);
		boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

		for (int i = 0; i < targetWords - 1; i++, sourceIndex++) {
			result.words[i] = wordAligned ? words[sourceIndex] : (words[sourceIndex] >>> fromIndex) | (words[sourceIndex + 1] << -fromIndex);
		}

		// Process the last word
		long lastWordMask = WORD_MASK >>> -toIndex;
		result.words[targetWords - 1] =
			((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
				? /* straddles source words */
				((words[sourceIndex] >>> fromIndex) |
					(words[sourceIndex + 1] & lastWordMask) << -fromIndex)
				:
				((words[sourceIndex] & lastWordMask) >>> fromIndex);

		// Set wordsInUse correctly
		result.wordsInUse = targetWords;
		result.recalculateWordsInUse();
		result.checkInvariants();

		return result;
	}

	public int nextSetBit(int fromIndex) {
		if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

		checkInvariants();

		int u = wordIndex(fromIndex);
		if (u >= wordsInUse) return -1;

		long word = words[u] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0) return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
			if (++u == wordsInUse) return -1;
			word = words[u];
		}
	}

	public int nextClearBit(int fromIndex) {
		if (fromIndex < 0) throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
		checkInvariants();
		int u = wordIndex(fromIndex);
		if (u >= wordsInUse) return fromIndex;
		long word = ~words[u] & (WORD_MASK << fromIndex);

		while (true) {
			if (word != 0) return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
			if (++u == wordsInUse) return wordsInUse * BITS_PER_WORD;
			word = ~words[u];
		}
	}

	public int previousSetBit(int fromIndex) {
		if (fromIndex < 0) {
			if (fromIndex == -1) return -1;
			throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
		}

		checkInvariants();

		int u = wordIndex(fromIndex);
		if (u >= wordsInUse) return length() - 1;

		long word = words[u] & (WORD_MASK >>> -(fromIndex + 1));

		while (true) {
			if (word != 0) return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
			if (u-- == 0) return -1;
			word = words[u];
		}
	}

	public int previousClearBit(int fromIndex) {
		if (fromIndex < 0) {
			if (fromIndex == -1) return -1;
			throw new IndexOutOfBoundsException("fromIndex < -1: " + fromIndex);
		}

		checkInvariants();

		int u = wordIndex(fromIndex);
		if (u >= wordsInUse) return fromIndex;

		long word = ~words[u] & (WORD_MASK >>> -(fromIndex + 1));

		while (true) {
			if (word != 0) return (u + 1) * BITS_PER_WORD - 1 - Long.numberOfLeadingZeros(word);
			if (u-- == 0) return -1;
			word = ~words[u];
		}
	}

	public int length() {
		if (wordsInUse == 0) return 0;

		return BITS_PER_WORD * (wordsInUse - 1) + (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wordsInUse - 1]));
	}

	public boolean isEmpty() {
		return wordsInUse == 0;
	}

	public boolean intersects(BitSet set) {
		for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--) {
			if ((words[i] & set.words[i]) != 0) return true;
		}
		return false;
	}

	public int cardinality() {
		int sum = 0;
		for (int i = 0; i < wordsInUse; i++) sum += Long.bitCount(words[i]);
		return sum;
	}

	public void and(BitSet set) {
		if (this == set) return;

		while (wordsInUse > set.wordsInUse) words[--wordsInUse] = 0;
		for (int i = 0; i < wordsInUse; i++) words[i] &= set.words[i];
		recalculateWordsInUse();
		checkInvariants();
	}

	public void or(BitSet set) {
		if (this == set) return;

		int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

		if (wordsInUse < set.wordsInUse) {
			ensureCapacity(set.wordsInUse);
			wordsInUse = set.wordsInUse;
		}

		for (int i = 0; i < wordsInCommon; i++) words[i] |= set.words[i];

		if (wordsInCommon < set.wordsInUse) {
			System.arraycopy(set.words, wordsInCommon, words, wordsInCommon, wordsInUse - wordsInCommon);
		}

		checkInvariants();
	}

	public void xor(BitSet set) {
		int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

		if (wordsInUse < set.wordsInUse) {
			ensureCapacity(set.wordsInUse);
			wordsInUse = set.wordsInUse;
		}

		for (int i = 0; i < wordsInCommon; i++) words[i] ^= set.words[i];

		if (wordsInCommon < set.wordsInUse)
			System.arraycopy(set.words, wordsInCommon, words, wordsInCommon, set.wordsInUse - wordsInCommon);

		recalculateWordsInUse();
		checkInvariants();
	}

	public void andNot(BitSet set) {
		for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--) words[i] &= ~set.words[i];
		recalculateWordsInUse();
		checkInvariants();
	}

	public int hashCode() {
		long h = 1234;
		for (int i = wordsInUse; --i >= 0; ) h ^= words[i] * (i + 1);
		return (int) ((h >> 32) ^ h);
	}

	public int size() {
		return words.length * BITS_PER_WORD;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof BitSet)) return false;
		if (this == obj) return true;
		BitSet set = (BitSet) obj;
		checkInvariants();
		set.checkInvariants();
		if (wordsInUse != set.wordsInUse) return false;

		for (int i = 0; i < wordsInUse; i++) {
			if (words[i] != set.words[i]) return false;
		}

		return true;
	}

	public Object clone() {
		if (!sizeIsSticky) trimToSize();

		try {
			BitSet result = (BitSet) super.clone();
			result.words = words.clone();
			result.checkInvariants();
			return result;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	private void trimToSize() {
		if (wordsInUse != words.length) {
			words = Arrays.copyOf(words, wordsInUse);
			checkInvariants();
		}
	}

	public String toString() {
		checkInvariants();

		int numBits = (wordsInUse > 128) ? cardinality() : wordsInUse * BITS_PER_WORD;
		StringBuilder b = new StringBuilder(6 * numBits + 2);
		b.append('{');

		int i = nextSetBit(0);
		if (i != -1) {
			b.append(i);
			while (true) {
				if (++i < 0) break;
				if ((i = nextSetBit(i)) < 0) break;
				int endOfRun = nextClearBit(i);
				do {
					b.append(", ").append(i);
				}
				while (++i != endOfRun);
			}
		}

		b.append('}');
		return b.toString();
	}
}
