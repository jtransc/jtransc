package java.lang.ref;

public class SoftReference<T> extends Reference<T> {
	static private long clock;

	private long timestamp;

	public SoftReference(T referent) {
		super(referent);
		this.timestamp = clock;
	}

	public SoftReference(T referent, ReferenceQueue<? super T> q) {
		super(referent, q);
		this.timestamp = clock;
	}

	public T get() {
		T o = super.get();
		if (o != null && this.timestamp != clock) this.timestamp = clock;
		return o;
	}
}
