package java.util.zip;

class ZStreamRef {
	private long address;

	ZStreamRef(long address) {
		this.address = address;
	}

	long address() {
		return address;
	}

	void clear() {
		address = 0;
	}
}
