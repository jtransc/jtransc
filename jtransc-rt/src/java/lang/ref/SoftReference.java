package java.lang.ref;

public class SoftReference<T> extends Reference<T> {
	private long time;

	static private long getTime() {
		return System.currentTimeMillis();
	}

	public SoftReference(T referent) {
		super(referent);
		this.time = getTime();
	}

	public SoftReference(T referent, ReferenceQueue<? super T> q) {
		super(referent, q);
		this.time = getTime();
	}

	public T get() {
		T o = super.get();
		long current = getTime();
		if (o != null && this.time != current) this.time = current;
		return o;
	}
}
