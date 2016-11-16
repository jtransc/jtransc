package java.awt.image;

@SuppressWarnings("WeakerAccess")
public abstract class DataBuffer {
	public static final int TYPE_BYTE = 0;
	public static final int TYPE_USHORT = 1;
	public static final int TYPE_SHORT = 2;
	public static final int TYPE_INT = 3;
	public static final int TYPE_FLOAT = 4;
	public static final int TYPE_DOUBLE = 5;
	public static final int TYPE_UNDEFINED = 32;
	protected int dataType;
	protected int banks;
	protected int offset;
	protected int size;
	protected int offsets[];

	public static int getDataTypeSize(int type) {
		switch (type) {
			case TYPE_BYTE:
				return 8;
			case TYPE_USHORT:
				return 16;
			case TYPE_SHORT:
				return 16;
			case TYPE_INT:
				return 32;
			case TYPE_FLOAT:
				return 32;
			case TYPE_DOUBLE:
				return 64;
		}
		return -1;
	}

	protected DataBuffer(int dataType, int size) {
	}

	protected DataBuffer(int dataType, int size, int numBanks) {
	}

	protected DataBuffer(int dataType, int size, int numBanks, int offset) {
	}

	protected DataBuffer(int dataType, int size, int numBanks, int offsets[]) {
	}

	public int getDataType() {
		return dataType;
	}

	public int getSize() {
		return size;
	}

	public int getOffset() {
		return offset;
	}

	public int[] getOffsets() {
		return offsets;
	}

	public int getNumBanks() {
		return banks;
	}

	public int getElem(int i) {
		return getElem(0, i);
	}

	public abstract int getElem(int bank, int i);

	public void setElem(int i, int val) {
		setElem(0, i, val);
	}

	public abstract void setElem(int bank, int i, int val);

	public float getElemFloat(int i) {
		return (float) getElem(i);
	}

	public float getElemFloat(int bank, int i) {
		return (float) getElem(bank, i);
	}

	public void setElemFloat(int i, float val) {
		setElem(i, (int) val);
	}

	public void setElemFloat(int bank, int i, float val) {
		setElem(bank, i, (int) val);
	}

	public double getElemDouble(int i) {
		return (double) getElem(i);
	}

	public double getElemDouble(int bank, int i) {
		return (double) getElem(bank, i);
	}

	public void setElemDouble(int i, double val) {
		setElem(i, (int) val);
	}

	public void setElemDouble(int bank, int i, double val) {
		setElem(bank, i, (int) val);
	}
}