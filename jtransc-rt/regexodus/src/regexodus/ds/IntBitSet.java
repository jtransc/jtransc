package regexodus.ds;

import java.util.Arrays;

/**
 * An implementation of BitSet (that does not technically extend BitSet due to BitSet not existing under GWT) using 32-bit
 * sections instead of the normal 64-bit (again, for GWT reasons; 64-bit integer math is slower on GWT).
 * Created by Tommy Ettinger on 3/30/2016.
 */
public class IntBitSet {
    private int[] data;
    public IntBitSet() {
        data = new int[8];
    }

    public IntBitSet(int disregard)
    {
        this();
    }

    /**
     * Constructs a CharBitSet that includes all bits between start and end, inclusive.
     * @param start inclusive
     * @param end inclusive
     */
    public IntBitSet(int start, int end) {
        data = new int[8];
        set(start, end+1);
    }
    public IntBitSet(int[] ints) {
        data = new int[8];
        System.arraycopy(ints, 0, data, 0, Math.min(8, ints.length));
    }

    public void flip(int bitIndex) {
        data[bitIndex >> 5] ^= 1 << (bitIndex & 31);
    }

    public void flip(int fromIndex, int toIndex) {
        for (int i = fromIndex; i <= toIndex; i++) {
            data[i >> 5] ^= 1 << (i & 31);
        }
    }

    public void set(int bitIndex) {
        data[bitIndex >> 5] |= 1 << (bitIndex & 31);
    }

    public void set(int bitIndex, boolean value) {
        data[bitIndex >> 5] ^= ((value ? -1 : 0) ^ data[bitIndex >> 5]) & (1 << (bitIndex & 31));
    }

    public void set(int fromIndex, int toIndex) {
        for (int i = fromIndex; i <= toIndex; i++) {
            data[i >> 5] |= 1 << (i & 31);
        }
    }

    public void set(int fromIndex, int toIndex, boolean value) {
        int val = (value ? -1 : 0);
        for (int bitIndex = fromIndex; bitIndex <= toIndex; bitIndex++) {
            data[bitIndex >> 5] ^= (val ^ data[bitIndex >> 5]) & (1 << (bitIndex & 31));
        }

    }

    public void clear(int bitIndex) {
        data[bitIndex >> 5] &= ~(1 << (bitIndex & 31));
    }

    public void clear(int fromIndex, int toIndex) {
        for (int bitIndex = fromIndex; bitIndex <= toIndex; bitIndex++) {
            data[bitIndex >> 5] &= ~(1 << (bitIndex & 31));
        }
    }

    public void clear() {
        Arrays.fill(data, 0);
    }

    public boolean get(int bitIndex) {
        return ((data[bitIndex >> 5] >>> (bitIndex & 31)) & 1) != 0;
    }

    public IntBitSet get(int fromIndex, int toIndex) {
        IntBitSet ibs = new IntBitSet();
        for (int bitIndex = fromIndex; bitIndex <= toIndex; bitIndex++) {
            ibs.set(bitIndex, get(bitIndex));
        }
        return ibs;
    }

    public int length() {
        return 32 * data.length;
    }

    public boolean isEmpty() {
        for (int i = 0; i < 8; i++) {
            if(data[i] != 0)
                return false;
        }
        return true;
    }

    public boolean intersects(IntBitSet set) {
        for (int i = 0; i < 8; i++) {
            if((data[i] & set.data[i]) != 0)
                return true;
        }
        return false;
    }

    public int cardinality() {
        int card = 0;
        for (int i = 0; i < 8; i++) {
            card += Integer.bitCount(data[i]);
        }
        return card;
    }

    public IntBitSet and(IntBitSet set) {
        for (int i = 0; i < 8; i++) {
            data[i] &= set.data[i];
        }
        return this;
    }

    public IntBitSet or(IntBitSet set) {
        for (int i = 0; i < 8; i++) {
            data[i] |= set.data[i];
        }
        return this;
    }

    public IntBitSet xor(IntBitSet set) {
        for (int i = 0; i < 8; i++) {
            data[i] ^= set.data[i];
        }
        return this;
    }

    public IntBitSet andNot(IntBitSet set) {
        for (int i = 0; i < 8; i++) {
            data[i] &= ~set.data[i];
        }
        return this;
    }
    public IntBitSet negate()
    {
        for (int i = 0; i < 8; i++) {
            data[i] = ~data[i];
        }
        return this;
    }
    public int nextSetBit(int current)
    {
        int low = 0;
        for (int i = current >>> 5; i < 8 && current < 256; i++) {
            if (current % 32 != 31)
                low = Integer.numberOfTrailingZeros(Integer.lowestOneBit(data[(current) >>> 5] >>> (current & 31)));
            if (low % 32 != 0)
                return current + low;
            current = ((current >>> 5) + 1) * 32;
        }
        return -1;
    }
    public int nextClearBit(int current)
    {
        int low = 0;
        for (int i = current >>> 5; i < 8 && current < 256; i++) {
            if (current % 32 != 31)
                low = Integer.numberOfTrailingZeros(Integer.lowestOneBit(~(data[(current) >>> 5] >>> (current & 31))));
            if (low % 32 != 0)
                return current + low;
            current = ((current >>> 5) + 1) * 32;
        }
        return -1;
    }

    public int size() {
        return 256;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntBitSet intBitSet = (IntBitSet) o;

        return Arrays.equals(data, intBitSet.data);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public IntBitSet clone() {
        return new IntBitSet(data);
    }

}
