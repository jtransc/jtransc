package libcore.io;

import com.jtransc.JTranscBits;
import com.jtransc.annotation.JTranscMethodBody;

import java.nio.ByteOrder;

public class Memory {
	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static char peekAlignedCharLE(byte[] src, int offset) {
		return peekCharLE(src, offset * 2);
	}

	//@JTranscMethodBody(target = "cpp", value = "return ((int16_t*)GET_OBJECT(JA_B, p0)->_data)[p1];")
	public static short peekAlignedShortLE(byte[] src, int offset) {
		return peekShortLE(src, offset * 2);
	}

	//@JTranscMethodBody(target = "cpp", value = "return ((int32_t*)GET_OBJECT(JA_B, p0)->_data)[p1];")
	public static int peekAlignedIntLE(byte[] src, int offset) {
		return peekIntLE(src, offset * 4);
	}

	//@JTranscMethodBody(target = "cpp", value = "return ((int64_t*)GET_OBJECT(JA_B, p0)->_data)[p1];")
	public static long peekAlignedLongLE(byte[] src, int offset) {
		return peekLongLE(src, offset * 8);
	}

	//@JTranscMethodBody(target = "cpp", value = "return ((float32_t*)GET_OBJECT(JA_B, p0)->_data)[p1];")
	public static float peekAlignedFloatLE(byte[] src, int offset) {
		return peekFloatLE(src, offset * 4);
	}

	//@JTranscMethodBody(target = "cpp", value = "return ((float64_t*)GET_OBJECT(JA_B, p0)->_data)[p1];")
	public static double peekAlignedDoubleLE(byte[] src, int offset) {
		return peekDoubleLE(src, offset * 8);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static char peekAlignedCharBE(byte[] src, int offset) {
		return Character.reverseBytes(peekAlignedCharLE(src, offset));
	}

	public static short peekAlignedShortBE(byte[] src, int offset) {
		return Short.reverseBytes(peekAlignedShortLE(src, offset));
	}

	public static int peekAlignedIntBE(byte[] src, int offset) {
		return Integer.reverseBytes(peekAlignedIntLE(src, offset));
	}

	public static long peekAlignedLongBE(byte[] src, int offset) {
		return Long.reverseBytes(peekAlignedLongLE(src, offset));
	}

	public static float peekAlignedFloatBE(byte[] src, int offset) {
		return Float.intBitsToFloat(peekAlignedIntBE(src, offset));
	}

	public static double peekAlignedDoubleBE(byte[] src, int offset) {
		return Double.longBitsToDouble(peekAlignedLongBE(src, offset));
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static char peekCharLE(byte[] src, int offset) {
		return (char) ((src[offset + 1] << 8) | (src[offset] & 0xff));
	}

	public static short peekShortLE(byte[] src, int offset) {
		return (short) ((src[offset + 1] << 8) | (src[offset] & 0xff));
	}

	public static int peekIntLE(byte[] src, int offset) {
		return (((src[offset++] & 0xff) << 0) |
			((src[offset++] & 0xff) << 8) |
			((src[offset++] & 0xff) << 16) |
			((src[offset] & 0xff) << 24));
	}

	public static long peekLongLE(byte[] src, int offset) {
		int l = ((src[offset++] & 0xff) << 0) |
			((src[offset++] & 0xff) << 8) |
			((src[offset++] & 0xff) << 16) |
			((src[offset++] & 0xff) << 24);
		int h = ((src[offset++] & 0xff) << 0) |
			((src[offset++] & 0xff) << 8) |
			((src[offset++] & 0xff) << 16) |
			((src[offset] & 0xff) << 24);
		return (((long) h) << 32L) | ((long) l) & 0xffffffffL;
	}

	public static float peekFloatLE(byte[] src, int offset) {
		return Float.intBitsToFloat(peekIntLE(src, offset));
	}

	public static double peekDoubleLE(byte[] src, int offset) {
		return Double.longBitsToDouble(peekLongLE(src, offset));
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static short peekShortBE(byte[] src, int offset) {
		return Short.reverseBytes(peekShortLE(src, offset));
	}

	public static int peekIntBE(byte[] src, int offset) {
		return Integer.reverseBytes(peekIntLE(src, offset));
	}

	public static long peekLongBE(byte[] src, int offset) {
		return Long.reverseBytes(peekLongLE(src, offset));
	}

	public static float peekFloatBE(byte[] src, int offset) {
		return Float.intBitsToFloat(peekIntBE(src, offset));
	}

	public static double peekDoubleBE(byte[] src, int offset) {
		return Double.longBitsToDouble(peekLongBE(src, offset));
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static short peekShort(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekShortLE(src, offset);
		} else {
			return peekShortBE(src, offset);
		}
	}

	public static int peekInt(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekIntLE(src, offset);
		} else {
			return peekIntBE(src, offset);
		}
	}

	public static long peekLong(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekLongLE(src, offset);
		} else {
			return peekLongBE(src, offset);
		}
	}

	public static float peekFloat(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekFloatLE(src, offset);
		} else {
			return peekFloatBE(src, offset);
		}
	}

