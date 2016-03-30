package java.util.concurrent.locks;

public class ReentrantReadWriteLock implements ReadWriteLock, java.io.Serializable {
	@Override
	public Lock readLock() {
		return new ReentrantLock(); // Dummy LOCK! Since not implemented!
	}

	@Override
	public Lock writeLock() {
		return new ReentrantLock(); // Dummy LOCK! Since not implemented!
	}
}
