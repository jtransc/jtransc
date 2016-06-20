package libcore.io;

import com.jtransc.JTranscBits;

import java.nio.ByteOrder;

public class Memory {

	public static int peekInt(byte[] src, int offset, ByteOrder order) {
		if (order == ByteOrder.BIG_ENDIAN) {
			return (((src[offset++] & 0xff) << 24) |
				((src[offset++] & 0xff) << 16) |
				((src[offset++] & 0xff) <<  8) |
				((src[offset  ] & 0xff) <<  0));
		} else {
			return (((src[offset++] & 0xff) <<  0) |
				((src[offset++] & 0xff) <<  8) |
				((src[offset++] & 0xff) << 16) |
				((src[offset  ] & 0xff) << 24));
		}
	}

	public static long peekLong(byte[] src, int offset, ByteOrder order) {
		if (order == ByteOrder.BIG_ENDIAN) {
			int h = ((src[offset++] & 0xff) << 24) |
				((src[offset++] & 0xff) << 16) |
				((src[offset++] & 0xff) <<  8) |
				((src[offset++] & 0xff) <<  0);
			int l = ((src[offset++] & 0xff) << 24) |
				((src[offset++] & 0xff) << 16) |
				((src[offset++] & 0xff) <<  8) |
				((src[offset  ] & 0xff) <<  0);
			return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
		} else {
			int l = ((src[offset++] & 0xff) <<  0) |
				((src[offset++] & 0xff) <<  8) |
				((src[offset++] & 0xff) << 16) |
				((src[offset++] & 0xff) << 24);
			int h = ((src[offset++] & 0xff) <<  0) |
				((src[offset++] & 0xff) <<  8) |
				((src[offset++] & 0xff) << 16) |
				((src[offset  ] & 0xff) << 24);
			return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
		}
	}

	public static short peekShort(byte[] src, int offset, ByteOrder order) {
		if (order == ByteOrder.BIG_ENDIAN) {
			return (short) ((src[offset] << 8) | (src[offset + 1] & 0xff));
		} else {
			return (short) ((src[offset + 1] << 8) | (src[offset] & 0xff));
		}
	}

	public static void pokeInt(byte[] dst, int offset, int value, ByteOrder order) {
		JTranscBits.writeInt(dst, offset, value, order == ByteOrder.LITTLE_ENDIAN);
	}

	public static void pokeLong(byte[] dst, int offset, long value, ByteOrder order) {
		JTranscBits.writeLong(dst, offset, value, order == ByteOrder.LITTLE_ENDIAN);
	}

	public static void pokeShort(byte[] dst, int offset, short value, ByteOrder order) {
		JTranscBits.writeShort(dst, offset, value, order == ByteOrder.LITTLE_ENDIAN);
	}

	public static void unsafeBulkGet(Object dst, int dstOffset, int byteCount, byte[] src, int srcOffset, int sizeofElements, boolean swap) {
		if (dst instanceof int[]) {
			unsafeBulkGet((int[])dst, dstOffset, byteCount, src, srcOffset, sizeofElements, swap);
		} else {
			throw new RuntimeException("Unhandled unsafeBulkGet dst: " + dst);
		}
	}

	static private ByteOrder NATIVE = ByteOrder.nativeOrder();
	static private ByteOrder SWAPPED = (NATIVE == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

	public static void unsafeBulkGet(int[] dst, int dstOffset, int byteCount, byte[] src, int srcOffset, int sizeofElements, boolean swap) {
		int elementCount = byteCount / 4;
		ByteOrder order = swap ? SWAPPED : NATIVE;
		for (int n = 0; n < elementCount; n++) {
			dst[dstOffset + n] = peekInt(src, srcOffset + n * sizeofElements, order);
		}
	}
}