	public static double peekDouble(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekDoubleLE(src, offset);
		} else {
			return peekDoubleBE(src, offset);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static char peekAlignedChar(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekAlignedCharLE(src, offset);
		} else {
			return peekAlignedCharBE(src, offset);
		}
	}

	public static short peekAlignedShort(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekAlignedShortLE(src, offset);
		} else {
			return peekAlignedShortBE(src, offset);
		}
	}

	public static int peekAlignedInt(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekAlignedIntLE(src, offset);
		} else {
			return peekAlignedIntBE(src, offset);
		}
	}

	public static long peekAlignedLong(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekAlignedLongLE(src, offset);
		} else {
			return peekAlignedLongBE(src, offset);
		}
	}

	public static float peekAlignedFloat(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekAlignedFloatLE(src, offset);
		} else {
			return peekAlignedFloatBE(src, offset);
		}
	}

	public static double peekAlignedDouble(byte[] src, int offset, boolean isLittleEndian) {
		if (isLittleEndian) {
			return peekAlignedDoubleLE(src, offset);
		} else {
			return peekAlignedDoubleBE(src, offset);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////

	public static void pokeShort(byte[] dst, int offset, short value, boolean isLittleEndian) {
		JTranscBits.writeShort(dst, offset, value, isLittleEndian);
	}

	public static void pokeChar(byte[] dst, int offset, char value, boolean isLittleEndian) {
		JTranscBits.writeChar(dst, offset, value, isLittleEndian);
	}

	public static void pokeInt(byte[] dst, int offset, int value, boolean isLittleEndian) {
		JTranscBits.writeInt(dst, offset, value, isLittleEndian);
	}

	public static void pokeLong(byte[] dst, int offset, long value, boolean isLittleEndian) {
		JTranscBits.writeLong(dst, offset, value, isLittleEndian);
	}

	public static void pokeFloat(byte[] dst, int offset, float value, boolean isLittleEndian) {
		JTranscBits.writeFloat(dst, offset, value, isLittleEndian);
	}

	public static void pokeDouble(byte[] dst, int offset, double value, boolean isLittleEndian) {
		JTranscBits.writeDouble(dst, offset, value, isLittleEndian);
	}


	///////////////////////////

	public static void pokeAlignedShort(byte[] dst, int offset, short value, boolean isLittleEndian) {
		JTranscBits.writeShort(dst, offset * 2, value, isLittleEndian);
	}

	public static void pokeAlignedChar(byte[] dst, int offset, char value, boolean isLittleEndian) {
		JTranscBits.writeChar(dst, offset * 2, value, isLittleEndian);
	}

	public static void pokeAlignedInt(byte[] dst, int offset, int value, boolean isLittleEndian) {
		JTranscBits.writeInt(dst, offset * 4, value, isLittleEndian);
	}

	public static void pokeAlignedLong(byte[] dst, int offset, long value, boolean isLittleEndian) {
		JTranscBits.writeLong(dst, offset * 8, value, isLittleEndian);
	}

	public static void pokeAlignedFloat(byte[] dst, int offset, float value, boolean isLittleEndian) {
		JTranscBits.writeFloat(dst, offset * 4, value, isLittleEndian);
	}

	public static void pokeAlignedDouble(byte[] dst, int offset, double value, boolean isLittleEndian) {
		JTranscBits.writeDouble(dst, offset * 8, value, isLittleEndian);
	}

	///////////////////////////

	public static void pokeAlignedShortLE(byte[] dst, int offset, short value) {
		JTranscBits.writeShortLE(dst, offset * 2, value);
	}

	public static void pokeAlignedCharLE(byte[] dst, int offset, char value) {
		JTranscBits.writeCharLE(dst, offset * 2, value);
	}

	public static void pokeAlignedIntLE(byte[] dst, int offset, int value) {
		JTranscBits.writeIntLE(dst, offset * 4, value);
	}

	public static void pokeAlignedLongLE(byte[] dst, int offset, long value) {
		JTranscBits.writeLongLE(dst, offset * 8, value);
	}

	public static void pokeAlignedFloatLE(byte[] dst, int offset, float value) {
		JTranscBits.writeFloatLE(dst, offset * 4, value);
	}

	public static void pokeAlignedDoubleLE(byte[] dst, int offset, double value) {
		JTranscBits.writeDoubleLE(dst, offset * 8, value);
	}

	///////////////////////////

	public static void pokeAlignedShortBE(byte[] dst, int offset, short value) {
		JTranscBits.writeShortBE(dst, offset * 2, value);
	}

	public static void pokeAlignedCharBE(byte[] dst, int offset, char value) {
		JTranscBits.writeCharBE(dst, offset * 2, value);
	}

	public static void pokeAlignedIntBE(byte[] dst, int offset, int value) {
		JTranscBits.writeIntBE(dst, offset * 4, value);
	}

	public static void pokeAlignedLongBE(byte[] dst, int offset, long value) {
		JTranscBits.writeLongBE(dst, offset * 8, value);
	}

	public static void pokeAlignedFloatBE(byte[] dst, int offset, float value) {
		JTranscBits.writeFloatBE(dst, offset * 4, value);
	}

	public static void pokeAlignedDoubleBE(byte[] dst, int offset, double value) {
		JTranscBits.writeDoubleBE(dst, offset * 8, value);
	}

	///////////////////////////

	public static void unsafeBulkGet(Object dst, int dstOffset, int byteCount, byte[] src, int srcOffset, int sizeofElements, boolean swap) {
		if (dst instanceof int[]) {
			unsafeBulkGet((int[]) dst, dstOffset, byteCount, src, srcOffset, sizeofElements, swap);
		} else {
			throw new RuntimeException("Unhandled unsafeBulkGet dst: " + dst);
		}
	}

	static private ByteOrder NATIVE = ByteOrder.nativeOrder();
	static private ByteOrder SWAPPED = (NATIVE == ByteOrder.LITTLE_ENDIAN) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;

	public static void unsafeBulkGet(int[] dst, int dstOffset, int byteCount, byte[] src, int srcOffset, int sizeofElements, boolean swap) {
		int elementCount = byteCount / 4;
		ByteOrder order = swap ? SWAPPED : NATIVE;
		boolean isLittleEndian = order == ByteOrder.LITTLE_ENDIAN;
		for (int n = 0; n < elementCount; n++) {
			dst[dstOffset + n] = peekInt(src, srcOffset + n * sizeofElements, isLittleEndian);
		}
	}
}
