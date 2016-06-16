package libcore.io;

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
		if (order == ByteOrder.BIG_ENDIAN) {
			dst[offset++] = (byte) ((value >> 24) & 0xff);
			dst[offset++] = (byte) ((value >> 16) & 0xff);
			dst[offset++] = (byte) ((value >>  8) & 0xff);
			dst[offset  ] = (byte) ((value >>  0) & 0xff);
		} else {
			dst[offset++] = (byte) ((value >>  0) & 0xff);
			dst[offset++] = (byte) ((value >>  8) & 0xff);
			dst[offset++] = (byte) ((value >> 16) & 0xff);
			dst[offset  ] = (byte) ((value >> 24) & 0xff);
		}
	}

	public static void pokeLong(byte[] dst, int offset, long value, ByteOrder order) {
		if (order == ByteOrder.BIG_ENDIAN) {
			int i = (int) (value >> 32);
			dst[offset++] = (byte) ((i >> 24) & 0xff);
			dst[offset++] = (byte) ((i >> 16) & 0xff);
			dst[offset++] = (byte) ((i >>  8) & 0xff);
			dst[offset++] = (byte) ((i >>  0) & 0xff);
			i = (int) value;
			dst[offset++] = (byte) ((i >> 24) & 0xff);
			dst[offset++] = (byte) ((i >> 16) & 0xff);
			dst[offset++] = (byte) ((i >>  8) & 0xff);
			dst[offset  ] = (byte) ((i >>  0) & 0xff);
		} else {
			int i = (int) value;
			dst[offset++] = (byte) ((i >>  0) & 0xff);
			dst[offset++] = (byte) ((i >>  8) & 0xff);
			dst[offset++] = (byte) ((i >> 16) & 0xff);
			dst[offset++] = (byte) ((i >> 24) & 0xff);
			i = (int) (value >> 32);
			dst[offset++] = (byte) ((i >>  0) & 0xff);
			dst[offset++] = (byte) ((i >>  8) & 0xff);
			dst[offset++] = (byte) ((i >> 16) & 0xff);
			dst[offset  ] = (byte) ((i >> 24) & 0xff);
		}
	}

	public static void pokeShort(byte[] dst, int offset, short value, ByteOrder order) {
		if (order == ByteOrder.BIG_ENDIAN) {
			dst[offset++] = (byte) ((value >> 8) & 0xff);
			dst[offset  ] = (byte) ((value >> 0) & 0xff);
		} else {
			dst[offset++] = (byte) ((value >> 0) & 0xff);
			dst[offset  ] = (byte) ((value >> 8) & 0xff);
		}
	}

	public static void unsafeBulkGet(Object dst, int dstOffset, int byteCount, byte[] src, int srcOffset, int sizeofElements, boolean swap) {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * Used to optimize nio heap buffer bulk put operations. 'src' must be a primitive array.
	 * 'srcOffset' is measured in units of 'sizeofElements' bytes.
	 */
	public static void unsafeBulkPut(byte[] dst, int dstOffset, int byteCount, Object src, int srcOffset, int sizeofElements, boolean swap) {
		throw new RuntimeException("Not implemented");
	}

}
